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
import common.KeystoreKeys
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.BaseSpec
import models._
import models.investorDetails.InvestorDetailsModel
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class TotalAmountRaisedControllerSpec extends BaseSpec {

  val tradeDateConditionMet = Some(true)
  val tradeDateConditionNotMet = Some(false)
  val validTotalAmountRaisedModel = TotalAmountRaisedModel(12345)

  object TestController extends TotalAmountRaisedController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
    override lazy val submissionConnector = mockSubmissionConnector
  }

  def setupMocks(totalAmountRaisedModel: Option[TotalAmountRaisedModel] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[TotalAmountRaisedModel](Matchers.eq(KeystoreKeys.totalAmountRaised))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(totalAmountRaisedModel))
  }

  def setupSubmissionMocks(tradingConditionLessThanFourMonths: Option[Boolean] = None,
                           businessActivityModel: Option[QualifyBusinessActivityModel] = None,
                           tradeStartDateModel: Option[HasInvestmentTradeStartedModel] = None,
                           researchStartDateModel: Option[ResearchStartDateModel] = None,
                           investorDetailsModel: Option[Vector[InvestorDetailsModel]]): Unit = {

    when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(CacheMap("", Map())))

    when(mockS4lConnector.fetchAndGetFormData[QualifyBusinessActivityModel](Matchers.eq(KeystoreKeys.isQualifyBusinessActivity))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(businessActivityModel))

    when(mockS4lConnector.fetchAndGetFormData[HasInvestmentTradeStartedModel](Matchers.eq(KeystoreKeys.hasInvestmentTradeStarted))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(tradeStartDateModel))

    when(mockS4lConnector.fetchAndGetFormData[ResearchStartDateModel](Matchers.eq(KeystoreKeys.researchStartDate))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(researchStartDateModel))


    when(TestController.submissionConnector.validateHasInvestmentTradeStartedCondition(
      Matchers.any(), Matchers.any(), Matchers.any())
    (Matchers.any())).thenReturn(tradingConditionLessThanFourMonths)

    when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(CacheMap("", Map())))

    when(mockS4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](Matchers.eq(KeystoreKeys.investorDetails))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(investorDetailsModel))

  }

  "TotalAmountRaisedController" should {
    "use the correct keystore connector" in {
      TotalAmountRaisedController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      TotalAmountRaisedController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      TotalAmountRaisedController.enrolmentConnector shouldBe EnrolmentConnector
    }
    "use the correct application config" in {
      TotalAmountRaisedController.applicationConfig shouldBe FrontendAppConfig
    }
  }

  "Sending a GET request to TotalAmountRaisedController when authenticated and enrolled" should {
    "return a 200 when a saved model is fetched from keystore" in {
      setupMocks(Some(validTotalAmountRaisedModel))
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return an OK 200 when nothing is fetched from keystore" in {
      setupMocks(None)
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show())(
        result => status(result) shouldBe OK
      )
    }
  }

  // test api validation not passing

  "Sending a form submit to the TotalAmountRaisedController where the lifetime amount is exceeded" should {
    "redirect to expected lifetime allowance error controller" in {
      when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(true)))
      when(mockSubmissionConnector.checkAnnualLimitExceeded(Matchers.any(), Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(CacheMap("", Map())))
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(falseKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[ShareIssueDateModel](Matchers.eq(KeystoreKeys.shareIssueDate))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(shareIssuetDateModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale7YearsOneDay)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesYes)))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(),
        Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(keyStoreSavedDOI3YearsLessOneDay)))
      when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(),
        Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(previousSchemeTrueKIVectorList)))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "amount" -> "123456")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.LifetimeAllowanceExceededErrorController.show().url)
        }
      )
    }
  }


  "Sending a form submit to the TotalAmountRaisedController where the lifetime amount is not exceeded but annual check is exceeded" should {
    "redirect to expected annual limit exceeded error controller" in {
      when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockSubmissionConnector.checkAnnualLimitExceeded(Matchers.any(), Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(true)))
      when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(CacheMap("", Map())))
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(falseKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[ShareIssueDateModel](Matchers.eq(KeystoreKeys.shareIssueDate))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(shareIssuetDateModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale7YearsOneDay)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesYes)))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(),
        Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(keyStoreSavedDOI3YearsLessOneDay)))
      when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(),
        Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(previousSchemeTrueKIVectorList)))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "amount" -> "123456")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.AnnualLimitExceededErrorController.show().url)
        }
      )
    }
  }

  "Sending a form submit to the TotalAmountRaisedController where the lifetime allwance check api check does not return true/false" should {
    "return a internal server error 500 response" in {
      when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(None))
      when(mockSubmissionConnector.checkAnnualLimitExceeded(Matchers.any(), Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(CacheMap("", Map())))
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(falseKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[ShareIssueDateModel](Matchers.eq(KeystoreKeys.shareIssueDate))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(shareIssuetDateModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale7YearsOneDay)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesYes)))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(),
        Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(keyStoreSavedDOI3YearsLessOneDay)))
      when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(),
        Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(previousSchemeTrueKIVectorList)))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "amount" -> "123456")(
        result => status(result) shouldBe INTERNAL_SERVER_ERROR
      )
    }
  }

  "Sending a form submit to the TotalAmountRaisedController where the annual limit api check does not return true/false" should {
    "return a internal server error 500 response" in {
      when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockSubmissionConnector.checkAnnualLimitExceeded(Matchers.any(), Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(None))
      when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(CacheMap("", Map())))
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(falseKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[ShareIssueDateModel](Matchers.eq(KeystoreKeys.shareIssueDate))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(shareIssuetDateModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale7YearsOneDay)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesYes)))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(),
        Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(keyStoreSavedDOI3YearsLessOneDay)))
      when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(),
        Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(previousSchemeTrueKIVectorList)))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "amount" -> "123456")(
        result => status(result) shouldBe INTERNAL_SERVER_ERROR
      )
    }
  }


  // tests of continue logic - both APIs are not exceeded
  // ====================================================
  "Sending a valid form submit to the TotalAmountRaisedController for first investment when more than 7 years from " +
    "Commercial sale date when not deemed knowledge intensive and lifetime limit has not been exceeded" should {
    "redirect to new geographical market page" in {
      when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockSubmissionConnector.checkAnnualLimitExceeded(Matchers.any(), Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(CacheMap("", Map())))
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(falseKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[ShareIssueDateModel](Matchers.eq(KeystoreKeys.shareIssueDate))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(shareIssuetDateModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale7YearsOneDay)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesYes)))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(),
        Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(keyStoreSavedDOI3YearsLessOneDay)))
      when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(),
        Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(previousSchemeTrueKIVectorList)))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "amount" -> "123456")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.NewGeographicalMarketController.show().url)
        }
      )
    }
  }

  "Sending a valid form submit to the TotalAmountRaised Controlller for first investment when more than 10 years from " +
    "Commercial sale date and not exceeded lifetime limit" should {
    "redirect to new geographical market page" in {
      when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockSubmissionConnector.checkAnnualLimitExceeded(Matchers.any(), Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(trueKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[ShareIssueDateModel](Matchers.eq(KeystoreKeys.shareIssueDate))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(shareIssuetDateModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale10YearsOneDay)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesYes)))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(),
        Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(keyStoreSavedDOI3YearsLessOneDay)))
      when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(),
        Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(previousSchemeTrueKIVectorList)))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "amount" -> "123456")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.NewGeographicalMarketController.show().url)
        }
      )
    }
  }

  "Sending a valid form submit to the TotalAmountRaisedController for first investment when NOT more than 7 years from " +
    "Commercial sale date when not deemed knowledge intensive with subsidiaries and not exceeded lifetime limit" should {
    "redirect to new subsidiaries-spending-investment page" in {
      when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockSubmissionConnector.checkAnnualLimitExceeded(Matchers.any(), Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(falseKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[ShareIssueDateModel](Matchers.eq(KeystoreKeys.shareIssueDate))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(shareIssuetDateModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale7YearsLessOneDay)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesYes)))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(),
        Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(keyStoreSavedDOI3YearsLessOneDay)))
      when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(),
        Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(previousSchemeTrueKIVectorList)))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "amount" -> "123456")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.SubsidiariesSpendingInvestmentController.show().url)
        }
      )
    }
  }

  "Sending a valid form submit to the TotalAmountRaisedController for first investment when EXACTLY 7 years from " +
    "Commercial sale date when not deemed knowledge intensive with subsidiaries and not exceeded lifetime limit" should {
    "redirect to new subsidiaries-spending-investment page" in {
      when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockSubmissionConnector.checkAnnualLimitExceeded(Matchers.any(), Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(falseKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[ShareIssueDateModel](Matchers.eq(KeystoreKeys.shareIssueDate))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(shareIssuetDateModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale7Years)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesYes)))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(),
        Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(keyStoreSavedDOI3YearsLessOneDay)))
      when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(),
        Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(previousSchemeTrueKIVectorList)))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "amount" -> "123456")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.SubsidiariesSpendingInvestmentController.show().url)
        }
      )
    }
  }

  "Sending a valid form submit to the TotalAmountRaisedController for first investment when NOT more than 10 years from " +
    "Commercial sale date when it is deemed knowledge intensive with subsidiaries and not exceeded lifetime limit" should {
    "redirect to new subsidiaries-spending-investment page" in {
      when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockSubmissionConnector.checkAnnualLimitExceeded(Matchers.any(), Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(trueKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[ShareIssueDateModel](Matchers.eq(KeystoreKeys.shareIssueDate))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(shareIssuetDateModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale7YearsLessOneDay)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesYes)))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(),
        Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(keyStoreSavedDOI3Years)))
      when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(),
        Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(previousSchemeTrueKIVectorList)))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "amount" -> "123456")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.SubsidiariesSpendingInvestmentController.show().url)
        }
      )
    }
  }

  "Sending a valid form submit to the TotalAmountRaisedController for first investment when EXACTLY 10 years from " +
    "Commercial sale date when it is deemed knowledge intensive with subsidiaries and not exceeded lifetime limit" should {
    "redirect to new subsidiaries-spending-investment page" in {
      when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockSubmissionConnector.checkAnnualLimitExceeded(Matchers.any(), Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(trueKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[ShareIssueDateModel](Matchers.eq(KeystoreKeys.shareIssueDate))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(shareIssuetDateModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale7Years)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesYes)))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(),
        Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(keyStoreSavedDOI3Years)))
      when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(),
        Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(previousSchemeTrueKIVectorList)))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "amount" -> "123456")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.SubsidiariesSpendingInvestmentController.show().url)
        }
      )
    }
  }


  "Sending a valid form submit to the TotalAmountRaisedController for first investment is NOT more than 7 years from " +
    "Commercial sale date when it is NOT deemed knowledge intensive and without any subsidiaries and not exceeded lifetime limit" should {
    "redirect to new how-plan-to-use-investment page" in {
      when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockSubmissionConnector.checkAnnualLimitExceeded(Matchers.any(), Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(falseKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[ShareIssueDateModel](Matchers.eq(KeystoreKeys.shareIssueDate))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(shareIssuetDateModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale7YearsLessOneDay)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesNo)))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(),
        Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(keyStoreSavedDOI3YearsLessOneDay)))
      when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(),
        Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(previousSchemeTrueKIVectorList)))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "amount" -> "123456")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.InvestmentGrowController.show().url)
        }
      )
    }
  }

  "Sending a valid form submit to the TotalAmountRaisedController for first investment is EXACTLY 7 years from " +
    "Commercial sale date when it is NOT deemed knowledge intensive and without any subsidiaries and not exceeded lifetime limit" should {
    "redirect to new how-plan-to-use-investment page" in {
      when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockSubmissionConnector.checkAnnualLimitExceeded(Matchers.any(), Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(falseKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[ShareIssueDateModel](Matchers.eq(KeystoreKeys.shareIssueDate))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(shareIssuetDateModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale7Years)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesNo)))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(),
        Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(keyStoreSavedDOI3YearsOneDay)))
      when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(),
        Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(previousSchemeTrueKIVectorList)))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "amount" -> "123456")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.InvestmentGrowController.show().url)
        }
      )
    }
  }

  "Sending a valid form submit to the TotalAmountRaisedController for the first investment when NOT more than 10 years from " +
    "Commercial sale date and when it IS deemed knowledge intensive and without any subsidiaries and not exceeded lifetime limit" should {
    "redirect to new how-plan-to-use-investment page" in {
      when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockSubmissionConnector.checkAnnualLimitExceeded(Matchers.any(), Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(trueKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[ShareIssueDateModel](Matchers.eq(KeystoreKeys.shareIssueDate))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(shareIssuetDateModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale10YearsLessOneDay)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesNo)))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(),
        Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(keyStoreSavedDOI3Years)))
      when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(),
        Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(previousSchemeTrueKIVectorList)))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "amount" -> "123456")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.InvestmentGrowController.show().url)
        }
      )
    }
  }

  "Sending a valid form submit to the TotalAmountRaisedController for the first investment when EXACTLY 10 years from " +
    "Commercial sale date and when it IS deemed knowledge intensive and without any subsidiaries and not exceeded lifetime limit" should {
    "redirect to new how-plan-to-use-investment page" in {
      when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockSubmissionConnector.checkAnnualLimitExceeded(Matchers.any(), Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(trueKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[ShareIssueDateModel](Matchers.eq(KeystoreKeys.shareIssueDate))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(shareIssuetDateModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale10Years)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesNo)))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(),
        Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(keyStoreSavedDOI3Years)))
      when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(),
        Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(previousSchemeTrueKIVectorList)))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "amount" -> "123456")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.InvestmentGrowController.show().url)
        }
      )
    }
  }

  "Sending a valid form submit to the TotalAmountRaisedController for the first investment when no commercial sale has been made " +
    "and when it IS deemed knowledge intensive and has subsidiaries and not exceeded lifetime limit" should {
    "redirect to new subsidiaries-spending-investment page" in {
      when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockSubmissionConnector.checkAnnualLimitExceeded(Matchers.any(), Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(trueKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[ShareIssueDateModel](Matchers.eq(KeystoreKeys.shareIssueDate))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(shareIssuetDateModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSaleNo)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesYes)))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(),
        Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(keyStoreSavedDOI3Years)))
      when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(),
        Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(previousSchemeTrueKIVectorList)))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "amount" -> "123456")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.SubsidiariesSpendingInvestmentController.show().url)
        }
      )
    }
  }

  "Sending a valid form submit to the TotalAmountRaisedController for the first investment when no commercial sale has been made " +
    "and when it IS deemed knowledge intensive and does not have subsidiaries and not exceeded lifetime limit" should {
    "redirect to investment grow page" in {
      when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockSubmissionConnector.checkAnnualLimitExceeded(Matchers.any(), Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(trueKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[ShareIssueDateModel](Matchers.eq(KeystoreKeys.shareIssueDate))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(shareIssuetDateModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSaleNo)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesNo)))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any(),
        Matchers.any())).thenReturn(Future.successful(Option(keyStoreSavedDOI3Years)))
      when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(),
        Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(previousSchemeTrueKIVectorList)))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "amount" -> "123456")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.InvestmentGrowController.show().url)
        }
      )
    }
  }

  "Sending a valid form submit to the TotalAmountRaisedController for the first investment when no commercial sale has been made " +
    "and when it IS NOT deemed knowledge intensive and does not have subsidiaries and not exceeded lifetime limit" should {
    "redirect to investment grow page" in {
      when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockSubmissionConnector.checkAnnualLimitExceeded(Matchers.any(), Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(falseKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[ShareIssueDateModel](Matchers.eq(KeystoreKeys.shareIssueDate))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(shareIssuetDateModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSaleNo)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesNo)))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(),
        Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(keyStoreSavedDOI3YearsLessOneDay)))
      when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(),
        Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(previousSchemeTrueKIVectorList)))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "amount" -> "123456")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.InvestmentGrowController.show().url)
        }
      )
    }
  }

  "Sending a valid form submit to the TotalAmountRaisedController for the first investment when no commercial sale has been made " +
    "and when it IS NOT deemed knowledge intensive and has subsidiaries and not exceeded lifetime limit" should {
    "redirect to new subsidiaries-spending-investment page" in {
      when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockSubmissionConnector.checkAnnualLimitExceeded(Matchers.any(), Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(falseKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[ShareIssueDateModel](Matchers.eq(KeystoreKeys.shareIssueDate))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(shareIssuetDateModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSaleNo)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesYes)))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any(),
        Matchers.any())).thenReturn(Future.successful(Option(keyStoreSavedDOI3YearsLessOneDay)))
      when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(),
        Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(previousSchemeTrueKIVectorList)))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "amount" -> "123456")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.SubsidiariesSpendingInvestmentController.show().url)
        }
      )
    }
  }

  "Sending a valid form submit to the TotalAmountRaisedController when investment has been used previously and a commercial sale exists" +
    "and has a date that isn't within the range and not exceeded lifetime limit" should {
    "redirect to subsidiaries page" in {
      when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockSubmissionConnector.checkAnnualLimitExceeded(Matchers.any(), Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(trueKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[ShareIssueDateModel](Matchers.eq(KeystoreKeys.shareIssueDate))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(shareIssuetDateModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFIYes)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale1Year)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any(),
        Matchers.any())).thenReturn(Future.successful(Option(keyStoreSavedDOI3Years)))
      when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(),
        Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(previousSchemeTrueKIVectorList)))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "amount" -> "123456")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.SubsidiariesController.show().url)
        }
      )
    }
  }

  "Sending a valid form submit to the TotalAmountRaisedController when investment has been used previously and a commercial sale exists " +
    "and not exceeded lifetime limit" should {
    "redirect to reason used before page" in {
      when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockSubmissionConnector.checkAnnualLimitExceeded(Matchers.any(), Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(falseKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[ShareIssueDateModel](Matchers.eq(KeystoreKeys.shareIssueDate))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(shareIssuetDateModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFIYes)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale7YearsOneDay)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesYes)))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any(),
        Matchers.any())).thenReturn(Future.successful(Option(keyStoreSavedDOI3YearsLessOneDay)))
      when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(),
        Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(previousSchemeTrueKIVectorList)))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "amount" -> "123456")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.UsedInvestmentReasonBeforeController.show().url)
        }
      )
    }
  }

  "Sending a valid form submit to the TotalAmountRaisedController when investment has been used previously and a commercial sale exists" +
    "but using different models and not exceeded lifetime limit" should {
    "redirect to reason used before page" in {
      when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockSubmissionConnector.checkAnnualLimitExceeded(Matchers.any(), Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(falseKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[ShareIssueDateModel](Matchers.eq(KeystoreKeys.shareIssueDate))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(shareIssuetDateModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFIYes)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale10YearsOneDay)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesYes)))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(),
        Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(keyStoreSavedDOI3Years)))
      when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(),
        Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(previousSchemeTrueKIVectorList)))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "amount" -> "123456")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.UsedInvestmentReasonBeforeController.show().url)
        }
      )
    }
  }

  "Sending a valid form submit to the TotalAmountRaisedController for first investment with empty PrevRFI and not exceeded lifetime limit" should {
    "redirect to used-investment-scheme-before page" in {
      when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockSubmissionConnector.checkAnnualLimitExceeded(Matchers.any(), Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(),
        Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(trueKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(),
        Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
      when(mockS4lConnector.fetchAndGetFormData[ShareIssueDateModel](Matchers.eq(KeystoreKeys.shareIssueDate))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(shareIssuetDateModel)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(),
        Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(keyStoreSavedCommercialSale7Years)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(),
        Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(),
        Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
      when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(),
        Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(previousSchemeTrueKIVectorList)))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "amount" -> "123456")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.HadPreviousRFIController.show().url)
        }
      )
    }
  }

  "Sending a valid form submit to the TotalAmountRaisedController for the first investment with an empty Commercial sale " +
    "and not exceeded lifetime limit" should {
    "redirect to commercial-sale page" in {
      when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockSubmissionConnector.checkAnnualLimitExceeded(Matchers.any(), Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(trueKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[ShareIssueDateModel](Matchers.eq(KeystoreKeys.shareIssueDate))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(shareIssuetDateModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFIYes)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(),
        Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
      when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(),
        Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(previousSchemeTrueKIVectorList)))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "amount" -> "123456")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.CommercialSaleController.show().url)
        }
      )
    }
  }

  "Sending a valid form submit to the TotalAmountRaisedController for the first investment with an empty KIProcessingModel " +
    "and not exceeded lifetime limit" should {
    "redirect to date-of-incorporation page" in {
      when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockSubmissionConnector.checkAnnualLimitExceeded(Matchers.any(), Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockS4lConnector.fetchAndGetFormData[ShareIssueDateModel](Matchers.eq(KeystoreKeys.shareIssueDate))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(shareIssuetDateModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFIYes)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale7Years)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesYes)))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(),
        Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(keyStoreSavedDOI3Years)))
      when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(),
        Matchers.any(), Matchers.any()))        .thenReturn(Future.successful(Option(previousSchemeTrueKIVectorList)))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "amount" -> "123456")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.DateOfIncorporationController.show().url)
        }
      )
    }
  }

}
