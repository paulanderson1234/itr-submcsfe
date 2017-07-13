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

import java.net.URLEncoder

import auth.{MockAuthConnector, MockConfig}
import common.KeystoreKeys
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.BaseSpec
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import services.SubscriptionService

import scala.concurrent.Future

class CompanyDetailsControllerSpec extends BaseSpec {

  object TestController extends CompanyDetailsController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
    val subscriptionService = SubscriptionService
  }

  "CompanyDetailsController" should {
    "use the correct auth connector" in {
      CompanyDetailsController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct keystore connector" in {
      CompanyDetailsController.s4lConnector shouldBe S4LConnector
    }
    "use the correct enrolment connector" in {
      CompanyDetailsController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  def setupMocks(companyDetailsModel : Option[CompanyDetailsModel] = None): Unit =
    when(mockS4lConnector.fetchAndGetFormData[CompanyDetailsModel](Matchers.eq(KeystoreKeys.companyDetails))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(companyDetailsModel))

  "Sending a GET request to CompanyDetailsController when authenticated and enrolled" should {
    "return a 200 OK when something is fetched from keystore" in {
      setupMocks(Option(companyDetailsModel))
      mockEnrolledRequest(seisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore" in {
      setupMocks()
      mockEnrolledRequest(seisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a valid form submit to the CompanyDetailsController when authenticated and enrolled" should {
    "redirect to the Company Details Controller page" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      val formInput =
        Seq("companyName" -> "Line 0",
          "companyAddressline1" -> "Line 1",
          "companyAddressline2" -> "Line 2",
          "companyAddressline3" -> "Line 3",
          "companyAddressline4" -> "line 4",
          "companyPostcode" -> "AA1 1AA",
          "countryCode" -> "GB")

      submitWithSessionAndAuth(TestController.submit, formInput: _*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief-cs/seis/company-details")
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the CompanyDetailsController when authenticated and enrolled" should {
    "redirect to itself" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      val formInput = Seq("companyName" -> "", "companyAddressLine1" -> "", "companyAddressline1" -> "", "companyAddressline3" -> "Line3",
        "companyAddressline4" -> "Line4", "companyPostCode" -> "AA1 1AA", "countryCode" -> "GB")
      submitWithSessionAndAuth(TestController.submit, formInput: _*)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }

}
