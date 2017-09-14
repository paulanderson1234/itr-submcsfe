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
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import views.html.eis.shareDetails.AnnualLimitExceededError


import scala.concurrent.Future

object AnnualLimitExceededErrorController extends AnnualLimitExceededErrorController{
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait AnnualLimitExceededErrorController extends FrontendController with AuthorisedAndEnrolledForTAVC {

  override val acceptedFlows = Seq(Seq(EIS), Seq(VCT), Seq(EIS, VCT))

  val show = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    Future.successful(Ok(AnnualLimitExceededError()))
  }

  val submit = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    //TODO: The total amount raised controller can navigate to a number of different pages based on various conditions
    // This route below will have to mirror that logic.
    // The proposedInvestorController has much of the rerqired logic that need moving to the TotalAmountRaisedController.
    // when doing this the navigation implementation (routeRequest) should be put in a common helper so this page can call it to continue
    Future.successful(Redirect(routes.InvestmentGrowController.show()))
  }
}