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
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import forms.DateSharesRepaidForm._
import models.DateSharesRepaidModel
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import views.html.eis.investors.DateSharesRepaid
import scala.concurrent.Future
import play.api.mvc.{Action, AnyContent, Result}
import play.api.data.Form


object DateSharesRepaidController extends DateSharesRepaidController{
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait DateSharesRepaidController extends FrontendController with AuthorisedAndEnrolledForTAVC  {

  override val acceptedFlows = Seq(Seq(EIS),Seq(VCT),Seq(EIS,VCT))

  val show = 
    AuthorisedAndEnrolled.async { implicit user => implicit request =>
      s4lConnector.fetchAndGetFormData[DateSharesRepaidModel](KeystoreKeys.dateSharesRepaid).map {
        case Some(data) => Ok(DateSharesRepaid(dateSharesRepaidForm.fill(data)))
        case None => Ok(DateSharesRepaid(dateSharesRepaidForm))
      }
    }
  

  val submit: Action[AnyContent] = 
    AuthorisedAndEnrolled.async { implicit user => implicit request =>
      val success: DateSharesRepaidModel => Future[Result] = { model =>
        s4lConnector.saveFormData(KeystoreKeys.dateSharesRepaid, model).map(_ =>
          //TODO: Route to next page when available
          Redirect(routes.DateSharesRepaidController.show())
        )
      }

      val failure: Form[DateSharesRepaidModel] => Future[Result] = { form =>
        Future.successful(BadRequest(DateSharesRepaid(form)))
      }

      dateSharesRepaidForm.bindFromRequest().fold(failure, success)
    }
  
}

