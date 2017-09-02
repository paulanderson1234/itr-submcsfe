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

import auth.{MockAuthConnector, MockConfigSingleFlow}
import common.{Constants, KeystoreKeys}
import controllers.seis.{ContactDetailsController, routes}
import forms.ContactDetailsForm._
import models.ContactDetailsModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._
import views.helpers.ViewSpec
import views.html.seis.contactInformation.ContactDetails

import scala.concurrent.Future

class ContactDetailsSpec extends ViewSpec {

  implicit val request = fakeRequest

  "The Contact Details page" should {

    "Verify that the contact details page contains the correct elements when a valid ContactDetailsModel is passed" in new SEISSetup {
      val document: Document = Jsoup.parse(contentAsString(ContactDetails(contactDetailsForm)))

      document.title() shouldBe Messages("page.contactInformation.contactDetails.title")
      document.select("error-summary--show").isEmpty shouldBe true
      document.getElementById("main-heading").text() shouldBe Messages("page.contactInformation.contactDetails.heading")
      document.select("form").attr("method") shouldBe "POST"
      document.select("form").attr("action") shouldBe controllers.seis.routes.ContactDetailsController.submit().url
      document.getElementById("label-forename").text() shouldBe Messages("page.contactInformation.contactDetails.forename.label")
      document.select("#label-forename input").attr("maxlength") shouldBe Constants.forenameLength.toString
      document.getElementById("label-surname").text() shouldBe Messages("page.contactInformation.contactDetails.surname.label")
      document.select("#label-surname input").attr("maxlength") shouldBe Constants.surnameLength.toString
      document.getElementById("label-telephoneNumber").text() shouldBe Messages("page.contactInformation.contactDetails.phoneNumber.label")
      document.select("#label-telephoneNumber input").attr("maxlength") shouldBe Constants.phoneLength.toString
      document.getElementById("label-mobileNumber").text() shouldBe Messages("page.contactInformation.contactDetails.mobileNumber.label")
      document.select("#label-mobileNumber input").attr("maxlength") shouldBe Constants.phoneLength.toString
      document.getElementById("label-email").text() shouldBe Messages("page.contactInformation.contactDetails.email.label")
      document.select("#label-email input").attr("maxlength") shouldBe Constants.emailLength.toString
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.seis.routes.ConfirmContactDetailsController.show().url
      document.select("a.back-link").text() shouldBe Messages("common.button.back")
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.details.five")
    }

    "Verify that the proposed investment page contains the correct elements when an invalid ContactDetailsModel is passed" in new SEISSetup {
      val document: Document = Jsoup.parse(contentAsString(ContactDetails(contactDetailsForm.bind(Map("" -> "")))))

      document.title() shouldBe Messages("page.contactInformation.contactDetails.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.contactInformation.contactDetails.heading")
      document.getElementById("label-forename").text() contains Messages("page.contactInformation.contactDetails.forename.label")
      document.getElementById("label-surname").text() contains Messages("page.contactInformation.contactDetails.surname.label")
      document.getElementById("label-telephoneNumber").text() contains Messages("page.contactInformation.contactDetails.phoneNumber.label")
      document.getElementById("label-mobileNumber").text() shouldBe Messages("page.contactInformation.contactDetails.mobileNumber.label")
      document.getElementById("label-email").text() contains Messages("page.contactInformation.contactDetails.email.label")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.ConfirmContactDetailsController.show().url
      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.details.five")
      document.getElementById("error-summary-display").hasClass("error-summary--show")
    }

  }

}
