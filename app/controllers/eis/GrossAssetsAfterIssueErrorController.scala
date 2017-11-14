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
import models.DateOfIncorporationModel
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.eis.companyDetails.GrossAssetsAfterIssueError
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import scala.concurrent.Future

object GrossAssetsAfterIssueErrorController extends GrossAssetsAfterIssueErrorController
{
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
  override lazy val s4lConnector = S4LConnector
}

trait GrossAssetsAfterIssueErrorController extends FrontendController with AuthorisedAndEnrolledForTAVC {


  override val acceptedFlows = Seq(Seq(EIS))

  val show = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    Future.successful(Ok(GrossAssetsAfterIssueError()))
  }

  val submit = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    s4lConnector.fetchAndGetFormData[DateOfIncorporationModel](KeystoreKeys.dateOfIncorporation).map {
      case Some(data) => ControllerHelpers.redirectGrossAssetsAfterIssue(Some(data), s4lConnector)
      case None => Redirect(routes.DateOfIncorporationController.show())
    }
  }

}
