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
import forms.ContactAddressForm._
import models.AddressModel
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._
import views.helpers.ViewSpec
import views.html.eis.contactInformation.ContactAddress

class ContactAddressSpec extends ViewSpec {

  lazy val errorForm = contactAddressForm.bind(Map("addressline1" -> "ABC XYZ",
    "addressline2" -> "1 ABCDE Street",
    "addressline3" -> "",
    "addressline4" -> "",
    "postcode" -> "",
    "countryCode" -> ""))

  val countriesList: List[(String, String)] = List(("JP", "Japan"), ("GB", "United Kingdom"))
  lazy val emptyPage = ContactAddress(contactAddressForm, countriesList)(authorisedFakeRequest, applicationMessages)
  lazy val errorPage = ContactAddress(errorForm, countriesList)(authorisedFakeRequest, applicationMessages)

  "The Provide Correspondence Address page" should {

    "Verify that the Provide Correspondence Address page contains the correct elements " +
      "when an empty AddressModel is passed" in {

      lazy val document = Jsoup.parse(contentAsString(emptyPage))

      document.title() shouldBe Messages("page.contactInformation.ProvideContactAddress.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.contactInformation.ProvideContactAddress.heading")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.ConfirmCorrespondAddressController.show().url
      document.select("a.back-link").text() shouldBe Messages("common.button.back")
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.details.five")
      document.body.getElementById("get-help-action").text shouldBe Messages("common.error.help.text")
      document.select("form").attr("method") shouldBe "POST"
      document.select("form").attr("action") shouldBe controllers.eis.routes.ContactAddressController.submit().url
      document.select("label#label-addressline1").text() shouldBe Messages("page.contactInformation.ProvideContactAddress.addressline1.label")
      document.select("label#label-addressline1 input").attr("maxlength") shouldBe Constants.addressLineLength.toString
      document.select("label#label-addressline2").text() shouldBe Messages("page.contactInformation.ProvideContactAddress.addressline2.label")
      document.select("label#label-addressline2 input").attr("maxlength") shouldBe Constants.addressLineLength.toString
      document.select("label#label-addressline3").text() shouldBe Messages("page.contactInformation.ProvideContactAddress.addressline3.label")
      document.select("label#label-addressline3 input").attr("maxlength") shouldBe Constants.addressLineLength.toString
      document.select("label#label-addressline4").text() shouldBe Messages("page.contactInformation.ProvideContactAddress.addressline4.label")
      document.select("label#label-addressline4 input").attr("maxlength") shouldBe Constants.addressLineLength.toString
      document.select("label#label-postcode").text() shouldBe Messages("page.contactInformation.ProvideContactAddress.postcode.label")
      document.select("label#label-postcode input").attr("maxlength") shouldBe Constants.postcodeLength.toString
      document.select("label#countryCode_field").text() shouldBe Messages("page.contactInformation.ProvideContactAddress.country.label")
      document.select("error-summary--show").isEmpty shouldBe true

    }

    "Verify that the Provide Correspondence Address page contains the correct elements when an invalid AddressModel is passed" in {

      lazy val document = Jsoup.parse(contentAsString(errorPage))

      document.title() shouldBe Messages("page.contactInformation.ProvideContactAddress.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.contactInformation.ProvideContactAddress.heading")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.ConfirmCorrespondAddressController.show().url
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.details.five")
      document.body.getElementById("get-help-action").text shouldBe Messages("common.error.help.text")
      document.getElementById("error-summary-display").hasClass("error-summary--show")
      document.getElementById("countryCode-error-summary").text should include(Messages("validation.error.countryCode"))
    }
  }
}
