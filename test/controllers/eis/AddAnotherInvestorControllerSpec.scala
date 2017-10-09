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
import common.{Constants, KeystoreKeys}
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.BaseSpec
import models._
import models.investorDetails.InvestorDetailsModel
import models.repayments.SharesRepaymentDetailsModel
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
    "use the correct config" in {
      AddAnotherInvestorController.applicationConfig shouldBe FrontendAppConfig
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
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show())(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore" in {
      setupMocks(None)
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show())(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a valid Yes form submission to the AddAnotherInvestorController when authenticated and enrolled" should {
    "redirect to the Add investor or nominee page" in {
      val formInput = "addAnotherInvestor" -> Constants.StandardRadioButtonYesValue
      setupMocks()
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.eis.routes.AddInvestorOrNomineeController.show().url)
        }
      )
    }
  }

  "Sending a valid No form submission to the AddAnotherInvestorController with SharesRepaymentDetailsModel " +
    "when authenticated and enrolled" should {
    "redirect to the Value Receieved page" in {
      val formInput = "addAnotherInvestor" -> Constants.StandardRadioButtonNoValue
      setupMocks()
      when(mockS4lConnector.fetchAndGetFormData[Vector[SharesRepaymentDetailsModel]](Matchers.eq(KeystoreKeys.sharesRepaymentDetails))
        (Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Some(incompleteSharesRepaymentDetailsVector))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.eis.routes.ReviewPreviousRepaymentsController.show().url)
        }
      )
    }
  }

  "Sending a valid No form submission to the AddAnotherInvestorController with no or empty SharesRepaymentDetailsModel " +
    "when authenticated and enrolled" should {
    "redirect to the Value Receieved page" in {
      val formInput = "addAnotherInvestor" -> Constants.StandardRadioButtonNoValue
      setupMocks()
      when(mockS4lConnector.fetchAndGetFormData[Vector[SharesRepaymentDetailsModel]](Matchers.eq(KeystoreKeys.sharesRepaymentDetails))
        (Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(None)
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.eis.routes.AnySharesRepaymentController.show().url)
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the AddAnotherInvestorController when authenticated and enrolled" should {
    "respond wih a bad request" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = "addAnotherInvestor" -> ""
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }

}
