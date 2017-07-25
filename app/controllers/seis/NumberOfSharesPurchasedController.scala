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
import common.{Constants, KeystoreKeys}
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.Helpers.{ControllerHelpers, PreviousInvestorsHelper}
import controllers.predicates.FeatureSwitch
import models.CompanyOrIndividualModel
import forms.NumberOfSharesPurchasedForm._
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import views.html.seis.investors.NumberOfSharesPurchased
import models.investorDetails.{InvestorDetailsModel, NumberOfSharesPurchasedModel}
import play.api.mvc.{Action, AnyContent}

import scala.concurrent.Future

object NumberOfSharesPurchasedController extends NumberOfSharesPurchasedController
{
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait NumberOfSharesPurchasedController extends FrontendController with AuthorisedAndEnrolledForTAVC with FeatureSwitch with ControllerHelpers {

  override val acceptedFlows = Seq(Seq(SEIS))

  def show(id: Int): Action[AnyContent] = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>

      s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map {
        case Some(data) => {
          val itemToUpdateIndex = data.indexWhere(_.processingId.getOrElse(0) == id)
          if (itemToUpdateIndex != -1) {
            val model = data.lift(itemToUpdateIndex)
            if (model.get.numberOfSharesPurchasedModel.isDefined) {
              Ok(NumberOfSharesPurchased(model.get.companyOrIndividualModel.get.companyOrIndividual, "TODO",
                numberOfSharesPurchasedForm.fill(model.get.numberOfSharesPurchasedModel.get)))
            }
            else
              Ok(NumberOfSharesPurchased(model.get.companyOrIndividualModel.get.companyOrIndividual, "TODO", numberOfSharesPurchasedForm))
          }
          else {
            // Set back to the review page later
            Redirect(routes.AddInvestorOrNomineeController.show())
          }
        }
        case None => {
          Redirect(controllers.seis.routes.ShareDescriptionController.show())
        }
      }
    }
  }

  val submit = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user =>
      implicit request =>
        numberOfSharesPurchasedForm.bindFromRequest().fold(
          formWithErrors => {
            s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map {
              case Some(data) => {
                val investorDetailsModel = data.last
                BadRequest(NumberOfSharesPurchased(investorDetailsModel.companyOrIndividualModel.get.companyOrIndividual, "TODO", formWithErrors))
              }
            }
          },
          validFormData => {
            validFormData.processingId match {
              case Some(_) => PreviousInvestorsHelper.updateNumOfSharesPurchasedDetails(s4lConnector, validFormData).map {
                investorDetailsModel => Redirect(routes.HowMuchSpentOnSharesController.show(investorDetailsModel.processingId.get))
              }
              case None => PreviousInvestorsHelper.addNumOfSharesPurchasedDetails(s4lConnector, validFormData).map {
                investorDetailsModel => Redirect(routes.HowMuchSpentOnSharesController.show(investorDetailsModel.processingId.get))
              }
            }
          }
        )
    }
  }
}
