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
import forms.CompanyDetailsForm._
import models.CompanyDetailsModel
import models.investorDetails.InvestorDetailsModel
import play.api.Play.current
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Action, AnyContent}
import services.SubscriptionService
import uk.gov.hmrc.play.frontend.controller.FrontendController
import utils.CountriesHelper
import views.html.seis.investors.CompanyDetails

import scala.concurrent.Future

object CompanyDetailsController extends CompanyDetailsController with AuthorisedAndEnrolledForTAVC {

  val subscriptionService = SubscriptionService
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait CompanyDetailsController extends FrontendController with AuthorisedAndEnrolledForTAVC with ControllerHelpers {

  override val acceptedFlows = Seq(Seq(SEIS))

  val subscriptionService: SubscriptionService

  lazy val countriesList = CountriesHelper.getIsoCodeTupleList
  lazy val emptyModelWithUkCode = CompanyDetailsModel("", "", "", None, None, None, Constants.countyCodeGB, None)

  def show(id: Int): Action[AnyContent] = AuthorisedAndEnrolled.async { implicit user =>
    implicit request =>
      def process(backUrl: Option[String]) = {
        if (backUrl.isDefined) {
          s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map { vector =>
            redirectNoInvestors(vector) { data =>
              val itemToUpdateIndex = getInvestorIndex(id, data)
              redirectInvalidInvestor(itemToUpdateIndex) { index =>
                if (data.lift(itemToUpdateIndex).get.companyOrIndividualModel.isDefined) {
                  val form = fillForm(companyDetailsForm, retrieveInvestorData(index, data)(_.companyDetailsModel))
                  Ok(CompanyDetails(if (form.value.isEmpty) companyDetailsForm.fill(emptyModelWithUkCode) else form, countriesList, backUrl.get))
                }
                else Redirect(routes.CompanyOrIndividualController.show(id))
              }
            }
          }
        }
        else Future.successful(Redirect(routes.AddInvestorOrNomineeController.show()))
      }
      for {
        backUrl <- s4lConnector.fetchAndGetFormData[String](KeystoreKeys.backLinkCompanyAndIndividualBoth)
        route <- process(backUrl)
      } yield route
  }

  val submit = AuthorisedAndEnrolled.async { implicit user =>
    implicit request =>
      companyDetailsForm.bindFromRequest().fold(
        formWithErrors => {
          ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkCompanyAndIndividualBoth, s4lConnector).flatMap(url =>
            Future.successful(BadRequest(CompanyDetails(if (formWithErrors.hasGlobalErrors)
              formWithErrors.discardingErrors.withError("companyPostcode", Messages("validation.error.countrypostcode"))
            else formWithErrors, countriesList, url.get))))
        },
        validFormData => {
          validFormData.processingId match {
            case Some(_) => PreviousInvestorsHelper.updateCompanyDetails(s4lConnector, validFormData).map {
              investorDetailsModel => {
                s4lConnector.saveFormData(KeystoreKeys.backLinkNumberOfSharesPurchased,
                  routes.CompanyDetailsController.show(investorDetailsModel.processingId.get).url)
                Redirect(routes.NumberOfSharesPurchasedController.show(investorDetailsModel.processingId.get))
              }
            }
            case None => PreviousInvestorsHelper.addCompanyDetails(s4lConnector, validFormData).map {
              investorDetailsModel => {
                s4lConnector.saveFormData(KeystoreKeys.backLinkNumberOfSharesPurchased,
                  routes.CompanyDetailsController.show(investorDetailsModel.processingId.get).url)
                Redirect(routes.NumberOfSharesPurchasedController.show(investorDetailsModel.processingId.get))
              }
            }
          }
        }
      )
  }
}