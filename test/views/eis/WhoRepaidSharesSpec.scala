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

import forms.WhoRepaidSharesForm._
import models.WhoRepaidSharesModel
import org.jsoup.Jsoup
import play.api.data.Form
import play.api.i18n.Messages
import views.helpers.ViewSpec
import play.api.i18n.Messages.Implicits._
import controllers.eis.routes
import views.html.eis.investors.WhoRepaidShares

class WhoRepaidSharesSpec extends ViewSpec {

  val page = (form: Form[WhoRepaidSharesModel]) =>
    WhoRepaidShares(form)(fakeRequest, applicationMessages)

    "The WhoRepaidShares page" should {
    "contain the correct elements for a GET when a valid WhoRepaidSharesModel is loaded" in new Setup {
      val document = Jsoup.parse(page(whoRepaidSharesForm.fill(WhoRepaidSharesModel("Bill", "Smith"))).body)
      document.title() shouldBe Messages("page.WhoRepaidShares.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.WhoRepaidShares.heading")
      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.details.four")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.WhoRepaidSharesController.show().url
      document.getElementsByTag("legend").select(".visuallyhidden").text() shouldBe Messages("page.WhoRepaidShares.heading")
      document.getElementById("label-forename").text() shouldBe Messages("common.page.forename.label")
      document.getElementById("label-surname").text() shouldBe Messages("common.page.surname.label")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.select(".error-summary").isEmpty shouldBe true

    }

    "contain the correct elements when the model is empty and no WhoRepaidSharesModel is loaded" in new Setup {
      val document = Jsoup.parse(page(whoRepaidSharesForm).body)
      document.title() shouldBe Messages("page.WhoRepaidShares.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.WhoRepaidShares.heading")
      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.details.four")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.WhoRepaidSharesController.show().url
      document.getElementsByTag("legend").select(".visuallyhidden").text() shouldBe Messages("page.WhoRepaidShares.heading")
      document.getElementById("label-forename").text() shouldBe Messages("common.page.forename.label")
      document.getElementById("label-surname").text() shouldBe Messages("common.page.surname.label")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.select(".error-summary").isEmpty shouldBe true

    }

    "contain the correct elements including an error sumary when an invalid WhoRepaidSharesModel is loaded" in new Setup {
      val document = Jsoup.parse(page(whoRepaidSharesForm.bindFromRequest()(fakeRequest.withHeaders("" -> ""))).body)
      document.title() shouldBe Messages("page.WhoRepaidShares.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.WhoRepaidShares.heading")
      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.details.four")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.WhoRepaidSharesController.show().url
      document.getElementsByTag("legend").select(".visuallyhidden").text() shouldBe Messages("page.WhoRepaidShares.heading")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")

      // Check error present:
      document.getElementById("error-summary-display").hasClass("error-summary--show") shouldBe true


    }

  }

}
