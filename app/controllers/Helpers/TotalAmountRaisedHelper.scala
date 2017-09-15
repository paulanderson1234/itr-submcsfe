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
import play.api.mvc.{AnyContent, Request, Result}
import uk.gov.hmrc.play.http.HeaderCarrier
import utils.Validation
import connectors.S4LConnector
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object TotalAmountRaisedHelper extends TotalAmountRaisedHelper {

}

trait TotalAmountRaisedHelper {

  def getRoute(prevRFI: HadPreviousRFIModel, commercialSale: Option[CommercialSaleModel],
               hasSub: Option[SubsidiariesModel], isKi: Boolean,  s4lConnector: S4LConnector)
              (implicit hc: HeaderCarrier, user: TAVCUser): Future[Result] = {

    commercialSale match {
      case Some(sale) if sale.hasCommercialSale == Constants.StandardRadioButtonNoValue => subsidiariesCheck(hasSub,s4lConnector)
      case Some(sale) if sale.hasCommercialSale == Constants.StandardRadioButtonYesValue => getPreviousSaleRoute(prevRFI, sale, hasSub, isKi, s4lConnector)
      case None => Future.successful(Redirect(routes.CommercialSaleController.show()))
    }
  }

  def getAgeLimit(isKI: Boolean): Int = {
    if (isKI) Constants.IsKnowledgeIntensiveYears
    else Constants.IsNotKnowledgeIntensiveYears
  }

  def subsidiariesCheck(hasSub: Option[SubsidiariesModel], s4lConnector: S4LConnector)(implicit hc: HeaderCarrier, user: TAVCUser): Future[Result] = {
    hasSub match {
      case Some(data) => if (data.ownSubsidiaries.equals(Constants.StandardRadioButtonYesValue)) {
        s4lConnector.saveFormData(KeystoreKeys.backLinkSubSpendingInvestment,
          routes.ProposedInvestmentController.show().url)
        Future.successful(Redirect(routes.SubsidiariesSpendingInvestmentController.show()))
      } else {
        s4lConnector.saveFormData(KeystoreKeys.backLinkInvestmentGrow,
          routes.ProposedInvestmentController.show().url)
        Future.successful(Redirect(routes.InvestmentGrowController.show()))
      }
      case None => {
        s4lConnector.saveFormData(KeystoreKeys.backLinkSubsidiaries,
          routes.ProposedInvestmentController.show().url)
        Future.successful(Redirect(routes.SubsidiariesController.show()))
      }
    }
  }

  def getPreviousSaleRoute(prevRFI: HadPreviousRFIModel, commercialSale: CommercialSaleModel,
                           hasSub: Option[SubsidiariesModel], isKi: Boolean, s4lConnector: S4LConnector)
                          (implicit hc: HeaderCarrier, user: TAVCUser): Future[Result] = {

    val dateWithinRangeRule: Boolean = Validation.checkAgeRule(commercialSale.commercialSaleDay.get,
      commercialSale.commercialSaleMonth.get, commercialSale.commercialSaleYear.get, getAgeLimit(isKi))

    prevRFI match {
      case rfi if rfi.hadPreviousRFI == Constants.StandardRadioButtonNoValue => {
        // this is first scheme
        if (dateWithinRangeRule) {
          s4lConnector.saveFormData(KeystoreKeys.backLinkNewGeoMarket,
            routes.ProposedInvestmentController.show().url)
          Future.successful(Redirect(routes.NewGeographicalMarketController.show()))
        }
        else subsidiariesCheck(hasSub,s4lConnector)
      }
      case rfi if rfi.hadPreviousRFI == Constants.StandardRadioButtonYesValue => {
        // subsequent scheme
        if (dateWithinRangeRule) Future.successful(Redirect(routes.UsedInvestmentReasonBeforeController.show()))
        else subsidiariesCheck(hasSub,s4lConnector)
      }

    }
  }
}
