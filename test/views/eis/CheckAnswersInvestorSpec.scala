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
import models._
import models.repayments.SharesRepaymentDetailsModel.{getAmountAsFormattedString, toDateString}
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import views.helpers.CheckAnswersSpec
import views.html.eis.checkAndSubmit.CheckAnswers

class CheckAnswersInvestorSpec extends CheckAnswersSpec {

  val wasAnyValueReceivedModel = WasAnyValueReceivedModel(Constants.StandardRadioButtonYesValue,
    Some("text"))
  val shareCapitalChangesModel = ShareCapitalChangesModel(Constants.StandardRadioButtonYesValue, Some("test"))
  "The Check Answers page" should {

    "Verify that the Check Answers page contains the correct elements for Section 3: Investment" +
      " when it is fully populated with investment models" in {
      val model = CheckAnswersModel(Some(registeredAddressModel), Some(dateOfIncorporationModel), Some(natureOfBusinessModel),
        Some(commercialSaleModelYes), Some(isCompanyKnowledgeIntensiveModelYes), Some(isKnowledgeIntensiveModelYes),
        Some(operatingCostsModel), Some(percentageStaffWithMastersModelNo), Some(tenYearPlanModelYes), Some(hadPreviousRFIModelYes),
        previousSchemesValid, Some(totalAmountRaisedValid), Some(thirtyDayRuleModelYes), Some(anySharesRepaymentModelYes), Some(newGeographicalMarketModelYes),
        Some(newProductMarketModelYes), Some(contactDetailsModel), Some(addressModel), Some(investmentGrowModel), Some(qualifyTrade),
        Some(hasInvestmentTradeStartedModelYes), Some(shareIssuetDateModel), Some(grossAssetsModel), Some(fullTimeEmployeeModel),
        Some(shareDescriptionModel), Some(numberOfSharesModel), Some(listOfInvestorsWithShareHoldings), Some(wasAnyValueReceivedModel), Some(shareCapitalChangesModel),
        Some(MarketDescriptionModel("test")), Some(validSharesRepaymentDetailsVector), Some(grossAssetsAfterIssueModel),
        Some(turnoverCostsValid), Some(researchStartDateModelYes))
      val page = CheckAnswers(model)(authorisedFakeRequest, applicationMessages)
      val document = Jsoup.parse(page.body)

      lazy val investorTableTbody = document.getElementById("investors-table").select("tbody")

      //Section table heading
      document.getElementById("investorsSection-table-heading").text() shouldBe Messages("page.summaryQuestion.investorsSection")

      investorTableTbody.select("tr").get(0).getElementById("numberOfInvestors-question").text() shouldBe
        Messages("page.summaryQuestion.noOfInvestors")
      investorTableTbody.select("tr").get(0).getElementById("numberOfInvestors-answer").text() shouldBe
        listOfInvestorsWithShareHoldings.size.toString
      investorTableTbody.select("tr").get(0).getElementById("numberOfInvestors-link")
        .attr("href") shouldEqual controllers.eis.routes.ReviewAllInvestorsController.show().url

      investorTableTbody.select("tr").get(1).getElementById("anySharesRepaymentModel-question").text() shouldBe
        Messages("page.summaryQuestion.investorShares")
      investorTableTbody.select("tr").get(1).getElementById("anySharesRepaymentModel-answer").text() shouldBe
        anySharesRepaymentModelYes.anySharesRepayment
      investorTableTbody.select("tr").get(1).getElementById("anySharesRepaymentModel-link")
        .attr("href") shouldEqual controllers.eis.routes.AnySharesRepaymentController.show().url

      investorTableTbody.select("tr").get(2).getElementById("repayment-0-question").text() shouldBe
        s"${sharesRepaymentDetailsForIdOne.whoRepaidSharesModel.get.forename} ${sharesRepaymentDetailsForIdOne.whoRepaidSharesModel.get.surname}"
      investorTableTbody.select("tr").get(2).getElementById("repayment-0-Line0").text() shouldBe
        s"${Messages("page.repayments.type.label")} ${sharesRepaymentDetailsForIdOne.sharesRepaymentTypeModel.get.sharesRepaymentType}"
      investorTableTbody.select("tr").get(2).getElementById("repayment-0-Line1").text() shouldBe
        s"${Messages("page.repayments.date.label")}${toDateString(sharesRepaymentDetailsForIdOne.dateSharesRepaidModel.get.day.get,
          sharesRepaymentDetailsForIdOne.dateSharesRepaidModel.get.month.get,sharesRepaymentDetailsForIdOne.dateSharesRepaidModel.get.year.get).trim}"
      investorTableTbody.select("tr").get(2).getElementById("repayment-0-Line2").text() shouldBe
        s"${Messages("page.repayments.amount.label")} ${getAmountAsFormattedString(sharesRepaymentDetailsForIdOne.amountSharesRepaymentModel.get.amount)}"
      investorTableTbody.select("tr").get(2).getElementById("repayment-0-link")
        .attr("href") shouldEqual controllers.eis.routes.ReviewPreviousRepaymentsController.show().url

      investorTableTbody.select("tr").get(3).getElementById("repayment-1-question").text() shouldBe
        s"${sharesRepaymentDetailsForIdTwo.whoRepaidSharesModel.get.forename} ${sharesRepaymentDetailsForIdTwo.whoRepaidSharesModel.get.surname}"
      investorTableTbody.select("tr").get(3).getElementById("repayment-1-Line0").text() shouldBe
        s"${Messages("page.repayments.type.label")} ${sharesRepaymentDetailsForIdTwo.sharesRepaymentTypeModel.get.sharesRepaymentType}"
      investorTableTbody.select("tr").get(3).getElementById("repayment-1-Line1").text() shouldBe
        s"${Messages("page.repayments.date.label")}${toDateString(sharesRepaymentDetailsForIdTwo.dateSharesRepaidModel.get.day.get,
          sharesRepaymentDetailsForIdTwo.dateSharesRepaidModel.get.month.get,sharesRepaymentDetailsForIdTwo.dateSharesRepaidModel.get.year.get)}"
      investorTableTbody.select("tr").get(3).getElementById("repayment-1-Line2").text() shouldBe
        s"${Messages("page.repayments.amount.label")} ${getAmountAsFormattedString(sharesRepaymentDetailsForIdTwo.amountSharesRepaymentModel.get.amount)}"
      investorTableTbody.select("tr").get(3).getElementById("repayment-1-link")
        .attr("href") shouldEqual controllers.eis.routes.ReviewPreviousRepaymentsController.show().url

      investorTableTbody.select("tr").get(4).getElementById("valueReceived-question").text() shouldBe
        Messages("page.summaryQuestion.isSharesValueReceived")+" "+Messages("page.summaryQuestion.sharesValueReceived")
      investorTableTbody.select("tr").get(4).getElementById("valueReceived-answer").text() shouldBe
        wasAnyValueReceivedModel.wasAnyValueReceived +" "+ wasAnyValueReceivedModel.aboutValueReceived.get
      investorTableTbody.select("tr").get(4).getElementById("valueReceived-link")
        .attr("href") shouldEqual controllers.eis.routes.WasAnyValueReceivedController.show().url

      investorTableTbody.select("tr").get(5).getElementById("shareCapitalChanges-Question0").text() shouldBe
        Messages("page.summaryQuestion.isSharesValueChanged")
      investorTableTbody.select("tr").get(5).getElementById("shareCapitalChanges-Question2").text() shouldBe
        Messages("page.summaryQuestion.sharesValueChanges")
      investorTableTbody.select("tr").get(5).getElementById("shareCapitalChanges-Answer0").text() shouldBe
        shareCapitalChangesModel.hasChanges
      investorTableTbody.select("tr").get(5).getElementById("shareCapitalChanges-Answer2").text() shouldBe
        shareCapitalChangesModel.changesDescription.get
      investorTableTbody.select("tr").get(5).getElementById("shareCapitalChanges-link")
        .attr("href") shouldEqual controllers.eis.routes.ShareCapitalChangesController.show().url
    }

    "Verify that the Check Answers page contains an empty table for Section 3: Investment" +
      " when the investment models are empty" in new SEISSetup {
      val model = CheckAnswersModel(Some(registeredAddressModel), Some(dateOfIncorporationModel), Some(natureOfBusinessModel),
        Some(commercialSaleModelYes), Some(isCompanyKnowledgeIntensiveModelYes), Some(isKnowledgeIntensiveModelYes),
        Some(operatingCostsModel), Some(percentageStaffWithMastersModelNo), Some(tenYearPlanModelYes), Some(hadPreviousRFIModelYes),
        previousSchemesValid, Some(totalAmountRaisedValid), Some(thirtyDayRuleModelYes), None, Some(newGeographicalMarketModelYes),
        Some(newProductMarketModelYes), Some(contactDetailsModel), Some(addressModel), Some(investmentGrowModel), Some(qualifyTrade),
        Some(hasInvestmentTradeStartedModelYes), Some(shareIssuetDateModel), Some(grossAssetsModel), Some(fullTimeEmployeeModel),
        Some(shareDescriptionModel), Some(numberOfSharesModel), None, None, None, Some(MarketDescriptionModel("test")),
        None, Some(grossAssetsAfterIssueModel),
        Some(turnoverCostsValid), Some(researchStartDateModelYes))
      val page = CheckAnswers(model)(authorisedFakeRequest, applicationMessages)
      val document = Jsoup.parse(page.body)

      lazy val investmentTableTbody = document.getElementById("investors-table").select("tbody")
      lazy val notAvailableMessage = Messages("common.notAvailable")

      //Section table heading
      document.getElementById("investorsSection-table-heading").text() shouldBe Messages("page.summaryQuestion.investorsSection")
      investmentTableTbody.select("tr").size() shouldBe 0
    }
  }
}

