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

package views.eis

import common.Constants
import models.investorDetails.PreviousShareHoldingModel
import models._
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import views.helpers.CheckAnswersSpec
import views.html.eis.checkAndSubmit.CheckAnswers

class CheckAnswersSharesSpec extends CheckAnswersSpec {

  "The Check Answers page" should {

    "Verify that the Check Answers page contains the correct elements for Section 3: Investment" +
      " when it is fully populated with investment models" in {
      val model = CheckAnswersModel(Some(registeredAddressModel), Some(dateOfIncorporationModel), Some(natureOfBusinessModel),
        Some(commercialSaleModelYes), Some(isCompanyKnowledgeIntensiveModelYes), Some(isKnowledgeIntensiveModelYes),
        Some(operatingCostsModel), Some(percentageStaffWithMastersModelNo), Some(tenYearPlanModelYes), Some(hadPreviousRFIModelYes),
        Vector(), Some(totalAmountRaisedValid), Some(thirtyDayRuleModelYes), Some(anySharesRepaymentModelYes), Some(newGeographicalMarketModelYes),
        Some(newProductMarketModelYes), Some(contactDetailsModel), Some(addressModel), Some(investmentGrowModel), Some(qualifyPrepareToTrade),
        Some(hasInvestmentTradeStartedModelYes), Some(shareIssuetDateModel), Some(grossAssetsModel), Some(fullTimeEmployeeModel),
        Some(shareDescriptionModel), Some(numberOfSharesModel), Some(listOfInvestorsWithShareHoldings), Some(WasAnyValueReceivedModel(Constants.StandardRadioButtonYesValue,
          Some("text"))), Some(ShareCapitalChangesModel(Constants.StandardRadioButtonYesValue, Some("test"))), Some(MarketDescriptionModel("test")),
        Some(validSharesRepaymentDetailsVector), Some(grossAssetsAfterIssueModel),
        Some(turnoverCostsValid), Some(researchStartDateModelYes), false)

      val page = CheckAnswers(model)(authorisedFakeRequest, applicationMessages)
      val document = Jsoup.parse(page.body)

      lazy val sharesTableTbody = document.getElementById("shares-table").select("tbody")

      //Section table heading
      document.getElementById("sharesSection-table-heading").text() shouldBe Messages("page.summaryQuestion.shares")

      //shareDescription
      sharesTableTbody.select("tr").get(0).getElementById("shareDescription-question").text() shouldBe
        Messages("page.summaryQuestion.shareClass")
      sharesTableTbody.select("tr").get(0).getElementById("shareDescription-answer").text() shouldBe
        shareDescriptionModel.shareDescription
      sharesTableTbody.select("tr").get(0).getElementById("shareDescription-link")
        .attr("href") shouldEqual controllers.eis.routes.ShareDescriptionController.show().url

      //numberOfShares
      sharesTableTbody.select("tr").get(1).getElementById("numberOfShares-question").text() shouldBe
        Messages("page.summaryQuestion.sharesIssues")
      sharesTableTbody.select("tr").get(1).getElementById("numberOfShares-answer").text() shouldBe
        numberOfSharesModel.numberOfShares.toString()
      sharesTableTbody.select("tr").get(1).getElementById("numberOfShares-link")
        .attr("href") shouldEqual controllers.eis.routes.NumberOfSharesController.show().url

      //totalAmountRaised
      sharesTableTbody.select("tr").get(2).getElementById("totalAmountRaised-question").text() shouldBe
        Messages("page.summaryQuestion.amountRaised")
      sharesTableTbody.select("tr").get(2).getElementById("totalAmountRaised-answer").text() shouldBe
        PreviousShareHoldingModel.getAmountAsFormattedString(totalAmountRaisedValid.amount)
      sharesTableTbody.select("tr").get(2).getElementById("totalAmountRaised-link")
        .attr("href") shouldEqual controllers.eis.routes.TotalAmountRaisedController.show().url

      //newGeoMarket
      sharesTableTbody.select("tr").get(3).getElementById("newGeoMarket-question").text() shouldBe
        Messages("page.summaryQuestion.newGeoMarket")
      sharesTableTbody.select("tr").get(3).getElementById("newGeoMarket-answer").text() shouldBe
        newGeographicalMarketModelYes.isNewGeographicalMarket
      sharesTableTbody.select("tr").get(3).getElementById("newGeoMarket-link")
        .attr("href") shouldEqual controllers.eis.routes.NewGeographicalMarketController.show().url

      //newProduct
      sharesTableTbody.select("tr").get(4).getElementById("newProduct-question").text() shouldBe
        Messages("page.summaryQuestion.newProduct")
      sharesTableTbody.select("tr").get(4).getElementById("newProduct-answer").text() shouldBe
        newProductMarketModelYes.isNewProduct
      sharesTableTbody.select("tr").get(4).getElementById("newProduct-link")
        .attr("href") shouldEqual controllers.eis.routes.NewProductController.show().url

      //turnoverCosts
      sharesTableTbody.select("tr").get(5).getElementById("turnoverCosts-question").text() shouldBe
        Messages("page.summaryQuestion.turnoverCosts")
      sharesTableTbody.select("tr").get(5).getElementById("turnoverCosts-answer").text() shouldBe
        AnnualTurnoverCostsModel.averagedAnnualTurnover(
          turnoverCostsValid.amount1,turnoverCostsValid.amount2,turnoverCostsValid.amount3,
          turnoverCostsValid.amount4,turnoverCostsValid.amount5)
      sharesTableTbody.select("tr").get(5).getElementById("turnoverCosts-link")
        .attr("href") shouldEqual controllers.eis.routes.TurnoverCostsController.show().url

      //thirtyDayRuleModel
      sharesTableTbody.select("tr").get(6).getElementById("thirtyDayRuleModel-question").text() shouldBe
        Messages("page.summaryQuestion.thirtyDayRuleModel")
      sharesTableTbody.select("tr").get(6).getElementById("thirtyDayRuleModel-answer").text() shouldBe
        thirtyDayRuleModelYes.thirtyDayRule
      sharesTableTbody.select("tr").get(6).getElementById("thirtyDayRuleModel-link")
        .attr("href") shouldEqual controllers.eis.routes.ThirtyDayRuleController.show().url

      // marketDescription
      sharesTableTbody.select("tr").get(7).getElementById("marketDescription-question").text() shouldBe
        Messages("page.summaryQuestion.marketDescription")
      sharesTableTbody.select("tr").get(7).getElementById("marketDescription-answer").text() shouldBe
        MarketDescriptionModel("test").text
      sharesTableTbody.select("tr").get(7).getElementById("marketDescription-link")
        .attr("href") shouldEqual controllers.eis.routes.MarketDescriptionController.show().url
      
      // investment Grow
      sharesTableTbody.select("tr").get(8).getElementById("investmentGrow-question").text() shouldBe
        Messages("page.summaryQuestion.investmentGrow")
      sharesTableTbody.select("tr").get(8).getElementById("investmentGrow-answer").text() shouldBe
        investmentGrowModel.investmentGrowDesc
      sharesTableTbody.select("tr").get(8).getElementById("investmentGrow-link")
        .attr("href") shouldEqual controllers.eis.routes.InvestmentGrowController.show().url

    }

    "Verify that the Check Answers page contains an empty table for Section 3: Investment" +
      " when the investment models are empty" in {
      val model = CheckAnswersModel(Some(registeredAddressModel), Some(dateOfIncorporationModel), Some(natureOfBusinessModel),
        Some(commercialSaleModelYes), Some(isCompanyKnowledgeIntensiveModelYes), Some(isKnowledgeIntensiveModelYes),
        Some(operatingCostsModel), Some(percentageStaffWithMastersModelNo), Some(tenYearPlanModelYes), Some(hadPreviousRFIModelYes),
        Vector(), None, None, Some(anySharesRepaymentModelYes), None,
        None, Some(contactDetailsModel), Some(addressModel), None, Some(qualifyPrepareToTrade),
        Some(hasInvestmentTradeStartedModelYes), Some(shareIssuetDateModel), Some(grossAssetsModel), Some(fullTimeEmployeeModel),
        None, None, Some(listOfInvestorsWithShareHoldings), Some(WasAnyValueReceivedModel(Constants.StandardRadioButtonYesValue,
          Some("text"))), Some(ShareCapitalChangesModel(Constants.StandardRadioButtonYesValue, Some("test"))), None,
        Some(validSharesRepaymentDetailsVector), Some(grossAssetsAfterIssueModel),
        None, Some(researchStartDateModelYes), false)

      val page = CheckAnswers(model)(authorisedFakeRequest, applicationMessages)
      val document = Jsoup.parse(page.body)

      lazy val sharesTableTbody = document.getElementById("shares-table").select("tbody")
      lazy val notAvailableMessage = Messages("common.notAvailable")

      //Section table heading
      document.getElementById("sharesSection-table-heading").text() shouldBe Messages("page.summaryQuestion.shares")
      sharesTableTbody.select("tr").size() shouldBe 0
    }
  }
}

