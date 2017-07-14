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
import models.{AddInvestorOrNomineeModel, CompanyOrIndividualModel}
import forms.CompanyOrIndividualForm._
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import views.html.seis.investors.CompanyOrIndividual

import scala.concurrent.Future

object CompanyOrIndividualController extends CompanyOrIndividualController
{
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait CompanyOrIndividualController extends FrontendController with AuthorisedAndEnrolledForTAVC with FeatureSwitch with ControllerHelpers{

  override val acceptedFlows = Seq(Seq(SEIS))

  val show = featureSwitch(applicationConfig.seisFlowEnabled) { AuthorisedAndEnrolled.async { implicit user => implicit request =>

      def routeRequest(investorOrNominee: Option[AddInvestorOrNomineeModel]) = {
        if (investorOrNominee.isDefined) {
          s4lConnector.fetchAndGetFormData[CompanyOrIndividualModel](KeystoreKeys.companyOrIndividual).map {
            case Some(data) => Ok(CompanyOrIndividual(investorOrNominee.get.addInvestorOrNominee,
              companyOrIndividualForm.fill(data)))
            case None => Ok(CompanyOrIndividual(investorOrNominee.get.addInvestorOrNominee, companyOrIndividualForm))
          }
        } else Future.successful(Redirect(routes.AddInvestorOrNomineeController.show()))
      }

      for {
        investorOrNominee <- s4lConnector.fetchAndGetFormData[AddInvestorOrNomineeModel](KeystoreKeys.addInvestor)
        route <- routeRequest(investorOrNominee)
      } yield route
    }
  }

  val submit = featureSwitch(applicationConfig.seisFlowEnabled) { AuthorisedAndEnrolled.async { implicit user => implicit request =>
    companyOrIndividualForm.bindFromRequest().fold(
      formWithErrors => {
        s4lConnector.fetchAndGetFormData[AddInvestorOrNomineeModel](KeystoreKeys.addInvestor).map {
          data => BadRequest(CompanyOrIndividual(data.get.addInvestorOrNominee, formWithErrors))
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
