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
import forms.HasInvestmentTradeStartedForm._
import models.HasInvestmentTradeStartedModel
import play.Logger
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.eis.companyDetails.HasInvestmentTradeStarted

import scala.concurrent.Future

object HasInvestmentTradeStartedController extends HasInvestmentTradeStartedController{
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
  override lazy val submissionConnector = SubmissionConnector

}

trait HasInvestmentTradeStartedController extends FrontendController with AuthorisedAndEnrolledForTAVC {

  override val acceptedFlows = Seq(Seq(EIS))

  val submissionConnector: SubmissionConnector

  val show = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    s4lConnector.fetchAndGetFormData[HasInvestmentTradeStartedModel](KeystoreKeys.hasInvestmentTradeStarted).map {
      case Some(data) => Ok(HasInvestmentTradeStarted(hasInvestmentTradeStartedForm.fill(data)))
      case None => Ok(HasInvestmentTradeStarted(hasInvestmentTradeStartedForm))
    }
  }

  val submit = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    hasInvestmentTradeStartedForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(HasInvestmentTradeStarted(formWithErrors)))
      },
      validFormData => {
        s4lConnector.saveFormData(KeystoreKeys.hasInvestmentTradeStarted, validFormData)
        validFormData.hasInvestmentTradeStarted match {
          case Constants.StandardRadioButtonYesValue => {
            submissionConnector.validateHasInvestmentTradeStartedCondition(validFormData.hasInvestmentTradeStartedDay.get,
              validFormData.hasInvestmentTradeStartedMonth.get, validFormData.hasInvestmentTradeStartedYear.get).map {
              case Some(validated) =>
                if (validated) {
                  s4lConnector.saveFormData(KeystoreKeys.backLinkShareIssueDate,
                    routes.HasInvestmentTradeStartedController.show().url)

                  Redirect(routes.CommercialSaleController.show())
                }
                else {
                  s4lConnector.saveFormData(KeystoreKeys.backLinkSeventyPercentSpent, routes.HasInvestmentTradeStartedController.show().url)
                  //TODO Should route to Investment Trade start date error page once completed, if less than 4 months
                  Redirect(routes.HasInvestmentTradeStartedController.show())
                }
              case _ => {
                Logger.warn(s"[HasInvestmentTradeStartedController][submit] - Call to validate investment trade start date in backend failed")
                InternalServerError(internalServerErrorTemplate)
              }
            }.recover {
              case e: Exception => {
                Logger.warn(s"[HasInvestmentTradeStartedController][submit] - Exception: ${e.getMessage}")
                InternalServerError(internalServerErrorTemplate)
              }
            }
          }
          //TODO Should route to Investment Trade start date error page once completed, if no button selected
          case Constants.StandardRadioButtonNoValue => {
            s4lConnector.saveFormData(KeystoreKeys.backLinkSeventyPercentSpent, routes.HasInvestmentTradeStartedController.show().url)
            Future.successful(Redirect(routes.HasInvestmentTradeStartedController.show()))
          }
        }
      }
    )
  }
}
