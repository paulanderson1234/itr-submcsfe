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

import auth.{MockAuthConnector, MockConfigEISFlow}
import common.KeystoreKeys
import config.FrontendAppConfig
import controllers.seis.PreviousShareHoldingNominalValueController
import models.investorDetails.PreviousShareHoldingNominalValueModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._
import views.helpers.ViewSpec

import scala.concurrent.Future

class PreviousShareHoldingNominalViewSpec extends ViewSpec {

  object TestController extends PreviousShareHoldingNominalValueController {
    override lazy val applicationConfig = MockConfigEISFlow
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  def setupMocks(previousShareHoldingNominalValueModel: Option[PreviousShareHoldingNominalValueModel] = None): Unit =
    when(mockS4lConnector.fetchAndGetFormData[PreviousShareHoldingNominalValueModel](Matchers.eq(KeystoreKeys.previousShareHoldingNominalValue))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(previousShareHoldingNominalValueModel))

  "The Previous Share Holding Nominal Value page" should {

    "Verify that the page contains the correct elements when a valid PreviousShareHoldingNominalValueModel is passed" in new Setup {
      val document: Document = {
        setupMocks(Some(previousShareHoldingNominalValueModel))
        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.seis.previousShareHoldingNominalValue.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.seis.previousShareHoldingNominalValue.heading")
      document.getElementById("nominalValueInfo").text() shouldBe Messages("page.seis.investors.previousShareHoldingNominalValue.info")
      document.getElementById("label-nominal-value-hint").text() shouldBe Messages("page.seis.previousShareHoldingNominalValue.whereToFind")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.seis.routes.PreviousShareHoldingNominalValueController.show().url
      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.four")
    }

    "Verify that the Previous Share Holding Nominal Value page contains the correct elements when an invalid Model is passed" in new Setup {
      val document: Document = {
        setupMocks()
        val result = TestController.submit.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.seis.previousShareHoldingNominalValue.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.seis.previousShareHoldingNominalValue.heading")
      document.getElementById("nominalValueInfo").text() shouldBe Messages("page.seis.investors.previousShareHoldingNominalValue.info")
      document.getElementById("label-nominal-value-hint").text() shouldBe Messages("page.seis.previousShareHoldingNominalValue.whereToFind")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.seis.routes.PreviousShareHoldingNominalValueController.show().url
      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.four")
      document.getElementById("error-summary-display").hasClass("error-summary--show")
    }

  }

}
