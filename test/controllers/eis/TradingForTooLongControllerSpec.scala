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
import config.FrontendAuthConnector
import connectors.EnrolmentConnector
import controllers.helpers.BaseSpec
import play.api.test.Helpers._


class TradingForTooLongControllerSpec extends BaseSpec {

  object TestController extends TradingForTooLongController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
    override lazy val s4lConnector = mockS4lConnector
  }

  "TradingForTooLongController" should {
    "use the correct auth connector" in {
      TradingForTooLongController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      TradingForTooLongController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  "Sending a GET request to TradingForTooLongController when authenticated and enrolled" should {
    "retun an OK and load the page" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a POST request to TradingForTooLongController when authenticated and enrolled" should {
    "REDIRECT to the TurnoverCostsController" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TurnoverCostsController.show().url)
        }
      )
    }
  }
}
