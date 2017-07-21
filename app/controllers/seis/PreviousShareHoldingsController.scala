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
import controllers.Helpers.ControllerHelpers
import controllers.predicates.FeatureSwitch
import models.{CompanyOrIndividualModel, PreviousShareHoldingsModel}
import forms.CompanyOrIndividualForm._
import forms.PreviousShareHoldingsForm._
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import views.html.seis.investors.PreviousShareHoldings

import scala.concurrent.Future

object PreviousShareHoldingsController extends PreviousShareHoldingsController
{
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait PreviousShareHoldingsController extends FrontendController with AuthorisedAndEnrolledForTAVC with FeatureSwitch with ControllerHelpers{

  override val acceptedFlows = Seq(Seq(SEIS))

  val show = featureSwitch(applicationConfig.seisFlowEnabled) { AuthorisedAndEnrolled.async {
    implicit user =>
      implicit request =>

    def routeRequest(companyOrIndividual: Option[CompanyOrIndividualModel]) = if (companyOrIndividual.isDefined) {
      s4lConnector.fetchAndGetFormData[PreviousShareHoldingsModel](KeystoreKeys.previousShareHoldings).map {
          case Some(data) => Ok(PreviousShareHoldings(companyOrIndividual.get.companyOrIndividual, previousShareHoldingsForm.fill(data)))
          case None => Ok(PreviousShareHoldings(companyOrIndividual.get.companyOrIndividual, previousShareHoldingsForm))
        }
      }
      else Future.successful(Redirect(routes.AddInvestorOrNomineeController.show()))


    for {
      companyOrIndividual <- s4lConnector.fetchAndGetFormData[CompanyOrIndividualModel](KeystoreKeys.companyOrIndividual)
      route <- routeRequest(companyOrIndividual)
    } yield route
  }
  }

  val submit = featureSwitch(applicationConfig.seisFlowEnabled) { AuthorisedAndEnrolled.async { implicit user => implicit request =>
    previousShareHoldingsForm.bindFromRequest().fold(
      formWithErrors => {
        s4lConnector.fetchAndGetFormData[PreviousShareHoldingsModel](KeystoreKeys.previousShareHoldings).map {
          data => BadRequest(PreviousShareHoldings(data.get., formWithErrors))
        }
      },
      validFormData => {
        s4lConnector.saveFormData(KeystoreKeys.companyOrIndividual, validFormData)
        validFormData.companyOrIndividual match {
          case Constants.typeCompany => Future.successful(Redirect(routes.CompanyDetailsController.show()))
          case Constants.typeIndividual => Future.successful(Redirect(routes.IndividualDetailsController.show()))
        }
      }
    )
  }
  }
}
