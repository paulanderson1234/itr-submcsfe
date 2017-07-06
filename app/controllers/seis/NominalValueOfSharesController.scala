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
import config.{AppConfig, FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.predicates.FeatureSwitch
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.frontend.controller.FrontendController

object NominalValueOfSharesController extends NominalValueOfSharesController {
  override val enrolmentConnector: EnrolmentConnector = EnrolmentConnector
  override val applicationConfig: AppConfig = FrontendAppConfig
  override val s4lConnector: S4LConnector = S4LConnector
  override val authConnector: AuthConnector = FrontendAuthConnector
}

trait NominalValueOfSharesController extends FrontendController with AuthorisedAndEnrolledForTAVC with FeatureSwitch {

  override val acceptedFlows = Seq(Seq(SEIS))

  val show: Action[AnyContent] = TODO

  val submit: Action[AnyContent] = TODO
}
