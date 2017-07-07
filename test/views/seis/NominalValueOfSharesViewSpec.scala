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

import controllers.helpers.FakeRequestHelper
import forms.NominalValueOfSharesForm._
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import views.helpers.ViewSpec

class NominalValueOfSharesViewSpec extends ViewSpec with FakeRequestHelper {

  "NominalValueOfSharesView" when {
    implicit lazy val request = fakeRequest

    "supplied with no errors" should {
      lazy val view = views.html.seis.shares.NominalValueOfShares(nominalValueOfSharesForm)
      lazy val doc = Jsoup.parse(view.body)

      "have the correct title" in {
        doc.title shouldBe Messages("page.seis.nominalValueOfShares.title")
      }

      "have the correct section" in {
        doc.select("article span").first().text() shouldBe Messages("common.section.progress.company.details.three")
      }

      "have a link to the number of shares page" in {
        doc.select("article a").first().attr("href") shouldBe controllers.seis.routes.NominalValueOfSharesController.show().url
      }

      "have a header with the correct question" in {
        doc.select("h1").text() shouldBe Messages("page.seis.nominalValueOfShares.title")
      }

      "have a form" which {
        lazy val form = doc.select("form")

        "has a method of POST" in {
          form.attr("method") shouldBe "POST"
        }

        "has an action to the correct route" in {
          form.attr("action") shouldBe controllers.seis.routes.NominalValueOfSharesController.submit().url
        }
      }

      "have an input" which {
        lazy val input = doc.select("fieldset")

        "contains a label" which {
          lazy val label = input.select("label")

          "has the correct message" in {
            label.text() shouldBe Messages("page.seis.nominalValueOfShares.title")
          }

          "is visually hidden" in {
            label.hasClass("visuallyhidden")
          }
        }

        "contains help text with the correct message" in {
          input.select("p").text() shouldBe Messages("page.seis.nominalValueOfShares.hint")
        }

        "contains an input field with the correct name" in {
          input.select("input").attr("name") shouldBe "value"
        }
      }

      "have a button with the correct text" in {
        doc.select("button").text() shouldBe Messages("common.button.snc")
      }
    }

    "supplied with some errors" should {
      lazy val view = views.html.seis.shares.NominalValueOfShares(nominalValueOfSharesForm.bind(Map("value" -> "")))
      lazy val doc = Jsoup.parse(view.body)

      "have an error summary" in {
        doc.getElementById("error-summary-display").hasClass("error-summary--show")
      }
    }
  }
}
