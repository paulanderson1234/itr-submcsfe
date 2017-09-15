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
import config.FrontendGlobal._
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector, SubmissionConnector}
import controllers.Helpers.ControllerHelpers
import forms.GrossAssetsAfterIssueForm._
import models.{DateOfIncorporationModel, GrossAssetsAfterIssueModel}
import play.api.Logger
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc._
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.eis.companyDetails.GrossAssetsAfterIssue

import scala.concurrent.Future

object GrossAssetsAfterIssueController extends GrossAssetsAfterIssueController {
  override lazy val s4lConnector = S4LConnector
  val submissionConnector: SubmissionConnector = SubmissionConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait GrossAssetsAfterIssueController extends FrontendController with AuthorisedAndEnrolledForTAVC {

  override val acceptedFlows = Seq(Seq(EIS))

  val submissionConnector: SubmissionConnector

  val show = AuthorisedAndEnrolled.async { implicit user => implicit request =>

    s4lConnector.fetchAndGetFormData[GrossAssetsAfterIssueModel](KeystoreKeys.grossAssetsAfterIssue).map {
      case Some(data) => Ok(GrossAssetsAfterIssue(grossAssetsAfterIssueForm.fill(data)))
      case None => Ok(GrossAssetsAfterIssue(grossAssetsAfterIssueForm))
    }
  }

  def submit: Action[AnyContent] = AuthorisedAndEnrolled.async { implicit user => implicit request =>

    def routeRequest(grossAssetsAfterIssueExceeded: Option[Boolean],
                     dateOfIncorporationModel: Option[DateOfIncorporationModel]): Future[Result] = {

      grossAssetsAfterIssueExceeded match {
        case Some(false) => Future.successful(ControllerHelpers.redirectGrossAssetsAfterIssue(dateOfIncorporationModel, s4lConnector))
        case _ => Future.successful(Redirect(routes.GrossAssetsAfterIssueErrorController.show()))
      }
    }

    grossAssetsAfterIssueForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(GrossAssetsAfterIssue(formWithErrors)))
      },
      validFormData => {
        s4lConnector.saveFormData(KeystoreKeys.grossAssetsAfterIssue, validFormData)
        (for {
          grossAssetsAfterIssueExceeded <- submissionConnector.checkGrossAssetsAfterIssueAmountExceeded(validFormData.grossAmount.toIntExact)
          dateOfIncorporationModel <- s4lConnector.fetchAndGetFormData[DateOfIncorporationModel](KeystoreKeys.dateOfIncorporation)
          route <- routeRequest(grossAssetsAfterIssueExceeded, dateOfIncorporationModel)
        } yield route) recover {
          case e: Exception => {
            Logger.warn(s"[GrossAssetsAfterIssueController][submit] - submit Exception: ${e.getMessage}")
            InternalServerError(internalServerErrorTemplate)
          }
        }
      }
    )
  }
}
