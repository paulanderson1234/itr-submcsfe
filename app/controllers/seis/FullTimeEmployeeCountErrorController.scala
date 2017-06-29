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

import auth.{AuthorisedAndEnrolledForTAVC, Flow, SEIS}
import config.{AppConfig, FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.predicates.FeatureSwitch
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future

object FullTimeEmployeeCountErrorController extends FullTimeEmployeeCountErrorController{
  override lazy val enrolmentConnector: EnrolmentConnector = EnrolmentConnector
  override lazy val applicationConfig: AppConfig = FrontendAppConfig
  override lazy val s4lConnector: S4LConnector = S4LConnector
  override lazy val authConnector: AuthConnector = FrontendAuthConnector
}

trait FullTimeEmployeeCountErrorController extends FrontendController with AuthorisedAndEnrolledForTAVC with FeatureSwitch {
  val acceptedFlows: Seq[Seq[Flow]] = Seq(Seq(SEIS))

  val show: Action[AnyContent] = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>
      Future.successful(Ok(views.html.seis.companyDetails.FullTimeEmployeeCountError()))
    }
  }
}
