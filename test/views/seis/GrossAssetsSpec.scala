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
import forms.GrossAssetsForm._
import models.GrossAssetsModel
import org.jsoup.Jsoup
import play.api.data.Form
import play.api.i18n.Messages
import views.helpers.ViewSpec
import play.api.i18n.Messages.Implicits._
import views.html.seis.companyDetails.GrossAssets

class GrossAssetsSpec extends ViewSpec {

  val page = (form: Form[GrossAssetsModel]) =>
    GrossAssets(form)(fakeRequest, applicationMessages)

  "The gross assets page" should {

    "Verify that the gross assets page contains the correct elements when a valid GrossAssetsModel is passed" in new SEISSetup {
      val document = Jsoup.parse(page(grossAssetsForm.fill(GrossAssetsModel(200000))).body)
      document.title() shouldBe Messages("page.grossAssets.amount.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.grossAssets.amount.heading")
      document.getElementById("label-amount").select("span").hasClass("visuallyhidden") shouldBe true
      document.getElementById("label-amount").select(".visuallyhidden").text() shouldBe Messages("page.grossAssets.amount.heading")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.GrossAssetsController.show().url
      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.one")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
    }

    "Verify that the gross assets page contains the correct elements when an invalid GrossAssetsModel is passed" in new SEISSetup {
      val document = Jsoup.parse(page(grossAssetsForm.bindFromRequest()(fakeRequest.withHeaders("" -> ""))).body)
      document.title() shouldBe Messages("page.grossAssets.amount.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.grossAssets.amount.heading")
      document.getElementById("label-amount").select("span").hasClass("visuallyhidden") shouldBe true
      document.getElementById("label-amount").select(".visuallyhidden").text() shouldBe Messages("page.grossAssets.amount.heading")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.GrossAssetsController.show().url
      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.one")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.getElementById("error-summary-display").hasClass("error-summary--show")
    }

    "Verify that the gross assets page contains the correct elements when an empty GrossAssetsModel is passed" in new SEISSetup {
      val document = Jsoup.parse(page(grossAssetsForm).body)
      document.title() shouldBe Messages("page.grossAssets.amount.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.grossAssets.amount.heading")
      document.getElementById("label-amount").select("span").hasClass("visuallyhidden") shouldBe true
      document.getElementById("label-amount").select(".visuallyhidden").text() shouldBe Messages("page.grossAssets.amount.heading")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.GrossAssetsController.show().url
      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.one")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")

    }
  }

}
