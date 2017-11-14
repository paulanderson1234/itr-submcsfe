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
import controllers.eis.routes
import models.{AddInvestorOrNomineeModel, CompanyDetailsModel, CompanyOrIndividualModel, IndividualDetailsModel}
import models.investorDetails.{InvestorShareIssueDateModel, NumberOfPreviouslyIssuedSharesModel, PreviousShareHoldingDescriptionModel, PreviousShareHoldingNominalValueModel, _}
import org.jsoup.Jsoup
import play.api.i18n.Messages
import views.helpers.ViewSpec
import play.api.i18n.Messages.Implicits._

class ReviewAllInvestorsSpec extends ViewSpec {

  val expectedAmountSpentResult = "£1,000"

  val testModelValidCompany = validModelWithPrevShareHoldings
  val testModelValidIndividual = validModelWithPrevShareHoldings.copy(
    companyOrIndividualModel = Some(CompanyOrIndividualModel(Constants.typeIndividual, validModelWithPrevShareHoldings.processingId)),
    companyDetailsModel = None,
    individualDetailsModel = Some(individualDetailsModel),
    processingId = Some(3))

  val testModelInValid = InvestorDetailsModel(
    Some(AddInvestorOrNomineeModel("Investor", Some(1))),
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    Some(1)
  )

  val combinationValid = Vector(testModelValidCompany, testModelValidIndividual)
  val combinationInvalid = Vector(testModelValidCompany, testModelInValid)

  "The Review All Investors page" when {
    implicit val request = fakeRequest

    "provided with VALID investor details" should {
      lazy val view = views.html.eis.investors.ReviewAllInvestors(combinationValid)
      lazy val doc = Jsoup.parse(view.body)

      "have no backlink" in {
        doc.select("back-link").isEmpty shouldBe true
      }

      "have the correct title" in {
        doc.title() shouldBe Messages("page.investors.reviewAllInvestors.title")
      }

      "have the correct heading" in {
        doc.select("h1").text() shouldBe Messages("page.investors.reviewAllInvestors.title")
      }

      "have the correct sub text" in {
        doc.getElementById("review-all-investors-info").text() shouldBe Messages("page.investors.reviewAllInvestors.info")
      }

      "have a table for investors" which {
        lazy val table = doc.select("table").get(0)

        "has the correct table headings" in {
          table.select("th").get(0).text() shouldBe Messages("page.investors.reviewAllInvestors.investorName")
          table.select("th").get(1).text() shouldBe Messages("page.investors.reviewAllInvestors.numberOfShares")
          table.select("th").get(2).text() shouldBe Messages("page.investors.reviewAllInvestors.amountSpent")
          table.select("th").get(3).getElementsByTag("span").get(0).className() shouldBe "visuallyhidden"
          table.select("th").get(4).getElementsByTag("span").get(0).className() shouldBe "visuallyhidden"
        }

        "has the correct investor names" in {
          val id = "td#investor-name"
          table.select(s"$id-0").text() shouldBe testModelValidCompany.companyDetailsModel.get.companyName
          table.select(s"$id-1").text() shouldBe "Joe Bloggs"
        }

        "has the correct number of shares for each investor" in {
          val id = "td#number-of-shares"
          table.select(s"$id-0").text() shouldBe testModelValidCompany.numberOfSharesPurchasedModel.get.numberOfSharesPurchased.toString()
          table.select(s"$id-1").text() shouldBe testModelValidIndividual.numberOfSharesPurchasedModel.get.numberOfSharesPurchased.toString()
        }

        "has the correct amounts spent for each investor" in {
          val id = "td#amount-raised"
          table.select(s"$id-0").text() shouldBe expectedAmountSpentResult
          table.select(s"$id-1").text() shouldBe expectedAmountSpentResult
        }

        "have the correct change links text" in {
          val id = "td#change"
          table.select(s"$id-0").text() shouldBe Messages("common.base.change")
          table.select(s"$id-1").text() shouldBe Messages("common.base.change")
        }

        "have the correct change links reference" in {
          val id = "td#change"
          table.select(s"$id-0").first().getElementById("change-ref-0").attr("href") shouldBe
            routes.ReviewAllInvestorsController.change(testModelValidCompany.processingId.get).toString
          table.select(s"$id-1").first().getElementById("change-ref-1").attr("href") shouldBe
            routes.ReviewAllInvestorsController.change(testModelValidIndividual.processingId.get).toString
        }

        "have the correct remove links text" in {
          val id = "td#remove"
          table.select(s"$id-0").text() shouldBe Messages("common.base.remove")
          table.select(s"$id-1").text() shouldBe Messages("common.base.remove")
        }

        "have the correct remove links action" in {
          val id = "td#remove"
          table.select(s"$id-0").first().select("form").attr("action") shouldBe
            routes.ReviewAllInvestorsController.remove(testModelValidCompany.processingId.get).toString
          table.select(s"$id-1").first().select("form").attr("action") shouldBe
            routes.ReviewAllInvestorsController.remove(testModelValidIndividual.processingId.get).toString
        }

        "display the 'add another investor' button" in {
          table.select("td#add-investor").isEmpty shouldBe false
        }

        "have the correct 'add another investor' button text" in {
          table.select("td#add-investor").text() shouldBe Messages("page.investors.reviewAllInvestors.add")
        }

        "have the correct 'add another investor' button reference" in {
          table.select("td#add-investor").get(0).getElementById("add-investor-ref").attr("href") shouldBe
            routes.ReviewAllInvestorsController.add().toString
        }

      }
    }


    "provided with INVALID investor details" should {
      lazy val view = views.html.eis.investors.ReviewAllInvestors(combinationInvalid)
      lazy val doc = Jsoup.parse(view.body)

      "have no backlink" in {
        doc.select("back-link").isEmpty shouldBe true
      }

      "have the correct title" in {
        doc.title() shouldBe Messages("page.investors.reviewAllInvestors.title")
      }

      "have the correct heading" in {
        doc.select("h1").text() shouldBe Messages("page.investors.reviewAllInvestors.title")
      }

      "have the correct sub text" in {
        doc.getElementById("review-all-investors-info").text() shouldBe Messages("page.investors.reviewAllInvestors.info")
      }

      "have a table for investors" which {
        lazy val table = doc.select("table").get(0)

        "has the correct table headings" in {
          table.select("th").get(0).text() shouldBe Messages("page.investors.reviewAllInvestors.investorName")
          table.select("th").get(1).text() shouldBe Messages("page.investors.reviewAllInvestors.numberOfShares")
          table.select("th").get(2).text() shouldBe Messages("page.investors.reviewAllInvestors.amountSpent")
          table.select("th").get(3).getElementsByTag("span").get(0).className() shouldBe "visuallyhidden"
          table.select("th").get(4).getElementsByTag("span").get(0).className() shouldBe "visuallyhidden"
        }

        "has the correct investor names" in {
          val id = "td#investor-name"
          table.select(s"$id-0").text() shouldBe testModelValidCompany.companyDetailsModel.get.companyName
          table.select(s"$id-1").text() shouldBe Messages("page.investors.reviewInvestorDetails.incomplete")
        }

        "has the correct number of shares for each investor" in {
          val id = "td#number-of-shares"
          table.select(s"$id-0").text() shouldBe testModelValidCompany.numberOfSharesPurchasedModel.get.numberOfSharesPurchased.toString()
          table.select(s"$id-1").text() shouldBe Messages("page.investors.reviewInvestorDetails.incomplete")
        }

        "has the correct amounts spent for each investor" in {
          val id = "td#amount-raised"
          table.select(s"$id-0").text() shouldBe expectedAmountSpentResult
          table.select(s"$id-1").text() shouldBe Messages("page.investors.reviewInvestorDetails.incomplete")
        }

        "have the correct continue links text" in {
          val id = "td#continue"
          table.select(s"$id-0").isEmpty shouldBe true
          table.select(s"$id-1").text() shouldBe Messages("common.button.continue")
        }

        "have the correct continue links reference" in {
          val id = "td#continue"
          table.select(s"$id-1").first().getElementById("continue-ref-1").attr("href") shouldBe
            routes.ReviewAllInvestorsController.change(testModelInValid.processingId.get).toString
        }

        "have the correct remove links text" in {
          val id = "td#remove"
          table.select(s"$id-0").text() shouldBe Messages("common.base.remove")
          table.select(s"$id-1").text() shouldBe Messages("common.base.remove")
        }

        "have the correct remove links action" in {
          val id = "td#remove"
          table.select(s"$id-0").first().select("form").attr("action") shouldBe
            routes.ReviewAllInvestorsController.remove(testModelValidCompany.processingId.get).toString
          table.select(s"$id-1").first().select("form").attr("action") shouldBe
            routes.ReviewAllInvestorsController.remove(testModelInValid.processingId.get).toString
        }

        "not display the 'add another investor' button" in {
          table.select("td#add-investor").isEmpty shouldBe true
        }

      }

    }
  }
}
