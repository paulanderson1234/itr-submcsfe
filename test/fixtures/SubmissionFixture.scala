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

import common.Constants
import models.registration.RegistrationDetailsModel
import models.{KiProcessingModel, _}
import org.mockito.Matchers
import org.mockito.Mockito._
import auth.AuthEnrolledTestController.{INTERNAL_SERVER_ERROR => _, OK => _, SEE_OTHER => _, _}
import models.investorDetails.{HowMuchSpentOnSharesModel, InvestorDetailsModel, IsExistingShareHolderModel, NumberOfSharesPurchasedModel}
import models.submission._
import services.RegistrationDetailsService

import scala.concurrent.Future

trait SubmissionFixture {

  def setUpMocksRegistrationService(mockRegistrationService: RegistrationDetailsService): Unit = {
    when(mockRegistrationService.getRegistrationDetails(Matchers.eq(tavcReferenceId))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(Option(registrationDetailsModel)))
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

  val tradeStartDateModelYes = TradeStartDateModel(Constants.StandardRadioButtonYesValue, Some(1), Some(1), Some(2001))
  val tradeStartDateModelNo = TradeStartDateModel(Constants.StandardRadioButtonNoValue, None, None, None)

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

  val SEISAnswersModelToPost = ComplianceStatementAnswersModel(
    CompanyDetailsAnswersModel(natureOfBusinessValid, dateOfIncorporationValid, QualifyBusinessActivityModel(Constants.qualifyResearchAndDevelopment),
      hasInvestmentTradeStartedModel = None, researchStartDateModel = Some(ResearchStartDateModel("Yes", Some(1), Some(4), Some(2016))), seventyPercentSpentModel = None,
      shareIssueDateModel = shareIssueDateModel, grossAssetsModel = GrossAssetsModel(1000),
      grossAssetsAfterModel = None,fullTimeEmployeeCountModel = FullTimeEmployeeCountModel(1), commercialSaleModel = None),
    PreviousSchemesAnswersModel(HadPreviousRFIModel("Yes"), HadOtherInvestmentsModel("Yes"),
      Some(List(PreviousSchemeModel("test", 1, Some(1), Some("Name"), Some(1), Some(2), Some(2015), Some(1))))),
    ShareDetailsAnswersModel(ShareDescriptionModel(""),
      NumberOfSharesModel(5), TotalAmountRaisedModel(5), Some(TotalAmountSpentModel(5))),
    InvestorDetailsAnswersModel(validInvestors,
      WasAnyValueReceivedModel("No", None), ShareCapitalChangesModel("No", None)),
    ContactDetailsAnswersModel(ContactDetailsModel("", "", None, None, ""),fullCorrespondenceAddress),
    SupportingDocumentsUploadModel("Yes"),
    SchemeTypesModel(eis = false, seis = true), None, None, CostsAnswerModel(None, None),None, None,None, None)
}

