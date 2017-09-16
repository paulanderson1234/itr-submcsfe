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
import common.{Constants, KeystoreKeys}
import config.FrontendGlobal._
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector, SubmissionConnector}
import controllers.Helpers.{ControllerHelpers, PreviousSchemesHelper}
import forms.TotalAmountRaisedForm._
import models.{HadPreviousRFIModel, KiProcessingModel, ShareIssueDateModel, TotalAmountRaisedModel}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import views.html.eis.shareDetails.TotalAmountRaised

import scala.concurrent.Future
import play.Logger
import play.api.mvc.Result
import controllers.Helpers.TotalAmountRaisedHelper

object TotalAmountRaisedController extends TotalAmountRaisedController{
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
  override lazy val submissionConnector = SubmissionConnector
}

trait TotalAmountRaisedController extends FrontendController with AuthorisedAndEnrolledForTAVC {

  override val acceptedFlows = Seq(Seq(EIS))

  val submissionConnector: SubmissionConnector

  val show = AuthorisedAndEnrolled.async { implicit user =>
    implicit request =>
      s4lConnector.fetchAndGetFormData[TotalAmountRaisedModel](KeystoreKeys.totalAmountRaised).map {
        case Some(data) => Ok(TotalAmountRaised(totalAmountRaisedForm.fill(data)))
        case None => Ok(TotalAmountRaised(totalAmountRaisedForm))
      }
  }

  val submit = AuthorisedAndEnrolled.async { implicit user =>
    implicit request =>

      def validateLifetimeAllowanceFirstCheck(kiModel: Option[KiProcessingModel], isLifetimeAlowanceExceeded: Option[Boolean],
                       prevRFI: HadPreviousRFIModel, totalAmountRaised: Int): Future[Result] = {
        kiModel match {
          // check previous answers present
          case Some(dataWithPreviousValid) => {
            // all good - TODO:Save the lifetime exceeded flag? - decide how to handle. For now I put it in keystore..
            if(isLifetimeAlowanceExceeded.nonEmpty){
              s4lConnector.saveFormData(KeystoreKeys.lifeTimeAllowanceExceeded, isLifetimeAlowanceExceeded.getOrElse(false))
            }
            isLifetimeAlowanceExceeded match {
              case Some(data) =>
                // if it's exceeded go to the error page
                if (data) {
                  Future.successful(Redirect(routes.LifetimeAllowanceExceededController.show()))
                } else {
                  // first API condition passed. Need to check second condition now..
                  validateAnnualLimitRouteRequestSecondCheck(totalAmountRaised)
                }
              // if none, redirect back to HadPreviousRFI page. Will only hit this if there is no backend connected.
              case None =>
                Logger.warn("TotalAmountRaisedController][submit] - unexpected None response returned from submissionConnector.checkLifetimeAllowanceExceeded")
                Future.successful(InternalServerError(internalServerErrorTemplate))
            }
          }
          case None => Future.successful(Redirect(routes.DateOfIncorporationController.show()))
        }
      }

      def validateAnnualLimitRouteRequestSecondCheck(totalAmountRaised:Int) : Future[Result] = {
        def routeForResult(shareIssueDate: Option[ShareIssueDateModel], isAnnualLimitExceeded: Option[Boolean]): Future[Result] = {
          if(shareIssueDate.isEmpty) {
            Future.successful(Redirect(routes.ShareIssueDateController.show()))
          } else
            // evaluate
            isAnnualLimitExceeded match {
              case Some(check) if !check => TotalAmountRaisedHelper.getContinueRouteRequest(s4lConnector)
              case Some(check) if check => Future.successful(Redirect(routes.AnnualLimitExceededErrorController.show()))
              case None =>
                Logger.warn("TotalAmountRaisedController][submit] - unexpected None response returned from submissionConnector.checkAnnualLimit")
                Future.successful(InternalServerError(internalServerErrorTemplate))
            }
        }
        for {
          shareIssueDate <- s4lConnector.fetchAndGetFormData[ShareIssueDateModel](KeystoreKeys.shareIssueDate)
          previousInvestmentTotalInRange <- PreviousSchemesHelper.getPreviousInvestmentsInShareIssueDateRangeTotal(s4lConnector)
          // Call API check 2
          isAnnualLimitExceeded <- submissionConnector.checkAnnualLimitExceeded(previousInvestmentTotalInRange, totalAmountRaised)
          route <- routeForResult(shareIssueDate, isAnnualLimitExceeded)
        } yield route
      }

      // Form submit validation  and routing
      totalAmountRaisedForm.bindFromRequest().fold(
        formWithErrors => {
          ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkProposedInvestment, s4lConnector).flatMap(url =>
            Future.successful(BadRequest(TotalAmountRaised(formWithErrors))))
        },
        validFormData => {
          s4lConnector.saveFormData(KeystoreKeys.totalAmountRaised, validFormData)
          (for {
            kiModel <- s4lConnector.fetchAndGetFormData[KiProcessingModel](KeystoreKeys.kiProcessingModel)
            hadPrevRFI <- s4lConnector.fetchAndGetFormData[HadPreviousRFIModel](KeystoreKeys.hadPreviousRFI)
            previousInvestments <- PreviousSchemesHelper.getPreviousInvestmentTotalFromKeystore(s4lConnector)

            // Call API check 1 (takes priority over check 2)
            isLifetimeAlowanceExceeded <- submissionConnector.checkLifetimeAllowanceExceeded(
              if (hadPrevRFI.fold(Constants.StandardRadioButtonNoValue)(_.hadPreviousRFI) == Constants.StandardRadioButtonYesValue) true else false,
              if (kiModel.isDefined) kiModel.get.isKi else false, previousInvestments,
              validFormData.amount.toIntExact)

            route <- validateLifetimeAllowanceFirstCheck(kiModel, isLifetimeAlowanceExceeded, hadPrevRFI.get, validFormData.amount.toIntExact)
          } yield route) recover {
            case e: NoSuchElementException => Redirect(routes.HadPreviousRFIController.show())
            case e: Exception => {
              Logger.warn(s"[TotalAmountRaisedController][submit] - Submit Exception: ${e.getMessage}")
              InternalServerError(internalServerErrorTemplate)
            }
          }
        }
      )
  }
}