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
import models._
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import views.helpers.CheckAnswersSpec
import views.html.eis.checkAndSubmit.CheckAnswers

class CheckAnswersCompanyDetailsSpec extends CheckAnswersSpec {

  object TestController extends CheckAnswersController {
    override lazy val applicationConfig = MockConfigEISFlow
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
    override val emailVerificationService = mockEmailVerificationService
  }

  "The Check Answers page" should {

    "Verify that the Check Answers page contains the correct elements for Section 1: Company details" +
      " when it is fully populated with company detail models" in new Setup {
      val model = CheckAnswersModel(Some(registeredAddressModel), Some(dateOfIncorporationModel), Some(natureOfBusinessModel),
        Some(commercialSaleModelYes), Some(isCompanyKnowledgeIntensiveModelYes), Some(isKnowledgeIntensiveModelYes),
        Some(operatingCostsModel), Some(percentageStaffWithMastersModelNo), Some(tenYearPlanModelYes), Some(hadPreviousRFIModelYes),
        Vector(), Some(totalAmountRaisedValid), Some(thirtyDayRuleModelYes), Some(anySharesRepaymentModelYes), Some(newGeographicalMarketModelYes),
        Some(newProductMarketModelYes), Some(contactDetailsModel), Some(addressModel), Some(investmentGrowModel), Some(qualifyTrade),
        Some(hasInvestmentTradeStartedModelYes), Some(shareIssuetDateModel), Some(grossAssetsModel), Some(fullTimeEmployeeModel),
        Some(shareDescriptionModel), Some(numberOfSharesModel), Some(listOfInvestorsWithShareHoldings), Some(WasAnyValueReceivedModel(Constants.StandardRadioButtonYesValue,
          Some("text"))), Some(ShareCapitalChangesModel(Constants.StandardRadioButtonYesValue, Some("test"))), Some(MarketDescriptionModel("test")),
        Some(validSharesRepaymentDetailsVector), Some(grossAssetsAfterIssueModel),
        Some(turnoverCostsValid), Some(researchStartDateModelYes))

      val page = CheckAnswers(model)(authorisedFakeRequest, applicationMessages)
      val document = Jsoup.parse(page.body)

      lazy val companyDetailsTableTBody = document.getElementById("company-details-table").select("tbody")
      lazy val notAvailableMessage = Messages("common.notAvailable")

      document.getElementById("main-heading").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.heading")
      document.getElementById("description-one").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.description.one") +" " +
        Messages("page.checkAndSubmit.checkAnswers.scheme.eis")
      document.getElementById("description-two").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.description.two")

      //Section 1 table heading
      document.getElementById("companyDetailsSection-table-heading").text() shouldBe Messages("summaryQuestion.companyDetailsSection")
      //Nature of business
      companyDetailsTableTBody.select("tr").get(0).getElementById("natureOfBusiness-question").text() shouldBe
        Messages("summaryQuestion.natureOfBusiness")
      companyDetailsTableTBody.select("tr").get(0).getElementById("natureOfBusiness-answer").text() shouldBe
        natureOfBusinessModel.natureofbusiness
      companyDetailsTableTBody.select("tr").get(0).getElementById("natureOfBusiness-link")
        .attr("href") shouldEqual controllers.eis.routes.NatureOfBusinessController.show().url
      //Date of incorporation
      companyDetailsTableTBody.select("tr").get(1).getElementById("dateOfIncorporation-question").text() shouldBe
        Messages("summaryQuestion.dateOfIncorporation")
      companyDetailsTableTBody.select("tr").get(1).getElementById("dateOfIncorporation-answer").text() shouldBe
        DateOfIncorporationModel.toDateString(dateOfIncorporationModel.day.get,dateOfIncorporationModel.month.get,dateOfIncorporationModel.year.get)
      companyDetailsTableTBody.select("tr").get(1).getElementById("dateOfIncorporation-link")
        .attr("href") shouldEqual controllers.eis.routes.DateOfIncorporationController.show().url
      //  qualifying business
      companyDetailsTableTBody.select("tr").get(2).getElementById("qualifyBusinessActivity-question").text() shouldBe
        Messages("summaryQuestion.bussinessPurpose")
      companyDetailsTableTBody.select("tr").get(2).getElementById("qualifyBusinessActivity-answer").text() shouldBe
        Messages("page.companyDetails.qualifyBusinessActivity.bullet.one")
      companyDetailsTableTBody.select("tr").get(2).getElementById("qualifyBusinessActivity-link")
        .attr("href") shouldEqual controllers.eis.routes.QualifyBusinessActivityController.show().url
      //Date of first commercial sale
      companyDetailsTableTBody.select("tr").get(4).getElementById("commercialSale-Question0").text() shouldBe
        Messages("summaryQuestion.hasCommercialSale")
      companyDetailsTableTBody.select("tr").get(4).getElementById("commercialSale-Answer0").text() shouldBe
        commercialSaleModelYes.hasCommercialSale
      companyDetailsTableTBody.select("tr").get(4).getElementById("commercialSale-Question2").text() shouldBe
        Messages("summaryQuestion.commercialSaleDate")
      companyDetailsTableTBody.select("tr").get(4).getElementById("commercialSale-Answer2").text() shouldBe
        CommercialSaleModel.toDateString(commercialSaleModelYes.commercialSaleDay.get,commercialSaleModelYes.commercialSaleMonth.get,commercialSaleModelYes.commercialSaleYear.get)
      companyDetailsTableTBody.select("tr").get(4).getElementById("commercialSale-link")
        .attr("href") shouldEqual controllers.eis.routes.CommercialSaleController.show().url

      //shareIssueDate
      companyDetailsTableTBody.select("tr").get(5).getElementById("shareIssueDate-question").text() shouldBe
        Messages("summaryQuestion.shareIssueDate")
      companyDetailsTableTBody.select("tr").get(5).getElementById("shareIssueDate-answer").text() shouldBe
        ShareIssueDateModel.toDateString(shareIssuetDateModel.day.get, shareIssuetDateModel.month.get, shareIssuetDateModel.year.get)
      companyDetailsTableTBody.select("tr").get(5).getElementById("shareIssueDate-link")
        .attr("href") shouldEqual controllers.eis.routes.ShareIssueDateController.show().url

      //Gross assets
      companyDetailsTableTBody.select("tr").get(6).getElementById("grossAssets-question").text() shouldBe
        Messages("summaryQuestion.businessGrossAssets")
      companyDetailsTableTBody.select("tr").get(6).getElementById("grossAssets-answer").text() shouldBe
        GrossAssetsModel.getAmountAsFormattedString(grossAssetsModel.grossAmount)
      companyDetailsTableTBody.select("tr").get(6).getElementById("grossAssets-link")
        .attr("href") shouldEqual controllers.eis.routes.GrossAssetsController.show().url

      //Gross assets
      companyDetailsTableTBody.select("tr").get(7).getElementById("grossAssetsAfterIssue-question").text() shouldBe
        Messages("summaryQuestion.businessGrossAssetsAfterIssue")
      companyDetailsTableTBody.select("tr").get(7).getElementById("grossAssetsAfterIssue-answer").text() shouldBe
        GrossAssetsModel.getAmountAsFormattedString(grossAssetsAfterIssueModel.grossAmount)
      companyDetailsTableTBody.select("tr").get(7).getElementById("grossAssetsAfterIssue-link")
        .attr("href") shouldEqual controllers.eis.routes.GrossAssetsAfterIssueController.show().url

      //Is Company Knowledge Intensive
      companyDetailsTableTBody.select("tr").get(8).getElementById("isCompanyKnowledgeIntensive-question").text() shouldBe
        Messages("summaryQuestion.isCompanyKnowledgeIntensiveModel")
      companyDetailsTableTBody.select("tr").get(8).getElementById("isCompanyKnowledgeIntensive-answer").text() shouldBe
        isKnowledgeIntensiveModelYes.isKnowledgeIntensive
      companyDetailsTableTBody.select("tr").get(8).getElementById("isCompanyKnowledgeIntensive-link")
        .attr("href") shouldEqual controllers.eis.routes.IsCompanyKnowledgeIntensiveController.show().url

      //Is Knowledge Intensive
      companyDetailsTableTBody.select("tr").get(9).getElementById("knowledgeIntensive-question").text() shouldBe
        Messages("summaryQuestion.knowledgeIntensive")
      companyDetailsTableTBody.select("tr").get(9).getElementById("knowledgeIntensive-answer").text() shouldBe
        isKnowledgeIntensiveModelYes.isKnowledgeIntensive
      companyDetailsTableTBody.select("tr").get(9).getElementById("knowledgeIntensive-link")
        .attr("href") shouldEqual controllers.eis.routes.IsKnowledgeIntensiveController.show().url


      //Operating costs
      companyDetailsTableTBody.select("tr").get(10).getElementById("operatingCosts-Question0").text() shouldBe
        Messages("summaryQuestion.operatingCosts")

      companyDetailsTableTBody.select("tr").get(10).getElementById("operatingCosts-Question3").text() shouldBe
        Messages("summaryQuestion.rdCosts")

      companyDetailsTableTBody.select("tr").get(10).getElementById("operatingCosts-Answer0").text() shouldBe
        OperatingCostsModel.getOperatingAndRDCostsAsFormattedString(operatingCostsModel.operatingCosts1stYear, Messages("page.companyDetails.OperatingCosts.row.heading.one"))

      companyDetailsTableTBody.select("tr").get(10).getElementById("operatingCosts-Answer1").text() shouldBe
        OperatingCostsModel.getOperatingAndRDCostsAsFormattedString(operatingCostsModel.operatingCosts2ndYear, Messages("page.companyDetails.OperatingCosts.row.heading.two"))

      companyDetailsTableTBody.select("tr").get(10).getElementById("operatingCosts-Answer2").text() shouldBe
        OperatingCostsModel.getOperatingAndRDCostsAsFormattedString(operatingCostsModel.operatingCosts3rdYear, Messages("page.companyDetails.OperatingCosts.row.heading.three"))

      companyDetailsTableTBody.select("tr").get(10).getElementById("operatingCosts-link")
        .attr("href") shouldEqual controllers.eis.routes.OperatingCostsController.show().url

      companyDetailsTableTBody.select("tr").get(10).getElementById("operatingCosts-Answer3").text() shouldBe
        OperatingCostsModel.getOperatingAndRDCostsAsFormattedString(operatingCostsModel.rAndDCosts1stYear, Messages("page.companyDetails.OperatingCosts.row.heading.one"))

      companyDetailsTableTBody.select("tr").get(10).getElementById("operatingCosts-Answer4").text() shouldBe
        OperatingCostsModel.getOperatingAndRDCostsAsFormattedString(operatingCostsModel.rAndDCosts2ndYear, Messages("page.companyDetails.OperatingCosts.row.heading.two"))

      companyDetailsTableTBody.select("tr").get(10).getElementById("operatingCosts-Answer5").text() shouldBe
        OperatingCostsModel.getOperatingAndRDCostsAsFormattedString(operatingCostsModel.rAndDCosts3rdYear, Messages("page.companyDetails.OperatingCosts.row.heading.three"))

      //Percentage of staff with masters
      companyDetailsTableTBody.select("tr").get(11).getElementById("percentageStaffWithMasters-question").text() shouldBe
        Messages("summaryQuestion.percentageStaffWithMasters")
      companyDetailsTableTBody.select("tr").get(11).getElementById("percentageStaffWithMasters-answer").text() shouldBe
        PercentageStaffWithMastersModel.staffWithMastersToString(percentageStaffWithMastersModelNo.staffWithMasters)
      companyDetailsTableTBody.select("tr").get(11).getElementById("percentageStaffWithMasters-link")
        .attr("href") shouldEqual controllers.eis.routes.PercentageStaffWithMastersController.show().url

      //Ten year plan
      companyDetailsTableTBody.select("tr").get(12).getElementById("tenYearPlan-Question0").text() shouldBe
        Messages("summaryQuestion.developmentPlan")
      companyDetailsTableTBody.select("tr").get(12).getElementById("tenYearPlan-Answer0").text() shouldBe
        tenYearPlanModelYes.hasTenYearPlan
      companyDetailsTableTBody.select("tr").get(12).getElementById("tenYearPlan-Question1").text() shouldBe
        Messages("summaryQuestion.developmentPlanDesc")
      companyDetailsTableTBody.select("tr").get(12).getElementById("tenYearPlan-Answer1").text() shouldBe
        tenYearPlanModelYes.tenYearPlanDesc.get

      companyDetailsTableTBody.select("tr").get(12).getElementById("tenYearPlan-link")
        .attr("href") shouldEqual controllers.eis.routes.TenYearPlanController.show().url

      //Fulltime employees
      companyDetailsTableTBody.select("tr").get(13).getElementById("fullTimeEmployees-question").text() shouldBe
        Messages("summaryQuestion.fullTimeEmployees")
      companyDetailsTableTBody.select("tr").get(13).getElementById("fullTimeEmployees-answer").text() shouldBe
        fullTimeEmployeeModel.employeeCount.toString()
      companyDetailsTableTBody.select("tr").get(13).getElementById("fullTimeEmployees-link")
        .attr("href") shouldEqual controllers.eis.routes.FullTimeEmployeeCountController.show().url

      document.getElementById("submit").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.button.confirm")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.SupportingDocumentsUploadController.show().url
    }
  }


  "The Check Answers page" should {

    "Verify that the Check Answers page contains an empty table for Section 1: Company details" +
      " when an empty set of company detail models are passed" in new Setup {
      val model = CheckAnswersModel(Some(registeredAddressModel), Some(dateOfIncorporationModel), Some(natureOfBusinessModel),
        Some(commercialSaleModelYes), Some(isCompanyKnowledgeIntensiveModelYes), Some(isKnowledgeIntensiveModelYes),
        Some(operatingCostsModel), Some(percentageStaffWithMastersModelYes), Some(tenYearPlanModelYes), Some(hadPreviousRFIModelYes),
        Vector(), Some(totalAmountRaisedValid), Some(thirtyDayRuleModelYes), Some(anySharesRepaymentModelYes), Some(newGeographicalMarketModelYes),
        Some(newProductMarketModelYes), Some(contactDetailsModel), Some(addressModel), Some(investmentGrowModel), Some(qualifyTrade),
        Some(hasInvestmentTradeStartedModelYes), Some(shareIssuetDateModel), Some(grossAssetsModel), Some(FullTimeEmployeeCountModel(22)),
        Some(shareDescriptionModel), Some(numberOfSharesModel), Some(listOfInvestorsWithShareHoldings), Some(WasAnyValueReceivedModel(Constants.StandardRadioButtonYesValue,
          Some("text"))), Some(ShareCapitalChangesModel(Constants.StandardRadioButtonYesValue, Some("test"))), Some(MarketDescriptionModel("test")),
        Some(validSharesRepaymentDetailsVector), Some(grossAssetsAfterIssueModel),
        Some(turnoverCostsValid), Some(researchStartDateModelYes))

      val page = CheckAnswers(model)(authorisedFakeRequest, applicationMessages)
      val document = Jsoup.parse(page.body)

      lazy val companyDetailsTableTBody = document.getElementById("company-details-table").select("tbody")
      lazy val notAvailableMessage = Messages("common.notAvailable")

      document.getElementById("main-heading").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.heading")
      document.getElementById("description-one").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.description.one") +" " +
        Messages("page.checkAndSubmit.checkAnswers.scheme.eis")
      document.getElementById("description-two").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.description.two")

      //Section 1 table heading
      document.getElementById("companyDetailsSection-table-heading").text() shouldBe Messages("summaryQuestion.companyDetailsSection")
      //Nature of business
      companyDetailsTableTBody.select("tr").get(0).getElementById("natureOfBusiness-question").text() shouldBe
        Messages("summaryQuestion.natureOfBusiness")
      companyDetailsTableTBody.select("tr").get(0).getElementById("natureOfBusiness-answer").text() shouldBe
        natureOfBusinessModel.natureofbusiness
      companyDetailsTableTBody.select("tr").get(0).getElementById("natureOfBusiness-link")
        .attr("href") shouldEqual controllers.eis.routes.NatureOfBusinessController.show().url
      //Date of incorporation
      companyDetailsTableTBody.select("tr").get(1).getElementById("dateOfIncorporation-question").text() shouldBe
        Messages("summaryQuestion.dateOfIncorporation")
      companyDetailsTableTBody.select("tr").get(1).getElementById("dateOfIncorporation-answer").text() shouldBe
        DateOfIncorporationModel.toDateString(dateOfIncorporationModel.day.get,dateOfIncorporationModel.month.get,dateOfIncorporationModel.year.get)
      companyDetailsTableTBody.select("tr").get(1).getElementById("dateOfIncorporation-link")
        .attr("href") shouldEqual controllers.eis.routes.DateOfIncorporationController.show().url
      //  qualifying business
      companyDetailsTableTBody.select("tr").get(2).getElementById("qualifyBusinessActivity-question").text() shouldBe
        Messages("summaryQuestion.bussinessPurpose")
      companyDetailsTableTBody.select("tr").get(2).getElementById("qualifyBusinessActivity-answer").text() shouldBe
        Messages("page.companyDetails.qualifyBusinessActivity.bullet.one")
      companyDetailsTableTBody.select("tr").get(2).getElementById("qualifyBusinessActivity-link")
        .attr("href") shouldEqual controllers.eis.routes.QualifyBusinessActivityController.show().url
      //Date of first commercial sale
      companyDetailsTableTBody.select("tr").get(4).getElementById("commercialSale-Question0").text() shouldBe
        Messages("summaryQuestion.hasCommercialSale")
      companyDetailsTableTBody.select("tr").get(4).getElementById("commercialSale-Answer0").text() shouldBe
        commercialSaleModelYes.hasCommercialSale
      companyDetailsTableTBody.select("tr").get(4).getElementById("commercialSale-Question2").text() shouldBe
        Messages("summaryQuestion.commercialSaleDate")
      companyDetailsTableTBody.select("tr").get(4).getElementById("commercialSale-Answer2").text() shouldBe
        CommercialSaleModel.toDateString(commercialSaleModelYes.commercialSaleDay.get,commercialSaleModelYes.commercialSaleMonth.get,commercialSaleModelYes.commercialSaleYear.get)
      companyDetailsTableTBody.select("tr").get(4).getElementById("commercialSale-link")
        .attr("href") shouldEqual controllers.eis.routes.CommercialSaleController.show().url

      //shareIssueDate
      companyDetailsTableTBody.select("tr").get(5).getElementById("shareIssueDate-question").text() shouldBe
        Messages("summaryQuestion.shareIssueDate")
      companyDetailsTableTBody.select("tr").get(5).getElementById("shareIssueDate-answer").text() shouldBe
        ShareIssueDateModel.toDateString(shareIssuetDateModel.day.get, shareIssuetDateModel.month.get, shareIssuetDateModel.year.get)
      companyDetailsTableTBody.select("tr").get(5).getElementById("shareIssueDate-link")
        .attr("href") shouldEqual controllers.eis.routes.ShareIssueDateController.show().url

      //Gross assets
      companyDetailsTableTBody.select("tr").get(6).getElementById("grossAssets-question").text() shouldBe
        Messages("summaryQuestion.businessGrossAssets")
      companyDetailsTableTBody.select("tr").get(6).getElementById("grossAssets-answer").text() shouldBe
        GrossAssetsModel.getAmountAsFormattedString(grossAssetsModel.grossAmount)
      companyDetailsTableTBody.select("tr").get(6).getElementById("grossAssets-link")
        .attr("href") shouldEqual controllers.eis.routes.GrossAssetsController.show().url

      //Gross assets
      companyDetailsTableTBody.select("tr").get(7).getElementById("grossAssetsAfterIssue-question").text() shouldBe
        Messages("summaryQuestion.businessGrossAssetsAfterIssue")
      companyDetailsTableTBody.select("tr").get(7).getElementById("grossAssetsAfterIssue-answer").text() shouldBe
        GrossAssetsModel.getAmountAsFormattedString(grossAssetsAfterIssueModel.grossAmount)
      companyDetailsTableTBody.select("tr").get(7).getElementById("grossAssetsAfterIssue-link")
        .attr("href") shouldEqual controllers.eis.routes.GrossAssetsAfterIssueController.show().url

      //Is Company Knowledge Intensive
      companyDetailsTableTBody.select("tr").get(8).getElementById("isCompanyKnowledgeIntensive-question").text() shouldBe
        Messages("summaryQuestion.isCompanyKnowledgeIntensiveModel")
      companyDetailsTableTBody.select("tr").get(8).getElementById("isCompanyKnowledgeIntensive-answer").text() shouldBe
        isKnowledgeIntensiveModelYes.isKnowledgeIntensive
      companyDetailsTableTBody.select("tr").get(8).getElementById("isCompanyKnowledgeIntensive-link")
        .attr("href") shouldEqual controllers.eis.routes.IsCompanyKnowledgeIntensiveController.show().url

      //Is Knowledge Intensive
      companyDetailsTableTBody.select("tr").get(9).getElementById("knowledgeIntensive-question").text() shouldBe
        Messages("summaryQuestion.knowledgeIntensive")
      companyDetailsTableTBody.select("tr").get(9).getElementById("knowledgeIntensive-answer").text() shouldBe
        isKnowledgeIntensiveModelYes.isKnowledgeIntensive
      companyDetailsTableTBody.select("tr").get(9).getElementById("knowledgeIntensive-link")
        .attr("href") shouldEqual controllers.eis.routes.IsKnowledgeIntensiveController.show().url


      //Operating costs
      companyDetailsTableTBody.select("tr").get(10).getElementById("operatingCosts-Question0").text() shouldBe
        Messages("summaryQuestion.operatingCosts")

      companyDetailsTableTBody.select("tr").get(10).getElementById("operatingCosts-Question3").text() shouldBe
        Messages("summaryQuestion.rdCosts")

      companyDetailsTableTBody.select("tr").get(10).getElementById("operatingCosts-Answer0").text() shouldBe
        OperatingCostsModel.getOperatingAndRDCostsAsFormattedString(operatingCostsModel.operatingCosts1stYear, Messages("page.companyDetails.OperatingCosts.row.heading.one"))

      companyDetailsTableTBody.select("tr").get(10).getElementById("operatingCosts-Answer1").text() shouldBe
        OperatingCostsModel.getOperatingAndRDCostsAsFormattedString(operatingCostsModel.operatingCosts2ndYear, Messages("page.companyDetails.OperatingCosts.row.heading.two"))

      companyDetailsTableTBody.select("tr").get(10).getElementById("operatingCosts-Answer2").text() shouldBe
        OperatingCostsModel.getOperatingAndRDCostsAsFormattedString(operatingCostsModel.operatingCosts3rdYear, Messages("page.companyDetails.OperatingCosts.row.heading.three"))

      companyDetailsTableTBody.select("tr").get(10).getElementById("operatingCosts-link")
        .attr("href") shouldEqual controllers.eis.routes.OperatingCostsController.show().url

      companyDetailsTableTBody.select("tr").get(10).getElementById("operatingCosts-Answer3").text() shouldBe
        OperatingCostsModel.getOperatingAndRDCostsAsFormattedString(operatingCostsModel.rAndDCosts1stYear, Messages("page.companyDetails.OperatingCosts.row.heading.one"))

      companyDetailsTableTBody.select("tr").get(10).getElementById("operatingCosts-Answer4").text() shouldBe
        OperatingCostsModel.getOperatingAndRDCostsAsFormattedString(operatingCostsModel.rAndDCosts2ndYear, Messages("page.companyDetails.OperatingCosts.row.heading.two"))

      companyDetailsTableTBody.select("tr").get(10).getElementById("operatingCosts-Answer5").text() shouldBe
        OperatingCostsModel.getOperatingAndRDCostsAsFormattedString(operatingCostsModel.rAndDCosts3rdYear, Messages("page.companyDetails.OperatingCosts.row.heading.three"))


      //Percentage of staff with masters
      companyDetailsTableTBody.select("tr").get(11).getElementById("percentageStaffWithMasters-question").text() shouldBe
        Messages("summaryQuestion.percentageStaffWithMasters")
      companyDetailsTableTBody.select("tr").get(11).getElementById("percentageStaffWithMasters-answer").text() shouldBe
        PercentageStaffWithMastersModel.staffWithMastersToString(percentageStaffWithMastersModelYes.staffWithMasters)
      companyDetailsTableTBody.select("tr").get(11).getElementById("percentageStaffWithMasters-link")
        .attr("href") shouldEqual controllers.eis.routes.PercentageStaffWithMastersController.show().url

      //Fulltime employees
      companyDetailsTableTBody.select("tr").get(12).getElementById("fullTimeEmployees-question").text() shouldBe
        Messages("summaryQuestion.fullTimeEmployees")
      companyDetailsTableTBody.select("tr").get(12).getElementById("fullTimeEmployees-answer").text() shouldBe
        fullTimeEmployeeModel.employeeCount.toString()
      companyDetailsTableTBody.select("tr").get(12).getElementById("fullTimeEmployees-link")
        .attr("href") shouldEqual controllers.eis.routes.FullTimeEmployeeCountController.show().url

      document.getElementById("submit").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.button.confirm")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.SupportingDocumentsUploadController.show().url
    }
  }


  "The Check Answers page" should {

    "Verify that the Check Answers page contains the correct elements for Section 1: Company details" +
      " when it is fully populated with company detail models but a commercial sale has not been made" in new Setup {
      val model = CheckAnswersModel(Some(registeredAddressModel), Some(dateOfIncorporationModel), Some(natureOfBusinessModel),
        Some(commercialSaleModelNo), Some(isCompanyKnowledgeIntensiveModelYes), Some(isKnowledgeIntensiveModelYes),
        Some(operatingCostsModel), Some(percentageStaffWithMastersModelYes), Some(tenYearPlanModelYes), Some(hadPreviousRFIModelYes),
        Vector(), Some(totalAmountRaisedValid), Some(thirtyDayRuleModelYes), Some(anySharesRepaymentModelYes), Some(newGeographicalMarketModelYes),
        Some(newProductMarketModelYes), Some(contactDetailsModel), Some(addressModel), Some(investmentGrowModel), Some(qualifyTrade),
        Some(hasInvestmentTradeStartedModelYes), Some(shareIssuetDateModel), Some(grossAssetsModel), Some(FullTimeEmployeeCountModel(22)),
        Some(shareDescriptionModel), Some(numberOfSharesModel), Some(listOfInvestorsWithShareHoldings), Some(WasAnyValueReceivedModel(Constants.StandardRadioButtonYesValue,
          Some("text"))), Some(ShareCapitalChangesModel(Constants.StandardRadioButtonYesValue, Some("test"))), Some(MarketDescriptionModel("test")),
        Some(validSharesRepaymentDetailsVector), Some(grossAssetsAfterIssueModel),
        Some(turnoverCostsValid), Some(researchStartDateModelYes))

      val page = CheckAnswers(model)(authorisedFakeRequest, applicationMessages)
      val document = Jsoup.parse(page.body)
      lazy val companyDetailsTableTBody = document.getElementById("company-details-table").select("tbody")
      lazy val notAvailableMessage = Messages("common.notAvailable")

      document.getElementById("main-heading").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.heading")
      document.getElementById("description-one").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.description.one") +" " +
        Messages("page.checkAndSubmit.checkAnswers.scheme.eis")
      document.getElementById("description-two").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.description.two")

      //Section 1 table heading
      document.getElementById("companyDetailsSection-table-heading").text() shouldBe Messages("summaryQuestion.companyDetailsSection")
      //Nature of business
      companyDetailsTableTBody.select("tr").get(0).getElementById("natureOfBusiness-question").text() shouldBe
        Messages("summaryQuestion.natureOfBusiness")
      companyDetailsTableTBody.select("tr").get(0).getElementById("natureOfBusiness-answer").text() shouldBe
        natureOfBusinessModel.natureofbusiness
      companyDetailsTableTBody.select("tr").get(0).getElementById("natureOfBusiness-link")
        .attr("href") shouldEqual controllers.eis.routes.NatureOfBusinessController.show().url
      //Date of incorporation
      companyDetailsTableTBody.select("tr").get(1).getElementById("dateOfIncorporation-question").text() shouldBe
        Messages("summaryQuestion.dateOfIncorporation")
      companyDetailsTableTBody.select("tr").get(1).getElementById("dateOfIncorporation-answer").text() shouldBe
        DateOfIncorporationModel.toDateString(dateOfIncorporationModel.day.get,dateOfIncorporationModel.month.get,dateOfIncorporationModel.year.get)
      companyDetailsTableTBody.select("tr").get(1).getElementById("dateOfIncorporation-link")
        .attr("href") shouldEqual controllers.eis.routes.DateOfIncorporationController.show().url
      //  qualifying business
      companyDetailsTableTBody.select("tr").get(2).getElementById("qualifyBusinessActivity-question").text() shouldBe
        Messages("summaryQuestion.bussinessPurpose")
      companyDetailsTableTBody.select("tr").get(2).getElementById("qualifyBusinessActivity-answer").text() shouldBe
        Messages("page.companyDetails.qualifyBusinessActivity.bullet.one")
      companyDetailsTableTBody.select("tr").get(2).getElementById("qualifyBusinessActivity-link")
        .attr("href") shouldEqual controllers.eis.routes.QualifyBusinessActivityController.show().url

      //Date of first commercial sale
      companyDetailsTableTBody.select("tr").get(4).getElementById("commercialSale-question").text() shouldBe
        Messages("summaryQuestion.hasCommercialSale")
      companyDetailsTableTBody.select("tr").get(4).getElementById("commercialSale-answer").text() shouldBe
        commercialSaleModelNo.hasCommercialSale
      companyDetailsTableTBody.select("tr").get(4).getElementById("commercialSale-link")
        .attr("href") shouldEqual controllers.eis.routes.CommercialSaleController.show().url

      //shareIssueDate
      companyDetailsTableTBody.select("tr").get(5).getElementById("shareIssueDate-question").text() shouldBe
        Messages("summaryQuestion.shareIssueDate")
      companyDetailsTableTBody.select("tr").get(5).getElementById("shareIssueDate-answer").text() shouldBe
        ShareIssueDateModel.toDateString(shareIssuetDateModel.day.get, shareIssuetDateModel.month.get, shareIssuetDateModel.year.get)
      companyDetailsTableTBody.select("tr").get(5).getElementById("shareIssueDate-link")
        .attr("href") shouldEqual controllers.eis.routes.ShareIssueDateController.show().url

      //Gross assets
      companyDetailsTableTBody.select("tr").get(6).getElementById("grossAssets-question").text() shouldBe
        Messages("summaryQuestion.businessGrossAssets")
      companyDetailsTableTBody.select("tr").get(6).getElementById("grossAssets-answer").text() shouldBe
        GrossAssetsModel.getAmountAsFormattedString(grossAssetsModel.grossAmount)
      companyDetailsTableTBody.select("tr").get(6).getElementById("grossAssets-link")
        .attr("href") shouldEqual controllers.eis.routes.GrossAssetsController.show().url

      //Gross assets
      companyDetailsTableTBody.select("tr").get(7).getElementById("grossAssetsAfterIssue-question").text() shouldBe
        Messages("summaryQuestion.businessGrossAssetsAfterIssue")
      companyDetailsTableTBody.select("tr").get(7).getElementById("grossAssetsAfterIssue-answer").text() shouldBe
        GrossAssetsModel.getAmountAsFormattedString(grossAssetsAfterIssueModel.grossAmount)
      companyDetailsTableTBody.select("tr").get(7).getElementById("grossAssetsAfterIssue-link")
        .attr("href") shouldEqual controllers.eis.routes.GrossAssetsAfterIssueController.show().url

      //Is Company Knowledge Intensive
      companyDetailsTableTBody.select("tr").get(8).getElementById("isCompanyKnowledgeIntensive-question").text() shouldBe
        Messages("summaryQuestion.isCompanyKnowledgeIntensiveModel")
      companyDetailsTableTBody.select("tr").get(8).getElementById("isCompanyKnowledgeIntensive-answer").text() shouldBe
        isKnowledgeIntensiveModelYes.isKnowledgeIntensive
      companyDetailsTableTBody.select("tr").get(8).getElementById("isCompanyKnowledgeIntensive-link")
        .attr("href") shouldEqual controllers.eis.routes.IsCompanyKnowledgeIntensiveController.show().url

      //Is Knowledge Intensive
      companyDetailsTableTBody.select("tr").get(9).getElementById("knowledgeIntensive-question").text() shouldBe
        Messages("summaryQuestion.knowledgeIntensive")
      companyDetailsTableTBody.select("tr").get(9).getElementById("knowledgeIntensive-answer").text() shouldBe
        isKnowledgeIntensiveModelYes.isKnowledgeIntensive
      companyDetailsTableTBody.select("tr").get(9).getElementById("knowledgeIntensive-link")
        .attr("href") shouldEqual controllers.eis.routes.IsKnowledgeIntensiveController.show().url


      //Operating costs
      companyDetailsTableTBody.select("tr").get(10).getElementById("operatingCosts-Question0").text() shouldBe
        Messages("summaryQuestion.operatingCosts")

      companyDetailsTableTBody.select("tr").get(10).getElementById("operatingCosts-Question3").text() shouldBe
        Messages("summaryQuestion.rdCosts")

      companyDetailsTableTBody.select("tr").get(10).getElementById("operatingCosts-Answer0").text() shouldBe
        OperatingCostsModel.getOperatingAndRDCostsAsFormattedString(operatingCostsModel.operatingCosts1stYear, Messages("page.companyDetails.OperatingCosts.row.heading.one"))

      companyDetailsTableTBody.select("tr").get(10).getElementById("operatingCosts-Answer1").text() shouldBe
        OperatingCostsModel.getOperatingAndRDCostsAsFormattedString(operatingCostsModel.operatingCosts2ndYear, Messages("page.companyDetails.OperatingCosts.row.heading.two"))

      companyDetailsTableTBody.select("tr").get(10).getElementById("operatingCosts-Answer2").text() shouldBe
        OperatingCostsModel.getOperatingAndRDCostsAsFormattedString(operatingCostsModel.operatingCosts3rdYear, Messages("page.companyDetails.OperatingCosts.row.heading.three"))

      companyDetailsTableTBody.select("tr").get(10).getElementById("operatingCosts-link")
        .attr("href") shouldEqual controllers.eis.routes.OperatingCostsController.show().url

      companyDetailsTableTBody.select("tr").get(10).getElementById("operatingCosts-Answer3").text() shouldBe
        OperatingCostsModel.getOperatingAndRDCostsAsFormattedString(operatingCostsModel.rAndDCosts1stYear, Messages("page.companyDetails.OperatingCosts.row.heading.one"))

      companyDetailsTableTBody.select("tr").get(10).getElementById("operatingCosts-Answer4").text() shouldBe
        OperatingCostsModel.getOperatingAndRDCostsAsFormattedString(operatingCostsModel.rAndDCosts2ndYear, Messages("page.companyDetails.OperatingCosts.row.heading.two"))

      companyDetailsTableTBody.select("tr").get(10).getElementById("operatingCosts-Answer5").text() shouldBe
        OperatingCostsModel.getOperatingAndRDCostsAsFormattedString(operatingCostsModel.rAndDCosts3rdYear, Messages("page.companyDetails.OperatingCosts.row.heading.three"))

      //Percentage of staff with masters
      companyDetailsTableTBody.select("tr").get(11).getElementById("percentageStaffWithMasters-question").text() shouldBe
        Messages("summaryQuestion.percentageStaffWithMasters")
      companyDetailsTableTBody.select("tr").get(11).getElementById("percentageStaffWithMasters-answer").text() shouldBe
        PercentageStaffWithMastersModel.staffWithMastersToString(percentageStaffWithMastersModelYes.staffWithMasters)
      companyDetailsTableTBody.select("tr").get(11).getElementById("percentageStaffWithMasters-link")
        .attr("href") shouldEqual controllers.eis.routes.PercentageStaffWithMastersController.show().url

      //Fulltime employees
      companyDetailsTableTBody.select("tr").get(12).getElementById("fullTimeEmployees-question").text() shouldBe
        Messages("summaryQuestion.fullTimeEmployees")
      companyDetailsTableTBody.select("tr").get(12).getElementById("fullTimeEmployees-answer").text() shouldBe
        fullTimeEmployeeModel.employeeCount.toString()
      companyDetailsTableTBody.select("tr").get(12).getElementById("fullTimeEmployees-link")
        .attr("href") shouldEqual controllers.eis.routes.FullTimeEmployeeCountController.show().url

      document.getElementById("submit").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.button.confirm")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.SupportingDocumentsUploadController.show().url
    }
  }

  "The Check Answers page" should {

    "Verify that the Check Answers page contains the correct elements for Section 1: Company details" +
      " when it is fully populated with company detail models but it is not knowledge intensive and therefore should not show KI pages" in new Setup {
      val model = CheckAnswersModel(Some(registeredAddressModel), Some(dateOfIncorporationModel), Some(natureOfBusinessModel),
        Some(commercialSaleModelYes), Some(isCompanyKnowledgeIntensiveModelNo), Some(isKnowledgeIntensiveModelNo),
        Some(operatingCostsModel), Some(percentageStaffWithMastersModelYes), Some(tenYearPlanModelYes), Some(hadPreviousRFIModelYes),
        Vector(), Some(totalAmountRaisedValid), Some(thirtyDayRuleModelYes), Some(anySharesRepaymentModelYes), Some(newGeographicalMarketModelYes),
        Some(newProductMarketModelYes), Some(contactDetailsModel), Some(addressModel), Some(investmentGrowModel), Some(qualifyTrade),
        Some(hasInvestmentTradeStartedModelYes), Some(shareIssuetDateModel), Some(grossAssetsModel), Some(FullTimeEmployeeCountModel(22)),
        Some(shareDescriptionModel), Some(numberOfSharesModel), Some(listOfInvestorsWithShareHoldings), Some(WasAnyValueReceivedModel(Constants.StandardRadioButtonYesValue,
          Some("text"))), Some(ShareCapitalChangesModel(Constants.StandardRadioButtonYesValue, Some("test"))), Some(MarketDescriptionModel("test")),
        Some(validSharesRepaymentDetailsVector), Some(grossAssetsAfterIssueModel),
        Some(turnoverCostsValid), Some(researchStartDateModelYes))

      val page = CheckAnswers(model)(authorisedFakeRequest, applicationMessages)
      val document = Jsoup.parse(page.body)

      lazy val companyDetailsTableTBody = document.getElementById("company-details-table").select("tbody")
      lazy val notAvailableMessage = Messages("common.notAvailable")

      document.getElementById("main-heading").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.heading")
      document.getElementById("description-one").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.description.one") + " " +
        Messages("page.checkAndSubmit.checkAnswers.scheme.eis")
      document.getElementById("description-two").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.description.two")

      //Section 1 table heading
      document.getElementById("companyDetailsSection-table-heading").text() shouldBe Messages("summaryQuestion.companyDetailsSection")
      //Nature of business
      companyDetailsTableTBody.select("tr").get(0).getElementById("natureOfBusiness-question").text() shouldBe
        Messages("summaryQuestion.natureOfBusiness")
      companyDetailsTableTBody.select("tr").get(0).getElementById("natureOfBusiness-answer").text() shouldBe
        natureOfBusinessModel.natureofbusiness
      companyDetailsTableTBody.select("tr").get(0).getElementById("natureOfBusiness-link")
        .attr("href") shouldEqual controllers.eis.routes.NatureOfBusinessController.show().url
      //Date of incorporation
      companyDetailsTableTBody.select("tr").get(1).getElementById("dateOfIncorporation-question").text() shouldBe
        Messages("summaryQuestion.dateOfIncorporation")
      companyDetailsTableTBody.select("tr").get(1).getElementById("dateOfIncorporation-answer").text() shouldBe
        DateOfIncorporationModel.toDateString(dateOfIncorporationModel.day.get, dateOfIncorporationModel.month.get, dateOfIncorporationModel.year.get)
      companyDetailsTableTBody.select("tr").get(1).getElementById("dateOfIncorporation-link")
        .attr("href") shouldEqual controllers.eis.routes.DateOfIncorporationController.show().url
      //  qualifying business
      companyDetailsTableTBody.select("tr").get(2).getElementById("qualifyBusinessActivity-question").text() shouldBe
        Messages("summaryQuestion.bussinessPurpose")
      companyDetailsTableTBody.select("tr").get(2).getElementById("qualifyBusinessActivity-answer").text() shouldBe
        Messages("page.companyDetails.qualifyBusinessActivity.bullet.one")
      companyDetailsTableTBody.select("tr").get(2).getElementById("qualifyBusinessActivity-link")
        .attr("href") shouldEqual controllers.eis.routes.QualifyBusinessActivityController.show().url
      //Date of first commercial sale
      companyDetailsTableTBody.select("tr").get(4).getElementById("commercialSale-Question0").text() shouldBe
        Messages("summaryQuestion.hasCommercialSale")
      companyDetailsTableTBody.select("tr").get(4).getElementById("commercialSale-Answer0").text() shouldBe
        commercialSaleModelYes.hasCommercialSale
      companyDetailsTableTBody.select("tr").get(4).getElementById("commercialSale-Question2").text() shouldBe
        Messages("summaryQuestion.commercialSaleDate")
      companyDetailsTableTBody.select("tr").get(4).getElementById("commercialSale-Answer2").text() shouldBe
        CommercialSaleModel.toDateString(commercialSaleModelYes.commercialSaleDay.get, commercialSaleModelYes.commercialSaleMonth.get, commercialSaleModelYes.commercialSaleYear.get)
      companyDetailsTableTBody.select("tr").get(4).getElementById("commercialSale-link")
        .attr("href") shouldEqual controllers.eis.routes.CommercialSaleController.show().url

      //shareIssueDate
      companyDetailsTableTBody.select("tr").get(5).getElementById("shareIssueDate-question").text() shouldBe
        Messages("summaryQuestion.shareIssueDate")
      companyDetailsTableTBody.select("tr").get(5).getElementById("shareIssueDate-answer").text() shouldBe
        ShareIssueDateModel.toDateString(shareIssuetDateModel.day.get, shareIssuetDateModel.month.get, shareIssuetDateModel.year.get)
      companyDetailsTableTBody.select("tr").get(5).getElementById("shareIssueDate-link")
        .attr("href") shouldEqual controllers.eis.routes.ShareIssueDateController.show().url

      //Gross assets
      companyDetailsTableTBody.select("tr").get(6).getElementById("grossAssets-question").text() shouldBe
        Messages("summaryQuestion.businessGrossAssets")
      companyDetailsTableTBody.select("tr").get(6).getElementById("grossAssets-answer").text() shouldBe
        GrossAssetsModel.getAmountAsFormattedString(grossAssetsModel.grossAmount)
      companyDetailsTableTBody.select("tr").get(6).getElementById("grossAssets-link")
        .attr("href") shouldEqual controllers.eis.routes.GrossAssetsController.show().url

      //Gross assets
      companyDetailsTableTBody.select("tr").get(7).getElementById("grossAssetsAfterIssue-question").text() shouldBe
        Messages("summaryQuestion.businessGrossAssetsAfterIssue")
      companyDetailsTableTBody.select("tr").get(7).getElementById("grossAssetsAfterIssue-answer").text() shouldBe
        GrossAssetsModel.getAmountAsFormattedString(grossAssetsAfterIssueModel.grossAmount)
      companyDetailsTableTBody.select("tr").get(7).getElementById("grossAssetsAfterIssue-link")
        .attr("href") shouldEqual controllers.eis.routes.GrossAssetsAfterIssueController.show().url

      //Is Company Knowledge Intensive
      companyDetailsTableTBody.select("tr").get(8).getElementById("isCompanyKnowledgeIntensive-question").text() shouldBe
        Messages("summaryQuestion.isCompanyKnowledgeIntensiveModel")
      companyDetailsTableTBody.select("tr").get(8).getElementById("isCompanyKnowledgeIntensive-answer").text() shouldBe
        isKnowledgeIntensiveModelNo.isKnowledgeIntensive
      companyDetailsTableTBody.select("tr").get(8).getElementById("isCompanyKnowledgeIntensive-link")
        .attr("href") shouldEqual controllers.eis.routes.IsCompanyKnowledgeIntensiveController.show().url

      //Fulltime employees
      companyDetailsTableTBody.select("tr").get(9).getElementById("fullTimeEmployees-question").text() shouldBe
        Messages("summaryQuestion.fullTimeEmployees")
      companyDetailsTableTBody.select("tr").get(9).getElementById("fullTimeEmployees-answer").text() shouldBe
        fullTimeEmployeeModel.employeeCount.toString()
      companyDetailsTableTBody.select("tr").get(9).getElementById("fullTimeEmployees-link")
        .attr("href") shouldEqual controllers.eis.routes.FullTimeEmployeeCountController.show().url
      document.getElementById("submit").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.button.confirm")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.SupportingDocumentsUploadController.show().url
    }
  }

  "The Check Answers page" should {

    "Verify that the Check Answers page contains the correct elements for Section 1: Company details" +
      " when it is fully populated with company detail models but it does not have a ten year plan and" +
      " so should not have a ten year description row" in new Setup {
      val model = CheckAnswersModel(Some(registeredAddressModel), Some(dateOfIncorporationModel), Some(natureOfBusinessModel),
        Some(commercialSaleModelYes), Some(isCompanyKnowledgeIntensiveModelYes), Some(isKnowledgeIntensiveModelYes),
        Some(operatingCostsModel), Some(percentageStaffWithMastersModelYes), None, Some(hadPreviousRFIModelYes),
        Vector(), Some(totalAmountRaisedValid), Some(thirtyDayRuleModelYes), Some(anySharesRepaymentModelYes), Some(newGeographicalMarketModelYes),
        Some(newProductMarketModelYes), Some(contactDetailsModel), Some(addressModel), Some(investmentGrowModel), Some(qualifyResearchAndDevelopment),
        Some(hasInvestmentTradeStartedModelYes), Some(shareIssuetDateModel), Some(grossAssetsModel), Some(fullTimeEmployeeModel),
        Some(shareDescriptionModel), Some(numberOfSharesModel), Some(listOfInvestorsWithShareHoldings),
        Some(WasAnyValueReceivedModel(Constants.StandardRadioButtonYesValue, Some("text"))),
        Some(ShareCapitalChangesModel(Constants.StandardRadioButtonYesValue, Some("test"))), Some(MarketDescriptionModel("test")),
        Some(validSharesRepaymentDetailsVector), Some(grossAssetsAfterIssueModel),
        Some(turnoverCostsValid), Some(researchStartDateModelYes))

      val page = CheckAnswers(model)(authorisedFakeRequest, applicationMessages)
      val document = Jsoup.parse(page.body)

      lazy val companyDetailsTableTBody = document.getElementById("company-details-table").select("tbody")
      lazy val notAvailableMessage = Messages("common.notAvailable")

      document.getElementById("main-heading").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.heading")
      document.getElementById("description-one").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.description.one") +" " +
        Messages("page.checkAndSubmit.checkAnswers.scheme.eis")
      document.getElementById("description-two").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.description.two")

      //Section 1 table heading
      document.getElementById("companyDetailsSection-table-heading").text() shouldBe Messages("summaryQuestion.companyDetailsSection")
      //Nature of business
      companyDetailsTableTBody.select("tr").get(0).getElementById("natureOfBusiness-question").text() shouldBe
        Messages("summaryQuestion.natureOfBusiness")
      companyDetailsTableTBody.select("tr").get(0).getElementById("natureOfBusiness-answer").text() shouldBe
        natureOfBusinessModel.natureofbusiness
      companyDetailsTableTBody.select("tr").get(0).getElementById("natureOfBusiness-link")
        .attr("href") shouldEqual controllers.eis.routes.NatureOfBusinessController.show().url
      //Date of incorporation
      companyDetailsTableTBody.select("tr").get(1).getElementById("dateOfIncorporation-question").text() shouldBe
        Messages("summaryQuestion.dateOfIncorporation")
      companyDetailsTableTBody.select("tr").get(1).getElementById("dateOfIncorporation-answer").text() shouldBe
        DateOfIncorporationModel.toDateString(dateOfIncorporationModel.day.get,dateOfIncorporationModel.month.get,dateOfIncorporationModel.year.get)
      companyDetailsTableTBody.select("tr").get(1).getElementById("dateOfIncorporation-link")
        .attr("href") shouldEqual controllers.eis.routes.DateOfIncorporationController.show().url
      //  qualifying business
      companyDetailsTableTBody.select("tr").get(2).getElementById("qualifyBusinessActivity-question").text() shouldBe
        Messages("summaryQuestion.bussinessPurpose")
      companyDetailsTableTBody.select("tr").get(2).getElementById("qualifyBusinessActivity-answer").text() shouldBe
        Messages("page.companyDetails.qualifyBusinessActivity.bullet.two")
      companyDetailsTableTBody.select("tr").get(2).getElementById("qualifyBusinessActivity-link")
        .attr("href") shouldEqual controllers.eis.routes.QualifyBusinessActivityController.show().url
      //Date of first commercial sale
      companyDetailsTableTBody.select("tr").get(4).getElementById("commercialSale-Question0").text() shouldBe
        Messages("summaryQuestion.hasCommercialSale")
      companyDetailsTableTBody.select("tr").get(4).getElementById("commercialSale-Answer0").text() shouldBe
        commercialSaleModelYes.hasCommercialSale
      companyDetailsTableTBody.select("tr").get(4).getElementById("commercialSale-Question2").text() shouldBe
        Messages("summaryQuestion.commercialSaleDate")
      companyDetailsTableTBody.select("tr").get(4).getElementById("commercialSale-Answer2").text() shouldBe
        CommercialSaleModel.toDateString(commercialSaleModelYes.commercialSaleDay.get,commercialSaleModelYes.commercialSaleMonth.get,commercialSaleModelYes.commercialSaleYear.get)
      companyDetailsTableTBody.select("tr").get(4).getElementById("commercialSale-link")
        .attr("href") shouldEqual controllers.eis.routes.CommercialSaleController.show().url

      //shareIssueDate
      companyDetailsTableTBody.select("tr").get(5).getElementById("shareIssueDate-question").text() shouldBe
        Messages("summaryQuestion.shareIssueDate")
      companyDetailsTableTBody.select("tr").get(5).getElementById("shareIssueDate-answer").text() shouldBe
        ShareIssueDateModel.toDateString(shareIssuetDateModel.day.get, shareIssuetDateModel.month.get, shareIssuetDateModel.year.get)
      companyDetailsTableTBody.select("tr").get(5).getElementById("shareIssueDate-link")
        .attr("href") shouldEqual controllers.eis.routes.ShareIssueDateController.show().url

      //Gross assets
      companyDetailsTableTBody.select("tr").get(6).getElementById("grossAssets-question").text() shouldBe
        Messages("summaryQuestion.businessGrossAssets")
      companyDetailsTableTBody.select("tr").get(6).getElementById("grossAssets-answer").text() shouldBe
        GrossAssetsModel.getAmountAsFormattedString(grossAssetsModel.grossAmount)
      companyDetailsTableTBody.select("tr").get(6).getElementById("grossAssets-link")
        .attr("href") shouldEqual controllers.eis.routes.GrossAssetsController.show().url

      //Gross assets
      companyDetailsTableTBody.select("tr").get(7).getElementById("grossAssetsAfterIssue-question").text() shouldBe
        Messages("summaryQuestion.businessGrossAssetsAfterIssue")
      companyDetailsTableTBody.select("tr").get(7).getElementById("grossAssetsAfterIssue-answer").text() shouldBe
        GrossAssetsModel.getAmountAsFormattedString(grossAssetsAfterIssueModel.grossAmount)
      companyDetailsTableTBody.select("tr").get(7).getElementById("grossAssetsAfterIssue-link")
        .attr("href") shouldEqual controllers.eis.routes.GrossAssetsAfterIssueController.show().url

      //Is Company Knowledge Intensive
      companyDetailsTableTBody.select("tr").get(8).getElementById("isCompanyKnowledgeIntensive-question").text() shouldBe
        Messages("summaryQuestion.isCompanyKnowledgeIntensiveModel")
      companyDetailsTableTBody.select("tr").get(8).getElementById("isCompanyKnowledgeIntensive-answer").text() shouldBe
        isKnowledgeIntensiveModelYes.isKnowledgeIntensive
      companyDetailsTableTBody.select("tr").get(8).getElementById("isCompanyKnowledgeIntensive-link")
        .attr("href") shouldEqual controllers.eis.routes.IsCompanyKnowledgeIntensiveController.show().url

      //Is Knowledge Intensive
      companyDetailsTableTBody.select("tr").get(9).getElementById("knowledgeIntensive-question").text() shouldBe
        Messages("summaryQuestion.knowledgeIntensive")
      companyDetailsTableTBody.select("tr").get(9).getElementById("knowledgeIntensive-answer").text() shouldBe
        isKnowledgeIntensiveModelYes.isKnowledgeIntensive
      companyDetailsTableTBody.select("tr").get(9).getElementById("knowledgeIntensive-link")
        .attr("href") shouldEqual controllers.eis.routes.IsKnowledgeIntensiveController.show().url


      //Operating costs
      companyDetailsTableTBody.select("tr").get(10).getElementById("operatingCosts-Question0").text() shouldBe
        Messages("summaryQuestion.operatingCosts")

      companyDetailsTableTBody.select("tr").get(10).getElementById("operatingCosts-Question3").text() shouldBe
        Messages("summaryQuestion.rdCosts")

      companyDetailsTableTBody.select("tr").get(10).getElementById("operatingCosts-Answer0").text() shouldBe
        OperatingCostsModel.getOperatingAndRDCostsAsFormattedString(operatingCostsModel.operatingCosts1stYear, Messages("page.companyDetails.OperatingCosts.row.heading.one"))

      companyDetailsTableTBody.select("tr").get(10).getElementById("operatingCosts-Answer1").text() shouldBe
        OperatingCostsModel.getOperatingAndRDCostsAsFormattedString(operatingCostsModel.operatingCosts2ndYear, Messages("page.companyDetails.OperatingCosts.row.heading.two"))

      companyDetailsTableTBody.select("tr").get(10).getElementById("operatingCosts-Answer2").text() shouldBe
        OperatingCostsModel.getOperatingAndRDCostsAsFormattedString(operatingCostsModel.operatingCosts3rdYear, Messages("page.companyDetails.OperatingCosts.row.heading.three"))

      companyDetailsTableTBody.select("tr").get(10).getElementById("operatingCosts-link")
        .attr("href") shouldEqual controllers.eis.routes.OperatingCostsController.show().url

      companyDetailsTableTBody.select("tr").get(10).getElementById("operatingCosts-Answer3").text() shouldBe
        OperatingCostsModel.getOperatingAndRDCostsAsFormattedString(operatingCostsModel.rAndDCosts1stYear, Messages("page.companyDetails.OperatingCosts.row.heading.one"))

      companyDetailsTableTBody.select("tr").get(10).getElementById("operatingCosts-Answer4").text() shouldBe
        OperatingCostsModel.getOperatingAndRDCostsAsFormattedString(operatingCostsModel.rAndDCosts2ndYear, Messages("page.companyDetails.OperatingCosts.row.heading.two"))

      companyDetailsTableTBody.select("tr").get(10).getElementById("operatingCosts-Answer5").text() shouldBe
        OperatingCostsModel.getOperatingAndRDCostsAsFormattedString(operatingCostsModel.rAndDCosts3rdYear, Messages("page.companyDetails.OperatingCosts.row.heading.three"))

      //Percentage of staff with masters
      companyDetailsTableTBody.select("tr").get(11).getElementById("percentageStaffWithMasters-question").text() shouldBe
        Messages("summaryQuestion.percentageStaffWithMasters")
      companyDetailsTableTBody.select("tr").get(11).getElementById("percentageStaffWithMasters-answer").text() shouldBe
        PercentageStaffWithMastersModel.staffWithMastersToString(percentageStaffWithMastersModelYes.staffWithMasters)
      companyDetailsTableTBody.select("tr").get(11).getElementById("percentageStaffWithMasters-link")
        .attr("href") shouldEqual controllers.eis.routes.PercentageStaffWithMastersController.show().url

      //Fulltime employees
      companyDetailsTableTBody.select("tr").get(12).getElementById("fullTimeEmployees-question").text() shouldBe
        Messages("summaryQuestion.fullTimeEmployees")
      companyDetailsTableTBody.select("tr").get(12).getElementById("fullTimeEmployees-answer").text() shouldBe
        fullTimeEmployeeModel.employeeCount.toString()
      companyDetailsTableTBody.select("tr").get(12).getElementById("fullTimeEmployees-link")
        .attr("href") shouldEqual controllers.eis.routes.FullTimeEmployeeCountController.show().url

      document.getElementById("submit").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.button.confirm")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.SupportingDocumentsUploadController.show().url
    }
  }

  "The Check Answers page" should {

    "Verify that the Check Answers page contains the correct elements for Section 1: Company details" +
      " when it is fully populated with company detail models with percentageStaffWithMasters less than 20% and a ten year plan and" +
      " so should not have a ten year description row" in new Setup {
      val model = CheckAnswersModel(Some(registeredAddressModel), Some(dateOfIncorporationModel), Some(natureOfBusinessModel),
        Some(commercialSaleModelYes), Some(isCompanyKnowledgeIntensiveModelYes), Some(isKnowledgeIntensiveModelYes),
        Some(operatingCostsModel), Some(percentageStaffWithMastersModelNo), Some(tenYearPlanModelYes), Some(hadPreviousRFIModelYes),
        Vector(), Some(totalAmountRaisedValid), Some(thirtyDayRuleModelYes), Some(anySharesRepaymentModelYes), Some(newGeographicalMarketModelYes),
        Some(newProductMarketModelYes), Some(contactDetailsModel), Some(addressModel), Some(investmentGrowModel), Some(qualifyTrade),
        Some(hasInvestmentTradeStartedModelYes), Some(shareIssuetDateModel), Some(grossAssetsModel), Some(fullTimeEmployeeModel),
        Some(shareDescriptionModel), Some(numberOfSharesModel), Some(listOfInvestorsWithShareHoldings),
        Some(WasAnyValueReceivedModel(Constants.StandardRadioButtonYesValue, Some("text"))),
        Some(ShareCapitalChangesModel(Constants.StandardRadioButtonYesValue, Some("test"))), Some(MarketDescriptionModel("test")),
        Some(validSharesRepaymentDetailsVector), Some(grossAssetsAfterIssueModel),
        Some(turnoverCostsValid), Some(researchStartDateModelYes))

      val page = CheckAnswers(model)(authorisedFakeRequest, applicationMessages)
      val document = Jsoup.parse(page.body)

      lazy val companyDetailsTableTBody = document.getElementById("company-details-table").select("tbody")
      lazy val notAvailableMessage = Messages("common.notAvailable")

      document.getElementById("main-heading").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.heading")
      document.getElementById("description-one").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.description.one") +" " +
        Messages("page.checkAndSubmit.checkAnswers.scheme.eis")
      document.getElementById("description-two").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.description.two")

      //Section 1 table heading
      document.getElementById("companyDetailsSection-table-heading").text() shouldBe Messages("summaryQuestion.companyDetailsSection")
      //Nature of business
      companyDetailsTableTBody.select("tr").get(0).getElementById("natureOfBusiness-question").text() shouldBe
        Messages("summaryQuestion.natureOfBusiness")
      companyDetailsTableTBody.select("tr").get(0).getElementById("natureOfBusiness-answer").text() shouldBe
        natureOfBusinessModel.natureofbusiness
      companyDetailsTableTBody.select("tr").get(0).getElementById("natureOfBusiness-link")
        .attr("href") shouldEqual controllers.eis.routes.NatureOfBusinessController.show().url
      //Date of incorporation
      companyDetailsTableTBody.select("tr").get(1).getElementById("dateOfIncorporation-question").text() shouldBe
        Messages("summaryQuestion.dateOfIncorporation")
      companyDetailsTableTBody.select("tr").get(1).getElementById("dateOfIncorporation-answer").text() shouldBe
        DateOfIncorporationModel.toDateString(dateOfIncorporationModel.day.get,dateOfIncorporationModel.month.get,dateOfIncorporationModel.year.get)
      companyDetailsTableTBody.select("tr").get(1).getElementById("dateOfIncorporation-link")
        .attr("href") shouldEqual controllers.eis.routes.DateOfIncorporationController.show().url
      //  qualifying business
      companyDetailsTableTBody.select("tr").get(2).getElementById("qualifyBusinessActivity-question").text() shouldBe
        Messages("summaryQuestion.bussinessPurpose")
      companyDetailsTableTBody.select("tr").get(2).getElementById("qualifyBusinessActivity-answer").text() shouldBe
        Messages("page.companyDetails.qualifyBusinessActivity.bullet.one")
      companyDetailsTableTBody.select("tr").get(2).getElementById("qualifyBusinessActivity-link")
        .attr("href") shouldEqual controllers.eis.routes.QualifyBusinessActivityController.show().url
      //Date of first commercial sale
      companyDetailsTableTBody.select("tr").get(4).getElementById("commercialSale-Question0").text() shouldBe
        Messages("summaryQuestion.hasCommercialSale")
      companyDetailsTableTBody.select("tr").get(4).getElementById("commercialSale-Answer0").text() shouldBe
        commercialSaleModelYes.hasCommercialSale
      companyDetailsTableTBody.select("tr").get(4).getElementById("commercialSale-Question2").text() shouldBe
        Messages("summaryQuestion.commercialSaleDate")
      companyDetailsTableTBody.select("tr").get(4).getElementById("commercialSale-Answer2").text() shouldBe
        CommercialSaleModel.toDateString(commercialSaleModelYes.commercialSaleDay.get,commercialSaleModelYes.commercialSaleMonth.get,commercialSaleModelYes.commercialSaleYear.get)
      companyDetailsTableTBody.select("tr").get(4).getElementById("commercialSale-link")
        .attr("href") shouldEqual controllers.eis.routes.CommercialSaleController.show().url

      //shareIssueDate
      companyDetailsTableTBody.select("tr").get(5).getElementById("shareIssueDate-question").text() shouldBe
        Messages("summaryQuestion.shareIssueDate")
      companyDetailsTableTBody.select("tr").get(5).getElementById("shareIssueDate-answer").text() shouldBe
        ShareIssueDateModel.toDateString(shareIssuetDateModel.day.get, shareIssuetDateModel.month.get, shareIssuetDateModel.year.get)
      companyDetailsTableTBody.select("tr").get(5).getElementById("shareIssueDate-link")
        .attr("href") shouldEqual controllers.eis.routes.ShareIssueDateController.show().url

      //Gross assets
      companyDetailsTableTBody.select("tr").get(6).getElementById("grossAssets-question").text() shouldBe
        Messages("summaryQuestion.businessGrossAssets")
      companyDetailsTableTBody.select("tr").get(6).getElementById("grossAssets-answer").text() shouldBe
        GrossAssetsModel.getAmountAsFormattedString(grossAssetsModel.grossAmount)
      companyDetailsTableTBody.select("tr").get(6).getElementById("grossAssets-link")
        .attr("href") shouldEqual controllers.eis.routes.GrossAssetsController.show().url

      //Gross assets
      companyDetailsTableTBody.select("tr").get(7).getElementById("grossAssetsAfterIssue-question").text() shouldBe
        Messages("summaryQuestion.businessGrossAssetsAfterIssue")
      companyDetailsTableTBody.select("tr").get(7).getElementById("grossAssetsAfterIssue-answer").text() shouldBe
        GrossAssetsModel.getAmountAsFormattedString(grossAssetsAfterIssueModel.grossAmount)
      companyDetailsTableTBody.select("tr").get(7).getElementById("grossAssetsAfterIssue-link")
        .attr("href") shouldEqual controllers.eis.routes.GrossAssetsAfterIssueController.show().url

      //Is Company Knowledge Intensive
      companyDetailsTableTBody.select("tr").get(8).getElementById("isCompanyKnowledgeIntensive-question").text() shouldBe
        Messages("summaryQuestion.isCompanyKnowledgeIntensiveModel")
      companyDetailsTableTBody.select("tr").get(8).getElementById("isCompanyKnowledgeIntensive-answer").text() shouldBe
        isKnowledgeIntensiveModelYes.isKnowledgeIntensive
      companyDetailsTableTBody.select("tr").get(8).getElementById("isCompanyKnowledgeIntensive-link")
        .attr("href") shouldEqual controllers.eis.routes.IsCompanyKnowledgeIntensiveController.show().url

      //Is Knowledge Intensive
      companyDetailsTableTBody.select("tr").get(9).getElementById("knowledgeIntensive-question").text() shouldBe
        Messages("summaryQuestion.knowledgeIntensive")
      companyDetailsTableTBody.select("tr").get(9).getElementById("knowledgeIntensive-answer").text() shouldBe
        isKnowledgeIntensiveModelYes.isKnowledgeIntensive
      companyDetailsTableTBody.select("tr").get(9).getElementById("knowledgeIntensive-link")
        .attr("href") shouldEqual controllers.eis.routes.IsKnowledgeIntensiveController.show().url


      //Operating costs
      companyDetailsTableTBody.select("tr").get(10).getElementById("operatingCosts-Question0").text() shouldBe
        Messages("summaryQuestion.operatingCosts")

      companyDetailsTableTBody.select("tr").get(10).getElementById("operatingCosts-Question3").text() shouldBe
        Messages("summaryQuestion.rdCosts")

      companyDetailsTableTBody.select("tr").get(10).getElementById("operatingCosts-Answer0").text() shouldBe
        OperatingCostsModel.getOperatingAndRDCostsAsFormattedString(operatingCostsModel.operatingCosts1stYear, Messages("page.companyDetails.OperatingCosts.row.heading.one"))

      companyDetailsTableTBody.select("tr").get(10).getElementById("operatingCosts-Answer1").text() shouldBe
        OperatingCostsModel.getOperatingAndRDCostsAsFormattedString(operatingCostsModel.operatingCosts2ndYear, Messages("page.companyDetails.OperatingCosts.row.heading.two"))

      companyDetailsTableTBody.select("tr").get(10).getElementById("operatingCosts-Answer2").text() shouldBe
        OperatingCostsModel.getOperatingAndRDCostsAsFormattedString(operatingCostsModel.operatingCosts3rdYear, Messages("page.companyDetails.OperatingCosts.row.heading.three"))

      companyDetailsTableTBody.select("tr").get(10).getElementById("operatingCosts-link")
        .attr("href") shouldEqual controllers.eis.routes.OperatingCostsController.show().url

      companyDetailsTableTBody.select("tr").get(10).getElementById("operatingCosts-Answer3").text() shouldBe
        OperatingCostsModel.getOperatingAndRDCostsAsFormattedString(operatingCostsModel.rAndDCosts1stYear, Messages("page.companyDetails.OperatingCosts.row.heading.one"))

      companyDetailsTableTBody.select("tr").get(10).getElementById("operatingCosts-Answer4").text() shouldBe
        OperatingCostsModel.getOperatingAndRDCostsAsFormattedString(operatingCostsModel.rAndDCosts2ndYear, Messages("page.companyDetails.OperatingCosts.row.heading.two"))

      companyDetailsTableTBody.select("tr").get(10).getElementById("operatingCosts-Answer5").text() shouldBe
        OperatingCostsModel.getOperatingAndRDCostsAsFormattedString(operatingCostsModel.rAndDCosts3rdYear, Messages("page.companyDetails.OperatingCosts.row.heading.three"))

      //Percentage of staff with masters
      companyDetailsTableTBody.select("tr").get(11).getElementById("percentageStaffWithMasters-question").text() shouldBe
        Messages("summaryQuestion.percentageStaffWithMasters")
      companyDetailsTableTBody.select("tr").get(11).getElementById("percentageStaffWithMasters-answer").text() shouldBe
        PercentageStaffWithMastersModel.staffWithMastersToString(percentageStaffWithMastersModelNo.staffWithMasters)
      companyDetailsTableTBody.select("tr").get(11).getElementById("percentageStaffWithMasters-link")
        .attr("href") shouldEqual controllers.eis.routes.PercentageStaffWithMastersController.show().url

      //Ten year plan
      companyDetailsTableTBody.select("tr").get(12).getElementById("tenYearPlan-Question0").text() shouldBe
        Messages("summaryQuestion.developmentPlan")
      companyDetailsTableTBody.select("tr").get(12).getElementById("tenYearPlan-Answer0").text() shouldBe
        tenYearPlanModelYes.hasTenYearPlan
      companyDetailsTableTBody.select("tr").get(12).getElementById("tenYearPlan-Question1").text() shouldBe
        Messages("summaryQuestion.developmentPlanDesc")
      companyDetailsTableTBody.select("tr").get(12).getElementById("tenYearPlan-Answer1").text() shouldBe
        tenYearPlanModelYes.tenYearPlanDesc.get

      companyDetailsTableTBody.select("tr").get(12).getElementById("tenYearPlan-link")
        .attr("href") shouldEqual controllers.eis.routes.TenYearPlanController.show().url

      //Fulltime employees
      companyDetailsTableTBody.select("tr").get(13).getElementById("fullTimeEmployees-question").text() shouldBe
        Messages("summaryQuestion.fullTimeEmployees")
      companyDetailsTableTBody.select("tr").get(13).getElementById("fullTimeEmployees-answer").text() shouldBe
        fullTimeEmployeeModel.employeeCount.toString()
      companyDetailsTableTBody.select("tr").get(13).getElementById("fullTimeEmployees-link")
        .attr("href") shouldEqual controllers.eis.routes.FullTimeEmployeeCountController.show().url

      document.getElementById("submit").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.button.confirm")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.SupportingDocumentsUploadController.show().url
    }
  }
}
