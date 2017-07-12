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
import controllers.Helpers.ControllerHelpers
import controllers.predicates.FeatureSwitch
import forms.AddInvestorOrNomineeForm._
import models.AddInvestorOrNomineeModel
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
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

trait AddInvestorOrNomineeController extends FrontendController with AuthorisedAndEnrolledForTAVC with FeatureSwitch {
  override val acceptedFlows = Seq(Seq(SEIS))

  val show = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user =>
      implicit request =>
        def routeRequest(backUrl: Option[String]) = {
          if (backUrl.isDefined) {
            s4lConnector.fetchAndGetFormData[AddInvestorOrNomineeModel](KeystoreKeys.addInvestor).map {
              case Some(data) => Ok(AddInvestorOrNominee(addInvestorOrNomineeForm.fill(data), backUrl.get))
              case None => Ok(AddInvestorOrNominee(addInvestorOrNomineeForm, backUrl.get))
            }
          }
          // inconsistent data navigate to beginning of flow
          else {
            Future.successful(Redirect(controllers.seis.routes.ShareDescriptionController.show()))
          }
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
            s4lConnector.saveFormData[AddInvestorOrNomineeModel](KeystoreKeys.addInvestor, validFormData)
              Future.successful(Redirect(routes.AddInvestorOrNomineeController.show()))
          }
        )
    }
  }
}
