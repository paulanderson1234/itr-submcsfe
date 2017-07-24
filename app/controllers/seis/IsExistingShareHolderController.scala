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
import models.CompanyOrIndividualModel
import models.investorDetails.IsExistingShareHolderModel
import forms.CompanyOrIndividualForm._
import forms.IsExistingShareHolderForm._
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import views.html.seis.investors.IsExistingShareHolder

import scala.concurrent.Future

object IsExistingShareHolderController extends IsExistingShareHolderController
{
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait IsExistingShareHolderController extends FrontendController with AuthorisedAndEnrolledForTAVC with FeatureSwitch with ControllerHelpers{

  override val acceptedFlows = Seq(Seq(SEIS))

  val show = featureSwitch(applicationConfig.seisFlowEnabled) { AuthorisedAndEnrolled.async {
    implicit user =>
      implicit request =>

    def routeRequest(companyOrIndividual: Option[CompanyOrIndividualModel]) = if (companyOrIndividual.isDefined) {
      s4lConnector.fetchAndGetFormData[IsExistingShareHolderModel](KeystoreKeys.isExistingShareHolder).map {
          case Some(data) => Ok(IsExistingShareHolder(companyOrIndividual.get.companyOrIndividual, isExistingShareHolderForm.fill(data)))
          case None => Ok(IsExistingShareHolder(companyOrIndividual.get.companyOrIndividual, isExistingShareHolderForm))
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
    isExistingShareHolderForm.bindFromRequest().fold(
      formWithErrors => {
        s4lConnector.fetchAndGetFormData[CompanyOrIndividualModel](KeystoreKeys.companyOrIndividual).map {
          case Some(data) => BadRequest(IsExistingShareHolder(data.companyOrIndividual, formWithErrors))
          case None => InternalServerError
        }
      },
      validFormData => {
        s4lConnector.saveFormData(KeystoreKeys.isExistingShareHolder, validFormData)
        validFormData.isExistingShareHolder match {

          case Constants.StandardRadioButtonYesValue =>
            Future.successful(Redirect(routes.InvestorShareIssueDateController.show()))

          case Constants.StandardRadioButtonNoValue =>
            Future.successful(Redirect(routes.IsExistingShareHolderController.show()))
        }
      }
    )
  }
  }
}
