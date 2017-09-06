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
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.Helpers.ControllerHelpers
import forms.ShareIssueDateForm._
import models.ShareIssueDateModel
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.eis.companyDetails.ShareIssueDate
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future

object ShareIssueDateController extends ShareIssueDateController{
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait ShareIssueDateController extends FrontendController with AuthorisedAndEnrolledForTAVC {

  override val acceptedFlows = Seq(Seq(EIS))

  val show = AuthorisedAndEnrolled.async { implicit user => implicit request =>

        s4lConnector.fetchAndGetFormData[ShareIssueDateModel](KeystoreKeys.shareIssueDate).map {
          case Some(data) => Ok(ShareIssueDate(shareIssueDateForm.fill(data)))
          case None => Ok(ShareIssueDate(shareIssueDateForm))
        }
      }


  val submit = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    shareIssueDateForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(ShareIssueDate(formWithErrors)))
        },
      validFormData => {
        s4lConnector.saveFormData(KeystoreKeys.shareIssueDate, validFormData)
        Future.successful(Redirect(routes.GrossAssetsController.show()))
      }
    )
  }
}