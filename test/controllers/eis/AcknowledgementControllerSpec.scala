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

import auth.AuthEnrolledTestController.{INTERNAL_SERVER_ERROR => _, NO_CONTENT => _, OK => _, SEE_OTHER => _, _}
import auth._
import common.KeystoreKeys
import config.FrontendAuthConnector
import connectors.{EnrolmentConnector, S4LConnector, SubmissionConnector}
import controllers.helpers.BaseSpec
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import services.FileUploadService
import uk.gov.hmrc.play.http.HttpResponse
import fixtures.ModelSubmissionFixture
import models.repayments.SharesRepaymentDetailsModel
import models.submission.SchemeTypesModel

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

  "Extracting all the answers from the EIS flow" should {
    "return a valid model if all required data is found and calling validate should pass" in {
      setupEisSubmissionMocks()
      val model = await(TestController.getAnswers.get)
      model shouldBe EISAnswersModel
      await(model.validateEis(mockSubmissionConnector, mockS4lConnector)) shouldBe true
    }
  }

  "Extracting all the answers from the EIS flow" should {
    "return an error if any of the calls to save for later fail" in {
      setupEisSubmissionMocks()
      when(mockS4lConnector.fetchAndGetFormData[NatureOfBusinessModel](Matchers.eq(KeystoreKeys.natureOfBusiness))(Matchers.any(),
        Matchers.any(), Matchers.any())).thenReturn(Future.failed(new Exception("test error")))
      when(mockS4lConnector.fetchAndGetFormData[SchemeTypesModel](Matchers.eq(KeystoreKeys.selectedSchemes))
        (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(schemeTypesEIS)))
      intercept[Exception](await(TestController.getAnswers)).getMessage shouldBe "test error"
    }
  }

  "Extracting all the answers from the EIS flow" should {
    "return a None if any of the mandatory data is missing (nature of business)" in {
      setupEisSubmissionMocks(natureOfBusiness = None)
      await(TestController.getAnswers) shouldBe None

      mockEnrolledRequest(eisSchemeTypesModel)
      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the EIS flow" should {
    "return a None if any of the mandatory data is missing (date of incorporation)" in {
      setupEisSubmissionMocks(dateOfIncorporation = None)
      await(TestController.getAnswers) shouldBe None

      mockEnrolledRequest(eisSchemeTypesModel)
      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the EIS flow" should {
    "return a None if any of the mandatory data is missing (qualifying business activity)" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupEisSubmissionMocks(qualifyBusinessActivity = None)
      await(TestController.getAnswers) shouldBe None

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the EIS flow" should {
    "return a None if any of the mandatory data is missing (share issue date)" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupEisSubmissionMocks(shareIssueDate = None)
      await(TestController.getAnswers) shouldBe None

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the EIS flow" should {
    "return a None if any of the mandatory data is missing in storage (gross assets)" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupEisSubmissionMocks(grossAssets = None)
      await(TestController.getAnswers) shouldBe None

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }


  "Extracting all the answers from the EIS flow" should {
    "return a None if any of the mandatory data is missing in storage(full time employee count)" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupEisSubmissionMocks(fullTimeEmploymentCount = None)
      await(TestController.getAnswers) shouldBe None

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the EIS flow" should {
    "return a model if gross assets after is not present in storage but fail validation" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupEisSubmissionMocks(grossAssetsAfterIssue = None)
      val model = await(TestController.getAnswers)
      model.nonEmpty shouldBe true

      await(model.get.validateEis(mockSubmissionConnector, mockS4lConnector)) shouldBe false

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the EIS flow" should {
    "return a model if commercial sale is not present in storage but fail validation" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupEisSubmissionMocks(commercialSale = None)
      val model = await(TestController.getAnswers)
      model.nonEmpty shouldBe true

      await(model.get.validateEis(mockSubmissionConnector, mockS4lConnector)) shouldBe false

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the EIS flow" should {
    "return a model if both trade date and research date are not present in storage but fail validation" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupEisSubmissionMocks(hasInvestmentTradeStarted = None, researchStartDate = None)
      val model = await(TestController.getAnswers)
      model.nonEmpty shouldBe true

      await(model.get.validateEis(mockSubmissionConnector, mockS4lConnector)) shouldBe false

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the EIS flow" should {
    "return a None if any of the mandatory data is missing in storage(HadPreviousRFIModel)" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupEisSubmissionMocks(hadPreviousRFI = None)
      await(TestController.getAnswers) shouldBe None

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the EIS flow" should {
    "return a None if any of the mandatory data is missing in storage(HadOtherInvestmentsModel)" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupEisSubmissionMocks(hadOtherInvestments = None)
      await(TestController.getAnswers) shouldBe None

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the EIS flow" should {
    "return a None if any of the mandatory data is missing in storage(ShareDescriptionModel)" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupEisSubmissionMocks(shareDescription = None)
      await(TestController.getAnswers) shouldBe None

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the EIS flow" should {
    "return a None if any of the mandatory data is missing in storage(NumberOfSharesModel)" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupEisSubmissionMocks(numberOfShares = None)
      await(TestController.getAnswers) shouldBe None

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the EIS flow" should {
    "return a None if any of the mandatory data is missing in storage(TotalAmountRaisedModel)" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupEisSubmissionMocks(totalRaised = None)
      await(TestController.getAnswers) shouldBe None

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the EIS flow" should {
    "return a None if any of the mandatory data is missing in storage(InvestorDetailsModel)" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupEisSubmissionMocks(investorDetails = None)
      await(TestController.getAnswers) shouldBe None

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the EIS flow" should {
    "return a None if any of the mandatory data is missing in storage(WasAnyValueReceivedModel)" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupEisSubmissionMocks(wasAnyValueReceived = None)
      await(TestController.getAnswers) shouldBe None

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the EIS flow" should {
    "return a None if any of the mandatory data is missing in storage(ShareCapitalChangesModel)" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupEisSubmissionMocks(shareCapitalChanges = None)
      await(TestController.getAnswers) shouldBe None

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the EIS flow" should {
    "return a None if any of the mandatory data is missing in storage(ContactDetailsModel)" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupEisSubmissionMocks(contactDetails = None)
      await(TestController.getAnswers) shouldBe None

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the EIS flow" should {
    "return a None if any of the mandatory data is missing in storage(AddressModel)" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupEisSubmissionMocks(contactAddress = None)
      await(TestController.getAnswers) shouldBe None

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the EIS flow" should {
    "return a None and redirect if investment start date is present but validateHasInvestmentTradeStartedCondition API fails validation" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupEisSubmissionMocks(getHasInvestmentTradeStartedCondition = Some(false))
      val model = await(TestController.getAnswers)
      model.nonEmpty shouldBe true

      await(model.get.validateEis(mockSubmissionConnector, mockS4lConnector)) shouldBe false

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the EIS flow" should {
    "return a None and redirect if investment start date is present but validateHasInvestmentTradeStartedCondition API result is missing in storage" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupEisSubmissionMocks(getHasInvestmentTradeStartedCondition = None)
      val model = await(TestController.getAnswers)
      model.nonEmpty shouldBe true

      await(model.get.validateEis(mockSubmissionConnector, mockS4lConnector)) shouldBe false

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the EIS flow" should {
    "return a None and redirect if research start date is present for a research business activity" +
      "but validateHasInvestmentTradeStartedCondition API fails validation" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupEisSubmissionMocks(qualifyBusinessActivity = Some(qualifyResearchAndDevelopment), getHasInvestmentTradeStartedCondition = Some(false))
      val model = await(TestController.getAnswers)
      model.nonEmpty shouldBe true

      await(model.get.validateEis(mockSubmissionConnector, mockS4lConnector)) shouldBe false

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the EIS flow" should {
    "return a None and redirect if research start date is present for a research business activity" +
      "but validateHasInvestmentTradeStartedCondition API is missing in storage" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupEisSubmissionMocks(qualifyBusinessActivity = Some(qualifyResearchAndDevelopment), getHasInvestmentTradeStartedCondition = None)
      val model = await(TestController.getAnswers)
      model.nonEmpty shouldBe true

      await(model.get.validateEis(mockSubmissionConnector, mockS4lConnector)) shouldBe false

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the EIS flow should fail validation and " should {
    "return a None and redirect if HadPreviousRFIModel is 'Yes' and previous investments is None" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupEisSubmissionMocks(hadPreviousRFI = Some(hadPreviousRFIModelYes), hadOtherInvestments = Some(hadOtherInvestmentsModelNo), previousSchemes = None)
      val model = await(TestController.getAnswers)
      model.nonEmpty shouldBe true

      await(model.get.validateEis(mockSubmissionConnector, mockS4lConnector)) shouldBe false

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the EIS flow should fail validation and " should {
    "return a None and redirect when HadPreviousRFIModel is 'Yes' and previous investments is an empty list" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupEisSubmissionMocks(hadPreviousRFI = Some(hadPreviousRFIModelYes), hadOtherInvestments = Some(hadOtherInvestmentsModelNo),
        previousSchemes = Some(List.empty[PreviousSchemeModel]))
      val model = await(TestController.getAnswers)
      model.nonEmpty shouldBe true

      await(model.get.validateEis(mockSubmissionConnector, mockS4lConnector)) shouldBe false

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the EIS flow should fail validation and " should {
    "return a None and redirect if hadOtherInvestments is 'Yes' and previous investments is None" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupEisSubmissionMocks(hadPreviousRFI = Some(hadPreviousRFIModelNo), hadOtherInvestments = Some(hadOtherInvestmentsModelYes), previousSchemes = None)
      val model = await(TestController.getAnswers)
      model.nonEmpty shouldBe true

      await(model.get.validateEis(mockSubmissionConnector, mockS4lConnector)) shouldBe false

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the EIS flow should fail validation and " should {
    "return a None and redirect when hadOtherInvestments is 'Yes' and previous investments is an empty list" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupEisSubmissionMocks(hadPreviousRFI = Some(hadPreviousRFIModelNo), hadOtherInvestments = Some(hadOtherInvestmentsModelYes),
        previousSchemes = Some(List.empty[PreviousSchemeModel]))
      val model = await(TestController.getAnswers)
      model.nonEmpty shouldBe true

      await(model.get.validateEis(mockSubmissionConnector, mockS4lConnector)) shouldBe false

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the EIS flow should fail validation and " should {
    "return a None and redirect if anySharesRepaymentModel is 'Yes' and sharesRepaymentDetailsModel list is None" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupEisSubmissionMocks(anySharesRepayment = Some(anySharesRepaymentModelYes), sharesRepaymentDetails = None)
      val model = await(TestController.getAnswers)
      model.nonEmpty shouldBe true

      await(model.get.validateEis(mockSubmissionConnector, mockS4lConnector)) shouldBe false

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the EIS flow should fail validation and " should {
    "return a None and redirect if anySharesRepaymentModel is 'Yes' and sharesRepaymentDetailsModel list is any empty list" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupEisSubmissionMocks(anySharesRepayment = Some(anySharesRepaymentModelYes), sharesRepaymentDetails = Some(List.empty[SharesRepaymentDetailsModel]))
      val model = await(TestController.getAnswers)
      model.nonEmpty shouldBe true

      await(model.get.validateEis(mockSubmissionConnector, mockS4lConnector)) shouldBe false

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the EIS flow should fail validation and " should {
    "return a None and redirect if anySharesRepaymentModel is 'Yes' and sharesRepaymentDetailsModel list is a list with incomplete items" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupEisSubmissionMocks(anySharesRepayment = Some(anySharesRepaymentModelYes), sharesRepaymentDetails = Some(incompleteSharesRepaymentDetailsVector.toList))
      val model = await(TestController.getAnswers)
      model.nonEmpty shouldBe true

      await(model.get.validateEis(mockSubmissionConnector, mockS4lConnector)) shouldBe false

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the EIS flow should fail validation and " should {
    "return a None and redirect if the investor list has incomplete items" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupEisSubmissionMocks(investorDetails = Some(listOfInvestorsMissingNumberOfPreviouslyIssuedSharesForSubmission))
      val model = await(TestController.getAnswers)
      model.nonEmpty shouldBe true

      await(model.get.validateEis(mockSubmissionConnector, mockS4lConnector)) shouldBe false

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the EIS flow should fail validation and " should {
    "return a None and redirect if the Ki Processing model has no date condition met" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupEisSubmissionMocks(kiProcessingModel = Some(KiProcessingModel(dateConditionMet = None)))
      val model = await(TestController.getAnswers)
      model.nonEmpty shouldBe true

      await(model.get.validateEis(mockSubmissionConnector, mockS4lConnector)) shouldBe false

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the EIS flow should fail validation and " should {
    "return a None and redirect if the Ki Processing model has date condition met, applyKi = true and assetsKi = true " +
      "but costs condition met is None" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupEisSubmissionMocks(kiProcessingModel = Some(KiProcessingModel(dateConditionMet = Some(true), companyAssertsIsKi = Some(true),
        companyWishesToApplyKi = Some(true), costsConditionMet = None, secondaryCondtionsMet = Some(true))))
      val model = await(TestController.getAnswers)
      model.nonEmpty shouldBe true

      await(model.get.validateEis(mockSubmissionConnector, mockS4lConnector)) shouldBe false

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the EIS flow should fail validation and " should {
    "return a None and redirect if the Ki Processing model has date condition met, applyKi = true and assetsKi = true " +
      "but seconday condition met is None" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupEisSubmissionMocks(kiProcessingModel = Some(KiProcessingModel(dateConditionMet = Some(true), companyAssertsIsKi = Some(true), companyWishesToApplyKi = Some(true),
        costsConditionMet = Some(true), secondaryCondtionsMet = None)))
      val model = await(TestController.getAnswers)
      model.nonEmpty shouldBe true

      await(model.get.validateEis(mockSubmissionConnector, mockS4lConnector)) shouldBe false

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the EIS flow should fail validation and " should {
    "return a None and redirect if newGeographicMarket is 'yes' but turnover costs are empty" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupEisSubmissionMocks(newGeographicalMarket = Some(newGeographicalMarketModelYes), newProduct = Some(newProductMarketModelNo), turnoverCosts = None)
      val model = await(TestController.getAnswers)
      model.nonEmpty shouldBe true

      await(model.get.validateEis(mockSubmissionConnector, mockS4lConnector)) shouldBe false

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the EIS flow should fail validation and " should {
    "return a None and redirect if newProduct is 'yes' but turnover costs are empty" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupEisSubmissionMocks(newGeographicalMarket = Some(newGeographicalMarketModelNo), newProduct = Some(newProductMarketModelYes), turnoverCosts = None)
      val model = await(TestController.getAnswers)
      model.nonEmpty shouldBe true

      await(model.get.validateEis(mockSubmissionConnector, mockS4lConnector)) shouldBe false

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the EIS flow should fail validation and " should {
    "return a None and redirect if newProduct is 'yes', turnover costs exist but 30 day rule is empty if when the API check failed validation" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupEisSubmissionMocks(newGeographicalMarket = Some(newGeographicalMarketModelNo), newProduct = Some(newProductMarketModelYes),
        turnoverCosts = Some(turnoverCostsValid), turnoverAPiCheckPassed = Some(turnoverCheckPassedFalse), thirtyDayRule = None)
      val model = await(TestController.getAnswers)
      model.nonEmpty shouldBe true

      await(model.get.validateEis(mockSubmissionConnector, mockS4lConnector)) shouldBe false

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the EIS flow should fail validation and " should {
    "return a None and redirect if newGeographicMarket is 'yes', turnover costs exist but 30 day rule is empty if when the API check failed validation" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupEisSubmissionMocks(newGeographicalMarket = Some(newGeographicalMarketModelYes), newProduct = Some(newProductMarketModelNo),
        turnoverCosts = Some(turnoverCostsValid), turnoverAPiCheckPassed = Some(turnoverCheckPassedFalse), thirtyDayRule = None)
      val model = await(TestController.getAnswers)
      model.nonEmpty shouldBe true

      await(model.get.validateEis(mockSubmissionConnector, mockS4lConnector)) shouldBe false

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the EIS flow should fail validation when MarketRoutingCheckResult returns {true. true} requiring extra validation " should {
    "and UsedInvestmentReasonBeforeModel is missing. (hadPreviousRFI = Yes condition)" +
      "(i.e. For a subsequent investment (used previously) and a commercial sale exists and is not Ki and has subsidiaries.)" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupEisSubmissionMocks(kiProcessingModel = Some(falseKIModel), hadPreviousRFI = Some(keyStoreSavedHadPreviousRFIYes),
        hadOtherInvestments = Some(hadOtherInvestmentsModelNo), commercialSale = Some(keyStoreSavedCommercialSale7YearsOneDay),
        subsidiaries = Some(keyStoreSavedSubsidiariesYes),
        dateOfIncorporation = Some(keyStoreSavedDOI3YearsLessOneDay), previousSchemes = Some(previousSchemesList),
        previousBeforeDOFCS = Some(previousBeforeDOFCSModelYes), usedInvestmentReasonBefore = None)
      val model = await(TestController.getAnswers)
      model.nonEmpty shouldBe true

      //ensure this produces a marketInfo result requiring extra validation
      model.get.marketInfo.get.isMarketRouteApplicable.reasonBeforeValidationRequired shouldBe true
      model.get.marketInfo.get.isMarketRouteApplicable.isMarketInfoRoute shouldBe true

      // and model validation fails because extra validation fails because usedInvestmentReasonBefore is None"
      await(model.get.validateEis(mockSubmissionConnector, mockS4lConnector)) shouldBe false

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the EIS flow should fail validation when MarketRoutingCheckResult returns {true. true} requiring extra validation " should {
    "and UsedInvestmentReasonBeforeModel is missing. (hadOtherInvestments = Yes condition)" +
      "(i.e. For a subsequent investment (used previously) and a commercial sale exists and is not Ki and has subsidiaries.)" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupEisSubmissionMocks(kiProcessingModel = Some(falseKIModel), hadPreviousRFI = Some(keyStoreSavedHadPreviousRFINo),
        hadOtherInvestments = Some(hadOtherInvestmentsModelYes), commercialSale = Some(keyStoreSavedCommercialSale7YearsOneDay),
        subsidiaries = Some(keyStoreSavedSubsidiariesYes),
        dateOfIncorporation = Some(keyStoreSavedDOI3YearsLessOneDay), previousSchemes = Some(previousSchemesList), usedInvestmentReasonBefore = None,
        previousBeforeDOFCS = Some(previousBeforeDOFCSModelYes))
      val model = await(TestController.getAnswers)
      model.nonEmpty shouldBe true

      //ensure this produces a marketInfo result requiring extra validation
      model.get.marketInfo.get.isMarketRouteApplicable.reasonBeforeValidationRequired shouldBe true
      model.get.marketInfo.get.isMarketRouteApplicable.isMarketInfoRoute shouldBe true

      // and model validation fails because extra validation fails because usedInvestmentReasonBefore is None"
      await(model.get.validateEis(mockSubmissionConnector, mockS4lConnector)) shouldBe false

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the EIS flow should fail validation when MarketRoutingCheckResult returns {true. true} requiring extra validation " should {
    "and PreviousBeforeDOFCSModel is missing. (hadPreviousRFI = Yes condition)" +
      "(i.e. For a subsequent investment (used previously) and a commercial sale exists and is not Ki and has subsidiaries.)" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupEisSubmissionMocks(kiProcessingModel = Some(falseKIModel), hadPreviousRFI = Some(keyStoreSavedHadPreviousRFIYes),
        hadOtherInvestments = Some(hadOtherInvestmentsModelNo), commercialSale = Some(keyStoreSavedCommercialSale7YearsOneDay),
        subsidiaries = Some(keyStoreSavedSubsidiariesYes), dateOfIncorporation = Some(keyStoreSavedDOI3YearsLessOneDay), previousSchemes = Some(previousSchemesList),
        previousBeforeDOFCS = None, usedInvestmentReasonBefore = Some(usedInvestmentReasonBeforeModelYes))
      val model = await(TestController.getAnswers)
      model.nonEmpty shouldBe true

      //ensure this produces a marketInfo result requiring extra validation
      model.get.marketInfo.get.isMarketRouteApplicable.reasonBeforeValidationRequired shouldBe true
      model.get.marketInfo.get.isMarketRouteApplicable.isMarketInfoRoute shouldBe true

      // and model validation fails because extra validation fails because PreviousBeforeDOFCSModel is None"
      await(model.get.validateEis(mockSubmissionConnector, mockS4lConnector)) shouldBe false

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the EIS flow should fail validation when MarketRoutingCheckResult returns {true. true} requiring extra validation " should {
    "and PreviousBeforeDOFCSModel is missing. (hadOtherInvestments = Yes condition)" +
      "(i.e. For a subsequent investment (used previously) and a commercial sale exists and is not Ki and has subsidiaries.)" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupEisSubmissionMocks(kiProcessingModel = Some(falseKIModel), hadPreviousRFI = Some(keyStoreSavedHadPreviousRFINo),
        hadOtherInvestments = Some(hadOtherInvestmentsModelYes), commercialSale = Some(keyStoreSavedCommercialSale7YearsOneDay),
        subsidiaries = Some(keyStoreSavedSubsidiariesYes),
        dateOfIncorporation = Some(keyStoreSavedDOI3YearsLessOneDay), previousSchemes = Some(previousSchemesList),
        previousBeforeDOFCS = None, usedInvestmentReasonBefore = Some(usedInvestmentReasonBeforeModelYes))

      val model = await(TestController.getAnswers)
      model.nonEmpty shouldBe true

      //ensure this produces a marketInfo result requiring extra validation
      model.get.marketInfo.get.isMarketRouteApplicable.reasonBeforeValidationRequired shouldBe true
      model.get.marketInfo.get.isMarketRouteApplicable.isMarketInfoRoute shouldBe true

      // and model validation fails because extra validation fails because PreviousBeforeDOFCSModel is None"
      await(model.get.validateEis(mockSubmissionConnector, mockS4lConnector)) shouldBe false

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the EIS flow should pass validation when MarketRoutingCheckResult returns {true. true} requiring extra validation " should {
    "and PreviousBeforeDOFCSModel and usedInvestmentReasonBeforeModelYes are both present. (hadOtherInvestments = Yes condition)" +
      "(i.e. For a subsequent investment (used previously) and a commercial sale exists and is not Ki and has subsidiaries.)" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupEisSubmissionMocks(kiProcessingModel = Some(falseKIModel), hadPreviousRFI = Some(keyStoreSavedHadPreviousRFINo),
        hadOtherInvestments = Some(hadOtherInvestmentsModelYes), commercialSale = Some(keyStoreSavedCommercialSale7YearsOneDay),
        subsidiaries = Some(keyStoreSavedSubsidiariesYes),
        dateOfIncorporation = Some(keyStoreSavedDOI3YearsLessOneDay), previousSchemes = Some(previousSchemesList),
        previousBeforeDOFCS = Some(previousBeforeDOFCSModelNo), usedInvestmentReasonBefore = Some(usedInvestmentReasonBeforeModelYes))

      val model = await(TestController.getAnswers)
      model.nonEmpty shouldBe true

      //ensure this produces a marketInfo result requiring extra validation
      model.get.marketInfo.get.isMarketRouteApplicable.reasonBeforeValidationRequired shouldBe true
      model.get.marketInfo.get.isMarketRouteApplicable.isMarketInfoRoute shouldBe true

      // and model validation passes because extra validation passes because PreviousBeforeDOFCSModel and UsedInvestmentReasonBeforeModel are both present
      await(model.get.validateEis(mockSubmissionConnector, mockS4lConnector)) shouldBe true

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe OK
    }
  }

  "Extracting all the answers from the EIS flow should pass validation when MarketRoutingCheckResult returns {true. true} requiring extra validation " should {
    "and PreviousBeforeDOFCSModel and usedInvestmentReasonBeforeModelYes are both present. (hadPreviousRFI = Yes condition)" +
      "(i.e. For a subsequent investment (used previously) and a commercial sale exists and is not Ki and has subsidiaries.)" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupEisSubmissionMocks(kiProcessingModel = Some(falseKIModel), hadPreviousRFI = Some(keyStoreSavedHadPreviousRFIYes),
        hadOtherInvestments = Some(hadOtherInvestmentsModelNo), commercialSale = Some(keyStoreSavedCommercialSale7YearsOneDay),
        subsidiaries = Some(keyStoreSavedSubsidiariesYes),
        dateOfIncorporation = Some(keyStoreSavedDOI3YearsLessOneDay), previousSchemes = Some(previousSchemesList),
        previousBeforeDOFCS = Some(previousBeforeDOFCSModelNo), usedInvestmentReasonBefore = Some(usedInvestmentReasonBeforeModelYes))

      val model = await(TestController.getAnswers)
      model.nonEmpty shouldBe true

      //ensure this produces a marketInfo result requiring extra validation
      model.get.marketInfo.get.isMarketRouteApplicable.reasonBeforeValidationRequired shouldBe true
      model.get.marketInfo.get.isMarketRouteApplicable.isMarketInfoRoute shouldBe true

      // and model validation passes because extra validation passes because PreviousBeforeDOFCSModel and UsedInvestmentReasonBeforeModel are both present
      await(model.get.validateEis(mockSubmissionConnector, mockS4lConnector)) shouldBe true

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe OK

    }
  }

  "Extracting all the answers from the EIS flow should fail validation when MarketRoutingCheckResult returns {true. true} requiring extra validation " should {
    "and UsedInvestmentReasonBeforeModel is missing. (hadPreviousRFI = Yes condition)" +
      "(i.e. For a subsequent investment (used previously) and a commercial sale exists when Ki with subsidiaries.)" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupEisSubmissionMocks(kiProcessingModel = Some(trueKIModel), hadPreviousRFI = Some(keyStoreSavedHadPreviousRFIYes),
        hadOtherInvestments = Some(hadOtherInvestmentsModelNo), commercialSale = Some(keyStoreSavedCommercialSale10YearsOneDay),
        subsidiaries = Some(keyStoreSavedSubsidiariesYes),
        dateOfIncorporation = Some(keyStoreSavedDOI3YearsLessOneDay), previousSchemes = Some(previousSchemesList),
        previousBeforeDOFCS = Some(previousBeforeDOFCSModelYes), usedInvestmentReasonBefore = None)
      val model = await(TestController.getAnswers)
      model.nonEmpty shouldBe true

      //ensure this produces a marketInfo result requiring extra validation
      model.get.marketInfo.get.isMarketRouteApplicable.reasonBeforeValidationRequired shouldBe true
      model.get.marketInfo.get.isMarketRouteApplicable.isMarketInfoRoute shouldBe true

      // and model validation fails because extra validation fails because usedInvestmentReasonBefore is None"
      await(model.get.validateEis(mockSubmissionConnector, mockS4lConnector)) shouldBe false

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the EIS flow should fail validation when MarketRoutingCheckResult returns {true. true} requiring extra validation " should {
    "and UsedInvestmentReasonBeforeModel is missing. (hadOtherInvestments = Yes condition)" +
      "(i.e. For a subsequent investment (used previously) and a commercial sale exists when Ki with subsidiaries.)" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupEisSubmissionMocks(kiProcessingModel = Some(trueKIModel), hadPreviousRFI = Some(keyStoreSavedHadPreviousRFINo),
        hadOtherInvestments = Some(hadOtherInvestmentsModelYes), commercialSale = Some(keyStoreSavedCommercialSale10YearsOneDay),
        subsidiaries = Some(keyStoreSavedSubsidiariesYes),
        dateOfIncorporation = Some(keyStoreSavedDOI3YearsLessOneDay), previousSchemes = Some(previousSchemesList), usedInvestmentReasonBefore = None,
        previousBeforeDOFCS = Some(previousBeforeDOFCSModelYes))
      val model = await(TestController.getAnswers)
      model.nonEmpty shouldBe true

      //ensure this produces a marketInfo result requiring extra validation
      model.get.marketInfo.get.isMarketRouteApplicable.reasonBeforeValidationRequired shouldBe true
      model.get.marketInfo.get.isMarketRouteApplicable.isMarketInfoRoute shouldBe true

      // and model validation fails because extra validation fails because usedInvestmentReasonBefore is None"
      await(model.get.validateEis(mockSubmissionConnector, mockS4lConnector)) shouldBe false

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the EIS flow should fail validation when MarketRoutingCheckResult returns {true. true} requiring extra validation " should {
    "and PreviousBeforeDOFCSModel is missing. (hadPreviousRFI = Yes condition)" +
      "(i.e. For a subsequent investment (used previously) and a commercial sale exists when Ki with subsidiaries.)" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupEisSubmissionMocks(kiProcessingModel = Some(trueKIModel), hadPreviousRFI = Some(keyStoreSavedHadPreviousRFIYes),
        hadOtherInvestments = Some(hadOtherInvestmentsModelNo), commercialSale = Some(keyStoreSavedCommercialSale10YearsOneDay),
        subsidiaries = Some(keyStoreSavedSubsidiariesYes), dateOfIncorporation = Some(keyStoreSavedDOI3YearsLessOneDay), previousSchemes = Some(previousSchemesList),
        previousBeforeDOFCS = None, usedInvestmentReasonBefore = Some(usedInvestmentReasonBeforeModelYes))
      val model = await(TestController.getAnswers)
      model.nonEmpty shouldBe true

      //ensure this produces a marketInfo result requiring extra validation
      model.get.marketInfo.get.isMarketRouteApplicable.reasonBeforeValidationRequired shouldBe true
      model.get.marketInfo.get.isMarketRouteApplicable.isMarketInfoRoute shouldBe true

      // and model validation fails because extra validation fails because PreviousBeforeDOFCSModel is None"
      await(model.get.validateEis(mockSubmissionConnector, mockS4lConnector)) shouldBe false

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the EIS flow should fail validation when MarketRoutingCheckResult returns {true. true} requiring extra validation " should {
    "and PreviousBeforeDOFCSModel is missing. (hadOtherInvestments = Yes condition)" +
      "(i.e. For a subsequent investment (used previously) and a commercial sale exists when Ki with subsidiaries.)" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupEisSubmissionMocks(kiProcessingModel = Some(trueKIModel), hadPreviousRFI = Some(keyStoreSavedHadPreviousRFINo),
        hadOtherInvestments = Some(hadOtherInvestmentsModelYes), commercialSale = Some(keyStoreSavedCommercialSale10YearsOneDay),
        subsidiaries = Some(keyStoreSavedSubsidiariesYes),
        dateOfIncorporation = Some(keyStoreSavedDOI3YearsLessOneDay), previousSchemes = Some(previousSchemesList),
        previousBeforeDOFCS = None, usedInvestmentReasonBefore = Some(usedInvestmentReasonBeforeModelYes))

      val model = await(TestController.getAnswers)
      model.nonEmpty shouldBe true

      //ensure this produces a marketInfo result requiring extra validation
      model.get.marketInfo.get.isMarketRouteApplicable.reasonBeforeValidationRequired shouldBe true
      model.get.marketInfo.get.isMarketRouteApplicable.isMarketInfoRoute shouldBe true

      // and model validation fails because extra validation fails because PreviousBeforeDOFCSModel is None"
      await(model.get.validateEis(mockSubmissionConnector, mockS4lConnector)) shouldBe false

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.HomeController.redirectToHub().url)
    }
  }

  "Extracting all the answers from the EIS flow should pass validation when MarketRoutingCheckResult returns {true. true} requiring extra validation " should {
    "and PreviousBeforeDOFCSModel and usedInvestmentReasonBeforeModelYes are both present. (hadOtherInvestments = Yes condition)" +
      "(i.e. For a subsequent investment (used previously) and a commercial sale exists when Ki with subsidiaries)" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupEisSubmissionMocks(kiProcessingModel = Some(trueKIModel), hadPreviousRFI = Some(keyStoreSavedHadPreviousRFINo),
        hadOtherInvestments = Some(hadOtherInvestmentsModelYes), commercialSale = Some(keyStoreSavedCommercialSale10YearsOneDay),
        subsidiaries = Some(keyStoreSavedSubsidiariesYes),
        dateOfIncorporation = Some(keyStoreSavedDOI3YearsLessOneDay), previousSchemes = Some(previousSchemesList),
        previousBeforeDOFCS = Some(previousBeforeDOFCSModelNo), usedInvestmentReasonBefore = Some(usedInvestmentReasonBeforeModelYes))

      val model = await(TestController.getAnswers)
      model.nonEmpty shouldBe true

      //ensure this produces a marketInfo result requiring extra validation
      model.get.marketInfo.get.isMarketRouteApplicable.reasonBeforeValidationRequired shouldBe true
      model.get.marketInfo.get.isMarketRouteApplicable.isMarketInfoRoute shouldBe true

      // and model validation passes because extra validation passes because PreviousBeforeDOFCSModel and UsedInvestmentReasonBeforeModel are both present
      await(model.get.validateEis(mockSubmissionConnector, mockS4lConnector)) shouldBe true

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe OK
    }
  }

  "Extracting all the answers from the EIS flow should pass validation when MarketRoutingCheckResult returns {true. true} requiring extra validation " should {
    "and PreviousBeforeDOFCSModel and usedInvestmentReasonBeforeModelYes are both present. (hadPreviousRFI = Yes condition)" +
      "(i.e. For a subsequent investment (used previously) and a commercial sale exists when Ki with subsidiaries.)" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupEisSubmissionMocks(kiProcessingModel = Some(trueKIModel), hadPreviousRFI = Some(keyStoreSavedHadPreviousRFIYes),
        hadOtherInvestments = Some(hadOtherInvestmentsModelNo), commercialSale = Some(keyStoreSavedCommercialSale10YearsOneDay),
        subsidiaries = Some(keyStoreSavedSubsidiariesYes),
        dateOfIncorporation = Some(keyStoreSavedDOI3YearsLessOneDay), previousSchemes = Some(previousSchemesList),
        previousBeforeDOFCS = Some(previousBeforeDOFCSModelNo), usedInvestmentReasonBefore = Some(usedInvestmentReasonBeforeModelYes))

      val model = await(TestController.getAnswers)
      model.nonEmpty shouldBe true

      //ensure this produces a marketInfo result requiring extra validation
      model.get.marketInfo.get.isMarketRouteApplicable.reasonBeforeValidationRequired shouldBe true
      model.get.marketInfo.get.isMarketRouteApplicable.isMarketInfoRoute shouldBe true

      // and model validation passes because extra validation passes because PreviousBeforeDOFCSModel and UsedInvestmentReasonBeforeModel are both present
      await(model.get.validateEis(mockSubmissionConnector, mockS4lConnector)) shouldBe true

      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe OK

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
      setupEisSubmissionMocks()
      mockEnrolledRequest(eisSchemeTypesModel)
      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe OK
    }
  }

  "Sending an Authenticated and Enrolled GET request with a session to AcknowledgementController" should {
    "return a 500 internal server error if the submitComplianceStatement fails with an internal server error" in {
      when(mockFileUploadService.closeEnvelope(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(),
        Matchers.any())).thenReturn(Future(HttpResponse(500)))
      when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.envelopeId))
        (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(envelopeId))
      when(mockS4lConnector.clearCache()(Matchers.any(), Matchers.any())).thenReturn(HttpResponse(NO_CONTENT))
      setupEisSubmissionMocks()
      when(mockSubmissionConnector.submitComplianceStatement(Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(INTERNAL_SERVER_ERROR)))
      mockEnrolledRequest(eisSchemeTypesModel)
      val result = TestController.show.apply(authorisedFakeRequest)
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }
  }

  "Sending a POST request to the Acknowledgement controller when authenticated and enrolled" should {
    "redirect to the feedback page" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.feedback.routes.FeedbackController.show().url)
        }
      )
    }
  }


}