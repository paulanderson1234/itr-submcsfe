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
import connectors.{EnrolmentConnector, S4LConnector, SubmissionConnector}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.mvc._
import controllers.predicates.FeatureSwitch
import forms.GrossAssetsForm._
import models.GrossAssetsModel
import play.api.Logger
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import views.html.seis.companyDetails.GrossAssets
import config.FrontendGlobal._

import scala.concurrent.Future

object GrossAssetsController extends GrossAssetsController {
  override lazy val s4lConnector = S4LConnector
  val submissionConnector: SubmissionConnector = SubmissionConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait GrossAssetsController extends FrontendController with AuthorisedAndEnrolledForTAVC with FeatureSwitch {

  override val acceptedFlows = Seq(Seq(SEIS))

  val submissionConnector: SubmissionConnector

  val show: Action[AnyContent] = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>

      s4lConnector.fetchAndGetFormData[GrossAssetsModel](KeystoreKeys.grossAssets).map {
        case Some(data) => Ok(GrossAssets(grossAssetsForm.fill(data)))
        case None => Ok(GrossAssets(grossAssetsForm))
      }
    }
  }

  def submit: Action[AnyContent] = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>

      def routeRequest(grossAssetsExceeded: Option[Boolean]): Future[Result] = {
        if (grossAssetsExceeded.nonEmpty) {
          grossAssetsExceeded match {
            case Some(false) => Future.successful(Redirect(routes.FullTimeEmployeeCountController.show()))
            case _ => Future.successful(Redirect(routes.GrossAssetsErrorController.show()))
          }
        }
        else {
          // no expected true/false from service returned
          Future.successful(InternalServerError(internalServerErrorTemplate))
        }
      }

      grossAssetsForm.bindFromRequest().fold(
        formWithErrors => {
          Future.successful(BadRequest(GrossAssets(formWithErrors)))
        },
        validFormData => {
          s4lConnector.saveFormData(KeystoreKeys.grossAssets, validFormData)
          (for {
            grossAssetsExceeded <- submissionConnector.checkGrossAssetsAmountExceeded(validFormData)
            route <- routeRequest(grossAssetsExceeded)
          } yield route) recover {
            case e: Exception => {
              Logger.warn(s"[GrossAssetsController][submit] - submit Exception: ${e.getMessage}")
              InternalServerError(internalServerErrorTemplate)
            }
          }
        }
      )
    }
  }
}
