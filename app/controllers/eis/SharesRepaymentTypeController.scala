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
import forms.SharesRepaymentTypeForm
import models.SharesRepaymentTypeModel
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import views.html.eis.investors.SharesRepaymentType

import scala.concurrent.Future

object SharesRepaymentTypeController extends SharesRepaymentTypeController
{
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait SharesRepaymentTypeController extends FrontendController with AuthorisedAndEnrolledForTAVC {

  override val acceptedFlows = Seq(Seq(EIS))

  val show = AuthorisedAndEnrolled.async {
    implicit user =>
      implicit request => {
        s4lConnector.fetchAndGetFormData[SharesRepaymentTypeModel](KeystoreKeys.sharesRepaymentType).map {
          case Some(data) => Ok(SharesRepaymentType(SharesRepaymentTypeForm.sharesRepaymentTypeForm.fill(data)))
          case None => Ok(SharesRepaymentType(SharesRepaymentTypeForm.sharesRepaymentTypeForm))
        }
      }
  }

  val submit = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    SharesRepaymentTypeForm.sharesRepaymentTypeForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(SharesRepaymentType(formWithErrors)))
      },
      validFormData => {
        s4lConnector.saveFormData(KeystoreKeys.sharesRepaymentType, validFormData)
        validFormData.sharesRepaymentType match {

          case Constants.repaymentTypeShares => {
            Future.successful(Redirect(routes.DateSharesRepaidController.show()))
          }
          case Constants.repaymentTypeDebentures => {
            Future.successful(Redirect(routes.WasAnyValueReceivedController.show()))
          }
        }

      }
    )
  }
}
