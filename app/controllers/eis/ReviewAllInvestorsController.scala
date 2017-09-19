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

import auth.{AuthorisedAndEnrolledForTAVC, EIS}
import common.KeystoreKeys
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.Helpers.ControllerHelpers
import models.investorDetails.InvestorDetailsModel
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.eis.investors.ReviewAllInvestors

import scala.concurrent.Future

object ReviewAllInvestorsController extends ReviewAllInvestorsController
{
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait ReviewAllInvestorsController extends FrontendController with AuthorisedAndEnrolledForTAVC with ControllerHelpers {

  override val acceptedFlows = Seq(Seq(EIS))

  val show = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map {
      case Some(investors) => if (investors.nonEmpty) Ok(ReviewAllInvestors(investors)) else Redirect(routes.AddInvestorOrNomineeController.show(None))
      case None => Redirect(routes.AddInvestorOrNomineeController.show(None))
    }
  }

  val submit = AuthorisedAndEnrolled.async { implicit user => implicit request =>

    s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map { vector =>
      redirectEisNoInvestors(vector) { data =>
        if (data.forall(_.validate))
          Redirect(routes.ProcessRepaymentsController.show())
        else Redirect(routes.ReviewAllInvestorsController.show())
      }
    }
  }

  def change(id: Int): Action[AnyContent] = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map { vector =>
      redirectEisNoInvestors(vector) { data =>
        if (!data(getInvestorIndex(id, data)).validate || data.forall(_.validate))
          Redirect(routes.ReviewInvestorDetailsController.show(id))
        else Redirect(routes.ReviewAllInvestorsController.show())
      }
    }
  }

  def add: Action[AnyContent] = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map { vector =>
      redirectEisNoInvestors(vector) { data =>
        if (data.forall(_.validate))
          Redirect(routes.AddInvestorOrNomineeController.show())
        else Redirect(routes.ReviewAllInvestorsController.show())
      }
    }
  }

  def remove(id: Int): Action[AnyContent] = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    Future.successful(Redirect(routes.DeleteInvestorController.show(id)))
  }
}