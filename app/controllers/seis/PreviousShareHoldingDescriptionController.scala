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

package controllers.seis

import controllers.Helpers.ControllerHelpers
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import auth.{AuthorisedAndEnrolledForTAVC, SEIS}
import common.KeystoreKeys
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.predicates.FeatureSwitch
import forms.PreviousShareHoldingDescriptionForm._
import models.investorDetails.PreviousShareHoldingDescriptionModel
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.seis.investors.PreviousShareHoldingDescription

import scala.concurrent.Future

object PreviousShareHoldingDescriptionController extends PreviousShareHoldingDescriptionController {
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector

}

trait PreviousShareHoldingDescriptionController extends FrontendController with AuthorisedAndEnrolledForTAVC with FeatureSwitch {

  override val acceptedFlows = Seq(Seq(SEIS))

  val PreviousShareHoldingDescription = previousShareHoldingDescriptionForm

  val show = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>
      def routeRequest(backUrl: Option[String]) = {
        if (backUrl.isDefined) {
          s4lConnector.fetchAndGetFormData[PreviousShareHoldingDescriptionModel](KeystoreKeys.previousShareHoldingDescription).map {
            case Some(data) => Ok(PreviousShareHoldingDescription(backUrl.get))
            case None => Ok(PreviousShareHoldingDescription(backUrl.get))

          }
        }
        else {
          Future.successful(Redirect(controllers.seis.routes.HadOtherInvestmentsController.show()))
          //Add redirect to Existing Shareholder Page.
        }
      }

      for {
        link <- ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkPreviousShareHoldingDescription, s4lConnector)
        route <- routeRequest(link)
      } yield route
    }
  }

  val submit = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>
      previousShareHoldingDescriptionForm.bindFromRequest().fold(
        formWithErrors => {
          ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkPreviousShareHoldingDescription, s4lConnector).flatMap(url =>
            Future.successful(BadRequest(PreviousShareHoldingDescription(formWithErrors,url.get))))
        },
        validFormData => {
          s4lConnector.saveFormData(KeystoreKeys.previousShareHoldingDescription, validFormData)
          Future.successful(Redirect(controllers.seis.routes.PreviousShareHoldingDescriptionController.show(Id: Int)))
          //Add redirect to When were these previous shares issued? Page.
        }
      )
    }
  }
}
