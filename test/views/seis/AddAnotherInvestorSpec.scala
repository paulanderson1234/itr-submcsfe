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
import connectors.SubmissionConnector
import controllers.seis.{AddAnotherInvestorController, routes}
import models.AddAnotherInvestorModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._
import views.helpers.ViewSpec

import scala.concurrent.Future

class AddAnotherInvestorSpec extends ViewSpec {

  object TestController extends AddAnotherInvestorController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
    override lazy val submissionConnector: SubmissionConnector = mockSubmissionConnector
  }

  def setupMocks(addAnotherInvestorModel: Option[AddAnotherInvestorModel] = None): Unit =
    when(mockS4lConnector.fetchAndGetFormData[AddAnotherInvestorModel](Matchers.eq(KeystoreKeys.addAnotherInvestor))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(addAnotherInvestorModel))

  "The AddAnotherInvestor page" should {

    "Verify that the AddAnotherInvestor page contains the correct elements when a valid AddAnotherInvestorModel is passed from keystore" in new SEISSetup {
      val document: Document = {
        setupMocks(Some(addAnotherInvestorModelYes))
        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.investors.addAnotherInvestor.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.investors.addAnotherInvestor.heading")
      document.getElementById("main-heading").hasClass("h1-heading")
      document.select("label[for=addAnotherInvestor-yes]").text() shouldBe Messages("common.radioYesLabel")
      document.select("label[for=addAnotherInvestor-no]").text() shouldBe Messages("common.radioNoLabel")
      document.select("legend").text() shouldBe Messages("page.investors.addAnotherInvestor.legend")
      document.select("legend").hasClass("visuallyhidden") shouldBe true
      document.getElementById("error-summary-display").hasClass("error-summary--show") shouldBe false
      document.body.getElementById("back-link").attr("href") shouldEqual routes.ReviewAllInvestorsController.show().url
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.details.four")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
    }


    "Verify that the Is First Trade page contains the correct elements when an invalid AddAnotherInvestorModel is passed" in new SEISSetup {
      val document: Document = {
        setupMocks()
        val result = TestController.submit.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.investors.addAnotherInvestor.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.investors.addAnotherInvestor.heading")
      document.select("label[for=addAnotherInvestor-yes]").text() shouldBe Messages("common.radioYesLabel")
      document.select("label[for=addAnotherInvestor-no]").text() shouldBe Messages("common.radioNoLabel")
      document.select("legend").text() shouldBe Messages("page.investors.addAnotherInvestor.legend")
      document.select("legend").hasClass("visuallyhidden") shouldBe true
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.seis.routes.ReviewAllInvestorsController.show().url
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.details.four")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.getElementById("error-summary-display").hasClass("error-summary--show") shouldBe true

    }
  }
}
