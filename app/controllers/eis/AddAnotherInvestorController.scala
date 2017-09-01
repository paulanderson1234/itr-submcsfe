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
import common.Constants
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector, SubmissionConnector}
import forms.AddAnotherInvestorForm._
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.eis.investors.AddAnotherInvestor

import scala.concurrent.Future

object AddAnotherInvestorController extends AddAnotherInvestorController{
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
  override lazy val submissionConnector = SubmissionConnector

}

trait AddAnotherInvestorController extends FrontendController with AuthorisedAndEnrolledForTAVC {

  override val acceptedFlows = Seq(Seq(EIS))

  val submissionConnector: SubmissionConnector

  val show = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    Future.successful(Ok(AddAnotherInvestor(addAnotherInvestorForm)))
  }


  val submit = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    addAnotherInvestorForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(AddAnotherInvestor(formWithErrors)))
      },
      validFormData => {
        validFormData.addAnotherInvestor match {

          case Constants.StandardRadioButtonYesValue => {
            Future.successful(Redirect(routes.AddInvestorOrNomineeController.show()))
          }
          case Constants.StandardRadioButtonNoValue => {
            Future.successful(Redirect(routes.WasAnyValueReceivedController.show()))
          }
        }
      }
    )
  }
}
