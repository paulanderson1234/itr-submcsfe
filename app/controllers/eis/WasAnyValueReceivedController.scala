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
import forms.WasAnyValueReceivedForm._
import models.WasAnyValueReceivedModel
import play.api.Play.current
import play.api.data.Form
import play.api.i18n.Messages.Implicits._
import play.api.mvc.Result
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.eis.investors.WasAnyValueReceived

import scala.concurrent.Future

object WasAnyValueReceivedController extends WasAnyValueReceivedController {
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait WasAnyValueReceivedController extends FrontendController with AuthorisedAndEnrolledForTAVC {

  override val acceptedFlows = Seq(Seq(EIS))

  val show = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    def routeRequest(backUrl: Option[String]) = {
      if (backUrl.isDefined) {
        println("IT WAST FOUND=========================================================")
        s4lConnector.fetchAndGetFormData[WasAnyValueReceivedModel](KeystoreKeys.wasAnyValueReceived).map {
          case Some(data) => Ok(views.html.eis.investors.WasAnyValueReceived(wasAnyValueReceivedForm.fill(data), backUrl.getOrElse("")))
          case None => Ok(views.html.eis.investors.WasAnyValueReceived(wasAnyValueReceivedForm, backUrl.getOrElse("")))
        }
      }
      else {
        //TODO: Route to the beginning of flow as no backlink found
        println("NOT FOUND=========================================================")
        Future.successful(Redirect(routes.AnySharesRepaymentController.show()))
      }
    }

    for {
      link <- ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkWasAnyValueReceived, s4lConnector)
      route <- routeRequest(link)
    } yield route
  }

  val submit = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    val errorResponse: Form[WasAnyValueReceivedModel] => Future[Result] = form => {
      ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkWasAnyValueReceived, s4lConnector).flatMap(url =>
      Future.successful(BadRequest(WasAnyValueReceived(form, url.get))))
    }

    val successResponse: WasAnyValueReceivedModel => Future[Result] = model =>
      s4lConnector.saveFormData(KeystoreKeys.wasAnyValueReceived,
        if (model.wasAnyValueReceived == Constants.StandardRadioButtonYesValue) model else model.copy(aboutValueReceived = None)).map { _ =>
        Redirect(controllers.eis.routes.ShareCapitalChangesController.show())
      }
    wasAnyValueReceivedForm.bindFromRequest().fold(errorResponse, successResponse)
  }

}