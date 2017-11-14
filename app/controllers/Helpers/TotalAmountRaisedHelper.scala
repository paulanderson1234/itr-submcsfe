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

package controllers.Helpers

import auth.TAVCUser
import common.{Constants, KeystoreKeys}
import controllers.eis.routes
import models._
import play.api.mvc.Results._
import play.api.mvc.Result
import utils.Validation
import connectors.S4LConnector
import models.submission.MarketRoutingCheckResult

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier

object TotalAmountRaisedHelper extends TotalAmountRaisedHelper {

}

trait TotalAmountRaisedHelper {

  //Note if this logic chnages then change the checkIfMarketInfoApplies to match. They mirror the same logic but one as
  // validation and one as routing
  def getContinueRouteRequest(s4lConnector: S4LConnector) (implicit hc: HeaderCarrier, user: TAVCUser): Future[Result] = {
    for {
      kiModel <- s4lConnector.fetchAndGetFormData[KiProcessingModel](KeystoreKeys.kiProcessingModel)
      prevRFI <- s4lConnector.fetchAndGetFormData[HadPreviousRFIModel](KeystoreKeys.hadPreviousRFI)
      hadOtherInvestments <- s4lConnector.fetchAndGetFormData[HadOtherInvestmentsModel](KeystoreKeys.hadOtherInvestments)
      comSale <- s4lConnector.fetchAndGetFormData[CommercialSaleModel](KeystoreKeys.commercialSale)
      hasSub <- s4lConnector.fetchAndGetFormData[SubsidiariesModel](KeystoreKeys.subsidiaries)
      route <- getRoute(prevRFI, hadOtherInvestments, comSale, hasSub, kiModel, s4lConnector)
    } yield route
  }

  private def getRoute(prevRFI: Option[HadPreviousRFIModel], hadOtherInvestments: Option[HadOtherInvestmentsModel],
                       commercialSale: Option[CommercialSaleModel], hasSub: Option[SubsidiariesModel], kiProcessingModel: Option[KiProcessingModel],  s4lConnector: S4LConnector)
              (implicit hc: HeaderCarrier, user: TAVCUser): Future[Result] = {

    if(kiProcessingModel.isEmpty) Future.successful(Redirect(routes.IsCompanyKnowledgeIntensiveController.show()))
    else if (prevRFI.isEmpty) Future.successful(Redirect(routes.HadPreviousRFIController.show()))
    else if (hadOtherInvestments.isEmpty) Future.successful(Redirect(routes.HadOtherInvestmentsController.show()))
    else  commercialSale match {
      case Some(sale) if sale.hasCommercialSale == Constants.StandardRadioButtonNoValue => subsidiariesCheck(hasSub,s4lConnector)
      case Some(sale) if sale.hasCommercialSale == Constants.StandardRadioButtonYesValue =>
        getPreviousSaleRoute(prevRFI.get, hadOtherInvestments.get, sale, hasSub, kiProcessingModel.get.isKi,s4lConnector)
      case None => Future.successful(Redirect(routes.CommercialSaleController.show()))
    }
  }

  private def getAgeLimit(isKI: Boolean): Int = {
    if (isKI) Constants.IsKnowledgeIntensiveYears
    else Constants.IsNotKnowledgeIntensiveYears
  }

  private def subsidiariesCheck(hasSub: Option[SubsidiariesModel], s4lConnector: S4LConnector)(implicit hc: HeaderCarrier, user: TAVCUser): Future[Result] = {
    hasSub match {
      case Some(data) => if (data.ownSubsidiaries.equals(Constants.StandardRadioButtonYesValue)) {
        s4lConnector.saveFormData(KeystoreKeys.backLinkSubSpendingInvestment,
          routes.TotalAmountRaisedController.show().url)
        Future.successful(Redirect(routes.SubsidiariesSpendingInvestmentController.show()))
      } else {
        s4lConnector.saveFormData(KeystoreKeys.backLinkInvestmentGrow,
          routes.TotalAmountRaisedController.show().url)
        Future.successful(Redirect(routes.InvestmentGrowController.show()))
      }
      case None =>
        s4lConnector.saveFormData(KeystoreKeys.backLinkSubsidiaries,
          routes.TotalAmountRaisedController.show().url)
        Future.successful(Redirect(routes.SubsidiariesController.show()))
    }
  }

  private def getPreviousSaleRoute(prevRFI: HadPreviousRFIModel, hadOtherInvestments: HadOtherInvestmentsModel, commercialSale: CommercialSaleModel,
                           hasSub: Option[SubsidiariesModel], isKi: Boolean, s4lConnector: S4LConnector)
                          (implicit hc: HeaderCarrier, user: TAVCUser): Future[Result] = {

    val dateWithinRangeRule: Boolean = Validation.checkAgeRule(commercialSale.commercialSaleDay.get,
      commercialSale.commercialSaleMonth.get, commercialSale.commercialSaleYear.get, getAgeLimit(isKi))

    if (prevRFI.hadPreviousRFI == Constants.StandardRadioButtonNoValue &&
      hadOtherInvestments.hadOtherInvestments == Constants.StandardRadioButtonNoValue){
        // this is first scheme
        if (dateWithinRangeRule) {
          s4lConnector.saveFormData(KeystoreKeys.backLinkNewGeoMarket,
            routes.TotalAmountRaisedController.show().url)
          Future.successful(Redirect(routes.NewGeographicalMarketController.show()))
        }
        else subsidiariesCheck(hasSub,s4lConnector)
      }
    else if (dateWithinRangeRule) Future.successful(Redirect(routes.UsedInvestmentReasonBeforeController.show()))
    else subsidiariesCheck(hasSub,s4lConnector)
  }

  /** Helper method to check routing that determines if market data (new geo mkt, new prod mkt etc) should be sent as Json or not regardless whether populated
    * ineligibility flag relating to the previous scheme types condition.
    *
    * @param s4lConnector                          An instance of the Save4Later Connector.
    *
    * returns Future[MarketRoutingCheckResult] where first boolean indicates market info should be populated and
    * second boolean indicates a usedReasonBefore route and there is a need to validate previousBeforeDofcs and usedReasonBefore
    * are non empty and questions have been answered correctly for this route before, otherwise it's an incomplete journey and no Json should be submitted.
    * NOTE. THIS MIRRIRS THE LOGIC OF getContinueRouteRequest AOVE SO BOTH SHOULD MIRROR EACH OTHER IF CHNANGED
    */
  def checkIfMarketInfoApplies(s4lConnector: S4LConnector) (implicit hc: HeaderCarrier, user: TAVCUser): Future[MarketRoutingCheckResult] = {
    for {
      kiModel <- s4lConnector.fetchAndGetFormData[KiProcessingModel](KeystoreKeys.kiProcessingModel)
      prevRFI <- s4lConnector.fetchAndGetFormData[HadPreviousRFIModel](KeystoreKeys.hadPreviousRFI)
      hadOtherInvestments <- s4lConnector.fetchAndGetFormData[HadOtherInvestmentsModel](KeystoreKeys.hadOtherInvestments)
      comSale <- s4lConnector.fetchAndGetFormData[CommercialSaleModel](KeystoreKeys.commercialSale)
      hasSub <- s4lConnector.fetchAndGetFormData[SubsidiariesModel](KeystoreKeys.subsidiaries)
      usedReasonBefore <- s4lConnector.fetchAndGetFormData[UsedInvestmentReasonBeforeModel](KeystoreKeys.usedInvestmentReasonBefore)
      previousBeforeDofcs <- s4lConnector.fetchAndGetFormData[PreviousBeforeDOFCSModel](KeystoreKeys.previousBeforeDOFCS)
      route <- checkMarketInfoRouting(prevRFI, hadOtherInvestments, comSale, hasSub, kiModel,
        usedReasonBefore,previousBeforeDofcs, s4lConnector)
    } yield route
  }

  private def checkMarketInfoRouting(prevRFI: Option[HadPreviousRFIModel], hadOtherInvestments: Option[HadOtherInvestmentsModel],
                       commercialSale: Option[CommercialSaleModel], hasSub: Option[SubsidiariesModel], kiProcessingModel: Option[KiProcessingModel],
                                     usedReasonBefore:Option[UsedInvestmentReasonBeforeModel],
                                     previousBeforeDofcs:Option[PreviousBeforeDOFCSModel],s4lConnector: S4LConnector)
                      (implicit hc: HeaderCarrier, user: TAVCUser): Future[MarketRoutingCheckResult] = {

    if(kiProcessingModel.isEmpty) Future.successful(MarketRoutingCheckResult(isMarketInfoRoute = false))
    else if (prevRFI.isEmpty) Future.successful(MarketRoutingCheckResult(isMarketInfoRoute = false))
    else if (hadOtherInvestments.isEmpty) Future.successful(MarketRoutingCheckResult(isMarketInfoRoute = false))
    else  commercialSale match {
      case Some(sale) if sale.hasCommercialSale == Constants.StandardRadioButtonNoValue => subsidiariesMarketInfoCheck(hasSub)
      case Some(sale) if sale.hasCommercialSale == Constants.StandardRadioButtonYesValue =>
        getMarketInfoPreviousSaleRouting(prevRFI.get, hadOtherInvestments.get, sale, hasSub,
          kiProcessingModel.get.isKi, usedReasonBefore, previousBeforeDofcs)
      case None => Future.successful(MarketRoutingCheckResult(isMarketInfoRoute = false))
    }
  }

  private def subsidiariesMarketInfoCheck(hasSub: Option[SubsidiariesModel])(implicit hc: HeaderCarrier, user: TAVCUser): Future[MarketRoutingCheckResult] = {
    hasSub match {
      case Some(data) => if (data.ownSubsidiaries.equals(Constants.StandardRadioButtonYesValue)) {
        // this currently always set to 'No' until groups and subs implemented. needs updating then depending if market route affected
        Future.successful(MarketRoutingCheckResult(isMarketInfoRoute = false))
      } else {
        // this route would skip market info and go to investment grow. no market info required. extra validation not required.
        // this result currently happens if first date of commercial sale = no, or date range not within 7/10 years (getMarketInfoPreviousSaleRouting)
        Future.successful(MarketRoutingCheckResult(isMarketInfoRoute = false))
      }
      case None =>
        // incomplete data but always set currently so won't be hit until groups and subs implemented
        // when groups and subs implemented may need updating then depending if market route affected
        Future.successful(MarketRoutingCheckResult(isMarketInfoRoute = false))
    }
  }

  private def getMarketInfoPreviousSaleRouting(prevRFI: HadPreviousRFIModel, hadOtherInvestments: HadOtherInvestmentsModel, commercialSale: CommercialSaleModel,
                                   hasSub: Option[SubsidiariesModel], isKi: Boolean,
                                               usedReasonBefore:Option[UsedInvestmentReasonBeforeModel],
                                               previousBeforeDofcs:Option[PreviousBeforeDOFCSModel])
                                  (implicit hc: HeaderCarrier, user: TAVCUser): Future[MarketRoutingCheckResult] = {

    val dateWithinRangeRule: Boolean = Validation.checkAgeRule(commercialSale.commercialSaleDay.get,
      commercialSale.commercialSaleMonth.get, commercialSale.commercialSaleYear.get, getAgeLimit(isKi))

    if (prevRFI.hadPreviousRFI == Constants.StandardRadioButtonNoValue &&
      hadOtherInvestments.hadOtherInvestments == Constants.StandardRadioButtonNoValue){
      // this is first scheme
      if (dateWithinRangeRule) {
        // goes to new geo make so market info should be populated and sent. no extra validation required.
        Future.successful(MarketRoutingCheckResult(isMarketInfoRoute = true))
      }
      else subsidiariesMarketInfoCheck(hasSub)
    }
    else if (dateWithinRangeRule) {
      //This is the UsedInvestmentReasonBeforeController route
      // Market info would not be populated after this route if.
      // if both answers = Yes we skip Market information and does not need populating
      if(usedReasonBefore.fold(false)(_.usedInvestmentReasonBefore == Constants.StandardRadioButtonYesValue) &&
        previousBeforeDofcs.fold(false)(_.previousBeforeDOFCS == Constants.StandardRadioButtonYesValue)) Future.successful(MarketRoutingCheckResult(isMarketInfoRoute = false))
      else
        // we have market info to populate and should send the JSON for market info.
        // However we need the caller to validate previousBeforeDofcs and usedReasonBefore are non empty to ensure these questions are complete
        // before submitting JSON as this journey is incomplete otherwise and no JSON at all should be submitted.
        // Set second boolean = true to denotes this
        Future.successful(MarketRoutingCheckResult(isMarketInfoRoute = true, reasonBeforeValidationRequired = true))
    }
    else subsidiariesMarketInfoCheck(hasSub)
  }
}
