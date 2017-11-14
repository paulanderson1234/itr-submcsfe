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
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.BaseSpec
import play.api.test.Helpers._

class HasInvestmentTradeStartedErrorControllerSpec extends BaseSpec {

  object TestController extends HasInvestmentTradeStartedErrorController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
    override lazy val s4lConnector = mockS4lConnector
  }

  "HasInvestmentTradeStartedErrorController" should {
    "use the correct auth connector" in {
      HasInvestmentTradeStartedErrorController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      HasInvestmentTradeStartedErrorController.enrolmentConnector shouldBe EnrolmentConnector
    }
	"use the correct storage connector" in {
      HasInvestmentTradeStartedErrorController.s4lConnector shouldBe S4LConnector
    }
	"use the correct application config" in {
      HasInvestmentTradeStartedErrorController.applicationConfig shouldBe FrontendAppConfig
    }
  }

  "Sending a GET request to HasInvestmentTradeStartedErrorController when authenticated and enrolled" should {
    "return a 200 OK" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }
  }

}
