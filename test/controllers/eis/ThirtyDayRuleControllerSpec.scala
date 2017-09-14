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

package controllers.eis

import auth.{MockAuthConnector, MockConfig}
import common.{Constants, KeystoreKeys}
import config.FrontendAuthConnector
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.BaseSpec
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import scala.concurrent.Future

class ThirtyDayRuleControllerSpec extends BaseSpec {

  object TestController extends ThirtyDayRuleController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  "ThirtyDayRuleController" should {
    "use the correct storage connector" in {
      ThirtyDayRuleController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      ThirtyDayRuleController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      ThirtyDayRuleController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  def setupMocks(thirtyDayRuleModel: Option[ThirtyDayRuleModel] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[ThirtyDayRuleModel](Matchers.eq(KeystoreKeys.thirtyDayRule))
      (Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(thirtyDayRuleModel))

    when(mockS4lConnector.saveFormData(Matchers.eq(KeystoreKeys.thirtyDayRule),
      Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
    .thenReturn(Future.successful(CacheMap("", Map())))

    when(mockS4lConnector.saveFormData(Matchers.eq(KeystoreKeys.backLinkMarketDescription),
      Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(CacheMap("", Map())))

  }

  "Sending a GET request to ThirtyDayRuleController when authenticated and enrolled" should {
    "return a 200 when a saved model is fetched from storage" in {
      setupMocks(Some(thirtyDayRuleModelYes))
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show())(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return an OK 200 when nothing is fetched from storage" in {
      setupMocks(None)
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show())(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a valid Yes form submission to the ThirtyDayRuleController when authenticated and enrolled" should {
    "redirect to the expected page condition is met" in {
      val formInput = "thirtyDayRule" -> Constants.StandardRadioButtonYesValue
      setupMocks()
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.MarketDescriptionController.show().url)
        }
      )
    }
  }

  "Sending a valid No form submission to the ThirtyDayRuleController when authenticated and enrolled" should {
    "redirect to the expected page" in {
      val formInput = "thirtyDayRule" -> Constants.StandardRadioButtonNoValue
      setupMocks()
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.ThirtyDayRuleErrorController.show().url)
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the ThirtyDayRuleController when authenticated and enrolled" should {
    "return a bad request status" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = "thirtyDayRule" -> ""
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }

}
