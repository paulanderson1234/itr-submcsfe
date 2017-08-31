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

import auth.{MockAuthConnector, MockConfigEISFlow, MockConfigSingleFlow}
import common.KeystoreKeys
import controllers.eis.ShareDescriptionController
import models.ShareDescriptionModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.i18n.Messages
import play.api.test.Helpers._
import views.helpers.ViewSpec
import play.api.i18n.Messages.Implicits._
import forms.ShareDescriptionForm._
import views.html.eis.checkAndSubmit.CheckAnswers
import views.html.eis.shareDetails.ShareDescription

import scala.concurrent.Future


class ShareDescriptionSpec extends ViewSpec {

  "The share description page" should {
    "show the correct elements" when {
      "there is no share description model" in {
        val page = ShareDescription(shareDescriptionForm,
          controllers.eis.routes.HadOtherInvestmentsController.show().toString)(authorisedFakeRequest, applicationMessages)
        val document = Jsoup.parse(page.body)

        document.title() shouldBe Messages("page.shares.shareDescription.title")
        document.getElementById("main-heading").text() shouldBe Messages("page.shares.shareDescription.heading")
        document.getElementById("description-two").text() shouldBe Messages("page.shares.shareDescription.example.text")
        document.getElementById("bullet-one").text() shouldBe Messages("page.shares.shareDescription.bullet.one")
        document.getElementById("bullet-two").text() shouldBe Messages("page.shares.shareDescription.bullet.two")
        document.getElementById("bullet-three").text() shouldBe Messages("page.shares.shareDescription.bullet.three")
        document.getElementById("labelTextId").text() shouldBe Messages("page.shares.shareDescription.heading")
        document.getElementById("help").text() shouldBe Messages("page.shares.shareDescription.whereToFind")
        document.getElementById("share-description-where-to-find").text() shouldBe Messages("page.shares.ShareDescription.location")
      }

      "there is a share description model" in {
        val shareDescriptionModel = ShareDescriptionModel("")
        val page = ShareDescription(shareDescriptionForm.fill(shareDescriptionModel),
          controllers.eis.routes.HadOtherInvestmentsController.show().toString)(authorisedFakeRequest, applicationMessages)
        val document = Jsoup.parse(page.body)

        document.title() shouldBe Messages("page.shares.shareDescription.title")
        document.getElementById("main-heading").text() shouldBe Messages("page.shares.shareDescription.heading")
        document.getElementById("description-two").text() shouldBe Messages("page.shares.shareDescription.example.text")
        document.getElementById("bullet-one").text() shouldBe Messages("page.shares.shareDescription.bullet.one")
        document.getElementById("bullet-two").text() shouldBe Messages("page.shares.shareDescription.bullet.two")
        document.getElementById("bullet-three").text() shouldBe Messages("page.shares.shareDescription.bullet.three")
        document.getElementById("labelTextId").text() shouldBe Messages("page.shares.shareDescription.heading")
        document.getElementById("help").text() shouldBe Messages("page.shares.shareDescription.whereToFind")
        document.getElementById("share-description-where-to-find").text() shouldBe Messages("page.shares.ShareDescription.location")
      }
    }
  }



}
