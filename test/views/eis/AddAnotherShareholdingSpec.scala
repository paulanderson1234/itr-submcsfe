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
import controllers.helpers.FakeRequestHelper
import views.helpers.ViewSpec
import forms.AddAnotherShareholdingForm._
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._

class AddAnotherShareholdingSpec extends ViewSpec with FakeRequestHelper {
  implicit val request = fakeRequest

  "The AddAnotherShareHolding view" when {

    "no errors occur" should {
      lazy val view = views.html.eis.investors.AddAnotherShareholding(addAnotherShareholdingForm, 1)
      lazy val doc = Jsoup.parse(view.body)

      "have the correct title" in {
        doc.title() shouldBe Messages("page.investors.AddAnotherShareholding.title")
      }

      "have a back link" which {
        lazy val backLink = doc.select("a.back-link")

        "has the correct text" in {
          backLink.text() shouldBe Messages("common.button.back")
        }

        "has a link to the review shareholders page" in {
          backLink.attr("href") shouldBe controllers.eis.routes.PreviousShareHoldingsReviewController.show(1).url
        }
      }

      "not contain an error summary" in {
        doc.select("div.error-summary").isEmpty shouldBe true
      }

      "has a progress bar with the correct text" in {
        doc.select("span.form-hint-breadcrumb").text() shouldBe Messages("common.section.progress.details.four")
      }

      "have the correct heading" in {
        doc.select("h1").text() shouldBe Messages("page.investors.AddAnotherShareholding.title")
      }

      "have a form" which {
        lazy val form = doc.select("form")

        "has a method of POST" in {
          form.attr("method") shouldBe "POST"
        }

        "has the correct POST action" in {
          form.attr("action") shouldBe controllers.eis.routes.AddAnotherShareholdingController.submit(1).url
        }
      }

      "have the correct visually hidden legend" in {
        doc.select("legend.visuallyhidden").text() shouldBe Messages("page.investors.AddAnotherShareholding.title")
      }

      "have an input label for 'Yes'" which {

        "contains the correct text" in {
          doc.select("label[for=addAnotherShareholding-yes]").text() shouldBe Constants.StandardRadioButtonYesValue
        }

        "contains the correct value" in {
          doc.select("label[for=addAnotherShareholding-yes] input").attr("value") shouldBe Constants.StandardRadioButtonYesValue
        }
      }

      "have an input label for 'No'" which {

        "contains the correct text" in {
          doc.select("label[for=addAnotherShareholding-no]").text() shouldBe Constants.StandardRadioButtonNoValue
        }

        "contains the correct value" in {
          doc.select("label[for=addAnotherShareholding-no] input").attr("value") shouldBe Constants.StandardRadioButtonNoValue
        }
      }

      "has the correct continue button" in {
        doc.select("button").text() shouldBe Messages("common.button.snc")
      }
    }

    "some errors occur" should {
      lazy val view = views.html.eis.investors.AddAnotherShareholding(addAnotherShareholdingForm.bind(Map("addAnotherShareholding" -> "")), 2)
      lazy val doc = Jsoup.parse(view.body)

      "have the correct title" in {
        doc.title() shouldBe Messages("page.investors.AddAnotherShareholding.title")
      }

      "have a back link" which {
        lazy val backLink = doc.select("a.back-link")

        "has the correct text" in {
          backLink.text() shouldBe Messages("common.button.back")
        }

        "has a link to the review shareholders page" in {
          backLink.attr("href") shouldBe controllers.eis.routes.PreviousShareHoldingsReviewController.show(2).url
        }
      }

      "contain an error summary" in {
        doc.select("div.error-summary").isEmpty shouldBe false
      }

      "has a progress bar with the correct text" in {
        doc.select("span.form-hint-breadcrumb").text() shouldBe Messages("common.section.progress.details.four")
      }

      "have the correct heading" in {
        doc.select("h1").text() shouldBe Messages("page.investors.AddAnotherShareholding.title")
      }

      "have a form" which {
        lazy val form = doc.select("form")

        "has a method of POST" in {
          form.attr("method") shouldBe "POST"
        }

        "has the correct POST action" in {
          form.attr("action") shouldBe controllers.eis.routes.AddAnotherShareholdingController.submit(2).url
        }
      }

      "have the correct visually hidden legend" in {
        doc.select("legend.visuallyhidden").text() shouldBe Messages("page.investors.AddAnotherShareholding.title")
      }

      "have an input label for 'Yes'" which {

        "contains the correct text" in {
          doc.select("label[for=addAnotherShareholding-yes]").text() shouldBe Constants.StandardRadioButtonYesValue
        }

        "contains the correct value" in {
          doc.select("label[for=addAnotherShareholding-yes] input").attr("value") shouldBe Constants.StandardRadioButtonYesValue
        }
      }

      "have an input label for 'No'" which {

        "contains the correct text" in {
          doc.select("label[for=addAnotherShareholding-no]").text() shouldBe Constants.StandardRadioButtonNoValue
        }

        "contains the correct value" in {
          doc.select("label[for=addAnotherShareholding-no] input").attr("value") shouldBe Constants.StandardRadioButtonNoValue
        }
      }

      "has the correct continue button" in {
        doc.select("button").text() shouldBe Messages("common.button.snc")
      }
    }
  }
}
