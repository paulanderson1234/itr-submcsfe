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

package controllers.seis

import auth.{AuthorisedAndEnrolledForTAVC, SEIS}
import common.KeystoreKeys
import config.{AppConfig, FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.Helpers.PreviousInvestorsHelper
import controllers.predicates.FeatureSwitch
import forms.HowMuchSpentOnSharesForm._
import forms.NumberOfSharesPurchasedForm.numberOfSharesPurchasedForm
import models.CompanyOrIndividualModel
import models.investorDetails.{HowMuchSpentOnSharesModel, InvestorDetailsModel}
import play.api.Play.current
import play.api.data.Form
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Action, AnyContent, Result}
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.seis.investors.HowMuchSpentOnShares

import scala.concurrent.Future

object HowMuchSpentOnSharesController extends HowMuchSpentOnSharesController {
  override lazy val enrolmentConnector: EnrolmentConnector = EnrolmentConnector
  override lazy val applicationConfig: AppConfig = FrontendAppConfig
  override lazy val s4lConnector: S4LConnector = S4LConnector
  override lazy val authConnector: AuthConnector = FrontendAuthConnector
}

trait HowMuchSpentOnSharesController extends FrontendController with AuthorisedAndEnrolledForTAVC with FeatureSwitch {

  override val acceptedFlows = Seq(Seq(SEIS))

  def show(id: Int): Action[AnyContent] = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user =>
      implicit request =>
        def process(backUrl: Option[String]) = {
          if (backUrl.isDefined) {

            s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map {
              case Some(data) => {
                val itemToUpdateIndex = data.indexWhere(_.processingId.getOrElse(0) == id)
                if (itemToUpdateIndex != -1) {
                  val model = data.lift(itemToUpdateIndex)
                  if (model.get.amountSpentModel.isDefined) {
                    Ok(HowMuchSpentOnShares(model.get.companyOrIndividualModel.get.companyOrIndividual,
                      howMuchSpentOnSharesForm.fill(model.get.amountSpentModel.get), backUrl.get))
                  }
                  else
                    Ok(HowMuchSpentOnShares(model.get.companyOrIndividualModel.get.companyOrIndividual,
                      howMuchSpentOnSharesForm, backUrl.get))
                }
                else Redirect(routes.AddInvestorOrNomineeController.show())
              }
              case None => Redirect(routes.AddInvestorOrNomineeController.show())
            }
          }
          else {
            // No back URL so send them back to any page as per the requirement
            Future.successful(Redirect(routes.AddInvestorOrNomineeController.show()))
          }
        }
        for {
          backUrl <- s4lConnector.fetchAndGetFormData[String](KeystoreKeys.backLinkHowMuchSpentOnShares)
          route <- process(backUrl)
        } yield route
    }
  }

  def submit(backUrl: Option[String]): Action[AnyContent] = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async {
      implicit user =>
        implicit request =>
          val success: HowMuchSpentOnSharesModel => Future[Result] = { validFormData =>
            validFormData.processingId match {
              case Some(_) => PreviousInvestorsHelper.updateAmountSpentOnSharesDetails(s4lConnector, validFormData).map {
                investorDetailsModel => {
                  s4lConnector.saveFormData(KeystoreKeys.backLinkIsExistingShareHolder,
                    routes.HowMuchSpentOnSharesController.show(investorDetailsModel.processingId.get).url)
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
            s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map {
              case Some(data) => {
                val investorDetailsModel = data.last
                BadRequest(HowMuchSpentOnShares(investorDetailsModel.companyOrIndividualModel.get.companyOrIndividual, formWithErrors, backUrl.get))
              }
            }
          }
          howMuchSpentOnSharesForm.bindFromRequest().fold(failure, success)
    }
  }
}

