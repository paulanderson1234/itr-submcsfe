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
import common.KeystoreKeys
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.Helpers.ControllerHelpers
import controllers.predicates.FeatureSwitch
import models.investorDetails.{InvestorDetailsModel, PreviousShareHoldingModel}
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.seis.investors.PreviousShareHoldingsReview

import scala.concurrent.Future

object PreviousShareHoldingsReviewController extends PreviousShareHoldingsReviewController {
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait PreviousShareHoldingsReviewController extends FrontendController with AuthorisedAndEnrolledForTAVC with FeatureSwitch with ControllerHelpers{

  override val acceptedFlows = Seq(Seq(SEIS))

  def show(investorProcessingId: Int, id: Option[Int]): Action[AnyContent] = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async {
      implicit user =>
        implicit request =>
          def process(backUrl: Option[String]) = {
            if (backUrl.isDefined) {
              s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map { vector =>
                redirectNoInvestors(vector) { data =>
                  redirectInvalidInvestor(getInvestorIndex(investorProcessingId, data)) { investorIdVal =>
                    val shareHoldings = retrieveInvestorData(investorIdVal, data)(_.previousShareHoldingModels)
                    if (shareHoldings.getOrElse(Vector.empty).size > 0) {
                      Ok(PreviousShareHoldingsReview(data.lift(getInvestorIndex(investorProcessingId, data)).get, backUrl.get))
                    }
                    else Redirect(controllers.seis.routes.IsExistingShareHolderController.show(investorProcessingId))
                  }
                }
              }
            }
            else Future.successful(Redirect(controllers.seis.routes.AddInvestorOrNomineeController.show()))
          }

          for {
            backUrl <- s4lConnector.fetchAndGetFormData[String](KeystoreKeys.backLinkInvestorPreviousShareHoldingReview)
            route <- process(backUrl)
          } yield route
    }
  }

  def remove(investorProcessingId: Int, id: Int): Action[AnyContent] =
    featureSwitch(applicationConfig.seisFlowEnabled) {
      AuthorisedAndEnrolled.async { implicit user =>
        implicit request =>
          Future.successful(Redirect(controllers.seis.routes.DeletePreviousShareHolderController.show(investorProcessingId, id)))
      }
  }

  def submit(investorProcessingId: Int): Action[AnyContent] = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user =>
      implicit request =>
        // this should change it to REVIEW INVESTOR DETAILS PAGE
         Future.successful(Redirect(controllers.seis.routes.AddInvestorOrNomineeController.show(Some(investorProcessingId))))
    }
  }
}
