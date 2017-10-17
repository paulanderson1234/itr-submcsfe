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
import config.FrontendAppConfig
import controllers.eis.CheckAnswersController
import models._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._
import utils.CountriesHelper
import views.helpers.CheckAnswersSpec
import views.html.eis.checkAndSubmit.CheckAnswers

class CheckAnswersContactDetailsSpec extends CheckAnswersSpec {

  object TestController extends CheckAnswersController {
    override lazy val applicationConfig = MockConfigEISFlow
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
    override val emailVerificationService = mockEmailVerificationService
  }

  "The Check Answers page" should {

    "Verify that the Check Answers page contains the correct elements for Section 4: Contact Details" +
      " when the contact details model is fully populated" in new Setup {
      val model = CheckAnswersModel(Some(registeredAddressModel), Some(dateOfIncorporationModel), Some(natureOfBusinessModel),
        Some(commercialSaleModelYes), Some(isCompanyKnowledgeIntensiveModelYes), Some(isKnowledgeIntensiveModelYes),
        Some(operatingCostsModel), Some(percentageStaffWithMastersModelNo), Some(tenYearPlanModelYes), Some(hadPreviousRFIModelYes),
        Vector(), Some(totalAmountRaisedValid), Some(thirtyDayRuleModelYes), Some(anySharesRepaymentModelYes), Some(newGeographicalMarketModelYes),
        Some(newProductMarketModelYes), Some(contactDetailsModel), Some(contactAddressModel), Some(investmentGrowModel), Some(qualifyTrade),
        Some(hasInvestmentTradeStartedModelYes), Some(shareIssuetDateModel), Some(grossAssetsModel), Some(fullTimeEmployeeModel),
        Some(shareDescriptionModel), Some(numberOfSharesModel), Some(listOfInvestorsWithShareHoldings), Some(WasAnyValueReceivedModel(Constants.StandardRadioButtonYesValue,
          Some("text"))), Some(ShareCapitalChangesModel(Constants.StandardRadioButtonYesValue, Some("test"))), Some(MarketDescriptionModel("test")),
        Some(validSharesRepaymentDetailsVector), Some(grossAssetsAfterIssueModel),
        Some(turnoverCostsValid), Some(researchStartDateModelYes), false)

      val page = CheckAnswers(model)(authorisedFakeRequest, applicationMessages)
      val document = Jsoup.parse(page.body)

      lazy val contactDetailsTable = document.getElementById("contactDetails-table").select("tbody")

      //Section table heading
      document.getElementById("contactDetailsSection-table-heading").text() shouldBe Messages("page.summaryQuestion.companyDetailsSectionFive")

      // contactDetails
      contactDetailsTable.select("tr").get(0).getElementById("contactDetails-question").text() shouldBe
        Messages("page.summaryQuestion.contactDetails")

      contactDetailsTable.select("tr").get(0).getElementById("contactDetails-Line0").text() shouldBe
        contactDetailsModel.fullName
      contactDetailsTable.select("tr").get(0).getElementById("contactDetails-Line1").text() shouldBe
        contactDetailsModel.telephoneNumber.get
      contactDetailsTable.select("tr").get(0).getElementById("contactDetails-Line2").text() shouldBe
        contactDetailsModel.mobileNumber.get
      contactDetailsTable.select("tr").get(0).getElementById("contactDetails-Line3").text() shouldBe
        contactDetailsModel.email

      contactDetailsTable.select("tr").get(0).getElementById("contactDetails-link")
        .attr("href") shouldBe controllers.eis.routes.ContactDetailsController.show().toString

      //address
      contactDetailsTable.select("tr").get(1).getElementById("address-question").text() shouldBe
        Messages("page.summaryQuestion.contactAddress")
      contactDetailsTable.select("tr").get(1).getElementById("address-Line0").text() shouldBe
        contactAddressModel.addressline1
      contactDetailsTable.select("tr").get(1).getElementById("address-Line1").text() shouldBe
        contactAddressModel.addressline2
      contactDetailsTable.select("tr").get(1).getElementById("address-Line2").text() shouldBe
        CountriesHelper.getSelectedCountry(contactAddressModel.countryCode)
      contactDetailsTable.select("tr").get(1).getElementById("address-link")
        .attr("href") shouldBe controllers.eis.routes.ContactAddressController.show().toString
    }
  }

  "The Check Answers page" should {

    "Verify that the Check Answers page contains an empty table for Section 4: Contact Details" +
      " when the contact details model is not populated" in new Setup {
      val model = CheckAnswersModel(Some(registeredAddressModel), Some(dateOfIncorporationModel), Some(natureOfBusinessModel),
        Some(commercialSaleModelYes), Some(isCompanyKnowledgeIntensiveModelYes), Some(isKnowledgeIntensiveModelYes),
        Some(operatingCostsModel), Some(percentageStaffWithMastersModelNo), Some(tenYearPlanModelYes), Some(hadPreviousRFIModelYes),
        Vector(), Some(totalAmountRaisedValid), Some(thirtyDayRuleModelYes), Some(anySharesRepaymentModelYes), Some(newGeographicalMarketModelYes),
        Some(newProductMarketModelYes), None, None, Some(investmentGrowModel), Some(qualifyTrade),
        Some(hasInvestmentTradeStartedModelYes), Some(shareIssuetDateModel), Some(grossAssetsModel), Some(fullTimeEmployeeModel),
        Some(shareDescriptionModel), Some(numberOfSharesModel), Some(listOfInvestorsWithShareHoldings), Some(WasAnyValueReceivedModel(Constants.StandardRadioButtonYesValue,
          Some("text"))), Some(ShareCapitalChangesModel(Constants.StandardRadioButtonYesValue, Some("test"))), Some(MarketDescriptionModel("test")),
        Some(validSharesRepaymentDetailsVector), Some(grossAssetsAfterIssueModel),
        Some(turnoverCostsValid), Some(researchStartDateModelYes), false)

      val page = CheckAnswers(model)(authorisedFakeRequest, applicationMessages)
      val document = Jsoup.parse(page.body)

      lazy val contactDetailsTable = document.getElementById("contactDetails-table").select("tbody")
      lazy val notAvailableMessage = Messages("common.notAvailable")

      //Section table heading
      document.getElementById("contactDetailsSection-table-heading").text() shouldBe Messages("page.summaryQuestion.companyDetailsSectionFive")

      contactDetailsTable.select("tr").size() shouldBe 0
    }
  }
}
