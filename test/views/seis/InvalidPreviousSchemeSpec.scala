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
import controllers.seis.routes
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import views.helpers.ViewSpec

class InvalidPreviousSchemeSpec extends ViewSpec with FakeRequestHelper {

  implicit val request = fakeRequest

  "The Invalid Previous Scheme error page" should {
    lazy val view = views.html.seis.previousInvestment.InvalidPreviousScheme(1)
    lazy val document: Document = Jsoup.parse(view.body)

    "contain the correct title" in {
      document.title shouldBe Messages("page.previousInvestment.InvalidPreviousScheme.title")
    }

    "contain a back link" which {
      lazy val backLink = document.select("article > a")

      "has the correct text" in {
        backLink.text() shouldBe Messages("common.button.back")
      }

      "has the correct link" in {
        backLink.attr("href") shouldBe routes.ReviewPreviousSchemesController.change(1).url
      }
    }

    "contain the correct heading" in {
      document.select("h1").text() shouldBe Messages("page.previousInvestment.InvalidPreviousScheme.heading")
    }

    "contain a description of the error" in {
      document.select("article div p").first().text() shouldBe Messages("page.previousInvestment.InvalidPreviousScheme.reason")
    }

    "contain the correct secondary heading" in {
      document.select("h2").text().trim shouldBe Messages("page.previousInvestment.InvalidPreviousScheme.secondaryHeading")
    }

    "contains the correct what next information" in {
      document.select("article div p").get(1).text() shouldBe Messages("page.previousInvestment.InvalidPreviousScheme.whatNext")
    }

    "contain a change link" which {
      lazy val changeLink = document.select("article div p").get(2)

      "has the correct sentence" in {
        changeLink.text() shouldBe Messages("common.changeAnswers.text") +
          " " + Messages("common.changeAnswers.link") + "."
      }

      "contains the correct link text" in {
        changeLink.select("a").text() shouldBe Messages("common.changeAnswers.link")
      }

      "contains a link to the review-previous-schemes-change page" in {
        changeLink.select("a").attr("href") shouldBe routes.ReviewPreviousSchemesController.change(1).url
      }
    }

    "contain a continue button" which {
      lazy val button = document.getElementById("submit")

      "contains the correct message" in {
        button.text() shouldBe Messages("common.button.continue")
      }

      "have a form posting to the correct route" in {
        document.select("form").attr("action") shouldBe controllers.seis.routes.InvalidPreviousSchemeController.submit().url
      }
    }
  }
}
