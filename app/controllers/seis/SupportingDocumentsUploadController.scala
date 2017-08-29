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
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.Helpers.ControllerHelpers
import forms.SupportingDocumentsUploadForm.supportingDocumentsUploadForm
import models.SupportingDocumentsUploadModel
import services.FileUploadService
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.seis.supportingDocuments.SupportingDocumentsUpload
import config.FrontendGlobal.notFoundTemplate
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import controllers.predicates.FeatureSwitch

import scala.concurrent.Future

object SupportingDocumentsUploadController extends SupportingDocumentsUploadController
{
  override lazy val s4lConnector = S4LConnector
  val attachmentsFrontEndUrl = applicationConfig.attachmentFileUploadUrl(Constants.schemeTypeSeis.toLowerCase)
  val fileUploadService: FileUploadService = FileUploadService
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait SupportingDocumentsUploadController extends FrontendController with AuthorisedAndEnrolledForTAVC with FeatureSwitch {

  override val acceptedFlows = Seq(Seq(SEIS))


  val attachmentsFrontEndUrl: String
  val fileUploadService: FileUploadService

  val show = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>
      def routeRequest(backUrl: Option[String]) = {

        //TODO: this enforces the feature lock but would be good to make this a predicate (see controller predicates folder)
        if (!fileUploadService.getUploadFeatureEnabled) {
          Future.successful(NotFound(notFoundTemplate))
        }
        else {
          if (backUrl.isDefined) {
            s4lConnector.fetchAndGetFormData[SupportingDocumentsUploadModel](KeystoreKeys.supportingDocumentsUpload).map {
              case Some(data) => Ok(SupportingDocumentsUpload(supportingDocumentsUploadForm.fill(data), backUrl.get))
              case None => Ok(SupportingDocumentsUpload(supportingDocumentsUploadForm, backUrl.get))
            }

          } else {
            // no back link - send to beginning of flow
            Future.successful(Redirect(routes.ConfirmCorrespondAddressController.show()))
          }
        }
      }

      for {
        link <- ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkSupportingDocs, s4lConnector)
        route <- routeRequest(link)
      } yield route
    }
  }

  val submit = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>
      supportingDocumentsUploadForm.bindFromRequest().fold(
        formWithErrors => {
          ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkSupportingDocs, s4lConnector).flatMap {
            case Some(link) => Future.successful(BadRequest(SupportingDocumentsUpload(formWithErrors, link)))
            case None => Future.successful(Redirect(routes.ConfirmCorrespondAddressController.show()))
          }
        },
        validFormData => {
          s4lConnector.saveFormData(KeystoreKeys.supportingDocumentsUpload, validFormData)
          validFormData.doUpload match {
            case Constants.StandardRadioButtonYesValue => Future.successful(Redirect(attachmentsFrontEndUrl))
            case Constants.StandardRadioButtonNoValue => Future.successful(Redirect(routes.CheckAnswersController.show()))
          }
        }
      )
    }
  }
}
