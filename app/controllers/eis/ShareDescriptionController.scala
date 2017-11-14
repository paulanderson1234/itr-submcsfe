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
import models.{ShareDescriptionModel, ShareIssueDateModel}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import utils.DateFormatter
import views.html.eis.shareDetails.ShareDescription

import scala.concurrent.Future

object ShareDescriptionController extends ShareDescriptionController {
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector

}

trait ShareDescriptionController extends FrontendController with AuthorisedAndEnrolledForTAVC with DateFormatter{

  override val acceptedFlows = Seq(Seq(EIS))

  val show = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    def routeRequest(backUrl: Option[String], shareIssueDate:Option[ShareIssueDateModel]) = {
      if (backUrl.isDefined) {
        if(shareIssueDate.isDefined) {
          val date = shareIssueDate.get
          s4lConnector.fetchAndGetFormData[ShareDescriptionModel](KeystoreKeys.shareDescription).map {
            case Some(data) => Ok(ShareDescription(shareDescriptionForm.fill(data),
              backUrl.get, dateToStringWithNoZeroDay(date.day.get, date.month.get, date.year.get)))
            case None => Ok(ShareDescription(shareDescriptionForm, backUrl.get,
              dateToStringWithNoZeroDay(date.day.get, date.month.get, date.year.get)))
          }
        }
        else
        {
          Future.successful(Redirect(controllers.eis.routes.ShareIssueDateController.show()))
        }
      }
      else {
        Future.successful(Redirect(controllers.eis.routes.HadOtherInvestmentsController.show()))
      }
    }

    for {
      link <- ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkShareDescription, s4lConnector)
      shareIssueDate <- s4lConnector.fetchAndGetFormData[ShareIssueDateModel](KeystoreKeys.shareIssueDate)
      route <- routeRequest(link, shareIssueDate)
    } yield route
  }



  def submit(shareIssueDate:String): Action[AnyContent] = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    shareDescriptionForm.bindFromRequest().fold(
      formWithErrors => {
        ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkShareDescription, s4lConnector).flatMap(url =>
          Future.successful(BadRequest(ShareDescription(formWithErrors, url.get, shareIssueDate))))
      },
      validFormData => {
        s4lConnector.saveFormData(KeystoreKeys.shareDescription, validFormData)
        Future.successful(Redirect(controllers.eis.routes.NumberOfSharesController.show()))
      }
    )
  }

}