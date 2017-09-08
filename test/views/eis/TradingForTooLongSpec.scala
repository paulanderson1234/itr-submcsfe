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

import controllers.helpers.FakeRequestHelper
import controllers.eis.routes
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import views.helpers.ViewSpec
import views.html.eis.investment._

class TradingForTooLongSpec extends ViewSpec with FakeRequestHelper {

  implicit val request = fakeRequest

  "The Trading For Too Long error page" should {
    lazy val view = TradingForTooLong()
    lazy val document: Document = Jsoup.parse(view.body)

    "contain the correct title" in {
      document.title shouldBe Messages("page.investment.TradingForTooLong.title")
    }

    "contain a back link" which {
      lazy val backLink = document.select("article > a")

      "has the correct text" in {
        backLink.text() shouldBe Messages("common.button.back")
      }

      "has the correct link" in {
        backLink.attr("href") shouldBe routes.NewProductController.show().url
      }
    }

    "contain the correct heading" in {
      document.select("h1").text() shouldBe Messages("common.error.soft.heading")
    }

    "contain a description of the error" in {
      document.select("article div p").first().text() shouldBe Messages("page.investment.TradingForTooLong.reason")
      document.getElementById("trading-too-long").text() shouldBe Messages("page.investment.TradingForTooLong.bullet.one")
      document.getElementById("not-new-business").text() shouldBe Messages("page.investment.TradingForTooLong.bullet.two")
    }

    "contain a change link" which {
      lazy val changeLink = document.select("article div p").get(1)

      "has the correct sentence" in {
        changeLink.text() shouldBe Messages("common.changeAnswers.text") +
          " " + Messages("common.changeAnswers.link") + "."
      }

      "contains the correct link text" in {
        changeLink.select("a").text() shouldBe Messages("common.changeAnswers.link")
      }

      "contains a link to the NewProductMarket page" in {
        changeLink.select("a").attr("href") shouldBe routes.NewProductController.show().url
      }
    }

    "contain the correct secondary heading" in {
      document.select("h2").text().trim shouldBe Messages("common.error.soft.secondaryHeading")
    }

    "contains the correct what next information" in {
      document.select("article div p").get(2).text() shouldBe Messages("common.error.soft.whatNext")
    }


    "contain a continue button" which {
      lazy val button = document.getElementById("next")

      "contains the correct message" in {
        button.text() shouldBe Messages("common.button.continue")
      }

      "have a form posting to the correct route" in {
        document.select("form").attr("action") shouldBe routes.TradingForTooLongController.submit().url
      }
    }
  }
}
