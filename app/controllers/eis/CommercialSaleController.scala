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
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.Helpers.ControllerHelpers
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.mvc._
import models.{CommercialSaleModel, KiProcessingModel}
import forms.CommercialSaleForm._
import views.html.eis.companyDetails.CommercialSale
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future

object CommercialSaleController extends CommercialSaleController {
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait CommercialSaleController extends FrontendController with AuthorisedAndEnrolledForTAVC {

  override val acceptedFlows = Seq(Seq(EIS))

  val show = AuthorisedAndEnrolled.async { implicit user => implicit request =>

    def routeRequest(backUrl: Option[String]) = {
      if (backUrl.isDefined) {
        s4lConnector.fetchAndGetFormData[CommercialSaleModel](KeystoreKeys.commercialSale).map {
          case Some(data) => Ok(CommercialSale(commercialSaleForm.fill(data), backUrl.getOrElse("")))
          case None => Ok(CommercialSale(commercialSaleForm, backUrl.getOrElse("")))
        }
      }
      else Future.successful(Redirect(routes.QualifyBusinessActivityController.show()))
    }
    for {
      link <- ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkCommercialSale, s4lConnector)
      route <- routeRequest(link)
    } yield route
  }

  val submit = AuthorisedAndEnrolled.async { implicit user => implicit request =>

    commercialSaleForm.bindFromRequest().fold(
      formWithErrors => {
        s4lConnector.fetchAndGetFormData[String](KeystoreKeys.backLinkCommercialSale).flatMap {
          case backUrl => Future.successful(BadRequest(CommercialSale(formWithErrors, backUrl.fold("")(_.self))))
        }
      },
      validFormData => {
        s4lConnector.saveFormData(KeystoreKeys.commercialSale, validFormData)
        Future.successful(Redirect(routes.ShareIssueDateController.show()))
      }
    )
  }
}
