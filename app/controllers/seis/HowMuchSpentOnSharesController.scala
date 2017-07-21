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
import controllers.predicates.FeatureSwitch
import controllers.Helpers.ControllerHelpers._
import forms.HowMuchSpentOnSharesForm._
import models.{CompanyOrIndividualModel, HowMuchSpentOnSharesModel}
import play.api.Play.current
import play.api.data.Form
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Action, AnyContent, Result}
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.seis.investors.HowMuchSpentOnShares

import scala.concurrent.Future

object HowMuchSpentOnSharesController extends HowMuchSpentOnSharesController {
  override lazy val enrolmentConnector: EnrolmentConnector = EnrolmentConnector
  override lazy val applicationConfig: AppConfig = FrontendAppConfig
  override lazy val s4lConnector: S4LConnector = S4LConnector
  override lazy val authConnector: AuthConnector = FrontendAuthConnector
}

trait HowMuchSpentOnSharesController extends FrontendController with AuthorisedAndEnrolledForTAVC with FeatureSwitch {

  override val acceptedFlows = Seq(Seq(SEIS))

  val show: Action[AnyContent] = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async {
      implicit user =>
        implicit request =>

          def routeRequest(companyOrIndividual: Option[CompanyOrIndividualModel]) = if (companyOrIndividual.isDefined) {
            s4lConnector.fetchAndGetFormData[HowMuchSpentOnSharesModel](KeystoreKeys.howMuchSpentOnShares).map {
              case Some(data) => Ok(HowMuchSpentOnShares(companyOrIndividual.get.companyOrIndividual, howMuchSpentOnSharesForm.fill(data)))
              case None => Ok(HowMuchSpentOnShares(companyOrIndividual.get.companyOrIndividual, howMuchSpentOnSharesForm))
            }
          }

          else Future.successful(Redirect(routes.CompanyOrIndividualController.show()))

              for {
                companyOrIndividual <- s4lConnector.fetchAndGetFormData[CompanyOrIndividualModel](KeystoreKeys.companyOrIndividual)
                route <- routeRequest(companyOrIndividual)
              }
                yield route
            }

  }


    val submit: Action[AnyContent] = featureSwitch(applicationConfig.seisFlowEnabled) {
      AuthorisedAndEnrolled.async {
        implicit user =>
          implicit request =>
            val success: HowMuchSpentOnSharesModel => Future[Result] = { model =>
              s4lConnector.saveFormData(KeystoreKeys.howMuchSpentOnShares, model).map(_ =>
                Redirect(controllers.seis.routes.HowMuchSpentOnSharesController.show())
              )
            }

            val failure: Form[HowMuchSpentOnSharesModel] => Future[Result] = { formWithErrors =>
              s4lConnector.fetchAndGetFormData[CompanyOrIndividualModel](KeystoreKeys.companyOrIndividual).map {
               data =>  BadRequest(HowMuchSpentOnShares(data.get.companyOrIndividual, formWithErrors))
              }
            }

              howMuchSpentOnSharesForm.bindFromRequest().fold(failure, success)

              //            val submit = featureSwitch(applicationConfig.seisFlowEnabled) { AuthorisedAndEnrolled.async { implicit user => implicit request =>
              //              companyOrIndividualForm.bindFromRequest().fold(
              //                formWithErrors => {
              //                  s4lConnector.fetchAndGetFormData[AddInvestorOrNomineeModel](KeystoreKeys.addInvestor).map {
              //                    data => BadRequest(CompanyOrIndividual(useInvestorOrNomineeValueAsHeadingText(data.get), formWithErrors))
              //                  }
              //                },
              //                validFormData => {
              //                  s4lConnector.saveFormData(KeystoreKeys.companyOrIndividual, validFormData)
              //                  validFormData.companyOrIndividual match {
              //                    case Constants.typeCompany => Future.successful(Redirect(routes.CompanyDetailsController.show()))
              //                    case Constants.typeIndividual => Future.successful(Redirect(routes.IndividualDetailsController.show()))
              //                  }
              //

//(HowMuchSpentOnShares(useCompanyOrIndividualAsHeadingText(companyOrIndividual.get)
      }
    }
  }

