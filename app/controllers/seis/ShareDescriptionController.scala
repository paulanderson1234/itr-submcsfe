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

import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import auth.{AuthorisedAndEnrolledForTAVC, SEIS}
import common.KeystoreKeys
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.predicates.FeatureSwitch
import forms.ShareDescriptionForm._
import models.ShareDescriptionModel
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.seis.companyDetails.ShareDescription

import scala.concurrent.Future


object ShareDescriptionController extends ShareDescriptionController {


  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector

}

trait ShareDescriptionController extends FrontendController with AuthorisedAndEnrolledForTAVC with FeatureSwitch {

  override val acceptedFlows = Seq(Seq(SEIS))

  val show = featureSwitch(applicationConfig.seisFlowEnabled) { AuthorisedAndEnrolled.async { implicit user => implicit request =>
    s4lConnector.fetchAndGetFormData[ShareDescriptionModel](KeystoreKeys.shareDescription).map {
      case Some(data) => Ok(ShareDescription(shareDescriptionForm.fill(data)))
      case None => Ok(ShareDescription(shareDescriptionForm))
      }
    }
  }

  val submit = featureSwitch(applicationConfig.seisFlowEnabled) { AuthorisedAndEnrolled.async { implicit user => implicit request =>
    shareDescriptionForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(ShareDescription(formWithErrors)))
      },
      validFormData => {
        s4lConnector.saveFormData(KeystoreKeys.shareDescription, validFormData)
        Future.successful(Redirect(controllers.seis.routes.ShareDescriptionController.show()))
      }
    )
  }
  }
}
