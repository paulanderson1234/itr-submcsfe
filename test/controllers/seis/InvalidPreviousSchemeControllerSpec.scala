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

import scala.concurrent.Future

class InvalidPreviousSchemeControllerSpec extends BaseSpec {

  val schemeId = 1
  object TestController extends InvalidPreviousSchemeController{
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
    override lazy val s4lConnector = mockS4lConnector
  }

  "InvalidPreviousSchemeController" should {
    "use the correct auth connector" in {
      InvalidPreviousSchemeController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      InvalidPreviousSchemeController.enrolmentConnector shouldBe EnrolmentConnector
    }
    "use the correct keystore connector" in {
      IndividualDetailsController.s4lConnector shouldBe S4LConnector
    }
    "use the correct config" in {
      InvalidPreviousSchemeController.applicationConfig shouldBe FrontendAppConfig
    }

  }

  "Sending a GET request to InvalidPreviousSchemeController when authenticated and enrolled" should {
    "return a 200" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show(schemeId))(
        result => status(result) shouldBe OK
      )
    }
  }

  "Continue to the ReviewPreviousSchemeController when authenticated and enrolled" should {
    "redirect Review previous screen page " in {
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.ReviewPreviousSchemesController.show().url)
        }
      )
    }
  }
}
