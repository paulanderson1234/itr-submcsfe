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
import common.KeystoreKeys
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector, SubmissionConnector}
import controllers.helpers.BaseSpec
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class TotalAmountRaisedControllerSpec extends BaseSpec {

  val tradeDateConditionMet = Some(true)
  val tradeDateConditionNotMet = Some(false)
  val validTotalAmountRaisedModel = TotalAmountRaisedModel(12345)

  object TestController extends TotalAmountRaisedController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
    override lazy val submissionConnector = mockSubmissionConnector
  }

  def setupMocks(totalAmountRaisedModel: Option[TotalAmountRaisedModel] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[TotalAmountRaisedModel](Matchers.eq(KeystoreKeys.totalAmountRaised))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(totalAmountRaisedModel))
  }

  def setupSubmissionMocks(tradingConditionLessThanFourMonths: Option[Boolean] = None,
                           businessActivityModel: Option[QualifyBusinessActivityModel] = None,
                           tradeStartDateModel: Option[HasInvestmentTradeStartedModel] = None,
                           researchStartDateModel: Option[ResearchStartDateModel] = None): Unit = {

    when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(CacheMap("", Map())))

    when(mockS4lConnector.fetchAndGetFormData[QualifyBusinessActivityModel](Matchers.eq(KeystoreKeys.isQualifyBusinessActivity))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(businessActivityModel))

    when(mockS4lConnector.fetchAndGetFormData[HasInvestmentTradeStartedModel](Matchers.eq(KeystoreKeys.hasInvestmentTradeStarted))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(tradeStartDateModel))

    when(mockS4lConnector.fetchAndGetFormData[ResearchStartDateModel](Matchers.eq(KeystoreKeys.researchStartDate))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(researchStartDateModel))


    when(TestController.submissionConnector.validateHasInvestmentTradeStartedCondition(
      Matchers.any(), Matchers.any(), Matchers.any())
    (Matchers.any())).thenReturn(tradingConditionLessThanFourMonths)

    when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(CacheMap("", Map())))

  }

  "TotalAmountRaisedController" should {
    "use the correct keystore connector" in {
      TotalAmountRaisedController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      TotalAmountRaisedController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      TotalAmountRaisedController.enrolmentConnector shouldBe EnrolmentConnector
    }
    "use the correct application config" in {
      TotalAmountRaisedController.applicationConfig shouldBe FrontendAppConfig
    }
  }

  "Sending a GET request to TotalAmountRaisedController when authenticated and enrolled" should {
    "return a 200 when a saved model is fetched from keystore" in {
      setupMocks(Some(validTotalAmountRaisedModel))
      mockEnrolledRequest(seisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return an OK 200 when nothing is fetched from keystore" in {
      setupMocks(None)
      mockEnrolledRequest(seisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show())(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a valid form submit and date validation API condition true to the TotalAmountRaisedController" should {
    "redirect to the expected page if valid trade activity has that started is found in s4l but no research date" in {
      setupSubmissionMocks(tradeDateConditionMet, Some(qualifyPrepareToTrade),
        Some(hasInvestmentTradeStartedModelYes), None)
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "amount" -> "1")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TotalAmountSpentController.show().url)
        }
      )
    }
  }


  "Sending a valid form submit and date validation API condition false to the TotalAmountRaisedController" should {
    "redirect to the expected page if valid trade activity has that started in s4l but no research date" in {
      setupSubmissionMocks(tradeDateConditionNotMet, Some(qualifyPrepareToTrade),
        Some(hasInvestmentTradeStartedModelYes), None)
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "amount" -> "1")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.AddInvestorOrNomineeController.show().url)
        }
      )
    }
  }

  "Sending a valid form submit and date validation API condition true to the TotalAmountRaisedController" should {
    "redirect to the expected page if valid research activity that has started in s4l but no trade date found" in {
      setupSubmissionMocks(tradeDateConditionMet, Some(qualifyResearchAndDevelopment),
        None, Some(researchStartDateModelYes))
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "amount" -> "1")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TotalAmountSpentController.show().url)
        }
      )
    }
  }

  "Sending a valid form submit and date validation API condition false to the TotalAmountRaisedController" should {
    "redirect to the expected page if valid research activity that has started in s4l but no trade date found" in {
      setupSubmissionMocks(tradeDateConditionNotMet, Some(qualifyResearchAndDevelopment),
        None, Some(researchStartDateModelYes))
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "amount" -> "1")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.AddInvestorOrNomineeController.show().url)
        }
      )
    }
  }

  "Sending a valid form submit and date validation API condition None (not called) to the TotalAmountRaisedController" should {
    "redirect to the expected page if a valid trade activity that has NOT Started is found in s4l and no trade date" in {
      setupSubmissionMocks(None, Some(qualifyPrepareToTrade),
        Some(hasInvestmentTradeStartedModelNo), None)
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "amount" -> "1")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TotalAmountSpentController.show().url)
        }
      )
    }
  }

  "Sending a valid form submit and date validation API condition None (not called) to the TotalAmountRaisedController" should {
    "redirect to the expected page if valid research activity that has NOT Started is found in s4l and no trade date" in {
      setupSubmissionMocks(None, Some(qualifyResearchAndDevelopment),
        None, Some(researchStartDateModelNo))
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "amount" -> "1")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TotalAmountSpentController.show().url)
        }
      )
    }
  }

  "Sending a valid form submit and date validation API condition None (not called) to the TotalAmountRaisedController" should {
    "redirect to the expected page if no business activity is found is s4l even if research date and trade are date present as started" in {
      setupSubmissionMocks(None, None,
        Some(hasInvestmentTradeStartedModelYes), Some(researchStartDateModelYes))
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "amount" -> "1")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.QualifyBusinessActivityController.show().url)
        }
      )
    }
  }

  "Sending a valid form submit and date validation API condition None (not called) to the TotalAmountRaisedController" should {
    "redirect to the expected page if no business activity is found is s4l even if research date and trade are date present as NOT started" in {
      setupSubmissionMocks(None, None,
        Some(hasInvestmentTradeStartedModelNo), Some(researchStartDateModelNo))
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "amount" -> "1")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.QualifyBusinessActivityController.show().url)
        }
      )
    }
  }

  "Sending a valid form submit and date validation API condition None (not called) to the TotalAmountRaisedController" should {
    "redirect to the expected page if no business activity is found is s4l and research date and trade date are also NOT present" in {
      setupSubmissionMocks(None, None,
        Some(hasInvestmentTradeStartedModelNo), Some(researchStartDateModelNo))
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "amount" -> "1")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.QualifyBusinessActivityController.show().url)
        }
      )
    }
  }

  "Sending a valid form submit where valid research buiness activity and research date exist in S4L to the TotalAmountRaisedController" should {
    "return a 500 internal server error if the API return None instead of the expected true/false response" in {
      setupSubmissionMocks(None, Some(qualifyResearchAndDevelopment),
        None, Some(researchStartDateModelYes))
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "amount" -> "1")(
        result => {
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
      )
    }
  }

  "Sending a valid form submit where valid trade buiness activity and trade date exist in S4L to the TotalAmountRaisedController" should {
    "return a 500 internal server error if the API return None instead of the expected true/false response" in {
      setupSubmissionMocks(None, Some(qualifyPrepareToTrade),
      Some(hasInvestmentTradeStartedModelYes), None)
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "amount" -> "1")(
        result => {
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the TotalAmountRaisedController when authenticated and enrolled" should {
    "return a bad request status" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "amount" -> "")(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }

}
