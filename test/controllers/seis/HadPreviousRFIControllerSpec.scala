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

import scala.concurrent.Future

class HadPreviousRFIControllerSpec extends BaseSpec {

  object TestController extends HadPreviousRFIController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  "HadPreviousRFIController" should {
    "use the correct keystore connector" in {
      HadPreviousRFIController.s4lConnector shouldBe S4LConnector
    }
    "use the correct config" in {
      HadPreviousRFIController.applicationConfig shouldBe FrontendAppConfig
    }
    "use the correct auth connector" in {
      HadPreviousRFIController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      HadPreviousRFIController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  def setupMocks(hadPreviousRFIModel: Option[HadPreviousRFIModel] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(hadPreviousRFIModel))
  }

  "Sending a GET request to HadPreviousRFIController when authenticated and enrolled for SEIS" should {
    "return a 200 when something is fetched from keystore" in {
      setupMocks(Some(hadPreviousRFIModelYes))
      mockEnrolledRequest(seisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show())(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore for SEIS" in {
      setupMocks(None)
      mockEnrolledRequest(seisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show())(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a valid 'Yes' form submit to the HadPreviousRFIController when authenticated and enrolled" +
    "and there are no previous enrolments for SEIS" should {
    "redirect to previous scheme page" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      val formInput = "hadPreviousRFI" -> Constants.StandardRadioButtonYesValue
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.HadOtherInvestmentsController.show().url)
        }
      )
    }
  }

  "Sending a valid 'No' form submit to the HadPreviousRFIController when authenticated and enrolled for SEIS" should {
    "redirect to the commercial sale page" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      val formInput = "hadPreviousRFI" -> Constants.StandardRadioButtonNoValue
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief-cs/seis/had-other-investments-before")
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the HadPreviousRFIController when authenticated " +
    "and enrolled for SEIS" should {
    "respond wih a bad request" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      val formInput = "hadPreviousRFI" -> ""
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }
}
