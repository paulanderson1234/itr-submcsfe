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

import auth.{AuthorisedAndEnrolledForTAVC, EIS, Flow}
import common.{Constants, KeystoreKeys}
import config.{AppConfig, FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import models.KiProcessingModel
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import views.html.eis.companyDetails.FullTimeEmployeeCountError

import scala.concurrent.Future

object FullTimeEmployeeCountErrorController extends FullTimeEmployeeCountErrorController{
  override lazy val enrolmentConnector: EnrolmentConnector = EnrolmentConnector
  override lazy val applicationConfig: AppConfig = FrontendAppConfig
  override lazy val s4lConnector: S4LConnector = S4LConnector
  override lazy val authConnector: AuthConnector = FrontendAuthConnector
}

trait FullTimeEmployeeCountErrorController extends FrontendController with AuthorisedAndEnrolledForTAVC {
  val acceptedFlows: Seq[Seq[Flow]] = Seq(Seq(EIS))

  val show: Action[AnyContent] = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    s4lConnector.fetchAndGetFormData[KiProcessingModel](KeystoreKeys.kiProcessingModel).map {
      case Some(data) if(data.isKi)=> Ok(FullTimeEmployeeCountError(Constants.schemeTypeEisKi))
      case _ => Ok(FullTimeEmployeeCountError(Constants.schemeTypeEis))
    }
  }
}
