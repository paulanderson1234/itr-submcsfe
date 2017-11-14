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

import controllers.eis.routes
import models.KiProcessingModel
import org.jsoup.Jsoup
import play.api.i18n.Messages
import views.helpers.ViewSpec
import play.api.i18n.Messages.Implicits._
import views.html.eis.shareDetails.LifetimeAllowanceExceededError
import common.Constants

class LifetimeAllowanceExceededErrorSpec extends ViewSpec {

  "The LifetimeAllowanceExceededError page" should {
    "contain the expected elements for the warning page where the application is identified as Knowledge Intensive" in {
      val page = LifetimeAllowanceExceededError(kiProcessingModelIsKi)(fakeRequest, applicationMessages)
      val document = Jsoup.parse(page.body)

      document.title shouldEqual Messages("common.error.soft.title")
      document.body.getElementById("main-heading").text() shouldEqual Messages("common.error.soft.heading")
      document.body.getElementById("warning-description").text() shouldEqual
        Messages("page.LifetimeAllowanceExceededWarning.description", Constants.lifetimeLogicLimitKiToString)
      document.body.getElementById("what-next-heading").text() shouldEqual Messages("common.error.soft.secondaryHeading")
      document.body.getElementById("continue-text").text() shouldEqual Messages("common.error.soft.whatNext.compliance")

      document.body.getElementById("incorrect-info").text() shouldEqual Messages("common.changeAnswers.incorrect.text") +
        " " + Messages("common.changeAnswers.link") + "."
      document.body.getElementById("change-answers").attr("href") shouldEqual routes.TotalAmountRaisedController.show().url
      document.body.getElementById("back-link").attr("href") shouldEqual routes.TotalAmountRaisedController.show().url
      document.getElementById("next").text() shouldBe Messages("common.button.continue")
    }

    "contain the expected elements for the warning page where the application is not identified as Knowledge Intensive" in {
      val page = LifetimeAllowanceExceededError(kiProcessingModelNotMet)(fakeRequest, applicationMessages)
      val document = Jsoup.parse(page.body)

      document.title shouldEqual Messages("common.error.soft.title")
      document.body.getElementById("main-heading").text() shouldEqual Messages("common.error.soft.heading")
      document.body.getElementById("warning-description").text() shouldEqual
        Messages("page.LifetimeAllowanceExceededWarning.description", Constants.lifetimeLogicLimitNotKiToString)
      document.body.getElementById("what-next-heading").text() shouldEqual Messages("common.error.soft.secondaryHeading")
      document.body.getElementById("continue-text").text() shouldEqual Messages("common.error.soft.whatNext.compliance")

      document.body.getElementById("incorrect-info").text() shouldEqual Messages("common.changeAnswers.incorrect.text") +
        " " + Messages("common.changeAnswers.link") + "."
      document.body.getElementById("change-answers").attr("href") shouldEqual routes.TotalAmountRaisedController.show().url
      document.body.getElementById("back-link").attr("href") shouldEqual routes.TotalAmountRaisedController.show().url
      document.getElementById("next").text() shouldBe Messages("common.button.continue")
    }
  }
}
