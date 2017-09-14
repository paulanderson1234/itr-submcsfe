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
import forms.GrossAssetsAfterIssueForm._
import models.GrossAssetsAfterIssueModel
import org.jsoup.Jsoup
import play.api.data.Form
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import views.helpers.ViewSpec
import views.html.eis.companyDetails.GrossAssetsAfterIssue

class GrossAssetsAfterIssueSpec extends ViewSpec {

  val grossAssetsAmount = 200000

  val page = (form: Form[GrossAssetsAfterIssueModel]) =>
    GrossAssetsAfterIssue(form)(fakeRequest, applicationMessages)

  "The gross assets page" should {

    "Verify that the gross assets page contains the correct elements when a populated form is passed" in {
      val document = Jsoup.parse(page(grossAssetsAfterIssueForm.fill(GrossAssetsAfterIssueModel(grossAssetsAmount))).body)
      document.title() shouldBe Messages("page.grossAssetsAfterIssue.amount.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.grossAssetsAfterIssue.amount.heading")
      document.getElementById("label-amount").select("span").hasClass("visuallyhidden") shouldBe true
      document.getElementById("label-amount").select(".visuallyhidden").text() shouldBe Messages("page.grossAssetsAfterIssue.amount.heading")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.GrossAssetsController.show().url
      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.details.one")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.select(".error-summary").isEmpty shouldBe true
    }


    "Verify that the gross assets page contains the correct elements when an empty form is passed" in  {
      val document = Jsoup.parse(page(grossAssetsAfterIssueForm).body)
      document.title() shouldBe Messages("page.grossAssetsAfterIssue.amount.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.grossAssetsAfterIssue.amount.heading")
      document.getElementById("label-amount").select("span").hasClass("visuallyhidden") shouldBe true
      document.getElementById("label-amount").select(".visuallyhidden").text() shouldBe Messages("page.grossAssetsAfterIssue.amount.heading")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.GrossAssetsController.show().url
      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.details.one")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.select(".error-summary").isEmpty shouldBe true

    }

    "Verify that the gross assets page contains the correct elements when an invalid form with errors is passed" in  {
      val document = Jsoup.parse(page(grossAssetsAfterIssueForm.bindFromRequest()(fakeRequest.withHeaders("" -> ""))).body)
      document.title() shouldBe Messages("page.grossAssetsAfterIssue.amount.title")
      document.getElementById("error-summary-display").hasClass("error-summary--show") shouldBe true
      document.getElementById("error-summary-heading").text shouldBe Messages("common.error.summary.heading")
      document.getElementById("grossAmount-error-summary").text shouldBe Messages("validation.common.error.fieldRequired")
      document.getElementsByClass("error-notification").text shouldBe Messages("validation.common.error.fieldRequired")
    }
  }

}
