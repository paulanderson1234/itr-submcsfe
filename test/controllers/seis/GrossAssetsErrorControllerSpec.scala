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

package controllers.seis

import auth.{MockAuthConnector, MockConfig}
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.BaseSpec
import play.api.test.Helpers._


class GrossAssetsErrorControllerSpec extends BaseSpec {

  object TestController extends GrossAssetsErrorController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
    override lazy val s4lConnector = mockS4lConnector
  }

  "GrossAssetsController" should {
    "use the correct keystore connector" in {
      GrossAssetsErrorController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      GrossAssetsErrorController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      GrossAssetsErrorController.enrolmentConnector shouldBe EnrolmentConnector
    }
    "use the correct application config" in {
      GrossAssetsErrorController.applicationConfig shouldBe FrontendAppConfig
    }
  }

  "Sending a GET request to GrossAssetsErrorController when authenticated and enrolled" should {
    "return a 200 OK" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending submission to GrossAssetsErrorController when authenticated and enrolled" should {
    "redirect to correct page" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.FullTimeEmployeeCountController.show().url)
        }
      )
    }
  }

}
