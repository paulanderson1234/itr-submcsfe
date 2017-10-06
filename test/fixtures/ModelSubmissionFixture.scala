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

package fixtures

import common.KeystoreKeys
import controllers.helpers.BaseSpec
import models.submission.{RepaidSharesAnswersModel, _}
import models.{PreviousBeforeDOFCSModel, SeventyPercentSpentModel, TotalAmountSpentModel, UsedInvestmentReasonBeforeModel, _}
import models.investorDetails.InvestorDetailsModel
import models.registration.RegistrationDetailsModel
import models.repayments.{AnySharesRepaymentModel, SharesRepaymentDetailsModel}
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HttpResponse

import scala.concurrent.Future

trait ModelSubmissionFixture extends BaseSpec{

  val submissionResponse = SubmissionResponse("2014-12-17", "FBUND09889765")
  val turnoverCheckPassedTrue = true
  val turnoverCheckPassedFalse = false

  //noinspection ScalaStyle
  // THIS MODEL DEFAULTS TO A VALID POPULATED EIS MODEL IF CALLLED WITH NO PARAMETERS - VALIDATE will PASS
  def setupEisSubmissionMocks(
                               grossAssets: Option[GrossAssetsModel] = Some(validGrossAssets),
                               fullTimeEmploymentCount: Option[FullTimeEmployeeCountModel] = Some(validFteCount),
                               shareIssueDate: Option[ShareIssueDateModel] = Some(shareIssuetDateModel),
                               tradeStartDate: Option[TradeStartDateModel] = Some(tradeStartDateModelYes),
                               schemeTypes: Option[SchemeTypesModel] = Some(schemeTypesEIS),
                               hadPreviousRFI: Option[HadPreviousRFIModel] = Some(hadPreviousRFIModelYes),
                               hadOtherInvestments: Option[HadOtherInvestmentsModel] = Some(hadOtherInvestmentsModelYes),
                               previousSchemes: Option[List[PreviousSchemeModel]] = Some(previousSchemesList),
                               shareDescription: Option[ShareDescriptionModel] = Some(shareDescriptionModel),
                               numberOfShares: Option[NumberOfSharesModel] = Some(numberOfSharesModel),
                               totalRaised: Option[TotalAmountRaisedModel] = Some(totalAmountRaisedSubmission),
                               investorDetails: Option[Vector[InvestorDetailsModel]] = Some(listOfInvestorsWithShareHoldingsForSubmission),
                               wasAnyValueReceived: Option[WasAnyValueReceivedModel] = Some(wasAnyValueReceivedYes),
                               shareCapitalChanges: Option[ShareCapitalChangesModel] = Some(shareCapitalChangesYes),
                               supportingDocumentsUpload: Option[SupportingDocumentsUploadModel] = Some(SupportingDocumentsUploadNo),
                               contactDetails: Option[ContactDetailsModel] = Some(contactDetailsModel),
                               confirmContactAddress: Option[ConfirmCorrespondAddressModel] = Some(confirmCorrespondAddressModel),
                               qualifyBusinessActivity: Option[QualifyBusinessActivityModel] = Some(qualifyTrade),
                               dateOfIncorporation: Option[DateOfIncorporationModel] = Some(keyStoreSavedDOI3YearsLessOneDay),
                               natureOfBusiness: Option[NatureOfBusinessModel] = Some(natureOfBusinessModel),
                               subsidiaries: Option[SubsidiariesModel] = Some(keyStoreSavedSubsidiariesNo),
                               hasInvestmentTradeStarted: Option[HasInvestmentTradeStartedModel] = Some(hasInvestmentTradeStartedModelYes),
                               researchStartDate: Option[ResearchStartDateModel] = Some(researchStartDateModelYes),
                               selectedSchemes: Option[SchemeTypesModel] = Some(schemeTypesEIS),
                               contactAddress: Option[AddressModel] = Some(fullCorrespondenceAddress),
                               kiProcessingModel: Option[KiProcessingModel] = Some(trueKIModel),
                               commercialSale: Option[CommercialSaleModel] = Some(keyStoreSavedCommercialSale10YearsOneDay),
                               tenYearPlan: Option[TenYearPlanModel] = Some(tenYearPlanModelYes),
                               newGeographicalMarket: Option[NewGeographicalMarketModel] = Some(newGeographicalMarketModelYes),
                               newProduct: Option[NewProductModel] = Some(newProductMarketModelYes),
                               marketDescription: Option[MarketDescriptionModel] = Some(marketDescriptionPopulated),
                               turnoverAPiCheckPassed: Option[Boolean] = Some(turnoverCheckPassedFalse),
                               turnoverCosts: Option[AnnualTurnoverCostsModel] = Some(turnoverCostsValid),
                               operatingCosts: Option[OperatingCostsModel] = Some(operatingCostsValid),
                               thirtyDayRule: Option[ThirtyDayRuleModel] = Some(thirtyDayRuleModelYes),
                               investmentGrow: Option[InvestmentGrowModel] = Some(investmentGrowValid),
                               subsidiariesSpendingInvestment: Option[SubsidiariesSpendingInvestmentModel] = Some(subsidiariesSpendingInvestmentModelNo),
                               subsidiariesNinetyOwned: Option[SubsidiariesNinetyOwnedModel] = Some(subsidiariesNinetyOwnedModelYes),
                               anySharesRepayment: Option[AnySharesRepaymentModel] = Some(anySharesRepaymentModelYes),
                               grossAssetsAfterIssue: Option[GrossAssetsAfterIssueModel] = Some(validGrossAssetsAfter),
                               usedInvestmentReasonBefore: Option[UsedInvestmentReasonBeforeModel] = Some(usedInvestmentReasonBeforeModelNo),
                               previousBeforeDOFCS: Option[PreviousBeforeDOFCSModel] = Some(previousBeforeDOFCSModelNo),
                               sharesRepaymentDetails: Option[List[SharesRepaymentDetailsModel]] = Some(validSharesRepaymentDetailsVector.toList),
                               getHasInvestmentTradeStartedCondition: Option[Boolean] = Some(true),
                               getRegistrationDetails: Option[RegistrationDetailsModel] = Some(registrationDetailsModel),
                               getTavcReferenceNumber: String = "XATAVC000123456") : Unit = {

    when(mockS4lConnector.fetchAndGetFormData[GrossAssetsModel](Matchers.eq(KeystoreKeys.grossAssets))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(grossAssets))
    when(mockS4lConnector.fetchAndGetFormData[FullTimeEmployeeCountModel](Matchers.eq(KeystoreKeys.fullTimeEmployeeCount))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(fullTimeEmploymentCount))
    when(mockS4lConnector.fetchAndGetFormData[ShareIssueDateModel](Matchers.eq(KeystoreKeys.shareIssueDate))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(shareIssueDate))
    when(mockS4lConnector.fetchAndGetFormData[TradeStartDateModel](Matchers.eq(KeystoreKeys.tradeStartDate))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(tradeStartDate)
    when(mockS4lConnector.fetchAndGetFormData[SchemeTypesModel](Matchers.eq(KeystoreKeys.selectedSchemes))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(schemeTypes)
    when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(hadPreviousRFI))
    when(mockS4lConnector.fetchAndGetFormData[HadOtherInvestmentsModel](Matchers.eq(KeystoreKeys.hadOtherInvestments))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(hadOtherInvestments))
    when(mockS4lConnector.fetchAndGetFormData[List[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(previousSchemes))
    when(mockS4lConnector.fetchAndGetFormData[ShareDescriptionModel](Matchers.eq(KeystoreKeys.shareDescription))
      (Matchers.any(), Matchers.any(), Matchers.any())) .thenReturn(Future.successful(shareDescription))
    when(mockS4lConnector.fetchAndGetFormData[NumberOfSharesModel](Matchers.eq(KeystoreKeys.numberOfShares))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(numberOfShares))
    when(mockS4lConnector.fetchAndGetFormData[TotalAmountRaisedModel](Matchers.eq(KeystoreKeys.totalAmountRaised))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(totalRaised))
    when(mockS4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](Matchers.eq(KeystoreKeys.investorDetails))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(investorDetails))
    when(mockS4lConnector.fetchAndGetFormData[WasAnyValueReceivedModel](Matchers.eq(KeystoreKeys.wasAnyValueReceived))(Matchers.any(),
      Matchers.any(), Matchers.any())).thenReturn(Future.successful(wasAnyValueReceived))
    when(mockS4lConnector.fetchAndGetFormData[ShareCapitalChangesModel](Matchers.eq(KeystoreKeys.shareCapitalChanges))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(shareCapitalChanges))
    when(mockS4lConnector.fetchAndGetFormData[SupportingDocumentsUploadModel](Matchers.eq(KeystoreKeys.supportingDocumentsUpload))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(supportingDocumentsUpload))
    when(mockS4lConnector.fetchAndGetFormData[ContactDetailsModel](Matchers.eq(KeystoreKeys.contactDetails))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(contactDetails))
    when(mockS4lConnector.fetchAndGetFormData[ConfirmCorrespondAddressModel](Matchers.eq(KeystoreKeys.confirmContactAddress))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(confirmContactAddress))
    when(mockS4lConnector.fetchAndGetFormData[QualifyBusinessActivityModel](Matchers.eq(KeystoreKeys.isQualifyBusinessActivity))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(qualifyBusinessActivity))
    when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(dateOfIncorporation))
    when(mockS4lConnector.fetchAndGetFormData[NatureOfBusinessModel](Matchers.eq(KeystoreKeys.natureOfBusiness))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(natureOfBusiness))
    when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(subsidiaries))
    when(mockS4lConnector.fetchAndGetFormData[ResearchStartDateModel](Matchers.eq(KeystoreKeys.researchStartDate))(Matchers.any(), Matchers.any(),
      Matchers.any())).thenReturn(Future.successful(researchStartDate))
    when(mockS4lConnector.fetchAndGetFormData[SchemeTypesModel](Matchers.eq(KeystoreKeys.selectedSchemes))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(selectedSchemes))
    when(mockS4lConnector.fetchAndGetFormData[AddressModel](Matchers.eq(KeystoreKeys.contactAddress))(Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(contactAddress))
    when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(kiProcessingModel))
    when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(commercialSale))
    when(mockS4lConnector.fetchAndGetFormData[TenYearPlanModel](Matchers.eq(KeystoreKeys.tenYearPlan))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(tenYearPlan))
    when(mockS4lConnector.fetchAndGetFormData[NewGeographicalMarketModel](Matchers.eq(KeystoreKeys.newGeographicalMarket))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(newGeographicalMarket))
    when(mockS4lConnector.fetchAndGetFormData[NewProductModel](Matchers.eq(KeystoreKeys.newProduct))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(newProduct))
    when(mockS4lConnector.fetchAndGetFormData[MarketDescriptionModel](Matchers.eq(KeystoreKeys.marketDescription))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(marketDescription))
    when(mockS4lConnector.fetchAndGetFormData[Boolean](Matchers.eq(KeystoreKeys.turnoverAPiCheckPassed))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(turnoverAPiCheckPassed))
    when(mockS4lConnector.fetchAndGetFormData[AnnualTurnoverCostsModel](Matchers.eq(KeystoreKeys.turnoverCosts))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(turnoverCosts))
    when(mockS4lConnector.fetchAndGetFormData[OperatingCostsModel](Matchers.eq(KeystoreKeys.operatingCosts))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(operatingCosts))
    when(mockS4lConnector.fetchAndGetFormData[ThirtyDayRuleModel](Matchers.eq(KeystoreKeys.thirtyDayRule))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(thirtyDayRule))
    when(mockS4lConnector.fetchAndGetFormData[InvestmentGrowModel](Matchers.eq(KeystoreKeys.investmentGrow))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(investmentGrow))
    when(mockS4lConnector.fetchAndGetFormData[SubsidiariesSpendingInvestmentModel](Matchers.eq(KeystoreKeys.subsidiariesSpendingInvestment))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(subsidiariesSpendingInvestment))
    when(mockS4lConnector.fetchAndGetFormData[SubsidiariesNinetyOwnedModel](Matchers.eq(KeystoreKeys.subsidiariesNinetyOwned))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(subsidiariesNinetyOwned))
    when(mockS4lConnector.fetchAndGetFormData[AnySharesRepaymentModel](Matchers.eq(KeystoreKeys.anySharesRepayment))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(anySharesRepayment))
    when(mockS4lConnector.fetchAndGetFormData[GrossAssetsAfterIssueModel](Matchers.eq(KeystoreKeys.grossAssetsAfterIssue))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(grossAssetsAfterIssue))
    when(mockS4lConnector.fetchAndGetFormData[HasInvestmentTradeStartedModel](Matchers.eq(KeystoreKeys.hasInvestmentTradeStarted))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(hasInvestmentTradeStarted))
    when(mockS4lConnector.fetchAndGetFormData[List[SharesRepaymentDetailsModel]](Matchers.eq(KeystoreKeys.sharesRepaymentDetails))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(sharesRepaymentDetails))
    when(mockS4lConnector.fetchAndGetFormData[UsedInvestmentReasonBeforeModel](Matchers.eq(KeystoreKeys.usedInvestmentReasonBefore))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(usedInvestmentReasonBefore))
    when(mockS4lConnector.fetchAndGetFormData[PreviousBeforeDOFCSModel](Matchers.eq(KeystoreKeys.previousBeforeDOFCS))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(previousBeforeDOFCS))
    when(mockSubmissionConnector.validateHasInvestmentTradeStartedCondition(Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(getHasInvestmentTradeStartedCondition))
    when(mockRegistrationDetailsService.getRegistrationDetails(Matchers.any())(Matchers.any(),Matchers.any(), Matchers.any())).
      thenReturn(Future.successful(getRegistrationDetails))
    when(mockEnrolmentConnector.getTavcReferenceNumber(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(getTavcReferenceNumber))
    //SEIS ONLY - JUST SET TO NONE FOR EIS
    when(mockS4lConnector.fetchAndGetFormData[TotalAmountSpentModel](Matchers.eq(KeystoreKeys.totalAmountSpent))(Matchers.any(),
      Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[SeventyPercentSpentModel](Matchers.eq(KeystoreKeys.seventyPercentSpent))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
    when(mockSubmissionConnector.submitComplianceStatement(Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(submissionResponse)))))


  }

  // this model is a constructed with the same default values that are mocked in setupEisSubmissionMocks above to compare the expected result
  val EISAnswersModel = ComplianceStatementAnswersModel(
    CompanyDetailsAnswersModel(natureOfBusinessModel, keyStoreSavedDOI3YearsLessOneDay, qualifyTrade,
      hasInvestmentTradeStartedModel = Some(hasInvestmentTradeStartedModelYes), researchStartDateModel = Some(researchStartDateModelYes), seventyPercentSpentModel = None,
      shareIssueDateModel = shareIssuetDateModel, grossAssetsModel = validGrossAssets,
      grossAssetsAfterModel = Some(validGrossAssetsAfter), fullTimeEmployeeCountModel = validFteCount, commercialSaleModel = Some(keyStoreSavedCommercialSale10YearsOneDay)),
    PreviousSchemesAnswersModel(hadPreviousRFIModelYes, hadOtherInvestmentsModelYes, Some(previousSchemesList)),
    ShareDetailsAnswersModel(shareDescriptionModel,
      numberOfSharesModel, totalAmountRaisedSubmission, totalAmountSpentModel = None),
    InvestorDetailsAnswersModel(listOfInvestorsWithShareHoldingsForSubmission,
      wasAnyValueReceivedYes, shareCapitalChangesYes),
    ContactDetailsAnswersModel(contactDetailsModel, fullCorrespondenceAddress),
    supportingDocumentsUploadModel = SupportingDocumentsUploadNo, schemeTypesEIS, kiAnswersModel = Some(KiAnswersModel(kiProcessingModel = trueKIModel,
      tenYearPlanModel = Some(tenYearPlanModelYes))),
    Some(MarketInfoAnswersModel(newGeographicalMarketModelYes,newProductMarketModelYes, Some(marketDescriptionPopulated),
      isMarketRouteApplicable = MarketRoutingCheckResult(isMarketInfoRoute = true, reasonBeforeValidationRequired = true), turnoverApiCheckPassed = Some(turnoverCheckPassedFalse))),
    costsAnswersModel =   CostsAnswerModel(Some(operatingCostsValid), Some(turnoverCostsValid)),
    thirtyDayRuleAnswersModel = Some(ThirtyDayRuleAnswersModel(thirtyDayRuleModelYes, turnoverApiCheckPassed = false)),
    investmentGrow = Some(InvestmentGrowAnswersModel(investmentGrowValid)), Some(SubsidiariesAnswersModel(subsidiariesSpendingInvestmentModelNo,subsidiariesNinetyOwnedModelYes)),
    repaidSharesAnswersModel =  Some(RepaidSharesAnswersModel(anySharesRepaymentModelYes, Some(validSharesRepaymentDetailsVector.toList))))

}
