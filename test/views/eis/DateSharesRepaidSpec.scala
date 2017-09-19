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
import common.KeystoreKeys
import controllers.eis.{DateSharesRepaidController, routes}
import models.repayments.DateSharesRepaidModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._
import views.helpers.ViewSpec

import scala.concurrent.Future

class DateSharesRepaidSpec extends ViewSpec {

  object TestController extends DateSharesRepaidController {
    override lazy val applicationConfig = MockConfigEISFlow
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  def setupMocks(dateSharesRepaidModel: Option[DateSharesRepaidModel] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[DateSharesRepaidModel](Matchers.eq(KeystoreKeys.dateSharesRepaid))
      (Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(dateSharesRepaidModel))
  }

   "The DateSharesRepaid page" should {
    "contain the correct elements for a GET when a valid DateSharesRepaidModel is returned from keystore" in new Setup {
      val document: Document = {
        setupMocks(Some(dateSharesRepaidModel))
        val result = TestController.show(1).apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.DateSharesRepaid.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.DateSharesRepaid.heading")
      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.details.four")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.SharesRepaymentTypeController.show(1).url
      document.body.getElementsByClass("form-hint").text should include(Messages("common.date.hint.example"))
      document.body.getElementById("dateSharesRepaidDay").parent.text shouldBe Messages("common.date.fields.day")
      document.body.getElementById("dateSharesRepaidMonth").parent.text shouldBe Messages("common.date.fields.month")
      document.body.getElementById("dateSharesRepaidYear").parent.text shouldBe Messages("common.date.fields.year")
      document.getElementById("question-date-text-legend-id").hasClass("visuallyhidden") shouldBe true
      document.getElementById("question-text-id").text() shouldBe Messages("page.DateSharesRepaid.heading")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.select(".error-summary").isEmpty shouldBe true
    }

	 "contain the correct elements for a GET when there is no DateSharesRepaidModel returned from keystore" in new Setup {
      val document: Document = {
        setupMocks()
        val result = TestController.show(1).apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.DateSharesRepaid.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.DateSharesRepaid.heading")
      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.details.four")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.SharesRepaymentTypeController.show(1).url
      document.body.getElementsByClass("form-hint").text should include(Messages("common.date.hint.example"))
      document.body.getElementById("dateSharesRepaidDay").parent.text shouldBe Messages("common.date.fields.day")
      document.body.getElementById("dateSharesRepaidMonth").parent.text shouldBe Messages("common.date.fields.month")
      document.body.getElementById("dateSharesRepaidYear").parent.text shouldBe Messages("common.date.fields.year")
	    document.getElementById("question-date-text-legend-id").hasClass("visuallyhidden") shouldBe true
      document.getElementById("question-text-id").text() shouldBe Messages("page.DateSharesRepaid.heading")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.select(".error-summary").isEmpty shouldBe true

    }

    "contain the correct elements including an error summary for an POST that fails form validation" in new Setup {
      val document: Document = {
        setupMocks()
        val result = TestController.submit.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.DateSharesRepaid.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.DateSharesRepaid.heading")
      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.details.four")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.SharesRepaymentTypeController.show(1).url
      document.body.getElementsByClass("form-hint").text should include(Messages("common.date.hint.example"))
      document.body.getElementById("dateSharesRepaidDay").parent.text shouldBe Messages("common.date.fields.day")
      document.body.getElementById("dateSharesRepaidMonth").parent.text shouldBe Messages("common.date.fields.month")
      document.body.getElementById("dateSharesRepaidYear").parent.text shouldBe Messages("common.date.fields.year")
      document.getElementById("question-date-text-legend-id").hasClass("visuallyhidden") shouldBe true
      document.getElementById("question-text-id").text() shouldBe Messages("page.DateSharesRepaid.heading")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      
      //Check error present:
      document.getElementById("error-summary-display").hasClass("error-summary--show") shouldBe true


    }

  }

}
