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
import forms.ResearchStartDateForm._
import models.ResearchStartDateModel
import play.Logger
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.eis.companyDetails.ResearchStartDate

import scala.concurrent.Future

object ResearchStartDateController extends ResearchStartDateController{
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
  override lazy val submissionConnector = SubmissionConnector

}

trait ResearchStartDateController extends FrontendController with AuthorisedAndEnrolledForTAVC {

  override val acceptedFlows = Seq(Seq(EIS))

  val submissionConnector: SubmissionConnector

  val show = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    s4lConnector.fetchAndGetFormData[ResearchStartDateModel](KeystoreKeys.researchStartDate).map {
      case Some(data) => Ok(ResearchStartDate(researchStartDateForm.fill(data)))
      case _ => Ok(ResearchStartDate(researchStartDateForm))
    }
  }

  val submit = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    researchStartDateForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(ResearchStartDate(formWithErrors)))
      },
      validFormData => {
        s4lConnector.saveFormData(KeystoreKeys.researchStartDate, validFormData)
        validFormData.hasStartedResearch match {
          case Constants.StandardRadioButtonYesValue => {
            submissionConnector.validateHasInvestmentTradeStartedCondition(validFormData.researchStartDay.get,
              validFormData.researchStartMonth.get, validFormData.researchStartYear.get).map {
              case Some(validated) =>
                if (validated) {
                  s4lConnector.saveFormData(KeystoreKeys.backLinkShareIssueDate,
                    routes.ResearchStartDateController.show().url)

                  Redirect(routes.CommercialSaleController.show())
                }
                else {
                  s4lConnector.saveFormData(KeystoreKeys.backLinkSeventyPercentSpent, routes.ResearchStartDateController.show().url)
                  //TODO Should route to Research start date error page once completed, if less than 4 months
                  Redirect(routes.ResearchStartDateController.show())
                }
              case _ => {
                Logger.warn(s"[ResearchStartDateController][submit] - Call to validate investment trade start date in backend failed")
                InternalServerError(internalServerErrorTemplate)
              }
            }.recover {
              case e: Exception => {
                Logger.warn(s"[ResearchStartDateController][submit] - Exception: ${e.getMessage}")
                InternalServerError(internalServerErrorTemplate)
              }
            }
          }
          //TODO Should route to Research start date error page once completed, if no button selected
          case Constants.StandardRadioButtonNoValue => {
            s4lConnector.saveFormData(KeystoreKeys.backLinkSeventyPercentSpent, routes.ResearchStartDateController.show().url)
            Future.successful(Redirect(routes.ResearchStartDateController.show()))
          }
        }
      }
    )
  }
}
