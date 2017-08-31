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
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class ShareCapitalChangesControllerSpec extends BaseSpec {

  val shareCapitalChangesModelYes = ShareCapitalChangesModel(Constants.StandardRadioButtonYesValue, Some("test"))
  val shareCapitalChangesModelNo = ShareCapitalChangesModel(Constants.StandardRadioButtonNoValue, None)

  object TestController extends ShareCapitalChangesController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  def setupMocks(shareCapitalChangesModel: Option[ShareCapitalChangesModel] = None, shareIssueDate: Option[ShareIssueDateModel] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[ShareCapitalChangesModel](Matchers.eq(KeystoreKeys.shareCapitalChanges))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(shareCapitalChangesModel))
    when(mockS4lConnector.fetchAndGetFormData[ShareIssueDateModel](Matchers.eq(KeystoreKeys.shareIssueDate))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(shareIssueDate))
    when(mockS4lConnector.saveFormData(Matchers.eq(KeystoreKeys.shareCapitalChanges),
      Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(CacheMap("", Map())))
  }

  "ShareCapitalChangesController" should {
    "use the correct storage connector" in {
      ShareCapitalChangesController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      ShareCapitalChangesController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      ShareCapitalChangesController.enrolmentConnector shouldBe EnrolmentConnector
    }
    "use the correct application config" in {
      ShareCapitalChangesController.applicationConfig shouldBe FrontendAppConfig
    }
  }

  "Sending a GET request to ShareCapitalChangesController when authenticated and enrolled" should {
    "return a 200 when a saved yes model and valid shareIssueDate are fetched from storage" in {
      setupMocks(Some(shareCapitalChangesModelYes), Some(shareIssuetDateModel))
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }

    "return a 200 when a saved no model and valid shareIssueDate are fetched from storage" in {
      setupMocks(Some(shareCapitalChangesModelNo), Some(shareIssuetDateModel))
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }

    "return a 200 when no model exists in storage but valid shareIssueDate is present in storage" in {
      setupMocks(None, Some(shareIssuetDateModel))
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }

    "redirect to the expected start of flow page when no valid shareIssueDate is present in storage" in {
      setupMocks(Some(shareCapitalChangesModelNo), None)
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.ShareIssueDateController.show().url)
        }
      )
    }
  }


  "Sending a valid yes form submit to the ContactDetailsController when authenticated and enrolled" should {
    "redirect to the Confirm Correspondence Address Controller page" in {
      setupMocks(None, Some(shareIssuetDateModel))
      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = Seq(
        "hasChanges" -> Constants.StandardRadioButtonYesValue,
        "descriptionTextArea" -> "test"
      )
      submitWithSessionAndAuth(TestController.submit,formInput:_*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.ConfirmContactDetailsController.show().url)
        }
      )
    }
  }

  "Sending a valid no form submit to the ContactDetailsController when authenticated and enrolled" should {
    "redirect to the Confirm Correspondence Address Controller page" in {
      setupMocks(None, Some(shareIssuetDateModel))
      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = Seq(
        "hasChanges" -> Constants.StandardRadioButtonNoValue,
        "descriptionTextArea" -> ""
      )
      submitWithSessionAndAuth(TestController.submit,formInput:_*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.ConfirmContactDetailsController.show().url)
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the ShareCapitalChangesController when authenticated and enrolled" should {
    "return a bad request status" in {
      setupMocks(Some(shareCapitalChangesModelYes), Some(shareIssuetDateModel))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "hasChanges" -> "")(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }

}
