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
import models.investorDetails._
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.Future

object ReviewInvestorDetailsController extends ReviewInvestorDetailsController {
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait ReviewInvestorDetailsController extends FrontendController with AuthorisedAndEnrolledForTAVC with FeatureSwitch with ControllerHelpers {

  override val acceptedFlows = Seq(Seq(SEIS))

  val show: Int => Action[AnyContent] = id => featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>
      s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map { vector =>
        redirectNoInvestors(vector) { data =>
          val itemToUpdateIndex = getInvestorIndex(id, data)
          redirectInvalidInvestor(itemToUpdateIndex) { index =>
            Ok(views.html.seis.investors.ReviewInvestorDetails(retrieveInvestorData(index, data)(model => Option(model)).get))
          }
        }
      }
    }
  }

  def change(actionType: String, id: Int): Action[AnyContent] = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user =>
      implicit request =>
        actionType match {
          case Constants.HowMuchSpentOnSharesController => {
            s4lConnector.saveFormData(KeystoreKeys.backLinkHowMuchSpentOnShares,
              routes.ReviewInvestorDetailsController.show(id).url)
            Future.successful(Redirect(routes.HowMuchSpentOnSharesController.show(id)))
          }
          case Constants.AddInvestorOrNomineeController => {
            s4lConnector.saveFormData(KeystoreKeys.backLinkAddInvestorOrNominee,
              routes.ReviewInvestorDetailsController.show(id).url)
            Future.successful(Redirect(routes.AddInvestorOrNomineeController.show(Some(id))))
          }
          case Constants.CompanyOrIndividualController => {
            s4lConnector.saveFormData(KeystoreKeys.backLinkCompanyOrIndividual,
              routes.ReviewInvestorDetailsController.show(id).url)
            Future.successful(Redirect(routes.CompanyOrIndividualController.show(id)))
          }
          case Constants.CompanyDetailsController => {
            s4lConnector.saveFormData(KeystoreKeys.backLinkCompanyAndIndividualBoth,
              routes.ReviewInvestorDetailsController.show(id).url)
            Future.successful(Redirect(routes.CompanyDetailsController.show(id)))
          }
          case Constants.IndividualDetailsController => {
            s4lConnector.saveFormData(KeystoreKeys.backLinkCompanyAndIndividualBoth,
              routes.ReviewInvestorDetailsController.show(id).url)
            Future.successful(Redirect(routes.IndividualDetailsController.show(id)))
          }
          case Constants.NumberOfSharesPurchasedController => {
            s4lConnector.saveFormData(KeystoreKeys.backLinkNumberOfSharesPurchased,
              routes.ReviewInvestorDetailsController.show(id).url)
            Future.successful(Redirect(routes.NumberOfSharesPurchasedController.show(id)))
          }
          case Constants.IsExistingShareHolderController => {
            s4lConnector.saveFormData(KeystoreKeys.backLinkIsExistingShareHolder,
              routes.ReviewInvestorDetailsController.show(id).url)
            Future.successful(Redirect(routes.IsExistingShareHolderController.show(id)))
          }
        }
    }
  }
}