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
import forms.AddInvestorOrNomineeForm.addInvestorOrNomineeForm
import forms.CompanyOrIndividualForm._
import models.{AddInvestorOrNomineeModel, InvestorDetailsModel}
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.seis.investors.CompanyOrIndividual

import scala.concurrent.Future

object CompanyOrIndividualController extends CompanyOrIndividualController
{
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait CompanyOrIndividualController extends FrontendController with AuthorisedAndEnrolledForTAVC with FeatureSwitch with ControllerHelpers {

  override val acceptedFlows = Seq(Seq(SEIS))

  def show(id: Option[Int]): Action[AnyContent] = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user =>
      implicit request =>

        s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map {
          case Some(data) => {
            id match {
              case Some(idVal) => {
                val itemToUpdateIndex = data.indexWhere(_.processingId.getOrElse(0) == idVal)
                if (itemToUpdateIndex != -1) {
                  val model = data.lift(itemToUpdateIndex)
                  Ok(CompanyOrIndividual(useInvestorOrNomineeValueAsHeadingText(model.get.investorOrNomineeModel.get),
                    companyOrIndividualForm.fill(model.get.companyOrIndividualModel.get)))
                }
                else {
                  // Set to the review screen
                  val investorDetailsModel = data.last
                  val itemToUpdateIndex = data.indexWhere(_.processingId.getOrElse(0) ==
                    investorDetailsModel.processingId.getOrElse(0))
                  val model = data.lift(itemToUpdateIndex)
                  Ok(CompanyOrIndividual(useInvestorOrNomineeValueAsHeadingText(model.get.investorOrNomineeModel.get),
                    companyOrIndividualForm.fill(model.get.companyOrIndividualModel.get)))
                }
              }
              case None => {
                val investorDetailsModel = data.last
                val itemToUpdateIndex = data.indexWhere(_.processingId.getOrElse(0) ==
                  investorDetailsModel.processingId.getOrElse(0))
                val model = data.lift(itemToUpdateIndex)
                Ok(CompanyOrIndividual(useInvestorOrNomineeValueAsHeadingText(model.get.investorOrNomineeModel.get),
                  companyOrIndividualForm))
              }
            }
          }
          case None => {
            // Set back to the review later
            Redirect(routes.AddInvestorOrNomineeController.show())
          }
        }
    }
  }

  val submit = featureSwitch(applicationConfig.seisFlowEnabled) { AuthorisedAndEnrolled.async { implicit user => implicit request =>
    companyOrIndividualForm.bindFromRequest().fold(
      formWithErrors => {
        s4lConnector.fetchAndGetFormData[AddInvestorOrNomineeModel](KeystoreKeys.addInvestor).map {
          data => BadRequest(CompanyOrIndividual(useInvestorOrNomineeValueAsHeadingText(data.get), formWithErrors))
        }
      },
      validFormData => {
        s4lConnector.fetchAndGetFormData[InvestorDetailsModel](KeystoreKeys.investorDetails).map {
          case Some(investorDetailsModel) => {
            s4lConnector.saveFormData[InvestorDetailsModel](KeystoreKeys.investorDetails,
              investorDetailsModel.copy(companyOrIndividualModel = Some(validFormData)))
          }
        }
        validFormData.companyOrIndividual match {
          case Constants.typeCompany => Future.successful(Redirect(routes.CompanyDetailsController.show()))
          case Constants.typeIndividual => Future.successful(Redirect(routes.IndividualDetailsController.show()))
        }
      }
    )
  }
  }
}
