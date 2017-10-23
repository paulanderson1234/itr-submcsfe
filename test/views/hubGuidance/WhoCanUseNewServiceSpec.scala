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

package views.hubGuidance

import common.Constants
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._
import views.helpers.ViewSpec
import views.html.hubGuidance.WhoCanUseNewService


class WhoCanUseNewServiceSpec extends ViewSpec {


  "The Who can use our service page" should {

    "contain the correct elements when loaded" in {

      lazy val page = WhoCanUseNewService()(fakeRequest, applicationMessages)
      lazy val document = Jsoup.parse(contentAsString(page))

      //title
      document.title() shouldBe Messages("page.hubGuidance.whoCanUseNewService.title")

      //main heading
      document.getElementById("main-heading").text() shouldBe Messages("page.hubGuidance.whoCanUseNewService.heading")

      //description
      document.body.getElementById("description").text() shouldBe Messages("page.hubGuidance.whoCanUseNewService.apply.if")
      document.body.getElementById("reason-one").text() shouldBe Messages("page.hubGuidance.whoCanUseNewService.apply.if.one")
      document.body.getElementById("reason-two").text() shouldBe Messages("page.hubGuidance.whoCanUseNewService.apply.if.two")
      document.body.getElementById("reason-three").text() shouldBe Messages("page.hubGuidance.whoCanUseNewService.apply.if.three")
      document.body.getElementById("reason-four").text() shouldBe Messages("page.hubGuidance.whoCanUseNewService.apply.if.four")
      document.body.getElementById("sub-heading").text() shouldBe Messages("page.hubGuidance.whoCanUseNewService.conditions.apply")
      document.body.getElementById("still-apply").text() shouldBe
        Messages("page.hubGuidance.whoCanUseNewService.still.apply.one") + " " + Constants.schemeTypeEis + " " +
          "or " + Constants.schemeTypeSeis + " " + Messages("page.hubGuidance.whoCanUseNewService.still.apply.two")

      //link
      document.body.getElementById("eis-guidance").text() shouldBe Constants.schemeTypeEis
      document.body.getElementById("seis-guidance").text() shouldBe Constants.schemeTypeSeis
      document.body.getElementById("eis-guidance").attr("href") shouldBe Constants.eisGuidanceRedirectUrl
      document.body.getElementById("seis-guidance").attr("href") shouldBe Constants.seisGuidanceRedirectUrl

      //continue button
      document.body.getElementById("next").text() shouldBe Messages("common.button.continue")
    }

  }
}
