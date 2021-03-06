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

import auth.{AuthorisedAndEnrolledForTAVC, EIS, VCT}
import common.KeystoreKeys
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector, SubmissionConnector}
import controllers.Helpers.{ControllerHelpers, PreviousSchemesHelper}
import models.HadPreviousRFIModel
import play.api.mvc.{Action, AnyContent, _}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.eis.previousInvestment.ReviewPreviousSchemes
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future

object ReviewPreviousSchemesController extends ReviewPreviousSchemesController {
  override lazy val s4lConnector = S4LConnector
  override lazy val submissionConnector: SubmissionConnector = SubmissionConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait ReviewPreviousSchemesController extends FrontendController with AuthorisedAndEnrolledForTAVC with PreviousSchemesHelper {

  override val acceptedFlows = Seq(Seq(EIS))
  val submissionConnector: SubmissionConnector

  val show: Action[AnyContent] = AuthorisedAndEnrolled.async { implicit user =>
    implicit request =>
      PreviousSchemesHelper.getAllInvestmentFromKeystore(s4lConnector).flatMap {
        previousSchemes =>
          if (previousSchemes.nonEmpty) {
            Future.successful(Ok(ReviewPreviousSchemes(previousSchemes)))
          }
          else Future.successful(Redirect(routes.HadPreviousRFIController.show()))
      }
  }


  def add: Action[AnyContent] = AuthorisedAndEnrolled.async { implicit user =>
    implicit request =>
      s4lConnector.saveFormData(KeystoreKeys.backLinkPreviousScheme, routes.ReviewPreviousSchemesController.show().url)
      Future.successful(Redirect(routes.PreviousSchemeController.show(None)))
  }

  def change(id: Int): Action[AnyContent] = AuthorisedAndEnrolled.async { implicit user =>
    implicit request =>
      s4lConnector.saveFormData(KeystoreKeys.backLinkPreviousScheme, routes.ReviewPreviousSchemesController.show().url)
      Future.successful(Redirect(routes.PreviousSchemeController.show(Some(id))))
  }

  def remove(id: Int): Action[AnyContent] = AuthorisedAndEnrolled.async { implicit user =>
    implicit request =>
      Future.successful(Redirect(routes.DeletePreviousSchemeController.show(id)))
  }

  val submit: Action[AnyContent] = AuthorisedAndEnrolled.async { implicit user =>
    implicit request =>

      def routeRequest(previousSchemesExist: Boolean): Future[Result] = {
        if (!previousSchemesExist) {
          Future.successful(Redirect(routes.ReviewPreviousSchemesController.show()))
        }
        else {
          Future.successful(Redirect(routes.ShareDescriptionController.show()))
        }
      }

      s4lConnector.saveFormData(KeystoreKeys.backLinkShareDescription, routes.ReviewPreviousSchemesController.show().url)
      for {
        previousSchemesExist <- PreviousSchemesHelper.previousInvestmentsExist(s4lConnector)
        hadPrevRFI <- s4lConnector.fetchAndGetFormData[HadPreviousRFIModel](KeystoreKeys.hadPreviousRFI)
        route <- routeRequest(previousSchemesExist)
      } yield route
  }
}
