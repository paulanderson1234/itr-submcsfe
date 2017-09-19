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
import controllers.Helpers.{PreviousRepaymentsHelper, ControllerHelpers}
import models.repayments.SharesRepaymentDetailsModel
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.eis.investors.DeleteSharesRepayment

object DeleteSharesRepaymentController extends DeleteSharesRepaymentController {
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait DeleteSharesRepaymentController extends FrontendController with AuthorisedAndEnrolledForTAVC with ControllerHelpers {

  override val acceptedFlows = Seq(Seq(EIS))

  val show: Int => Action[AnyContent] = id =>
    AuthorisedAndEnrolled.async { implicit user => implicit request =>
      s4lConnector.fetchAndGetFormData[Vector[SharesRepaymentDetailsModel]](KeystoreKeys.sharesRepaymentDetails).map { vector =>
        redirectIfNoRepayments(vector) { data =>
          val itemToUpdateIndex = getRepaymentsIndex(id, data)
          redirectInvalidRepayments(itemToUpdateIndex) { index =>
            Ok(DeleteSharesRepayment(retrieveRepaymentsData(index, data)(model =>
              Option(model)).get))
          }
        }
      }
    }

  def submit(repaymentProcessingId: Int): Action[AnyContent] = AuthorisedAndEnrolled.async { implicit user =>
    implicit request =>
      PreviousRepaymentsHelper.removeKeystorePreviousRepayment(s4lConnector, repaymentProcessingId).map {
        data => Redirect(routes.ReviewPreviousRepaymentsController.show())
      }
  }
}
