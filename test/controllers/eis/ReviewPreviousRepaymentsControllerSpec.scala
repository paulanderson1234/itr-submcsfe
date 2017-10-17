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
import config.{AppConfig, FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.BaseSpec
import models.repayments.SharesRepaymentDetailsModel
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector


class ReviewPreviousRepaymentsControllerSpec extends BaseSpec{

  lazy val testController = new ReviewPreviousRepaymentsController {
    override lazy val s4lConnector: S4LConnector = mockS4lConnector
    override lazy val enrolmentConnector: EnrolmentConnector = mockEnrolmentConnector
    override lazy val applicationConfig: AppConfig = MockConfig
    override lazy val authConnector: AuthConnector = MockAuthConnector
  }

  def setupMocks(sharesRepaymentDetails: Option[Vector[SharesRepaymentDetailsModel]] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[Vector[SharesRepaymentDetailsModel]](Matchers.eq(KeystoreKeys.sharesRepaymentDetails))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(sharesRepaymentDetails)
    mockEnrolledRequest(eisSchemeTypesModel)
  }

  "The Number Of Previously Issued Shares controller" should {

    "use the correct auth connector" in {
      ReviewPreviousRepaymentsController.authConnector shouldBe FrontendAuthConnector
    }

    "use the correct config" in {
      ReviewPreviousRepaymentsController.applicationConfig shouldBe FrontendAppConfig
    }

    "use the correct keystore connector" in {
      ReviewPreviousRepaymentsController.s4lConnector shouldBe S4LConnector
    }

    "use the correct enrolment connector" in {
      ReviewPreviousRepaymentsController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  "When a GET request is made to ReviewPreviousRepaymentsController show method" which {

    "does not find any repayments" should {
      lazy val result = {
        setupMocks(None)
        testController.show()(authorisedFakeRequest)
      }

      "return a status of 303" in {
        status(result) shouldBe SEE_OTHER
      }

      "redirect the user to the AnySharesRepayment page" in {
        redirectLocation(result) shouldBe Some(controllers.eis.routes.AnySharesRepaymentController.show().url)
      }
    }


    "finds previous repayments details" should {
      lazy val result = {
        setupMocks(Some(validSharesRepaymentDetailsVector))
        testController.show()(authorisedFakeRequest)
      }

      "return a status of 200" in {
        status(result) shouldBe OK
      }
    }
  }

  "When a POST request is made to ReviewPreviousRepaymentsController submit method" which {

    "finds previous repayments details which are all Valid" should {
      lazy val result = {
        setupMocks(Some(validSharesRepaymentDetailsVector))
        testController.submit()(authorisedFakeRequest)
      }

      "return a status of 303" in {
        status(result) shouldBe SEE_OTHER
      }

      "Redirect to the AddAnotherInvestorOrNominee page" in {
        redirectLocation(result) shouldBe Some(controllers.eis.routes.WasAnyValueReceivedController.show().url)
      }

    }
  }

  "When a GET request is made to ReviewPreviousRepaymentsController change method" which {

    "does not find any repayments" should {
      lazy val result = {
        setupMocks(Some(incompleteSharesRepaymentDetailsVector))
        testController.change(2)(authorisedFakeRequest)
      }

      "return a status of 303" in {
        status(result) shouldBe SEE_OTHER
      }

      "redirect the user to the WhoRepaidSharesController page" in {
        redirectLocation(result) shouldBe Some(controllers.eis.routes.WhoRepaidSharesController.show(Some(2)).url)
      }
    }


    "finds previous repayment details which are all valid" should {
      lazy val result = {
        setupMocks(Some(validSharesRepaymentDetailsVector))
        testController.change(2)(authorisedFakeRequest)
      }

      "return a status of 303" in {
        status(result) shouldBe SEE_OTHER
      }

      "Redirect to the WhoRepaidSharesController page" in {
        redirectLocation(result) shouldBe Some(controllers.eis.routes.WhoRepaidSharesController.show(Some(2)).url)
      }
    }

    "finds previous repayment details, some of which are incomplete" should {
      lazy val result = {
        setupMocks(Some(incompleteSharesRepaymentDetailsVector))
        testController.change(1)(authorisedFakeRequest)
      }

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "Redirect to the itself page" in {
        redirectLocation(result) shouldBe Some(controllers.eis.routes.WhoRepaidSharesController.show(Some(1)).url)
      }
    }
  }

  "Making a GET request to the ReviewAllInvestorsController remove method" should {

    lazy val result = {
      setupMocks(Some(incompleteSharesRepaymentDetailsVector))
      testController.remove(1)(authorisedFakeRequest)
    }

    "return a status of 303" in {
      status(result) shouldBe SEE_OTHER
    }

    "redirect to the remove investor page" in {
      redirectLocation(result) shouldBe Some(controllers.eis.routes.DeleteSharesRepaymentController.show(1).url)
    }

  }
}
