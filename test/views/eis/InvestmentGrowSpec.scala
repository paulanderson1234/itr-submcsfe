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
import common.{Constants, KeystoreKeys}
import controllers.eis.InvestmentGrowController
import controllers.helpers.MockDataGenerator
import models.{InvestmentGrowModel, NewGeographicalMarketModel, NewProductModel}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._
import views.helpers.ViewSpec

import scala.concurrent.Future

class InvestmentGrowSpec extends ViewSpec {

  lazy val SuggestedMaxLengthText: String = MockDataGenerator.randomAlphanumericString(Constants.SuggestedTextMaxLength)
  val validInvestmentGrowModelMaxLength = InvestmentGrowModel(SuggestedMaxLengthText)

  object TestController extends InvestmentGrowController {
    override lazy val applicationConfig = MockConfigEISFlow
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  def setupMocks(investmentGrowModel: Option[InvestmentGrowModel] = None,
                 newGeographicalMarketModel: Option[NewGeographicalMarketModel] = None,
                 newProductModel: Option[NewProductModel] = None, backLink: Option[String] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[InvestmentGrowModel](Matchers.eq(KeystoreKeys.investmentGrow))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(investmentGrowModel))
    when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkInvestmentGrow))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(backLink))
    when(mockS4lConnector.fetchAndGetFormData[NewGeographicalMarketModel](Matchers.eq(KeystoreKeys.newGeographicalMarket))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(newGeographicalMarketModel))
    when(mockS4lConnector.fetchAndGetFormData[NewProductModel](Matchers.eq(KeystoreKeys.newProduct))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(newProductModel))
  }

  "The InvestmentGrow Page" should {

    "Verify that the correct elements are loaded when coming from WhatWillUse page" in new Setup {


      val document: Document = {
        setupMocks(investmentGrowModel = Some(validInvestmentGrowModelMaxLength),backLink = Some(controllers.eis.routes.TotalAmountRaisedController.show().url))
        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.investment.InvestmentGrow.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
      document.getElementById("description-one").text() shouldBe Messages("page.investment.InvestmentGrow.example.text")
      document.getElementById("bullet-one").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.one")
      document.getElementById("bullet-two").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.two")
      document.getElementById("bullet-three").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.three")
      document.getElementById("description-three").text() shouldBe Messages("page.investment.InvestmentGrow.description.three")
      document.getElementById("labelTextId").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("descriptionTextArea").attr("maxlength") shouldBe s"${Constants.SuggestedTextMaxLength}"
      document.body.getElementById("descriptionTextArea").text() shouldBe SuggestedMaxLengthText
      document.getElementsByTag("textarea").attr("name") shouldBe "descriptionTextArea"
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.TotalAmountRaisedController.show().url
      document.body.getElementById("get-help-action").text shouldBe Messages("common.error.help.text")

      document.getElementById("labelTextId").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
      document.getElementById("labelTextId").hasClass("visuallyhidden") shouldBe true
      document.getElementById("how-to-write-business-plan").text() should include (Messages("page.investment.InvestmentGrow.businessplan.readmore"))
      document.body.getElementById("business-plan").text() shouldEqual getExternalLinkText(Messages("page.investment.InvestmentGrow.businessplan.link"))
      document.body.getElementById("business-plan").attr("href") shouldEqual "https://www.gov.uk/write-business-plan"
      document.body.getElementById("business-plan").hasClass("external-link") shouldBe true
      document.select(".error-summary").isEmpty shouldBe true
    }

    "Verify that the correct elements are loaded when coming from PreviousBeforeDOFCS page" in new Setup {


      val document: Document = {
        setupMocks(investmentGrowModel = Some(validInvestmentGrowModelMaxLength),backLink = Some(controllers.eis.routes.PreviousBeforeDOFCSController.show().url))
        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.investment.InvestmentGrow.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
      document.getElementById("description-one").text() shouldBe Messages("page.investment.InvestmentGrow.example.text")
      document.getElementById("bullet-one").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.one")
      document.getElementById("bullet-two").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.two")
      document.getElementById("bullet-three").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.three")
      document.getElementById("description-three").text() shouldBe Messages("page.investment.InvestmentGrow.description.three")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("descriptionTextArea").attr("maxlength") shouldBe s"${Constants.SuggestedTextMaxLength}"
      document.body.getElementById("descriptionTextArea").text() shouldBe SuggestedMaxLengthText
      document.getElementsByTag("textarea").attr("name") shouldBe "descriptionTextArea"
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.PreviousBeforeDOFCSController.show().url
      document.body.getElementById("get-help-action").text shouldBe Messages("common.error.help.text")
      document.getElementById("labelTextId").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
      document.getElementById("labelTextId").hasClass("visuallyhidden") shouldBe true
      document.getElementById("how-to-write-business-plan").text() should include (Messages("page.investment.InvestmentGrow.businessplan.readmore"))
      document.body.getElementById("business-plan").text() shouldEqual getExternalLinkText(Messages("page.investment.InvestmentGrow.businessplan.link"))
      document.body.getElementById("business-plan").attr("href") shouldEqual "https://www.gov.uk/write-business-plan"
      document.body.getElementById("business-plan").hasClass("external-link") shouldBe true
      document.select(".error-summary").isEmpty shouldBe true
    }

    "Verify that the correct elements are loaded when coming from NewProduct page" in new Setup {

      val document: Document = {
        setupMocks(investmentGrowModel = Some(validInvestmentGrowModelMaxLength), backLink = Some(controllers.eis.routes.NewProductController.show().url))
        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.investment.InvestmentGrow.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
      document.getElementById("description-one").text() shouldBe Messages("page.investment.InvestmentGrow.example.text")
      document.getElementById("bullet-one").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.one")
      document.getElementById("bullet-two").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.two")
      document.getElementById("bullet-three").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.three")
      document.getElementById("description-three").text() shouldBe Messages("page.investment.InvestmentGrow.description.three")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("descriptionTextArea").attr("maxlength") shouldBe s"${Constants.SuggestedTextMaxLength}"
      document.body.getElementById("descriptionTextArea").text() shouldBe SuggestedMaxLengthText
      document.getElementsByTag("textarea").attr("name") shouldBe "descriptionTextArea"
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.NewProductController.show().url
      document.body.getElementById("get-help-action").text shouldBe Messages("common.error.help.text")
      document.getElementById("labelTextId").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
      document.getElementById("labelTextId").hasClass("visuallyhidden") shouldBe true
      document.getElementById("how-to-write-business-plan").text() should include (Messages("page.investment.InvestmentGrow.businessplan.readmore"))
      document.body.getElementById("business-plan").text() shouldEqual getExternalLinkText(Messages("page.investment.InvestmentGrow.businessplan.link"))
      document.body.getElementById("business-plan").attr("href") shouldEqual "https://www.gov.uk/write-business-plan"
      document.body.getElementById("business-plan").hasClass("external-link") shouldBe true
      document.select(".error-summary").isEmpty shouldBe true

    }

    "Verify that the correct elements are loaded when coming from the SubsidiariesSpendingInvestment page)" in new Setup {
      val document: Document = {
        setupMocks(investmentGrowModel = Some(validInvestmentGrowModelMaxLength), backLink = Some(controllers.eis.routes.SubsidiariesSpendingInvestmentController.show().url))
        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.investment.InvestmentGrow.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
      document.getElementById("description-one").text() shouldBe Messages("page.investment.InvestmentGrow.example.text")
      document.getElementById("bullet-one").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.one")
      document.getElementById("bullet-two").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.two")
      document.getElementById("bullet-three").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.three")
      document.getElementById("description-three").text() shouldBe Messages("page.investment.InvestmentGrow.description.three")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("descriptionTextArea").attr("maxlength") shouldBe s"${Constants.SuggestedTextMaxLength}"
      document.body.getElementById("descriptionTextArea").text() shouldBe SuggestedMaxLengthText
      document.getElementsByTag("textarea").attr("name") shouldBe "descriptionTextArea"
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.SubsidiariesSpendingInvestmentController.show().url
      document.body.getElementById("get-help-action").text shouldBe Messages("common.error.help.text")
      document.getElementById("labelTextId").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
      document.getElementById("labelTextId").hasClass("visuallyhidden") shouldBe true
      document.getElementById("how-to-write-business-plan").text() should include (Messages("page.investment.InvestmentGrow.businessplan.readmore"))
      document.body.getElementById("business-plan").text() shouldEqual getExternalLinkText(Messages("page.investment.InvestmentGrow.businessplan.link"))
      document.body.getElementById("business-plan").attr("href") shouldEqual "https://www.gov.uk/write-business-plan"
      document.body.getElementById("business-plan").hasClass("external-link") shouldBe true
      document.select(".error-summary").isEmpty shouldBe true
    }


    "Verify that the correct elements are loaded when coming from the SubsidiariesNinetyOwned page" in new Setup {
      val document: Document = {
        setupMocks(investmentGrowModel = Some(validInvestmentGrowModelMaxLength), backLink = Some(controllers.eis.routes.SubsidiariesNinetyOwnedController.show().url))
        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.investment.InvestmentGrow.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
      document.getElementById("description-one").text() shouldBe Messages("page.investment.InvestmentGrow.example.text")
      document.getElementById("bullet-one").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.one")
      document.getElementById("bullet-two").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.two")
      document.getElementById("bullet-three").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.three")
      document.getElementById("description-three").text() shouldBe Messages("page.investment.InvestmentGrow.description.three")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("descriptionTextArea").attr("maxlength") shouldBe s"${Constants.SuggestedTextMaxLength}"
      document.body.getElementById("descriptionTextArea").text() shouldBe SuggestedMaxLengthText
      document.getElementsByTag("textarea").attr("name") shouldBe "descriptionTextArea"
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.SubsidiariesNinetyOwnedController.show().url
      document.body.getElementById("get-help-action").text shouldBe Messages("common.error.help.text")
      document.getElementById("labelTextId").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
      document.getElementById("labelTextId").hasClass("visuallyhidden") shouldBe true
      document.getElementById("how-to-write-business-plan").text() should include (Messages("page.investment.InvestmentGrow.businessplan.readmore"))
      document.body.getElementById("business-plan").text() shouldEqual getExternalLinkText(Messages("page.investment.InvestmentGrow.businessplan.link"))
      document.body.getElementById("business-plan").attr("href") shouldEqual "https://www.gov.uk/write-business-plan"
      document.body.getElementById("business-plan").hasClass("external-link") shouldBe true
      document.select(".error-summary").isEmpty shouldBe true
    }

    "Verify that the correct elements are loaded when hasGeoMarket is true and hasNewProduct is true" in new Setup{
      val document: Document = {
        setupMocks(Some(validInvestmentGrowModelMaxLength),Some(newGeographicalMarketModelYes),
          Some(newProductMarketModelYes),Some(controllers.eis.routes.TotalAmountRaisedController.show().url))
        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.investment.InvestmentGrow.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
      document.getElementById("description-one").text() shouldBe Messages("page.investment.InvestmentGrow.example.text")
      document.getElementById("bullet-one").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.one")
      document.getElementById("bullet-two").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.two")
      document.getElementById("bullet-three").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.three")
      document.getElementById("description-two").text() shouldBe Messages("page.investment.InvestmentGrow.description.two")
      document.getElementById("optional-bullet-list").children().size() shouldBe 2
      document.getElementById("bullet-geographical-market").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.geographicalMarket")
      document.getElementById("bullet-product-market").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.productMarket")
      document.getElementById("description-three").text() shouldBe Messages("page.investment.InvestmentGrow.description.three")
      document.getElementById("labelTextId").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("descriptionTextArea").attr("maxlength") shouldBe s"${Constants.SuggestedTextMaxLength}"
      document.body.getElementById("descriptionTextArea").text() shouldBe SuggestedMaxLengthText
      document.getElementsByTag("textarea").attr("name") shouldBe "descriptionTextArea"
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.TotalAmountRaisedController.show().url
      document.body.getElementById("get-help-action").text shouldBe  Messages("common.error.help.text")
      document.getElementById("labelTextId").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
      document.getElementById("labelTextId").hasClass("visuallyhidden") shouldBe true
      document.getElementById("how-to-write-business-plan").text() should include (Messages("page.investment.InvestmentGrow.businessplan.readmore"))
      document.body.getElementById("business-plan").text() shouldEqual getExternalLinkText(Messages("page.investment.InvestmentGrow.businessplan.link"))
      document.body.getElementById("business-plan").attr("href") shouldEqual "https://www.gov.uk/write-business-plan"
      document.body.getElementById("business-plan").hasClass("external-link") shouldBe true
      document.select(".error-summary").isEmpty shouldBe true
    }

    "Verify that the correct elements are loaded when hasGeoMarket is true and hasNewProduct is false" in new Setup{

      val document: Document = {
        setupMocks(Some(validInvestmentGrowModelMaxLength),Some(newGeographicalMarketModelYes),
          Some(newProductMarketModelNo),Some(controllers.eis.routes.TotalAmountRaisedController.show().url))
        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.investment.InvestmentGrow.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
      document.getElementById("description-one").text() shouldBe Messages("page.investment.InvestmentGrow.example.text")
      document.getElementById("bullet-one").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.one")
      document.getElementById("bullet-two").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.two")
      document.getElementById("bullet-three").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.three")
      document.getElementById("description-two").text() shouldBe Messages("page.investment.InvestmentGrow.description.two")
      document.getElementById("optional-bullet-list").children().size() shouldBe 1
      document.getElementById("bullet-geographical-market").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.geographicalMarket")
      document.getElementById("description-three").text() shouldBe Messages("page.investment.InvestmentGrow.description.three")
      document.getElementById("labelTextId").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("descriptionTextArea").attr("maxlength") shouldBe s"${Constants.SuggestedTextMaxLength}"
      document.body.getElementById("descriptionTextArea").text() shouldBe SuggestedMaxLengthText
      document.getElementsByTag("textarea").attr("name") shouldBe "descriptionTextArea"
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.TotalAmountRaisedController.show().url
      document.body.getElementById("get-help-action").text shouldBe  Messages("common.error.help.text")
      document.getElementById("labelTextId").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
      document.getElementById("labelTextId").hasClass("visuallyhidden") shouldBe true
      document.getElementById("how-to-write-business-plan").text() should include (Messages("page.investment.InvestmentGrow.businessplan.readmore"))
      document.body.getElementById("business-plan").text() shouldEqual getExternalLinkText(Messages("page.investment.InvestmentGrow.businessplan.link"))
      document.body.getElementById("business-plan").attr("href") shouldEqual "https://www.gov.uk/write-business-plan"
      document.body.getElementById("business-plan").hasClass("external-link") shouldBe true
      document.select(".error-summary").isEmpty shouldBe true
    }

    "Verify that the correct elements are loaded when hasGeoMarket is false and hasNewProduct is true" in new Setup{
      val document: Document = {
        setupMocks(Some(validInvestmentGrowModelMaxLength),Some(newGeographicalMarketModelNo),
          Some(newProductMarketModelYes),Some(controllers.eis.routes.TotalAmountRaisedController.show().url))
        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.investment.InvestmentGrow.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
      document.getElementById("description-one").text() shouldBe Messages("page.investment.InvestmentGrow.example.text")
      document.getElementById("bullet-one").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.one")
      document.getElementById("bullet-two").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.two")
      document.getElementById("bullet-three").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.three")
      document.getElementById("description-two").text() shouldBe Messages("page.investment.InvestmentGrow.description.two")
      document.getElementById("optional-bullet-list").children().size() shouldBe 1
      document.getElementById("bullet-product-market").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.productMarket")
      document.getElementById("description-three").text() shouldBe Messages("page.investment.InvestmentGrow.description.three")
      document.getElementById("labelTextId").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("descriptionTextArea").attr("maxlength") shouldBe s"${Constants.SuggestedTextMaxLength}"
      document.body.getElementById("descriptionTextArea").text() shouldBe SuggestedMaxLengthText
      document.getElementsByTag("textarea").attr("name") shouldBe "descriptionTextArea"
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.TotalAmountRaisedController.show().url
      document.body.getElementById("get-help-action").text shouldBe  Messages("common.error.help.text")
      document.getElementById("labelTextId").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
      document.getElementById("labelTextId").hasClass("visuallyhidden") shouldBe true
      document.getElementById("how-to-write-business-plan").text() should include (Messages("page.investment.InvestmentGrow.businessplan.readmore"))
      document.body.getElementById("business-plan").text() shouldEqual getExternalLinkText(Messages("page.investment.InvestmentGrow.businessplan.link"))
      document.body.getElementById("business-plan").attr("href") shouldEqual "https://www.gov.uk/write-business-plan"
      document.body.getElementById("business-plan").hasClass("external-link") shouldBe true
      document.select(".error-summary").isEmpty shouldBe true
    }

    "Verify that the correct elements are loaded when hasGeoMarket is false and hasNewProduct is false" in new Setup{

      val document: Document = {
        setupMocks(Some(validInvestmentGrowModelMaxLength),Some(newGeographicalMarketModelNo),
          Some(newProductMarketModelNo),Some(controllers.eis.routes.TotalAmountRaisedController.show().url))
        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.investment.InvestmentGrow.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
      document.getElementById("description-one").text() shouldBe Messages("page.investment.InvestmentGrow.example.text")
      document.getElementById("bullet-one").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.one")
      document.getElementById("bullet-two").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.two")
      document.getElementById("bullet-three").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.three")
      document.getElementById("description-three").text() shouldBe Messages("page.investment.InvestmentGrow.description.three")
      document.getElementById("labelTextId").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("descriptionTextArea").attr("maxlength") shouldBe s"${Constants.SuggestedTextMaxLength}"
      document.body.getElementById("descriptionTextArea").text() shouldBe SuggestedMaxLengthText
      document.getElementsByTag("textarea").attr("name") shouldBe "descriptionTextArea"
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.TotalAmountRaisedController.show().url
      document.body.getElementById("get-help-action").text shouldBe  Messages("common.error.help.text")
      document.getElementById("labelTextId").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
      document.getElementById("labelTextId").hasClass("visuallyhidden") shouldBe true
      document.getElementById("how-to-write-business-plan").text() should include (Messages("page.investment.InvestmentGrow.businessplan.readmore"))
      document.body.getElementById("business-plan").text() shouldEqual getExternalLinkText(Messages("page.investment.InvestmentGrow.businessplan.link"))
      document.body.getElementById("business-plan").attr("href") shouldEqual "https://www.gov.uk/write-business-plan"
      document.body.getElementById("business-plan").hasClass("external-link") shouldBe true
      document.select(".error-summary").isEmpty shouldBe true
    }

    "Verify that the correct elements are loaded when newGeoMarket is not defined and hasNewProduct is not defined" in new Setup{
      val document: Document = {
        setupMocks(investmentGrowModel = Some(validInvestmentGrowModelMaxLength), backLink = Some(controllers.eis.routes.TotalAmountRaisedController.show().url))
        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.investment.InvestmentGrow.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
      document.getElementById("description-one").text() shouldBe Messages("page.investment.InvestmentGrow.example.text")
      document.getElementById("bullet-one").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.one")
      document.getElementById("bullet-two").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.two")
      document.getElementById("bullet-three").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.three")
      document.getElementById("description-three").text() shouldBe Messages("page.investment.InvestmentGrow.description.three")
      document.getElementById("labelTextId").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("descriptionTextArea").attr("maxlength") shouldBe s"${Constants.SuggestedTextMaxLength}"
      document.body.getElementById("descriptionTextArea").text() shouldBe SuggestedMaxLengthText
      document.getElementsByTag("textarea").attr("name") shouldBe "descriptionTextArea"
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.TotalAmountRaisedController.show().url
      document.body.getElementById("get-help-action").text shouldBe  Messages("common.error.help.text")
      document.getElementById("labelTextId").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
      document.getElementById("labelTextId").hasClass("visuallyhidden") shouldBe true
      document.getElementById("how-to-write-business-plan").text() should include (Messages("page.investment.InvestmentGrow.businessplan.readmore"))
      document.body.getElementById("business-plan").text() shouldEqual getExternalLinkText(Messages("page.investment.InvestmentGrow.businessplan.link"))
      document.body.getElementById("business-plan").attr("href") shouldEqual "https://www.gov.uk/write-business-plan"
      document.body.getElementById("business-plan").hasClass("external-link") shouldBe true
      document.select(".error-summary").isEmpty shouldBe true
    }

    "show an error no data entered" in new Setup {
      val document: Document = {
        setupMocks(backLink = Some(controllers.eis.routes.TotalAmountRaisedController.show().url))
        val result = TestController.submit.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.investment.InvestmentGrow.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
      document.getElementById("description-one").text() shouldBe Messages("page.investment.InvestmentGrow.example.text")
      document.getElementById("bullet-one").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.one")
      document.getElementById("bullet-two").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.two")
      document.getElementById("bullet-three").text() shouldBe Messages("page.investment.InvestmentGrow.bullet.three")
      document.getElementById("description-three").text() shouldBe Messages("page.investment.InvestmentGrow.description.three")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.TotalAmountRaisedController.show().url
      document.body.getElementById("get-help-action").text shouldBe Messages("common.error.help.text")
      document.getElementById("labelTextId").text() shouldBe Messages("page.investment.InvestmentGrow.heading")
      document.getElementById("labelTextId").hasClass("visuallyhidden") shouldBe true
      document.getElementById("how-to-write-business-plan").text() should include (Messages("page.investment.InvestmentGrow.businessplan.readmore"))
      document.body.getElementById("business-plan").text() shouldEqual getExternalLinkText(Messages("page.investment.InvestmentGrow.businessplan.link"))
      document.body.getElementById("business-plan").attr("href") shouldEqual "https://www.gov.uk/write-business-plan"
      document.body.getElementById("business-plan").hasClass("external-link") shouldBe true
      document.getElementById("error-summary-display").hasClass("error-summary--show") shouldBe true
      document.getElementById("error-summary-heading").text shouldBe Messages("common.error.summary.heading")
      document.getElementById("descriptionTextArea-error-summary").text shouldBe Messages("common.error.fieldRequired")
      document.getElementsByClass("error-notification").text shouldBe Messages("common.error.fieldRequired")
    }
  }
}
