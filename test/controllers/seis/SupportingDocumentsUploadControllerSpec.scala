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
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.BaseSpec
import models.SupportingDocumentsUploadModel
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import services.FileUploadService

import scala.concurrent.Future

class SupportingDocumentsUploadControllerSpec extends BaseSpec {

  object TestController extends SupportingDocumentsUploadController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override val fileUploadService = mockFileUploadService
    override val attachmentsFrontEndUrl = MockConfig.attachmentFileUploadUrl(Constants.schemeTypeSeis.toLowerCase)
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  val supportingDocumentsUploadDoUpload = SupportingDocumentsUploadModel("Yes")
  val supportingDocumentsUploadDontDoUpload = SupportingDocumentsUploadModel("No")

  def setupMocks(backLink: Option[String] = None, supportingDocumentsUploadModel: Option[SupportingDocumentsUploadModel] = None,
                 uploadFeatureEnabled: Boolean = true): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkSupportingDocs))(Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(backLink))
    when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkSubsidiaries))(Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(backLink))
    when(mockS4lConnector.fetchAndGetFormData[SupportingDocumentsUploadModel](Matchers.eq(KeystoreKeys.supportingDocumentsUpload))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(supportingDocumentsUploadModel))
    when(mockFileUploadService.getUploadFeatureEnabled).thenReturn(uploadFeatureEnabled)
  }

    "SupportingDocumentsUploadController" should {
      "use the correct keystore connector" in {
        SupportingDocumentsUploadController.s4lConnector shouldBe S4LConnector
      }
      "use the correct auth connector" in {
        SupportingDocumentsUploadController.authConnector shouldBe FrontendAuthConnector
      }
      "use the correct enrolment connector" in {
        SupportingDocumentsUploadController.enrolmentConnector shouldBe EnrolmentConnector
      }
      "use the correct upload service" in {
        SupportingDocumentsUploadController.fileUploadService shouldBe FileUploadService
      }
    }

    "Sending a GET request to SupportingDocumentsUploadController with upload feature enabled" should {
      "return a 200 OK" in {
        mockEnrolledRequest(seisSchemeTypesModel)
        setupMocks(Some(routes.ConfirmCorrespondAddressController.show().url), Some(supportingDocumentsUploadDoUpload),
          uploadFeatureEnabled = true)
        showWithSessionAndAuth(TestController.show)(
          result => status(result) shouldBe OK
        )
      }
    }

    "Sending a GET request to SupportingDocumentsUploadController with upload feature disabled" should {
      "return a 404 NOT_FOUND" in {
        mockEnrolledRequest(seisSchemeTypesModel)
        setupMocks(Some(routes.ConfirmCorrespondAddressController.show().url), None, uploadFeatureEnabled = false)
        showWithSessionAndAuth(TestController.show)(
          result => status(result) shouldBe NOT_FOUND
        )
      }
    }

    "Sending a Get request to the SupportingDocumentsUploadController when authenticated and enrolled with upload feature disabled" should {
      "redirect to the confirm correspondence address page if no back link is found" in {
        mockEnrolledRequest(seisSchemeTypesModel)
        setupMocks()
        showWithSessionAndAuth(TestController.show)(
          result => {
            status(result) shouldBe SEE_OTHER
          }
        )
      }
    }

    "Sending a Get request to the SupportingDocumentsUploadController when authenticated and enrolled with upload feature enabled" should {
      "redirect to the confirm correspondence address page if no SupportingDocumentsUploadModel is found" in {
        mockEnrolledRequest(seisSchemeTypesModel)
        setupMocks(Some(routes.ConfirmCorrespondAddressController.show().url))
        showWithSessionAndAuth(TestController.show)(
          result => {
            status(result) shouldBe OK
          }
        )
      }
    }

    "Posting to the SupportingDocumentsUploadController when authenticated and enrolled and with upload feature enabled" should {
      "redirect to Check your answers page" in {
        mockEnrolledRequest(seisSchemeTypesModel)
        setupMocks()
        submitWithSessionAndAuth(TestController.submit, "doUpload" -> Constants.StandardRadioButtonYesValue){
          result => status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some(TestController.attachmentsFrontEndUrl)
        }
      }
    }

    "Posting to the SupportingDocumentsUploadController when authenticated and enrolled and with upload feature disabled" should {
      "redirect to Check your answers page" in {
        mockEnrolledRequest(seisSchemeTypesModel)
        setupMocks()
        submitWithSessionAndAuth(TestController.submit, "doUpload" -> Constants.StandardRadioButtonNoValue){
          result => status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some("/investment-tax-relief-cs/seis/check-your-answers")
        }
      }
    }

  "Posting to the SupportingDocumentsUploadController when authenticated and enrolled with a form with errors" should {
    "redirect to itself when a backlink is found" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      setupMocks(Some(routes.ConfirmCorrespondAddressController.show().url), Some(supportingDocumentsUploadDoUpload),
        uploadFeatureEnabled = true)
      submitWithSessionAndAuth(TestController.submit, "doUpload" -> "") {
        result => status(result) shouldBe BAD_REQUEST
      }
    }
  }

  "Posting to the SupportingDocumentsUploadController when authenticated and enrolled with a form with errors" should {
    "redirect to the ConfirmCorrespondAddressController when no backlink is found" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      setupMocks(None, Some(supportingDocumentsUploadDoUpload),
        uploadFeatureEnabled = true)
      submitWithSessionAndAuth(TestController.submit, "doUpload" -> "") {
        result => status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.ConfirmCorrespondAddressController.show().url)
      }
    }
  }

}
