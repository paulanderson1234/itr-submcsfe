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
import config.FrontendAuthConnector
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.BaseSpec
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class ShareIssueDateControllerSpec extends BaseSpec {

  val testBackLink = routes.SeventyPercentSpentController.show().url

  object TestController extends ShareIssueDateController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

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

  def setupMocks(shareIssueDateModel: Option[ShareIssueDateModel] = None, backLink: Option[String] = None): Unit = {

    when(mockS4lConnector.fetchAndGetFormData[ShareIssueDateModel](Matchers.eq(KeystoreKeys.shareIssueDate))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(shareIssueDateModel))

    when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkShareIssueDate))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(backLink))

  }

  "Sending a GET request to ShareIssueDateController when authenticated and enrolled" should {
    "return an OK when something is fetched from storage" in {
      setupMocks(Some(shareIssuetDateModel), Some(testBackLink))
      mockEnrolledRequest(seisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show())(
        result => status(result) shouldBe OK
      )
    }

    "return an OK when nothing is fetched from storage" in {
      setupMocks(None, Some(testBackLink))
      mockEnrolledRequest(seisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show())(
        result => status(result) shouldBe OK
      )
    }

    "redirect to the correct page when no back link is provided" in {
      setupMocks(Some(shareIssuetDateModel), None)
      mockEnrolledRequest(seisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.seis.routes.QualifyBusinessActivityController.show().url)
        }
      )
    }
  }

  "Sending a valid form submit to the ShareIssueDateController when authenticated and enrolled" should {
    "redirect to the correct page" in {
      setupMocks(Some(shareIssueDateModel))
      mockEnrolledRequest(seisSchemeTypesModel)

      val formInput = Seq(
        "shareIssueDay" -> "23",
        "shareIssueMonth" -> "11",
        "shareIssueYear" -> "1993")

      submitWithSessionAndAuth(TestController.submit,formInput:_*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.GrossAssetsController.show().url)
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the ShareIssueDateController when authenticated and enrolled" should {
    "return a BadRequest when a backlink is provided" in {
      setupMocks(None, Some(testBackLink))
      mockEnrolledRequest(seisSchemeTypesModel)
      val formInput = Seq(
        "shareIssueDay" -> "",
        "shareIssueMonth" -> "",
        "shareIssueYear" -> "")

      submitWithSessionAndAuth(TestController.submit,formInput:_*)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }


  "Sending an invalid form submission with validation errors to the ShareIssueDateController when authenticated and enrolled" should {
    "redirect to the correct page when no back link is provided" in {
      setupMocks(None, None)
      mockEnrolledRequest(seisSchemeTypesModel)
      val formInput = Seq(
        "shareIssueDay" -> "",
        "shareIssueMonth" -> "",
        "shareIssueYear" -> "")

      submitWithSessionAndAuth(TestController.submit,formInput:_*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.QualifyBusinessActivityController.show().url)
        }
      )
    }
  }

}

