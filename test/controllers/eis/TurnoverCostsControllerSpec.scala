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
import config.FrontendAuthConnector
import connectors.{EnrolmentConnector, S4LConnector, SubmissionConnector}
import controllers.helpers.BaseSpec
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class TurnoverCostsControllerSpec extends BaseSpec {

  object TestController extends TurnoverCostsController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val submissionConnector = mockSubmissionConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  def setupShowMocks(turnoverCostsModel: Option[AnnualTurnoverCostsModel] = None, checkAveragedAnnualTurnover: Option[Boolean] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[AnnualTurnoverCostsModel](Matchers.eq(KeystoreKeys.turnoverCosts))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(turnoverCostsModel))

    // Change to checkAveragedAnnualTurnover method below when ready and perform additional tests
    when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(checkAveragedAnnualTurnover))
  }

  def setupSubmitMocks(totalAmountRaisedModel: Option[TotalAmountRaisedModel] = None,
                       subsidiariesModel: Option[SubsidiariesModel] = None, checkedTurnover: Option[Boolean] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[TotalAmountRaisedModel](Matchers.eq(KeystoreKeys.totalAmountRaised))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(totalAmountRaisedModel))
    when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(subsidiariesModel))
    when(mockSubmissionConnector.checkAveragedAnnualTurnover(Matchers.any(), Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(checkedTurnover))

    when(mockS4lConnector.saveFormData(Matchers.eq(KeystoreKeys.turnoverAPiCheckPassed),
      Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(CacheMap("", Map())))
  }

  "TurnoverCostsController" should {
    "use the correct keystore connector" in {
      TurnoverCostsController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      TurnoverCostsController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      TurnoverCostsController.enrolmentConnector shouldBe EnrolmentConnector
    }
    "use the correct submission connector" in {
      TurnoverCostsController.submissionConnector shouldBe SubmissionConnector
    }
  }

  "Sending a GET formInput to TurnoverCostsController when Authenticated and enrolled" when {

    "The AnnualTurnoverCostsModel can be obtained from keystore" should {
      "return an OK" in {
        setupShowMocks(Some(annualTurnoverCostsModel), Some(true))
        mockEnrolledRequest(eisSchemeTypesModel)
        showWithSessionAndAuth(TestController.show())(
          result => status(result) shouldBe OK
        )
      }
    }

    "The AnnualTurnoverCostsModel can't be obtained from keystore" should {
      "return an OK" in {
        setupShowMocks(Some(annualTurnoverCostsModel), Some(true))
        mockEnrolledRequest(eisSchemeTypesModel)
        showWithSessionAndAuth(TestController.show())(
          result => status(result) shouldBe OK
        )
      }
    }
  }

  "Sending a valid form submission to the TurnoverCostsController when Authenticated and enrolled" should {
    "redirect to subsidiariess spending investment form when annual turnover check returns true and owns subsidiaries is true" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupSubmitMocks(Some(totalAmountRaisedModel), Some(subsidiariesModelYes), Some(true))
      val formInput = Seq(
        "amount1" -> "100",
        "amount2" -> "100",
        "amount3" -> "100",
        "amount4" -> "100",
        "amount5" -> "100",
        "firstYear" -> "2003",
        "secondYear" -> "2004",
        "thirdYear" -> "2005",
        "fourthYear" -> "2006",
        "fifthYear" -> "2007"
      )
      submitWithSessionAndAuth(TestController.submit, formInput: _*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.SubsidiariesSpendingInvestmentController.show().url)
        }
      )
    }

    "redirect to the correct page when annual turnover check returns true and owns subsidiaries is false" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupSubmitMocks(Some(totalAmountRaisedModel), Some(subsidiariesModelNo), Some(true))
      val formInput = Seq(
        "amount1" -> "100",
        "amount2" -> "100",
        "amount3" -> "100",
        "amount4" -> "100",
        "amount5" -> "100",
        "firstYear" -> "2003",
        "secondYear" -> "2004",
        "thirdYear" -> "2005",
        "fourthYear" -> "2006",
        "fifthYear" -> "2007"
      )
      submitWithSessionAndAuth(TestController.submit, formInput: _*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.MarketDescriptionController.show().url)
        }
      )
    }

    "redirect to the correct page when annual turnover check returns false" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupSubmitMocks(Some(totalAmountRaisedModel), Some(subsidiariesModelNo), Some(false))
      val formInput = Seq(
        "amount1" -> "100",
        "amount2" -> "100",
        "amount3" -> "100",
        "amount4" -> "100",
        "amount5" -> "100",
        "firstYear" -> "2003",
        "secondYear" -> "2004",
        "thirdYear" -> "2005",
        "fourthYear" -> "2006",
        "fifthYear" -> "2007"
      )
      submitWithSessionAndAuth(TestController.submit, formInput: _*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.ThirtyDayRuleController.show().url)
        }
      )
    }

    "redirect to total amount raised when no total amount raised is returned from keystore" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupSubmitMocks(subsidiariesModel = Some(subsidiariesModelYes))
      val formInput = Seq(
        "amount1" -> "100",
        "amount2" -> "100",
        "amount3" -> "100",
        "amount4" -> "100",
        "amount5" -> "100",
        "firstYear" -> "2003",
        "secondYear" -> "2004",
        "thirdYear" -> "2005",
        "fourthYear" -> "2006",
        "fifthYear" -> "2007"
      )
      submitWithSessionAndAuth(TestController.submit, formInput: _*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TotalAmountRaisedController.show().url)
        }
      )
    }

    "redirect to subsidiaries page when no subsidiaries model is returned from keystore" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupSubmitMocks(Some(totalAmountRaisedModel),checkedTurnover = Some(true))
      val formInput = Seq(
        "amount1" -> "100",
        "amount2" -> "100",
        "amount3" -> "100",
        "amount4" -> "100",
        "amount5" -> "100",
        "firstYear" -> "2003",
        "secondYear" -> "2004",
        "thirdYear" -> "2005",
        "fourthYear" -> "2006",
        "fifthYear" -> "2007"
      )
      submitWithSessionAndAuth(TestController.submit, formInput: _*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.SubsidiariesController.show().url)
        }
      )
    }

  }

  "Sending an invalid form submit to the TurnoverCostsController when Authenticated and enrolled" should {
    "return a bad request" in {
      setupSubmitMocks(subsidiariesModel = Some(subsidiariesModelNo))
      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = Seq(
        "amount1" -> "",
        "amount2" -> "",
        "amount3" -> "",
        "amount4" -> "",
        "amount5" -> "",
        "firstYear" -> "2003",
        "secondYear" -> "2004",
        "thirdYear" -> "2005",
        "fourthYear" -> "2006",
        "fifthYear" -> "2007"
      )
      submitWithSessionAndAuth(TestController.submit, formInput: _*)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }
}
