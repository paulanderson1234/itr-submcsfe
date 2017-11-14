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
import config.{AppConfig, FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.BaseSpec
import models.{FullTimeEmployeeCountModel, KiProcessingModel}
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

  val validFullTimeEmploymentEISCount = Some(FullTimeEmployeeCountModel(Constants.fullTimeEquivalenceEISLimit))
  val invalidFullTimeEmploymentEISCount = Some(FullTimeEmployeeCountModel(Constants.fullTimeEquivalenceEISInvalidLimit))
  val invalidFullTimeEmploymentCount = Some(FullTimeEmployeeCountModel(Constants.fullTimeEquivalenceInvalidLimit))
  val validFullTimeEmploymentEISWithKICount = Some(FullTimeEmployeeCountModel(Constants.fullTimeEquivalenceEISWithKILimit))
  val invalidFullTimeEmploymentEISWithKICount = Some(FullTimeEmployeeCountModel(Constants.fullTimeEquivalenceEISWithKIInvalidLimit))

  def setupMocks(fullTimeEmployeeCountModel: Option[FullTimeEmployeeCountModel] = None,
                 validCount : Boolean, kiProcessingModel: Option[KiProcessingModel]): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[FullTimeEmployeeCountModel](Matchers.eq(KeystoreKeys.fullTimeEmployeeCount))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(fullTimeEmployeeCountModel))
    when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(kiProcessingModel))
    when(controller.submissionService.validateFullTimeEmployeeCount(Matchers.any(), Matchers.any())
      (Matchers.any(), Matchers.any()))
      .thenReturn(validCount)
    when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkFullTimeEmployeeCount))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(
      Future.successful(Some(controllers.eis.FullTimeEmployeeCountController.show().toString())))
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

    "return a valid 200 response from a show GET request when authorised with filled FullTimeEmployeeCount form" in {
      setupMocks(validFullTimeEmploymentEISCount, validCount = true, None)
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(controller.show)(
        result => {
          status(result) shouldBe OK
        }
      )
    }

    "return a valid 200 response from a show GET request when authorised with empty form" in {
      setupMocks(None, validCount = true, None)
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(controller.show)(
        result => {
          status(result) shouldBe OK
        }
      )
    }

    "Submitting the form to the FullTimeEmployeeCountController when authenticated and enrolled" should {
      "redirect to the HadPreviousRFI page with valid count" in {
        val formInput = "employeeCount" -> "220"
        setupMocks(validFullTimeEmploymentEISCount, validCount = true, None)
        mockEnrolledRequest(eisSchemeTypesModel)
        submitWithSessionAndAuth(controller.submit,formInput)(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some(controllers.eis.routes.SubsidiariesController.show().url)
          }
        )
      }
      "redirect to the FullTimeEmployeeCountErrorController page with invalid count" in {
        val formInput = "employeeCount" -> "280"
        setupMocks(invalidFullTimeEmploymentEISCount, validCount = false, None)
        mockEnrolledRequest(eisSchemeTypesModel)
        submitWithSessionAndAuth(controller.submit,formInput)(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some(controllers.eis.routes.FullTimeEmployeeCountErrorController.show().url)
          }
        )
      }
    }

    "Submitting the form to the FullTimeEmployeeCountController with KI process when authenticated and enrolled" should {
      "redirect to the HadPreviousRFI page with valid KI count" in {
        val formInput = "employeeCount" -> "500"
        setupMocks(validFullTimeEmploymentEISWithKICount, validCount = true, Some(trueKIModel))
        mockEnrolledRequest(eisSchemeTypesModel)
        submitWithSessionAndAuth(controller.submit,formInput)(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some(controllers.eis.routes.SubsidiariesController.show().url)
          }
        )
      }
      "redirect to the FullTimeEmployeeCountErrorController page with invalid KI count" in {
        val formInput = "employeeCount" -> "580"
        setupMocks(invalidFullTimeEmploymentEISWithKICount, validCount = false, Some(trueKIModel))
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
      "respond with a bad request" in {
        setupMocks(invalidFullTimeEmploymentCount, validCount = false, None)
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
