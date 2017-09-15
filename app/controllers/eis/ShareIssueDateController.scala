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
import common.{Constants, KeystoreKeys}
import config.FrontendGlobal.internalServerErrorTemplate
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector, SubmissionConnector}
import controllers.Helpers.ControllerHelpers
import forms.ShareIssueDateForm._
import models.{ShareIssueDateModel, TradeStartDateModel}
import play.api.Logger
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.eis.companyDetails.ShareIssueDate
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.mvc.Result

import scala.concurrent.Future

object ShareIssueDateController extends ShareIssueDateController{
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
  override lazy val submissionConnector = SubmissionConnector
}

trait ShareIssueDateController extends FrontendController with AuthorisedAndEnrolledForTAVC {

  override val acceptedFlows = Seq(Seq(EIS))
  val submissionConnector: SubmissionConnector

  val show = AuthorisedAndEnrolled.async { implicit user => implicit request =>

        s4lConnector.fetchAndGetFormData[ShareIssueDateModel](KeystoreKeys.shareIssueDate).map {
          case Some(data) => Ok(ShareIssueDate(shareIssueDateForm.fill(data)))
          case None => Ok(ShareIssueDate(shareIssueDateForm))
        }
      }


  val submit = AuthorisedAndEnrolled.async { implicit user => implicit request =>

    def routeRequest(tradeStartDate: TradeStartDateModel, shareIssueDate: ShareIssueDateModel) = {

      submissionConnector.validateSubmissionPeriod(tradeStartDate.tradeStartDay.get, tradeStartDate.tradeStartMonth.get, tradeStartDate.tradeStartYear.get,
        shareIssueDate.day.get, shareIssueDate.month.get, shareIssueDate.year.get) map {
        case canProceed => if (canProceed) Redirect(routes.GrossAssetsController.show())
        else Redirect(routes.ShareIssueDateErrorController.show())
      }

    }

    shareIssueDateForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(ShareIssueDate(formWithErrors)))
      },

      validFormData => {
        s4lConnector.saveFormData(KeystoreKeys.shareIssueDate, validFormData)

        s4lConnector.fetchAndGetFormData[TradeStartDateModel](KeystoreKeys.tradeStartDate) flatMap {
          case Some(data) => if(data.hasTradeStartDate == Constants.StandardRadioButtonYesValue)
                                routeRequest(data, validFormData)
                             else Future.successful(Redirect(routes.HasInvestmentTradeStartedController.show()))
          case None => Future.successful(Redirect(routes.HasInvestmentTradeStartedController.show()))
        }
      }
    )
  }
}