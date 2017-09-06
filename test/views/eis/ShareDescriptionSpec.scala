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
import controllers.helpers.MockDataGenerator
import models.ShareDescriptionModel
import org.jsoup.Jsoup
import play.api.i18n.Messages
import views.helpers.ViewSpec
import play.api.i18n.Messages.Implicits._
import forms.ShareDescriptionForm._
import views.html.eis.shareDetails.ShareDescription

class ShareDescriptionSpec extends ViewSpec {

  "The share description page" should {
    "show the correct elements" when {
      "there is no share description model" in {
        val page = ShareDescription(shareDescriptionForm,
          controllers.eis.routes.HadOtherInvestmentsController.show().toString)(authorisedFakeRequest, applicationMessages)
        val document = Jsoup.parse(page.body)

        document.title() shouldBe Messages("page.shares.shareDescription.title")
        document.getElementById("main-heading").text() shouldBe Messages("page.shares.shareDescription.heading")
        document.getElementById("back-link").text() shouldBe Messages("common.button.back")
        document.getElementById("back-link").attr("href") shouldBe controllers.eis.routes.HadOtherInvestmentsController.show().url
        document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.details.three")
        document.getElementById("description-two").text() shouldBe Messages("page.shares.shareDescription.example.text")
        document.getElementById("bullet-one").text() shouldBe Messages("page.shares.shareDescription.bullet.one")
        document.getElementById("bullet-two").text() shouldBe Messages("page.shares.shareDescription.bullet.two")
        document.getElementById("bullet-three").text() shouldBe Messages("page.shares.shareDescription.bullet.three")
        document.getElementById("desc-one").text() shouldBe Messages("page.shares.shareDescription.question.hint")
        document.getElementsByTag("legend").text() shouldBe Messages("page.shares.shareDescription.title")
        document.getElementsByTag("legend").hasClass("visuallyhidden") shouldBe true
        document.getElementById("labelTextId").text() shouldBe Messages("page.shares.shareDescription.heading")
        document.getElementById("labelTextId").hasClass("visuallyhidden") shouldBe true
        document.body.getElementById("descriptionTextArea").attr("maxlength") shouldBe s"${Constants.shortTextLimit}"
        document.getElementsByTag("textarea").attr("name") shouldBe "descriptionTextArea"
        document.getElementById("help").text() shouldBe Messages("common.help.whereToFind")
        document.getElementById("share-description-where-to-find").text() shouldBe Messages("page.shares.ShareDescription.location")
        document.getElementById("next").text() shouldBe Messages("common.button.snc")

      }

      "there is a share description model" in {
        lazy val maxLengthText: String = MockDataGenerator.randomAlphanumericString(Constants.shortTextLimit)

        val shareDescriptionModel = ShareDescriptionModel(maxLengthText)
        val page = ShareDescription(shareDescriptionForm.fill(shareDescriptionModel),
          controllers.eis.routes.HadOtherInvestmentsController.show().toString)(authorisedFakeRequest, applicationMessages)
        val document = Jsoup.parse(page.body)

        document.title() shouldBe Messages("page.shares.shareDescription.title")
        document.getElementById("main-heading").text() shouldBe Messages("page.shares.shareDescription.heading")
        document.getElementById("back-link").text() shouldBe Messages("common.button.back")
        document.getElementById("back-link").attr("href") shouldBe controllers.eis.routes.HadOtherInvestmentsController.show().url
        document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.details.three")
        document.getElementById("description-two").text() shouldBe Messages("page.shares.shareDescription.example.text")
        document.getElementById("bullet-one").text() shouldBe Messages("page.shares.shareDescription.bullet.one")
        document.getElementById("bullet-two").text() shouldBe Messages("page.shares.shareDescription.bullet.two")
        document.getElementById("bullet-three").text() shouldBe Messages("page.shares.shareDescription.bullet.three")
        document.getElementById("desc-one").text() shouldBe Messages("page.shares.shareDescription.question.hint")
        document.getElementsByTag("legend").text() shouldBe Messages("page.shares.shareDescription.title")
        document.getElementsByTag("legend").hasClass("visuallyhidden") shouldBe true
        document.getElementById("labelTextId").text() shouldBe Messages("page.shares.shareDescription.heading")
        document.getElementById("labelTextId").hasClass("visuallyhidden") shouldBe true
        document.body.getElementById("descriptionTextArea").attr("maxlength") shouldBe s"${Constants.shortTextLimit}"
        document.body.getElementById("descriptionTextArea").text() shouldBe maxLengthText
        document.getElementsByTag("textarea").attr("name") shouldBe "descriptionTextArea"
        document.getElementById("help").text() shouldBe Messages("common.help.whereToFind")
        document.getElementById("share-description-where-to-find").text() shouldBe Messages("page.shares.ShareDescription.location")
        document.getElementById("next").text() shouldBe Messages("common.button.snc")

      }

      "Verify that the Share Description page contains the correct elements when an invalid ShareDescriptionModel is passed" in {
        val page = ShareDescription(shareDescriptionForm.bind(Map("descriptionTextArea" -> "")),
          controllers.eis.routes.ReviewPreviousSchemesController.show().toString)(authorisedFakeRequest, applicationMessages)
        val document = Jsoup.parse(page.body)

        document.title() shouldBe Messages("page.shares.shareDescription.title")
        document.getElementById("back-link").attr("href") shouldBe controllers.eis.routes.ReviewPreviousSchemesController.show().url
        document.getElementById("error-summary-display").hasClass("error-summary--show") shouldBe true
        document.getElementById("error-summary-heading").text shouldBe Messages("common.error.summary.heading")
        document.getElementById("descriptionTextArea-error-summary").text shouldBe Messages("common.error.fieldRequired")
        document.getElementsByClass("error-notification").text shouldBe Messages("common.error.fieldRequired")
      }
    }
  }
}
