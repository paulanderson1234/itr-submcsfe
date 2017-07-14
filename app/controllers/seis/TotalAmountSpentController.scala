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

import forms.TotalAmountSpentForm
import models.TotalAmountSpentModel
import forms.TotalAmountSpentForm._
import play.api.Play.current
import play.api.data.Form
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Action, AnyContent, Result}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import auth.{AuthorisedAndEnrolledForTAVC, SEIS}
import common.KeystoreKeys
import config.{AppConfig, FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.predicates.FeatureSwitch

import scala.concurrent.Future

object TotalAmountSpentController extends TotalAmountSpentController {
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait TotalAmountSpentController extends FrontendController with AuthorisedAndEnrolledForTAVC with FeatureSwitch {

  override val acceptedFlows = Seq(Seq(SEIS))

  val show: Action[AnyContent] = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async {
      implicit user =>
        implicit request =>
          s4lConnector.fetchAndGetFormData[TotalAmountSpentModel](KeystoreKeys.totalAmountSpent).map {
            case Some(data) => Ok(views.html.seis.shareDetails.TotalAmountSpent(totalAmountSpentForm.fill(data)))
            case None => Ok(views.html.seis.shareDetails.TotalAmountSpent(totalAmountSpentForm))
          }
    }
  }

  val submit: Action[AnyContent] = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async {
      implicit user =>
        implicit request =>
          val success: TotalAmountSpentModel => Future[Result] = { model =>
            s4lConnector.saveFormData(KeystoreKeys.totalAmountSpent, model).map(_ => {
              s4lConnector.saveFormData(KeystoreKeys.backLinkAddInvestorOrNominee, routes.TotalAmountSpentController.show().url)
              Redirect(controllers.seis.routes.AddInvestorOrNomineeController.show())
            }
            )
          }

          val failure: Form[TotalAmountSpentModel] => Future[Result] = { form =>
            Future.successful(BadRequest(views.html.seis.shareDetails.TotalAmountSpent(form)))
          }

          TotalAmountSpentForm.totalAmountSpentForm.bindFromRequest().fold(failure, success)
    }
  }
}


