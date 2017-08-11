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
import common.{KeystoreKeys, Constants}
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.Helpers.ControllerHelpers
import controllers.predicates.FeatureSwitch
import models.{AddInvestorOrNomineeModel, IndividualDetailsModel, CompanyDetailsModel, CompanyOrIndividualModel}
import models.investorDetails.{IsExistingShareHolderModel, HowMuchSpentOnSharesModel, NumberOfSharesPurchasedModel, InvestorDetailsModel}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import views.html.seis.investors.ReviewAllInvestors
import play.api.mvc.{Action, AnyContent, _}
import scala.concurrent.Future

object ReviewAllInvestorsController extends ReviewAllInvestorsController
{
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait ReviewAllInvestorsController extends FrontendController with AuthorisedAndEnrolledForTAVC with FeatureSwitch with ControllerHelpers{

  override val acceptedFlows = Seq(Seq(SEIS))

  val show = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>
      s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map {
        case Some(investors) => Ok(ReviewAllInvestors(investors))
        case None => Redirect(routes.AddInvestorOrNomineeController.show(None))
      }
    }
  }


  val submit = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>
      s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map { vector =>
        redirectNoInvestors(vector) { data =>
          if (data.forall(_.validate))
            Redirect(routes.AddAnotherInvestorController.show())
          else Redirect(routes.ReviewAllInvestorsController.show())
        }
      }
    }
  }


  def change(id: Int): Action[AnyContent] = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>
      s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map { vector =>
        redirectNoInvestors(vector) { data =>
          if(data(getInvestorIndex(id,data)).validate || data.forall(_.validate))
            Redirect(routes.ReviewInvestorDetailsController.show(id))
          else Redirect(routes.ReviewAllInvestorsController.show())
        }
      }

    }
  }

  def add: Action[AnyContent] = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>
      s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map { vector =>
        redirectNoInvestors(vector) { data =>
          if (data.forall(_.validate))
            Redirect(routes.AddInvestorOrNomineeController.show())
          else Redirect(routes.ReviewAllInvestorsController.show())
          }
      }
    }
  }

  def remove(id: Int): Action[AnyContent] = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>
      //TODO redirect to investor delete page with correct ID
      Future.successful(Redirect(""))
    }
  }
}
