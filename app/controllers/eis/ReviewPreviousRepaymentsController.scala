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
import controllers.Helpers.{ControllerHelpers, PreviousRepaymentsHelper}
import models.repayments.SharesRepaymentDetailsModel
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.eis.investors.ReviewPreviousRepayments
import play.api.Play.current
import play.api.i18n.Messages.Implicits._

import scala.concurrent.Future

object ReviewPreviousRepaymentsController extends ReviewPreviousRepaymentsController {
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait ReviewPreviousRepaymentsController extends FrontendController with AuthorisedAndEnrolledForTAVC with ControllerHelpers {

  override val acceptedFlows = Seq(Seq(EIS))

  val show = AuthorisedAndEnrolled.async {
    implicit user =>
      implicit request =>
        s4lConnector.fetchAndGetFormData[Vector[SharesRepaymentDetailsModel]](KeystoreKeys.sharesRepaymentDetails).map {
          case Some(data) if (data.nonEmpty) => Ok(ReviewPreviousRepayments(data))
          case _ => Redirect(controllers.eis.routes.AnySharesRepaymentController.show())
        }
  }

  def remove(id: Int): Action[AnyContent] = AuthorisedAndEnrolled.async { implicit user =>
    implicit request =>
      Future.successful(Redirect(controllers.eis.routes.DeleteSharesRepaymentController.show(id)))
  }

  def change(id: Int): Action[AnyContent] = AuthorisedAndEnrolled.async { implicit user =>
    implicit request =>
      Future.successful(Redirect(controllers.eis.routes.WhoRepaidSharesController.show(Some(id))))
  }

  val submit = AuthorisedAndEnrolled.async { implicit user =>
    implicit request =>
      s4lConnector.saveFormData(KeystoreKeys.backLinkWasAnyValueReceived, routes.ReviewPreviousRepaymentsController.show().url)
      Future.successful(Redirect(controllers.eis.routes.WasAnyValueReceivedController.show()))
  }
}