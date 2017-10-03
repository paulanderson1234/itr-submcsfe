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
import connectors.{EnrolmentConnector, S4LConnector, SubmissionConnector}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.mvc._
import forms.NumberOfSharesForm._
import models.NumberOfSharesModel
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import views.html.eis.shareDetails.NumberOfShares

import scala.concurrent.Future

object NumberOfSharesController extends NumberOfSharesController {
  override lazy val s4lConnector = S4LConnector
  val submissionConnector: SubmissionConnector = SubmissionConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait NumberOfSharesController extends FrontendController with AuthorisedAndEnrolledForTAVC {

  override val acceptedFlows = Seq(Seq(EIS))

  val show: Action[AnyContent] = AuthorisedAndEnrolled.async { implicit user => implicit request =>

    s4lConnector.fetchAndGetFormData[NumberOfSharesModel](KeystoreKeys.numberOfShares).map {
      case Some(data) => Ok(NumberOfShares(numberOfSharesForm.fill(data)))
      case None => Ok(NumberOfShares(numberOfSharesForm))
    }
  }

  def submit: Action[AnyContent] = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    numberOfSharesForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(NumberOfShares(formWithErrors)))
      },
      validFormData => {
        s4lConnector.saveFormData(KeystoreKeys.numberOfShares, validFormData)
        Future.successful(Redirect(routes.TotalAmountRaisedController.show()))
      }
    )
  }
}
