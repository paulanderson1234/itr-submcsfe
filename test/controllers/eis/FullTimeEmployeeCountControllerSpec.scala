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
import models.FullTimeEmployeeCountModel
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import services.SubmissionService
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future

class FullTimeEmployeeCountControllerSpec extends BaseSpec {

  lazy val controller = new FullTimeEmployeeCountController {
    override lazy val s4lConnector: S4LConnector = mockS4lConnector
    override lazy val enrolmentConnector: EnrolmentConnector = mockEnrolmentConnector
    override lazy val applicationConfig: AppConfig = MockConfig
    override lazy val authConnector: AuthConnector = MockAuthConnector
    val submissionService = mock[SubmissionService]
  }

  val validFullTimeEmploymentCount = Some(FullTimeEmployeeCountModel(22))
  val invalidFullTimeEmploymentCount = Some(FullTimeEmployeeCountModel(28))

  def setupMocks(fullTimeEmployeeCountModel: Option[FullTimeEmployeeCountModel] = None, validCount : Boolean): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[FullTimeEmployeeCountModel](Matchers.eq(KeystoreKeys.fullTimeEmployeeCount))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(fullTimeEmployeeCountModel))
    when(controller.submissionService.validateFullTimeEmployeeCount(Matchers.anyDouble())
      (Matchers.any(), Matchers.any()))
      .thenReturn(validCount)
  }

  "FullTimeEmployeeCountErrorController" should {

    "use the correct keystore connector" in {
      FullTimeEmployeeCountController.s4lConnector shouldBe S4LConnector
    }

    "use the correct enrollment connector" in {
      FullTimeEmployeeCountController.enrolmentConnector shouldBe EnrolmentConnector
    }

    "use the correct config" in {
      FullTimeEmployeeCountController.applicationConfig shouldBe FrontendAppConfig
    }

    "use the correct auth connector" in {
      FullTimeEmployeeCountController.authConnector shouldBe FrontendAuthConnector
    }

    "return a valid 200 response from a show GET request when authorised" in {
      setupMocks(validFullTimeEmploymentCount, validCount = false)
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(controller.show)(
        result => {
          status(result) shouldBe OK
        }
      )
    }

    "Sending a valid employee count form submission to the FullTimeEmployeeCountController when authenticated and enrolled" should {
      "redirect to the HadPreviousRFI page" in {
        val formInput = "employeeCount" -> "22"
        setupMocks(fullTimeEmployeeCountModel = validFullTimeEmploymentCount, validCount = true)
        mockEnrolledRequest(eisSchemeTypesModel)
        submitWithSessionAndAuth(controller.submit,formInput)(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some(controllers.eis.routes.HadPreviousRFIController.show().url)
          }
        )
      }
    }

    "Sending a employee count form that fails API validation submission to the FullTimeEmployeeCountController when authenticated and enrolled" should {
      "redirect to the FullTimeEmployeeCountError page" in {
        val formInput = "employeeCount" -> "44"
        setupMocks(validFullTimeEmploymentCount, validCount = false)
        mockEnrolledRequest(eisSchemeTypesModel)
        submitWithSessionAndAuth(controller.submit,formInput)(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some(controllers.eis.routes.FullTimeEmployeeCountErrorController.show().url)
          }
        )
      }
    }

    "Sending an invalid employee count form submission to the FullTimeEmployeeCountController when authenticated and enrolled" should {
      "rsepond with a bad request" in {
        mockEnrolledRequest(eisSchemeTypesModel)
        val formInput = "isFirstTrade" -> ""
        submitWithSessionAndAuth(controller.submit,formInput)(
          result => {
            status(result) shouldBe BAD_REQUEST
          }
        )
      }
    }
  }
}
