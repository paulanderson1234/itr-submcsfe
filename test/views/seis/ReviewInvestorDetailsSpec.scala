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

import models.{AddInvestorOrNomineeModel, CompanyDetailsModel, CompanyOrIndividualModel, IndividualDetailsModel}
import models.investorDetails.{InvestorShareIssueDateModel, NumberOfPreviouslyIssuedSharesModel, PreviousShareHoldingDescriptionModel, PreviousShareHoldingNominalValueModel, _}
import org.jsoup.Jsoup
import play.api.i18n.Messages
import views.helpers.ViewSpec
import play.api.i18n.Messages.Implicits._

class ReviewInvestorDetailsSpec extends ViewSpec {

  val testModel = InvestorDetailsModel(
    Some(AddInvestorOrNomineeModel("Investor", Some(1))),
    Some(CompanyOrIndividualModel("Company", Some(1))),
    Some(CompanyDetailsModel("Company Name", "72", "Redwood Close", Some("Albrook"), None, None, "DE", Some(1))),
    Some(IndividualDetailsModel("Jon", "Doe", "68", "Purbeck Road", Some("Woking"), None, Some("W13 2QW"), "GB", Some(1))),
    Some(NumberOfSharesPurchasedModel(100, Some(1))),
    Some(HowMuchSpentOnSharesModel(1000, Some(1))),
    Some(IsExistingShareHolderModel("Yes")),
    Some(Vector(
      PreviousShareHoldingModel(
        Some(InvestorShareIssueDateModel(Some(1), Some(2), Some(2016), Some(1), Some(1))),
        Some(NumberOfPreviouslyIssuedSharesModel(1000, Some(1), Some(1))),
        Some(PreviousShareHoldingNominalValueModel(10000, Some(1), Some(1))),
        Some(PreviousShareHoldingDescriptionModel("Class 1 Share", Some(1), Some(1))),
        Some(1),
        Some(1)
      ),
      PreviousShareHoldingModel(
        Some(InvestorShareIssueDateModel(Some(1), Some(2), Some(2016), Some(1), Some(1))),
        Some(NumberOfPreviouslyIssuedSharesModel(1000, Some(1), Some(1))),
        Some(PreviousShareHoldingNominalValueModel(10000, Some(1), Some(1))),
        Some(PreviousShareHoldingDescriptionModel("Class 2 Share", Some(2), Some(1))),
        Some(2),
        Some(1)
      )
    )),
    Some(1)
  )

  "The Review Investor Details page" when {
    implicit val request = fakeRequest

    "provided with an unfinished model for a nominee" should {
      lazy val view = views.html.seis.investors.ReviewInvestorDetails(InvestorDetailsModel(Some(AddInvestorOrNomineeModel(
        "Nominee", Some(1))), processingId = Some(1)))
      lazy val doc = Jsoup.parse(view.body)

      "have the correct title for a nominee" in {
        doc.title() shouldBe Messages("page.investors.reviewInvestorDetails.title", "nominee")
      }

      "have a progress breadcrumb for section 4" in {
        doc.select("#content p").first().text() shouldBe Messages("common.section.progress.company.details.four")
      }

      "have the correct heading for a nominee" in {
        doc.select("h1").text() shouldBe Messages("page.investors.reviewInvestorDetails.title", "nominee")
      }

      "have a table for investor details" which {
        lazy val table = doc.select("table").get(0)

        "has the correct subheading for a nominee" in {
          table.select("th").text() shouldBe Messages("page.investors.reviewInvestorDetails.investor.subheading", "Nominee")
        }

        "has an entry for investor or nominee" which {
          val id = "td#investor-or-nominee"

          "has the correct question text" in {
            table.select(s"$id-question").text() shouldBe Messages("page.investors.reviewInvestorDetails.investor.investorOrNominee")
          }

          "has an answer of 'Nominee'" in {
            table.select(s"$id-answer").text() shouldBe Messages("page.investors.AddInvestorOrNominee.radioButton.two")
          }

          "has a change link to the correct page" in {
            table.select(s"$id-change a").attr("href") shouldBe controllers.seis.routes.AddInvestorOrNomineeController.show(Some(1)).url
          }
        }

        "has an entry for company or individual" which {
          val id = "td#company-or-individual"

          "has the correct question text" in {
            table.select(s"$id-question").text() shouldBe Messages("page.investors.reviewInvestorDetails.investor.companyOrIndividual")
          }

          "has an answer of 'Incomplete'" in {
            table.select(s"$id-answer").text() shouldBe Messages("page.investors.reviewInvestorDetails.incomplete")
          }

          "has a change link to the correct page" in {
            table.select(s"$id-change a").attr("href") shouldBe controllers.seis.routes.CompanyOrIndividualController.show(1).url
          }
        }

        "has no entry for company name" in {
          table.select("td#company-name-question").isEmpty shouldBe true
        }

        "has no entry for company address" in {
          table.select("td#company-address-question").isEmpty shouldBe true
        }

        "has no entry for individual name" in {
          table.select("td#individual-name-question").isEmpty shouldBe true
        }

        "has no entry for individual address" in {
          table.select("td#individual-address-question").isEmpty shouldBe true
        }

        "has an entry for number of shares purchased" which {
          val id = "td#shares-purchased"

          "has the correct question text" in {
            table.select(s"$id-question").text() shouldBe Messages("page.investors.reviewInvestorDetails.investor.purchasedShares")
          }

          "has an answer of 'Incomplete'" in {
            table.select(s"$id-answer").text() shouldBe Messages("page.investors.reviewInvestorDetails.incomplete")
          }

          "has a change link to the correct page" in {
            table.select(s"$id-change a").attr("href") shouldBe controllers.seis.routes.NumberOfSharesPurchasedController.show(1).url
          }
        }

        "has an entry for amount spent" which {
          val id = "td#amount-spent"

          "has the correct question text" in {
            table.select(s"$id-question").text() shouldBe Messages("page.investors.reviewInvestorDetails.investor.totalSpent")
          }

          "has an answer of 'Incomplete'" in {
            table.select(s"$id-answer").text() shouldBe Messages("page.investors.reviewInvestorDetails.incomplete")
          }

          "has a change link to the correct page" in {
            table.select(s"$id-change a").attr("href") shouldBe controllers.seis.routes.HowMuchSpentOnSharesController.show(1).url
          }
        }
      }

      "have a table for shareholding details" which {
        lazy val table = doc.select("table").get(1)

        "has the correct subheading" in {
          table.select("th").text() shouldBe Messages("page.investors.reviewInvestorDetails.shareholdings.subheading")
        }

        "has an entry for previous shareholdings" which {
          val id = "td#is-existing-shareholder"

          "has the correct question text" in {
            table.select(s"$id-question").text() shouldBe Messages("page.investors.reviewInvestorDetails.shareholdings.isExistingShareholder")
          }

          "has an answer of 'Incomplete'" in {
            table.select(s"$id-answer").text() shouldBe Messages("page.investors.reviewInvestorDetails.incomplete")
          }

          "has a change link to the correct page" in {
            table.select(s"$id-change a").attr("href") shouldBe controllers.seis.routes.IsExistingShareHolderController.show(1).url
          }
        }

        "has no entries for any shareholdings" in {
          table.select("tr").size() shouldBe 2
        }
      }

      "have no save and continue button" in {
        doc.select("a.button").isEmpty shouldBe true
      }
    }

    "provided with a completed model for an investor who is a company" should {
      lazy val view = views.html.seis.investors.ReviewInvestorDetails(testModel)
      lazy val doc = Jsoup.parse(view.body)

      "have the correct title for an investor" in {
        doc.title() shouldBe Messages("page.investors.reviewInvestorDetails.title", "investor")
      }

      "have a progress breadcrumb for section 4" in {
        doc.select("#content p").first().text() shouldBe Messages("common.section.progress.company.details.four")
      }

      "have the correct heading for a nominee" in {
        doc.select("h1").text() shouldBe Messages("page.investors.reviewInvestorDetails.title", "investor")
      }

      "have a table for investor details" which {
        lazy val table = doc.select("table").get(0)

        "has the correct subheading for a nominee" in {
          table.select("th").text() shouldBe Messages("page.investors.reviewInvestorDetails.investor.subheading", "Investor")
        }

        "has an entry for investor or nominee" which {
          val id = "td#investor-or-nominee"

          "has the correct question text" in {
            table.select(s"$id-question").text() shouldBe Messages("page.investors.reviewInvestorDetails.investor.investorOrNominee")
          }

          "has an answer of 'Investor'" in {
            table.select(s"$id-answer").text() shouldBe Messages("page.investors.AddInvestorOrNominee.radioButton.one")
          }

          "has a change link to the correct page" in {
            table.select(s"$id-change a").attr("href") shouldBe controllers.seis.routes.AddInvestorOrNomineeController.show(Some(1)).url
          }
        }

        "has an entry for company or individual" which {
          val id = "td#company-or-individual"

          "has the correct question text" in {
            table.select(s"$id-question").text() shouldBe Messages("page.investors.reviewInvestorDetails.investor.companyOrIndividual")
          }

          "has an answer of 'Company'" in {
            table.select(s"$id-answer").text() shouldBe Messages("page.investors.companyOrIndividual.company")
          }

          "has a change link to the correct page" in {
            table.select(s"$id-change a").attr("href") shouldBe controllers.seis.routes.CompanyOrIndividualController.show(1).url
          }
        }

        "has an entry for company name" which {
          val id = "td#company-name"

          "has the correct question text" in {
            table.select(s"$id-question").text() shouldBe Messages("page.investors.reviewInvestorDetails.investor.companyName")
          }

          "has an answer of 'Company name'" in {
            table.select(s"$id-answer").text() shouldBe "Company Name"
          }

          "has a change link to the correct page" in {
            table.select(s"$id-change a").attr("href") shouldBe controllers.seis.routes.CompanyDetailsController.show(1).url
          }
        }

        "has an entry for company address" which {
          val id = "td#company-address"

          "has the correct question text" in {
            table.select(s"$id-question").text() shouldBe Messages("page.investors.reviewInvestorDetails.investor.contactAddress")
          }

          "has an answer of '72 Redwood Close Albrook DE'" in {
            table.select(s"$id-answer").text() shouldBe "72 Redwood Close Albrook DE"
          }

          "has a change link to the correct page" in {
            table.select(s"$id-change a").attr("href") shouldBe controllers.seis.routes.CompanyDetailsController.show(1).url
          }
        }

        "has no entry for individual name" in {
          table.select("td#individual-name-question").isEmpty shouldBe true
        }

        "has no entry for individual address" in {
          table.select("td#individual-address-question").isEmpty shouldBe true
        }

        "has an entry for number of shares purchased" which {
          val id = "td#shares-purchased"

          "has the correct question text" in {
            table.select(s"$id-question").text() shouldBe Messages("page.investors.reviewInvestorDetails.investor.purchasedShares")
          }

          "has an answer of '100'" in {
            table.select(s"$id-answer").text() shouldBe "100"
          }

          "has a change link to the correct page" in {
            table.select(s"$id-change a").attr("href") shouldBe controllers.seis.routes.NumberOfSharesPurchasedController.show(1).url
          }
        }

        "has an entry for amount spent" which {
          val id = "td#amount-spent"

          "has the correct question text" in {
            table.select(s"$id-question").text() shouldBe Messages("page.investors.reviewInvestorDetails.investor.totalSpent")
          }

          "has an answer of '£1000'" in {
            table.select(s"$id-answer").text() shouldBe "£1,000"
          }

          "has a change link to the correct page" in {
            table.select(s"$id-change a").attr("href") shouldBe controllers.seis.routes.HowMuchSpentOnSharesController.show(1).url
          }
        }
      }

      "have a table for shareholding details" which {
        lazy val table = doc.select("table").get(1)

        "has the correct subheading" in {
          table.select("th").text() shouldBe Messages("page.investors.reviewInvestorDetails.shareholdings.subheading")
        }

        "has an entry for previous shareholdings" which {
          val id = "td#is-existing-shareholder"

          "has the correct question text" in {
            table.select(s"$id-question").text() shouldBe Messages("page.investors.reviewInvestorDetails.shareholdings.isExistingShareholder")
          }

          "has an answer of 'Yes'" in {
            table.select(s"$id-answer").text() shouldBe Messages("common.radioYesLabel")
          }
          
          "has no change link" in {
            table.select(s"$id-change a").isEmpty shouldBe true
          }
        }

        "has a previous shareholding with an id of 1" which {
          val id = "td#previous-shareholding-1"

          "has the correct description text" in {
            table.select(s"$id-question").text() shouldBe "Class 1 Share"
          }

          "has an answer of 'Nominal value: £10,000 Date of investment: 01 January 2016 Number of shares purchased: 1,000'" in {
            table.select(s"$id-answer").text() shouldBe "Nominal value: £10,000 Date of investment: 01 January 2016 Number of shares purchased: 1,000"
          }

          "has a change link to the correct page" in {
            table.select(s"$id-change a").attr("href") shouldBe controllers.seis.routes.PreviousShareHoldingsReviewController.show(1).url
          }
        }

        "has a previous shareholding with an id of 2" which {
          val id = "td#previous-shareholding-2"

          "has the correct description text" in {
            table.select(s"$id-question").text() shouldBe "Class 2 Share"
          }

          "has an answer of 'Nominal value: £10,000 Date of investment: 01 January 2016 Number of shares purchased: 1,000'" in {
            table.select(s"$id-answer").text() shouldBe "Nominal value: £10,000 Date of investment: 01 January 2016 Number of shares purchased: 1,000"
          }

          "has a change link to the correct page" in {
            table.select(s"$id-change a").attr("href") shouldBe controllers.seis.routes.PreviousShareHoldingsReviewController.show(1).url
          }
        }

        "has an add another shareholding link" which {
          lazy val link = table.select("td#add-shareholder-link a")

          "has the correct text" in {
            link.text() shouldBe Messages("page.investors.reviewInvestorDetails.shareholdings.addAnotherShareholding")
          }

          "has a link to the shareholder description page" in {
            link.attr("href") shouldBe controllers.seis.routes.PreviousShareHoldingDescriptionController.show(1).url
          }
        }
      }

      "have a save and continue button" which {
        lazy val button = doc.select("a.button")

        "has the correct text" in {
          button.text() shouldBe Messages("common.button.snc")
        }

        "links to the review investors page" in {
          button.attr("href") shouldBe controllers.seis.routes.ReviewAllInvestorsController.show().url
        }
      }
    }

    "provided with a model for an individual with an unfinished shareholding" should {
      lazy val view = views.html.seis.investors.ReviewInvestorDetails(testModel.copy(
        companyOrIndividualModel = Some(CompanyOrIndividualModel("Individual", Some(1))),
        previousShareHoldingModels = Some(Vector(PreviousShareHoldingModel()))
      ))
      lazy val doc = Jsoup.parse(view.body)

      "have a table for investor details" which {
        lazy val table = doc.select("table").get(0)

        "has an answer of 'Individual' for company and individual" in {
          table.select("td#company-or-individual-answer").text() shouldBe Messages("page.investors.companyOrIndividual.individual")
        }

        "has no entry for company name" in {
          table.select("td#company-name-question").isEmpty shouldBe true
        }

        "has no entry for company address" in {
          table.select("td#company-address-question").isEmpty shouldBe true
        }

        "has an entry for individual name" which {
          val id = "td#individual-name"

          "has the correct question text" in {
            table.select(s"$id-question").text() shouldBe Messages("page.investors.reviewInvestorDetails.investor.contactName")
          }

          "has an answer of 'Jon Doe'" in {
            table.select(s"$id-answer").text() shouldBe "Jon Doe"
          }

          "has a change link to the correct page" in {
            table.select(s"$id-change a").attr("href") shouldBe controllers.seis.routes.IndividualDetailsController.show(1).url
          }
        }

        "has an entry for individual address" which {
          val id = "td#individual-address"

          "has the correct question text" in {
            table.select(s"$id-question").text() shouldBe Messages("page.investors.reviewInvestorDetails.investor.contactAddress")
          }

          "has an answer of '68 Purbeck Road Woking W13 2QW GB'" in {
            table.select(s"$id-answer").text() shouldBe "68 Purbeck Road Woking W13 2QW GB"
          }

          "has a change link to the correct page" in {
            table.select(s"$id-change a").attr("href") shouldBe controllers.seis.routes.IndividualDetailsController.show(1).url
          }
        }
      }

      "have a table for previous shareholdings" which {
        lazy val table = doc.select("table").get(1)

        "has a previous shareholding with an id of 1" which {
          val id = "td#previous-shareholding-1"

          "has the correct description text" in {
            table.select(s"$id-question").text() shouldBe Messages("page.investors.reviewInvestorDetails.incomplete")
          }

          "has an answer of 'Incomplete Incomplete Incomplete'" in {
            table.select(s"$id-answer").text() shouldBe "Incomplete Incomplete Incomplete"
          }

          "has a change link to the correct page" in {
            table.select(s"$id-change a").attr("href") shouldBe controllers.seis.routes.PreviousShareHoldingsReviewController.show(1).url
          }
        }

        "has no add another shareholding link" in {
          table.select("td#add-shareholder-link a").isEmpty shouldBe true
        }
      }
    }

    "provided with a model missing the individual details" should {
      lazy val view = views.html.seis.investors.ReviewInvestorDetails(testModel.copy(
        companyOrIndividualModel = Some(CompanyOrIndividualModel("Individual", Some(1))),
        individualDetailsModel = None
      ))
      lazy val doc = Jsoup.parse(view.body)

      "have a table for investor details" which {
        lazy val table = doc.select("table").get(0)

        "has an incomplete entry for individual name" in {
          table.select("td#individual-name-answer").text() shouldBe Messages("page.investors.reviewInvestorDetails.incomplete")
        }

        "has an incomplete entry for individual address" in {
          table.select("td#individual-address-answer").text() shouldBe Messages("page.investors.reviewInvestorDetails.incomplete")
        }
      }
    }

    "provided with a model missing the company details" should {
      lazy val view = views.html.seis.investors.ReviewInvestorDetails(testModel.copy(
        companyDetailsModel = None
      ))
      lazy val doc = Jsoup.parse(view.body)

      "have a table for investor details" which {
        lazy val table = doc.select("table").get(0)

        "has an incomplete entry for company name" in {
          table.select("td#company-name-answer").text() shouldBe Messages("page.investors.reviewInvestorDetails.incomplete")
        }

        "has an incomplete entry for company address" in {
          table.select("td#company-address-answer").text() shouldBe Messages("page.investors.reviewInvestorDetails.incomplete")
        }
      }
    }
  }
}
