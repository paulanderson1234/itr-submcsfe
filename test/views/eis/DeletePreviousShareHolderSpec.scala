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
import models.investorDetails.PreviousShareHoldingModel
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import views.html.eis.investors.DeletePreviousShareHolder

class DeletePreviousShareHolderSpec  extends BaseSpec {

  val shareHoldersModel = PreviousShareHoldingModel(Some(investorShareIssueDateModel1), Some(numberOfPreviouslyIssuedSharesModel1),
    Some(previousShareHoldingNominalValueModel1), Some(previousShareHoldingDescriptionModel1), Some(1), Some(2))

  "The previous share holder delete page" should {

    "contain the correct elements when loaded share holders details" in {

      lazy val page = DeletePreviousShareHolder(shareHoldersModel)(fakeRequest, applicationMessages)
      lazy val document = Jsoup.parse(page.body)

      //title and heading
      document.title() shouldBe Messages("page.investors.DeletePreviousShareHolder.title")

      document.body.getElementById("main-heading").text() shouldBe Messages("page.investors.DeletePreviousShareHolder.heading")
      if(shareHoldersModel.investorShareIssueDateModel.isDefined){
        document.body.getElementById("share-holding-delete-hint").text() shouldBe
          Messages("page.investors.DeletePreviousShareHolder.message.one",
            shareHoldersModel.previousShareHoldingDescriptionModel.get.description) +" " + Messages("page.investors.DeletePreviousShareHolder.message.two",
            PreviousShareHoldingModel.toDateString(shareHoldersModel.investorShareIssueDateModel.get.investorShareIssueDateDay.get,
              shareHoldersModel.investorShareIssueDateModel.get.investorShareIssueDateMonth.get,
              shareHoldersModel.investorShareIssueDateModel.get.investorShareIssueDateYear.get)) + " " +
            Messages("page.investors.DeletePreviousShareHolder.message.three")
      }

      document.body.getElementById("share-holding-remove").text() shouldBe Messages("common.base.remove")
      document.body.getElementById("share-holding-cancel").text() shouldBe Messages("common.button.cancel")
      
    }
  }

}
