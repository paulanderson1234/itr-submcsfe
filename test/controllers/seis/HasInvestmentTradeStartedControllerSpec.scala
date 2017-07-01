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
import config.FrontendAuthConnector
import connectors.{EnrolmentConnector, S4LConnector, SubmissionConnector}
import controllers.helpers.BaseSpec
import models.HasInvestmentTradeStartedModel
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class HasInvestmentTradeStartedControllerSpec extends BaseSpec {

  object TestController extends HasInvestmentTradeStartedController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
    override lazy val submissionConnector = mockSubmissionConnector
  }

  "HasInvestmentTradeStartedController" should {
    "use the correct auth connector" in {
      HasInvestmentTradeStartedController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct keystore connector" in {
      HasInvestmentTradeStartedController.s4lConnector shouldBe S4LConnector
    }
    "use the correct enrolment connector" in {
      HasInvestmentTradeStartedController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  def setupShowMocks(hasInvestmentTradeStartedModel: Option[HasInvestmentTradeStartedModel] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[HasInvestmentTradeStartedModel](Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(hasInvestmentTradeStartedModel))
    when(mockS4lConnector.saveFormData(Matchers.eq(KeystoreKeys.backLinkSeventyPercentSpent),
      Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(CacheMap("", Map())))

    when(mockS4lConnector.saveFormData(Matchers.eq(KeystoreKeys.backLinkShareIssueDate),
      Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(CacheMap("", Map())))
  }

  def setUpSubmitMocks(tradeIsvalidated:Boolean = false):Unit = {
    when(mockS4lConnector.saveFormData(Matchers.eq(KeystoreKeys.backLinkSeventyPercentSpent),
      Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(CacheMap("", Map())))

    when(mockS4lConnector.saveFormData(Matchers.eq(KeystoreKeys.backLinkShareIssueDate),
      Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(CacheMap("", Map())))

    when(TestController.submissionConnector.validateHasInvestmentTradeStartedCondition(Matchers.any(),
      Matchers.any(),Matchers.any())(Matchers.any())).thenReturn(Some(tradeIsvalidated))
  }


  "Sending a GET request to HasInvestmentTradeStartedController when authenticated and enrolled" should {
    "return a 200 when something is fetched from keystore" in {
      setupShowMocks(Some(hasInvestmentTradeStartedModelYes))
      mockEnrolledRequest(seisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore" in {
      setupShowMocks()
      mockEnrolledRequest(seisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a valid Yes form submission to the HasInvestmentTradeStartedController when authenticated and enrolled" should {
    "redirect to share issue date when the investment start date is greater than 4 months" in {
      setUpSubmitMocks(true)
      val formInput = Seq("hasInvestmentTradeStarted" -> Constants.StandardRadioButtonYesValue,
        "hasInvestmentTradeStartedDay" -> "23",
        "hasInvestmentTradeStartedMonth" -> "11",
        "hasInvestmentTradeStartedYear" -> "2000")
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,formInput: _*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.seis.routes.ShareIssueDateController.show().url)
        }
      )
    }
  }

  "Sending a valid Yes form submission to the HasInvestmentTradeStartedController when authenticated and enrolled" should {
    "redirect to itself(todo) if the investment start date is less than 4 months" in {
      setUpSubmitMocks(false)
      val formInput = Seq("hasInvestmentTradeStarted" -> Constants.StandardRadioButtonYesValue,
        "hasInvestmentTradeStartedDay" -> "29",
        "hasInvestmentTradeStartedMonth" -> "6",
        "hasInvestmentTradeStartedYear" -> "2017")
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,formInput: _*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.seis.routes.SeventyPercentSpentController.show().url)
        }
      )
    }
  }

  "Sending a valid No form submission to the HasInvestmentTradeStartedController when authenticated and enrolled" should {
    "redirect to itself(todo)" in {
      setUpSubmitMocks(true)
      val formInput = Seq(
        "hasInvestmentTradeStarted" -> Constants.StandardRadioButtonNoValue,
        "hasInvestmentTradeStartedDay" -> "",
        "hasInvestmentTradeStartedMonth" -> "",
        "hasInvestmentTradeStartedYear" -> "")
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,formInput:_*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.seis.routes.SeventyPercentSpentController.show().url)
        }
      )
    }
  }

  "Sending an invalid form submission to the HasInvestmentTradeStartedController when authenticated and enrolled" should {
    "redirect respond with BADREQUEST" in {
      val formInput = Seq(
        "hasInvestmentTradeStarted" -> "",
        "hasInvestmentTradeStartedDay" -> "",
        "hasInvestmentTradeStartedMonth" -> "",
        "hasInvestmentTradeStartedYear" -> "")
      setUpSubmitMocks(true)
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,formInput:_*)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }

}
