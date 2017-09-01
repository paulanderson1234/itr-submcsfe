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
import forms.WasAnyValueReceivedForm._
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import views.helpers.ViewSpec

class WasAnyValueReceivedViewSpec extends ViewSpec with FakeRequestHelper {

  "Was any value received view" when {
    implicit val request = fakeRequest

    "no errors are present" should {
      lazy val doc = Jsoup.parse(views.html.eis.investors.WasAnyValueReceived(wasAnyValueReceivedForm).body)

      "have the correct title" in {
        doc.title() shouldBe Messages("page.investors.wasAnyValueReceived.title")
      }

      "have a back link" which {
        lazy val backLink = doc.select("a.back-link")

        "has the correct message" in {
          backLink.text() shouldBe Messages("common.button.back")
        }

        "links to the review investors page" in {
          backLink.attr("href") shouldBe controllers.eis.routes.ReviewAllInvestorsController.show().url
        }
      }

      "have the correct breadcrumbs message" in {
        doc.select("span.form-hint-breadcrumb").text() shouldBe Messages("common.section.progress.details.four")
      }

      "have no error summary" in {
        doc.select(".error-summary").isEmpty shouldBe true
      }

      "have the correct heading" in {
        doc.select("h1").text() shouldBe Messages("page.investors.wasAnyValueReceived.title")
      }

      "have a guidance section" which {

        "has a definition for value received" in {
          doc.select("div.form-group p").first().text() shouldBe Messages("page.investors.wasAnyValueReceived.guidance.definition")
        }

        "has a list" which {
          lazy val list = doc.select("div.form-group > ul")

          "has the correct list title" in {
            doc.select("div.form-group p").get(1).text() shouldBe Messages("page.investors.wasAnyValueReceived.guidance.title")
          }

          "has the correct entry for the first bullet point" in {
            list.select("li").get(0).text() shouldBe Messages("page.investors.wasAnyValueReceived.guidance.list.one")
          }

          "has the correct entry for the second bullet point" in {
            list.select("li").get(1).text() shouldBe Messages("page.investors.wasAnyValueReceived.guidance.list.two")
          }

          "has the correct entry for the third bullet point" in {
            list.select("li").get(2).text() shouldBe Messages("page.investors.wasAnyValueReceived.guidance.list.three")
          }

          "has the correct entry for the fourth bullet point" in {
            list.select("li").get(3).text() shouldBe Messages("page.investors.wasAnyValueReceived.guidance.list.four")
          }
        }

        "has an instruction for how to answer the question" in {
          doc.select("div.form-group p").get(2).text() shouldBe Messages("page.investors.wasAnyValueReceived.guidance.hint")
        }
      }

      "have a form element" which {
        lazy val form = doc.select("form")

        "has an action of the page's submit route" in {
          form.attr("action") shouldBe "/investment-tax-relief-cs/eis/was-any-value-received"
        }

        "has a method of POST" in {
          form.attr("method") shouldBe "POST"
        }
      }

      "have a legend" which {
        lazy val legend = doc.select("legend")

        "has the correct message" in {
          legend.text() shouldBe Messages("page.investors.wasAnyValueReceived.title")
        }

        "has a class of visuallyhidden" in {
          legend.hasClass("visuallyhidden") shouldBe true
        }
      }

      "have en element for an option of 'Yes'" in {
        doc.select("label input#wasAnyValueReceived-yes").attr("value") shouldBe "Yes"
      }

      "have en element for an option of 'No'" in {
        doc.select("label input#wasAnyValueReceived-no").attr("value") shouldBe "No"
      }

      "have a hidden dropdown section" which {

        "has a secondary heading with the correct message" in {
          doc.select("h2.heading-medium").text() shouldBe Messages("page.investors.wasAnyValueReceived.subHeading")
        }

        "has a list" which {
          lazy val list = doc.select("form ul")

          "has the correct list title" in {
            doc.select("form p").text() shouldBe Messages("page.investors.wasAnyValueReceived.additionalGuidance.title")
          }

          "has the correct entry for the first bullet point" in {
            list.select("li").get(0).text() shouldBe Messages("page.investors.wasAnyValueReceived.additionalGuidance.list.one")
          }

          "has the correct entry for the second bullet point" in {
            list.select("li").get(1).text() shouldBe Messages("page.investors.wasAnyValueReceived.additionalGuidance.list.two")
          }

          "has the correct entry for the third bullet point" in {
            list.select("li").get(2).text() shouldBe Messages("page.investors.wasAnyValueReceived.additionalGuidance.list.three")
          }

          "has the correct entry for the fourth bullet point" in {
            list.select("li").get(3).text() shouldBe Messages("page.investors.wasAnyValueReceived.additionalGuidance.list.four")
          }

          "has the correct entry for the fifth bullet point" in {
            list.select("li").get(4).text() shouldBe Messages("page.investors.wasAnyValueReceived.additionalGuidance.list.five")
          }
        }

        "has an input box for text" in {
          doc.select("form textarea").attr("name") shouldBe "descriptionTextArea"
        }
      }

      "have a continue button" which {
        lazy val button = doc.select("button")

        "has the correct message" in {
          button.text() shouldBe Messages("common.button.snc")
        }

        "has a type of 'Submit'" in {
          button.attr("type") shouldBe "submit"
        }
      }
    }

    "errors are present" should {
      lazy val doc = Jsoup.parse(views.html.eis.investors.WasAnyValueReceived(wasAnyValueReceivedForm.bind(Map(
        "wasAnyValueReceived" -> "",
        "descriptionTextArea" -> ""
      ))).body)

      "display an error summary" in {
        doc.select(".error-summary").isEmpty shouldBe false
      }
    }
  }
}
