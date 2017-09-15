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
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.BaseSpec
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class ShareIssueDateControllerSpec extends BaseSpec {

  object TestController extends ShareIssueDateController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
    override lazy val submissionConnector = mockSubmissionConnector
  }

  "ShareIssueDateController" should {
    "use the correct keystore connector" in {
      ShareIssueDateController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      ShareIssueDateController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      ShareIssueDateController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  def setupMocks(shareIssueDateModel: Option[ShareIssueDateModel] = None, tradeStartDateModel: Option[TradeStartDateModel] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[ShareIssueDateModel](Matchers.eq(KeystoreKeys.shareIssueDate))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(shareIssueDateModel))
    when(mockS4lConnector.fetchAndGetFormData[TradeStartDateModel](Matchers.eq(KeystoreKeys.tradeStartDate))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(tradeStartDateModel))
  }

  "Sending a GET request to ShareIssueDateController when authenticated and enrolled" should {

    "return a 200 when something is fetched from storage" in {
      setupMocks(shareIssueDateModel = Some(shareIssuetDateModel))
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show())(
        result => {
          status(result) shouldBe OK
        }
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using storage" in {
      setupMocks(shareIssueDateModel = None)
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show())(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a valid form submit to the ShareIssueDateController when authenticated and enrolled" should {
    "redirect to correct page when submission date is within the submission period" in {
      setupMocks(shareIssueDateModel = Some(shareIssueDateModel), tradeStartDateModel = Some(tradeStartDateModelYes))
      when(TestController.submissionConnector.validateSubmissionPeriod(tradeStartDateModelYes.tradeStartDay.get, tradeStartDateModelYes.tradeStartMonth.get,
        tradeStartDateModelYes.tradeStartYear.get, shareIssueDateModel.day.get, shareIssueDateModel.month.get, shareIssueDateModel.year.get)).thenReturn(true)
      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = Seq(
        "shareIssueDay" -> "23",
        "shareIssueMonth" -> "11",
        "shareIssueYear" -> "1993")

      submitWithSessionAndAuth(TestController.submit,formInput:_*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.GrossAssetsController.show().url)
        }
      )
    }
  }

  "Sending a valid form submit to the ShareIssueDateController when authenticated and enrolled" should {
    "redirect to correct error page when submission date is not within the submission period" in {
      setupMocks(shareIssueDateModel = Some(shareIssueDateModel), tradeStartDateModel = Some(tradeStartDateModelYes))
      when(TestController.submissionConnector.validateSubmissionPeriod(tradeStartDateModelYes.tradeStartDay.get, tradeStartDateModelYes.tradeStartMonth.get,
        tradeStartDateModelYes.tradeStartYear.get, shareIssueDateModel.day.get, shareIssueDateModel.month.get, shareIssueDateModel.year.get)).thenReturn(false)
      mockEnrolledRequest(eisSchemeTypesModel)

      val formInput = Seq(
        "shareIssueDay" -> "23",
        "shareIssueMonth" -> "11",
        "shareIssueYear" -> "1993")

      submitWithSessionAndAuth(TestController.submit,formInput:_*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.ShareIssueDateErrorController.show().url)
        }
      )
    }
  }

  "Sending a valid form submit to the ShareIssueDateController when authenticated and enrolled" should {
    "redirect to HasInvestmentTradeStarted page if the model is not in storage" in {
      setupMocks(shareIssueDateModel = Some(shareIssueDateModel), tradeStartDateModel = None)
      mockEnrolledRequest(eisSchemeTypesModel)

      val formInput = Seq(
        "shareIssueDay" -> "23",
        "shareIssueMonth" -> "11",
        "shareIssueYear" -> "1993")

      submitWithSessionAndAuth(TestController.submit,formInput:_*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.HasInvestmentTradeStartedController.show().url)
        }
      )
    }
  }

  "Sending a valid form submit to the ShareIssueDateController when authenticated and enrolled" should {
    "redirect to HasInvestmentTradeStarted page if the model has invalid data" in {
      setupMocks(shareIssueDateModel = Some(shareIssueDateModel), tradeStartDateModel = Some(tradeStartDateModelNo))
      mockEnrolledRequest(eisSchemeTypesModel)

      val formInput = Seq(
        "shareIssueDay" -> "23",
        "shareIssueMonth" -> "11",
        "shareIssueYear" -> "1993")

      submitWithSessionAndAuth(TestController.submit,formInput:_*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.HasInvestmentTradeStartedController.show().url)
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the ShareIssueDateController when authenticated and enrolled" should {
    "return a bad request" in {
      setupMocks(None)
      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = Seq(
        "shareIssueDay" -> "",
        "shareIssueMonth" -> "",
        "shareIssueYear" -> "")

      submitWithSessionAndAuth(TestController.submit,formInput:_*)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }

}

