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

import auth.{TAVCUser, AuthorisedAndEnrolledForTAVC, EIS, VCT}
import common.{Constants, KeystoreKeys}
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.Helpers.{ControllerHelpers, PreviousSchemesHelper}
import forms.HadPreviousRFIForm._
import models.{HadOtherInvestmentsModel, HadPreviousRFIModel}
import play.api.mvc.Result
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future
import views.html.eis.previousInvestment
import uk.gov.hmrc.http.HeaderCarrier

object HadPreviousRFIController extends HadPreviousRFIController{
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait HadPreviousRFIController extends FrontendController with AuthorisedAndEnrolledForTAVC with PreviousSchemesHelper {

  override val acceptedFlows = Seq(Seq(EIS))

  val show = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    s4lConnector.fetchAndGetFormData[HadPreviousRFIModel](KeystoreKeys.hadPreviousRFI).map {
      case Some(data) => Ok(previousInvestment.HadPreviousRFI(hadPreviousRFIForm.fill(data)))
      case None => Ok(previousInvestment.HadPreviousRFI(hadPreviousRFIForm))
    }
  }


  val submit = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    hadPreviousRFIForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(previousInvestment.HadPreviousRFI(formWithErrors)))
      },
      validFormData => {
        s4lConnector.saveFormData(KeystoreKeys.hadPreviousRFI, validFormData)
        s4lConnector.saveFormData(KeystoreKeys.backLinkHadRFI, routes.HadPreviousRFIController.show().url)
       for {
         hadOtherInvestments <- s4lConnector.fetchAndGetFormData[HadOtherInvestmentsModel](KeystoreKeys.hadOtherInvestments)
         result <- routeRequest(hadOtherInvestments, validFormData)
       } yield result
      }
    )
  }

  def routeRequest(hadOtherInvestmentsModel : Option[HadOtherInvestmentsModel], validFormData: HadPreviousRFIModel)
                  (implicit headerCarrier: HeaderCarrier, tavcUser:TAVCUser): Future[Result]= {
    validFormData.hadPreviousRFI match {
      case Constants.StandardRadioButtonYesValue => Future.successful(Redirect(routes.HadOtherInvestmentsController.show()))
      case Constants.StandardRadioButtonNoValue => {
        if (hadOtherInvestmentsModel.isDefined && hadOtherInvestmentsModel.get.hadOtherInvestments.equals(Constants.StandardRadioButtonNoValue)) {
          clearPreviousInvestments(s4lConnector)
        }
        Future.successful(Redirect(routes.HadOtherInvestmentsController.show()))
      }
    }
  }

}
