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


import controllers.eis.routes
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import views.helpers.ViewSpec
import views.html.eis.companyDetails.ShareIssueDate
import forms.ShareIssueDateForm._

class ShareIssueDateSpec extends ViewSpec {

  "The Share Issue Date page" should {

    "Verify that Share Issue Date page contains the correct elements when a valid form is passed" in {
      val page = ShareIssueDate(shareIssueDateForm)(fakeRequest, applicationMessages)
      val document = Jsoup.parse(page.body)

      document.title() shouldBe Messages("page.companyDetails.ShareIssueDate.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.ShareIssueDate.heading")
      document.body.getElementsByClass("form-hint").text should include(Messages("common.date.hint.example"))
      document.body.getElementById("shareIssueDay").parent.text shouldBe Messages("common.date.fields.day")
      document.body.getElementById("shareIssueMonth").parent.text shouldBe Messages("common.date.fields.month")
      document.body.getElementById("shareIssueYear").parent.text shouldBe Messages("common.date.fields.year")
      document.body.getElementById("date-of-shareIssue-where-to-find").parent.text should include
        Messages("page.companyDetails.ShareIssueDate.location")

      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.CommercialSaleController.show().url
      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.details.one")

      document.getElementById("question-date-text-legend-id").hasClass("visuallyhidden") shouldBe true
      document.getElementById("question-date-text-legend-id").text shouldBe Messages("page.companyDetails.ShareIssueDate.legend")

      document.select(".error-summary").isEmpty shouldBe true
    }

    "Verify that the Share Issue Date page contains the correct elements when a form with errors passed" in {
      val page = ShareIssueDate(shareIssueDateForm.bind(Map("" -> "")))(fakeRequest, applicationMessages)
      val document = Jsoup.parse(page.body)

      document.title() shouldBe Messages("page.companyDetails.ShareIssueDate.title")
      document.getElementById("back-link").attr("href") shouldBe routes.CommercialSaleController.show().url
      document.getElementById("error-summary-display").hasClass("error-summary--show") shouldBe true
      document.getElementById("error-summary-heading").text shouldBe Messages("common.error.summary.heading")
      document.getElementById("shareIssueDay-error-summary").text shouldBe Messages("validation.error.DateNotEntered")
      document.getElementsByClass("error-notification").text shouldBe Messages("validation.error.DateNotEntered")

    }

  }

}
