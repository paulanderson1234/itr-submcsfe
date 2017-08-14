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

import controllers.helpers.BaseSpec
import controllers.seis.DeleteInvestorController
import models.investorDetails.{InvestorDetailsModel, PreviousShareHoldingModel}
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import views.html.seis.investors.DeleteInvestor

class DeleteInvestorSpec  extends BaseSpec {

  "The delete investor page" should {

    "contain the correct elements for an investor (individual)" in {

      lazy val page = DeleteInvestor(investor1)(fakeRequest, applicationMessages)
      lazy val document = Jsoup.parse(page.body)

      //title and heading
      document.title() shouldBe Messages("page.investors.deleteInvestor.title", investor1.investorOrNomineeModel.get.addInvestorOrNominee.toLowerCase())
      document.title() shouldBe "Confirm that you want to remove this investor"

      document.body.getElementById("main-heading").text() shouldBe
      Messages("page.investors.deleteInvestor.heading", investor1.investorOrNomineeModel.get.addInvestorOrNominee.toLowerCase())
      document.body.getElementById("main-heading").text() shouldBe "Confirm that you want to remove this investor"

      document.body.getElementById("info").text() shouldBe
        Messages("page.investors.deleteInvestor.confirm", investor1.investorNomineeDescription)
      document.body.getElementById("info").text() shouldBe "This will remove Sam West from your list of investors and nominees."

      document.body.getElementById("remove-button").text() shouldBe Messages("common.base.remove")
      document.body.getElementById("remove-button").hasClass("button--alert") shouldBe true
      document.body.getElementById("delete-investor-cancel").text() shouldBe Messages("common.button.cancelRemove")
      //TODO href should be all investors when available
      document.body.getElementById("delete-investor-cancel").attr("href") shouldEqual controllers.seis.routes.TotalAmountRaisedController.show().url
    }

    "contain the correct elements for a nominee company" in {

      lazy val page = DeleteInvestor(investor2)(fakeRequest, applicationMessages)
      lazy val document = Jsoup.parse(page.body)

      //title and heading
      document.title() shouldBe Messages("page.investors.deleteInvestor.title", investor2.investorOrNomineeModel.get.addInvestorOrNominee.toLowerCase())
      document.title() shouldBe "Confirm that you want to remove this nominee"

      document.body.getElementById("main-heading").text() shouldBe
        Messages("page.investors.deleteInvestor.heading", investor2.investorOrNomineeModel.get.addInvestorOrNominee.toLowerCase())
      document.body.getElementById("main-heading").text() shouldBe "Confirm that you want to remove this nominee"

      document.body.getElementById("info").text() shouldBe
        Messages("page.investors.deleteInvestor.confirm", investor2.investorNomineeDescription)
      document.body.getElementById("info").text() shouldBe "This will remove Ben's Boots Ltd. from your list of investors and nominees."

      document.body.getElementById("remove-button").text() shouldBe Messages("common.base.remove")
      document.body.getElementById("remove-button").hasClass("button--alert") shouldBe true
      document.body.getElementById("delete-investor-cancel").text() shouldBe Messages("common.button.cancelRemove")
      //TODO href should be all investors when available
      document.body.getElementById("delete-investor-cancel").attr("href") shouldEqual controllers.seis.routes.TotalAmountRaisedController.show().url
    }

    "contain the correct elements for an investor but no company or individual option has been selected" in {

      lazy val page = DeleteInvestor(investor5)(fakeRequest, applicationMessages)
      lazy val document = Jsoup.parse(page.body)

      //title and heading
      document.title() shouldBe Messages("page.investors.deleteInvestor.title", investor5.investorOrNomineeModel.get.addInvestorOrNominee.toLowerCase())
      document.title() shouldBe "Confirm that you want to remove this investor"

      document.body.getElementById("main-heading").text() shouldBe
        Messages("page.investors.deleteInvestor.heading", investor5.investorOrNomineeModel.get.addInvestorOrNominee.toLowerCase())
      document.body.getElementById("main-heading").text() shouldBe "Confirm that you want to remove this investor"

      document.body.getElementById("info").text() shouldBe
        Messages("page.investors.deleteInvestor.confirm.noName", investor5.investorOrNomineeModel.get.addInvestorOrNominee.toLowerCase())
      document.body.getElementById("info").text() shouldBe "This investor will be permanently removed from your list of investors and nominees."

      document.body.getElementById("remove-button").text() shouldBe Messages("common.base.remove")
      document.body.getElementById("remove-button").hasClass("button--alert") shouldBe true
      document.body.getElementById("delete-investor-cancel").text() shouldBe Messages("common.button.cancelRemove")
      //TODO href should be all investors when available
      document.body.getElementById("delete-investor-cancel").attr("href") shouldEqual controllers.seis.routes.TotalAmountRaisedController.show().url
    }

    "contain the correct elements for an nominee/company but no company details have been entered" in {

      lazy val page = DeleteInvestor(investor4)(fakeRequest, applicationMessages)
      lazy val document = Jsoup.parse(page.body)

      //title and heading
      document.title() shouldBe Messages("page.investors.deleteInvestor.title", investor4.investorOrNomineeModel.get.addInvestorOrNominee.toLowerCase())
      document.title() shouldBe "Confirm that you want to remove this nominee"

      document.body.getElementById("main-heading").text() shouldBe
        Messages("page.investors.deleteInvestor.heading", investor4.investorOrNomineeModel.get.addInvestorOrNominee.toLowerCase())
      document.body.getElementById("main-heading").text() shouldBe "Confirm that you want to remove this nominee"

      document.body.getElementById("info").text() shouldBe
        Messages("page.investors.deleteInvestor.confirm.noName", investor4.investorOrNomineeModel.get.addInvestorOrNominee.toLowerCase())
      document.body.getElementById("info").text() shouldBe "This nominee will be permanently removed from your list of investors and nominees."

      document.body.getElementById("remove-button").text() shouldBe Messages("common.base.remove")
      document.body.getElementById("remove-button").hasClass("button--alert") shouldBe true
      document.body.getElementById("delete-investor-cancel").text() shouldBe Messages("common.button.cancelRemove")
      //TODO href should be all investors when available
      document.body.getElementById("delete-investor-cancel").attr("href") shouldEqual controllers.seis.routes.TotalAmountRaisedController.show().url
    }

    "contain the correct elements for an investor/individual but no individual details have been entered" in {

      lazy val page = DeleteInvestor(investor3)(fakeRequest, applicationMessages)
      lazy val document = Jsoup.parse(page.body)

      //title and heading
      document.title() shouldBe Messages("page.investors.deleteInvestor.title", investor3.investorOrNomineeModel.get.addInvestorOrNominee.toLowerCase())
      document.title() shouldBe "Confirm that you want to remove this investor"

      document.body.getElementById("main-heading").text() shouldBe
        Messages("page.investors.deleteInvestor.heading", investor3.investorOrNomineeModel.get.addInvestorOrNominee.toLowerCase())
      document.body.getElementById("main-heading").text() shouldBe "Confirm that you want to remove this investor"

      document.body.getElementById("info").text() shouldBe
        Messages("page.investors.deleteInvestor.confirm.noName", investor3.investorOrNomineeModel.get.addInvestorOrNominee.toLowerCase())
      document.body.getElementById("info").text() shouldBe "This investor will be permanently removed from your list of investors and nominees."

      document.body.getElementById("remove-button").text() shouldBe Messages("common.base.remove")
      document.body.getElementById("remove-button").hasClass("button--alert") shouldBe true
      document.body.getElementById("delete-investor-cancel").text() shouldBe Messages("common.button.cancelRemove")
      //TODO href should be all investors when available
      document.body.getElementById("delete-investor-cancel").attr("href") shouldEqual controllers.seis.routes.TotalAmountRaisedController.show().url
    }

  }

}
