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

import auth.{MockAuthConnector, MockConfigSingleFlow}
import common.KeystoreKeys
import controllers.seis.{InvestorShareIssueDateController, routes}
import models.investorDetails._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import views.helpers.ViewSpec

import scala.concurrent.Future

class InvestorShareIssueDateSpec extends ViewSpec {


  object TestController extends InvestorShareIssueDateController {
    override lazy val applicationConfig = MockConfigSingleFlow
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  def setupMocks(investorShareIssueDateModel: Option[InvestorShareIssueDateModel] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[InvestorShareIssueDateModel](Matchers.eq(KeystoreKeys.investorShareIssueDate))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(investorShareIssueDateModel))

    when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(CacheMap("", Map())))
  }

  "The Investor Share Issue Date page" should {

    "Verify that Investor Share Issue Date page contains the correct elements when a valid InvestorShareIssueDateModel " +
      "is passed with expected url" in new SEISSetup {
      val document: Document = {
        setupMocks(Some(investorShareIssueDateModel))
        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.investors.investorShareIssueDate.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.investors.investorShareIssueDate.heading")
      document.body.getElementsByClass("form-hint").text should include(Messages("common.date.hint.example"))
      document.body.getElementById("investorShareIssueDateDay").parent.text shouldBe Messages("common.date.fields.day")
      document.body.getElementById("investorShareIssueDateMonth").parent.text shouldBe Messages("common.date.fields.month")
      document.body.getElementById("investorShareIssueDateYear").parent.text shouldBe Messages("common.date.fields.year")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.seis.routes.InvestorShareIssueDateController.show().url
      //route should equal Existing Shareholders? to be changed once others merged
      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.four")
    }

    "Verify that Investor Share Issue Date page contains the correct elements when a valid ShareIssueDateModel is passed with alternate url" in new SEISSetup {
      val document: Document = {
        setupMocks(Some(investorShareIssueDateModel))
        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.investors.investorShareIssueDate.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.investors.investorShareIssueDate.heading")
      document.body.getElementsByClass("form-hint").text should include(Messages("common.date.hint.example"))
      document.body.getElementById("investorShareIssueDateDay").parent.text shouldBe Messages("common.date.fields.day")
      document.body.getElementById("investorShareIssueDateMonth").parent.text shouldBe Messages("common.date.fields.month")
      document.body.getElementById("investorShareIssueDateYear").parent.text shouldBe Messages("common.date.fields.year")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.seis.routes.InvestorShareIssueDateController.show().url
      //route should equal Existing Shareholders? to be changed once others merged
      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.four")
    }

    "Verify that the Investor Share Issue Date page contains the correct elements when an invalid ShareIssueDateModel is passed" in new SEISSetup {
      val document: Document = {
        setupMocks(None)
        val result = TestController.submit.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.investors.investorShareIssueDate.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.investors.investorShareIssueDate.heading")
      document.body.getElementsByClass("form-hint").text should include(Messages("common.date.hint.example"))
      document.body.getElementById("investorShareIssueDateDay").parent.text shouldBe Messages("common.date.fields.day")
      document.body.getElementById("investorShareIssueDateMonth").parent.text shouldBe Messages("common.date.fields.month")
      document.body.getElementById("investorShareIssueDateYear").parent.text shouldBe Messages("common.date.fields.year")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.seis.routes.InvestorShareIssueDateController.show().url
      //route should equal Existing Shareholders? to be changed once others merged
      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.four")
      document.getElementById("error-summary-display").hasClass("error-summary--show")

    }

  }

}
