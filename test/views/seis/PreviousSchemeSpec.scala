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

import forms.PreviousSchemeForm._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import views.helpers.ViewSpec
import play.api.i18n.Messages.Implicits._
import views.html.seis.previousInvestment.PreviousScheme
import controllers.seis.routes

class PreviousSchemeSpec extends ViewSpec {


  "The  Add Previous Scheme page" should {

    lazy val backLink = routes.ReviewPreviousSchemesController.show().url

    "contain the correct elements for a new scheme" in {
      val document: Document = {
        val page = PreviousScheme(previousSchemeForm , backLink)(fakeRequest, applicationMessages)
        Jsoup.parse(page.body)
      }
      document.title() shouldBe Messages("page.investment.PreviousScheme.title")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.ReviewPreviousSchemesController.show().url
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.details.two")

      document.getElementById("main-heading").text() shouldBe Messages("page.investment.PreviousScheme.heading")

      document.getElementById("guideline").text() shouldBe Messages("page.investment.PreviousScheme.oneAtATime")
      //document.select("legend").text() shouldBe Messages("page.previousInvestment.reviewPreviousSchemes.dateOfShareIssue")
      document.select("label[for=schemeTypeDesc-eis]").text() shouldBe Messages("page.previousInvestment.schemeType.eis")
      document.select("label[for=schemeTypeDesc-seis]").text() shouldBe Messages("page.previousInvestment.schemeType.seis")
      document.select("label[for=schemeTypeDesc-sitr]").text() shouldBe Messages("page.previousInvestment.schemeType.sitr")
      document.select("label[for=schemeTypeDesc-vct]").text() shouldBe Messages("page.previousInvestment.schemeType.vct")
      document.select("label[for=schemeTypeDesc-other]").text() shouldBe Messages("page.previousInvestment.schemeType.other")
      document.getElementById("label-amount").text() shouldBe Messages("page.investment.PreviousScheme.investmentAmount")


      document.getElementById("label-amount-spent").text() shouldBe Messages("page.previousInvestment.amountSpent.label")
      document.getElementById("label-other-scheme").text() shouldBe Messages("page.investment.PreviousScheme.otherSchemeName.label")

      document.getElementById("question-text-id").text() shouldBe Messages("page.previousInvestment.reviewPreviousSchemes.dateOfShareIssue")
      document.body.getElementById("investmentDay").parent.text shouldBe Messages("common.date.fields.day")
      document.body.getElementById("investmentMonth").parent.text shouldBe Messages("common.date.fields.month")
      document.body.getElementById("investmentYear").parent.text shouldBe Messages("common.date.fields.year")

      document.getElementById("help").text() shouldBe Messages("page.investment.PreviousScheme.howToFind")
      document.getElementById("date-of-share-issue-where-to-find").text() should include(Messages("page.investment.PreviousScheme.location"))
      document.getElementById("company-house-db").attr("href") shouldBe "https://www.gov.uk/get-information-about-a-company"
      document.body.getElementById("company-house-db").text() shouldEqual getExternalLinkText(Messages("page.investment.PreviousScheme.companiesHouse"))

      document.getElementById("next").text() shouldBe Messages("page.investment.PreviousScheme.button.add")
      document.select(".error-summary").isEmpty shouldBe true
    }

    "contain the correct elements when displaying an existing scheme" in {
      val document: Document = {
        val page = PreviousScheme(previousSchemeForm.fill(previousSchemeModel1) , backLink)(fakeRequest, applicationMessages)
        Jsoup.parse(page.body)
      }

      document.title() shouldBe Messages("page.investment.PreviousScheme.title")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.ReviewPreviousSchemesController.show().url
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.details.two")

      document.getElementById("main-heading").text() shouldBe Messages("page.investment.PreviousScheme.heading")

      document.getElementById("guideline").text() shouldBe Messages("page.investment.PreviousScheme.oneAtATime")
      //document.select("legend").text() shouldBe Messages("page.investment.PreviousScheme.schemeType")
      document.select("label[for=schemeTypeDesc-eis]").text() shouldBe Messages("page.previousInvestment.schemeType.eis")
      document.select("label[for=schemeTypeDesc-seis]").text() shouldBe Messages("page.previousInvestment.schemeType.seis")
      document.select("label[for=schemeTypeDesc-sitr]").text() shouldBe Messages("page.previousInvestment.schemeType.sitr")
      document.select("label[for=schemeTypeDesc-vct]").text() shouldBe Messages("page.previousInvestment.schemeType.vct")
      document.select("label[for=schemeTypeDesc-other]").text() shouldBe Messages("page.previousInvestment.schemeType.other")

      document.getElementById("label-amount").text() shouldBe Messages("page.previousInvestment.reviewPreviousSchemes.investmentAmountRaised")

      document.getElementById("label-amount-spent").text() shouldBe Messages("page.previousInvestment.amountSpent.label")
      document.getElementById("label-other-scheme").text() shouldBe Messages("page.investment.PreviousScheme.otherSchemeName.label")

      document.getElementById("question-text-id").text() shouldBe Messages("page.previousInvestment.reviewPreviousSchemes.dateOfShareIssue")
      document.body.getElementById("investmentDay").parent.text shouldBe Messages("common.date.fields.day")
      document.body.getElementById("investmentMonth").parent.text shouldBe Messages("common.date.fields.month")
      document.body.getElementById("investmentYear").parent.text shouldBe Messages("common.date.fields.year")

      document.getElementById("help").text() shouldBe Messages("page.investment.PreviousScheme.howToFind")
      document.getElementById("date-of-share-issue-where-to-find").text() should include(Messages("page.investment.PreviousScheme.location"))
      document.getElementById("company-house-db").attr("href") shouldBe "https://www.gov.uk/get-information-about-a-company"
      document.body.getElementById("company-house-db").text() shouldEqual getExternalLinkText(Messages("page.investment.PreviousScheme.companiesHouse"))



      document.getElementById("next").text() shouldBe Messages("page.investment.PreviousScheme.button.update")
      document.select(".error-summary").isEmpty shouldBe true
    }

    "contain the error summary when a form with errors is passed" in {
      val document: Document = {
        val page = PreviousScheme(previousSchemeForm.bind(Map(""->"")), backLink)(fakeRequest, applicationMessages)
        Jsoup.parse(page.body)
      }

      document.title() shouldBe Messages("page.investment.PreviousScheme.title")
      document.getElementById("error-summary-display").hasClass("error-summary--show") shouldBe true
      document.getElementById("error-summary-heading").text shouldBe Messages("common.error.summary.heading")
      document.getElementById("schemeTypeDesc-error-summary").text shouldBe Messages("validation.common.error.fieldRequired")
      document.getElementById("investmentAmount-error-summary").text shouldBe Messages("validation.common.error.fieldRequired")
    }
  }

}
