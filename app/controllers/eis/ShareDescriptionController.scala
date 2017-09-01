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

import controllers.Helpers.ControllerHelpers
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import auth.{AuthorisedAndEnrolledForTAVC, EIS}
import common.KeystoreKeys
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import forms.ShareDescriptionForm._
import models.ShareDescriptionModel
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.eis.shareDetails.ShareDescription

import scala.concurrent.Future

object ShareDescriptionController extends ShareDescriptionController {
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector

}

trait ShareDescriptionController extends FrontendController with AuthorisedAndEnrolledForTAVC {

  override val acceptedFlows = Seq(Seq(EIS))

  val show = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    def routeRequest(backUrl: Option[String]) = {
      if (backUrl.isDefined) {
        s4lConnector.fetchAndGetFormData[ShareDescriptionModel](KeystoreKeys.shareDescription).map {
          case Some(data) => Ok(ShareDescription(shareDescriptionForm.fill(data), backUrl.get))
          case None => Ok(ShareDescription(shareDescriptionForm, backUrl.get))
        }
      }
      else {
        Future.successful(Redirect(controllers.eis.routes.HadOtherInvestmentsController.show()))
      }
    }

    for {
      link <- ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkShareDescription, s4lConnector)
      route <- routeRequest(link)
    } yield route
  }

  val submit = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    shareDescriptionForm.bindFromRequest().fold(
      formWithErrors => {
        ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkShareDescription, s4lConnector).flatMap(url =>
          Future.successful(BadRequest(ShareDescription(formWithErrors, url.get))))
      },
      validFormData => {
        s4lConnector.saveFormData(KeystoreKeys.shareDescription, validFormData)
        Future.successful(Redirect(controllers.eis.routes.NumberOfSharesController.show()))
      }
    )
  }
}