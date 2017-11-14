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
import common.{Constants, KeystoreKeys}
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.Helpers.{ControllerHelpers, PreviousInvestorsHelper}
import forms.IsExistingShareHolderForm._
import models.investorDetails.{InvestorDetailsModel, IsExistingShareHolderModel}
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Action, AnyContent, Result}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.eis.investors.IsExistingShareHolder

import scala.concurrent.Future

object IsExistingShareHolderController extends IsExistingShareHolderController
{
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait IsExistingShareHolderController extends FrontendController with AuthorisedAndEnrolledForTAVC with ControllerHelpers {

  override val acceptedFlows = Seq(Seq(EIS))

  def show(id: Int): Action[AnyContent] = AuthorisedAndEnrolled.async { implicit user =>
    implicit request =>
      def process(backUrl: Option[String]) = {
        if (backUrl.isDefined) {
          s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map { vector =>
            redirectEisNoInvestors(vector) { data =>
              val itemToUpdateIndex = getInvestorIndex(id, data)
              redirectEisInvalidInvestor(itemToUpdateIndex) { index =>
                if (data.lift(itemToUpdateIndex).get.companyOrIndividualModel.isDefined) {
                  val companyModel = retrieveInvestorData(index, data)(_.companyOrIndividualModel)
                  if (companyModel.isDefined) {
                    val form = fillForm(isExistingShareHolderForm, retrieveInvestorData(index, data)(_.isExistingShareHolderModel))
                    Ok(IsExistingShareHolder(companyModel.get.companyOrIndividual, form, backUrl.get))
                  }
                  else Redirect(routes.AddInvestorOrNomineeController.show())
                } else Redirect(routes.CompanyOrIndividualController.show(id))
              }
            }
          }
        }
        else Future.successful(Redirect(controllers.eis.routes.AddInvestorOrNomineeController.show()))
      }
      for {
        backUrl <- s4lConnector.fetchAndGetFormData[String](KeystoreKeys.backLinkIsExistingShareHolder)
        route <- process(backUrl)
      } yield route
  }

  def submit(companyOrIndividual: Option[String]): Action[AnyContent] = AuthorisedAndEnrolled.async { implicit user =>
    implicit request =>
      def routing(model: Option[IsExistingShareHolderModel], investorDetailsModel: InvestorDetailsModel): Result = {
        s4lConnector.saveFormData(KeystoreKeys.backLinkShareClassAndDescription,
          routes.IsExistingShareHolderController.show(investorDetailsModel.processingId.get).url)
        if (model.exists(_.isExistingShareHolder == Constants.StandardRadioButtonYesValue))
          if (investorDetailsModel.previousShareHoldingModels.isDefined &&
            investorDetailsModel.previousShareHoldingModels.get.nonEmpty) {
            Redirect(routes.PreviousShareHoldingsReviewController.show(investorDetailsModel.processingId.get))
          }
          else {
            Redirect(routes.PreviousShareHoldingDescriptionController.show(investorDetailsModel.processingId.get))
          }
        else
          Redirect(routes.ReviewInvestorDetailsController.show(investorDetailsModel.processingId.get))
      }

      isExistingShareHolderForm.bindFromRequest().fold(
        formWithErrors => {
          ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkIsExistingShareHolder, s4lConnector).flatMap(url =>
            Future.successful(BadRequest(IsExistingShareHolder(companyOrIndividual.get, formWithErrors, url.get))))
        },
        validFormData => {
          validFormData.processingId match {
            case Some(_) => PreviousInvestorsHelper.updateIsExistingShareHoldersDetails(s4lConnector, validFormData).map {
              investorDetailsModel => {
                routing(investorDetailsModel.isExistingShareHolderModel, investorDetailsModel)
              }
            }
            case None => PreviousInvestorsHelper.addIsExistingShareHoldersDetails(s4lConnector, validFormData).map {
              investorDetailsModel => {
                routing(investorDetailsModel.isExistingShareHolderModel, investorDetailsModel)
              }
            }
          }
        }
      )
  }
}