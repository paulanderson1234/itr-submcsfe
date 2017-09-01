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
import controllers.Helpers.{ControllerHelpers, PreviousInvestorShareHoldersHelper}
import forms.PreviousShareHoldingDescriptionForm._
import models.investorDetails.{InvestorDetailsModel, PreviousShareHoldingDescriptionModel}
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.eis.investors.PreviousShareHoldingDescription

import scala.concurrent.Future

object PreviousShareHoldingDescriptionController extends PreviousShareHoldingDescriptionController {
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait PreviousShareHoldingDescriptionController extends FrontendController with AuthorisedAndEnrolledForTAVC with ControllerHelpers {

  override val acceptedFlows = Seq(Seq(EIS))

  def show(investorProcessingId: Int, id: Option[Int]): Action[AnyContent] = AuthorisedAndEnrolled.async { implicit user =>
    implicit request =>

      def process(backUrl: Option[String]) = {
        if (backUrl.isDefined) {
          s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map {
            vector =>
              redirectEisNoInvestors(vector) {
                data => redirectEisInvalidInvestor(getInvestorIndex(investorProcessingId, data)) { investorIdVal =>
                  val previousShares = retrieveInvestorData(investorIdVal, data)(_.previousShareHoldingModels)
                  val form = fillForm[PreviousShareHoldingDescriptionModel](previousShareHoldingDescriptionForm,
                    id.flatMap(idVal =>
                      retrieveShareData(getShareIndex(idVal, previousShares.getOrElse(Vector.empty)),
                        previousShares)(_.previousShareHoldingDescriptionModel)))
                  if (data.lift(getInvestorIndex(investorProcessingId, data)).get.companyOrIndividualModel.isDefined)
                    Ok(PreviousShareHoldingDescription(retrieveInvestorData(investorIdVal,
                      data)(_.companyOrIndividualModel.map(_.companyOrIndividual)).get,
                      form, backUrl.get, investorProcessingId))
                  else Redirect(routes.CompanyOrIndividualController.show(investorProcessingId))
                }
              }
          }
        }
        else Future.successful(Redirect(controllers.eis.routes.AddInvestorOrNomineeController.show()))
      }
      for {
        backUrl <- s4lConnector.fetchAndGetFormData[String](KeystoreKeys.backLinkShareClassAndDescription)
        route <- process(backUrl)
      } yield route
  }

  def submit(companyOrIndividual: Option[String], investorProcessingId: Option[Int]): Action[AnyContent] = AuthorisedAndEnrolled.async { implicit user =>
    implicit request =>
      previousShareHoldingDescriptionForm.bindFromRequest().fold(
        formWithErrors => {
          ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkShareClassAndDescription, s4lConnector).flatMap(url =>
            Future.successful(BadRequest(PreviousShareHoldingDescription(companyOrIndividual.get, formWithErrors, url.get, investorProcessingId.get))))
        },
        validFormData => {
          validFormData.processingId match {
            case Some(_) => PreviousInvestorShareHoldersHelper.updateShareClassAndDescription(s4lConnector, validFormData).map {
              data => {
                s4lConnector.saveFormData(KeystoreKeys.backLinkIsPreviousShareHoldingNominalValue,
                  routes.PreviousShareHoldingDescriptionController.show(data.investorProcessingId.get, data.processingId).url)
                Redirect(routes.PreviousShareHoldingNominalValueController.show(data.investorProcessingId.get, data.processingId.get))
              }
            }
            case None => PreviousInvestorShareHoldersHelper.addShareClassAndDescription(s4lConnector, validFormData, investorProcessingId.get).map {
              data => {
                s4lConnector.saveFormData(KeystoreKeys.backLinkShareClassAndDescription,
                  routes.PreviousShareHoldingsReviewController.show(data.investorProcessingId.get).url)
                s4lConnector.saveFormData(KeystoreKeys.backLinkIsPreviousShareHoldingNominalValue,
                  routes.PreviousShareHoldingDescriptionController.show(data.investorProcessingId.get, data.processingId).url)
                Redirect(routes.PreviousShareHoldingNominalValueController.show(data.investorProcessingId.get, data.processingId.get))
              }
            }
          }
        }
      )
  }
}