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

import auth.{AuthorisedAndEnrolledForTAVC, SEIS}
import common.KeystoreKeys
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.predicates.FeatureSwitch
import forms.WasAnyValueReceivedForm._
import models.WasAnyValueReceivedModel
import play.api.Play.current
import play.api.data.Form
import play.api.i18n.Messages.Implicits._
import play.api.mvc.Result
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.Future

object WasAnyValueReceivedController extends WasAnyValueReceivedController {
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait WasAnyValueReceivedController extends FrontendController with AuthorisedAndEnrolledForTAVC with FeatureSwitch {

  override val acceptedFlows = Seq(Seq(SEIS))

  val show = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>
      s4lConnector.fetchAndGetFormData[WasAnyValueReceivedModel](KeystoreKeys.wasAnyValueReceived).map {
        case Some(data) => Ok(views.html.seis.investors.WasAnyValueReceived(wasAnyValueReceivedForm.fill(data)))
        case None => Ok(views.html.seis.investors.WasAnyValueReceived(wasAnyValueReceivedForm))
      }
    }
  }

  val submit = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>
      val errorResponse: Form[WasAnyValueReceivedModel] => Future[Result] = form =>
        Future(BadRequest(views.html.seis.investors.WasAnyValueReceived(form)))

      val successResponse: WasAnyValueReceivedModel => Future[Result] = model =>
        s4lConnector.saveFormData(KeystoreKeys.wasAnyValueReceived, model).map { _ =>
          Redirect(controllers.seis.routes.WasAnyValueReceivedController.show())
        }
      wasAnyValueReceivedForm.bindFromRequest().fold(errorResponse, successResponse)
    }
  }

}

