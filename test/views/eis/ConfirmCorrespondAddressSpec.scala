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

import controllers.helpers.BaseSpec
import data.SubscriptionTestData._
import forms.ConfirmCorrespondAddressForm._
import models.ConfirmCorrespondAddressModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._
import views.helpers.ViewSpec
import views.html.eis.contactInformation.ConfirmCorrespondAddress

class ConfirmCorrespondAddressSpec extends ViewSpec with BaseSpec {

  implicit val request = fakeRequest

  "The Confirm Correspondence Address page" should {

    "Verify that the Confirm Correspondence Address page contains the correct elements when no errors occur" in {
      val document: Document = Jsoup.parse(contentAsString(ConfirmCorrespondAddress(confirmCorrespondAddressForm.fill(
        ConfirmCorrespondAddressModel("", expectedContactAddressFull)), "back-link")))

      document.title() shouldBe Messages("page.contactInformation.ConfirmCorrespondAddress.title")
      document.select("error-summary--show").isEmpty shouldBe true
      document.getElementById("main-heading").text() shouldBe Messages("page.contactInformation.ConfirmCorrespondAddress.heading")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("back-link").attr("href") shouldEqual "back-link"
      document.select(".back-link").text() shouldBe  Messages("common.button.back")
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.details.five")
      document.select("form").attr("method") shouldBe "POST"
      document.select("form").attr("action") shouldBe controllers.eis.routes.ConfirmCorrespondAddressController.submit().url
      document.select("legend").text() shouldBe Messages("page.contactInformation.ConfirmCorrespondAddress.heading")
      document.select("legend").hasClass("visuallyhidden") shouldBe true
      document.body.getElementById("contactAddressUse-yesLabel").text shouldBe Messages("common.radioYesLabel")
      document.body.getElementById("contactAddressUse-noLabel").text shouldBe Messages("common.radioNoLabel")
      document.body.select("#contactAddressUse-yes").size() shouldBe 1
      document.body.select("#contactAddressUse-no").size() shouldBe 1
      document.body.getElementById("get-help-action").text shouldBe Messages("common.error.help.text")
      document.body.getElementById("line1-display").text shouldBe expectedContactAddressFull.addressline1
      document.body.getElementById("line2-display").text shouldBe expectedContactAddressFull.addressline2
      document.body.getElementById("line3-display").text shouldBe expectedContactAddressFull.addressline3.getOrElse("")
      document.body.getElementById("line4-display").text shouldBe expectedContactAddressFull.addressline4.getOrElse("")
      document.body.getElementById("postcode-display").text shouldBe expectedContactAddressFull.postcode.getOrElse("")
      document.body.getElementById("country-display").text shouldBe utils.CountriesHelper.getSelectedCountry(expectedContactAddressFull.countryCode)
      document.select("input[id=address.addressline1]").attr("value") shouldBe expectedContactAddressFull.addressline1
      document.select("input[id=address.addressline2]").attr("value") shouldBe expectedContactAddressFull.addressline2
      document.select("input[id=address.addressline3]").attr("value") shouldBe expectedContactAddressFull.addressline3.getOrElse("")
      document.select("input[id=address.addressline4]").attr("value") shouldBe expectedContactAddressFull.addressline4.getOrElse("")
      document.select("input[id=address.postcode]").attr("value") shouldBe expectedContactAddressFull.postcode.getOrElse("")
      document.select("input[id=address.countryCode]").attr("value") shouldBe expectedContactAddressFull.countryCode
    }

    "Verify that the Confirm Correspondence Address page contains the correct elements when an error is present" in {
      val document: Document = Jsoup.parse(contentAsString(ConfirmCorrespondAddress(confirmCorrespondAddressForm.bind(Map("" -> "")), "back-link")))

      document.title() shouldBe Messages("page.contactInformation.ConfirmCorrespondAddress.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.contactInformation.ConfirmCorrespondAddress.heading")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("back-link").attr("href") shouldEqual "back-link"
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.details.five")
      document.body.getElementById("contactAddressUse-yesLabel").text shouldBe Messages("common.radioYesLabel")
      document.body.getElementById("contactAddressUse-noLabel").text shouldBe Messages("common.radioNoLabel")
      document.body.select("#contactAddressUse-yes").size() shouldBe 1
      document.body.select("#contactAddressUse-no").size() shouldBe 1
      document.body.getElementById("get-help-action").text shouldBe Messages("common.error.help.text")
      document.getElementById("error-summary-display").hasClass("error-summary--show") shouldBe true
      document.body.getElementById("line1-display").text shouldBe ""
      document.body.getElementById("line2-display").text shouldBe ""
      document.body.getElementById("line3-display").text shouldBe ""
      document.body.getElementById("line4-display").text shouldBe ""
      document.body.getElementById("postcode-display").text shouldBe ""
      document.body.getElementById("country-display").text shouldBe ""
    }
  }
}
