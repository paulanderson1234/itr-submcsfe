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
import common.{Constants, KeystoreKeys}
import controllers.seis.{ShareCapitalChangesController, routes}
import models.{ShareCapitalChangesModel, ShareIssueDateModel}
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

class ShareCapitalChangesSpec extends ViewSpec {


  val shareCapitalChangesModelYes = ShareCapitalChangesModel(Constants.StandardRadioButtonYesValue, Some("test"))
  val shareCapitalChangesModelNo = ShareCapitalChangesModel(Constants.StandardRadioButtonNoValue, None)

  object TestController extends ShareCapitalChangesController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  def setupMocks(shareCapitalChangesModel: Option[ShareCapitalChangesModel] = None, shareIssueDate: Option[ShareIssueDateModel] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[ShareCapitalChangesModel](Matchers.eq(KeystoreKeys.shareCapitalChanges))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(shareCapitalChangesModel))
    when(mockS4lConnector.fetchAndGetFormData[ShareIssueDateModel](Matchers.eq(KeystoreKeys.shareIssueDate))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(shareIssueDate))
    when(mockS4lConnector.saveFormData(Matchers.eq(KeystoreKeys.shareCapitalChanges),
      Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(CacheMap("", Map())))
  }

  "The Is this the first trade your company has carried out page" should {

    "Verify that the page contains the correct elements when a valid model is passed from keystore with expected url" in new SEISSetup {
      val document: Document = {
        setupMocks(Some(shareCapitalChangesModelYes),Some(ShareIssueDateModel(Some(29),Some(2),Some(2004))))
        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe "Have there been any changes in your company's share capital since 29 February 2004?"
      document.getElementById("main-heading").text() shouldBe "Have there been any changes in your company's share capital since 29 February 2004?"
      document.getElementById("main-heading").hasClass("h1-heading")
      document.select("label[for=hasChanges-yes]").text() shouldBe Messages("common.radioYesLabel")
      document.select("label[for=hasChanges-no]").text() shouldBe Messages("common.radioNoLabel")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.WasAnyValueReceivedController.show().url
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.details.four")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.select("legend").text() shouldBe "Have there been any changes in your company's share capital since 29 February 2004?"
      document.select("legend").hasClass("visuallyhidden") shouldBe true
      //document.getElementById("error-summary-display").hasClass("error-summary--show") shouldBe false
      document.select(".error-summary").isEmpty shouldBe true
    }

    "Verify that the page contains the correct elements when a valid IsFirstTRadeModel is passed from keystore with alternate url" in new SEISSetup {
      val document: Document = {
        setupMocks(Some(shareCapitalChangesModelNo),Some(ShareIssueDateModel(Some(2),Some(3),Some(1970))))
        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe "Have there been any changes in your company's share capital since 2 March 1970?"
      document.getElementById("main-heading").text() shouldBe "Have there been any changes in your company's share capital since 2 March 1970?"
      document.getElementById("main-heading").hasClass("h1-heading")
      document.select("label[for=hasChanges-yes]").text() shouldBe Messages("common.radioYesLabel")
      document.select("label[for=hasChanges-no]").text() shouldBe Messages("common.radioNoLabel")

      document.body.getElementById("back-link").attr("href") shouldEqual routes.WasAnyValueReceivedController.show().url
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.details.four")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")

      document.select("legend").text() shouldBe "Have there been any changes in your company's share capital since 2 March 1970?"
      document.select("legend").hasClass("visuallyhidden") shouldBe true
      document.select(".error-summary").isEmpty shouldBe true
    }


    "Verify that the page page contains the correct elements when an invalid ShareCapitalChangesModel is passed" in new SEISSetup {
      val document: Document = {
        setupMocks(None, Some(ShareIssueDateModel(Some(2),Some(3),Some(1970))))
        val result = TestController.submit.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe "Have there been any changes in your company's share capital since 2 March 1970?"
      document.getElementById("main-heading").text() shouldBe "Have there been any changes in your company's share capital since 2 March 1970?"
      document.getElementById("main-heading").hasClass("h1-heading")

      document.select("label[for=hasChanges-yes]").text() shouldBe Messages("common.radioYesLabel")
      document.select("label[for=hasChanges-no]").text() shouldBe Messages("common.radioNoLabel")

      document.body.getElementById("back-link").attr("href") shouldEqual routes.WasAnyValueReceivedController.show().url
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.details.four")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.select("legend").text() shouldBe "Have there been any changes in your company's share capital since 2 March 1970?"
      document.select("legend").hasClass("visuallyhidden") shouldBe true
      document.getElementById("error-summary-display").hasClass("error-summary--show") shouldBe true




    }
  }
}
