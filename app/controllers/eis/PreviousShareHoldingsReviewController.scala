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
import models.investorDetails.InvestorDetailsModel
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.eis.investors.PreviousShareHoldingsReview

import scala.concurrent.Future

object PreviousShareHoldingsReviewController extends PreviousShareHoldingsReviewController {
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait PreviousShareHoldingsReviewController extends FrontendController with AuthorisedAndEnrolledForTAVC with ControllerHelpers {

  override val acceptedFlows = Seq(Seq(EIS))

  def show(investorProcessingId: Int): Action[AnyContent] = AuthorisedAndEnrolled.async {
    implicit user =>
      implicit request =>
        s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map { vector =>
          redirectEisNoInvestors(vector) { data =>
            redirectEisInvalidInvestor(getInvestorIndex(investorProcessingId, data)) { investorIdVal =>
              val shareHoldings = retrieveInvestorData(investorIdVal, data)(_.previousShareHoldingModels)
              if (shareHoldings.getOrElse(Vector.empty).nonEmpty) {
                Ok(PreviousShareHoldingsReview(data.lift(getInvestorIndex(investorProcessingId, data)).get))
              }
              else Redirect(controllers.eis.routes.IsExistingShareHolderController.show(investorProcessingId))
            }
          }
        }
  }

  def remove(investorProcessingId: Int, id: Int): Action[AnyContent] = AuthorisedAndEnrolled.async { implicit user =>
    implicit request =>
      Future.successful(Redirect(controllers.eis.routes.DeletePreviousShareHolderController.show(investorProcessingId, id)))
  }

  def change(investorProcessingId: Int, id: Int): Action[AnyContent] = AuthorisedAndEnrolled.async { implicit user =>
    implicit request =>
      s4lConnector.saveFormData(KeystoreKeys.backLinkShareClassAndDescription,
        routes.PreviousShareHoldingsReviewController.show(investorProcessingId).url)
      Future.successful(Redirect(controllers.eis.routes.PreviousShareHoldingDescriptionController.show(investorProcessingId, Some(id))))
  }

  def submit(investorProcessingId: Int): Action[AnyContent] = AuthorisedAndEnrolled.async { implicit user =>
    implicit request =>
      Future.successful(Redirect(controllers.eis.routes.AddAnotherShareholdingController.show(investorProcessingId)))
  }
}