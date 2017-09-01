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

import forms.PreviousShareHoldingNominalValueForm._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.helpers.ViewSpec
import views.html.eis.investors.PreviousShareHoldingNominalValue

class PreviousShareHoldingNominalValueSpec extends ViewSpec {

  val backUrl = controllers.eis.routes.PreviousShareHoldingDescriptionController.show(1, Some(1)).url

  "PreviousShareHoldingNominalValue view" when {
    implicit lazy val request = FakeRequest("GET", "")

      "not supplied with form errors" should {
        lazy val document: Document = {
          val result = PreviousShareHoldingNominalValue(previousShareHoldingNominalValueForm, backUrl, 1)
          Jsoup.parse(contentAsString(result))

        }

      "have the correct title" in {
        document.title() shouldBe Messages("page.investors.previousShareHoldingNominalValue.title")
      }

      "have the correct back link text" in {
        document.select("a.back-link").text() shouldBe Messages("common.button.back")
      }

      "have the correct back link url" in {
        document.select("a.back-link").attr("href") shouldBe backUrl
        //TODO change route to Previous Shareholdings Share Description page
      }

      "have the progress details" in {
        document.select("article span").first().text shouldBe Messages("common.section.progress.details.four")
      }

      "have the correct heading" in {
        document.select("h1").text() shouldBe Messages("page.investors.previousShareHoldingNominalValue.heading")
      }

      "have a paragraph for guidance" in {
        document.select("article p").first().text() shouldBe Messages("page.investors.previousShareHoldingNominalValue.whereToFind")
      }

      "have a form posting to the correct route" in {
        document.select("form").attr("action") shouldBe
          controllers.eis.routes.PreviousShareHoldingNominalValueController.submit(Some(1)).url
      }

      "have the correct hint" in {
        document.getElementById("label-nominal-value-hint").text() shouldBe Messages("page.investors.previousShareHoldingNominalValue.hint")
      }

      "have a next button" in {
        document.select("button").text() shouldBe Messages("common.button.snc")
      }
    }

    "supplied with form errors" should {
      lazy val document: Document = {
        val map = Map("previousShareHoldingNominalValue" -> "")
        val result = PreviousShareHoldingNominalValue(previousShareHoldingNominalValueForm.bind(map), backUrl, 1)
        Jsoup.parse(contentAsString(result))
      }

      "have an error summary" in {
        document.getElementById("error-summary-display").hasClass("error-summary--show")
      }

      "have the correct title" in {
        document.title() shouldBe Messages("page.investors.previousShareHoldingNominalValue.title")
      }

      "have the correct back link text" in {
        document.select("a.back-link").text() shouldBe Messages("common.button.back")
      }

      "have the correct back link url" in {
        document.select("a.back-link").attr("href") shouldBe backUrl
        //TODO change route to Previous Shareholdings Share Description page
      }

      "have the progress details" in {
        document.select("article span").first().text shouldBe Messages("common.section.progress.details.four")
      }

      "have the correct heading" in {
        document.select("h1").text() shouldBe Messages("page.investors.previousShareHoldingNominalValue.heading")
      }

      "have a paragraph for guidance" in {
        document.select("article p").first().text() shouldBe Messages("page.investors.previousShareHoldingNominalValue.whereToFind")
      }

      "have a form posting to the correct route" in {
        document.select("form").attr("action") shouldBe
          controllers.eis.routes.PreviousShareHoldingNominalValueController.submit(Some(1)).url
      }

      "have the correct hint" in {
        document.getElementById("label-nominal-value-hint").text() shouldBe Messages("page.investors.previousShareHoldingNominalValue.hint")
      }

      "have a next button" in {
        document.select("button").text() shouldBe Messages("common.button.snc")
      }
    }
  }
}
