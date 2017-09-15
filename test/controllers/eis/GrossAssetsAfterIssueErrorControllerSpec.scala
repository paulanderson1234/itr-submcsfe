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
import models.DateOfIncorporationModel
import org.mockito.Matchers
import org.mockito.Mockito.when
import play.api.test.Helpers._

import scala.concurrent.Future


class GrossAssetsAfterIssueErrorControllerSpec extends BaseSpec {

  object TestController extends GrossAssetsAfterIssueErrorController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
    override lazy val s4lConnector = mockS4lConnector
  }

  "GrossAssetsController" should {
    "use the correct keystore connector" in {
      GrossAssetsAfterIssueErrorController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      GrossAssetsAfterIssueErrorController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      GrossAssetsAfterIssueErrorController.enrolmentConnector shouldBe EnrolmentConnector
    }
    "use the correct application config" in {
      GrossAssetsAfterIssueErrorController.applicationConfig shouldBe FrontendAppConfig
    }
  }

  "Sending a GET request to GrossAssetsAfterIssueErrorController when authenticated and enrolled" should {
    "return an OK" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending submission to GrossAssetsAfterIssueErrorController when authenticated and enrolled" should {
    "redirect to correct page when the date of incorporation is less than 3 years ago" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))
        (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(Some(dateOfIncorporationModelKI)))
      submitWithSessionAndAuth(TestController.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.eis.routes.FullTimeEmployeeCountController.show().url)
        }
      )
    }
  }

  "Sending submission to GrossAssetsAfterIssueErrorController when authenticated and enrolled" should {
    "redirect to correct page when the date of incorporation is more than 3 years ago" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))
        (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(Some(dateOfIncorporationModel)))
      submitWithSessionAndAuth(TestController.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.eis.routes.IsCompanyKnowledgeIntensiveController.show().url)
        }
      )
    }
  }

}
