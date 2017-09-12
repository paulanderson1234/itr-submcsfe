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

import controllers.seis.routes
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import views.helpers.ViewSpec
import views.html.seis.previousInvestment.HadOtherInvestments
import forms.HadOtherInvestmentsForm._


class HadOtherInvestmentsSpec extends ViewSpec {

  val testBackLink = routes.HadPreviousRFIController.show().url

  "Verify that the HadOtherInvestments page" should {

    "contains the correct elements when a valid empty form is passed" in {
      val document: Document = {
        val page = HadOtherInvestments(hadOtherInvestmentsForm, testBackLink)(fakeRequest, applicationMessages)
        Jsoup.parse(page.body)
      }
      document.title() shouldBe Messages("page.previousInvestment.hadOtherInvestments.title")
      document.body.getElementById("back-link").attr("href") shouldEqual testBackLink
      document.getElementById("main-heading").text() shouldBe Messages("page.previousInvestment.hadOtherInvestments.heading")
      document.select("#hadOtherInvestments-yes").size() shouldBe 1
      document.select("#hadOtherInvestments-no").size() shouldBe 1
      document.getElementById("hadOtherInvestments-yesLabel").text() shouldBe Messages("common.radioYesLabel")
      document.getElementById("hadOtherInvestments-noLabel").text() shouldBe Messages("common.radioNoLabel")
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.details.two")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")

      document.getElementById("hadOtherInvestments-legend").hasClass("visuallyhidden") shouldBe true
      document.getElementById("hadOtherInvestments-legend").text shouldBe Messages("page.previousInvestment.hadOtherInvestments.legend")
      document.select(".error-summary").isEmpty shouldBe true
    }

    "contain the correct elements when a valid populated form is passed" in {
      val document: Document = {
        val page = HadOtherInvestments(hadOtherInvestmentsForm.fill(hadOtherInvestmentsModelYes), testBackLink)(fakeRequest, applicationMessages)
        Jsoup.parse(page.body)
      }
      document.title() shouldBe Messages("page.previousInvestment.hadOtherInvestments.title")
      document.body.getElementById("back-link").attr("href") shouldEqual testBackLink
      document.getElementById("main-heading").text() shouldBe Messages("page.previousInvestment.hadOtherInvestments.heading")
      document.select("#hadOtherInvestments-yes").size() shouldBe 1
      document.select("#hadOtherInvestments-no").size() shouldBe 1
      document.getElementById("hadOtherInvestments-yesLabel").text() shouldBe Messages("common.radioYesLabel")
      document.getElementById("hadOtherInvestments-noLabel").text() shouldBe Messages("common.radioNoLabel")
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.details.two")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")

      document.getElementById("hadOtherInvestments-legend").hasClass("visuallyhidden") shouldBe true
      document.getElementById("hadOtherInvestments-legend").text shouldBe Messages("page.previousInvestment.hadOtherInvestments.legend")
      document.select(".error-summary").isEmpty shouldBe true
    }

    "contain an error summary when a form with errors is passed" in {
      val document: Document = {
        val page = HadOtherInvestments(hadOtherInvestmentsForm.bind(Map("" -> "")), testBackLink)(fakeRequest, applicationMessages)
        Jsoup.parse(page.body)
      }
      document.title() shouldBe Messages("page.previousInvestment.hadOtherInvestments.title")
      document.getElementById("error-summary-display").hasClass("error-summary--show") shouldBe true
      document.getElementById("error-summary-heading").text shouldBe Messages("common.error.summary.heading")
      document.getElementById("hadOtherInvestments-error-summary").text shouldBe Messages("validation.common.error.fieldRequired")
      document.getElementsByClass("error-notification").text shouldBe Messages("validation.common.error.fieldRequired")
    }
  }
}
