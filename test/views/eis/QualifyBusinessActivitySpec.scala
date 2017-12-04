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
import controllers.eis.QualifyBusinessActivityController
import models.QualifyBusinessActivityModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._
import views.helpers.ViewSpec

import scala.concurrent.Future

class QualifyBusinessActivitySpec extends ViewSpec {

  object TestController extends QualifyBusinessActivityController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  def setupMocks(isQualifyBusinessActivity: Option[QualifyBusinessActivityModel] = None): Unit =
    when(mockS4lConnector.fetchAndGetFormData[QualifyBusinessActivityModel](Matchers.eq(KeystoreKeys.isQualifyBusinessActivity))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(isQualifyBusinessActivity))

  "The Qualify Business Activity page" should {

    "Verify whether the company is qualified for the trading process" in new Setup {
      val document: Document = {
        setupMocks(Some(qualifyTrade))
        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.companyDetails.qualifyBusinessActivity.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.qualifyBusinessActivity.heading")
      document.getElementById("main-heading").hasClass("heading-xlarge") shouldBe true
      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.details.one")
      document.select("label[for=isQualifyBusinessActivity-trade]").text() shouldBe Messages("page.companyDetails.qualifyBusinessActivity.bullet.one")
      document.select("label[for=isQualifyBusinessActivity-research_and_development]").text() shouldBe Messages("page.companyDetails.qualifyBusinessActivity.bullet.two")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.DateOfIncorporationController.show().url
      document.body.getElementById("help").text shouldBe Messages("page.companyDetails.qualifyBusinessActivity.help.heading")
      document.getElementById("help-text-one").text() shouldBe Messages("page.companyDetails.qualifyBusinessActivity.help.text.one")
      document.getElementById("help-text-two").text() shouldBe Messages("page.companyDetails.qualifyBusinessActivity.help.text.two")
      document.getElementById("help-bullet-one").text() shouldBe Messages("page.companyDetails.qualifyBusinessActivity.help.text.three")
      document.getElementById("help-bullet-two").text() shouldBe Messages("page.companyDetails.qualifyBusinessActivity.help.text.four")
      document.getElementById("help-bullet-three").text() shouldBe Messages("page.companyDetails.qualifyBusinessActivity.help.text.five")
      document.getElementById("help-text-three").text() shouldBe Messages("page.companyDetails.qualifyBusinessActivity.help.text.six")
      document.select("legend").text() shouldBe Messages("page.companyDetails.qualifyBusinessActivity.legend")
      document.select("legend").hasClass("visuallyhidden") shouldBe true
    }

    "Verify that the Qualifying Business Activity page contains the correct elements when an invalid QualifyingBusinessActivity is passed" in new Setup {
      val document: Document = {
        setupMocks()
        val result = TestController.submit.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.companyDetails.qualifyBusinessActivity.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.qualifyBusinessActivity.heading")
      document.getElementById("main-heading").hasClass("heading-xlarge") shouldBe true
      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.details.one")
      document.select("label[for=isQualifyBusinessActivity-trade]").text() shouldBe Messages("page.companyDetails.qualifyBusinessActivity.bullet.one")
      document.select("label[for=isQualifyBusinessActivity-research_and_development]").text() shouldBe Messages("page.companyDetails.qualifyBusinessActivity.bullet.two")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.DateOfIncorporationController.show().url
      document.body.getElementById("help").text shouldBe Messages("page.companyDetails.qualifyBusinessActivity.help.heading")
      document.getElementById("help-text-one").text() shouldBe Messages("page.companyDetails.qualifyBusinessActivity.help.text.one")
      document.getElementById("help-text-two").text() shouldBe Messages("page.companyDetails.qualifyBusinessActivity.help.text.two")
      document.getElementById("help-bullet-one").text() shouldBe Messages("page.companyDetails.qualifyBusinessActivity.help.text.three")
      document.getElementById("help-bullet-two").text() shouldBe Messages("page.companyDetails.qualifyBusinessActivity.help.text.four")
      document.getElementById("help-bullet-three").text() shouldBe Messages("page.companyDetails.qualifyBusinessActivity.help.text.five")
      document.getElementById("help-text-three").text() shouldBe Messages("page.companyDetails.qualifyBusinessActivity.help.text.six")
      document.select("legend").text() shouldBe Messages("page.companyDetails.qualifyBusinessActivity.legend")
      document.select("legend").hasClass("visuallyhidden") shouldBe true
      document.getElementById("error-summary-display").hasClass("error-summary--show") shouldBe true
      document.getElementById("error-summary-heading").text shouldBe Messages("common.error.summary.heading")
      document.getElementById("isQualifyBusinessActivity-error-summary").text shouldBe Messages("validation.common.error.fieldRequired")
      document.getElementsByClass("error-notification").text shouldBe Messages("validation.common.error.fieldRequired")

    }

  }

}
