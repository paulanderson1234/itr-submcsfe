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
import forms.CompanyOrIndividualForm._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.FormError
import play.api.i18n.Messages
import views.helpers.ViewSpec
import play.api.i18n.Messages.Implicits._
import views.html.seis.investors.CompanyOrIndividual

class CompanyOrIndividualSpec extends ViewSpec{

  def commonTests(document: Document, templateText: String): Unit = {
    document.title() shouldBe Messages("page.investors.companyOrIndividual.title", templateText)
    document.getElementById("main-heading").text() shouldBe Messages("page.investors.companyOrIndividual.title", templateText)
    document.getElementById("next").text() shouldBe Messages("common.button.snc")
    document.body.getElementById("back-link").attr("href") shouldEqual controllers.seis.routes.AddInvestorOrNomineeController.show().url
    document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.four")
    document.getElementById("companyOrIndividual-companyLabel").text() shouldBe Messages("page.investors.companyOrIndividual.company")
    document.getElementById("companyOrIndividual-individualLabel").text() shouldBe Messages("page.investors.companyOrIndividual.individual")
  }

  "The CompanyOrIndividual page" should {
    val templateText = "individual"
    "show the correct elements" when{
      "a form with an empty model is passed to the template" in {
        lazy val page = CompanyOrIndividual(templateText, companyOrIndividualForm)(fakeRequest, applicationMessages)
        lazy val document = Jsoup.parse(page.body)

        commonTests(document, templateText)
      }
      "a form with errors is passed to the template" in {
        val formWithErrors = companyOrIndividualForm.bind(Map(""->""))
        lazy val page = CompanyOrIndividual(templateText, formWithErrors)(fakeRequest, applicationMessages)
        lazy val document = Jsoup.parse(page.body)

        commonTests(document, templateText)

        document.getElementById("error-summary-display").hasClass("error-summary--show")
      }
    }
  }
}
