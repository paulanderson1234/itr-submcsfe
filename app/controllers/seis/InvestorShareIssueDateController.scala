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
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.Helpers.{ControllerHelpers, PreviousInvestorShareHoldersHelper}
import models.investorDetails.InvestorDetailsModel
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.seis.investors.InvestorShareIssueDate
import forms.InvestorShareIssueDateForm._

import scala.concurrent.Future

object InvestorShareIssueDateController extends InvestorShareIssueDateController {
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait InvestorShareIssueDateController extends FrontendController with AuthorisedAndEnrolledForTAVC with ControllerHelpers {

  override val acceptedFlows = Seq(Seq(SEIS))

  def show(investorProcessingId: Int, id: Int): Action[AnyContent] = AuthorisedAndEnrolled.async { implicit user =>
    implicit request =>

      def process(backUrl: Option[String]) = {
        if (backUrl.isDefined) {
          s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map { vector =>
            redirectNoInvestors(vector) { data =>
              redirectInvalidInvestor(getInvestorIndex(investorProcessingId, data)) { investorIdVal =>
                val shareHoldings = retrieveInvestorData(investorIdVal, data)(_.previousShareHoldingModels)
                redirectInvalidPreviousShareHolding(getShareIndex(id, shareHoldings.getOrElse(Vector.empty)),
                  investorProcessingId, shareHoldings) { shareHoldingsIndex =>
                  val form = fillForm(investorShareIssueDateForm, retrieveShareData(shareHoldingsIndex,
                    shareHoldings)(_.investorShareIssueDateModel))
                  Ok(InvestorShareIssueDate(form, backUrl.get, investorProcessingId))
                }
              }
            }
          }
        }
        else Future.successful(Redirect(controllers.seis.routes.AddInvestorOrNomineeController.show()))
      }

      for {
        backUrl <- s4lConnector.fetchAndGetFormData[String](KeystoreKeys.backLinkInvestorShareIssueDate)
        route <- process(backUrl)
      } yield route
  }

  def submit(investorProcessingId: Option[Int]): Action[AnyContent] = AuthorisedAndEnrolled.async { implicit user =>
    implicit request =>
      investorShareIssueDateForm.bindFromRequest().fold(
        formWithErrors => {
          ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkInvestorShareIssueDate, s4lConnector).flatMap(url =>
            Future.successful(BadRequest(InvestorShareIssueDate(formWithErrors, url.get, investorProcessingId.get))))
        },
        validFormData => {
          validFormData.processingId match {
            case Some(_) => PreviousInvestorShareHoldersHelper.updateInvestorShareIssueDate(s4lConnector, validFormData).map {
              data => {
                s4lConnector.saveFormData(KeystoreKeys.backLinkNumberOfPreviouslyIssuedShares,
                  routes.InvestorShareIssueDateController.show(data.investorProcessingId.get, data.processingId.get).url)
                Redirect(routes.NumberOfPreviouslyIssuedSharesController.show(data.investorProcessingId.get, data.processingId.get))
              }
            }
            case None => PreviousInvestorShareHoldersHelper.addInvestorShareIssueDate(s4lConnector, validFormData, investorProcessingId.get).map {
              data => {
                s4lConnector.saveFormData(KeystoreKeys.backLinkNumberOfPreviouslyIssuedShares,
                  routes.InvestorShareIssueDateController.show(data.investorProcessingId.get, data.processingId.get).url)
                Redirect(routes.NumberOfPreviouslyIssuedSharesController.show(data.investorProcessingId.get, data.processingId.get))
              }
            }
          }
        }
      )
  }
}