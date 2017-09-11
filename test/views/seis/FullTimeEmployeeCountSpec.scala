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

import common.Constants
import org.jsoup.Jsoup
import play.api.test.FakeRequest
import views.helpers.ViewSpec
import views.html.seis.companyDetails.FullTimeEmployeeCount
import forms.FullTimeEmployeeCountForm._
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._

class FullTimeEmployeeCountSpec extends ViewSpec {

  "FullTimeEmployeeCount view" when {
    implicit lazy val request = FakeRequest("GET", "")

    "not supplied with form errors" should {
      lazy val document: Document = {
        val result = FullTimeEmployeeCount(fullTimeEmployeeCountForm)
        Jsoup.parse(contentAsString(result))
      }

      "have the correct title" in {
        document.title() shouldBe Messages("page.companyDetails.fullTimeEmployeeCount.title")
      }

      "have the correct back link text" in {
        document.select("a.back-link").text() shouldBe Messages("common.button.back")
      }

      "have the correct back link url" in {
        document.select("a.back-link").attr("href") shouldBe "/investment-tax-relief-cs/seis/gross-assets"
      }

      "have the progress details" in {
        document.select("article span").first().text shouldBe Messages("common.section.progress.details.one")
      }

      "have the correct heading" in {
        document.select("h1").text() shouldBe Messages("page.companyDetails.fullTimeEmployeeCount.heading")
      }

      "have a paragraph for guidance" in {
        document.select("article p").first().text() shouldBe Messages("page.companyDetails.fullTimeEmployeeCount.guidance.one")
      }

      "have some progressive disclosure" in {
        document.select("summary").text() shouldBe Messages("page.companyDetails.fullTimeEmployeeCount.guidance.progressive")
      }

      "have a first paragraph of disclosed text" in {
        document.select("details p").get(0).text() shouldBe Messages("page.companyDetails.fullTimeEmployeeCount.guidance.two")
      }

      "have a second paragraph of disclosed text" in {
        document.select("details p").get(1).text() shouldBe Messages("page.companyDetails.fullTimeEmployeeCount.guidance.three")
      }

      "have a legend" which {
        lazy val legend = document.select("legend")

        "has the correct message" in {
          legend.text() shouldBe Messages("page.companyDetails.fullTimeEmployeeCount.title")
        }

        "has a class of visuallyhidden" in {
          legend.hasClass("visuallyhidden") shouldBe true
        }
      }

      "have a form posting to the correct route" in {
        document.select("form").attr("action") shouldBe controllers.seis.routes.FullTimeEmployeeCountController.submit().url
      }

      "have the correct question in a label" in {
        document.select("fieldset label").text() shouldBe Messages("page.companyDetails.fullTimeEmployeeCount.heading")
      }

      "have an input for employeeCount" in {
        document.select("input").attr("name") shouldBe "employeeCount"
      }

      "have max length for the employeeCount input field" in {
        document.select("input").attr("maxlength") shouldBe Constants.fullTimeEquivalenceFieldMaxLength
      }

      "have an error summary" in {
        document.select(".error-summary").isEmpty shouldBe true
      }

      "have a next button" in {
        document.select("button").text() shouldBe Messages("common.button.snc")
      }
    }

    "supplied with form errors" should {
      lazy val document: Document = {
        val map = Map("employeeCount" -> "")
        val result = FullTimeEmployeeCount(fullTimeEmployeeCountForm.bind(map))
        Jsoup.parse(contentAsString(result))
      }

      "have an error summary" in {
        document.getElementById("error-summary-display").hasClass("error-summary--show")
      }

      "have the correct title" in {
        document.title() shouldBe Messages("page.companyDetails.fullTimeEmployeeCount.title")
      }

      "have the correct back link text" in {
        document.select("a.back-link").text() shouldBe Messages("common.button.back")
      }

      "have the correct back link url" in {
        document.select("a.back-link").attr("href") shouldBe "/investment-tax-relief-cs/seis/gross-assets"
      }

      "have the progress details" in {
        document.select("article span").first().text shouldBe Messages("common.section.progress.details.one")
      }

      "have the correct heading" in {
        document.select("h1").text() shouldBe Messages("page.companyDetails.fullTimeEmployeeCount.heading")
      }

      "have a paragraph for guidance" in {
        document.select("article p").first().text() shouldBe Messages("page.companyDetails.fullTimeEmployeeCount.guidance.one")
      }

      "have some progressive disclosure" in {
        document.select("summary").text() shouldBe Messages("page.companyDetails.fullTimeEmployeeCount.guidance.progressive")
      }

      "have a first paragraph of disclosed text" in {
        document.select("details p").get(0).text() shouldBe Messages("page.companyDetails.fullTimeEmployeeCount.guidance.two")
      }

      "have a second paragraph of disclosed text" in {
        document.select("details p").get(1).text() shouldBe Messages("page.companyDetails.fullTimeEmployeeCount.guidance.three")
      }

      "have a legend" which {
        lazy val legend = document.select("legend")

        "has the correct message" in {
          legend.text() shouldBe Messages("page.companyDetails.fullTimeEmployeeCount.title")
        }

        "has a class of visuallyhidden" in {
          legend.hasClass("visuallyhidden") shouldBe true
        }
      }

      "have a form posting to the correct route" in {
        document.select("form").attr("action") shouldBe controllers.seis.routes.FullTimeEmployeeCountController.submit().url
      }

      "have the correct question in a label" in {
        document.select("label span.visuallyhidden").text() shouldBe Messages("page.companyDetails.fullTimeEmployeeCount.heading")
      }

      "have an input for employeeCount" in {
        document.select("input").attr("name") shouldBe "employeeCount"
      }

      "have max length for the employeeCount input field" in {
        document.select("input").attr("maxlength") shouldBe Constants.fullTimeEquivalenceFieldMaxLength
      }

      "have a next button" in {
        document.select("button").text() shouldBe Messages("common.button.snc")
      }
    }
  }
}
