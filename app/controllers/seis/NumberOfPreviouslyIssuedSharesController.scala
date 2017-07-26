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
import common.{KeystoreKeys}
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.Helpers.{ControllerHelpers}
import controllers.predicates.FeatureSwitch
import forms.NumberOfPreviouslyIssuedSharesForm._
import models.CompanyOrIndividualModel
import models.investorDetails.NumberOfPreviouslyIssuedSharesModel
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.mvc.{Action, AnyContent}
import views.html.seis.investors.NumberOfPreviouslyIssuedShares

import scala.concurrent.Future

object NumberOfPreviouslyIssuedSharesController extends NumberOfPreviouslyIssuedSharesController
{
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait NumberOfPreviouslyIssuedSharesController extends FrontendController with AuthorisedAndEnrolledForTAVC with FeatureSwitch with ControllerHelpers {

  override val acceptedFlows = Seq(Seq(SEIS))

  def show(id: Int): Action[AnyContent] = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>

      def routeRequest(companyOrIndividual: Option[CompanyOrIndividualModel]) = {
        if (companyOrIndividual.isDefined) {
          s4lConnector.fetchAndGetFormData[NumberOfPreviouslyIssuedSharesModel](KeystoreKeys.numberOfPreviouslyIssuedShares).map {
            case Some(data) => Ok(NumberOfPreviouslyIssuedShares(companyOrIndividual.get.companyOrIndividual,
              numberOfPreviouslyIssuedSharesForm.fill(data)))
            case None => Ok(NumberOfPreviouslyIssuedShares(companyOrIndividual.get.companyOrIndividual, numberOfPreviouslyIssuedSharesForm))
          }
        } else {
          Future.successful(Redirect(routes.CompanyOrIndividualController.show(id)))
        }
      }
      for {
        companyOrIndividual <- s4lConnector.fetchAndGetFormData[CompanyOrIndividualModel](KeystoreKeys.companyOrIndividual)
        route <- routeRequest(companyOrIndividual)
      } yield route
    }
  }

  val submit = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user =>
      implicit request =>
        numberOfPreviouslyIssuedSharesForm.bindFromRequest().fold(
          formWithErrors => {
            s4lConnector.fetchAndGetFormData[CompanyOrIndividualModel](KeystoreKeys.companyOrIndividual).map{
              companyOrIndividual => BadRequest(NumberOfPreviouslyIssuedShares(companyOrIndividual.get.companyOrIndividual, formWithErrors))
            }
          },
          validFormData => {
            s4lConnector.saveFormData(KeystoreKeys.numberOfPreviouslyIssuedShares, validFormData)
            Future.successful(Redirect(routes.DateOfIncorporationController.show()))
          }
        )
    }
  }
}
