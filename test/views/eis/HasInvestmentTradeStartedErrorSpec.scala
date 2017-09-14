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

      document.title shouldEqual Messages("page.companyDetails.HasInvestmentTradeStartedError.title")
      document.body.getElementById("main-heading").text() shouldEqual Messages("page.companyDetails.HasInvestmentTradeStartedError.heading")
      document.body.getElementById("error-description").text() shouldEqual Messages("page.companyDetails.HasInvestmentTradeStartedError.description")
     
      document.body.getElementById("incorrect-info").text() shouldEqual Messages("page.companyDetails.HasInvestmentTradeStartedError.incorrect.info.start") + " " + Messages("common.changeAnswers.link") + " " + Messages("page.companyDetails.HasInvestmentTradeStartedError.incorrect.info.end")
      document.body.getElementById("change-answers").attr("href") shouldEqual routes.HasInvestmentTradeStartedController.show().url
      document.body.getElementById("back-link").attr("href") shouldEqual routes.HasInvestmentTradeStartedController.show().url
      document.body.getElementById("return-dashboard").text() shouldEqual Messages("common.returnToDashboard")
      document.body.getElementById("return-dashboard").attr("href") shouldEqual controllers.routes.ApplicationHubController.show().url
    }
  }
}
