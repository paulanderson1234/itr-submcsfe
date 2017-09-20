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

import controllers.helpers.BaseSpec
import controllers.eis.routes
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import views.html.eis.investors.DeleteSharesRepayment

class DeleteSharesRepaymentSpec  extends BaseSpec {

  "The Delete Shares Repayment page" should {

    "contain the correct elements when a valid share repayment is passed to the view" in {

      lazy val page = DeleteSharesRepayment(validSharesRepaymentDetails)(fakeRequest, applicationMessages)
      lazy val document = Jsoup.parse(page.body)

      //title and heading
      document.title() shouldBe Messages("page.repayments.deleteSharesRepayment.title")

      document.body.getElementById("main-heading").text() shouldBe Messages("page.repayments.deleteSharesRepayment.heading")

      document.body.getElementById("info").text() shouldBe
        Messages("page.repayments.deleteSharesRepayment.description",
          validSharesRepaymentDetails.whoRepaidSharesModel.get.forename ++ " " ++ validSharesRepaymentDetails.whoRepaidSharesModel.get.surname)

      document.body.getElementById("remove-button").text() shouldBe Messages("common.base.remove")
      document.body.getElementById("remove-button").hasClass("button--alert") shouldBe true
      document.body.getElementById("delete-sharesRepayment-cancel").text() shouldBe Messages("common.button.cancel")

      document.body.getElementById("delete-sharesRepayment-cancel").attr("href") shouldEqual routes.ReviewPreviousRepaymentsController.show().url
    }


    "contain the correct elements when a invalid  share repayment is passed to the view" in {

      lazy val page = DeleteSharesRepayment(sharesRepaymentDetailsMissingRecipient)(fakeRequest, applicationMessages)
      lazy val document = Jsoup.parse(page.body)

      //title and heading
      document.title() shouldBe Messages("page.repayments.deleteSharesRepayment.title")

      document.body.getElementById("main-heading").text() shouldBe Messages("page.repayments.deleteSharesRepayment.heading")

      document.body.select("info").isEmpty shouldBe true

      document.body.getElementById("remove-button").text() shouldBe Messages("common.base.remove")
      document.body.getElementById("remove-button").hasClass("button--alert") shouldBe true
      document.body.getElementById("delete-sharesRepayment-cancel").text() shouldBe Messages("common.button.cancel")

      document.body.getElementById("delete-sharesRepayment-cancel").attr("href") shouldEqual routes.ReviewPreviousRepaymentsController.show().url
    }

  }

}