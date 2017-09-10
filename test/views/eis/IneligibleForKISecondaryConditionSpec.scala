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

import org.jsoup.Jsoup
import play.api.i18n.Messages
import views.helpers.ViewSpec
import play.api.i18n.Messages.Implicits._
import views.html.eis.knowledgeIntensive.IneligibleForKISecondaryCondition

class IneligibleForKISecondaryConditionSpec extends ViewSpec {

  "The IneligibleForKI page" should {
    "contain the expected elements" in {
      val page = IneligibleForKISecondaryCondition()(fakeRequest, applicationMessages)
      val document = Jsoup.parse(page.body)

      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.TenYearPlanController.show().url
      document.body.getElementById("change-answers").attr("href") shouldEqual controllers.eis.routes.TenYearPlanController.show().url
      document.title shouldEqual Messages("page.knowledgeIntensive.IneligibleForKI.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.knowledgeIntensive.IneligibleForKI.heading")
      document.getElementById("description-one").text() shouldEqual Messages("page.knowledgeIntensive.IneligibleForKI.secondary.description.one")
      document.getElementById("reason-one").text() shouldEqual Messages("page.knowledgeIntensive.IneligibleForKI.secondary.bullet.one")
      document.getElementById("reason-two").text() shouldEqual Messages("page.knowledgeIntensive.IneligibleForKI.secondary.bullet.two")
      document.getElementById("what-next-heading").text() shouldEqual Messages("common.error.soft.secondaryHeading")
      document.getElementById("next").text() shouldEqual Messages("common.button.continue")
      document.body.getElementById("incorrect-info").text() shouldEqual Messages("common.changeAnswers.incorrect.text") +
        " " + Messages("common.changeAnswers.link") + "."
    }
  }
}

