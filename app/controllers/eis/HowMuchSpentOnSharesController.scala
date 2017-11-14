/*
 * Copyright 2017 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.eis

import auth.{AuthorisedAndEnrolledForTAVC, EIS}
import common.KeystoreKeys
import config.{AppConfig, FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.Helpers.{ControllerHelpers, PreviousInvestorsHelper}
import forms.HowMuchSpentOnSharesForm._
import models.investorDetails.{HowMuchSpentOnSharesModel, InvestorDetailsModel}
import play.api.Play.current
import play.api.data.Form
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Action, AnyContent, Result}
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.eis.investors.HowMuchSpentOnShares

import scala.concurrent.Future

object HowMuchSpentOnSharesController extends HowMuchSpentOnSharesController {
  override lazy val enrolmentConnector: EnrolmentConnector = EnrolmentConnector
  override lazy val applicationConfig: AppConfig = FrontendAppConfig
  override lazy val s4lConnector: S4LConnector = S4LConnector
  override lazy val authConnector: AuthConnector = FrontendAuthConnector
}

trait HowMuchSpentOnSharesController extends FrontendController with AuthorisedAndEnrolledForTAVC with ControllerHelpers {

  override val acceptedFlows = Seq(Seq(EIS))

  def show(id: Int): Action[AnyContent] = AuthorisedAndEnrolled.async { implicit user =>
    implicit request =>
      def process(backUrl: Option[String]) = {
        if (backUrl.isDefined) {
          s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map { vector =>
            redirectEisNoInvestors(vector) { data =>
              val itemToUpdateIndex = getInvestorIndex(id, data)
              redirectEisInvalidInvestor(itemToUpdateIndex) { index =>
                if (data.lift(itemToUpdateIndex).get.companyOrIndividualModel.isDefined) {
                  val form = fillForm(howMuchSpentOnSharesForm, retrieveInvestorData[HowMuchSpentOnSharesModel](index, data)(_.amountSpentModel))
                  Ok(HowMuchSpentOnShares(retrieveInvestorData[String](index, data)(_.companyOrIndividualModel.map(_.companyOrIndividual)).get,
                    form, backUrl.get))
                }
                else Redirect(routes.CompanyOrIndividualController.show(id))
              }
            }
          }
        } else {
          // No back URL so send them back to any page as per the requirement
          Future.successful(Redirect(controllers.eis.routes.AddInvestorOrNomineeController.show()))
        }
      }

      for {
        backUrl <- s4lConnector.fetchAndGetFormData[String](KeystoreKeys.backLinkHowMuchSpentOnShares)
        route <- process(backUrl)
      } yield route
  }

  val submit = AuthorisedAndEnrolled.async {
    implicit user =>
      implicit request =>
        val success: HowMuchSpentOnSharesModel => Future[Result] = { validFormData =>
          validFormData.processingId match {
            case Some(_) => PreviousInvestorsHelper.updateAmountSpentOnSharesDetails(s4lConnector, validFormData).map {
              investorDetailsModel => {
                s4lConnector.saveFormData(KeystoreKeys.backLinkIsExistingShareHolder,
                  routes.HowMuchSpentOnSharesController.show(investorDetailsModel.processingId.get).url)
                if (investorDetailsModel.previousShareHoldingModels.isDefined &&
                  investorDetailsModel.previousShareHoldingModels.get.nonEmpty)
                  Redirect(routes.PreviousShareHoldingsReviewController.show(investorDetailsModel.processingId.get))
                else
                  Redirect(routes.IsExistingShareHolderController.show(investorDetailsModel.processingId.get))
              }
            }
            case None => PreviousInvestorsHelper.addAmountSpentOnSharesDetails(s4lConnector, validFormData).map {
              investorDetailsModel => {
                s4lConnector.saveFormData(KeystoreKeys.backLinkIsExistingShareHolder,
                  routes.HowMuchSpentOnSharesController.show(investorDetailsModel.processingId.get).url)
                Redirect(routes.IsExistingShareHolderController.show(investorDetailsModel.processingId.get))
              }
            }
          }
        }

        val failure: Form[HowMuchSpentOnSharesModel] => Future[Result] = { formWithErrors =>
          ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkHowMuchSpentOnShares, s4lConnector).flatMap(url =>
            s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map {
              case Some(data) => {
                val investorDetailsModel = data.last
                BadRequest(HowMuchSpentOnShares(investorDetailsModel.companyOrIndividualModel.get.companyOrIndividual, formWithErrors, url.get))
              }
            })
        }
        howMuchSpentOnSharesForm.bindFromRequest().fold(failure, success)
  }
}

