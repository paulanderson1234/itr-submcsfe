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
import controllers.Helpers.{ControllerHelpers, PreviousInvestorsHelper}
import controllers.predicates.FeatureSwitch
import forms.CompanyOrIndividualForm._
import models.investorDetails.InvestorDetailsModel
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
                  if (model.get.companyOrIndividualModel.isDefined)
                    Ok(CompanyOrIndividual(useInvestorOrNomineeValueAsHeadingText(model.get.investorOrNomineeModel.get),
                      companyOrIndividualForm.fill(model.get.companyOrIndividualModel.get), backUrl.get))
                  else
                    Ok(CompanyOrIndividual(useInvestorOrNomineeValueAsHeadingText(model.get.investorOrNomineeModel.get),
                      companyOrIndividualForm, backUrl.get))
                }
                else {
                  // Set back to the review page later
                  Redirect(routes.AddInvestorOrNomineeController.show())
                }
              }
              case None => {
                Redirect(controllers.seis.routes.AddInvestorOrNomineeController.show())
              }
            }
          }
          else {
            // No back URL so send them back to any page as per the requirement
            Future.successful(Redirect(controllers.seis.routes.AddInvestorOrNomineeController.show()))
          }
        }

        for {
          backUrl <- s4lConnector.fetchAndGetFormData[String](KeystoreKeys.backLinkCompanyOrIndividual)
          route <- process(backUrl)
        } yield route
    }
  }


  def submit(backUrl: Option[String]): Action[AnyContent] = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user =>
      implicit request =>
        companyOrIndividualForm.bindFromRequest().fold(
          formWithErrors => {
            s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map {
              case Some(data) => {
                val investorDetailsModel = data.last
                BadRequest(CompanyOrIndividual(useInvestorOrNomineeValueAsHeadingText(investorDetailsModel.investorOrNomineeModel.get),
                  formWithErrors, backUrl.get))
              }
            }
          },
          validFormData => {
            validFormData.processingId match {
              case Some(_) => PreviousInvestorsHelper.updateCompanyOrIndividual(s4lConnector, validFormData).map {
                investorDetailsModel => {
                  s4lConnector.saveFormData(KeystoreKeys.backLinkCompanyAndIndividualBoth,
                    routes.CompanyOrIndividualController.show(investorDetailsModel.processingId.get).url)
                  validFormData.companyOrIndividual match {
                    case Constants.typeCompany => Redirect(routes.CompanyDetailsController.show(investorDetailsModel.processingId.get))
                    case Constants.typeIndividual => Redirect(routes.IndividualDetailsController.show(investorDetailsModel.processingId.get))
                  }
                }
              }
              case None => PreviousInvestorsHelper.addCompanyOrIndividual(s4lConnector, validFormData).map {
                investorDetailsModel => {
                  s4lConnector.saveFormData(KeystoreKeys.backLinkCompanyAndIndividualBoth,
                    routes.CompanyOrIndividualController.show(investorDetailsModel.processingId.get).url)
                  validFormData.companyOrIndividual match {
                    case Constants.typeCompany => Redirect(routes.CompanyDetailsController.show(investorDetailsModel.processingId.get))
                    case Constants.typeIndividual => Redirect(routes.IndividualDetailsController.show(investorDetailsModel.processingId.get))
                  }
                }
              }
            }
          }
        )
    }
  }
}
