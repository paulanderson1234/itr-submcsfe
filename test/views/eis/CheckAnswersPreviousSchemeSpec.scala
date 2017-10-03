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

import auth.{MockAuthConnector, MockConfigEISFlow}
import common.Constants
import controllers.eis.CheckAnswersController
import models.PreviousSchemeModel._
import models._
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import views.helpers.CheckAnswersSpec
import views.html.eis.checkAndSubmit.CheckAnswers

class CheckAnswersPreviousSchemeSpec extends CheckAnswersSpec {

  object TestController extends CheckAnswersController {
     override lazy val applicationConfig = MockConfigEISFlow
     override lazy val authConnector = MockAuthConnector
     override lazy val s4lConnector = mockS4lConnector
     override lazy val enrolmentConnector = mockEnrolmentConnector
   }

  "The Check Answers page" should {

    "Verify that the Check Answers page contains the correct elements for Section 2: Previous Schemes" +
      " when a Vector of previous schemes can be retrieved" in new Setup {
      val model = CheckAnswersModel(Some(registeredAddressModel), Some(dateOfIncorporationModel), Some(natureOfBusinessModel),
        Some(commercialSaleModelYes), Some(isCompanyKnowledgeIntensiveModelYes), Some(isKnowledgeIntensiveModelYes),
        Some(operatingCostsModel), Some(percentageStaffWithMastersModelNo), Some(tenYearPlanModelYes), Some(hadPreviousRFIModelYes),
        previousSchemesValid, Some(totalAmountRaisedValid), Some(thirtyDayRuleModelYes), Some(anySharesRepaymentModelYes), Some(newGeographicalMarketModelYes),
        Some(newProductMarketModelYes), Some(contactDetailsModel), Some(addressModel), Some(investmentGrowModel), Some(qualifyTrade),
        Some(hasInvestmentTradeStartedModelYes), Some(shareIssuetDateModel), Some(grossAssetsModel), Some(fullTimeEmployeeModel),
        Some(shareDescriptionModel), Some(numberOfSharesModel), Some(listOfInvestorsWithShareHoldings), Some(WasAnyValueReceivedModel(Constants.StandardRadioButtonYesValue,
          Some("text"))), Some(ShareCapitalChangesModel(Constants.StandardRadioButtonYesValue, Some("test"))), Some(MarketDescriptionModel("test")),
        Some(validSharesRepaymentDetailsVector), Some(grossAssetsAfterIssueModel),
        Some(turnoverCostsValid), Some(researchStartDateModelYes), false)
      val page = CheckAnswers(model)(authorisedFakeRequest, applicationMessages)
      val document = Jsoup.parse(page.body)

      document.getElementById("main-heading").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.heading")
      document.getElementById("description-one").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.description.one") +" " +
        Messages("page.checkAndSubmit.checkAnswers.scheme.eis")
      document.getElementById("description-two").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.description.two")


      //Section 1 table heading
      document.getElementById("previousRFISection-table-heading").text() shouldBe Messages("summaryQuestion.previousRFISection")
      //Previous RFI None
      document.getElementById("previousScheme-0-question").text shouldBe PreviousSchemeModel.getSchemeName(
        previousSchemesValid(0).schemeTypeDesc, previousSchemesValid(0).otherSchemeName)
      document.getElementById("previousScheme-0-Line1").text shouldBe
        s"${Messages("page.investment.amount.label")} ${getAmountAsFormattedString(previousSchemesValid(0).investmentAmount)}"
      document.getElementById("previousScheme-0-Line2").text shouldBe
        s"${Messages("page.investment.amountSpent.label")} ${getAmountAsFormattedString(previousSchemesValid(0).investmentSpent.get)}"
      document.getElementById("previousScheme-0-Line0").text shouldBe
        s"${Messages("page.investment.dateOfShareIssue.label")} ${toDateString(previousSchemesValid(0).day.get,previousSchemesValid(0).month.get,
          previousSchemesValid(0).year.get)}"
      document.getElementById("previousScheme-0-link").attr("href") shouldBe controllers.eis.routes.ReviewPreviousSchemesController.show().url
      document.getElementById("previousScheme-1-question").text shouldBe PreviousSchemeModel.getSchemeName(
        previousSchemesValid(1).schemeTypeDesc, previousSchemesValid(1).otherSchemeName)
      document.getElementById("previousScheme-1-Line1").text shouldBe
        s"${Messages("page.investment.amount.label")} ${getAmountAsFormattedString(previousSchemesValid(1).investmentAmount)}"
      document.getElementById("previousScheme-1-Line2").text shouldBe
        s"${Messages("page.investment.amountSpent.label")} ${getAmountAsFormattedString(previousSchemesValid(1).investmentSpent.get)}"
      document.getElementById("previousScheme-1-Line0").text shouldBe
        s"${Messages("page.investment.dateOfShareIssue.label")} ${toDateString(previousSchemesValid(1).day.get,previousSchemesValid(1).month.get,
          previousSchemesValid(1).year.get)}"
      document.getElementById("previousScheme-1-link").attr("href") shouldBe controllers.eis.routes.ReviewPreviousSchemesController.show().url
      document.getElementById("previousScheme-2-question").text shouldBe PreviousSchemeModel.getSchemeName(
        previousSchemesValid(2).schemeTypeDesc,previousSchemesValid(2).otherSchemeName)
      document.getElementById("previousScheme-2-Line1").text shouldBe
        s"${Messages("page.investment.amount.label")} ${getAmountAsFormattedString(previousSchemesValid(2).investmentAmount)}"
      document.getElementById("previousScheme-2-Line2").text shouldBe
        s"${Messages("page.investment.amountSpent.label")} ${getAmountAsFormattedString(previousSchemesValid(2).investmentSpent.get)}"
      document.getElementById("previousScheme-2-Line0").text shouldBe
        s"${Messages("page.investment.dateOfShareIssue.label")} ${toDateString(previousSchemesValid(2).day.get,previousSchemesValid(2).month.get,
          previousSchemesValid(2).year.get)}"
      document.getElementById("previousScheme-2-link").attr("href") shouldBe controllers.eis.routes.ReviewPreviousSchemesController.show().url
      document.getElementById("submit").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.button.confirm")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.SupportingDocumentsController.show().url
    }

    "Verify that the Check Answers page contains the correct elements for Section 2: Previous Schemes" +
      " when an empty Vector is be retrieved" in new Setup {
      val model = CheckAnswersModel(Some(registeredAddressModel), Some(dateOfIncorporationModel), Some(natureOfBusinessModel),
        Some(commercialSaleModelYes), Some(isCompanyKnowledgeIntensiveModelYes), Some(isKnowledgeIntensiveModelYes),
        Some(operatingCostsModel), Some(percentageStaffWithMastersModelNo), Some(tenYearPlanModelYes), Some(hadPreviousRFIModelYes),
        Vector(), Some(totalAmountRaisedValid), Some(thirtyDayRuleModelYes), Some(anySharesRepaymentModelYes), Some(newGeographicalMarketModelYes),
        Some(newProductMarketModelYes), Some(contactDetailsModel), Some(addressModel), Some(investmentGrowModel), Some(qualifyTrade),
        Some(hasInvestmentTradeStartedModelYes), Some(shareIssuetDateModel), Some(grossAssetsModel), Some(fullTimeEmployeeModel),
        Some(shareDescriptionModel), Some(numberOfSharesModel), Some(listOfInvestorsWithShareHoldings), Some(WasAnyValueReceivedModel(Constants.StandardRadioButtonYesValue,
          Some("text"))), Some(ShareCapitalChangesModel(Constants.StandardRadioButtonYesValue, Some("test"))), Some(MarketDescriptionModel("test")),
        Some(validSharesRepaymentDetailsVector), Some(grossAssetsAfterIssueModel),
        Some(turnoverCostsValid), Some(researchStartDateModelYes), false)

      val page = CheckAnswers(model)(authorisedFakeRequest, applicationMessages)
      val document = Jsoup.parse(page.body)

      document.getElementById("main-heading").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.heading")
      document.getElementById("description-one").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.description.one") +" " +
        Messages("page.checkAndSubmit.checkAnswers.scheme.eis")
      document.getElementById("description-two").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.description.two")


      //Section 1 table heading
      document.getElementById("previousRFISection-table-heading").text() shouldBe Messages("summaryQuestion.previousRFISection")
      //Previous RFI None
      document.getElementById("noPreviousScheme-question").text shouldBe Messages("page.summaryQuestion.none.question")
      document.getElementById("noPreviousScheme-answer").text shouldBe Messages("page.summaryQuestion.none.answer")
      document.getElementById("noPreviousScheme-link").attr("href") shouldBe controllers.eis.routes.HadPreviousRFIController.show().url
      document.getElementById("submit").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.button.confirm")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.SupportingDocumentsController.show().url
    }
  }
}
