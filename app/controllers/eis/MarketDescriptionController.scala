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

import auth.{AuthorisedAndEnrolledForTAVC, EIS, VCT}
import common.KeystoreKeys
import controllers.Helpers.ControllerHelpers
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import forms.MarketDescriptionForm._
import models.MarketDescriptionModel
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import views.html.eis.shareDetails.MarketDescription
import scala.concurrent.Future


object MarketDescriptionController extends MarketDescriptionController{
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait MarketDescriptionController extends FrontendController with AuthorisedAndEnrolledForTAVC {

  override val acceptedFlows = Seq(Seq(EIS), Seq(VCT), Seq(EIS, VCT))

  val show = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    def routeRequest(backUrl: Option[String]) = {
      if (backUrl.isDefined) {
        s4lConnector.fetchAndGetFormData[MarketDescriptionModel](KeystoreKeys.marketDescription).map {
          case Some(data) => Ok(MarketDescription(marketDescriptionForm.fill(data), backUrl.get))
          case None => Ok(MarketDescription(marketDescriptionForm, backUrl.get))
        }
      }
      else Future.successful(Redirect(controllers.routes.HomeController.redirectToHub()))
    }

    for {
      link <- ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkMarketDescription, s4lConnector)
      route <- routeRequest(link)
    } yield route
  }

  val submit = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    marketDescriptionForm.bindFromRequest().fold(
      formWithErrors => {
        ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkMarketDescription, s4lConnector).flatMap(url =>
          Future.successful(BadRequest(MarketDescription(formWithErrors, url.get))))
      },
      validFormData => {
        s4lConnector.saveFormData(KeystoreKeys.marketDescription, validFormData)
        s4lConnector.saveFormData(KeystoreKeys.backLinkInvestmentGrow, routes.MarketDescriptionController.show().url)
        Future.successful(Redirect(routes.InvestmentGrowController.show()))
      }
    )
  }

}