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

import controllers.eis.routes
import models.PreviousSchemeModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import views.helpers.ViewSpec
import views.html.eis.previousInvestment.ReviewPreviousSchemes

class ReviewPreviousSchemesSpec extends ViewSpec {


  val expectedTotalInvestmentAmount = previousSchemeVectorList.foldLeft(0)(_ + _.investmentAmount)
  val notAvailable = "N/A"

  "The Review Previous Schemes page" should {

    "contain the correct table rows and data when a valid vector of PreviousSchemeModels are passed as returned from storage" in {
      val document: Document = {
        val page = ReviewPreviousSchemes(previousSchemeVectorList)(fakeRequest, applicationMessages)
        Jsoup.parse(page.body)
      }

      document.title shouldBe Messages("page.previousInvestment.reviewPreviousSchemes.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.previousInvestment.reviewPreviousSchemes.heading")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.HadPreviousRFIController.show().url
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.details.two")

      document.getElementById("change-heading").hasClass("visuallyhidden") shouldBe true
      document.getElementById("change-heading").text shouldBe Messages("common.base.change")
      document.getElementById("remove-heading").hasClass("visuallyhidden") shouldBe true
      document.getElementById("remove-heading").text shouldBe Messages("common.base.remove")


      lazy val reviewSchemesTableHead = document.getElementById("previous-schemes-table").select("thead")
      lazy val reviewSchemesTableBody = document.getElementById("previous-schemes-table").select("tbody")
      //head
      reviewSchemesTableHead.select("tr").get(0).getElementById("scheme-table-heading").text() shouldBe
        Messages("page.previousInvestment.reviewPreviousSchemes.scheme")
      reviewSchemesTableHead.select("tr").get(0).getElementById("date-table-heading").text() shouldBe
        Messages("page.previousInvestment.reviewPreviousSchemes.dateOfShareIssue")
      reviewSchemesTableHead.select("tr").get(0).getElementById("amount-raised-table-heading").text() shouldBe
        Messages("page.previousInvestment.reviewPreviousSchemes.investmentAmountRaised")
      reviewSchemesTableHead.select("tr").get(0).getElementById("amount-spent-table-heading").text() shouldBe
        Messages("page.previousInvestment.reviewPreviousSchemes.investmentAmountSpent")
      //body
      for((previousScheme, index) <- previousSchemeVectorList.zipWithIndex) {
        reviewSchemesTableBody.select("tr").get(index).getElementById(s"scheme-type-$index").text() shouldBe
          PreviousSchemeModel.getSchemeName(previousScheme.schemeTypeDesc, previousScheme.otherSchemeName)
        reviewSchemesTableBody.select("tr").get(index).getElementById(s"scheme-date-$index").text() shouldBe
          PreviousSchemeModel.toDateString(previousScheme.day.get, previousScheme.month.get, previousScheme.year.get)
        reviewSchemesTableBody.select("tr").get(index).getElementById(s"scheme-amount-raised-$index").text() shouldBe
          PreviousSchemeModel.getAmountAsFormattedString(previousScheme.investmentAmount)
        reviewSchemesTableBody.select("tr").get(index).getElementById(s"scheme-amount-spent-$index").text() shouldBe {
          if(previousScheme.investmentSpent.isDefined) PreviousSchemeModel.getAmountAsFormattedString(previousScheme.investmentSpent.get) else notAvailable
        }
        reviewSchemesTableBody.select("tr").get(index).getElementById(s"change-$index").text() shouldBe
          Messages("common.base.change")
        reviewSchemesTableBody.select("tr").get(index).getElementById(s"change-$index").getElementById(s"change-ref-$index").attr("href")shouldBe
          controllers.eis.routes.ReviewPreviousSchemesController.change(previousScheme.processingId.get).toString
        reviewSchemesTableBody.select("tr").get(index).getElementById(s"remove-$index").text() shouldBe
          Messages("common.base.remove")
      }

      reviewSchemesTableBody.select("tr").get(previousSchemeVectorList.size).getElementById("total-investment-heading").text() shouldBe
        Messages("page.previousInvestment.reviewPreviousSchemes.totalInvestment")
      reviewSchemesTableBody.select("tr").get(previousSchemeVectorList.size).getElementById("total-investment-amount").text() shouldBe
        PreviousSchemeModel.getAmountAsFormattedString(expectedTotalInvestmentAmount)
      reviewSchemesTableBody.select("tr").get(previousSchemeVectorList.size + 1).getElementById("add-scheme").attr("href") shouldBe
        routes.ReviewPreviousSchemesController.add.toString
      document.body.getElementById("next").text() shouldEqual Messages("common.button.snc")
      document.body.getElementById("get-help-action").text shouldBe Messages("common.error.help.text")
    }
  }
}
