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
import config.FrontendGlobal._
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.Helpers.ControllerHelpers
import controllers.predicates.FeatureSwitch
import forms.ConfirmCorrespondAddressForm._
import models.{AddressModel, ConfirmCorrespondAddressModel}
import play.api.mvc.Result
import services.SubscriptionService
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.seis.contactInformation.ConfirmCorrespondAddress
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future

object ConfirmCorrespondAddressController extends ConfirmCorrespondAddressController{
  val subscriptionService = SubscriptionService
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait ConfirmCorrespondAddressController extends FrontendController with AuthorisedAndEnrolledForTAVC with FeatureSwitch {

  override val acceptedFlows = Seq(Seq(SEIS))


  val subscriptionService: SubscriptionService

  val show = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>

      def getContactAddress: Future[Option[AddressModel]] = for {
        tavcRef <- getTavCReferenceNumber()
        contactAddress <- subscriptionService.getSubscriptionContactAddress(tavcRef)
      } yield contactAddress

      def routeRequest: (Option[ConfirmCorrespondAddressModel], Option[String]) => Future[Result] = {
        case (Some(savedData), Some(backLink)) =>
          Future.successful(Ok(ConfirmCorrespondAddress(confirmCorrespondAddressForm.fill(savedData), backLink)))
        case (_, Some(backLink)) => getContactAddress.map {
          case Some(data) => Ok(ConfirmCorrespondAddress(confirmCorrespondAddressForm.fill(ConfirmCorrespondAddressModel("", data)), backLink))
          case _ => InternalServerError(internalServerErrorTemplate)
        }
        case (_, _) => Future.successful(Redirect(routes.ConfirmContactDetailsController.show()))
      }

      for {
        backLink <- ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkConfirmCorrespondence, s4lConnector)
        confirmCorrespondenceAddress <- s4lConnector.fetchAndGetFormData[ConfirmCorrespondAddressModel](KeystoreKeys.confirmContactAddress)
        route <- routeRequest(confirmCorrespondenceAddress, backLink)
      } yield route
    }
  }

  val submit = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>

      def routeRequest: Option[String] => Future[Result] = {
        case Some(backLink) =>
          confirmCorrespondAddressForm.bindFromRequest().fold(
            formWithErrors => {
              Future.successful(BadRequest(ConfirmCorrespondAddress(formWithErrors, backLink)))
            },
            validFormData => {
              s4lConnector.saveFormData(KeystoreKeys.confirmContactAddress, validFormData)
              validFormData.contactAddressUse match {
                case Constants.StandardRadioButtonYesValue =>
                  s4lConnector.saveFormData(KeystoreKeys.backLinkSupportingDocs,
                    routes.ConfirmCorrespondAddressController.show().toString)
                  s4lConnector.saveFormData(KeystoreKeys.contactAddress, validFormData.address)
                  Future.successful(Redirect(routes.SupportingDocumentsController.show()))
                case Constants.StandardRadioButtonNoValue =>
                  Future.successful(Redirect(routes.ContactAddressController.show()))
              }
            }
          )
        case _ => Future.successful(Redirect(routes.ConfirmContactDetailsController.show()))
      }

      for {
        backLink <- ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkConfirmCorrespondence, s4lConnector)
        route <- routeRequest(backLink)
      } yield route
    }
  }
}