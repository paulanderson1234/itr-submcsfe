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
import forms.FullTimeEmployeeCountForm._
import models.FullTimeEmployeeCountModel
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import services.SubmissionService
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.seis.companyDetails.FullTimeEmployeeCount

import scala.concurrent.Future

object FullTimeEmployeeCountController extends FullTimeEmployeeCountController {
  override lazy val enrolmentConnector: EnrolmentConnector = EnrolmentConnector
  override lazy val applicationConfig: AppConfig = FrontendAppConfig
  override lazy val s4lConnector: S4LConnector = S4LConnector
  override lazy val authConnector: AuthConnector = FrontendAuthConnector
  val submissionService = SubmissionService
}

trait FullTimeEmployeeCountController extends FrontendController with AuthorisedAndEnrolledForTAVC with FeatureSwitch {
  override val acceptedFlows = Seq(Seq(SEIS))
  val submissionService : SubmissionService

  val show = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user =>
      implicit request =>
        s4lConnector.fetchAndGetFormData[FullTimeEmployeeCountModel](KeystoreKeys.fullTimeEmployeeCount).map {
          case Some(data) => Ok(FullTimeEmployeeCount(fullTimeEmployeeCountForm.fill(data)))
          case None => Ok(FullTimeEmployeeCount(fullTimeEmployeeCountForm))
        }
    }
  }

  val submit = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user =>
      implicit request =>
        fullTimeEmployeeCountForm.bindFromRequest().fold(
          formWithErrors => {
            Future.successful(BadRequest(FullTimeEmployeeCount(formWithErrors)))
          },
          validFormData => {
            submissionService.validateFullTimeEmployeeCount(validFormData.employeeCount).map {
              case true => Redirect(routes.ConfirmCorrespondAddressController.show())
              case false => Redirect(routes.ConfirmCorrespondAddressController.show())
            }
          }
        )
    }
  }
}
