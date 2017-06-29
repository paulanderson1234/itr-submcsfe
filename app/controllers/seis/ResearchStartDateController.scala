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
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector, SubmissionConnector}
import controllers.predicates.FeatureSwitch
import forms.ResearchStartDateForm._
import models.ResearchStartDateModel
import play.api.Play.current
import play.api.data.Form
import play.api.i18n.Messages.Implicits._
import play.api.mvc.Result
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.seis.companyDetails.ResearchStartDate

import scala.concurrent.Future

object ResearchStartDateController extends ResearchStartDateController {
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
  override lazy val submissionConnector = SubmissionConnector
}

trait ResearchStartDateController extends FrontendController with AuthorisedAndEnrolledForTAVC with FeatureSwitch {

  override val acceptedFlows = Seq(Seq(SEIS))


  val submissionConnector: SubmissionConnector

  val show = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>
      s4lConnector.fetchAndGetFormData[ResearchStartDateModel](KeystoreKeys.researchStartDate).map {
        case Some(data) => Ok(ResearchStartDate(researchStartDateForm.fill(data)))
        case _ => Ok(ResearchStartDate(researchStartDateForm))
      }
    }
  }

  val submit = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>

      val successAction: ResearchStartDateModel => Future[Result] = model => {
        s4lConnector.saveFormData[ResearchStartDateModel](KeystoreKeys.researchStartDate, model).map { _ =>
          Redirect(routes.ResearchStartDateController.show()) //TODO set to Share Issue Date page
        }
      }

      val errorAction: Form[ResearchStartDateModel] => Future[Result] = form => {
        Future.successful(BadRequest(ResearchStartDate(form)))
      }

      researchStartDateForm.bindFromRequest.fold(errorAction, successAction)
    }
  }
}
