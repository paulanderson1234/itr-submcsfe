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
import config.FrontendAuthConnector
import connectors.{EnrolmentConnector, S4LConnector, SubmissionConnector}
import controllers.helpers.BaseSpec

import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._

import scala.concurrent.Future

class TenYearPlanControllerSpec extends BaseSpec {

  object TestController extends TenYearPlanController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val submissionConnector = mockSubmissionConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  val noMastersKIModel = KiProcessingModel(Some(true), Some(true), Some(true), None, Some(true), Some(true), Some(true))
  val ineligibleKIModel = KiProcessingModel(Some(true), Some(false), Some(false), Some(false), None, Some(false), Some(true))

  def setupShowMocks(tenYearPlanModel: Option[TenYearPlanModel] = None, kiProcessingModel: Option[KiProcessingModel] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[TenYearPlanModel](Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(tenYearPlanModel))
    when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(kiProcessingModel))
  }

  def setupSubmitMocks(isValid: Option[Boolean] = None, kiProcessingModel: Option[KiProcessingModel] = None): Unit = {
    when(mockSubmissionConnector.validateSecondaryKiConditions(Matchers.any(),Matchers.any())
    (Matchers.any())).thenReturn(Future.successful(isValid))
    when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(kiProcessingModel))
  }

  "TenYearPlanController" should {
    "use the correct keystore connector" in {
      TenYearPlanController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      TenYearPlanController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      TenYearPlanController.enrolmentConnector shouldBe EnrolmentConnector
    }
    "use the correct submission connector" in {
      TenYearPlanController.submissionConnector shouldBe SubmissionConnector
    }
  }

  "Sending a GET request to TenYearPlanController when authenticated and enrolled" should {
    "return a 200 when something is fetched from keystore" in {
      setupShowMocks(Some(tenYearPlanModelYes), Some(trueKIModel))
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore" in {
      setupShowMocks()
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }

  }

  "Sending a valid No form submission to the TenYearPlanController with a false KI Model" should {
    "redirect to the isKI page if no and and no description" in {
      setupSubmitMocks(Some(false), Some(kiModelAssertsKiFalse))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "hasTenYearPlan" -> Constants.StandardRadioButtonNoValue,
        "descriptionTextArea" -> "")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.IsCompanyKnowledgeIntensiveController.show().url)
        }
      )
    }
  }

  "Sending a valid No form submission to the TenYearPlanController with a false KI Model" should {
    "redirect to the expected page if no and and no description" in {
      setupSubmitMocks(Some(false), Some(kiModelWantApplyKiFalse))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "hasTenYearPlan" -> Constants.StandardRadioButtonNoValue,
        "descriptionTextArea" -> "")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.IsKnowledgeIntensiveController.show().url)
        }
      )
    }
  }

  "Sending a valid No form submission to the TenYearPlanController with a Missing assetsKi in KI Model" should {
    "redirect to the expected page if no and and no description" in {
      setupSubmitMocks(Some(false), Some(kiModelMissingAssertKi))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "hasTenYearPlan" -> Constants.StandardRadioButtonNoValue,
        "descriptionTextArea" -> "")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.DateOfIncorporationController.show().url)
        }
      )
    }
  }

  "Sending a valid No form submission to the TenYearPlanController with a Missing wants apply Ki in KI Model" should {
    "redirect to the expected page if no and and no description" in {
      setupSubmitMocks(Some(false), Some(kiModelMissingWantApplyKi))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "hasTenYearPlan" -> Constants.StandardRadioButtonNoValue,
        "descriptionTextArea" -> "")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.DateOfIncorporationController.show().url)
        }
      )
    }
  }

  "Sending a valid No form submission to the TenYearPlanController without a KI Model" should {
    "redirect to the subsidiaries page if no and and no description" in {
      setupSubmitMocks(Some(false))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "hasTenYearPlan" -> Constants.StandardRadioButtonNoValue,
        "descriptionTextArea" -> "")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.DateOfIncorporationController.show().url)
        }
      )
    }
  }

  "Sending a valid No form submission to the TenYearPlanController without hasPercentageWithMasters in the KI Model" should {
    "redirect to the subsidiaries page if no and and no description" in {
      setupSubmitMocks(Some(false), Some(noMastersKIModel))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "hasTenYearPlan" -> Constants.StandardRadioButtonNoValue,
        "descriptionTextArea" -> "")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.DateOfIncorporationController.show().url)
        }
      )
    }
  }

  "Sending a valid No form submission to the TenYearPlanController" should {
    "redirect to the subsidiaries page if no and and no description" in {
      setupSubmitMocks(Some(false), Some(ineligibleKIModel))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "hasTenYearPlan" -> Constants.StandardRadioButtonNoValue,
        "descriptionTextArea" -> "")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.IneligibleForKISecondaryConditionController.show().url)
        }
      )
    }
  }

  "Sending a valid Yes form submission to the TenYearPlanController" should {
    "redirect to the subsidiaries page with valid submission" in {
      setupSubmitMocks(Some(true), Some(trueKIModel))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "hasTenYearPlan" -> Constants.StandardRadioButtonYesValue,
        "descriptionTextArea" -> "text")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.FullTimeEmployeeCountController.show().url)
        }
      )
    }
  }

  "Sending an empty invalid form submission with validation errors to the TenYearPlanController" should {
    "redirect to itself" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "hasTenYearPlan" -> "",
        "descriptionTextArea" -> "")(
        result => {
          status(result) shouldBe BAD_REQUEST
          redirectLocation(result) shouldBe None
        }
      )
    }
  }

  "Sending an an invalid form submission with both Yes and a blank description to the TenYearPlanController" should {
    "redirect to itself with validation errors" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "hasTenYearPlan" -> Constants.StandardRadioButtonYesValue,
        "descriptionTextArea" -> "")(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }
}
