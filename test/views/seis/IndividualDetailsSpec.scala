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

import controllers.helpers.FakeRequestHelper
import forms.IndividualDetailsForm.individualDetailsForm
import forms.NominalValueOfSharesForm.nominalValueOfSharesForm
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._
import views.helpers.ViewSpec
import views.html.seis.investors.IndividualDetails

class IndividualDetailsSpec extends ViewSpec with FakeRequestHelper {

  val backUrl = controllers.seis.routes.CompanyOrIndividualController.show(1).url

  "IndividualDetailsView" when {
    val countriesList: List[(String, String)] = List(("JP", "Japan"), ("GB", "United Kingdom"))

    "Supplied with no errors" should {
      lazy val page = IndividualDetails(individualDetailsForm, countriesList, backUrl)(authorisedFakeRequest, applicationMessages)

      lazy val document = {
        Jsoup.parse(contentAsString(page))
      }

      "have the correct title" in {
        document.title shouldBe Messages("page.individualDetails.title")
      }

      "have the correct section" in {
        document.select("article span").first().text() shouldBe Messages("common.section.progress.details.four")
      }

      "have a link to the number of shares page" in {
        document.select("article a").first().attr("href") shouldBe controllers.seis.routes.CompanyOrIndividualController.show(1).url
      }

      "have a header with the correct question" in {
        document.select("h1").text() shouldBe Messages("page.individualDetails.title")
      }

      "have a form" which {
        lazy val form = document.select("form")

        "has a method of POST" in {
          form.attr("method") shouldBe "POST"
        }

        "has an action to the correct route" in {
          form.attr("action") shouldBe controllers.seis.routes.IndividualDetailsController.submit().url
        }

        "has a label forename with the correct text" in {
          document.getElementById("label-forename").text() shouldBe "First Name"
        }
        "has a label surname with the correct text" in {
          document.getElementById("label-surname").text() shouldBe "Last Name"
        }
        "has a label address1 with the correct text" in {
          document.getElementById("label-addressline1").text() shouldBe "Address line 1"
        }

        "has a label address with the correct input text" in {
          document.getElementById("label-addressline2").text() shouldBe "Address line 2"
        }

        "has a label address3 with the correct text" in {
          document.getElementById("label-addressline3").text() shouldBe "Address line 3 (optional)"
        }
        "has a label address4 with the correct input text" in {
          document.getElementById("label-addressline4").text() shouldBe "Address line 4 (optional)"
        }

        "have a button with the correct text" in {
          document.select("button").text() shouldBe Messages("common.button.snc")
        }
      }}

    "supplied with some errors"  should {
          lazy val view = views.html.seis.investors.IndividualDetails(
            individualDetailsForm.bind(Map("value" -> "")), countriesList, backUrl)(authorisedFakeRequest, applicationMessages)

      lazy val doc = Jsoup.parse(view.body)

          "have an error summary" in {
            doc.getElementById("error-summary-display").hasClass("error-summary--show")
          }
      }
      }}