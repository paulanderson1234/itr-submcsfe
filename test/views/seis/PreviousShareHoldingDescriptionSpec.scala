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

import common.Constants
import controllers.helpers.BaseSpec
import forms.PreviousShareHoldingDescriptionForm._
import models.investorDetails.PreviousShareHoldingDescriptionModel
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import views.html.seis.investors.PreviousShareHoldingDescription

class PreviousShareHoldingDescriptionSpec  extends BaseSpec {

  "The previous shareholding dscription page" should {

    "contain the correct elements for an investor (company)" in {

      lazy val page = PreviousShareHoldingDescription(Constants.typeCompany,
        previousShareHoldingDescriptionForm.fill(PreviousShareHoldingDescriptionModel("test", Some(1), Some(2))),"/test/testing", 2)(fakeRequest, applicationMessages)
      lazy val document = Jsoup.parse(page.body)

      //title and heading
      document.title() shouldBe Messages("page.investors.previousShareHoldingDescription.heading", Constants.typeCompany.toLowerCase())
      document.title() shouldBe "What were the shares issued to this company called?"

      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.details.four")

      document.select("h1").text() shouldBe
        Messages("page.investors.previousShareHoldingDescription.heading", Constants.typeCompany.toLowerCase())
      document.select("h1").text() shouldBe "What were the shares issued to this company called?"

      document.body.getElementById("description-one").text() shouldBe Messages("page.investors.previousShareHoldingDescription.example.text")
      document.body.getElementById("help-bullet-one").text() shouldBe Messages("page.investors.previousShareHoldingDescription.bullet.one")
      document.body.getElementById("help-bullet-two").text() shouldBe Messages("page.investors.previousShareHoldingDescription.bullet.two")
      document.body.getElementById("desc-one").text() shouldBe Messages("page.investors.previousShareHoldingDescription.question.hint")

      document.body.getElementById("share-description-where-to-find").text() shouldBe  Messages("page.investors.previousShareHoldingDescription.location")
      document.body.getElementById("help").text() shouldBe  Messages("common.help.whereToFind")

      document.getElementById("labelTextId").text() shouldBe
        Messages("page.investors.previousShareHoldingDescription.heading", Constants.typeCompany.toLowerCase())
      document.getElementById("labelTextId").text() shouldBe "What were the shares issued to this company called?"

      document.select("fieldset legend").text() shouldBe Messages("page.investors.previousShareHoldingDescription.heading", Constants.typeCompany.toLowerCase())
      document.select("fieldset legend")hasClass "visuallyhidden" shouldBe true

      document.body.getElementById("processingId").attr("value") shouldBe "1"
      document.body.getElementById("processingId").attr("type") shouldBe "hidden"
      document.body.getElementById("investorProcessingId").attr("value") shouldBe "2"
      document.body.getElementById("investorProcessingId").attr("type") shouldBe "hidden"
      document.body.getElementById("descriptionTextArea").attr("maxlength") shouldBe s"${Constants.shortTextLimit}"
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.select(".error-summary").isEmpty shouldBe true

    }

    "contain the correct elements for an investor (individual)" in {

      lazy val page = PreviousShareHoldingDescription(Constants.typeIndividual,
        previousShareHoldingDescriptionForm.fill(PreviousShareHoldingDescriptionModel("test", Some(1), Some(2))),"/test/testing", 2)(fakeRequest, applicationMessages)
      lazy val document = Jsoup.parse(page.body)

      //title and heading
      document.title() shouldBe Messages("page.investors.previousShareHoldingDescription.heading", Constants.typeIndividual.toLowerCase())
      document.title() shouldBe "What were the shares issued to this individual called?"

      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.details.four")

      document.select("h1").text() shouldBe
        Messages("page.investors.previousShareHoldingDescription.heading", Constants.typeIndividual.toLowerCase())
      document.select("h1").text() shouldBe "What were the shares issued to this individual called?"

      document.body.getElementById("description-one").text() shouldBe Messages("page.investors.previousShareHoldingDescription.example.text")
      document.body.getElementById("help-bullet-one").text() shouldBe Messages("page.investors.previousShareHoldingDescription.bullet.one")
      document.body.getElementById("help-bullet-two").text() shouldBe Messages("page.investors.previousShareHoldingDescription.bullet.two")
      document.body.getElementById("desc-one").text() shouldBe Messages("page.investors.previousShareHoldingDescription.question.hint")

      document.body.getElementById("share-description-where-to-find").text() shouldBe  Messages("page.investors.previousShareHoldingDescription.location")
      document.body.getElementById("help").text() shouldBe  Messages("common.help.whereToFind")

      document.getElementById("labelTextId").text() shouldBe
        Messages("page.investors.previousShareHoldingDescription.heading", Constants.typeIndividual.toLowerCase())
      document.getElementById("labelTextId").text() shouldBe "What were the shares issued to this individual called?"

      document.select("fieldset legend").text() shouldBe Messages("page.investors.previousShareHoldingDescription.heading", Constants.typeIndividual.toLowerCase())
      document.select("fieldset legend")hasClass "visuallyhidden" shouldBe true

      document.body.getElementById("processingId").attr("value") shouldBe "1"
      document.body.getElementById("processingId").attr("type") shouldBe "hidden"
      document.body.getElementById("investorProcessingId").attr("value") shouldBe "2"
      document.body.getElementById("investorProcessingId").attr("type") shouldBe "hidden"
      document.body.getElementById("descriptionTextArea").attr("maxlength") shouldBe s"${Constants.shortTextLimit}"
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.select(".error-summary").isEmpty shouldBe true

    }


    "contain the correct elements for an investor (individual) with dynamic text lower cased as expected" in {

      lazy val upperCaseTest = "InDIViduAl"
      lazy val page = PreviousShareHoldingDescription(upperCaseTest,
        previousShareHoldingDescriptionForm.fill(PreviousShareHoldingDescriptionModel("test", Some(1), Some(2))),"/test/testing", 2)(fakeRequest, applicationMessages)
      lazy val document = Jsoup.parse(page.body)

      //title and heading
      document.title() shouldBe Messages("page.investors.previousShareHoldingDescription.heading", upperCaseTest.toLowerCase())
      document.title() shouldBe "What were the shares issued to this individual called?"

      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.details.four")

      document.select("h1").text() shouldBe
        Messages("page.investors.previousShareHoldingDescription.heading", upperCaseTest.toLowerCase())
      document.select("h1").text() shouldBe "What were the shares issued to this individual called?"

      document.body.getElementById("description-one").text() shouldBe Messages("page.investors.previousShareHoldingDescription.example.text")
      document.body.getElementById("help-bullet-one").text() shouldBe Messages("page.investors.previousShareHoldingDescription.bullet.one")
      document.body.getElementById("help-bullet-two").text() shouldBe Messages("page.investors.previousShareHoldingDescription.bullet.two")
      document.body.getElementById("desc-one").text() shouldBe Messages("page.investors.previousShareHoldingDescription.question.hint")

      document.body.getElementById("share-description-where-to-find").text() shouldBe  Messages("page.investors.previousShareHoldingDescription.location")
      document.body.getElementById("help").text() shouldBe  Messages("common.help.whereToFind")

      document.getElementById("labelTextId").text() shouldBe
        Messages("page.investors.previousShareHoldingDescription.heading", upperCaseTest.toLowerCase())
      document.getElementById("labelTextId").text() shouldBe "What were the shares issued to this individual called?"

      document.select("fieldset legend").text() shouldBe Messages("page.investors.previousShareHoldingDescription.heading", upperCaseTest.toLowerCase())
      document.select("fieldset legend")hasClass "visuallyhidden" shouldBe true

      document.body.getElementById("processingId").attr("value") shouldBe "1"
      document.body.getElementById("processingId").attr("type") shouldBe "hidden"
      document.body.getElementById("investorProcessingId").attr("value") shouldBe "2"
      document.body.getElementById("investorProcessingId").attr("type") shouldBe "hidden"
      document.body.getElementById("descriptionTextArea").attr("maxlength") shouldBe s"${Constants.shortTextLimit}"
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.select(".error-summary").isEmpty shouldBe true

    }

  }

}
