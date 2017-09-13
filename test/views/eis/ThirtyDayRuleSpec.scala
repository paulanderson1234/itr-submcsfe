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
import controllers.eis.ThirtyDayRuleController
import models.ThirtyDayRuleModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._
import views.helpers.ViewSpec

import scala.concurrent.Future

class ThirtyDayRuleSpec extends ViewSpec {

  object TestController extends ThirtyDayRuleController {
    override lazy val applicationConfig = MockConfigEISFlow
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }
  
  def setupMocks(thirtyDayRuleModel: Option[ThirtyDayRuleModel] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[ThirtyDayRuleModel](Matchers.eq(KeystoreKeys.thirtyDayRule))
      (Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(thirtyDayRuleModel))

  }

  "The ThirtyDayRule page" should {
    "contain the correct elements for a GET when a valid ThirtyDayRuleModel is returned from keystore" in new Setup {
      val document: Document = {
        setupMocks(Some(thirtyDayRuleModelYes))
        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.ThirtyDayRule.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.ThirtyDayRule.heading")
      document.getElementById("main-heading").hasClass("heading-xlarge") shouldBe true
      document.getElementById("thirtyDayRule-yesLabel").text() shouldBe Messages("common.radioYesLabel")
      document.getElementById("thirtyDayRule-noLabel").text() shouldBe Messages("common.radioNoLabel")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.ThirtyDayRuleController.show().url
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.PLEASE.UPDATE")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.getElementById("thirtyDayRule-legend").hasClass("visuallyhidden") shouldBe true
      document.getElementById("thirtyDayRule-legend").text shouldBe Messages("page.ThirtyDayRule.legend")
      document.body.getElementById("description-one").text shouldBe Messages("page.ThirtyDayRule.descriptionOne.text")
      document.getElementById("help-bullet-one").text() shouldBe Messages("page.ThirtyDayRule.help.bullet.one")
      document.getElementById("help-bullet-two").text() shouldBe Messages("page.ThirtyDayRule.help.bullet.two")
      document.select(".error-summary").isEmpty shouldBe true
    }
	
    "contain the correct elements for a GET when there is no ThirtyDayRuleModel returned from keystore" in new Setup {
      val document: Document = {
        setupMocks()
        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.ThirtyDayRule.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.ThirtyDayRule.heading")
      document.getElementById("main-heading").hasClass("heading-xlarge") shouldBe true
      document.getElementById("thirtyDayRule-yesLabel").text() shouldBe Messages("common.radioYesLabel")
      document.getElementById("thirtyDayRule-noLabel").text() shouldBe Messages("common.radioNoLabel")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.ThirtyDayRuleController.show().url
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.PLEASE.UPDATE")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.getElementById("thirtyDayRule-legend").hasClass("visuallyhidden") shouldBe true
      document.getElementById("thirtyDayRule-legend").text shouldBe Messages("page.ThirtyDayRule.legend")
      document.body.getElementById("description-one").text shouldBe Messages("page.ThirtyDayRule.descriptionOne.text")
      document.getElementById("help-bullet-one").text() shouldBe Messages("page.ThirtyDayRule.help.bullet.one")
      document.getElementById("help-bullet-two").text() shouldBe Messages("page.ThirtyDayRule.help.bullet.two")
      document.select(".error-summary").isEmpty shouldBe true
    }


    "contain the correct elements including an error summary for an POST that fails form validation" in new Setup {
      val document: Document = {
        setupMocks()
        val result = TestController.submit.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.ThirtyDayRule.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.ThirtyDayRule.heading")
      document.getElementById("main-heading").hasClass("heading-xlarge") shouldBe true
      document.getElementById("thirtyDayRule-yesLabel").text() shouldBe Messages("common.radioYesLabel")
      document.getElementById("thirtyDayRule-noLabel").text() shouldBe Messages("common.radioNoLabel")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.ThirtyDayRuleController.show().url
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.PLEASE.UPDATE")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.getElementById("thirtyDayRule-legend").hasClass("visuallyhidden") shouldBe true
      document.getElementById("thirtyDayRule-legend").text shouldBe Messages("page.ThirtyDayRule.legend")
      document.getElementById("error-summary-display").hasClass("error-summary--show")
      document.body.getElementById("description-one").text shouldBe Messages("page.ThirtyDayRule.descriptionOne.text")
      document.getElementById("help-bullet-one").text() shouldBe Messages("page.ThirtyDayRule.help.bullet.one")
      document.getElementById("help-bullet-two").text() shouldBe Messages("page.ThirtyDayRule.help.bullet.two")

      // Ensure Error section present
      document.getElementById("error-summary-display").hasClass("error-summary--show") shouldBe true

    }
  }
}
