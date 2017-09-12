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
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector, SubmissionConnector}
import controllers.helpers.BaseSpec
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.{Upstream5xxResponse, Upstream4xxResponse, BadRequestException}

import scala.concurrent.Future

class GrossAssetsControllerSpec extends BaseSpec {

  object TestController extends GrossAssetsController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val submissionConnector = mockSubmissionConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  lazy val grossAssetsAmount = 15000000
  val failedResponse = Upstream5xxResponse("Error",INTERNAL_SERVER_ERROR,INTERNAL_SERVER_ERROR)

  "GrossAssetsController" should {
    "use the correct keystore connector" in {
      GrossAssetsController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      GrossAssetsController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      GrossAssetsController.enrolmentConnector shouldBe EnrolmentConnector
    }
    "use the correct submission connector" in {
      GrossAssetsController.submissionConnector shouldBe SubmissionConnector
    }
    "use the correct application config" in {
      GrossAssetsController.applicationConfig shouldBe FrontendAppConfig
    }
  }

  "Sending a GET request to GrossAssetsController when authenticated and enrolled" should {

    "return an OK when something is fetched from storage" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      when(mockS4lConnector.fetchAndGetFormData[GrossAssetsModel](Matchers.eq(KeystoreKeys.grossAssets))
        (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(GrossAssetsModel(grossAssetsAmount))))
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }

    "return an OK when nothing is fetched from storage" in {
      when(mockS4lConnector.fetchAndGetFormData[GrossAssetsModel](Matchers.eq(KeystoreKeys.grossAssets))
        (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
      mockEnrolledRequest(seisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }

  }

  "Sending a valid form submit to the GrossAssetsController" should {
    "redirect to the correct next page if the allowed amount is not exceeded from API" in {
      when(mockSubmissionConnector.checkGrossAssetsAmountExceeded(Matchers.any(), Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "grossAmount" -> "15000000")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.FullTimeEmployeeCountController.show().url)
        }
      )
    }
  }


  "Sending a valid form submit to the GrossAssetsController" should {
    "redirect to the correct next page if the allowed amount is exceeded from API" in {
      when(mockSubmissionConnector.checkGrossAssetsAmountExceeded(Matchers.any(), Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(true)))
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "grossAmount" -> "15000001")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.GrossAssetsErrorController.show().url)
        }
      )
    }
  }

  "Sending a valid form submit to the GrossAssetsController" should {
    "show the INTERNAL_SERVER_ERROR page if an exception occurs during the API call" in {
      when(mockSubmissionConnector.checkGrossAssetsAmountExceeded(Matchers.any(), Matchers.any())
      (Matchers.any())).thenReturn(Future.failed(failedResponse))
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "grossAmount" -> "150000001")(
        result => {
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
      )
    }
  }

  "Sending a valid form submit to the GrossAssetsController" should {
    "show the INTERNAL_SERVER_ERROR page if the API call returns an empty Option" in {
      when(mockSubmissionConnector.checkGrossAssetsAmountExceeded(Matchers.any(), Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(None))
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "grossAmount" -> "150000001")(
        result => {
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the GrossAssetsController" should {
    "respond wih a bad request" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "grossAmount" -> "")(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }

}

