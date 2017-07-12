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
import forms.CompanyDetailsForm._
import models.CompanyDetailsModel
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._
import views.helpers.ViewSpec
import views.html.seis.investment.CompanyDetails

class CompanyDetailsSpec extends ViewSpec {

  val emptyCompanyDetailsModel = new CompanyDetailsModel("", "", "", None, None, None, countryCode = "")

  lazy val form = companyDetailsForm.bind(Map("name" -> "Line 0",
    "addressline1" -> "Line 1",
    "addressline2" -> "Line 2",
    "addressline3" -> "",
    "addressline4" -> "",
    "postcode" -> "",
    "countryCode" -> "JP"))

  lazy val emptyForm = companyDetailsForm.bind(Map("name" -> "",
    "addressline1" -> "",
    "addressline2" -> "",
    "addressline3" -> "",
    "addressline4" -> "",
    "postcode" -> "",
    "countryCode" -> ""))

  lazy val errorForm = companyDetailsForm.bind(Map("name" -> "ABCorp",
    "addressline1" -> "ABC XYZ",
    "addressline2" -> "1 ABCDE Street",
    "addressline3" -> "",
    "addressline4" -> "",
    "postcode" -> "",
    "countryCode" -> ""))

  val countriesList: List[(String, String)] = List(("JP", "Japan"), ("GB", "United Kingdom"))
  lazy val page = CompanyDetails(form, countriesList)(authorisedFakeRequest,applicationMessages)
  lazy val emptyPage = CompanyDetails(emptyForm, countriesList)(authorisedFakeRequest, applicationMessages)
  lazy val errorPage = CompanyDetails(errorForm, countriesList)(authorisedFakeRequest, applicationMessages)

  "The Company Details page" should {

    "Verify that the Company Details page contains the correct elements when a valid CompanyDetailsModel is passed" in {

      lazy val document = {
        Jsoup.parse(contentAsString(page))
      }

      document.title() shouldBe Messages("page.investment.companyDetails.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.investment.companyDetails.heading")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.CompanyDetailsController.show().url
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.four")
      document.body.getElementById("name").`val`() shouldBe companyDetailsModel.name
      document.body.getElementById("addressline1").`val`() shouldBe companyDetailsModel.addressline1
      document.body.getElementById("addressline2").`val`() shouldBe companyDetailsModel.addressline2
      document.body.getElementById("addressline3").`val`() shouldBe ""
      document.body.getElementById("addressline4").`val`() shouldBe ""
      document.body.getElementById("postcode").`val`() shouldBe ""
      document.body.select("select[name=countryCode] option[selected]").`val`() shouldBe companyDetailsModel.countryCode
      document.body.getElementById("get-help-action").text shouldBe Messages("common.error.help.text")
    }

    "Verify that the Company Details page contains the correct elements " +
      "when an empty CompanyDetailsModel is passed" in {

      lazy val document = {
        Jsoup.parse(contentAsString(emptyPage))
      }

      document.title() shouldBe Messages("page.investment.companyDetails.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.investment.companyDetails.heading")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.CompanyDetailsController.show().url
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.four")
      document.body.getElementById("get-help-action").text shouldBe Messages("common.error.help.text")
      document.getElementById("error-summary-display").hasClass("error-summary--show")
      document.getElementById("countryCode-error-summary").text should include(Messages("validation.error.countryCode"))
      document.getElementById("addressline1-error-summary").text should include(Messages("validation.error.mandatoryaddresssline"))
      document.getElementById("addressline2-error-summary").text should include(Messages("validation.error.mandatoryaddresssline"))
    }

    "Verify that the Company Details page contains the correct elements " +
      "when an invalid CompanyDetailsModel is passed" in {

      lazy val document = {
        Jsoup.parse(contentAsString(errorPage))
      }
      document.title() shouldBe Messages("page.investment.companyDetails.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.investment.companyDetails.heading")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.CompanyDetailsController.show().url
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.four")
      document.body.getElementById("get-help-action").text shouldBe Messages("common.error.help.text")
      document.getElementById("error-summary-display").hasClass("error-summary--show")
      document.getElementById("countryCode-error-summary").text should include(Messages("validation.error.countryCode"))
    }
  }
}
