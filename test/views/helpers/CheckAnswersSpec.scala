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

package views.helpers

import common.{Constants, KeystoreKeys}
import models._
import models.investorDetails.{InvestorDetailsModel, PreviousShareHoldingModel}
import models.repayments.{AnySharesRepaymentModel, SharesRepaymentDetailsModel}
import org.mockito.Matchers
import org.mockito.Mockito._

import scala.concurrent.Future

trait CheckAnswersSpec extends ViewSpec {


  val grossAssetsAmount = 12345
  val grossAssetsAfterIssueModel = GrossAssetsAfterIssueModel(grossAssetsAmount)
  val grossAssetsModel = GrossAssetsModel(grossAssetsAmount)
  val fullTimeEmployeeModel = FullTimeEmployeeCountModel(22)
  val shareHoldersModelForReview = Vector(PreviousShareHoldingModel(investorShareIssueDateModel = Some(investorShareIssueDateModel1),
    numberOfPreviouslyIssuedSharesModel = Some (numberOfPreviouslyIssuedSharesModel1),
    previousShareHoldingNominalValueModel = Some(previousShareHoldingNominalValueModel1),
    previousShareHoldingDescriptionModel = Some(previousShareHoldingDescriptionModel1),
    processingId = Some(1), investorProcessingId = Some(2)))

  val investorModelForReview = InvestorDetailsModel(Some(investorModel2), Some(companyOrIndividualModel2), Some(companyDetailsModel2), None,
    Some(numberOfSharesPurchasedModel2), Some(howMuchSpentOnSharesModel2), Some(isExistingShareHolderModelYes),
    previousShareHoldingModels = Some(shareHoldersModelForReview), processingId = Some(2))

  val listOfInvestorsEmptyShareHoldings =  Vector(validModelWithPrevShareHoldings.copy(previousShareHoldingModels = Some(Vector())))
  val listOfInvestorsWithShareHoldings =  Vector(investorModelForReview)
  val listOfInvestorsMissingNumberOfPreviouslyIssuedShares =  Vector(validModelWithPrevShareHoldings.copy(previousShareHoldingModels =
    Some(Vector(PreviousShareHoldingModel(previousShareHoldingDescriptionModel = Some(previousShareHoldingDescriptionModel1), processingId = Some(1))))))

  def setupEISMocks(): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[GrossAssetsAfterIssueModel](Matchers.eq(KeystoreKeys.grossAssetsAfterIssue))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(GrossAssetsAfterIssueModel(grossAssetsAmount))))
    when(mockS4lConnector.fetchAndGetFormData[AnnualTurnoverCostsModel](Matchers.eq(KeystoreKeys.turnoverCosts))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(Some(annualTurnoverCostsModel)))
    when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Some(true)))
    when(mockS4lConnector.fetchAndGetFormData[Vector[SharesRepaymentDetailsModel]](Matchers.eq(KeystoreKeys.sharesRepaymentDetails))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Some(validSharesRepaymentDetailsVector))
    when(mockS4lConnector.fetchAndGetFormData[IsCompanyKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isCompanyKnowledgeIntensive))
      (Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Some(isCompanyKnowledgeIntensiveModelYes)))
    when(mockS4lConnector.fetchAndGetFormData[MarketDescriptionModel](Matchers.eq(KeystoreKeys.marketDescription))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(MarketDescriptionModel("test"))))
    when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(Some(dateOfIncorporationModel)))
    when(mockS4lConnector.fetchAndGetFormData[NatureOfBusinessModel](Matchers.eq(KeystoreKeys.natureOfBusiness))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(Some(natureOfBusinessModel)))
    when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(),
      Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(keyStoreSavedCommercialSale7Years)))
    when(mockS4lConnector.fetchAndGetFormData[NewGeographicalMarketModel](Matchers.eq(KeystoreKeys.newGeographicalMarket))
      (Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(newGeographicalMarketValid)))
    when(mockS4lConnector.fetchAndGetFormData[NewProductModel](Matchers.eq(KeystoreKeys.newProduct))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(newProductValid)))
    when(mockS4lConnector.fetchAndGetFormData[TenYearPlanModel](Matchers.eq(KeystoreKeys.tenYearPlan))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(tenYearPlanValid)))
    when(mockS4lConnector.fetchAndGetFormData[OperatingCostsModel](Matchers.eq(KeystoreKeys.operatingCosts))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(operatingCostsValid)))
    when(mockS4lConnector.fetchAndGetFormData[PercentageStaffWithMastersModel](Matchers.eq(KeystoreKeys.percentageStaffWithMasters))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(Some(percentageStaffWithMastersModelYes)))
    when(mockS4lConnector.fetchAndGetFormData[ThirtyDayRuleModel](Matchers.eq(KeystoreKeys.thirtyDayRule))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(thirtyDayRuleModelYes)))
    when(mockS4lConnector.fetchAndGetFormData[AnySharesRepaymentModel](Matchers.eq(KeystoreKeys.anySharesRepayment))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(anySharesRepaymentModelYes)))

  }

  def setupMocks(): Unit = {
    mockEnrolledRequest(seisSchemeTypesModel)
    when(mockS4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](Matchers.eq(KeystoreKeys.investorDetails))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(listOfInvestorsWithShareHoldings)))
    when(mockS4lConnector.fetchAndGetFormData[QualifyBusinessActivityModel](Matchers.eq(KeystoreKeys.isQualifyBusinessActivity))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(Some(qualifyPrepareToTrade)))
    when(mockS4lConnector.fetchAndGetFormData[ShareDescriptionModel](Matchers.eq(KeystoreKeys.shareDescription))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(shareDescriptionModel)))
    when(mockS4lConnector.fetchAndGetFormData[HasInvestmentTradeStartedModel](Matchers.eq(KeystoreKeys.hasInvestmentTradeStarted))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(hasInvestmentTradeStartedModelYes)))
    when(mockS4lConnector.fetchAndGetFormData[GrossAssetsModel](Matchers.eq(KeystoreKeys.grossAssets))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(GrossAssetsModel(12345))))
    when(mockS4lConnector.fetchAndGetFormData[FullTimeEmployeeCountModel](Matchers.eq(KeystoreKeys.fullTimeEmployeeCount))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(FullTimeEmployeeCountModel(22))))
    when(mockS4lConnector.fetchAndGetFormData[ShareIssueDateModel](Matchers.eq(KeystoreKeys.shareIssueDate))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(shareIssuetDateModel)))
    when(mockS4lConnector.fetchAndGetFormData[NumberOfSharesModel](Matchers.eq(KeystoreKeys.numberOfShares))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(numberOfSharesModel)))
    when(mockS4lConnector.fetchAndGetFormData[TotalAmountRaisedModel](Matchers.eq(KeystoreKeys.totalAmountRaised))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(TotalAmountRaisedModel(12345))))
    when(mockS4lConnector.fetchAndGetFormData[WasAnyValueReceivedModel](Matchers.eq(KeystoreKeys.wasAnyValueReceived))(Matchers.any(),
      Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(WasAnyValueReceivedModel(Constants.StandardRadioButtonYesValue,
        Some("text")))))
    when(mockS4lConnector.fetchAndGetFormData[ShareCapitalChangesModel](Matchers.eq(KeystoreKeys.shareCapitalChanges))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(ShareCapitalChangesModel(Constants.StandardRadioButtonYesValue, Some("test")))))
    when(mockS4lConnector.fetchAndGetFormData[SupportingDocumentsUploadModel](Matchers.eq(KeystoreKeys.supportingDocumentsUpload))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(SupportingDocumentsUploadModel("No"))))
    when(mockS4lConnector.fetchAndGetFormData[SeventyPercentSpentModel](Matchers.eq(KeystoreKeys.seventyPercentSpent))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(isSeventyPercentSpentModelYes)))
    when(mockS4lConnector.fetchAndGetFormData[TotalAmountSpentModel](Matchers.eq(KeystoreKeys.totalAmountSpent))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Some(TotalAmountSpentModel(5))))
    when(mockS4lConnector.fetchAndGetFormData[ResearchStartDateModel](Matchers.eq(KeystoreKeys.researchStartDate))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(researchStartDateModelYes)))
  }

  def previousRFISetup(hadPreviousRFIModel: Option[HadPreviousRFIModel] = None,
                       previousSchemes: Option[Vector[PreviousSchemeModel]] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(hadPreviousRFIModel))
    when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(previousSchemes))
  }

  def investmentSetup(totalAmountRaisedModel: Option[TotalAmountRaisedModel] = None,
                      usedInvestmentReasonBeforeModel: Option[UsedInvestmentReasonBeforeModel] = None,
                      previousBeforeDOFCSModel: Option[PreviousBeforeDOFCSModel] = None,
                      newGeographicalMarketModel: Option[NewGeographicalMarketModel] = None, newProductModel: Option[NewProductModel] = None,
                      subsidiariesSpendingInvestmentModel: Option[SubsidiariesSpendingInvestmentModel] = None,
                      subsidiariesNinetyOwnedModel: Option[SubsidiariesNinetyOwnedModel] = None,
                      investmentGrowModel: Option[InvestmentGrowModel] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[TotalAmountRaisedModel](Matchers.eq(KeystoreKeys.totalAmountRaised))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(totalAmountRaisedModel))
    when(mockS4lConnector.fetchAndGetFormData[UsedInvestmentReasonBeforeModel](Matchers.eq(KeystoreKeys.usedInvestmentReasonBefore))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(usedInvestmentReasonBeforeModel))
    when(mockS4lConnector.fetchAndGetFormData[PreviousBeforeDOFCSModel](Matchers.eq(KeystoreKeys.previousBeforeDOFCS))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(previousBeforeDOFCSModel))
    when(mockS4lConnector.fetchAndGetFormData[NewGeographicalMarketModel](Matchers.eq(KeystoreKeys.newGeographicalMarket))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(newGeographicalMarketModel))
    when(mockS4lConnector.fetchAndGetFormData[NewProductModel](Matchers.eq(KeystoreKeys.newProduct))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(newProductModel))
    when(mockS4lConnector.fetchAndGetFormData[SubsidiariesSpendingInvestmentModel](Matchers.eq(KeystoreKeys.subsidiariesSpendingInvestment))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(subsidiariesSpendingInvestmentModel))
    when(mockS4lConnector.fetchAndGetFormData[SubsidiariesNinetyOwnedModel](Matchers.eq(KeystoreKeys.subsidiariesNinetyOwned))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(subsidiariesNinetyOwnedModel))
    when(mockS4lConnector.fetchAndGetFormData[InvestmentGrowModel](Matchers.eq(KeystoreKeys.investmentGrow))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(investmentGrowModel))
  }

  def contactDetailsSetup(contactDetailsModel: Option[ContactDetailsModel] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[ContactDetailsModel](Matchers.eq(KeystoreKeys.contactDetails))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(contactDetailsModel))
  }

  def contactAddressSetup(contactAddressModel: Option[AddressModel] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[AddressModel](Matchers.eq(KeystoreKeys.contactAddress))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(contactAddressModel))
  }

  def companyDetailsSetup(yourCompanyNeedModel: Option[YourCompanyNeedModel] = None, taxpayerReferenceModel: Option[TaxpayerReferenceModel] = None,
                          registeredAddressModel: Option[RegisteredAddressModel] = None, dateOfIncorporationModel: Option[DateOfIncorporationModel] = None,
                          natureOfBusinessModel: Option[NatureOfBusinessModel] = None, commercialSaleModel: Option[CommercialSaleModel] = None,
                          isKnowledgeIntensiveModel: Option[IsKnowledgeIntensiveModel] = None, operatingCostsModel: Option[OperatingCostsModel] = None,
                          percentageStaffWithMastersModel: Option[PercentageStaffWithMastersModel] = None, tenYearPlanModel: Option[TenYearPlanModel] = None,
                          subsidiariesModel: Option[SubsidiariesModel] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[YourCompanyNeedModel](Matchers.eq(KeystoreKeys.yourCompanyNeed))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(yourCompanyNeedModel))
    when(mockS4lConnector.fetchAndGetFormData[TaxpayerReferenceModel](Matchers.eq(KeystoreKeys.taxpayerReference))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(taxpayerReferenceModel))
    when(mockS4lConnector.fetchAndGetFormData[RegisteredAddressModel](Matchers.eq(KeystoreKeys.registeredAddress))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(registeredAddressModel))
    when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(dateOfIncorporationModel))
    when(mockS4lConnector.fetchAndGetFormData[NatureOfBusinessModel](Matchers.eq(KeystoreKeys.natureOfBusiness))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(natureOfBusinessModel))
    when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(commercialSaleModel))
    when(mockS4lConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(isKnowledgeIntensiveModel))
    when(mockS4lConnector.fetchAndGetFormData[OperatingCostsModel](Matchers.eq(KeystoreKeys.operatingCosts))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(operatingCostsModel))
    when(mockS4lConnector.fetchAndGetFormData[PercentageStaffWithMastersModel](Matchers.eq(KeystoreKeys.percentageStaffWithMasters))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(percentageStaffWithMastersModel))
    when(mockS4lConnector.fetchAndGetFormData[TenYearPlanModel](Matchers.eq(KeystoreKeys.tenYearPlan))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(tenYearPlanModel))
    when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(subsidiariesModel))
  }

  def seisInvestmentSetup(totalAmountRaisedModel: Option[TotalAmountRaisedModel] = None,
                          subsidiariesSpendingInvestmentModel: Option[SubsidiariesSpendingInvestmentModel] = None,
                          subsidiariesNinetyOwnedModel: Option[SubsidiariesNinetyOwnedModel] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[TotalAmountRaisedModel](Matchers.eq(KeystoreKeys.totalAmountSpent))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(totalAmountRaisedModel))
    when(mockS4lConnector.fetchAndGetFormData[SubsidiariesSpendingInvestmentModel](Matchers.eq(KeystoreKeys.subsidiariesSpendingInvestment))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(subsidiariesSpendingInvestmentModel))
    when(mockS4lConnector.fetchAndGetFormData[SubsidiariesNinetyOwnedModel](Matchers.eq(KeystoreKeys.subsidiariesNinetyOwned))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(subsidiariesNinetyOwnedModel))
  }

  def seisCompanyDetailsSetup(registeredAddressModel: Option[RegisteredAddressModel] = None,
                              dateOfIncorporationModel: Option[DateOfIncorporationModel] = None,
                              natureOfBusinessModel: Option[NatureOfBusinessModel] = None,
                              subsidiariesModel: Option[SubsidiariesModel] = None,
                              tradeStartDateModel: Option[TradeStartDateModel] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[RegisteredAddressModel](Matchers.eq(KeystoreKeys.registeredAddress))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(registeredAddressModel))
    when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(dateOfIncorporationModel))
    when(mockS4lConnector.fetchAndGetFormData[NatureOfBusinessModel](Matchers.eq(KeystoreKeys.natureOfBusiness))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(natureOfBusinessModel))
    when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(subsidiariesModel))
    when(mockS4lConnector.fetchAndGetFormData[TradeStartDateModel](Matchers.eq(KeystoreKeys.tradeStartDate))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(tradeStartDateModel))
  }

  def tradeStartDateSetup(tradeStartDateModel: Option[TradeStartDateModel] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[TradeStartDateModel](Matchers.eq(KeystoreKeys.tradeStartDate))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(tradeStartDateModel))
  }

  def isSeisInEligibleSetup(eisSeisProcessingModel: Option[EisSeisProcessingModel] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[EisSeisProcessingModel](Matchers.eq(KeystoreKeys.eisSeisProcessingModel))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(eisSeisProcessingModel))
  }

}
