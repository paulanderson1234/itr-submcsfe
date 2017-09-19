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
import models.ResearchStartDateModel
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class ResearchStartDateControllerSpec extends BaseSpec {

  object TestController extends ResearchStartDateController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
    override lazy val submissionConnector = mockSubmissionConnector
  }

  "ResearchStartDateController" should {
    "use the correct auth connector" in {
      ResearchStartDateController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct keystore connector" in {
      ResearchStartDateController.s4lConnector shouldBe S4LConnector
    }
    "use the correct config" in {
      ResearchStartDateController.applicationConfig shouldBe FrontendAppConfig
    }
    "use the correct enrolment connector" in {
      ResearchStartDateController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  def setupShowMocks(hasStartedResearchModel: Option[ResearchStartDateModel] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[ResearchStartDateModel](Matchers.any())(Matchers.any(), Matchers.any(),
      Matchers.any())).thenReturn(Future.successful(hasStartedResearchModel))

    when(mockS4lConnector.saveFormData(Matchers.eq(KeystoreKeys.backLinkShareIssueDate),
      Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(CacheMap("", Map())))

  }

  def setUpSubmitMocks(tradeIsvalidated:Boolean = false):Unit = {
   when(mockS4lConnector.saveFormData(Matchers.eq(KeystoreKeys.backLinkShareIssueDate),
      Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(CacheMap("", Map())))

    when(TestController.submissionConnector.validateHasInvestmentTradeStartedCondition(Matchers.any(),
      Matchers.any(),Matchers.any())(Matchers.any())).thenReturn(Some(tradeIsvalidated))
  }

  "Sending a GET request to ResearchStartDateController when authenticated and enrolled" should {
    "return a 200 when something is fetched from keystore" in {
      setupShowMocks(Some(researchStartDateModelYes))
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

  "Sending a valid Yes form submission to the ResearchStartDateController when authenticated and enrolled" should {
    "redirect to share issue date when the backend investment start date check returns true (greater than 4 months)" in {
     setUpSubmitMocks(true)
      val formInput = Seq("hasStartedResearch" -> Constants.StandardRadioButtonYesValue,
        "researchStartDay" -> "23",
        "researchStartMonth" -> "11",
        "researchStartYear" -> "2000")
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,formInput: _*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.eis.routes.CommercialSaleController.show().url)
        }
      )
    }
  }

  "Sending a valid Yes form submission to the ResearchStartDateController when authenticated and enrolled" should {
    "redirect to the expected controller when the backend investment start date check returns false (less than 4 months)" in {
      setUpSubmitMocks(false)
      val formInput = Seq("hasStartedResearch" -> Constants.StandardRadioButtonYesValue,
        "researchStartDay" -> "29",
        "researchStartMonth" -> "6",
        "researchStartYear" -> "2017")
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,formInput: _*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.eis.routes.ResearchStartDateErrorController.show().url)
        }
      )
    }
  }

  "Sending a valid No form submission to the ResearchStartDateController when authenticated and enrolled" should {
    "redirect to the expected page" in {
      val formInput = Seq(
        "hasStartedResearch" -> Constants.StandardRadioButtonNoValue,
        "researchStartDay" -> "",
        "researchStartMonth" -> "",
        "researchStartYear" -> "")
      mockEnrolledRequest(eisSchemeTypesModel)
      setUpSubmitMocks(true)
      submitWithSessionAndAuth(TestController.submit,formInput:_*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.eis.routes.ResearchStartDateErrorController.show().url)
        }
      )
    }
  }

  "Sending an invalid form submission to the ResearchStartDateController when authenticated and enrolled" should {
    "respond with a BADREQUEST" in {
      setUpSubmitMocks(true)
      val formInput = Seq(
        "hasStartedResearch" -> "",
        "researchStartDay" -> "",
        "researchStartMonth" -> "",
        "researchStartYear" -> "")
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,formInput:_*)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }

}
