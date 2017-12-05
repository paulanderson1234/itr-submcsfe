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

import auth.{MockConfig, MockAuthConnector}
import common.{Constants, KeystoreKeys}
import config.FrontendAppConfig
import controllers.eis.CommercialSaleController
import models.CommercialSaleModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._
import views.helpers.ViewSpec

import scala.concurrent.Future

class CommercialSaleSpec extends ViewSpec {

  val commercialSaleModelInvalidYes = new CommercialSaleModel(Constants.StandardRadioButtonYesValue, None, Some(25), Some(2015))
  val testUrl = "/test/testing"
  val testUrlAnother = "/test/testing/another"

  object TestController extends CommercialSaleController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  def setupMocks(commercialSaleModel: Option[CommercialSaleModel] = None, backUrl: Option[String]): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(commercialSaleModel))
    when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkCommercialSale))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(backUrl))
  }

  "The Contact Details page" should {

    "Verify that the commercial sale page contains the correct elements when a valid 'Yes' CommercialSaleModel is passed" in new Setup {
      val document: Document = {
        setupMocks(Some(commercialSaleModelYes), Some(testUrl))
        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.companyDetails.CommercialSale.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.CommercialSale.heading")
      document.getElementById("form-hint-id").text() shouldBe Messages("common.date.hint.example")
      document.getElementById("question-text-id").text() shouldBe Messages("page.companyDetails.CommercialSale.question.hint")
      document.getElementById("question-text-id").hasClass("h2-heading") shouldBe true

      document.select("label[for=hasCommercialSale-yes]").text() shouldBe Messages("common.radioYesLabel")
      document.select("label[for=hasCommercialSale-no]").text() shouldBe Messages("common.radioNoLabel")
      //println(s"=================" +  document.select("legend").text())
     // document.select("legend").text() shouldBe Messages("page.companyDetails.CommercialSale.question.hint")
      document.select("legend").hasClass("visuallyhidden") shouldBe true

      document.body.getElementById("back-link").attr("href") shouldEqual testUrl
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.details.one")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")

      document.select("error-summary--show").isEmpty shouldBe true
    }


    "Verify that the commercial sale page contains the correct elements when a valid 'No' CommercialSaleModel is passed" in new Setup {
      val document: Document = {
        setupMocks(Some(commercialSaleModelNo), Some(testUrlAnother))
        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.companyDetails.CommercialSale.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.CommercialSale.heading")
      document.getElementById("form-hint-id").text() shouldBe Messages("common.date.hint.example")
      document.getElementById("question-text-id").text() shouldBe Messages("page.companyDetails.CommercialSale.question.hint")
      document.getElementById("question-text-id").hasClass("h2-heading")  shouldBe true
      document.select("label[for=hasCommercialSale-yes]").text() shouldBe Messages("common.radioYesLabel")
      document.select("label[for=hasCommercialSale-no]").text() shouldBe Messages("common.radioNoLabel")
      //document.select("legend").text() shouldBe Messages("page.companyDetails.CommercialSale.question.hint")
      document.select("legend").hasClass("visuallyhidden") shouldBe true
      document.select("error-summary--show").isEmpty shouldBe true
      document.body.getElementById("back-link").attr("href") shouldEqual testUrlAnother
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.details.one")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
    }

    "Verify that the commercial sale page contains the correct elements when an invalid CommercialSaleModel is passed" in new Setup {
      val document: Document = {
        setupMocks(None,  Some(testUrl))
        val result = TestController.submit.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.companyDetails.CommercialSale.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.CommercialSale.heading")
      document.getElementById("form-hint-id").text() shouldBe Messages("common.date.hint.example")
      document.getElementById("question-text-id").text() shouldBe Messages("page.companyDetails.CommercialSale.question.hint")
      document.getElementById("question-text-id").hasClass("h2-heading") shouldBe true
      document.select("label[for=hasCommercialSale-yes]").text() shouldBe Messages("common.radioYesLabel")
      document.select("label[for=hasCommercialSale-no]").text() shouldBe Messages("common.radioNoLabel")
     // document.select("legend").text() shouldBe Messages("page.companyDetails.CommercialSale.question.hint")
      document.select("legend").hasClass("visuallyhidden") shouldBe true
      document.body.getElementById("back-link").attr("href") shouldEqual testUrl
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.details.one")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.select("error-summary--show").isEmpty shouldBe true
    }

    "Verify that the commercial sale page contains the correct elements when an invalid CommercialSaleYesModel is passed" in new Setup {
      val document: Document = {
        setupMocks(Some(commercialSaleModelInvalidYes), Some(testUrl))
        val result = TestController.submit.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.companyDetails.CommercialSale.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.CommercialSale.heading")
      document.getElementById("form-hint-id").text() shouldBe Messages("common.date.hint.example")
      document.getElementById("question-text-id").text() shouldBe Messages("page.companyDetails.CommercialSale.question.hint")
      document.getElementById("question-text-id").hasClass("h2-heading") shouldBe true
      document.select("label[for=hasCommercialSale-yes]").text() shouldBe Messages("common.radioYesLabel")
      document.select("label[for=hasCommercialSale-no]").text() shouldBe Messages("common.radioNoLabel")
      //document.select("legend").text() shouldBe Messages("page.companyDetails.CommercialSale.question.hint")
      document.select("legend").hasClass("visuallyhidden") shouldBe true
      document.body.getElementById("back-link").attr("href") shouldEqual testUrl
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.details.one")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.getElementById("error-summary-display").hasClass("error-summary--show") shouldBe true
    }

  }
}
