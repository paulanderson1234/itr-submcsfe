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

import auth.{MockAuthConnector, MockConfig}
import common.KeystoreKeys
import connectors.SubmissionConnector
import controllers.eis.HasInvestmentTradeStartedController
import models.HasInvestmentTradeStartedModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito.when
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._
import views.helpers.ViewSpec

import scala.concurrent.Future

class HasInvestmentTradeStartedSpec extends ViewSpec {

  object TestController extends HasInvestmentTradeStartedController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
    override lazy val submissionConnector = mockSubmissionConnector

  }

  def setupMocks(hasInvestmentTradeStartedModelModel: Option[HasInvestmentTradeStartedModel] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[HasInvestmentTradeStartedModel](Matchers.eq(KeystoreKeys.hasInvestmentTradeStarted))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(hasInvestmentTradeStartedModelModel))
  }

  "The Has Investment Trade Started Page" should {
    "Verify that has investment trade started page contains the correct " +
      "elements when a valid HasInvestmentTradeStartedModel is passed" in new Setup {
      val document: Document = {
        setupMocks(Some(hasInvestmentTradeStartedModelYes))
        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.companyDetails.HasInvestmentTradeStarted.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.HasInvestmentTradeStarted.heading")
      document.getElementById("description-one").text() shouldBe Messages("page.companyDetails.HasInvestmentTradeStarted.descriptionOne.text")
      document.body.getElementsByClass("form-hint").text should include(Messages("common.date.hint.example"))
      document.body.getElementById("hasInvestmentTradeStartedDay").parent.text shouldBe Messages("common.date.fields.day")
      document.body.getElementById("hasInvestmentTradeStartedMonth").parent.text shouldBe Messages("common.date.fields.month")
      document.body.getElementById("hasInvestmentTradeStartedYear").parent.text shouldBe Messages("common.date.fields.year")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.QualifyBusinessActivityController.show().url
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.details.one")
      document.getElementById("hasInvestmentTradeStarted").select(".visuallyhidden").text() shouldBe Messages("page.companyDetails.HasInvestmentTradeStarted.question.hint")
      document.body.getElementById("question-date-text-legend-id").text shouldBe Messages("page.companyDetails.HasInvestmentTradeStarted.question.hint")
      document.select(".error-summary").isEmpty shouldBe true

    }

    "Verify that has investment trade started page shows correct form data error " +
      "elements when a invalid HasInvestmentTradeStartedModel is submitted" in new Setup {
      val document: Document = {
        setupMocks(Some(hasInvestmentTradeStartedModelYes))
        val result = TestController.submit.apply(authorisedFakeRequest.withFormUrlEncodedBody("hasInvestmentTradeStarted"->""))
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.companyDetails.HasInvestmentTradeStarted.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.HasInvestmentTradeStarted.heading")
      document.getElementById("description-one").text() shouldBe Messages("page.companyDetails.HasInvestmentTradeStarted.descriptionOne.text")
      document.body.getElementsByClass("form-hint").text should include(Messages("common.date.hint.example"))
      document.body.getElementById("hasInvestmentTradeStartedDay").parent.text shouldBe Messages("common.date.fields.day")
      document.body.getElementById("hasInvestmentTradeStartedMonth").parent.text shouldBe Messages("common.date.fields.month")
      document.body.getElementById("hasInvestmentTradeStartedYear").parent.text shouldBe Messages("common.date.fields.year")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.QualifyBusinessActivityController.show().url
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.details.one")
      document.getElementById("hasInvestmentTradeStarted").select(".visuallyhidden").text() shouldBe Messages("page.companyDetails.HasInvestmentTradeStarted.question.hint")
      document.body.getElementById("question-date-text-legend-id").text shouldBe Messages("page.companyDetails.HasInvestmentTradeStarted.question.hint")
      document.getElementById("error-summary-display").hasClass("error-summary--show") shouldBe true
    }
  }
}
