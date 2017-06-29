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
import config.{AppConfig, FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.BaseSpec
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import play.api.test.Helpers._

class FullTimeEmployeeCountErrorControllerSpec extends BaseSpec {

  lazy val controller = new FullTimeEmployeeCountErrorController {
    override lazy val s4lConnector: S4LConnector = mockS4lConnector
    override lazy val enrolmentConnector: EnrolmentConnector = mockEnrolmentConnector
    override lazy val applicationConfig: AppConfig = MockConfig
    override lazy val authConnector: AuthConnector = MockAuthConnector

    mockEnrolledRequest(seisSchemeTypesModel)
  }

  "FullTimeEmployeeCountErrorController" should {

    "use the correct keystore connector" in {
      FullTimeEmployeeCountErrorController.s4lConnector shouldBe S4LConnector
    }

    "use the correct enrollment connector" in {
      FullTimeEmployeeCountErrorController.enrolmentConnector shouldBe EnrolmentConnector
    }

    "use the correct config" in {
      FullTimeEmployeeCountErrorController.applicationConfig shouldBe FrontendAppConfig
    }

    "use the correct auth connector" in {
      FullTimeEmployeeCountErrorController.authConnector shouldBe FrontendAuthConnector
    }

    "return a valid 200 response from a show GET request when authorised" in {
      showWithSessionAndAuth(controller.show)(
        result => {
          status(result) shouldBe OK
        }
      )
    }
  }
}
