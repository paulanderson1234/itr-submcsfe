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
import common.KeystoreKeys
import config.FrontendAuthConnector
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.BaseSpec
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._

import scala.concurrent.Future

class ShareIssueDateControllerSpec extends BaseSpec {

  object ShareIssueDateControllerTest extends ShareIssueDateController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  def setupMocks(shareIssueDateModel: Option[ShareIssueDateModel] = None): Unit =
    when(mockS4lConnector.fetchAndGetFormData[ShareIssueDateModel](Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(shareIssueDateModel))

  "ShareIssueDateController" should {
    "use the correct keystore connector" in {
      ShareIssueDateController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      ShareIssueDateController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      ShareIssueDateController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  "Sending a GET request to ShareIssueDateController when authenticated and enrolled" should {
    "return a 200 when something is fetched from keystore" in {
      setupMocks(Some(shareIssueDateModel))
      mockEnrolledRequest(seisSchemeTypesModel)
      showWithSessionAndAuth(ShareIssueDateControllerTest.show())(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore" in {
      setupMocks()
      mockEnrolledRequest(seisSchemeTypesModel)
      showWithSessionAndAuth(ShareIssueDateControllerTest.show())(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a valid form submit to the ShareIssueDateController when authenticated and enrolled" should {
    "redirect to first trade start date page" in {
      setupMocks(Some(shareIssueDateModel))
      mockEnrolledRequest(seisSchemeTypesModel)

      val formInput = Seq(
        "shareIssueDay" -> "23",
        "shareIssueMonth" -> "11",
        "shareIssueYear" -> "1993")

      submitWithSessionAndAuth(ShareIssueDateControllerTest.submit,formInput:_*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.ShareIssueDateController.show().url)
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the ShareIssueDateController when authenticated and enrolled" should {
    "return a bad request" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      val formInput = Seq(
        "shareIssueDay" -> "",
        "shareIssueMonth" -> "",
        "shareIssueYear" -> "")

      submitWithSessionAndAuth(ShareIssueDateControllerTest.submit,formInput:_*)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }

}
