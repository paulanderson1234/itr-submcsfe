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
import controllers.seis.SeventyPercentSpentController
import models.SeventyPercentSpentModel
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

class SeventyPercentSpentSpec extends ViewSpec {

  val testUrl = "/test/test"
  val testUrlOther = "/test/test/testanother"

  object TestController extends SeventyPercentSpentController {
    override lazy val applicationConfig = MockConfigSingleFlow
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  def setupMocks(seventyPercentSpentModel: Option[SeventyPercentSpentModel] = None, backLink: Option[String] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[SeventyPercentSpentModel](Matchers.eq(KeystoreKeys.seventyPercentSpent))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(seventyPercentSpentModel))

    when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(CacheMap("", Map())))

    when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkSeventyPercentSpent))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(backLink))

    when(mockS4lConnector.saveFormData(Matchers.eq(KeystoreKeys.backLinkShareIssueDate),
      Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(CacheMap("", Map())))
  }


  "The Is this the first trade your company has carried out page" should {

    "Verify that the page contains the correct elements when a valid model is passed from keystore with expected url" in new SEISSetup {
      val document: Document = {
        setupMocks(Some(isSeventyPercentSpentModelYes), Some(testUrl))
        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.companyDetails.seventyPercentSpent.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.seventyPercentSpent.heading")
      document.getElementById("main-heading").hasClass("h1-heading")
      document.getElementById("isSeventyPercentSpent-yesLabel").text() shouldBe Messages("common.radioYesLabel")
      document.getElementById("isSeventyPercentSpent-noLabel").text() shouldBe Messages("common.radioNoLabel")
      document.body.getElementById("back-link").attr("href") shouldEqual testUrl
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.details.one")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.getElementById("seventyPercentSpent-legend").hasClass("visuallyhidden") shouldBe true
      document.getElementById("seventyPercentSpent-legend").text shouldBe Messages("page.companyDetails.seventyPercentSpent.legend")
    }

    "Verify that the page contains the correct elements when a valid IsFirstTRadeModel is passed from keystore with alternate url" in new SEISSetup {
      val document: Document = {
        setupMocks(Some(isSeventyPercentSpentModelYes), Some(testUrlOther))
        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.companyDetails.seventyPercentSpent.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.seventyPercentSpent.heading")
      document.getElementById("main-heading").hasClass("h1-heading")
      document.getElementById("isSeventyPercentSpent-yesLabel").text() shouldBe Messages("common.radioYesLabel")
      document.getElementById("isSeventyPercentSpent-noLabel").text() shouldBe Messages("common.radioNoLabel")
      document.body.getElementById("back-link").attr("href") shouldEqual testUrlOther
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.details.one")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.getElementById("seventyPercentSpent-legend").hasClass("visuallyhidden") shouldBe true
      document.getElementById("seventyPercentSpent-legend").text shouldBe Messages("page.companyDetails.seventyPercentSpent.legend")
    }


    "Verify that the page page contains the correct elements when an invalid SeventyPercentSpentModel is passed" in new SEISSetup {
      val document: Document = {
        setupMocks(None, Some(testUrl))
        val result = TestController.submit.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.companyDetails.seventyPercentSpent.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.seventyPercentSpent.heading")
      document.getElementById("main-heading").hasClass("h1-heading")
      document.getElementById("isSeventyPercentSpent-yesLabel").text() shouldBe Messages("common.radioYesLabel")
      document.getElementById("isSeventyPercentSpent-noLabel").text() shouldBe Messages("common.radioNoLabel")
      document.body.getElementById("back-link").attr("href") shouldEqual testUrl
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.details.one")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.getElementById("seventyPercentSpent-legend").hasClass("visuallyhidden") shouldBe true
      document.getElementById("seventyPercentSpent-legend").text shouldBe Messages("page.companyDetails.seventyPercentSpent.legend")
      document.getElementById("error-summary-display").hasClass("error-summary--show")
      document.getElementById("seventyPercentSpent-error-summary")

    }
  }
}
