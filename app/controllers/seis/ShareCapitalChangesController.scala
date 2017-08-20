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
import common.{Constants, KeystoreKeys}
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector, SubmissionConnector}
import controllers.predicates.FeatureSwitch
import forms.ShareCapitalChangesForm._
import models.{ShareCapitalChangesModel, ShareIssueDateModel}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.mvc.{Action, AnyContent, Result}
import views.html.seis.investors.ShareCapitalChanges
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.data.Form
import utils.DateFormatter

import scala.concurrent.Future

object ShareCapitalChangesController extends ShareCapitalChangesController {
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait ShareCapitalChangesController extends FrontendController with AuthorisedAndEnrolledForTAVC with FeatureSwitch with DateFormatter {

  override val acceptedFlows = Seq(Seq(SEIS))

  val show = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>
      def routeRequest(shareIssueDate: Option[ShareIssueDateModel]) = {
        if (shareIssueDate.isDefined) {
          val date = dateToStringWithNoZeroDay(shareIssueDate.get.day.get, shareIssueDate.get.month.get, shareIssueDate.get.year.get)
          s4lConnector.fetchAndGetFormData[ShareCapitalChangesModel](KeystoreKeys.shareCapitalChanges).map {
            case Some(data) => Ok(ShareCapitalChanges(shareCapitalChangesForm.fill(data), date))
            case None => Ok(ShareCapitalChanges(shareCapitalChangesForm, date))
          }
        }
        else {
          //TODO: Route to the beginning of flow as no backlink found
          Future.successful(Redirect(routes.ShareIssueDateController.show()))
        }
      }

      for {
        shareIssueDate <- s4lConnector.fetchAndGetFormData[ShareIssueDateModel](KeystoreKeys.shareIssueDate)
        route <- routeRequest(shareIssueDate)
      } yield route
    }
  }


  val submit: Action[AnyContent] = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async {
      implicit user =>
        implicit request =>
          val success: ShareCapitalChangesModel => Future[Result] = { model =>
            s4lConnector.saveFormData(KeystoreKeys.shareCapitalChanges,
              if(model.hasChanges == Constants.StandardRadioButtonYesValue) model else model.copy(changesDescription = None)).map(_ =>
              Redirect(controllers.seis.routes.ConfirmContactDetailsController.show())
            )
          }

          val failure: Form[ShareCapitalChangesModel] => Future[Result] = { form =>
            s4lConnector.fetchAndGetFormData[ShareIssueDateModel](KeystoreKeys.shareIssueDate).flatMap {
              case backUrl => Future.successful(BadRequest(ShareCapitalChanges(form, backUrl.fold("")( shareIssueDate =>
                dateToStringWithNoZeroDay(shareIssueDate.day.get, shareIssueDate.month.get, shareIssueDate.year.get)))))
            }
          }
          shareCapitalChangesForm.bindFromRequest().fold(failure, success)
    }
  }

}
