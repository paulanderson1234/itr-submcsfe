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

import auth.AuthEnrolledTestController.{INTERNAL_SERVER_ERROR => _, NO_CONTENT => _, OK => _, SEE_OTHER => _, _}
import auth._
import common.KeystoreKeys
import config.FrontendAuthConnector
import connectors.{EnrolmentConnector, S4LConnector, SubmissionConnector}
import controllers.helpers.BaseSpec
import fixtures.ModelSubmissionFixture
import models._
import models.submission.SchemeTypesModel
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import services.FileUploadService
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future

class AcknowledgementControllerSpec extends BaseSpec with ModelSubmissionFixture {

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

  "Extracting all the answers from the SSEIS flow" should {
    "return a valid model if all required data is found and calling validate should pass" in {
      setupSeisSubmissionMocks()
      val model = await(TestController.getAnswers.get)
      model shouldBe SEISAnswersModel
      await(model.validateSeis(mockSubmissionConnector)) shouldBe true
    }
  }

  "Extracting all the answers from the SSEIS flow" should {
    "return an error if any of the calls to save for later fail" in {
      setupSeisSubmissionMocks()
      when(mockS4lConnector.fetchAndGetFormData[NatureOfBusinessModel](Matchers.eq(KeystoreKeys.natureOfBusiness))(Matchers.any(),
        Matchers.any(), Matchers.any())).thenReturn(Future.failed(new Exception("test error")))
      when(mockS4lConnector.fetchAndGetFormData[SchemeTypesModel](Matchers.eq(KeystoreKeys.selectedSchemes))
        (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(schemeTypesSEIS)))
      intercept[Exception](await(TestController.getAnswers)).getMessage shouldBe "test error"
    }
  }

  "Extracting all the answers from the SSEIS flow" should {
    "return a None if any of the mandatory data is missing (nature of business)" in {
      setupSeisSubmissionMocks(natureOfBusiness = None)
      await(TestController.getAnswers) shouldBe None

      mockEnrolledRequest(seisSchemeTypesModel)
      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the SEIS flow" should {
    "return a None if any of the mandatory data is missing (date of incorporation)" in {
      setupSeisSubmissionMocks(dateOfIncorporation = None)
      await(TestController.getAnswers) shouldBe None

      mockEnrolledRequest(seisSchemeTypesModel)
      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the SEIS flow" should {
    "return a None if any of the mandatory data is missing (qualifying business activity)" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      setupSeisSubmissionMocks(qualifyBusinessActivity = None)
      await(TestController.getAnswers) shouldBe None

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the SEIS flow" should {
    "return a None if any of the mandatory data is missing (share issue date)" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      setupSeisSubmissionMocks(shareIssueDate = None)
      await(TestController.getAnswers) shouldBe None

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the SEIS flow" should {
    "return a None if any of the mandatory data is missing in storage (gross assets)" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      setupSeisSubmissionMocks(grossAssets = None)
      await(TestController.getAnswers) shouldBe None

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }


  "Extracting all the answers from the SEIS flow" should {
    "return a None if any of the mandatory data is missing in storage(full time employee count)" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      setupSeisSubmissionMocks(fullTimeEmploymentCount = None)
      await(TestController.getAnswers) shouldBe None

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the SEIS flow" should {
    "return a model if both trade date and research date are not present in storage but fail validation" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      setupSeisSubmissionMocks(hasInvestmentTradeStarted = None, researchStartDate = None)
      val model = await(TestController.getAnswers)
      model.nonEmpty shouldBe true

      await(model.get.validateSeis(mockSubmissionConnector)) shouldBe false

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the SEIS flow" should {
    "return a None if any of the mandatory data is missing in storage (HadPreviousRFIModel)" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      setupSeisSubmissionMocks(hadPreviousRFI = None)
      await(TestController.getAnswers) shouldBe None

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the SEIS flow" should {
    "return a None if any of the mandatory data is missing in storage (HadOtherInvestmentsModel)" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      setupSeisSubmissionMocks(hadOtherInvestments = None)
      await(TestController.getAnswers) shouldBe None

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the SEIS flow" should {
    "return a None if any of the mandatory data is missing in storage(ShareDescriptionModel)" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      setupSeisSubmissionMocks(shareDescription = None)
      await(TestController.getAnswers) shouldBe None

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the SEIS flow" should {
    "return a None if any of the mandatory data is missing in storage(NumberOfSharesModel)" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      setupSeisSubmissionMocks(numberOfShares = None)
      await(TestController.getAnswers) shouldBe None

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the SEIS flow" should {
    "return a None if any of the mandatory data is missing in storage(TotalAmountRaisedModel)" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      setupSeisSubmissionMocks(totalRaised = None)
      await(TestController.getAnswers) shouldBe None

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the SEIS flow" should {
    "return a None if any of the mandatory data is missing in storage(InvestorDetailsModel)" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      setupSeisSubmissionMocks(investorDetails = None)
      await(TestController.getAnswers) shouldBe None

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the SEIS flow" should {
    "return a None if any of the mandatory data is missing in storage(WasAnyValueReceivedModel)" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      setupSeisSubmissionMocks(wasAnyValueReceived = None)
      await(TestController.getAnswers) shouldBe None

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the SEIS flow" should {
    "return a None if any of the mandatory data is missing in storage(ShareCapitalChangesModel)" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      setupSeisSubmissionMocks(shareCapitalChanges = None)
      await(TestController.getAnswers) shouldBe None

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the SEIS flow" should {
    "return a None if any of the mandatory data is missing in storage(ContactDetailsModel)" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      setupSeisSubmissionMocks(contactDetails = None)
      await(TestController.getAnswers) shouldBe None

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the SEIS flow" should {
    "return a None if any of the mandatory data is missing in storage(AddressModel)" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      setupSeisSubmissionMocks(contactAddress = None)
      await(TestController.getAnswers) shouldBe None

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the SEIS flow" should {
    "return a None and redirect if investment start date is present but validateHasInvestmentTradeStartedCondition API fails validation " +
      "and no 70 percent model" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      setupSeisSubmissionMocks(getHasInvestmentTradeStartedCondition = None, seventyPercentSpent = None)
      val model = await(TestController.getAnswers)
      model.nonEmpty shouldBe true

      await(model.get.validateSeis(mockSubmissionConnector)) shouldBe false

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the SEIS flow" should {
    "return a None and redirect if provided with an investment start Date that fails API check and there is no SeventyPercentSpentModel " +
      "and no 70 percent model" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      setupSeisSubmissionMocks(getHasInvestmentTradeStartedCondition = Some(false), seventyPercentSpent = None)
      val model = await(TestController.getAnswers)
      model.nonEmpty shouldBe true

      await(model.get.validateSeis(mockSubmissionConnector)) shouldBe false

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the SEIS flow" should {
    "return a None and redirect if research start date is present for a research business activity" +
      "but validateHasInvestmentTradeStartedCondition API retuns false and there is no seventy percent model in storage" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      setupSeisSubmissionMocks(qualifyBusinessActivity = Some(qualifyResearchAndDevelopment), getHasInvestmentTradeStartedCondition = Some(false), seventyPercentSpent = None)
      val model = await(TestController.getAnswers)
      model.nonEmpty shouldBe true

      await(model.get.validateSeis(mockSubmissionConnector)) shouldBe false

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the SEIS flow" should {
    "return a None and redirect if research start date is present for a research business activity" +
      "but validateHasInvestmentTradeStartedCondition API returns none and there is no seventy percent model in staorage" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      setupSeisSubmissionMocks(qualifyBusinessActivity = Some(qualifyResearchAndDevelopment), getHasInvestmentTradeStartedCondition = None, seventyPercentSpent = None)
      val model = await(TestController.getAnswers)
      model.nonEmpty shouldBe true

      await(model.get.validateSeis(mockSubmissionConnector)) shouldBe false

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }


  "Extracting all the answers from the SEIS flow should fail validation and " should {
    "return a None and redirect if HadPreviousRFIModel is 'Yes' and previous investments is None" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      setupSeisSubmissionMocks(hadPreviousRFI = Some(hadPreviousRFIModelYes), hadOtherInvestments = Some(hadOtherInvestmentsModelNo), previousSchemes = None)
      val model = await(TestController.getAnswers)
      model.nonEmpty shouldBe true

      await(model.get.validateSeis(mockSubmissionConnector)) shouldBe false

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the SEIS flow should fail validation and " should {
    "return a None and redirect when HadPreviousRFIModel is 'Yes' and previous investments is an empty list" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      setupSeisSubmissionMocks(hadPreviousRFI = Some(hadPreviousRFIModelYes), hadOtherInvestments = Some(hadOtherInvestmentsModelNo),
        previousSchemes = Some(List.empty[PreviousSchemeModel]))
      val model = await(TestController.getAnswers)
      model.nonEmpty shouldBe true

      await(model.get.validateSeis(mockSubmissionConnector)) shouldBe false

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the SEIS flow should fail validation and " should {
    "return a None and redirect if hadOtherInvestments is 'Yes' and previous investments is None" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      setupSeisSubmissionMocks(hadPreviousRFI = Some(hadPreviousRFIModelNo), hadOtherInvestments = Some(hadOtherInvestmentsModelYes), previousSchemes = None)
      val model = await(TestController.getAnswers)
      model.nonEmpty shouldBe true

      await(model.get.validateSeis(mockSubmissionConnector)) shouldBe false

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the SEIS flow should fail validation and " should {
    "return a None and redirect when hadOtherInvestments is 'Yes' and previous investments is an empty list" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      setupSeisSubmissionMocks(hadPreviousRFI = Some(hadPreviousRFIModelNo), hadOtherInvestments = Some(hadOtherInvestmentsModelYes),
        previousSchemes = Some(List.empty[PreviousSchemeModel]))
      val model = await(TestController.getAnswers)
      model.nonEmpty shouldBe true

      await(model.get.validateSeis(mockSubmissionConnector)) shouldBe false

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the SEIS flow should fail validation and " should {
    "return a None and redirect if the investor list has incomplete items" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      setupSeisSubmissionMocks(investorDetails = Some(listOfInvestorsMissingNumberOfPreviouslyIssuedSharesForSubmission))
      val model = await(TestController.getAnswers)
      model.nonEmpty shouldBe true

      await(model.get.validateSeis(mockSubmissionConnector)) shouldBe false

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Sending an Authenticated and Enrolled GET request with a session to AcknowledgementController" should {
    "return a 200, close the file upload envelope and " +
      "delete the current application when a valid submission data is submitted with the file upload flag enabled" in {
      when(mockFileUploadService.closeEnvelope(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(),
        Matchers.any())).thenReturn(Future(HttpResponse(OK)))
      when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.envelopeId))
        (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(envelopeId))
      when(mockS4lConnector.clearCache()(Matchers.any(), Matchers.any())).thenReturn(HttpResponse(NO_CONTENT))
      setupSeisSubmissionMocks()
      mockEnrolledRequest(seisSchemeTypesModel)
      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe OK
    }
  }

  "Sending an Authenticated and Enrolled GET request with a session to AcknowledgementController" should {
    "return a 500 internal server error if the submitComplianceStatement fails with an internal server error" in {
      when(mockFileUploadService.closeEnvelope(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(),
        Matchers.any())).thenReturn(Future(HttpResponse(OK)))
      when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.envelopeId))
        (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(envelopeId))
      when(mockS4lConnector.clearCache()(Matchers.any(), Matchers.any())).thenReturn(HttpResponse(NO_CONTENT))
      setupSeisSubmissionMocks()
      when(mockSubmissionConnector.submitComplianceStatement(Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any()))
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
          redirectLocation(result) shouldBe Some(config.FrontendAppConfig.feedbackUrl)
        }
      )
    }
  }

}
