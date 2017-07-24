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
import controllers.predicates.FeatureSwitch
import forms.CompanyDetailsForm._
import models.InvestorDetailsModel
import play.api.Play.current
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
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

  val show = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>
      s4lConnector.fetchAndGetFormData[InvestorDetailsModel](KeystoreKeys.investorDetails).map {
        case Some(data) if data.companyDetailsModel.isDefined =>
          Ok(CompanyDetails(companyDetailsForm.fill(data.companyDetailsModel.get), countriesList))
        case Some(data) if data.companyDetailsModel.isEmpty =>
          Ok(CompanyDetails(companyDetailsForm, countriesList))
        case None => Redirect(routes.AddInvestorOrNomineeController.show())
      }
    }
  }

  val submit = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>
      companyDetailsForm.bindFromRequest().fold(
        formWithErrors => {
          Future.successful(BadRequest(CompanyDetails(if (formWithErrors.hasGlobalErrors)
            formWithErrors.discardingErrors.withError("companyPostcode", Messages("validation.error.countrypostcode"))
          else formWithErrors, countriesList)))
        },
        validFormData => {
          s4lConnector.fetchAndGetFormData[InvestorDetailsModel](KeystoreKeys.investorDetails).map {
            case Some(investorDetailsModel) => {
              s4lConnector.saveFormData[InvestorDetailsModel](KeystoreKeys.investorDetails,
                investorDetailsModel.copy(companyDetailsModel = Some(validFormData)))
            }
          }
          Future.successful(Redirect(routes.CompanyDetailsController.show()))
        }
      )
    }
  }
}
