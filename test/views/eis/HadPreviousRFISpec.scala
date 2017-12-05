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

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import views.helpers.ViewSpec
import views.html.eis.previousInvestment.HadPreviousRFI
import forms.HadPreviousRFIForm._
import controllers.eis.routes

class HadPreviousRFISpec extends ViewSpec {

  "Verify that the HadPreviousRFI page" should {

    "contain the correct elements when an empty form is passed to the view" in {
      val document : Document = {
        val page = HadPreviousRFI(hadPreviousRFIForm)(fakeRequest, applicationMessages)
        Jsoup.parse(page.body)
      }
      document.body.getElementById("back-link").attr("href") shouldEqual routes.FullTimeEmployeeCountController.show().url
      document.title() shouldBe Messages("page.previousInvestment.hadPreviousRFI.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.previousInvestment.hadPreviousRFI.heading")
      document.getElementById("bullet-heading").text() shouldBe Messages("page.previousInvestment.hadPreviousRFI.hintTitle")
      document.getElementById("bullet-one").text() shouldBe Messages("page.previousInvestment.schemes.bullet.one")
      document.getElementById("bullet-two").text() shouldBe Messages("page.previousInvestment.schemes.bullet.two")
      document.getElementById("bullet-three").text() shouldBe Messages("page.previousInvestment.schemes.bullet.three")
      document.getElementById("bullet-four").text() shouldBe Messages("page.previousInvestment.schemes.bullet.four")
      document.select("#hadPreviousRFI-yes").size() shouldBe 1
      document.select("#hadPreviousRFI-yes").size() shouldBe 1
      document.select("label[for=hadPreviousRFI-yes]").text() shouldBe Messages("common.radioYesLabel")
      document.select("label[for=hadPreviousRFI-no]").text() shouldBe Messages("common.radioNoLabel")
      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.details.two")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.select("legend").text() shouldBe Messages("page.previousInvestment.hadPreviousRFI.legend")
      document.select("legend").hasClass("visuallyhidden") shouldBe true
      document.select(".error-summary").first().getElementsByAttributeValueContaining("style", "display").isEmpty shouldBe true
    }

    "contain the correct elements when a populated form is passed to the view" in {
      val document : Document = {
        val page = HadPreviousRFI(hadPreviousRFIForm.fill(hadPreviousRFIModelYes))(fakeRequest, applicationMessages)
        Jsoup.parse(page.body)
      }
      document.body.getElementById("back-link").attr("href") shouldEqual routes.FullTimeEmployeeCountController.show().url
      document.title() shouldBe Messages("page.previousInvestment.hadPreviousRFI.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.previousInvestment.hadPreviousRFI.heading")
      document.getElementById("bullet-heading").text() shouldBe Messages("page.previousInvestment.hadPreviousRFI.hintTitle")
      document.getElementById("bullet-one").text() shouldBe Messages("page.previousInvestment.schemes.bullet.one")
      document.getElementById("bullet-two").text() shouldBe Messages("page.previousInvestment.schemes.bullet.two")
      document.getElementById("bullet-three").text() shouldBe Messages("page.previousInvestment.schemes.bullet.three")
      document.getElementById("bullet-four").text() shouldBe Messages("page.previousInvestment.schemes.bullet.four")
      document.select("#hadPreviousRFI-yes").size() shouldBe 1
      document.select("#hadPreviousRFI-no").size() shouldBe 1
      document.select("label[for=hadPreviousRFI-yes]").text() shouldBe Messages("common.radioYesLabel")
      document.select("label[for=hadPreviousRFI-no]").text() shouldBe Messages("common.radioNoLabel")
      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.details.two")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")

      document.select("legend").text() shouldBe Messages("page.previousInvestment.hadPreviousRFI.legend")
      document.select("legend").hasClass("visuallyhidden") shouldBe true
      document.select(".error-summary").first().getElementsByAttributeValueContaining("style", "display").isEmpty shouldBe true

    }

    "contain an error summary when a form with errors is passed to the view" in  {
      val document : Document = {
        val page = HadPreviousRFI(hadPreviousRFIForm.bind(Map("" -> "")))(fakeRequest, applicationMessages)
        Jsoup.parse(page.body)
      }
      document.title() shouldBe Messages("page.previousInvestment.hadPreviousRFI.title")
      document.getElementById("error-summary-display").hasClass("error-summary--show") shouldBe true
      document.getElementById("error-summary-heading").text shouldBe Messages("common.error.summary.heading")
      document.getElementById("hadPreviousRFI-error-summary").text shouldBe Messages("validation.common.error.fieldRequired")
      document.getElementsByClass("error-notification").text shouldBe Messages("validation.common.error.fieldRequired")
    }
  }
}
