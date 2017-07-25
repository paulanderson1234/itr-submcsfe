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
import controllers.Helpers.ControllerHelpers
import controllers.predicates.FeatureSwitch
import forms.InvestorShareIssueDateForm._
import models.investorDetails.InvestorShareIssueDateModel
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.seis.investors.InvestorShareIssueDate
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future

object InvestorShareIssueDateController extends InvestorShareIssueDateController{
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait InvestorShareIssueDateController extends FrontendController with AuthorisedAndEnrolledForTAVC with FeatureSwitch {

  override val acceptedFlows = Seq(Seq(SEIS))

  val show = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>
        s4lConnector.fetchAndGetFormData[InvestorShareIssueDateModel](KeystoreKeys.investorShareIssueDate).map {
          case Some(data) => Ok(InvestorShareIssueDate(investorShareIssueDateForm.fill(data)))
          case None => Ok(InvestorShareIssueDate(investorShareIssueDateForm))
        }
    }
  }

  val submit = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>
      investorShareIssueDateForm.bindFromRequest().fold(
        formWithErrors => {
           Future.successful(BadRequest(InvestorShareIssueDate(formWithErrors)))
          //TODO route should be "Existing Shareholder?" page
        },
        validFormData => {
          s4lConnector.saveFormData(KeystoreKeys.investorShareIssueDate, validFormData)
          Future.successful(Redirect(routes.AddInvestorOrNomineeController.show()))
          //TODO Successful route should be "How many shares were bought" page
        }
      )
    }
  }
}