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
import controllers.Helpers.PreviousInvestorsHelper
import controllers.predicates.FeatureSwitch
import forms.CompanyDetailsForm._
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

object CompanyDetailsController extends CompanyDetailsController with AuthorisedAndEnrolledForTAVC with FeatureSwitch {

  val subscriptionService = SubscriptionService
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait CompanyDetailsController extends FrontendController with AuthorisedAndEnrolledForTAVC with FeatureSwitch {

  override val acceptedFlows = Seq(Seq(SEIS))

  val subscriptionService: SubscriptionService

  lazy val countriesList = CountriesHelper.getIsoCodeTupleList

  def show(id: Int): Action[AnyContent] = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user =>
      implicit request =>
        def process(backUrl: Option[String]) = {
          if (backUrl.isDefined) {
            s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map {
              case Some(data) => {
                val itemToUpdateIndex = data.indexWhere(_.processingId.getOrElse(0) == id)
                if (itemToUpdateIndex != -1) {
                  val model = data.lift(itemToUpdateIndex)
                  if (model.get.companyDetailsModel.isDefined) {
                    Ok(CompanyDetails(companyDetailsForm.fill(model.get.companyDetailsModel.get), countriesList, backUrl.get))
                  }
                  else
                    Ok(CompanyDetails(companyDetailsForm, countriesList, backUrl.get))
                }
                else {
                  // Set back to the review page later
                  Redirect("")
                }
              }
              case None => {
                Redirect(controllers.seis.routes.AddInvestorOrNomineeController.show())
              }
            }
          }
          else {
            // No back URL so send them back to any page as per the requirement
            Future.successful(Redirect(""))
          }
        }
        for {
          backUrl <- s4lConnector.fetchAndGetFormData[String](KeystoreKeys.backLinkCompanyAndIndividualBoth)
          route <- process(backUrl)
        } yield route
    }
  }

  def submit(backUrl: Option[String]): Action[AnyContent] = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user =>
      implicit request =>
        companyDetailsForm.bindFromRequest().fold(
          formWithErrors => {
            Future.successful(BadRequest(CompanyDetails(if (formWithErrors.hasGlobalErrors)
              formWithErrors.discardingErrors.withError("companyPostcode", Messages("validation.error.countrypostcode"))
            else formWithErrors, countriesList, backUrl.get)))
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
}
