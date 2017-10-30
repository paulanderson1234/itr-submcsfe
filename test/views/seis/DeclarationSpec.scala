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

import controllers.seis.{routes}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import views.helpers.ViewSpec
import views.html.seis.checkAndSubmit.Declaration

class DeclarationSpec extends ViewSpec {

  "The Declaration page" should {

    val requestType = "POST"

    "Verify that the declaration page has the correct elements" in {
      val document: Document = {
        val page = Declaration()(fakeRequest, applicationMessages)
        Jsoup.parse(page.body)
      }
      document.title() shouldBe Messages("page.declaration.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.declaration.heading")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.CheckAnswersController.show().url
      document.getElementById("description").text() shouldBe Messages("page.declaration.description")
      document.getElementById("warningMessage").text() shouldBe Messages("page.declaration.warning")
      document.getElementById("next").text() shouldBe Messages("page.declaration.submit")
      document.select("form").attr("method") shouldBe requestType
      document.select("form").attr("action") shouldBe routes.DeclarationController.submit().url
      document.getElementById("do-not-agree").text() shouldBe Messages("page.declaration.doNotAgree")
      document.getElementById("do-not-agree").attr("href") shouldBe controllers.routes.ApplicationHubController.show().toString
    }
  }

}
