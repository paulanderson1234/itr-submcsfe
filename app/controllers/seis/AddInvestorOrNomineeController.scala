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
import config.{AppConfig, FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.Helpers.{ControllerHelpers, PreviousInvestorsHelper}
import controllers.predicates.FeatureSwitch
import forms.AddInvestorOrNomineeForm._
import models.investorDetails.InvestorDetailsModel
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.seis.investors.AddInvestorOrNominee

import scala.concurrent.Future

object AddInvestorOrNomineeController extends AddInvestorOrNomineeController {
  override lazy val enrolmentConnector: EnrolmentConnector = EnrolmentConnector
  override lazy val applicationConfig: AppConfig = FrontendAppConfig
  override lazy val s4lConnector: S4LConnector = S4LConnector
  override lazy val authConnector: AuthConnector = FrontendAuthConnector
}

trait AddInvestorOrNomineeController extends FrontendController with AuthorisedAndEnrolledForTAVC with FeatureSwitch with ControllerHelpers {
  override val acceptedFlows = Seq(Seq(SEIS))

  def show(id: Option[Int], shareHolderProcessingId: Option[Int]): Action[AnyContent] = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user =>
      implicit request =>
        def routeRequest(backUrl: Option[String]) = {
          if (backUrl.isDefined) {
            s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map {
              case Some(data) =>
                id match {
                  case Some(idVal) => {
                    redirectInvalidInvestor(getInvestorIndex(idVal, data)) { id =>
                      val form = fillForm(addInvestorOrNomineeForm, retrieveInvestorData(id, data)(_.investorOrNomineeModel))
                      Ok(AddInvestorOrNominee(form, backUrl.get))
                    }
                  }
                  case None => {
                    val investorDetailsModel = data.last
                    if (investorDetailsModel.validate) Ok(AddInvestorOrNominee(addInvestorOrNomineeForm, backUrl.get))
                    else Ok(AddInvestorOrNominee(addInvestorOrNomineeForm.fill(investorDetailsModel.investorOrNomineeModel.get),
                      backUrl.get))
                  }
                }
              case _ => Ok(AddInvestorOrNominee(addInvestorOrNomineeForm, backUrl.get))
            }
          }
          else Future.successful(Redirect(controllers.seis.routes.ShareDescriptionController.show()))
        }

        for {
          link <- ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkAddInvestorOrNominee, s4lConnector)
          route <- routeRequest(link)
        } yield route
    }
  }

  val submit = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user =>
      implicit request =>
        addInvestorOrNomineeForm.bindFromRequest().fold(
          formWithErrors => {
            ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkAddInvestorOrNominee, s4lConnector).flatMap(url =>
              Future.successful(BadRequest(AddInvestorOrNominee(formWithErrors, url.get))))
          },
          validFormData => {
            validFormData.processingId match {
              case Some(_) => PreviousInvestorsHelper.updateInvestorOrNominee(s4lConnector, validFormData).map {
                investorDetailsModel => {
                  s4lConnector.saveFormData(KeystoreKeys.backLinkCompanyOrIndividual,
                    routes.AddInvestorOrNomineeController.show(investorDetailsModel.processingId).url)
                  Redirect(routes.CompanyOrIndividualController.show(investorDetailsModel.processingId.get))
                }
              }
              case None => PreviousInvestorsHelper.addInvestorOrNominee(s4lConnector, validFormData).map {
                investorDetailsModel => {
                  s4lConnector.saveFormData(KeystoreKeys.backLinkCompanyOrIndividual,
                    routes.AddInvestorOrNomineeController.show(investorDetailsModel.processingId).url)
                  Redirect(routes.CompanyOrIndividualController.show(investorDetailsModel.processingId.get))
                }
              }
            }
          }
        )
    }
  }
}
