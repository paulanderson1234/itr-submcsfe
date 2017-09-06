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
import forms.NatureOfBusinessForm._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._
import views.helpers.ViewSpec
import views.html.seis.companyDetails.NatureOfBusiness

class NatureOfBusinessSpec extends ViewSpec {

  implicit val request = fakeRequest

  "The Nature of business page" should {

    "Verify that the page contains the correct elements when a valid NatureOfBusinessModel is passed" in new SEISSetup {
      val document: Document = Jsoup.parse(contentAsString(NatureOfBusiness(natureOfBusinessForm)))

      document.title() shouldBe Messages("page.companyDetails.natureofbusiness.title")
      document.select(".error-summary--show").isEmpty shouldBe true
      document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.natureofbusiness.heading")
      document.getElementById("label-natureofbusiness").select("span").hasClass("visuallyhidden") shouldBe true
      document.getElementById("label-natureofbusiness").select(".visuallyhidden").text() shouldBe Messages("page.companyDetails.natureofbusiness.heading")
      document.getElementById("label-natureofbusiness-hint").text() shouldBe Messages("page.companyDetails.natureofbusiness.question.hint")
      document.getElementById("description-two").text() shouldBe Messages("page.companyDetails.natureofbusiness.example.text")
      document.getElementById("bullet-one").text() shouldBe Messages("page.companyDetails.natureofbusiness.bullet.one")
      document.getElementById("bullet-two").text() shouldBe Messages("page.companyDetails.natureofbusiness.bullet.two")
      document.getElementById("bullet-three").text() shouldBe Messages("page.companyDetails.natureofbusiness.bullet.three")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.routes.ApplicationHubController.show().url
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.details.one")
      document.select("input").attr("maxLength") shouldBe Constants.shortTextLimit.toString
      document.select("button").attr("type") shouldBe "submit"
      document.select("a.back-link").text() shouldBe Messages("common.button.back")
      document.getElementsByTag("legend").select(".visuallyhidden").text() shouldBe Messages("page.companyDetails.natureofbusiness.heading")
      document.select(".error-summary").isEmpty shouldBe true
    }

    "Verify that the nature of business page contains the correct elements when an invalid NatureOfBusinessModel model is passed" in new SEISSetup {
      val document: Document = Jsoup.parse(contentAsString(NatureOfBusiness(natureOfBusinessForm.bind(Map("natureofbusiness" -> "")))))

      // Check the error summary is displayed - the whole purpose of this test
      document.getElementById("error-summary-display").hasClass("error-summary--show") shouldBe true
      // additional page checks to make sure everything else still as expected if errors on page
      document.title() shouldBe Messages("page.companyDetails.natureofbusiness.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.natureofbusiness.heading")
      document.getElementById("label-natureofbusiness").select("span").hasClass("visuallyhidden") shouldBe true
      document.getElementById("label-natureofbusiness").select(".visuallyhidden").text() shouldBe Messages("page.companyDetails.natureofbusiness.heading")
      document.getElementById("label-natureofbusiness-hint").text() shouldBe Messages("page.companyDetails.natureofbusiness.question.hint")
      document.getElementById("description-two").text() shouldBe Messages("page.companyDetails.natureofbusiness.example.text")
      document.getElementById("bullet-one").text() shouldBe Messages("page.companyDetails.natureofbusiness.bullet.one")
      document.getElementById("bullet-two").text() shouldBe Messages("page.companyDetails.natureofbusiness.bullet.two")
      document.getElementById("bullet-three").text() shouldBe Messages("page.companyDetails.natureofbusiness.bullet.three")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.routes.ApplicationHubController.show().url
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.details.one")
      document.select("input").attr("maxLength") shouldBe Constants.shortTextLimit.toString
      document.select("button").attr("type") shouldBe "submit"
      document.getElementsByTag("legend").select(".visuallyhidden").text() shouldBe Messages("page.companyDetails.natureofbusiness.heading")
      document.select("a.back-link").text() shouldBe Messages("common.button.back")
    }

  }

}
