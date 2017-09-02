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

import forms.ResearchStartDateForm._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatestplus.play.OneAppPerSuite
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.helpers.ViewSpec
import views.html.seis.companyDetails.ResearchStartDate

class ResearchStartDateSpec extends ViewSpec with OneAppPerSuite{

  "The Research Start Date page" when {
    implicit lazy val request = FakeRequest("GET", "")

    "no error occurs" should {
      lazy val document: Document = {
        val result = ResearchStartDate(researchStartDateForm)
        Jsoup.parse(contentAsString(result))
      }

      "have the correct title" in {
        document.title() shouldBe Messages("page.companyDetails.researchStartDate.title")
      }

      "have the correct heading" in {
        document.select("h1").text() shouldBe Messages("page.companyDetails.researchStartDate.heading")
      }

      "have the correct question in a legend" in {
        document.select("legend").first().text() shouldBe Messages("page.companyDetails.researchStartDate.heading")
      }

      "have the question visually hidden" in {
        document.select("legend").first().attr("class") shouldBe "visuallyhidden"
      }

      "have a label for 'Yes'" in {
        document.select("label[for=hasStartedResearch-yes]").text() shouldBe Messages("common.radioYesLabel")
      }

      "have a label for 'No'" in {
        document.select("label[for=hasStartedResearch-no]").text() shouldBe Messages("common.radioNoLabel")
      }

      "have the secondary question" in {
        document.select("legend span.h2-heading").text() shouldBe Messages("page.companyDetails.researchStartDate.question")
      }

      "have the secondary question visually hidden" in {
        document.select("legend span.h2-heading").hasClass("visuallyhidden")
      }

      "have a label for 'Day'" in {
        document.select("label[for=researchStartDay]").text() shouldBe Messages("common.day")
      }

      "have a label for 'Month'" in {
        document.select("label[for=researchStartMonth]").text() shouldBe Messages("common.month")
      }

      "have a label for 'Year'" in {
        document.select("label[for=researchStartYear]").text() shouldBe Messages("common.year")
      }

      "have the correct back link text" in {
        document.select("a.back-link").text() shouldBe Messages("common.button.back")
      }

      "have the correct back link url" in {
        document.select("a.back-link").attr("href") shouldBe  controllers.seis.routes.QualifyBusinessActivityController.show().url
      }

      "have the progress details" in {
        document.select("article span").first().text shouldBe Messages("common.section.progress.details.one")
      }

      "have a next button" in {
        document.select("button").text() shouldBe Messages("common.button.snc")
      }
    }


    "an error occurs" should {
      lazy val document: Document = {
        val map = Map("hasStartedResearch" -> "Yes",
          "researchStartDay" -> "",
          "researchStartMonth" -> "",
          "researchStartYear" -> "")
        val result = ResearchStartDate(researchStartDateForm.bind(map))
        Jsoup.parse(contentAsString(result))
      }

      "have an error summary" in {
        document.getElementById("error-summary-display").hasClass("error-summary--show")
      }

      "have the correct title" in {
        document.title() shouldBe Messages("page.companyDetails.researchStartDate.title")
      }

      "have the correct heading" in {
        document.select("h1").text() shouldBe Messages("page.companyDetails.researchStartDate.heading")
      }

      "have a form posting to the correct route" in {
        document.select("form").attr("action") shouldBe controllers.seis.routes.ResearchStartDateController.submit().url
      }

      "have the correct question in a legend" in {
        document.select("legend").first().text() shouldBe Messages("page.companyDetails.researchStartDate.heading")
      }

      "have the question visually hidden" in {
        document.select("legend").first().attr("class") shouldBe "visuallyhidden"
      }

      "have a label for 'Yes'" in {
        document.select("label[for=hasStartedResearch-yes]").text() shouldBe Messages("common.radioYesLabel")
      }

      "have a label for 'No'" in {
        document.select("label[for=hasStartedResearch-no]").text() shouldBe Messages("common.radioNoLabel")
      }

      "have the secondary question" in {
        document.select("legend span.h2-heading").text() shouldBe Messages("page.companyDetails.researchStartDate.question")
      }

      "have the secondary question visually hidden" in {
        document.select("legend span.h2-heading").hasClass("visuallyhidden")
      }

      "have a label for 'Day'" in {
        document.select("label[for=researchStartDay]").text() shouldBe Messages("common.day")
      }

      "have a label for 'Month'" in {
        document.select("label[for=researchStartMonth]").text() shouldBe Messages("common.month")
      }

      "have a label for 'Year'" in {
        document.select("label[for=researchStartYear]").text() shouldBe Messages("common.year")
      }

      "have the correct back link text" in {
        document.select("a.back-link").text() shouldBe Messages("common.button.back")
      }

      "have the correct back link url" in {
        document.select("a.back-link").attr("href") shouldBe  controllers.seis.routes.QualifyBusinessActivityController.show().url
      }

      "have the progress details" in {
        document.select("article span").first().text shouldBe Messages("common.section.progress.details.one")
      }

      "have a next button" in {
        document.select("button").text() shouldBe Messages("common.button.snc")
      }
    }
  }
}
