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
import config.FrontendAuthConnector
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.BaseSpec
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._

import scala.concurrent.Future

class ShareDescriptionControllerSpec extends BaseSpec {

  lazy val validBackLink = controllers.seis.routes.HadOtherInvestmentsController.show().toString
  val shareIssueDateString = "1 January 2008"
  val validShareIssueDateModel = ShareIssueDateModel(Some(1), Some(1), Some(2008))

  object TestController extends ShareDescriptionController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  "ShareDescriptionController" should {
    "use the correct keystore connector" in {
      ShareDescriptionController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      ShareDescriptionController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      ShareDescriptionController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  def setupMocks(shareDescriptionModel: Option[ShareDescriptionModel] = None, backUrl: Option[String] = None,
                 shareIssueDate: Option[ShareIssueDateModel] = None) :Unit = {
    when(mockS4lConnector.fetchAndGetFormData[ShareDescriptionModel](Matchers.eq(KeystoreKeys.shareDescription))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(shareDescriptionModel))
    when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkShareDescription))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(backUrl))
    when(mockS4lConnector.fetchAndGetFormData[ShareIssueDateModel](Matchers.eq(KeystoreKeys.shareIssueDate))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(shareIssueDate))
  }

  "Sending a GET request to ShareDescriptionController when authenticated and enrolled" should {
    "redirect to the 'HadOtherInvestments' page when no valid back link is present" in {
      setupMocks(shareIssueDate = Some(validShareIssueDateModel))
      mockEnrolledRequest(seisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.seis.routes.HadOtherInvestmentsController.show().toString)
        }
      )
    }
    "return a 200 when something is fetched from keystore when a valid back and share issue date link ae present" in {
      setupMocks(Some(shareDescriptionModel), Some(validBackLink), Some(validShareIssueDateModel))
      mockEnrolledRequest(seisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show())(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore when a valid back link and share issue date link ae present" in {
      setupMocks(None, Some(validBackLink), Some(validShareIssueDateModel))
      mockEnrolledRequest(seisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show())(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a valid form submit to the ShareDescriptionController when authenticated and enrolled" should {
    "redirect to the number of shares in issue page" in {
      setupMocks(None,Some(validBackLink))
      mockEnrolledRequest(seisSchemeTypesModel)
      val formInput = "descriptionTextArea" -> "some text so it's valid"

      submitWithSessionAndAuth(TestController.submit(shareIssueDateString),formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.NumberOfSharesController.show().url)
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the ShareDescriptionController when authenticated and enrolled" should {
    "redirect to itself" in {
      setupMocks(None, Some(validBackLink))
      mockEnrolledRequest(seisSchemeTypesModel)
      val formInput = "descriptionTextArea" -> ""
      submitWithSessionAndAuth(TestController.submit(shareIssueDateString),formInput)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }

}
