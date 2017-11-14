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
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import views.helpers.ViewSpec
import views.html.eis.companyDetails.HasInvestmentTradeStartedError

class HasInvestmentTradeStartedErrorSpec extends ViewSpec {
 
  "The HasInvestmentTradeStartedError page" should {
    "contain the expected elements" in {
      val page = HasInvestmentTradeStartedError()(fakeRequest, applicationMessages)
      val document = Jsoup.parse(page.body)

      document.title shouldEqual Messages("common.error.hard.title")
      document.body.getElementById("main-heading").text() shouldEqual Messages("common.error.hard.heading")
      document.body.getElementById("error-description").text() shouldEqual Messages("common.error.hard.description")
     
      document.body.getElementById("incorrect-info").text() shouldEqual Messages("common.error.hard.incorrect.info.start") + " " + Messages("common.changeAnswers.link") + " " + Messages("common.error.hard.incorrect.info.end")
      document.body.getElementById("change-answers").attr("href") shouldEqual routes.HasInvestmentTradeStartedController.show().url
      document.body.getElementById("back-link").attr("href") shouldEqual routes.HasInvestmentTradeStartedController.show().url
      document.body.getElementById("return-dashboard").text() shouldEqual Messages("common.returnToDashboard")
      document.body.getElementById("return-dashboard").attr("href") shouldEqual controllers.routes.HomeController.redirectToHub().url
    }
  }
}
