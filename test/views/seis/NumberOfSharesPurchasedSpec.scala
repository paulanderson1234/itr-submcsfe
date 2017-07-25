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

import org.jsoup.Jsoup
import play.api.test.FakeRequest
import views.helpers.ViewSpec
import views.html.seis.investors.NumberOfSharesPurchased
import forms.NumberOfSharesPurchasedForm._
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._

class NumberOfSharesPurchasedSpec extends ViewSpec {

  "NumberOfSharesPurchased view" when {
    implicit lazy val request = FakeRequest("GET", "")

    "not supplied with form errors" should {
      lazy val document: Document = {
        val result = NumberOfSharesPurchased("Company", "TODO", numberOfSharesPurchasedForm)
        Jsoup.parse(contentAsString(result))
      }

      "have the correct title" in {
        document.title() shouldBe Messages("page.seis.investors.numberOfSharesPurchased.title", "company", "TODO")
      }

      "have the correct back link text" in {
        document.select("a.back-link").text() shouldBe Messages("common.button.back")
      }

      "have the correct back link url" in {
        document.select("a.back-link").attr("href") shouldBe "/investment-tax-relief-cs/seis/number-of-shares-purchased/1"
      }

     "have the correct heading" in {
        document.select("h1").text() shouldBe Messages("page.seis.investors.numberOfSharesPurchased.title", "company", "TODO")
      }

      "have a form posting to the correct route" in {
        document.select("form").attr("action") shouldBe controllers.seis.routes.NumberOfSharesPurchasedController.submit().url
      }

      "have the correct question in a label" in {
        document.select("fieldset label").text() shouldBe Messages("page.seis.investors.numberOfSharesPurchased.heading", "company", "TODO")
      }

      "have an input for SharesPurchased" in {
        document.select("input").attr("name") shouldBe "numberOfSharesPurchased"
      }
      "have a next button" in {
        document.select("button").text() shouldBe Messages("common.button.snc")
      }
    }

    "supplied with form errors" should {
      lazy val document: Document = {
        val map = Map("numberOfSharesPurchased" -> "")
        val result = NumberOfSharesPurchased("Company","TODO", numberOfSharesPurchasedForm.bind(map))
        Jsoup.parse(contentAsString(result))
      }

      "have an error summary" in {
        document.getElementById("error-summary-display").hasClass("error-summary--show")
      }

      "have the correct title" in {
        document.title() shouldBe Messages("page.seis.investors.numberOfSharesPurchased.title", "company", "TODO")
      }
      "have the correct back link text" in {
        document.select("a.back-link").text() shouldBe Messages("common.button.back")
      }

      "have the correct back link url" in {
        document.select("a.back-link").attr("href") shouldBe "/investment-tax-relief-cs/seis/number-of-shares-purchased/1"
      }

      "have the correct heading" in {
        document.select("h1").text() shouldBe Messages("page.seis.investors.numberOfSharesPurchased.heading", "company", "TODO")
      }

      "have a form posting to the correct route" in {
        document.select("form").attr("action") shouldBe controllers.seis.routes.NumberOfSharesPurchasedController.submit().url
      }

      "have the correct question in a label" in {
        document.select("label span.visuallyhidden").text() shouldBe Messages("page.seis.investors.numberOfSharesPurchased.heading", "company", "TODO")
      }

      "have an input for employeeCount" in {
        document.select("input").attr("name") shouldBe "numberOfSharesPurchased"
      }

      "have a next button" in {
        document.select("button").text() shouldBe Messages("common.button.snc")
      }
    }
  }
}
