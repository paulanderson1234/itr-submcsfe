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

import auth.{AuthorisedAndEnrolledForTAVC, EIS, VCT}
import common.{Constants, KeystoreKeys}
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.predicates.FeatureSwitch
import forms.QualifyBusinessActivityForm
import models.QualifyBusinessActivityModel
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.eis.companyDetails.QualifyBusinessActivity
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future

object QualifyBusinessActivityController extends QualifyBusinessActivityController
{
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait QualifyBusinessActivityController extends FrontendController with AuthorisedAndEnrolledForTAVC with FeatureSwitch {

  override val acceptedFlows = Seq(Seq(EIS),Seq(VCT),Seq(EIS,VCT))

  val show = featureSwitch(applicationConfig.eisFlowEnabled) {
    AuthorisedAndEnrolled.async {
      implicit user =>
        implicit request => {
          s4lConnector.fetchAndGetFormData[QualifyBusinessActivityModel](KeystoreKeys.isQualifyBusinessActivity).map {
            case Some(data) => Ok(QualifyBusinessActivity(QualifyBusinessActivityForm.qualifyBusinessActivityForm.fill(data)))
            case None => Ok(QualifyBusinessActivity(QualifyBusinessActivityForm.qualifyBusinessActivityForm))
          }
        }
    }

  }

  val submit = featureSwitch(applicationConfig.eisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>
      QualifyBusinessActivityForm.qualifyBusinessActivityForm.bindFromRequest().fold(
        formWithErrors => {
          Future.successful(BadRequest(QualifyBusinessActivity(formWithErrors)))
        },
        validFormData => {
          s4lConnector.saveFormData(KeystoreKeys.isQualifyBusinessActivity, validFormData)
          validFormData.isQualifyBusinessActivity match {

            case Constants.qualifyPrepareToTrade => {
              //TODO Future.successful(Redirect(routes.HasInvestmentTradeStartedController.show()))
              Future.successful(Redirect(routes.IneligibleForKIController.show()))
            }
            case Constants.qualifyResearchAndDevelopment => {
              //TODO Future.successful(Redirect(routes.ResearchStartDateController.show()))
              Future.successful(Redirect(routes.ContactDetailsController.show()))
            }
          }
        }
      )
    }
  }
}
