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
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.BaseSpec
import play.api.test.Helpers._

class IneligibleForKISecondaryConditionControllerSpec extends BaseSpec {

  object TestController extends IneligibleForKIController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  "IneligibleForKISecondaryConditionController" should {
    "use the correct keystore connector" in {
      IneligibleForKIController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      IneligibleForKIController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      IneligibleForKIController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }


  "Sending a GET request to IneligibleForKISecondaryConditionController when authenticated and enrolled" should {
    "return a 200" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show())(
        result => status(result) shouldBe OK
      )
    }
  }

  "Posting to the IneligibleForKISecondaryConditionController when authenticated and enrolled" should {
    "redirect to expected page" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.eis.routes.FullTimeEmployeeCountController.show().url)
        }
      )
    }
  }
}
