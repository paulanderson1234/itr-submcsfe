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
import models.NumberOfSharesModel
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import services.SubmissionService
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future

class NumberOfSharesControllerSpec extends BaseSpec {

  lazy val controller = new NumberOfSharesController {
    override lazy val s4lConnector: S4LConnector = mockS4lConnector
    override lazy val enrolmentConnector: EnrolmentConnector = mockEnrolmentConnector
    override lazy val applicationConfig: AppConfig = MockConfig
    override lazy val authConnector: AuthConnector = MockAuthConnector
    val submissionService = mock[SubmissionService]
  }

  def setupMocks(numberOfSharesControllerModel: Option[NumberOfSharesModel] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[NumberOfSharesModel](Matchers.eq(KeystoreKeys.numberOfShares))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(numberOfSharesControllerModel))
  }

  "NumberOfSharesControllerErrorController" should {

    "use the correct keystore connector" in {
      NumberOfSharesController.s4lConnector shouldBe S4LConnector
    }

    "use the correct enrollment connector" in {
      NumberOfSharesController.enrolmentConnector shouldBe EnrolmentConnector
    }

    "use the correct config" in {
      NumberOfSharesController.applicationConfig shouldBe FrontendAppConfig
    }

    "use the correct auth connector" in {
      NumberOfSharesController.authConnector shouldBe FrontendAuthConnector
    }


    "Sending a GET request to the NumberOfSharesControllerController when authenticated and enrolled" should {
      "return a 200 when something is fetched from keystore" in {
        setupMocks(Some(numberOfSharesModel))
        mockEnrolledRequest(eisSchemeTypesModel)
        showWithSessionAndAuth(controller.show())(
          result => status(result) shouldBe OK
        )
      }

      "provide an empty model and return a 200 when nothing is fetched using keystore" in {
        setupMocks(None)
        mockEnrolledRequest(eisSchemeTypesModel)
        showWithSessionAndAuth(controller.show())(
          result => status(result) shouldBe OK
        )
      }
    }

    "Sending an empty form submission to the NumberOfSharesController when authenticated and enrolled" should {
      "return a BadRequest" in {
        mockEnrolledRequest(eisSchemeTypesModel)
        val formInput = "numberOfShares" -> ""
        submitWithSessionAndAuth(controller.submit,formInput)(
          result => {
            status(result) shouldBe BAD_REQUEST
          }
        )
      }
    }

    "Sending an invalid amount exceeding the maximum to the NumberOfSharesController when authenticated and enrolled" should {
      "return a BadRequest" in {
        mockEnrolledRequest(eisSchemeTypesModel)
        val formInput = "numberOfShares" -> "9999999999999.0000000001"
        submitWithSessionAndAuth(controller.submit,formInput)(
          result => {
            status(result) shouldBe BAD_REQUEST
          }
        )
      }
    }

    "Sending a valid form submission to the NumberOfSharesController when authenticated and enrolled" should {
      "redirect with a success to the expected location" in {
        mockEnrolledRequest(eisSchemeTypesModel)
        val formInput = "numberOfShares" -> "1"
        submitWithSessionAndAuth(controller.submit,formInput)(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some(controllers.eis.routes.TotalAmountRaisedController.show().url)
          }
        )
      }
    }

  }
}
