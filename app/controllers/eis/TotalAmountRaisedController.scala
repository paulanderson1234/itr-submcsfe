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
import forms.ProposedInvestmentForm._
import forms.TotalAmountRaisedForm._
import models.{SubsidiariesModel, _}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import views.html.eis.shareDetails.TotalAmountRaised

import scala.concurrent.Future
import models.investorDetails.InvestorDetailsModel
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

//      def routeRequest(dateForActivity: Option[HasInvestmentTradeStartedModel]): Future[Result] = {
//        if (dateForActivity.isDefined) {
//          if (dateForActivity.get.hasInvestmentTradeStarted == Constants.StandardRadioButtonYesValue) {
//            val validationFlag = for {
//              startDateValid <- submissionConnector.validateHasInvestmentTradeStartedCondition(dateForActivity.get.hasInvestmentTradeStartedDay.get,
//                dateForActivity.get.hasInvestmentTradeStartedMonth.get, dateForActivity.get.hasInvestmentTradeStartedYear.get)
//              investorDetails <- s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails)
//            } yield (startDateValid, investorDetails)
//
//            validationFlag.map {
//              case (Some(startDateValid), investorDetails) if startDateValid => {
//                s4lConnector.saveFormData(KeystoreKeys.backLinkAddInvestorOrNominee, routes.TotalAmountRaisedController.show().url)
//                if (investorDetails.isDefined && investorDetails.get.nonEmpty)
//                  Redirect(controllers.eis.routes.ReviewAllInvestorsController.show())
//                else Redirect(controllers.eis.routes.AddInvestorOrNomineeController.show())
//              }
//              case (Some(startDateValid), _) if !startDateValid => Redirect(routes.UsedInvestmentReasonBeforeController.show())
//              case (None, _) =>
//                Logger.warn("[TotalAmountRaisedController][submit] - validateHasInvestmentTradeStartedCondition did not return expected true/false answer")
//                InternalServerError(internalServerErrorTemplate)
//            }
//          }
//          else
//          // trade not started so treat as less than 4 months trading
//            Future.successful(Redirect(routes.UsedInvestmentReasonBeforeController.show()))
//        }
//        else
//        // inconsistent date. User page skipping etc. Send to start of flow.
//          Future.successful(Redirect(routes.QualifyBusinessActivityController.show()))
//      }

      def routeReq(kiModel: KiProcessingModel, prevRFI: HadPreviousRFIModel,
                   comSale: Option[CommercialSaleModel], hasSub: Option[SubsidiariesModel]): Future[Result] = {
        TotalAmountRaisedHelper.getRoute(prevRFI, comSale, hasSub, kiModel.isKi, s4lConnector)
      }

      def routeRequest(kiModel: Option[KiProcessingModel], isLifeTimeAllowanceExceeded: Option[Boolean], prevRFI: HadPreviousRFIModel): Future[Result] = {
        kiModel match {
          // check previous answers present
          case Some(dataWithPreviousValid) => {
            // all good - TODO:Save the lifetime exceeded flag? - decide how to handle. For now I put it in keystore..
            if(isLifeTimeAllowanceExceeded.nonEmpty){
              s4lConnector.saveFormData(KeystoreKeys.lifeTimeAllowanceExceeded, isLifeTimeAllowanceExceeded.getOrElse(false))
            }

            isLifeTimeAllowanceExceeded match {
              case Some(data) =>
                // if it's exceeded go to the error page
                if (data) {
                  Future.successful(Redirect(routes.LifetimeAllowanceExceededController.show()))
                }
                else {
                  // not exceeded - continue
                  for {
                    comSale <- s4lConnector.fetchAndGetFormData[CommercialSaleModel](KeystoreKeys.commercialSale)
                    hasSub <- s4lConnector.fetchAndGetFormData[SubsidiariesModel](KeystoreKeys.subsidiaries)
                    route <- routeReq(kiModel.get, prevRFI, comSale, hasSub)
                  } yield route
                }

              // if none, redirect back to HadPreviousRFI page.
              // Will only hit this if there is no backend connected.
              case None => Future.successful(Redirect(routes.HadPreviousRFIController.show()))
            }
          }
          case None => Future.successful(Redirect(routes.DateOfIncorporationController.show()))
        }
      }
      totalAmountRaisedForm.bindFromRequest().fold(
        formWithErrors => {
          ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkProposedInvestment, s4lConnector).flatMap(url =>
            Future.successful(BadRequest(TotalAmountRaised(formWithErrors))))
        },
        validFormData => {
          s4lConnector.saveFormData(KeystoreKeys.proposedInvestment, validFormData)
          (for {
            kiModel <- s4lConnector.fetchAndGetFormData[KiProcessingModel](KeystoreKeys.kiProcessingModel)
            hadPrevRFI <- s4lConnector.fetchAndGetFormData[HadPreviousRFIModel](KeystoreKeys.hadPreviousRFI)
            previousInvestments <- PreviousSchemesHelper.getPreviousInvestmentTotalFromKeystore(s4lConnector)

            // Call API
            isLifeTimeAllowanceExceeded <- submissionConnector.checkLifetimeAllowanceExceeded(
              if (hadPrevRFI.get.hadPreviousRFI == Constants.StandardRadioButtonYesValue) true else false,
              if (kiModel.isDefined) kiModel.get.isKi else false, previousInvestments,
              validFormData.amount.toIntExact)

            route <- routeRequest(kiModel, isLifeTimeAllowanceExceeded, hadPrevRFI.get)
          } yield route) recover {
            case e: NoSuchElementException => Redirect(routes.HadPreviousRFIController.show())
            case e: Exception => {
              Logger.warn(s"[PercentageStaffWithMastersController][submit] - Exception validateSecondaryKiConditions: ${e.getMessage}")
              InternalServerError(internalServerErrorTemplate)
            }
          }
        }
      )

//      totalAmountRaisedForm.bindFromRequest().fold(
//        formWithErrors => {
//          Future.successful(BadRequest(TotalAmountRaised(formWithErrors)))
//        },
//        validFormData => {
//          s4lConnector.saveFormData(KeystoreKeys.totalAmountRaised, validFormData)
//          (for {
//            dateForActivity <- ControllerHelpers.getTradeStartDateForBusinessActivity(s4lConnector)
//            route <- routeRequest(dateForActivity)
//          } yield route) recover {
//            case e: Exception => {
//              Logger.warn(s"[TotalAmountRaisedController][submit]- Exception occurred: ${e.getMessage}")
//              InternalServerError(internalServerErrorTemplate)
//            }
//          }
//
//        }
//      )
  }
}