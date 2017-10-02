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
import common.KeystoreKeys
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.BaseSpec
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import common.Constants
import scala.concurrent.Future

class MarketDescriptionControllerSpec extends BaseSpec {

  val validBackLink = "/test/testing"
  val marketDescriptionModel = MarketDescriptionModel("test")
  val validData:String = "testOk"
  val overMaxLength = "X" * Constants.SuggestedTextMaxLength + 1

  object TestController extends MarketDescriptionController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  def setupMocks(marketDescriptionModel: Option[MarketDescriptionModel] = None, backUrl: Option[String] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[MarketDescriptionModel](Matchers.eq(KeystoreKeys.marketDescription))
      (Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(marketDescriptionModel))
    when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkMarketDescription))
      (Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(backUrl))
    when(mockS4lConnector.saveFormData(Matchers.eq(KeystoreKeys.marketDescription),
      Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
    .thenReturn(Future.successful(CacheMap("", Map())))
  }

  "MarketDescriptionController" should {
    "use the correct storage connector" in {
      MarketDescriptionController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      MarketDescriptionController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      MarketDescriptionController.enrolmentConnector shouldBe EnrolmentConnector
    }
    "use the correct application config" in {
      MarketDescriptionController.applicationConfig shouldBe FrontendAppConfig
    }
  }

  "Sending a GET request to MarketDescriptionController when authenticated and enrolled" should {
    "return a 200 when a saved model and valid backlink are fetched from storage" in {
     setupMocks(Some(marketDescriptionModel), Some(validBackLink))
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }

    "return a 200 when no model exists in storage but valid backlink is present in storage" in {
     setupMocks(None, Some(validBackLink))
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }

	"redirect to the expected start of flow page when no valid back link is present in storage" in {
      setupMocks(Some(marketDescriptionModel), None)
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.routes.ApplicationHubController.show().url)
        }
      )
    }
  }

  "Sending a valid form submit to the MarketDescriptionController" should {
    "redirect to the expected page" in {
      setupMocks(None, Some(validBackLink))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "descriptionTextArea" -> s"$validData")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.InvestmentGrowController.show().url)
        }
      )
    }
  }

  "Sending an invalid text exceeding the maximum to the MarketDescriptionController when authenticated and enrolled" should {
    "return a BadRequest" in {
      setupMocks(None, Some(validBackLink))
      mockEnrolledRequest(eisSchemeTypesModel)
       val formInput = "descriptionTextArea" -> s"$overMaxLength"
       submitWithSessionAndAuth(TestController.submit,formInput)(
       result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }

   "Sending an invalid form submission with validation errors to the MarketDescriptionController when authenticated and enrolled" should {
    "return a bad request status" in {
      setupMocks(None, Some(validBackLink))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "descriptionTextArea" -> "")(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }

}

