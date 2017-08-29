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
import common.{Constants, KeystoreKeys}
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.BaseSpec
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class SeventyPercentSpentControllerSpec extends BaseSpec {

  object TestController extends SeventyPercentSpentController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  "SeventyPercentSpentController" should {
    "use the correct keystore connector" in {
      SeventyPercentSpentController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      SeventyPercentSpentController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct config" in {
      SeventyPercentSpentController.applicationConfig shouldBe FrontendAppConfig
    }
    "use the correct enrolment connector" in {
      SeventyPercentSpentController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  def setupMocks(seventyPercentSpentModel: Option[SeventyPercentSpentModel] = None, backLink: Option[String] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[SeventyPercentSpentModel](Matchers.eq(KeystoreKeys.seventyPercentSpent))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(seventyPercentSpentModel))

    when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(CacheMap("", Map())))

    when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkSeventyPercentSpent))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(backLink))

    when(mockS4lConnector.saveFormData(Matchers.eq(KeystoreKeys.backLinkShareIssueDate),
      Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(CacheMap("", Map())))

  }

  "Sending a GET request to SeventyPercentSpentController when authenticated and enrolled" should {
    "return a 200 when something is fetched from keystore and back link returned" in {
      setupMocks(Some(isSeventyPercentSpentModelYes), Some("/test/test"))
      mockEnrolledRequest(seisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show())(
        result => status(result) shouldBe OK
      )
    }

    "return a 200 when something is fetched from keystore and back link is None" in {
      setupMocks(Some(isSeventyPercentSpentModelYes), None)
      mockEnrolledRequest(seisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.seis.routes.QualifyBusinessActivityController.show().url)
        }
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore" in {
      setupMocks(None, Some("/test/test/"))
      mockEnrolledRequest(seisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show())(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a valid Yes form submission to the SeventyPercentSpentController when authenticated and enrolled" should {
    "redirect to the Share IssueDate page" in {
      val formInput = "isSeventyPercentSpent" -> Constants.StandardRadioButtonYesValue
      setupMocks(None, Some("/test/test"))
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit, formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.seis.routes.ShareIssueDateController.show().url)
        }
      )
    }
  }

  "Sending a valid No form submission to the SeventyPercentSpentController when authenticated and enrolled" should {
    "redirect to the Is This First Trade Error page" in {
      val formInput = "isSeventyPercentSpent" -> Constants.StandardRadioButtonNoValue
      setupMocks(None, Some("/test/test"))
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit, formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.seis.routes.SeventyPercentSpentErrorController.show().url)
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the SeventyPercentSpentController when authenticated and enrolled" should {
    "redirect to itself" in {
      setupMocks(None, Some("/test/test"))
      mockEnrolledRequest(seisSchemeTypesModel)
      val formInput = "isSeventyPercentSpent" -> ""
      submitWithSessionAndAuth(TestController.submit, formInput)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }

}

