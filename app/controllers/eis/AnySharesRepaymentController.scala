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
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.Helpers.ControllerHelpers
import forms.AnySharesRepaymentForm._
import models.repayments.{AnySharesRepaymentModel, SharesRepaymentDetailsModel}
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.eis.investors.AnySharesRepayment

import scala.concurrent.Future


object AnySharesRepaymentController extends AnySharesRepaymentController{
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait AnySharesRepaymentController extends FrontendController with AuthorisedAndEnrolledForTAVC with ControllerHelpers{

  override val acceptedFlows = Seq(Seq(EIS))

  val show = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    s4lConnector.fetchAndGetFormData[AnySharesRepaymentModel](KeystoreKeys.anySharesRepayment).map {
      case Some(data) => Ok(AnySharesRepayment(anySharesRepaymentForm.fill(data)))
      case None => Ok(AnySharesRepayment(anySharesRepaymentForm))
    }
  }

  val submit = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    anySharesRepaymentForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(AnySharesRepayment(formWithErrors)))
      },
      validFormData => {
        s4lConnector.saveFormData(KeystoreKeys.anySharesRepayment, validFormData)
        validFormData.anySharesRepayment match {
          case Constants.StandardRadioButtonYesValue =>
            s4lConnector.fetchAndGetFormData[Vector[SharesRepaymentDetailsModel]](KeystoreKeys.sharesRepaymentDetails).map {
              case Some(data) if (data.nonEmpty) => Redirect(routes.ReviewPreviousRepaymentsController.show())
              case _ => s4lConnector.saveFormData(KeystoreKeys.backLinkWhoRepaidShares, routes.AnySharesRepaymentController.show().url)
                Redirect(routes.WhoRepaidSharesController.show())
            }
          case Constants.StandardRadioButtonNoValue =>
            s4lConnector.fetchAndGetFormData[Vector[SharesRepaymentDetailsModel]](KeystoreKeys.sharesRepaymentDetails).map {
              case Some(data) if (data.nonEmpty) => Redirect(routes.ReviewPreviousRepaymentsController.show())
              case _ => s4lConnector.saveFormData(KeystoreKeys.backLinkWasAnyValueReceived, routes.AnySharesRepaymentController.show().url)
                Redirect(routes.WasAnyValueReceivedController.show())
            }
        }
      }
    )
  }
}
