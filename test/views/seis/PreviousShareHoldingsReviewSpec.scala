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

import models.investorDetails.{InvestorDetailsModel, PreviousShareHoldingModel}
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import views.helpers.ViewSpec
import views.html.seis.investors.PreviousShareHoldingsReview

class PreviousShareHoldingsReviewSpec extends ViewSpec {

  val shareHoldersModelForReview = Vector(PreviousShareHoldingModel(investorShareIssueDateModel = Some(investorShareIssueDateModel1),
    numberOfPreviouslyIssuedSharesModel = Some (numberOfPreviouslyIssuedSharesModel1),
    previousShareHoldingNominalValueModel = Some(previousShareHoldingNominalValueModel1),
    previousShareHoldingDescriptionModel = Some(previousShareHoldingDescriptionModel1),
    processingId = Some(1), investorProcessingId = Some(2)))

  val investorModelForReview = InvestorDetailsModel(Some(investorModel2), Some(companyOrIndividualModel2), Some(companyDetailsModel2), None,
    Some(numberOfSharesPurchasedModel2), Some(howMuchSpentOnSharesModel2), Some(isExistingShareHolderModelYes),
    previousShareHoldingModels = Some(shareHoldersModelForReview), processingId = Some(2))

  "The Review Previous share holdings Spec page" should {

    "Verify that Review Previous share holdings page contains the correct table rows and data " +
      "when a valid vector of Previous share holdings model are passed as returned from keystore" in new SEISSetup {

      lazy val page = PreviousShareHoldingsReview(investorModelForReview)(fakeRequest, applicationMessages)
      lazy val document = Jsoup.parse(page.body)

      document.title shouldBe Messages("page.investors.previousShareHoldingReview.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.investors.previousShareHoldingReview.heading")

      lazy val reviewShareHoldingsTableHead = document.getElementById("previous-share-holdings-table").select("thead")
      lazy val reviewShareHoldingsTableBody = document.getElementById("previous-share-holdings-table").select("tbody")
      //head
      reviewShareHoldingsTableHead.select("tr").get(0).getElementById("share-holdings-table-heading").text() shouldBe
        Messages("page.investors.previousShareHoldingReview.heading.one")
//      reviewShareHoldingsTableHead.select("tr").get(0).getElementById("amount-raised-table-heading").text() shouldBe
//        Messages("page.investors.previousShareHoldingReview.heading.two")
      reviewShareHoldingsTableHead.select("tr").get(0).getElementById("date-table-heading").text() shouldBe
        Messages("page.investors.previousShareHoldingReview.heading.three")
      reviewShareHoldingsTableHead.select("tr").get(0).getElementById("count-table-heading").text() shouldBe
        Messages("page.investors.previousShareHoldingReview.heading.four")
      //body
      for((previousShareHoldingModel, index) <- investorModelForReview.previousShareHoldingModels.get.zipWithIndex) {
        reviewShareHoldingsTableBody.select("tr").get(index).getElementById(s"share-desc-$index").text() shouldBe
          previousShareHoldingModel.previousShareHoldingDescriptionModel.get.description
//        reviewShareHoldingsTableBody.select("tr").get(index).getElementById(s"share-nominal-value-$index").text() shouldBe
//          PreviousShareHoldingModel.getAmountAsFormattedString(previousShareHoldingModel.previousShareHoldingNominalValueModel.get.nominalValue)
        reviewShareHoldingsTableBody.select("tr").get(index).getElementById(s"share-purchase-date-$index").text() shouldBe
          PreviousShareHoldingModel.toDateString(previousShareHoldingModel.investorShareIssueDateModel.get.investorShareIssueDateDay.get,
            previousShareHoldingModel.investorShareIssueDateModel.get.investorShareIssueDateMonth.get,
            previousShareHoldingModel.investorShareIssueDateModel.get.investorShareIssueDateYear.get)
        reviewShareHoldingsTableBody.select("tr").get(index).getElementById(s"share-purchase-count-$index").text() shouldBe
          previousShareHoldingModel.numberOfPreviouslyIssuedSharesModel.get.previouslyIssuedShares.toString()
        if(previousShareHoldingModel.validate){
          reviewShareHoldingsTableBody.select("tr").get(index).getElementById(s"change-$index").text() shouldBe
            Messages("common.base.change")
          reviewShareHoldingsTableBody.select("tr").get(index).getElementById(s"change-$index").getElementById(s"change-ref-$index").attr("href")shouldBe
            controllers.seis.routes.PreviousShareHoldingsReviewController.change(
              previousShareHoldingModel.investorProcessingId.get, previousShareHoldingModel.processingId.get).toString
        }
        else{
          reviewShareHoldingsTableBody.select("tr").get(index).getElementById(s"continue-$index").text() shouldBe
            Messages("common.base.remove")
        }

        reviewShareHoldingsTableBody.select("tr").get(index).getElementById(s"remove-$index").text() shouldBe
          Messages("common.base.remove")
      }


      if(validModelWithPrevShareHoldings.validate){
        reviewShareHoldingsTableBody.select("tr").get(1).getElementById("add-share-holder").text() shouldBe
          Messages("page.investors.previousShareHoldingReview.add")
        reviewShareHoldingsTableBody.select("tr").get(1).getElementById("add-share-holder").attr("href") shouldBe
          controllers.seis.routes.PreviousShareHoldingDescriptionController.show(validModelWithPrevShareHoldings.processingId.get).toString
        document.body.getElementById("next").text() shouldEqual Messages("common.button.snc")
      }


    }
  }
}
