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
import controllers.Helpers.{ControllerHelpers, PreviousInvestorShareHoldersHelper}
import controllers.predicates.FeatureSwitch
import forms.NumberOfPreviouslyIssuedSharesForm._
import models.investorDetails.InvestorDetailsModel
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.seis.investors.NumberOfPreviouslyIssuedShares

import scala.concurrent.Future

object NumberOfPreviouslyIssuedSharesController extends NumberOfPreviouslyIssuedSharesController {
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait NumberOfPreviouslyIssuedSharesController extends FrontendController with AuthorisedAndEnrolledForTAVC with FeatureSwitch with ControllerHelpers {

  override val acceptedFlows = Seq(Seq(SEIS))

  def show(investorProcessingId: Int, id: Int): Action[AnyContent] = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user =>
      implicit request =>

        def process(backUrl: Option[String]) = {
          if (backUrl.isDefined) {
            s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map {
              case Some(data) => {
                val itemToUpdateIndex = data.indexWhere(_.processingId.getOrElse(0) == investorProcessingId)
                if (itemToUpdateIndex != -1) {
                  val model = data.lift(itemToUpdateIndex)
                  if (model.get.previousShareHoldingModels.isDefined && model.get.previousShareHoldingModels.get.size > 0) {
                    val shareHoldingsIndex = model.get.previousShareHoldingModels.get.indexWhere(_.processingId.getOrElse(0) == id)
                    if (shareHoldingsIndex != -1) {
                      val shareHolderModel = model.get.previousShareHoldingModels.get.lift(shareHoldingsIndex)
                      if (shareHolderModel.get.numberOfPreviouslyIssuedSharesModel.isDefined) {
                        Ok(NumberOfPreviouslyIssuedShares(model.get.companyOrIndividualModel.get.companyOrIndividual,
                          numberOfPreviouslyIssuedSharesForm.fill(shareHolderModel.get.numberOfPreviouslyIssuedSharesModel.get),
                          backUrl.get, investorProcessingId))
                      }
                      else
                        Ok(NumberOfPreviouslyIssuedShares(model.get.companyOrIndividualModel.get.companyOrIndividual,
                          numberOfPreviouslyIssuedSharesForm, backUrl.get, investorProcessingId))
                    }
                    else Redirect(routes.AddInvestorOrNomineeController.show(model.get.processingId))
                  }
                  else Redirect(routes.AddInvestorOrNomineeController.show(model.get.processingId))
                }
                else Redirect(routes.AddInvestorOrNomineeController.show())
              }
              case None => Redirect(controllers.seis.routes.AddInvestorOrNomineeController.show())
            }
          }
          else Future.successful(Redirect(controllers.seis.routes.AddInvestorOrNomineeController.show()))
        }
        for {
          backUrl <- s4lConnector.fetchAndGetFormData[String](KeystoreKeys.backLinkNumberOfPreviouslyIssuedShares)
          route <- process(backUrl)
        } yield route
    }
  }

  def submit(companyOrIndividual: Option[String], backUrl: Option[String], investorProcessingId: Option[Int]):
  Action[AnyContent] = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user =>
      implicit request =>
        numberOfPreviouslyIssuedSharesForm.bindFromRequest().fold(
          formWithErrors => {
            Future.successful(BadRequest(NumberOfPreviouslyIssuedShares(companyOrIndividual.get,
              formWithErrors, backUrl.get, investorProcessingId.get)))
          },
          validFormData => {
            validFormData.processingId match {
              case Some(_) => PreviousInvestorShareHoldersHelper.updateNumberOfPreviouslyIssuedShares(s4lConnector, validFormData).map {
                data => Redirect(routes.PreviousShareHoldingsReviewController.show(data.investorProcessingId.get))
              }
              case None => PreviousInvestorShareHoldersHelper.addNumberOfPreviouslyIssuedShares(s4lConnector,
                validFormData, investorProcessingId.get).map {
                data => Redirect(routes.PreviousShareHoldingsReviewController.show(data.investorProcessingId.get))
              }
            }
          }
        )
    }
  }
}