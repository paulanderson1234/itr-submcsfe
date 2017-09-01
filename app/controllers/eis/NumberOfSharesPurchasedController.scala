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
import controllers.Helpers.{ControllerHelpers, PreviousInvestorsHelper}
import controllers.predicates.FeatureSwitch
import forms.NumberOfSharesPurchasedForm._
import models.ShareIssueDateModel
import models.investorDetails.InvestorDetailsModel
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import utils.DateFormatter
import views.html.eis.investors.NumberOfSharesPurchased

import scala.concurrent.Future

object NumberOfSharesPurchasedController extends NumberOfSharesPurchasedController {
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait NumberOfSharesPurchasedController extends FrontendController with AuthorisedAndEnrolledForTAVC with FeatureSwitch
  with ControllerHelpers with DateFormatter {

  override val acceptedFlows = Seq(Seq(EIS))

  def show(id: Int): Action[AnyContent] = AuthorisedAndEnrolled.async { implicit user => implicit request =>

    def process(shareIssueDate: Option[ShareIssueDateModel], backUrl: Option[String]) = {
      if (backUrl.isDefined) {
        s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map { vector =>
          redirectEisNoInvestors(vector) { data =>
            val itemToUpdateIndex = getInvestorIndex(id, data)
            redirectEisInvalidInvestor(itemToUpdateIndex) { index =>
              val form = fillForm(numberOfSharesPurchasedForm, retrieveInvestorData(index, data)(_.numberOfSharesPurchasedModel))
              if (shareIssueDate.isDefined && data.lift(itemToUpdateIndex).get.companyOrIndividualModel.isDefined)
                Ok(NumberOfSharesPurchased(retrieveInvestorData(index, data)(_.companyOrIndividualModel.map(_.companyOrIndividual)).get,
                  dateToStringWithNoZeroDay(shareIssueDate.get.day.get, shareIssueDate.get.month.get, shareIssueDate.get.year.get),
                  form, backUrl.get))
              else if (data.lift(itemToUpdateIndex).get.companyOrIndividualModel.isDefined)
                Redirect(controllers.eis.routes.ShareIssueDateController.show())
              else Redirect(controllers.eis.routes.CompanyOrIndividualController.show(id))
            }
          }
        }
      }
      else Future.successful(Redirect(routes.AddInvestorOrNomineeController.show()))
    }

    for {
      shareIssueDate <- s4lConnector.fetchAndGetFormData[ShareIssueDateModel](KeystoreKeys.shareIssueDate)
      backUrl <- s4lConnector.fetchAndGetFormData[String](KeystoreKeys.backLinkNumberOfSharesPurchased)
      route <- process(shareIssueDate, backUrl)
    } yield route
  }

  def submit(shareIssueDate: Option[String]): Action[AnyContent] = AuthorisedAndEnrolled.async { implicit user =>
    implicit request =>
      numberOfSharesPurchasedForm.bindFromRequest().fold(
        formWithErrors => {
          ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkNumberOfSharesPurchased, s4lConnector).flatMap(url =>
            s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map {
              case Some(data) => {
                val investorDetailsModel = data.last
                BadRequest(NumberOfSharesPurchased(investorDetailsModel.companyOrIndividualModel.get.companyOrIndividual,
                  shareIssueDate.get, formWithErrors, url.get))
              }
            })
        },
        validFormData => {
          validFormData.processingId match {
            case Some(_) => PreviousInvestorsHelper.updateNumOfSharesPurchasedDetails(s4lConnector, validFormData).map {
              investorDetailsModel => {
                s4lConnector.saveFormData(KeystoreKeys.backLinkHowMuchSpentOnShares,
                  routes.NumberOfSharesPurchasedController.show(investorDetailsModel.processingId.get).url)
                Redirect(routes.HowMuchSpentOnSharesController.show(investorDetailsModel.processingId.get))
              }
            }
            case None => PreviousInvestorsHelper.addNumOfSharesPurchasedDetails(s4lConnector, validFormData).map {
              investorDetailsModel => {
                s4lConnector.saveFormData(KeystoreKeys.backLinkHowMuchSpentOnShares,
                  routes.NumberOfSharesPurchasedController.show(investorDetailsModel.processingId.get).url)
                Redirect(routes.HowMuchSpentOnSharesController.show(investorDetailsModel.processingId.get))
              }
            }
          }
        }
      )
  }
}
