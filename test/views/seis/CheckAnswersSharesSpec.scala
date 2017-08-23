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
import models.{TotalAmountRaisedModel, TotalAmountSpentModel, WasAnyValueReceivedModel}
import models.investorDetails.{InvestorDetailsModel, PreviousShareHoldingModel}
import models.seis.SEISCheckAnswersModel
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import views.helpers.CheckAnswersSpec
import views.html.seis.checkAndSubmit.CheckAnswers

class CheckAnswersSharesSpec extends CheckAnswersSpec {

  val totalAmountRaise = Some(TotalAmountRaisedModel(12345))
  val totalAmountSpent = Some(TotalAmountSpentModel(12345))
  "The Check Answers page" should {

    "Verify that the Check Answers page contains the correct elements for Section 3: Investment" +
      " when it is fully populated with investment models" in {
      val model = SEISCheckAnswersModel(None, None, None, None, Vector(), None, None, None, None, None, None, None,
        None, Some(shareDescriptionModel), Some(numberOfSharesModel), totalAmountRaise,
        totalAmountSpent, None, None, None, None, false)
      val page = CheckAnswers(model)(authorisedFakeRequest, applicationMessages)
      val document = Jsoup.parse(page.body)

      lazy val sharesTableTbody = document.getElementById("shares-table").select("tbody")

      //Section table heading
      document.getElementById("sharesSection-table-heading").text() shouldBe Messages("page.summaryQuestion.shares")

      sharesTableTbody.select("tr").get(0).getElementById("shareDescription-question").text() shouldBe
        Messages("page.summaryQuestion.shareClass")
      sharesTableTbody.select("tr").get(0).getElementById("shareDescription-answer").text() shouldBe
        shareDescriptionModel.shareDescription
      sharesTableTbody.select("tr").get(0).getElementById("shareDescription-link")
        .attr("href") shouldEqual controllers.seis.routes.ShareDescriptionController.show().url

      sharesTableTbody.select("tr").get(1).getElementById("numberOfShares-question").text() shouldBe
        Messages("page.summaryQuestion.sharesIssues")
      sharesTableTbody.select("tr").get(1).getElementById("numberOfShares-answer").text() shouldBe
        numberOfSharesModel.numberOfShares.toString()
      sharesTableTbody.select("tr").get(1).getElementById("numberOfShares-link")
        .attr("href") shouldEqual controllers.seis.routes.NumberOfSharesController.show().url

      sharesTableTbody.select("tr").get(2).getElementById("totalAmountRaised-question").text() shouldBe
        Messages("page.summaryQuestion.amountRaised")
      sharesTableTbody.select("tr").get(2).getElementById("totalAmountRaised-answer").text() shouldBe
        PreviousShareHoldingModel.getAmountAsFormattedString(totalAmountRaise.get.amount)
      sharesTableTbody.select("tr").get(2).getElementById("totalAmountRaised-link")
        .attr("href") shouldEqual controllers.seis.routes.TotalAmountRaisedController.show().url

      sharesTableTbody.select("tr").get(3).getElementById("totalAmountSpent-question").text() shouldBe
        Messages("page.summaryQuestion.amountSpent")
      sharesTableTbody.select("tr").get(3).getElementById("totalAmountSpent-answer").text() shouldBe
        PreviousShareHoldingModel.getAmountAsFormattedString(totalAmountSpent.get.totalAmountSpent)
      sharesTableTbody.select("tr").get(3).getElementById("totalAmountSpent-link")
        .attr("href") shouldEqual controllers.seis.routes.TotalAmountSpentController.show().url
    }

    "Verify that the Check Answers page contains an empty table for Section 3: Investment" +
      " when the investment models are empty" in new SEISSetup {
      val model = SEISCheckAnswersModel(None, None, None, None, Vector(),None, None, None, None, None, None, None,
        None, None, None, None, None, None,None, None, None, false)
      val page = CheckAnswers(model)(authorisedFakeRequest, applicationMessages)
      val document = Jsoup.parse(page.body)

      lazy val sharesTableTbody = document.getElementById("shares-table").select("tbody")
      lazy val notAvailableMessage = Messages("common.notAvailable")

      //Section table heading
      document.getElementById("sharesSection-table-heading").text() shouldBe Messages("page.summaryQuestion.shares")
      sharesTableTbody.select("tr").size() shouldBe 0
    }
  }
}

