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

import auth.{AuthorisedAndEnrolledForTAVC, EIS, TAVCUser}
import common.{Constants, KeystoreKeys}
import config.{AppConfig, FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.Helpers.ControllerHelpers
import forms.FullTimeEmployeeCountForm._
import models.{FullTimeEmployeeCountModel, KiProcessingModel, SubsidiariesModel}
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import services.SubmissionService
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.eis.companyDetails.FullTimeEmployeeCount

import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier

object FullTimeEmployeeCountController extends FullTimeEmployeeCountController {
  override lazy val enrolmentConnector: EnrolmentConnector = EnrolmentConnector
  override lazy val applicationConfig: AppConfig = FrontendAppConfig
  override lazy val s4lConnector: S4LConnector = S4LConnector
  override lazy val authConnector: AuthConnector = FrontendAuthConnector
  val submissionService = SubmissionService
}

trait FullTimeEmployeeCountController extends FrontendController with AuthorisedAndEnrolledForTAVC {
  override val acceptedFlows = Seq(Seq(EIS))
  val submissionService: SubmissionService

  val show = AuthorisedAndEnrolled.async { implicit user =>
    implicit request =>
      def routeRequest(backUrl: Option[String]) = {
        if (backUrl.isDefined) {
          s4lConnector.fetchAndGetFormData[FullTimeEmployeeCountModel](KeystoreKeys.fullTimeEmployeeCount).map {
            case Some(data) => Ok(FullTimeEmployeeCount(fullTimeEmployeeCountForm.fill(data), backUrl.get))
            case None => Ok(FullTimeEmployeeCount(fullTimeEmployeeCountForm, backUrl.get))
          }
        }
        else
          Future.successful(Redirect(controllers.eis.routes.GrossAssetsController.show()))
      }
      for {
        link <- ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkFullTimeEmployeeCount, s4lConnector)
        route <- routeRequest(link)
      } yield route
  }

  val submit = AuthorisedAndEnrolled.async { implicit user =>
    implicit request =>
      fullTimeEmployeeCountForm.bindFromRequest().fold(
        formWithErrors => {
          ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkFullTimeEmployeeCount, s4lConnector).flatMap(url =>
            Future.successful(BadRequest(FullTimeEmployeeCount(formWithErrors, url.get))))
        },
        validFormData => {
          s4lConnector.saveFormData[FullTimeEmployeeCountModel](KeystoreKeys.fullTimeEmployeeCount, validFormData)
          s4lConnector.saveFormData(KeystoreKeys.backLinkHadRFI, routes.HadPreviousRFIController.show().url)
          val fteStatus = getSchemeType(s4lConnector).flatMap {
            case Constants.schemeTypeEisKi =>
              submissionService.validateFullTimeEmployeeCount(Constants.schemeTypeEisKi, validFormData.employeeCount)
            case Constants.schemeTypeEis =>
              submissionService.validateFullTimeEmployeeCount(Constants.schemeTypeEis, validFormData.employeeCount)
          }

          fteStatus.map {
            case true => s4lConnector.saveFormData(KeystoreKeys.backLinkSubsidiaries, routes.FullTimeEmployeeCountController.show().url)
              Redirect(routes.SubsidiariesController.show())
            case false => s4lConnector.saveFormData(KeystoreKeys.backLinkSubsidiaries, routes.FullTimeEmployeeCountController.show().url)
              // default subsidiaries to yes - remove hen subsidiaries properly implemented
              s4lConnector.saveFormData[SubsidiariesModel](KeystoreKeys.subsidiaries, SubsidiariesModel(Constants.StandardRadioButtonNoValue))
              Redirect(routes.FullTimeEmployeeCountErrorController.show())
          }
        }
      )
  }

  def getSchemeType(s4lConnector: connectors.S4LConnector) (implicit hc: HeaderCarrier, user: TAVCUser): Future[String] = {
    s4lConnector.fetchAndGetFormData[KiProcessingModel](KeystoreKeys.kiProcessingModel).map {
      case Some(data) if data.isKi => Constants.schemeTypeEisKi
      case _ => Constants.schemeTypeEis
    }
  }
}