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

  // id: Option[Int] should be changed to Int once the initial pages were implemented
  def show(investorProcessingId: Int, id: Int): Action[AnyContent] = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user =>
      implicit request =>

        def process(backUrl: Option[String]) = {
          if (backUrl.isDefined) {
            s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map { vector =>
              redirectNoInvestors(vector) { data =>
                redirectInvalidInvestor(getInvestorIndex(investorProcessingId, data)) { investorIdVal =>
                  val shareHoldings = retrieveInvestorData(investorIdVal, data)(_.previousShareHoldingModels)
                  redirectInvalidPreviousShareHolding(id, shareHoldings) { shareHoldingsIndex =>
                    val form = fillForm(numberOfPreviouslyIssuedSharesForm, retrieveShareData(shareHoldingsIndex,
                      shareHoldings)(_.numberOfPreviouslyIssuedSharesModel))

                    Ok(NumberOfPreviouslyIssuedShares(retrieveInvestorData(investorIdVal, data)(_.companyOrIndividualModel.map(_.companyOrIndividual)).get,
                      form, backUrl.get))
                  }
                }
              }
            }
          }
          else Future.successful(Redirect(controllers.seis.routes.ShareDescriptionController.show()))
        }

        for {
          backUrl <- s4lConnector.fetchAndGetFormData[String](KeystoreKeys.backLinkNumberOfPreviouslyIssuedShares)
          route <- process(backUrl)
        } yield route
    }
  }

  def submit(companyOrIndividual: Option[String], backUrl: Option[String]): Action[AnyContent] = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user =>
      implicit request =>
        numberOfPreviouslyIssuedSharesForm.bindFromRequest().fold(
          formWithErrors => {
            Future.successful(BadRequest(NumberOfPreviouslyIssuedShares(companyOrIndividual.get, formWithErrors, backUrl.get)))
          },
          validFormData => {
            validFormData.processingId match {
              case Some(_) => PreviousInvestorShareHoldersHelper.updateNumberOfPreviouslyIssuedShares(s4lConnector, validFormData).map {
                data => {
                  Redirect(routes.NumberOfPreviouslyIssuedSharesController.show(data.investorProcessingId.get, data.processingId.get))
                }
              }
              case None => PreviousInvestorShareHoldersHelper.addNumberOfPreviouslyIssuedShares(s4lConnector, validFormData).map {
                data => {
                  Redirect(routes.NumberOfPreviouslyIssuedSharesController.show(data.investorProcessingId.get, data.processingId.get))
                }
              }
            }
          }
        )
    }
  }
}
