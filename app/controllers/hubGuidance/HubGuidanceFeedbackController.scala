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

package controllers.hubGuidance

import auth.AuthorisedAndEnrolledForTAVC
import common.KeystoreKeys
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.eis
import controllers.schemeSelection.SingleSchemeSelectionController
import models.submission.SchemeTypesModel
import play.Logger
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.hubGuidance.HubGuidanceFeedback

import scala.concurrent.Future

object HubGuidanceFeedbackController extends HubGuidanceFeedbackController
{
  override lazy val s4lConnector: S4LConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait HubGuidanceFeedbackController extends FrontendController with AuthorisedAndEnrolledForTAVC {

  override val acceptedFlows = Seq()

  val show: Action[AnyContent] = AuthorisedAndEnrolled.async { implicit user =>
    implicit request =>
      Future.successful(Ok(HubGuidanceFeedback()))
  }

  val submit:Action[AnyContent]  = AuthorisedAndEnrolled.async { implicit user =>
    implicit request =>

      Future.successful(Redirect(controllers.schemeSelection.routes.SingleSchemeSelectionController.show()))

      // TODO: Temporary fix. Store the fact we are starting an application and set the type to SESI
      // when scheme types page is incorporated that page will do this work
//      if (applicationConfig.seisFlowEnabled) {
//        s4lConnector.saveFormData(KeystoreKeys.selectedSchemes,
//          SchemeTypesModel(seis = true))
//        s4lConnector.saveFormData(KeystoreKeys.applicationInProgress, true)
//        Future.successful(Redirect(controllers.seis.routes.NatureOfBusinessController.show()))
//      }
//      else {
//        //TODO: go to 9635 hub on sub FE - how are we going to show them the app in progress and option to delete
//        // this app in progress? Presumably we need the SUB FE Hub to call this repo to know if user has one in progress
//        Future.successful(Redirect(controllers.routes.ApplicationHubController.show()))
//      }
  }

}