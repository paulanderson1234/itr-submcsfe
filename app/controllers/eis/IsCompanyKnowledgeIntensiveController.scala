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
import forms.IsCompanyKnowledgeIntensiveForm._
import models.{IsCompanyKnowledgeIntensiveModel, IsKnowledgeIntensiveModel, KiProcessingModel}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.mvc._
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future
import views.html.eis.companyDetails
import views.html.eis.companyDetails.IsCompanyKnowledgeIntensive

object IsCompanyKnowledgeIntensiveController extends IsCompanyKnowledgeIntensiveController{
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait IsCompanyKnowledgeIntensiveController extends FrontendController with AuthorisedAndEnrolledForTAVC {

  override val acceptedFlows = Seq(Seq(EIS))

  val show = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    s4lConnector.fetchAndGetFormData[IsCompanyKnowledgeIntensiveModel](KeystoreKeys.isCompanyKnowledgeIntensive).map {
      case Some(data) => Ok(companyDetails.IsCompanyKnowledgeIntensive(isCompanyKnowledgeIntensiveForm.fill(data)))
      case None => Ok(companyDetails.IsCompanyKnowledgeIntensive(isCompanyKnowledgeIntensiveForm))
    }
  }

  val submit = AuthorisedAndEnrolled.async { implicit user => implicit request =>

    def routeRequest(kiModel: Option[KiProcessingModel], isCompanyAssertingKnowledgeIntensive: Boolean): Future[Result] = {
      kiModel match {
        case Some(data) if data.dateConditionMet.isEmpty => {
          Future.successful(Redirect(routes.DateOfIncorporationController.show()))
        }
        case Some(dataWithDateCondition) => {
          if (!isCompanyAssertingKnowledgeIntensive) {
            // user has said not knowledge intensive so reset the KIProcessing model so it correctly calculates the KI Flag
            // Clear the processing data (keeping the date and is  company KI info)
            s4lConnector.saveFormData(KeystoreKeys.kiProcessingModel, KiProcessingModel(companyAssertsIsKi = Some(isCompanyAssertingKnowledgeIntensive),
              dateConditionMet = dataWithDateCondition.dateConditionMet))
            s4lConnector.saveFormData(KeystoreKeys.backLinkFullTimeEmployeeCount, routes.IsCompanyKnowledgeIntensiveController.show().url)

            // if user is not asserting they are KI need to clear any previous answer model for whether they also want to apply as Ki
            s4lConnector.saveFormData(KeystoreKeys.isKnowledgeIntensive, IsKnowledgeIntensiveModel(Constants.StandardRadioButtonNoValue))
            Future.successful(Redirect(routes.FullTimeEmployeeCountController.show()))
          }
          else {
            // said is ki
            s4lConnector.saveFormData(KeystoreKeys.kiProcessingModel,
              dataWithDateCondition.copy(companyAssertsIsKi = Some(isCompanyAssertingKnowledgeIntensive)))
            Future.successful(Redirect(routes.IsKnowledgeIntensiveController.show()))
          }
        }
        case None => Future.successful(Redirect(routes.DateOfIncorporationController.show()))
      }
    }

    isCompanyKnowledgeIntensiveForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(IsCompanyKnowledgeIntensive(formWithErrors)))
      },
      validFormData => {
        s4lConnector.saveFormData(KeystoreKeys.isCompanyKnowledgeIntensive, validFormData)
        for {
          kiModel <- s4lConnector.fetchAndGetFormData[KiProcessingModel](KeystoreKeys.kiProcessingModel)
          route <- routeRequest(kiModel, if (validFormData.isCompanyKnowledgeIntensive == Constants.StandardRadioButtonYesValue) true else false)
        } yield route
      }
    )
  }
}
