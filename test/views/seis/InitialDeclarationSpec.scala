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

import controllers.seis.routes
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import views.helpers.ViewSpec
import views.html.seis.checkAndSubmit.InitialDeclaration

class InitialDeclarationSpec extends ViewSpec {

  "The Initial Declaration page" should {

    val requestType = "GET"

    "Verify that the declaration page has the correct elements" in {
      val document: Document = {
        val page = InitialDeclaration()(fakeRequest, applicationMessages)
        Jsoup.parse(page.body)
      }
      document.title() shouldBe Messages("page.initial.declaration.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.initial.declaration.heading")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.routes.ApplicationHubController.show().url
      document.getElementById("description").text() shouldBe Messages("page.initial.declaration.description")
      document.getElementById("warningMessage").text() shouldBe Messages("page.declaration.warning")
      document.getElementById("next").text() shouldBe Messages("page.initial.declaration.submit")
      document.getElementById("help-bullet-one").text() shouldBe
        Messages("page.initial.declaration.description.bullet.one") + "" +
          " " + Messages("page.initial.declaration.description.bullet.two.seis") + " opens in a new window ."
      document.select("form").attr("method") shouldBe requestType
      document.select("form").attr("action") shouldBe routes.NatureOfBusinessController.show().url
      document.getElementById("do-not-agree").text() shouldBe Messages("page.declaration.doNotAgree")
      document.getElementById("do-not-agree").attr("href") shouldBe controllers.routes.ApplicationHubController.show().toString
    }
  }

}
