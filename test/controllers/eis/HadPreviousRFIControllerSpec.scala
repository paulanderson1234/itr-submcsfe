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
import models.{HadOtherInvestmentsModel, HadPreviousRFIModel, PreviousSchemeModel}
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class HadPreviousRFIControllerSpec extends BaseSpec {

  object TestController extends HadPreviousRFIController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  val cacheMapRemovedPreviousInvestments: CacheMap = CacheMap("", Map("" -> Json.toJson(Vector[PreviousSchemeModel]())))

  "HadPreviousRFIController" should {
    "use the correct keystore connector" in {
      HadPreviousRFIController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      HadPreviousRFIController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      HadPreviousRFIController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  def setupMocks(hadPreviousRFIModel: Option[HadPreviousRFIModel] = None, hadOtherInvestments: Option[HadOtherInvestmentsModel] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(hadPreviousRFIModel))
    when(mockS4lConnector.fetchAndGetFormData[HadOtherInvestmentsModel](Matchers.eq(KeystoreKeys.hadOtherInvestments))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(hadOtherInvestments))
  }

  "Sending a GET request to HadPreviousRFIController when authenticated and enrolled for EIS" should {
    "return an OK when something is fetched from storage" in {
      setupMocks(Some(hadPreviousRFIModelYes))
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show())(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty form and return an OK when nothing is fetched using storage for EIS" in {
      setupMocks(None)
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show())(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a valid 'Yes' form submit to the HadPreviousRFIController when authenticated and enrolled for EIS" should {
    "REDIRECT to the correct page in the flow" in {
      setupMocks()
      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = "hadPreviousRFI" -> Constants.StandardRadioButtonYesValue
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.HadOtherInvestmentsController.show().url)
        }
      )
    }
  }

  "Sending a valid 'No' form submit to the HadPreviousRFIController when authenticated and enrolled for EIS" should {
    "REDIRECT to the correct page in the flow" in {
      setupMocks()
      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = "hadPreviousRFI" -> Constants.StandardRadioButtonNoValue
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.HadOtherInvestmentsController.show().url)
        }
      )
    }
  }

  "Sending a valid 'No' form submit to the HadPreviousRFIController when authenticated and enrolled for EIS" should {
    "REDIRECT to the correct page in the flow and remove previous investors when hadOtherInvestments is also 'No'" in {
      setupMocks(hadOtherInvestments = Some(hadOtherInvestmentsModelNo))
      mockEnrolledRequest(eisSchemeTypesModel)
      when(mockS4lConnector.saveFormData(Matchers.eq(KeystoreKeys.previousSchemes), Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(cacheMapRemovedPreviousInvestments)
      val formInput = "hadPreviousRFI" -> Constants.StandardRadioButtonNoValue
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.HadOtherInvestmentsController.show().url)
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the HadPreviousRFIController when authenticated and enrolled for EIS" should {
    "load the page with a BAD_REQUEST when a backlink is found" in {
      setupMocks()
      when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkSubsidiaries))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Some(routes.FullTimeEmployeeCountController.show().url)))
      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = "hadPreviousRFI" -> ""
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }
}
