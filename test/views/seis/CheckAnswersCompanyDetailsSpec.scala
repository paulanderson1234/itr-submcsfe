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

package views.seis

import models._
import models.seis.SEISCheckAnswersModel
import org.jsoup.Jsoup
import play.api.i18n.Messages
import views.helpers.CheckAnswersSpec
import play.api.i18n.Messages.Implicits._
import views.html.seis.checkAndSubmit.CheckAnswers

class CheckAnswersCompanyDetailsSpec extends CheckAnswersSpec {

  val grossAssets = Some(GrossAssetsModel(100000))
  val fullTimeEmployees = Some(FullTimeEmployeeCountModel(22))
  "The Check Answers page" should {
    "Verify that the Check Answers page contains the correct elements for Section 1: Company details" +
      " when it is fully populated with company detail models and had trade start date is true" in {
      val model = SEISCheckAnswersModel(Some(registeredAddressModel), Some(dateOfIncorporationModel), Some(natureOfBusinessModel),
        Vector(), None, None, Some(qualifyTrade), Some(hasInvestmentTradeStartedModelYes),
        Some(isSeventyPercentSpentModelYes), Some(shareIssuetDateModel), grossAssets, fullTimeEmployees, None, None, None, None, None,
        None, None, None)
      val page = CheckAnswers(model)(authorisedFakeRequest, applicationMessages)
      val document = Jsoup.parse(page.body)

      lazy val companyDetailsTableTBody = document.getElementById("company-details-table").select("tbody")

      document.getElementById("main-heading").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.heading")
      document.getElementById("description-one").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.description.one") + " " + Messages("page.checkAndSubmit.checkAnswers.scheme.seis")
      document.getElementById("description-two").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.description.two")

      //Section 1 table heading
      document.getElementById("companyDetailsSection-table-heading").text() shouldBe Messages("summaryQuestion.companyDetailsSection")
      //Nature of business
      companyDetailsTableTBody.select("tr").get(0).getElementById("natureOfBusiness-question").text() shouldBe
        Messages("summaryQuestion.natureOfBusiness")
      companyDetailsTableTBody.select("tr").get(0).getElementById("natureOfBusiness-answer").text() shouldBe
        model.natureOfBusinessModel.get.natureofbusiness
      companyDetailsTableTBody.select("tr").get(0).getElementById("natureOfBusiness-link")
        .attr("href") shouldEqual controllers.seis.routes.NatureOfBusinessController.show().url
      //Date of incorporation
      companyDetailsTableTBody.select("tr").get(1).getElementById("dateOfIncorporation-question").text() shouldBe
        Messages("summaryQuestion.dateOfIncorporation")
      companyDetailsTableTBody.select("tr").get(1).getElementById("dateOfIncorporation-answer").text() shouldBe
        DateOfIncorporationModel.toDateString(model.dateOfIncorporationModel.get.day.get,
          model.dateOfIncorporationModel.get.month.get, model.dateOfIncorporationModel.get.year.get)
      companyDetailsTableTBody.select("tr").get(1).getElementById("dateOfIncorporation-link")
        .attr("href") shouldEqual controllers.seis.routes.DateOfIncorporationController.show().url

      //  qualifying business
      companyDetailsTableTBody.select("tr").get(2).getElementById("qualifyBusinessActivity-question").text() shouldBe
        Messages("summaryQuestion.bussinessPurpose")
      companyDetailsTableTBody.select("tr").get(2).getElementById("qualifyBusinessActivity-answer").text() shouldBe
        qualifyTrade.isQualifyBusinessActivity
      companyDetailsTableTBody.select("tr").get(2).getElementById("qualifyBusinessActivity-link")
        .attr("href") shouldEqual controllers.seis.routes.QualifyBusinessActivityController.show().url
      //hasInvestmentTradeStarted
      companyDetailsTableTBody.select("tr").get(3).getElementById("hasInvestmentTradeStarted-question").text() shouldBe
        Messages("summaryQuestion.bussinessStatus") + " " + Messages("summaryQuestion.bussinessActivityStarted")
      companyDetailsTableTBody.select("tr").get(3).getElementById("hasInvestmentTradeStarted-answer").text() shouldBe
        hasInvestmentTradeStartedModelYes.hasInvestmentTradeStarted + " " + HasInvestmentTradeStartedModel.toDateString(hasInvestmentTradeStartedModelYes.hasInvestmentTradeStartedDay.get,
          hasInvestmentTradeStartedModelYes.hasInvestmentTradeStartedMonth.get, hasInvestmentTradeStartedModelYes.hasInvestmentTradeStartedYear.get)
      companyDetailsTableTBody.select("tr").get(3).getElementById("hasInvestmentTradeStarted-link")
        .attr("href") shouldEqual controllers.seis.routes.HasInvestmentTradeStartedController.show().url

      //SeventyPercentSpent
      companyDetailsTableTBody.select("tr").get(4).getElementById("isSeventyPercentSpent-question").text() shouldBe
        Messages("summaryQuestion.IsSeventyPercentSpent")
      companyDetailsTableTBody.select("tr").get(4).getElementById("isSeventyPercentSpent-answer").text() shouldBe
        isSeventyPercentSpentModelYes.isSeventyPercentSpent
      companyDetailsTableTBody.select("tr").get(4).getElementById("isSeventyPercentSpent-link")
        .attr("href") shouldEqual controllers.seis.routes.SeventyPercentSpentController.show().url

      //shareIssueDate
      companyDetailsTableTBody.select("tr").get(5).getElementById("shareIssueDate-question").text() shouldBe
        Messages("summaryQuestion.shareIssueDate")
      companyDetailsTableTBody.select("tr").get(5).getElementById("shareIssueDate-answer").text() shouldBe
        ShareIssueDateModel.toDateString(shareIssuetDateModel.day.get, shareIssuetDateModel.month.get, shareIssuetDateModel.year.get)
      companyDetailsTableTBody.select("tr").get(5).getElementById("shareIssueDate-link")
        .attr("href") shouldEqual controllers.seis.routes.ShareIssueDateController.show().url

      //Gross assets
      companyDetailsTableTBody.select("tr").get(6).getElementById("grossAssets-question").text() shouldBe
        Messages("summaryQuestion.businessGrossAssets")
      companyDetailsTableTBody.select("tr").get(6).getElementById("grossAssets-answer").text() shouldBe
        grossAssetsSEISModel.grossAssetsAmountBand()
      companyDetailsTableTBody.select("tr").get(6).getElementById("grossAssets-link")
        .attr("href") shouldEqual controllers.seis.routes.GrossAssetsController.show().url

      //Fulltime employees
      companyDetailsTableTBody.select("tr").get(7).getElementById("fullTimeEmployees-question").text() shouldBe
        Messages("summaryQuestion.fullTimeEmployees")
      companyDetailsTableTBody.select("tr").get(7).getElementById("fullTimeEmployees-answer").text() shouldBe
        fullTimeEmployees.get.employeeCount.toString()
      companyDetailsTableTBody.select("tr").get(7).getElementById("fullTimeEmployees-link")
        .attr("href") shouldEqual controllers.seis.routes.FullTimeEmployeeCountController.show().url

      document.getElementById("submit").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.button.confirm")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.seis.routes.SupportingDocumentsUploadController.show().url
    }

    "Verify that the Check Answers page contains the correct elements for Section 1: Company details" +
      " when it is fully populated with company detail models and had trade start date is false" in {
      val model = SEISCheckAnswersModel(Some(registeredAddressModel), Some(dateOfIncorporationModel),
        Some(natureOfBusinessModel), Vector(), None, None, Some(qualifyTrade), Some(hasInvestmentTradeStartedModelYes),
        Some(isSeventyPercentSpentModelYes), Some(shareIssuetDateModel), grossAssets, fullTimeEmployees, None, None, None, None, None,
        None, None, None)
      val page = CheckAnswers(model)(authorisedFakeRequest, applicationMessages)
      val document = Jsoup.parse(page.body)

      lazy val companyDetailsTableTBody = document.getElementById("company-details-table").select("tbody")

      document.getElementById("main-heading").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.heading")
      document.getElementById("description-one").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.description.one") + " " + Messages("page.checkAndSubmit.checkAnswers.scheme.seis")
      document.getElementById("description-two").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.description.two")

      //Section 1 table heading
      document.getElementById("companyDetailsSection-table-heading").text() shouldBe Messages("summaryQuestion.companyDetailsSection")
      //Nature of business
      companyDetailsTableTBody.select("tr").get(0).getElementById("natureOfBusiness-question").text() shouldBe
        Messages("summaryQuestion.natureOfBusiness")
      companyDetailsTableTBody.select("tr").get(0).getElementById("natureOfBusiness-answer").text() shouldBe
        model.natureOfBusinessModel.get.natureofbusiness
      companyDetailsTableTBody.select("tr").get(0).getElementById("natureOfBusiness-link")
        .attr("href") shouldEqual controllers.seis.routes.NatureOfBusinessController.show().url
      //Date of incorporation
      companyDetailsTableTBody.select("tr").get(1).getElementById("dateOfIncorporation-question").text() shouldBe
        Messages("summaryQuestion.dateOfIncorporation")
      companyDetailsTableTBody.select("tr").get(1).getElementById("dateOfIncorporation-answer").text() shouldBe
        DateOfIncorporationModel.toDateString(model.dateOfIncorporationModel.get.day.get,
          model.dateOfIncorporationModel.get.month.get, model.dateOfIncorporationModel.get.year.get)
      companyDetailsTableTBody.select("tr").get(1).getElementById("dateOfIncorporation-link")
        .attr("href") shouldEqual controllers.seis.routes.DateOfIncorporationController.show().url

      companyDetailsTableTBody.select("tr").get(2).getElementById("qualifyBusinessActivity-question").text() shouldBe
        Messages("summaryQuestion.bussinessPurpose")
      companyDetailsTableTBody.select("tr").get(2).getElementById("qualifyBusinessActivity-answer").text() shouldBe
        qualifyTrade.isQualifyBusinessActivity
      companyDetailsTableTBody.select("tr").get(2).getElementById("qualifyBusinessActivity-link")
        .attr("href") shouldEqual controllers.seis.routes.QualifyBusinessActivityController.show().url
      //hasInvestmentTradeStarted
      companyDetailsTableTBody.select("tr").get(3).getElementById("hasInvestmentTradeStarted-question").text() shouldBe
        Messages("summaryQuestion.bussinessStatus") + " " + Messages("summaryQuestion.bussinessActivityStarted")
      companyDetailsTableTBody.select("tr").get(3).getElementById("hasInvestmentTradeStarted-answer").text() shouldBe
        hasInvestmentTradeStartedModelYes.hasInvestmentTradeStarted + " " + HasInvestmentTradeStartedModel.toDateString(hasInvestmentTradeStartedModelYes.hasInvestmentTradeStartedDay.get,
          hasInvestmentTradeStartedModelYes.hasInvestmentTradeStartedMonth.get, hasInvestmentTradeStartedModelYes.hasInvestmentTradeStartedYear.get)
      companyDetailsTableTBody.select("tr").get(3).getElementById("hasInvestmentTradeStarted-link")
        .attr("href") shouldEqual controllers.seis.routes.HasInvestmentTradeStartedController.show().url

      //SeventyPercentSpent
      companyDetailsTableTBody.select("tr").get(4).getElementById("isSeventyPercentSpent-question").text() shouldBe
        Messages("summaryQuestion.IsSeventyPercentSpent")
      companyDetailsTableTBody.select("tr").get(4).getElementById("isSeventyPercentSpent-answer").text() shouldBe
        isSeventyPercentSpentModelYes.isSeventyPercentSpent
      companyDetailsTableTBody.select("tr").get(4).getElementById("isSeventyPercentSpent-link")
        .attr("href") shouldEqual controllers.seis.routes.SeventyPercentSpentController.show().url

      //shareIssueDate
      companyDetailsTableTBody.select("tr").get(5).getElementById("shareIssueDate-question").text() shouldBe
        Messages("summaryQuestion.shareIssueDate")
      companyDetailsTableTBody.select("tr").get(5).getElementById("shareIssueDate-answer").text() shouldBe
        ShareIssueDateModel.toDateString(shareIssuetDateModel.day.get, shareIssuetDateModel.month.get, shareIssuetDateModel.year.get)
      companyDetailsTableTBody.select("tr").get(5).getElementById("shareIssueDate-link")
        .attr("href") shouldEqual controllers.seis.routes.ShareIssueDateController.show().url

      //Gross assets
      companyDetailsTableTBody.select("tr").get(6).getElementById("grossAssets-question").text() shouldBe
        Messages("summaryQuestion.businessGrossAssets")
      companyDetailsTableTBody.select("tr").get(6).getElementById("grossAssets-answer").text() shouldBe
        grossAssetsSEISModel.grossAssetsAmountBand()
      companyDetailsTableTBody.select("tr").get(6).getElementById("grossAssets-link")
        .attr("href") shouldEqual controllers.seis.routes.GrossAssetsController.show().url

      //Fulltime employees
      companyDetailsTableTBody.select("tr").get(7).getElementById("fullTimeEmployees-question").text() shouldBe
        Messages("summaryQuestion.fullTimeEmployees")
      companyDetailsTableTBody.select("tr").get(7).getElementById("fullTimeEmployees-answer").text() shouldBe
        fullTimeEmployees.get.employeeCount.toString()
      companyDetailsTableTBody.select("tr").get(7).getElementById("fullTimeEmployees-link")
        .attr("href") shouldEqual controllers.seis.routes.FullTimeEmployeeCountController.show().url

      document.getElementById("submit").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.button.confirm")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.seis.routes.SupportingDocumentsUploadController.show().url
    }

    "Verify that the Check Answers page contains an empty table for Section 1: Company details" +
      " when an empty set of company detail models are passed" in {
      val model = SEISCheckAnswersModel(None, None, None, Vector(), None, None, None, None,
        None, None, None, None, None, None, None, None, None, None, None, None)
      val page = CheckAnswers(model)(authorisedFakeRequest, applicationMessages)
      val document = Jsoup.parse(page.body)

      lazy val companyDetailsTableTBody = document.getElementById("company-details-table").select("tbody")

      document.getElementById("main-heading").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.heading")
      document.getElementById("description-one").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.description.one") + " " + Messages("page.checkAndSubmit.checkAnswers.scheme.seis")
      document.getElementById("description-two").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.description.two")

      //Section 1 table heading
      document.getElementById("companyDetailsSection-table-heading").text() shouldBe Messages("summaryQuestion.companyDetailsSection")
      companyDetailsTableTBody.select("tr").size() shouldBe 0

      document.getElementById("submit").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.button.confirm")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.seis.routes.SupportingDocumentsUploadController.show().url
    }
  }

  "The Check Answers page" should {

    "Verify that the scheme description contains only SEIS" in {

      val model = SEISCheckAnswersModel(None, None, None, Vector(), None, None, None, None, None, None, None, None,
        None, None, None, None, None, None, None, None)
      val page = CheckAnswers(model)(authorisedFakeRequest, applicationMessages)
      val document = Jsoup.parse(page.body)

      lazy val companyDetailsTableTBody = document.getElementById("company-details-table").select("tbody")
      lazy val notAvailableMessage = Messages("common.notAvailable")

document.getElementById("main-heading").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.heading")
      document.getElementById("description-one").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.description.one") + " " + Messages("page.checkAndSubmit.checkAnswers.scheme.seis")
      document.getElementById("description-two").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.description.two")

      document.getElementById("description-two").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.description.two")

      //Section 1 table heading
      document.getElementById("companyDetailsSection-table-heading").text() shouldBe Messages("summaryQuestion.companyDetailsSection")
      companyDetailsTableTBody.select("tr").size() shouldBe 0

      document.getElementById("submit").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.button.confirm")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.seis.routes.SupportingDocumentsUploadController.show().url
    }
  }

}

