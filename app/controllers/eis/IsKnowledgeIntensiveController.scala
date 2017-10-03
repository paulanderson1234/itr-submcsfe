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
import forms.IsKnowledgeIntensiveForm._
import models.{IsKnowledgeIntensiveModel, KiProcessingModel}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.mvc._
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import scala.concurrent.Future
import views.html.eis.companyDetails
import views.html.eis.companyDetails.IsKnowledgeIntensive

object IsKnowledgeIntensiveController extends IsKnowledgeIntensiveController{
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait IsKnowledgeIntensiveController extends FrontendController with AuthorisedAndEnrolledForTAVC {

  override val acceptedFlows = Seq(Seq(EIS))

  val show = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    s4lConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](KeystoreKeys.isKnowledgeIntensive).map {
      case Some(data) => Ok(companyDetails.IsKnowledgeIntensive(isKnowledgeIntensiveForm.fill(data)))
      case None => Ok(companyDetails.IsKnowledgeIntensive(isKnowledgeIntensiveForm))
    }
  }

  val submit = AuthorisedAndEnrolled.async { implicit user => implicit request =>

    def routeRequest(kiModel: Option[KiProcessingModel], wantsToApplyKi: Boolean): Future[Result] = {
      kiModel match {
        case Some(data) if data.dateConditionMet.isEmpty => {
          Future.successful(Redirect(routes.DateOfIncorporationController.show()))
        }
        case Some(data) if data.companyAssertsIsKi.isEmpty => {
          Future.successful(Redirect(routes.IsCompanyKnowledgeIntensiveController.show()))
        }
        case Some(dataWithDateCondition) => {
          if (!wantsToApplyKi & dataWithDateCondition.companyWishesToApplyKi.getOrElse(false)) {
            // user changed from yes to no. Clear the processing data (keeping the date and isKi info)
            s4lConnector.saveFormData(KeystoreKeys.kiProcessingModel,
              KiProcessingModel(companyAssertsIsKi = dataWithDateCondition.companyAssertsIsKi,
                dateConditionMet = dataWithDateCondition.dateConditionMet, companyWishesToApplyKi = Some(wantsToApplyKi)))

            // clear real data: TODO: it will work for now but we should probably clear the real data to ..how do this??
            //s4lConnector.saveFormData(KeystoreKeys.operatingCosts, Option[OperatingCostsModel] = None)
            //s4lConnector.saveFormData(KeystoreKeys.tenYearPlan, Option[TenYearPlanModel] = None)
            //s4lConnector.saveFormData(KeystoreKeys.percentageStaffWithMasters, PercentageStaffWithMastersModel] = None)

            s4lConnector.saveFormData(KeystoreKeys.backLinkFullTimeEmployeeCount, routes.IsKnowledgeIntensiveController.show().url)
            Future.successful(Redirect(routes.FullTimeEmployeeCountController.show()))
          }
          else {
            s4lConnector.saveFormData(KeystoreKeys.kiProcessingModel,
              dataWithDateCondition.copy(companyWishesToApplyKi = Some(wantsToApplyKi)))
            if (wantsToApplyKi) Future.successful(Redirect(routes.OperatingCostsController.show()))
            else {
              s4lConnector.saveFormData(KeystoreKeys.backLinkFullTimeEmployeeCount, routes.IsKnowledgeIntensiveController.show().url)
              Future.successful(Redirect(routes.FullTimeEmployeeCountController.show()))
            }
          }

        }
        case None => Future.successful(Redirect(routes.DateOfIncorporationController.show()))
      }
    }

    isKnowledgeIntensiveForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(IsKnowledgeIntensive(formWithErrors)))
      },
      validFormData => {
        s4lConnector.saveFormData(KeystoreKeys.isKnowledgeIntensive, validFormData)
        for {
          kiModel <- s4lConnector.fetchAndGetFormData[KiProcessingModel](KeystoreKeys.kiProcessingModel)
          route <- routeRequest(kiModel, if (validFormData.isKnowledgeIntensive == Constants.StandardRadioButtonYesValue) true else false)
        } yield route
      }
    )
  }
}
