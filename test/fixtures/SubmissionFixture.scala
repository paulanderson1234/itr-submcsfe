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

import common.{Constants, KeystoreKeys}
import connectors.S4LConnector
import models.registration.RegistrationDetailsModel
import models.{KiProcessingModel, _}
import org.mockito.Matchers
import org.mockito.Mockito._
import auth.AuthEnrolledTestController.{INTERNAL_SERVER_ERROR => _, OK => _, SEE_OTHER => _, _}
import models.investorDetails.{HowMuchSpentOnSharesModel, InvestorDetailsModel, IsExistingShareHolderModel, NumberOfSharesPurchasedModel}
import models.seis.{_}
import models.submission._
import services.RegistrationDetailsService

import scala.concurrent.Future

//noinspection ScalaStyle
trait SubmissionFixture {

  def setupMocksCs(mockS4lConnector: S4LConnector): Unit = {

    when(mockS4lConnector.fetchAndGetFormData[SchemeTypesModel](Matchers.eq(KeystoreKeys.selectedSchemes))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(schemeTypesSEIS)))
    when(mockS4lConnector.fetchAndGetFormData[NatureOfBusinessModel](Matchers.eq(KeystoreKeys.natureOfBusiness))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(natureOfBusinessValid)))
    when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(dateOfIncorporationValid)))
    when(mockS4lConnector.fetchAndGetFormData[QualifyBusinessActivityModel](Matchers.eq(KeystoreKeys.isQualifyBusinessActivity))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Some(QualifyBusinessActivityModel(Constants.qualifyResearchAndDevelopment))))
    when(mockS4lConnector.fetchAndGetFormData[HasInvestmentTradeStartedModel](Matchers.eq(KeystoreKeys.hasInvestmentTradeStarted))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[ResearchStartDateModel](Matchers.eq(KeystoreKeys.researchStartDate))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Some(ResearchStartDateModel("Yes", Some(1), Some(4), Some(2016)))))
    when(mockS4lConnector.fetchAndGetFormData[SeventyPercentSpentModel](Matchers.eq(KeystoreKeys.seventyPercentSpent))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[ShareIssueDateModel](Matchers.eq(KeystoreKeys.shareIssueDate))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Some(shareIssueDateModel)))
    when(mockS4lConnector.fetchAndGetFormData[GrossAssetsModel](Matchers.eq(KeystoreKeys.grossAssets))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Some(GrossAssetsModel(1000))))
    when(mockS4lConnector.fetchAndGetFormData[FullTimeEmployeeCountModel](Matchers.eq(KeystoreKeys.fullTimeEmployeeCount))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Some(FullTimeEmployeeCountModel(1))))
    when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Some(HadPreviousRFIModel("Yes"))))
    when(mockS4lConnector.fetchAndGetFormData[HadOtherInvestmentsModel](Matchers.eq(KeystoreKeys.hadOtherInvestments))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Some(HadOtherInvestmentsModel("Yes"))))
    when(mockS4lConnector.fetchAndGetFormData[List[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Some(List(PreviousSchemeModel("test", 1, Some(1), Some("Name"), Some(1), Some(2), Some(2015), Some(1))))))
    when(mockS4lConnector.fetchAndGetFormData[ShareDescriptionModel](Matchers.eq(KeystoreKeys.shareDescription))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Some(ShareDescriptionModel(""))))
    when(mockS4lConnector.fetchAndGetFormData[NumberOfSharesModel](Matchers.eq(KeystoreKeys.numberOfShares))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Some(NumberOfSharesModel(5))))
    when(mockS4lConnector.fetchAndGetFormData[TotalAmountRaisedModel](Matchers.eq(KeystoreKeys.totalAmountRaised))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Some(TotalAmountRaisedModel(5))))
    when(mockS4lConnector.fetchAndGetFormData[TotalAmountSpentModel](Matchers.eq(KeystoreKeys.totalAmountSpent))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Some(TotalAmountSpentModel(5))))
    when(mockS4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](Matchers.eq(KeystoreKeys.investorDetails))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Some(validInvestors)))
    when(mockS4lConnector.fetchAndGetFormData[WasAnyValueReceivedModel](Matchers.eq(KeystoreKeys.wasAnyValueReceived))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Some(WasAnyValueReceivedModel("No", None))))
    when(mockS4lConnector.fetchAndGetFormData[ShareCapitalChangesModel](Matchers.eq(KeystoreKeys.shareCapitalChanges))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Some(ShareCapitalChangesModel("No", None))))
    when(mockS4lConnector.fetchAndGetFormData[ContactDetailsModel](Matchers.eq(KeystoreKeys.contactDetails))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Some(ContactDetailsModel("", "", None, None, ""))))
    when(mockS4lConnector.fetchAndGetFormData[ConfirmCorrespondAddressModel](Matchers.eq(KeystoreKeys.confirmContactAddress))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Some(ConfirmCorrespondAddressModel("Yes", fullCorrespondenceAddress))))
    when(mockS4lConnector.fetchAndGetFormData[SupportingDocumentsUploadModel](Matchers.eq(KeystoreKeys.supportingDocumentsUpload))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Some(SupportingDocumentsUploadModel("Yes"))))

  }

  def setUpMocks(mockS4lConnector: S4LConnector) {

    // mandatory
    when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(kiProcModelValid)))
    when(mockS4lConnector.fetchAndGetFormData[NatureOfBusinessModel](Matchers.eq(KeystoreKeys.natureOfBusiness))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(natureOfBusinessValid)))
    when(mockS4lConnector.fetchAndGetFormData[ContactDetailsModel](Matchers.eq(KeystoreKeys.contactDetails))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(contactDetailsValid)))
    when(mockS4lConnector.fetchAndGetFormData[TotalAmountRaisedModel](Matchers.eq(KeystoreKeys.totalAmountRaised))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(totalAmountRaisedValid)))
    when(mockS4lConnector.fetchAndGetFormData[InvestmentGrowModel](Matchers.eq(KeystoreKeys.investmentGrow))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(investmentGrowValid)))
    when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(dateOfIncorporationValid)))
    when(mockS4lConnector.fetchAndGetFormData[AddressModel](Matchers.eq(KeystoreKeys.contactAddress))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(fullCorrespondenceAddress)))


    // potentially mandatory
    when(mockS4lConnector.fetchAndGetFormData[SubsidiariesSpendingInvestmentModel](Matchers.eq(KeystoreKeys.subsidiariesSpendingInvestment))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(subsidiariesSpendInvestValid)))
    when(mockS4lConnector.fetchAndGetFormData[SubsidiariesNinetyOwnedModel](Matchers.eq(KeystoreKeys.subsidiariesNinetyOwned))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(subsidiariesNinetyOwnedValid)))
    when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(previousSchemesValid)))
    when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(commercialSaleValid)))
    when(mockS4lConnector.fetchAndGetFormData[NewGeographicalMarketModel](Matchers.eq(KeystoreKeys.newGeographicalMarket))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(newGeographicalMarketValid)))
    when(mockS4lConnector.fetchAndGetFormData[NewProductModel](Matchers.eq(KeystoreKeys.newProduct))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(newProductValid)))
    when(mockS4lConnector.fetchAndGetFormData[TenYearPlanModel](Matchers.eq(KeystoreKeys.tenYearPlan))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(tenYearPlanValid)))
    when(mockS4lConnector.fetchAndGetFormData[OperatingCostsModel](Matchers.eq(KeystoreKeys.operatingCosts))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(operatingCostsValid)))
    when(mockS4lConnector.fetchAndGetFormData[AnnualTurnoverCostsModel](Matchers.eq(KeystoreKeys.turnoverCosts))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(turnoverCostsValid)))
  }

  def setUpMocksRegistrationService(mockRegistrationService: RegistrationDetailsService): Unit = {
    when(mockRegistrationService.getRegistrationDetails(Matchers.eq(tavcReferenceId))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(registrationDetailsModel)))
  }

  def setUpMocksMinimumRequiredModels(mockS4lConnector: S4LConnector) {


    // mandatory minimum
    when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(kiProcModelValid)))
    when(mockS4lConnector.fetchAndGetFormData[NatureOfBusinessModel](Matchers.eq(KeystoreKeys.natureOfBusiness))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(natureOfBusinessValid)))
    when(mockS4lConnector.fetchAndGetFormData[ContactDetailsModel](Matchers.eq(KeystoreKeys.contactDetails))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(contactDetailsValid)))
    when(mockS4lConnector.fetchAndGetFormData[TotalAmountRaisedModel](Matchers.eq(KeystoreKeys.totalAmountRaised))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(totalAmountRaisedValid)))
    when(mockS4lConnector.fetchAndGetFormData[InvestmentGrowModel](Matchers.eq(KeystoreKeys.investmentGrow))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(investmentGrowValid)))
    when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(dateOfIncorporationValid)))
    when(mockS4lConnector.fetchAndGetFormData[AddressModel](Matchers.eq(KeystoreKeys.contactAddress))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(fullCorrespondenceAddress)))

    // can be empty to pass
    when(mockS4lConnector.fetchAndGetFormData[SubsidiariesSpendingInvestmentModel](Matchers.eq(KeystoreKeys.subsidiariesSpendingInvestment))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[SubsidiariesNinetyOwnedModel](Matchers.eq(KeystoreKeys.subsidiariesNinetyOwned))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[NewGeographicalMarketModel](Matchers.eq(KeystoreKeys.newGeographicalMarket))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[NewProductModel](Matchers.eq(KeystoreKeys.newProduct))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[TenYearPlanModel](Matchers.eq(KeystoreKeys.tenYearPlan))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[OperatingCostsModel](Matchers.eq(KeystoreKeys.operatingCosts))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[AnnualTurnoverCostsModel](Matchers.eq(KeystoreKeys.turnoverCosts))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(turnoverCostsValid)))
  }

  def setUpMocksTestMinimumRequiredModels(mockS4lConnector: S4LConnector, mockRegistrationService: RegistrationDetailsService,
                                          kiModel: Option[KiProcessingModel],
                                          natureBusiness: Option[NatureOfBusinessModel],
                                          contactDetails: Option[ContactDetailsModel],
                                          totalAmountRaised: Option[TotalAmountRaisedModel],
                                          investGrow: Option[InvestmentGrowModel],
                                          dateIncorp: Option[DateOfIncorporationModel],
                                          contactAddress: Option[AddressModel],
                                          returnRegistrationDetails: Boolean
                                         )
  {

    // mandatory minimum
    when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn( if(kiModel.nonEmpty) Future.successful(Option(kiModel.get)) else Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[NatureOfBusinessModel](Matchers.eq(KeystoreKeys.natureOfBusiness))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn( if(natureBusiness.nonEmpty) Future.successful(Option(natureBusiness.get)) else Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[ContactDetailsModel](Matchers.eq(KeystoreKeys.contactDetails))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn( if(contactDetails.nonEmpty) Future.successful(Option(contactDetails.get)) else Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[TotalAmountRaisedModel](Matchers.eq(KeystoreKeys.totalAmountRaised))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn( if(totalAmountRaised.nonEmpty) Future.successful(Option(totalAmountRaised.get)) else Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[InvestmentGrowModel](Matchers.eq(KeystoreKeys.investmentGrow))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn( if(investGrow.nonEmpty) Future.successful(Option(investGrow.get)) else Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn( if(dateIncorp.nonEmpty) Future.successful(Option(dateIncorp.get)) else Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[AddressModel](Matchers.eq(KeystoreKeys.contactAddress))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn( if(contactAddress.nonEmpty) Future.successful(Option(contactAddress.get)) else Future.successful(None))

      when(mockRegistrationService.getRegistrationDetails(Matchers.eq(tavcReferenceId))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(if(returnRegistrationDetails) Future.successful(Option(registrationDetailsModel)) else Future.successful(None))


    // can be empty to pass
    when(mockS4lConnector.fetchAndGetFormData[SubsidiariesSpendingInvestmentModel](Matchers.eq(KeystoreKeys.subsidiariesSpendingInvestment))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[SubsidiariesNinetyOwnedModel](Matchers.eq(KeystoreKeys.subsidiariesNinetyOwned))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[NewGeographicalMarketModel](Matchers.eq(KeystoreKeys.newGeographicalMarket))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[NewProductModel](Matchers.eq(KeystoreKeys.newProduct))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[TenYearPlanModel](Matchers.eq(KeystoreKeys.tenYearPlan))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[OperatingCostsModel](Matchers.eq(KeystoreKeys.operatingCosts))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[AnnualTurnoverCostsModel](Matchers.eq(KeystoreKeys.turnoverCosts))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(turnoverCostsValid)))
  }

  val fullCorrespondenceAddress: AddressModel = AddressModel(addressline1 = "line 1",
    addressline2 = "Line 2", addressline3 = Some("Line 3"), addressline4 = Some("Line 4"),
    postcode = Some("TF1 4NY"), countryCode = "GB")

  val registrationDetailsModel = RegistrationDetailsModel("Company ltd", fullCorrespondenceAddress)

  val fullContactDetailsModel: ContactDetailsModel = ContactDetailsModel(forename = "Fred",
    surname = "Flinsstone", telephoneNumber = Some("01952 255899"), mobileNumber = None, email = "rubble@jurassic.com")

  val schemeTypesEIS: SchemeTypesModel = SchemeTypesModel(eis = true, seis = false, vct = false, sitr = false)
  val schemeTypesSEIS: SchemeTypesModel = SchemeTypesModel(eis = false, seis = true, vct = false, sitr = false)
  val testAgentRef = "AARN1234567"
  val tavcReferenceId = "XATAVC000123456"

  val marketInfo = SubmitMarketInfoModel(newGeographicalMarketModel = NewGeographicalMarketModel(Constants.StandardRadioButtonNoValue),
    newProductModel = NewProductModel(Constants.StandardRadioButtonYesValue))

   val opcostFull = OperatingCostsModel(operatingCosts1stYear = "101", operatingCosts2ndYear = "102",
    operatingCosts3rdYear = "103", rAndDCosts1stYear = "201", rAndDCosts2ndYear = "202", rAndDCosts3rdYear = "203",
     firstYear = "2005", secondYear = "2004", thirdYear = "2003")

  val costsFull = utils.Converters.operatingCostsToList(opcostFull)

  val turnover = List(TurnoverCostModel("2003", turnover = CostModel("66")),
    TurnoverCostModel("2004", turnover = CostModel("67")),
    TurnoverCostModel("2004", turnover = CostModel("68")),
    TurnoverCostModel("2004", turnover = CostModel("69")),
    TurnoverCostModel("2005", turnover = CostModel("70")))

  val dateOfIncorporationModel = DateOfIncorporationModel(day = Some(5), month = Some(6), year = Some(2007))
  val dateOfIncorporationModelKI = DateOfIncorporationModel(day = Some(5), month = Some(6), year = Some(2017))
  val shareIssueDateModel = ShareIssueDateModel(day = Some(5), month = Some(6), year = Some(2007))
  val startDateModelModelYes = TradeStartDateModel(tradeStartDay = Some(5), tradeStartMonth = Some(6),
    tradeStartYear = Some(2007), hasTradeStartDate = Constants.StandardRadioButtonYesValue)
  val startDateModelModelNo = TradeStartDateModel(tradeStartDay = Some(5), tradeStartMonth = Some(6),
    tradeStartYear = Some(2007), hasTradeStartDate = Constants.StandardRadioButtonNoValue)

  val subsidiaryPerformingTradeMinimumReq = SubsidiaryPerformingTradeModel(ninetyOwnedModel = SubsidiariesNinetyOwnedModel(Constants.StandardRadioButtonYesValue),
    organisationName = "Made up test subsidiary org name")
  val subsidiaryPerformingTradeWithAddress = SubsidiaryPerformingTradeModel(ninetyOwnedModel =
    SubsidiariesNinetyOwnedModel(Constants.StandardRadioButtonYesValue), organisationName = "Made up test subsidiary org name",
    companyAddress = Some(fullCorrespondenceAddress))

  val subsidiaryPerformingTradeWithFull = SubsidiaryPerformingTradeModel(ninetyOwnedModel =
    SubsidiariesNinetyOwnedModel("true"), organisationName = "Made up test subsidiary org name",
    companyAddress = Some(fullCorrespondenceAddress), ctUtr = Some("1234567891"), crn = Some("555589852"))

  val previousSchemesFull = Vector(PreviousSchemeModel(schemeTypeDesc = Constants.schemeTypeEis, investmentAmount = 2000,
    day = Some(1),
    month = Some(2),
    year = Some(2004),
    processingId = None,
    investmentSpent = Some(19),
    otherSchemeName = None),
    PreviousSchemeModel(schemeTypeDesc = Constants.schemeTypeEis, investmentAmount = 5000,
      day = Some(2),
      month = Some(3),
      year = Some(2003),
      processingId = None,
      investmentSpent = Some(20),
      otherSchemeName = None),
    PreviousSchemeModel(schemeTypeDesc = Constants.schemeTypeOther, investmentAmount = 6000,
      day = Some(4),
      month = Some(5),
      year = Some(2002),
      processingId = None,
      investmentSpent = Some(21),
      otherSchemeName = Some("Other 3"))
  )

  val organisationFull = OrganisationDetailsModel(utr = Some("1234567891"), organisationName = "my org name",
    chrn = Some("2222222222"), startDate = dateOfIncorporationModel, firstDateOfCommercialSale = Some("2009-04-01"),
    ctUtr = Some("5555555555"), crn = Some("crnvalue"), companyAddress = Some(fullCorrespondenceAddress),
    previousRFIs = Some(previousSchemesFull.toList))

  val tradeStartDateModelYes = TradeStartDateModel(Constants.StandardRadioButtonYesValue, Some(1), Some(1), Some(2001))
  val tradeStartDateModelNo = TradeStartDateModel(Constants.StandardRadioButtonNoValue, None, None, None)

  val model = AdvancedAssuranceSubmissionType(
    agentReferenceNumber = Some(testAgentRef),
    acknowledgementReference = Some("AARN1234567"),
    whatWillUseForModel = Some(WhatWillUseForModel(None)),
    natureOfBusinessModel = NatureOfBusinessModel("Some nature of business description"),
    contactDetailsModel = fullContactDetailsModel,
    correspondenceAddress = fullCorrespondenceAddress,
    schemeTypes = schemeTypesEIS,
    marketInfo = Some(marketInfo),
    dateTradeCommenced = tradeStartDateModelYes.toDate,
    annualCosts = Some(costsFull),
    annualTurnover = Some(turnover),
    proposedInvestmentModel = TotalAmountRaisedModel(250000),
    investmentGrowModel = InvestmentGrowModel("It will help me invest in new equipment and R&D"),
    knowledgeIntensive = Some(KiModel(skilledEmployeesConditionMet = true, innovationConditionMet = Some("reason met"), kiConditionMet = Some(true))),
    subsidiaryPerformingTrade = Some(subsidiaryPerformingTradeWithFull),
    organisationDetails = organisationFull
  )

  val fullSubmissionSourceData = Submission(model)

  val kiProcModelValid = KiProcessingModel(companyAssertsIsKi = Some(true), companyWishesToApplyKi = Some(true), dateConditionMet = Some(true), hasPercentageWithMasters = Some(true), costsConditionMet = Some(true))
  val kiProcModelValidAssertNo = KiProcessingModel(companyAssertsIsKi = Some(false), companyWishesToApplyKi = Some(true), dateConditionMet = Some(true), hasPercentageWithMasters = Some(true), costsConditionMet = Some(true))
  val whatWillUseForValid = None
  val natureOfBusinessValid = NatureOfBusinessModel("Technology supplier")
  val contactDetailsValid = ContactDetailsModel("fred", "Smith", Some("01952 245666"), None, "fred@hotmail.com")
  val totalAmountRaisedValid = TotalAmountRaisedModel(2000)
  val investmentGrowValid = InvestmentGrowModel("It will be used to pay for R&D")
  val dateOfIncorporationValid = DateOfIncorporationModel(Some(2), Some(3), Some(2012))

  // potentially optional or required
  val subsidiariesSpendInvestValid = SubsidiariesSpendingInvestmentModel(Constants.StandardRadioButtonYesValue)
  val subsidiariesNinetyOwnedValid = SubsidiariesNinetyOwnedModel(Constants.StandardRadioButtonYesValue)
  val previousSchemesValid = previousSchemesFull
  val commercialSaleValid = CommercialSaleModel(Constants.StandardRadioButtonYesValue, Some(12), Some(5), Some(2011))
  val newGeographicalMarketValid = NewGeographicalMarketModel(Constants.StandardRadioButtonYesValue)
  val newProductValid = NewProductModel(Constants.StandardRadioButtonYesValue)
  val tenYearPlanValid = TenYearPlanModel(Constants.StandardRadioButtonYesValue, Some("To borrow to invest as in business plan"))
  val operatingCostsValid = OperatingCostsModel("12", "13", "14", "15", "16", "17", "2005", "2004", "2003")
  val turnoverCostsValid = AnnualTurnoverCostsModel("12", "13", "14", "15", "16", "2003", "2004", "2005", "2006", "2007")

  val validInvestors = Vector(InvestorDetailsModel(Some(AddInvestorOrNomineeModel("Investor", Some(1))),
    Some(CompanyOrIndividualModel("Individual", Some(1))), None, Some(IndividualDetailsModel("", "", "", "", None, None, None, "UK", Some(1))),
    Some(NumberOfSharesPurchasedModel(1, Some(1))), Some(HowMuchSpentOnSharesModel(1, Some(1))), Some(IsExistingShareHolderModel("No", Some(1))),
    None, Some(1)))

  //TODO: this model sets EIS values to None - need to test ESI version of this too
  val validSEISAnswersModel = ComplianceStatementAnswersModel(
    CompanyDetailsAnswersModel(natureOfBusinessValid, dateOfIncorporationValid, QualifyBusinessActivityModel(Constants.qualifyResearchAndDevelopment),
      None, Some(ResearchStartDateModel("Yes", Some(1), Some(4), Some(2016))), None, shareIssueDateModel, GrossAssetsModel(1000),
      FullTimeEmployeeCountModel(1), None),
    PreviousSchemesAnswersModel(HadPreviousRFIModel("Yes"), HadOtherInvestmentsModel("Yes"),
      Some(List(PreviousSchemeModel("test", 1, Some(1), Some("Name"), Some(1), Some(2), Some(2015), Some(1))))),
    ShareDetailsAnswersModel(ShareDescriptionModel(""),
      NumberOfSharesModel(5), TotalAmountRaisedModel(5), Some(TotalAmountSpentModel(5))),
    InvestorDetailsAnswersModel(validInvestors,
      WasAnyValueReceivedModel("No", None), ShareCapitalChangesModel("No", None)),
    ContactDetailsAnswersModel(ContactDetailsModel("", "", None, None, ""),
      ConfirmCorrespondAddressModel("Yes", fullCorrespondenceAddress)),
    SupportingDocumentsUploadModel("Yes"),
    SchemeTypesModel(eis = false, seis = true), None, None, CostsAnswerModel(None, None))
}
