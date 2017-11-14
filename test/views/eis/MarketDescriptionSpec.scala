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
import models.MarketDescriptionModel
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import views.helpers.ViewSpec
import forms.MarketDescriptionForm._
import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import views.helpers.ViewSpec

class MarketDescriptionSpec extends ViewSpec {

  val testUrl = "/test/test"
  val testUrlOther = "/test/test/testanother"
  val marketDescriptionModel = MarketDescriptionModel("test")
  implicit val request = fakeRequest


   "The MarketDescription page" should {
     "contain the correct elements for a GET when a valid MarketDescriptionModel and valid back Url are returned from storage " in new Setup {
       lazy val document = Jsoup.parse(views.html.eis.shareDetails.MarketDescription(marketDescriptionForm.fill(marketDescriptionModel), testUrl).body)
       document.title() shouldBe Messages("page.MarketDescription.title")
       document.getElementById("main-heading").text() shouldBe Messages("page.MarketDescription.heading")
       document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.details.three")
       document.body.getElementById("back-link").attr("href") shouldEqual testUrl
       document.getElementById("labelTextId").text() shouldBe Messages("page.MarketDescription.heading")
       document.getElementById("labelTextId").hasClass("visuallyhidden") shouldBe true
       document.getElementById("legendId").text() shouldBe Messages("page.MarketDescription.heading")
       document.getElementById("legendId").hasClass("visuallyhidden") shouldBe true
       document.getElementById("description-hint").text() shouldBe Messages("page.MarketDescription.question.hint")
       document.getElementById("next").text() shouldBe Messages("common.button.snc")
       document.body.getElementById("description-one").text shouldBe Messages("page.MarketDescription.descriptionOne.text")
       document.select("textarea").attr("maxLength") shouldBe Constants.SuggestedTextMaxLength.toString
       document.select(".error-summary").isEmpty shouldBe true

     }

     "The MarketDescription page" should {
       "contain the correct elements for a GET when a valid MarketDescriptionModel and alternative valid back Url are returned from storage " in new Setup {
         lazy val document = Jsoup.parse(views.html.eis.shareDetails.MarketDescription(marketDescriptionForm.fill(marketDescriptionModel), testUrlOther).body)
         document.title() shouldBe Messages("page.MarketDescription.title")
         document.getElementById("main-heading").text() shouldBe Messages("page.MarketDescription.heading")
         document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.details.three")
         document.body.getElementById("back-link").attr("href") shouldEqual testUrlOther
         document.getElementById("labelTextId").text() shouldBe Messages("page.MarketDescription.heading")
         document.getElementById("labelTextId").hasClass("visuallyhidden") shouldBe true
         document.getElementById("legendId").text() shouldBe Messages("page.MarketDescription.heading")
         document.getElementById("legendId").hasClass("visuallyhidden") shouldBe true
         document.getElementById("description-hint").text() shouldBe Messages("page.MarketDescription.question.hint")
         document.getElementById("next").text() shouldBe Messages("common.button.snc")
         document.body.getElementById("description-one").text shouldBe Messages("page.MarketDescription.descriptionOne.text")
         document.select("textarea").attr("maxLength") shouldBe Constants.SuggestedTextMaxLength.toString
         document.select(".error-summary").isEmpty shouldBe true

       }

       "contain the correct elements for a GET when no MarketDescriptionModel is returned from storage " in new Setup {
         lazy val document = Jsoup.parse(views.html.eis.shareDetails.MarketDescription(marketDescriptionForm, testUrlOther).body)
         document.title() shouldBe Messages("page.MarketDescription.title")
         document.getElementById("main-heading").text() shouldBe Messages("page.MarketDescription.heading")
         document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.details.three")
         document.body.getElementById("back-link").attr("href") shouldEqual testUrlOther
         document.getElementById("labelTextId").text() shouldBe Messages("page.MarketDescription.heading")
         document.getElementById("labelTextId").hasClass("visuallyhidden") shouldBe true
         document.getElementById("legendId").text() shouldBe Messages("page.MarketDescription.heading")
         document.getElementById("legendId").hasClass("visuallyhidden") shouldBe true
         document.getElementById("description-hint").text() shouldBe Messages("page.MarketDescription.question.hint")
         document.getElementById("next").text() shouldBe Messages("common.button.snc")
         document.body.getElementById("description-one").text shouldBe Messages("page.MarketDescription.descriptionOne.text")
         document.select("textarea").attr("maxLength") shouldBe Constants.SuggestedTextMaxLength.toString

       }

       "contain the correct elements including an error summary for an POST that fails form validation" in new Setup {
         lazy val document = Jsoup.parse(views.html.eis.shareDetails.MarketDescription(marketDescriptionForm.bind(Map(
           "descriptionTextArea" -> ""
         )), testUrl).body)

         document.title() shouldBe Messages("page.MarketDescription.title")
         document.getElementById("main-heading").text() shouldBe Messages("page.MarketDescription.heading")
         document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.details.three")
         document.body.getElementById("back-link").attr("href") shouldEqual testUrl
         document.getElementById("labelTextId").text() shouldBe Messages("page.MarketDescription.heading")
         document.getElementById("labelTextId").hasClass("visuallyhidden") shouldBe true
         document.getElementById("legendId").text() shouldBe Messages("page.MarketDescription.heading")
         document.getElementById("legendId").hasClass("visuallyhidden") shouldBe true
         document.getElementById("description-hint").text() shouldBe Messages("page.MarketDescription.question.hint")
         document.getElementById("next").text() shouldBe Messages("common.button.snc")
         document.body.getElementById("description-one").text shouldBe Messages("page.MarketDescription.descriptionOne.text")
         document.select("textarea").attr("maxLength") shouldBe Constants.SuggestedTextMaxLength.toString
         // Check error present:
         document.getElementById("error-summary-display").hasClass("error-summary--show") shouldBe true


       }

     }
   }

}
