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
import models.investorDetails.{InvestorDetailsModel, PreviousShareHoldingModel}
import models.repayments.SharesRepaymentDetailsModel
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._

import scala.concurrent.Future

class DeleteSharesRepaymentControllerSpec extends BaseSpec {

  object TestController extends DeleteSharesRepaymentController {
    override lazy val enrolmentConnector = mockEnrolmentConnector
    override lazy val applicationConfig = MockConfig

    override protected def authConnector = MockAuthConnector

    override lazy val s4lConnector = mockS4lConnector
  }

  def setupMocks(sharesRepaymentDetails: Option[Vector[SharesRepaymentDetailsModel]] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[Vector[SharesRepaymentDetailsModel]](Matchers.eq(KeystoreKeys.sharesRepaymentDetails))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(sharesRepaymentDetails)

    mockEnrolledRequest(eisSchemeTypesModel)
  }


  "DeleteSharesRepaymentController" should {
    "use the correct keystore connector" in {
      DeleteSharesRepaymentController.s4lConnector shouldBe S4LConnector
    }

    "use the correct enrollment connector" in {
      DeleteSharesRepaymentController.enrolmentConnector shouldBe EnrolmentConnector
    }

    "use the correct config" in {
      DeleteSharesRepaymentController.applicationConfig shouldBe FrontendAppConfig
    }

    "use the correct auth connector" in {
      DeleteSharesRepaymentController.authConnector shouldBe FrontendAuthConnector
    }
  }

    "no 'repayment details list' is retrieved" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupMocks(None)
      showWithSessionAndAuth(TestController.show(2))(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.eis.routes.AnySharesRepaymentController.show().url)
        }
      )
    }


    "a 'repayment details list' is retrieved and an INVALID repayment ID is passed" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupMocks(Some(validSharesRepaymentDetailsVector))
      showWithSessionAndAuth(TestController.show(Constants.obviouslyInvalidId))(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.eis.routes.AnySharesRepaymentController.show().url)
        }
      )
    }


    "a 'repayment details list' is retrieved, a VALID repayment details " in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupMocks(Some(validSharesRepaymentDetailsVector))
      showWithSessionAndAuth(TestController.show(2))(
        result => {
          status(result) shouldBe OK
        }
      )
    }


  "Submitting to the DeleteSharesRepaymentController not the last processingId when authenticated and enrolled" should {
    "redirect to the ReviewPreviousRepayments page " in {

      setupMocks(Some(validSharesRepaymentDetailsVector))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit(2))(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe
            Some(controllers.eis.routes.ReviewPreviousRepaymentsController.show().url)
        }
      )
    }
  }

  "Submitting to the DeleteSharesRepaymentController the last processingId when authenticated and enrolled" should {
    "redirect to the AnySharesRepayment page " in {

      setupMocks(None)
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit(1))(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe
            Some(controllers.eis.routes.ReviewPreviousRepaymentsController.show().url)
        }
      )
    }
  }
}
