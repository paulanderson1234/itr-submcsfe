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
import forms.ShareIssueDateForm._
import models._
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

    def routeRequest(qualifyingBusinessDay: Int, qualifyingBusinessMonth: Int, qualifyingBusinessYear: Int, shareIssueDate: ShareIssueDateModel) = {

      submissionConnector.validateSubmissionPeriod(qualifyingBusinessDay, qualifyingBusinessMonth, qualifyingBusinessYear,
                                                   shareIssueDate.day.get, shareIssueDate.month.get, shareIssueDate.year.get) map {
        case canProceed => if (canProceed) Redirect(routes.GrossAssetsController.show())
                           else Redirect(routes.ShareIssueDateErrorController.show())
      }
    }

    def getQualifyingBusinessType(shareIssueDateModel: ShareIssueDateModel, qualifyBusinessActivityModel: QualifyBusinessActivityModel) = {
      qualifyBusinessActivityModel.isQualifyBusinessActivity match {
        case Constants.qualifyPrepareToTrade =>   {
          s4lConnector.fetchAndGetFormData[HasInvestmentTradeStartedModel](KeystoreKeys.hasInvestmentTradeStarted) flatMap {
            case Some(data) => if (data.hasDate) routeRequest(data.hasInvestmentTradeStartedDay.get, data.hasInvestmentTradeStartedMonth.get,
              data.hasInvestmentTradeStartedYear.get, shareIssueDateModel)
            else Future.successful(Redirect(routes.HasInvestmentTradeStartedController.show()))
            case None => Future.successful(Redirect(routes.HasInvestmentTradeStartedController.show()))
          }
        }
        case Constants.qualifyResearchAndDevelopment =>   {
          s4lConnector.fetchAndGetFormData[ResearchStartDateModel](KeystoreKeys.researchStartDate) flatMap {
            case Some(data) => if (data.hasDate) routeRequest(data.researchStartDay.get, data.researchStartMonth.get,
              data.researchStartYear.get, shareIssueDateModel)
            else Future.successful(Redirect(routes.ResearchStartDateController.show()))
            case None => Future.successful(Redirect(routes.ResearchStartDateController.show()))
          }
        }
      }
    }

    shareIssueDateForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(ShareIssueDate(formWithErrors)))
      },
      validFormData => {
        s4lConnector.saveFormData(KeystoreKeys.shareIssueDate, validFormData)
        (for{
          businessActivity <-  s4lConnector.fetchAndGetFormData[QualifyBusinessActivityModel](KeystoreKeys.isQualifyBusinessActivity)
          result <- getQualifyingBusinessType(validFormData, businessActivity.get)
        } yield result) recover {
          case e: NoSuchElementException => Redirect(routes.QualifyBusinessActivityController.show())
          case e: Exception => {
            Logger.warn(s"[ShareIssueDateController][submit] - Submit Exception: ${e.getMessage}")
            InternalServerError(internalServerErrorTemplate)
          }
        }
      }
    )
  }
}