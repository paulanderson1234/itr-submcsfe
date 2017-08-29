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

import java.util.concurrent.TimeUnit.SECONDS

import auth.AuthEnrolledTestController.{INTERNAL_SERVER_ERROR => _, NO_CONTENT => _, OK => _, SEE_OTHER => _, _}
import auth._
import common.KeystoreKeys
import config.FrontendAuthConnector
import connectors.{EnrolmentConnector, S4LConnector, SubmissionConnector}
import controllers.feedback
import controllers.helpers.BaseSpec
import models._
import models.submission.{SchemeTypesModel, SubmissionResponse}
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.libs.json.Json
import play.api.test.Helpers._
import services.FileUploadService
import uk.gov.hmrc.play.http.HttpResponse

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class AcknowledgementControllerSpec extends BaseSpec {

  val contactValid = ContactDetailsModel("first", "last", Some("07000 111222"), None, "test@test.com")
  val contactInvalid = ContactDetailsModel("first", "last", Some("07000 111222"), None, "test@badrequest.com")
  val yourCompanyNeed = YourCompanyNeedModel("AA")
  val submissionRequestValid = SubmissionRequest(contactValid, yourCompanyNeed)
  val submissionRequestInvalid = SubmissionRequest(contactInvalid, yourCompanyNeed)
  val submissionResponse = SubmissionResponse("2014-12-17", "FBUND09889765")

  implicit val user = mock[TAVCUser]

  object TestController extends AcknowledgementController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
    override lazy val registrationDetailsService = mockRegistrationDetailsService
    override lazy val s4lConnector = mockS4lConnector
    override lazy val submissionConnector = mockSubmissionConnector
    override lazy val fileUploadService = mockFileUploadService
  }

  class SetupPageFull() {
    setUpMocks(mockS4lConnector)
    setUpMocksRegistrationService(mockRegistrationDetailsService)
    when(mockS4lConnector.fetchAndGetFormData[TradeStartDateModel](Matchers.eq(KeystoreKeys.tradeStartDate))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Some(tradeStartDateModelYes))
  }

  class SetupPageMinimum() {
    when(mockSubmissionConnector.submitAdvancedAssurance(Matchers.any(), Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(submissionResponse)))))
    setUpMocksMinimumRequiredModels(mockS4lConnector)
    setUpMocksRegistrationService(mockRegistrationDetailsService)
  }

  def setupMocks(): Unit = {
    when(mockSubmissionConnector.submitAdvancedAssurance(Matchers.any(), Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(submissionResponse)))))
    when(mockS4lConnector.fetchAndGetFormData[TradeStartDateModel](Matchers.eq(KeystoreKeys.tradeStartDate))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Some(tradeStartDateModelYes))
    when(mockS4lConnector.fetchAndGetFormData[SchemeTypesModel](Matchers.eq(KeystoreKeys.selectedSchemes))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Some(schemeTypesSEIS))
  }

  "AcknowledgementController" should {
    "use the correct keystore connector" in {
      AcknowledgementController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      AcknowledgementController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct submission connector" in {
      AcknowledgementController.submissionConnector shouldBe SubmissionConnector
    }
    "use the correct enrolment connector" in {
      AcknowledgementController.enrolmentConnector shouldBe EnrolmentConnector
    }
    "use the correct file upload service" in {
      AcknowledgementController.fileUploadService shouldBe FileUploadService
    }
  }

  "Extracting all the answers from the SEIS flow" should {

    "return an error if any of the calls to save for later fail" in {
      setupMocksCs(mockS4lConnector)
      when(mockS4lConnector.fetchAndGetFormData[NatureOfBusinessModel](Matchers.eq(KeystoreKeys.natureOfBusiness))(Matchers.any(),
        Matchers.any(), Matchers.any())).thenReturn(Future.failed(new Exception("test error")))

      intercept[Exception](await(TestController.getAnswers)).getMessage shouldBe "test error"
    }

    "return a None if any of the mandatory data is missing" in {
      setupMocksCs(mockS4lConnector)
      when(mockS4lConnector.fetchAndGetFormData[NatureOfBusinessModel](Matchers.eq(KeystoreKeys.natureOfBusiness))(Matchers.any(),
        Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))

      await(TestController.getAnswers) shouldBe None
    }

    "return a valid model if all required data is found" in {
      setupMocksCs(mockS4lConnector)

      await(TestController.getAnswers).contains(validSEISAnswersModel) shouldBe true
    }
  }

  "Sending an Authenticated and Enrolled GET request with a session to AcknowledgementController" should {
    "return a 200 and delete the current application when a valid submission data is submitted" in new SetupPageFull {
      when(mockFileUploadService.getUploadFeatureEnabled).thenReturn(false)
      when(mockS4lConnector.clearCache()(Matchers.any(), Matchers.any())).thenReturn(HttpResponse(NO_CONTENT))
      setupMocks()
      mockEnrolledRequest(seisSchemeTypesModel)
      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe OK
    }
  }

  "Sending an Authenticated and Enrolled GET request with a session to AcknowledgementController" should {
    "return a 200, close the file upload envelope and " +
      "delete the current application when a valid submission data is submitted with the file upload flag enabled" in new SetupPageFull {
      when(mockFileUploadService.getUploadFeatureEnabled).thenReturn(true)
      when(mockFileUploadService.closeEnvelope(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(),
        Matchers.any())).thenReturn(Future(HttpResponse(OK)))
      when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.envelopeId))
        (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(envelopeId))
      when(mockS4lConnector.clearCache()(Matchers.any(), Matchers.any())).thenReturn(HttpResponse(NO_CONTENT))
      setupMocks()
      mockEnrolledRequest(seisSchemeTypesModel)
      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe OK
    }
  }

  "Sending an Authenticated and Enrolled GET request with a session to AcknowledgementController" should {
    "return a 200 and delete the current application when a valid submission data is submitted with minimum expected data" in new SetupPageMinimum {
      when(mockFileUploadService.getUploadFeatureEnabled).thenReturn(false)
      when(mockS4lConnector.clearCache()(Matchers.any(), Matchers.any())).thenReturn(HttpResponse(NO_CONTENT))
      setupMocks()
      mockEnrolledRequest(seisSchemeTypesModel)
      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe OK
    }
  }

  "Sending an Authenticated and Enrolled GET request with a session to AcknowledgementController" should {
    "return a 303 redirect if mandatory NatureOfBusinessModel is missing from keystore" in {

      setUpMocksTestMinimumRequiredModels(mockS4lConnector, mockRegistrationDetailsService, Some(kiProcModelValid),
        natureBusiness = None, Some(contactValid), Some(proposedInvestmentValid),
        Some(investmentGrowValid), Some(dateOfIncorporationValid), Some(fullCorrespondenceAddress), true)
      setupMocks()
      mockEnrolledRequest(seisSchemeTypesModel)
      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.ApplicationHubController.show().url)
    }
  }

  "Sending an Authenticated and Enrolled GET request with a session to AcknowledgementController" should {
    "return a 303 redirect if mandatory ContactDetailsModel is missing from keystore" in {

      setUpMocksTestMinimumRequiredModels(mockS4lConnector, mockRegistrationDetailsService, Some(kiProcModelValid),
        Some(natureOfBusinessValid), contactDetails = None, Some(proposedInvestmentValid),
        Some(investmentGrowValid), Some(dateOfIncorporationValid), Some(fullCorrespondenceAddress), true)
      setupMocks()
      mockEnrolledRequest(seisSchemeTypesModel)
      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.ApplicationHubController.show().url)
    }
  }

  "Sending an Authenticated and Enrolled GET request with a session to AcknowledgementController" should {
    "return a 303 redirect if mandatory ProposedInvestmentModel is missing from keystore" in {

      setUpMocksTestMinimumRequiredModels(mockS4lConnector, mockRegistrationDetailsService, Some(kiProcModelValid),
        Some(natureOfBusinessValid), Some(contactValid), proposedInvestment = None,
        Some(investmentGrowValid), Some(dateOfIncorporationValid), Some(fullCorrespondenceAddress), true)
      setupMocks()
      mockEnrolledRequest(seisSchemeTypesModel)
      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.ApplicationHubController.show().url)
    }
  }

  "Sending an Authenticated and Enrolled GET request with a session to AcknowledgementController" should {
    "return a 303 redirect if mandatory DateOfIncorporationModel is missing from keystore" in {

      setUpMocksTestMinimumRequiredModels(mockS4lConnector, mockRegistrationDetailsService, Some(kiProcModelValid),
        Some(natureOfBusinessValid), Some(contactValid), Some(proposedInvestmentValid),
        Some(investmentGrowValid), dateIncorp = None, Some(fullCorrespondenceAddress), true)
      setupMocks()
      mockEnrolledRequest(seisSchemeTypesModel)
      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.ApplicationHubController.show().url)
    }
  }

  "Sending an Authenticated and Enrolled GET request with a session to AcknowledgementController" should {
    "return a 303 redirect if mandatory AddressModel (contact address) is missing from keystore" in {

      setUpMocksTestMinimumRequiredModels(mockS4lConnector, mockRegistrationDetailsService, Some(kiProcModelValid),
        Some(natureOfBusinessValid), Some(contactValid), Some(proposedInvestmentValid),
        Some(investmentGrowValid), Some(dateOfIncorporationValid), contactAddress = None, true)
      setupMocks()
      mockEnrolledRequest(seisSchemeTypesModel)
      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.ApplicationHubController.show().url)
    }
  }

  "Sending an Authenticated and Enrolled GET request with a session to AcknowledgementController" should {
    "return a 303 redirect if mandatory registrationDetailsModel is from registration details service" in {

      setUpMocksTestMinimumRequiredModels(mockS4lConnector, mockRegistrationDetailsService, Some(kiProcModelValid),
        Some(natureOfBusinessValid), Some(contactValid), Some(proposedInvestmentValid),
        Some(investmentGrowValid), Some(dateOfIncorporationValid), Some(fullCorrespondenceAddress), false)
      setupMocks()
      mockEnrolledRequest(seisSchemeTypesModel)
      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.ApplicationHubController.show().url)
    }
  }

  "Sending an Authenticated and Enrolled GET request with a session to AcknowledgementController" should {
    "return a 5xx when an invalid email is submitted" in new SetupPageFull {
      when(mockS4lConnector.fetchAndGetFormData[SchemeTypesModel](Matchers.eq(KeystoreKeys.selectedSchemes))
        (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Some(schemeTypesEIS))
      when(mockSubmissionConnector.submitAdvancedAssurance(Matchers.any(), Matchers.any())(Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(INTERNAL_SERVER_ERROR)))
      mockEnrolledRequest(seisSchemeTypesModel)
      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }
  }

  "Sending a POST request to the Acknowledgement controller when authenticated and enrolled" should {
    "redirect to the feedback page" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(feedback.routes.FeedbackController.show().url)
        }
      )
    }
  }

}

