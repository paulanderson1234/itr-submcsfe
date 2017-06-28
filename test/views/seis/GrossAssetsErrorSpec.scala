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

import auth.{MockAuthConnector, MockConfig}
import controllers.seis.{GrossAssetsErrorController, routes}
import org.jsoup.Jsoup
import play.api.i18n.Messages
import views.helpers.ViewSpec
import play.api.i18n.Messages.Implicits._
import views.html.seis.companyDetails.GrossAssetsError

class GrossAssetsErrorSpec extends ViewSpec {

  object TestController extends GrossAssetsErrorController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
    override lazy val s4lConnector = mockS4lConnector
  }

  "The Gross Assets error page" should {

    "Verify that page has the expected eleemnts" in {
      val page = GrossAssetsError()(fakeRequest, applicationMessages)
      val document = Jsoup.parse(page.body)

      document.title shouldEqual Messages("page.companyDetails.grossAssetsError.title")
      document.body.getElementById("main-heading").text() shouldEqual Messages("page.companyDetails.grossAssetsError.heading")
      document.body.getElementById("error-description").text() shouldEqual Messages("page.companyDetails.grossAssetsError.description")
      document.body.getElementById("what-next-heading").text() shouldEqual Messages("page.companyDetails.grossAssetsError.whatNext.heading")
      document.body.getElementById("continue-text").text() shouldEqual Messages("page.companyDetails.grossAssetsError.whatNext.continue")

      document.body.getElementById("incorrect-info").text() shouldEqual Messages("page.companyDetails.grossAssetsError.incorrect.info") +
        " " + Messages("page.companyDetails.grossAssetsError.link.changeAnswers") + "."
      document.body.getElementById("change-answers").attr("href") shouldEqual controllers.seis.routes.GrossAssetsController.show().url
      document.body.getElementById("back-link").attr("href") shouldEqual routes.GrossAssetsController.show().url
      document.getElementById("next").text() shouldBe Messages("common.button.continue")
    }
  }
}
