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
import models.investorDetails.{InvestorDetailsModel, PreviousShareHoldingModel}
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._

import scala.concurrent.Future

class DeleteInvestorControllerSpec extends BaseSpec {

  object TestController extends DeleteInvestorController {
    override lazy val enrolmentConnector = mockEnrolmentConnector
    override lazy val applicationConfig = MockConfig

    override protected def authConnector = MockAuthConnector

    override lazy val s4lConnector = mockS4lConnector
  }

  def setupMocks(investorDetailsModel: Option[Vector[InvestorDetailsModel]]): Unit = {
    mockEnrolledRequest(seisSchemeTypesModel)

    when(mockS4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](Matchers.eq(KeystoreKeys.investorDetails))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(investorDetailsModel))

  }

  "DeleteInvestorController" should {
    "use the correct keystore connector" in {
      DeleteInvestorController.s4lConnector shouldBe S4LConnector
    }

    "use the correct enrollment connector" in {
      DeleteInvestorController.enrolmentConnector shouldBe EnrolmentConnector
    }

    "use the correct config" in {
      DeleteInvestorController.applicationConfig shouldBe FrontendAppConfig
    }

    "use the correct auth connector" in {
      DeleteInvestorController.authConnector shouldBe FrontendAuthConnector
    }
  }

  "no 'investor details list' is retrieved" in {
    mockEnrolledRequest(seisSchemeTypesModel)
    setupMocks(None)
    showWithSessionAndAuth(TestController.show(1))(
      result => {
        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(controllers.seis.routes.AddInvestorOrNomineeController.show(None).url)
      }
    )
  }


  "Load the delete confirmation page page" when {
    "an 'investor details list' is retrieved, and a VALID investor nominee details is provided" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      setupMocks(Some(investorListForDeleteTests))
      showWithSessionAndAuth(TestController.show(1))(
        result => {
          status(result) shouldBe OK
        }
      )
    }
  }

  "Load the delete confirmation page page" when {
    "an 'investor details list' is retrieved, and a VALID investor company details is provided" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      setupMocks(Some(investorListForDeleteTests))
      showWithSessionAndAuth(TestController.show(2))(
        result => {
          status(result) shouldBe OK
        }
      )
    }
  }

  "Submitting to the DeleteInvestorController when authenticated and enrolled" should {
    "redirect to the expected loaction page " in {
      setupMocks(Some(investorListForDeleteTests))
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit(2))(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe
          // TODO: navigate to all investors review page
          Some(controllers.seis.routes.TotalAmountRaisedController.show().url)
        }
      )
    }
  }

}