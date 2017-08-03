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

import scala.concurrent.Future

class AddAnotherInvestorControllerSpec extends BaseSpec {

  object TestController extends AddAnotherInvestorController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
    override lazy val submissionConnector = mockSubmissionConnector
  }

  "AddAnotherInvestorController" should {
    "use the correct keystore connector" in {
      AddAnotherInvestorController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      AddAnotherInvestorController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      AddAnotherInvestorController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  def setupMocks(addAnotherInvestorModel: Option[AddAnotherInvestorModel] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[AddAnotherInvestorModel](Matchers.eq(KeystoreKeys.addAnotherInvestor))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(addAnotherInvestorModel))

  }

  "Sending a GET request to AddAnotherInvestorController when authenticated and enrolled" should {
    "return a 200 when something is fetched from keystore" in {
      setupMocks(Some(addAnotherInvestorModelYes))
      mockEnrolledRequest(seisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show())(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore" in {
      setupMocks(None)
      mockEnrolledRequest(seisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show())(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a valid Yes form submission to the AddAnotherInvestorController when authenticated and enrolled" should {
    "redirect to the Used Investment Scheme Before page if the Trade start date condition is met" in {
      val formInput = "addAnotherInvestor" -> Constants.StandardRadioButtonYesValue
      setupMocks()
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.seis.routes.HadPreviousRFIController.show().url)
        }
      )
    }
  }

  "Sending a valid No form submission to the AddAnotherInvestorController when authenticated and enrolled" should {
    "redirect to the Is This First Trade Error page" in {
      val formInput = "addAnotherInvestor" -> Constants.StandardRadioButtonNoValue
      setupMocks()
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          // To navigate to NotFirstTradeError page
          redirectLocation(result) shouldBe Some(controllers.seis.routes.NotFirstTradeController.show().url)
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the AddAnotherInvestorController when authenticated and enrolled" should {
    "redirect to itself" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      val formInput = "addAnotherInvestor" -> ""
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }

}
