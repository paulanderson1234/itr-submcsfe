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

import models.submission.SubmissionResponse
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import views.helpers.ViewSpec
import views.html.eis.checkAndSubmit.Acknowledgement

class AcknowledgementSpec extends ViewSpec {

  val submissionResponse = SubmissionResponse("2014-12-17T09:30:47Z","FBUND09889765")

  "The Acknowledgement page" should {
    lazy val page = Acknowledgement(submissionResponse)(fakeRequest,applicationMessages)
    lazy val document = Jsoup.parse(page.body)

    "have the correct title" in {
      document.title() shouldBe Messages("page.checkAndSubmit.acknowledgement.title")
    }

    "have a transaction banner" which {
      lazy val banner = document.select("div.transaction-banner--complete")

      "has the correct heading" in {
        banner.select("h1").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.title")
      }

      "has a paragraph regarding the reference number" in {
        banner.select("p").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.refNumberHeading")
      }

      "has a span containing the reference number" in {
        banner.select("span").text() shouldBe "FBUND09889765"
      }
    }

    "have a what next section" which {
      lazy val section = document.select("div.column-two-thirds > div.form-group").get(1)

      "has the correct subheading" in {
        section.select("h2").text() shouldBe Messages("common.error.soft.secondaryHeading")
      }

      "has a list heading" in {
        section.select("p").get(0).text() shouldBe Messages("page.checkAndSubmit.acknowledgement.whatNext.list.heading")
      }

      "has a list" which {
        lazy val list = section.select("ul")

        "has the correct first element" in {
          list.select("li").get(0).text() shouldBe Messages("page.checkAndSubmit.acknowledgement.whatNext.list.one")
        }

        "has the correct second element" in {
          list.select("li").get(1).text() shouldBe Messages("page.checkAndSubmit.acknowledgement.whatNext.list.two")
        }
      }

      "has a description of the outcome" in {
        section.select("p").get(1).text() shouldBe Messages("page.checkAndSubmit.acknowledgement.whatNext.outcome")
      }

      "has a description of the certificate issue process" in {
        section.select("p").get(2).text() shouldBe Messages("page.eis.checkAndSubmit.acknowledgement.whatNext.issue")
      }

      "has a warning about claiming relief" in {
        section.select("p.important-notice").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.whatNext.certificate.warning")
      }

      "has a description of the failed condition process" in {
        section.select("p").get(4).text() shouldBe Messages("page.checkAndSubmit.acknowledgement.whatNext.conditions")
      }
    }

    "have a supporting documents section" which {
      lazy val section = document.select("div.column-two-thirds > div.form-group").get(2)

      "has the correct subheading" in {
        section.select("h2").text() shouldBe Messages("page.checkAndSubmit.acknowledgement.supporting.docs.heading")
      }

      "has the correct list heading" in {
        section.select("p").get(0).text() shouldBe Messages("page.checkAndSubmit.acknowledgement.review")
      }

      "has a list" which {
        lazy val list = section.select("ul")

        "has the correct first element" in {
          list.select("li").get(0).text() shouldBe Messages("page.supportingDocuments.bullet.one")
        }

        "has the correct second element" in {
          list.select("li").get(1).text() shouldBe Messages("page.supportingDocuments.bullet.two")
        }

        "has the correct third element" in {
          list.select("li").get(2).text() shouldBe Messages("page.supportingDocuments.bullet.three")
        }

        "has the correct fourth element" in {
          list.select("li").get(3).text() shouldBe Messages("page.supportingDocuments.bullet.four")
        }

        "has the correct fifth element" in {
          list.select("li").get(4).text() shouldBe Messages("page.supportingDocuments.bullet.five")
        }
      }
    }

    "has a final instructions paragraph" which {
      lazy val paragraph = document.select("div.column-two-thirds > div.form-group").get(3).select("p")

      "has the first part of the message" in {
        paragraph.select("span").first().text() shouldBe Messages("page.checkAndSubmit.acknowledgement.upload.one")
      }

      "has the second part of the message as a link" which {
        lazy val link = paragraph.select("a").first()

        "links to the upload page" in {
          link.attr("href") shouldBe controllers.eis.routes.SupportingDocumentsUploadController.show().url
        }

        "has the correct part of the message" in {
          link.text() shouldBe Messages("page.checkAndSubmit.acknowledgement.upload.link")
        }
      }

      "has the third part of the message" in {
        paragraph.select("span").last().text() shouldBe Messages("page.checkAndSubmit.acknowledgement.upload.two")
      }

      "has the fourth part of the message as a link" which {
        lazy val link = paragraph.select("a").last()

        "links to the hub" in {
          link.attr("href") shouldBe controllers.routes.HomeController.redirectToHub().url
        }

        "has the correct part of the message" in {
          link.text() shouldBe Messages("page.checkAndSubmit.acknowledgement.upload.dashboard")
        }
      }
    }

    "has a finish button" which {
      lazy val button = document.select("a.button")

      "links to the feedback page" in {
        button.attr("href") shouldBe controllers.feedback.routes.FeedbackController.show().url
      }

      "has the correct part of the message" in {
        button.text() shouldBe Messages("page.checkAndSubmit.acknowledgement.button.confirm")
      }
    }
  }

}
