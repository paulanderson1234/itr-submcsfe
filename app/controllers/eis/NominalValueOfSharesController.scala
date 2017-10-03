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
import config.{AppConfig, FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import forms.NominalValueOfSharesForm._
import models.NominalValueOfSharesModel
import play.api.Play.current
import play.api.data.Form
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Action, AnyContent, Result}
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.Future

object NominalValueOfSharesController extends NominalValueOfSharesController {
  override lazy val enrolmentConnector: EnrolmentConnector = EnrolmentConnector
  override lazy val applicationConfig: AppConfig = FrontendAppConfig
  override lazy val s4lConnector: S4LConnector = S4LConnector
  override lazy val authConnector: AuthConnector = FrontendAuthConnector
}

trait NominalValueOfSharesController extends FrontendController with AuthorisedAndEnrolledForTAVC {

  override val acceptedFlows = Seq(Seq(EIS))

  val show: Action[AnyContent] = AuthorisedAndEnrolled.async {
    implicit user =>
      implicit request =>

        //        s4lConnector.fetchAndGetFormData[NominalValueOfSharesModel](KeystoreKeys.nominalValueOfShares).map {
        //          case Some(data) => Ok(views.html.eis.shareDetails.NominalValueOfShares(nominalValueOfSharesForm.fill(data)))
        //          case None => Ok(views.html.eis.shareDetails.NominalValueOfShares(nominalValueOfSharesForm))
        //        }

        // Not in flow so route past this page to total amount raised if called. This page may come back later
        Future.successful(Redirect(routes.TotalAmountRaisedController.show()))
  }

  val submit: Action[AnyContent] = AuthorisedAndEnrolled.async {
    implicit user =>
      implicit request =>
        val success: NominalValueOfSharesModel => Future[Result] = { model =>
          s4lConnector.saveFormData(KeystoreKeys.nominalValueOfShares, model).map(_ =>
            Redirect(controllers.eis.routes.TotalAmountRaisedController.show())
          )
        }

        val failure: Form[NominalValueOfSharesModel] => Future[Result] = { form =>
          Future.successful(BadRequest(views.html.eis.shareDetails.NominalValueOfShares(form)))
        }

        nominalValueOfSharesForm.bindFromRequest().fold(failure, success)
  }
}