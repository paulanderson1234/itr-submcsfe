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

package controllers.helpers

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import auth.{Enrolment, Identifier}
import common.{Constants, KeystoreKeys}
import connectors.{EnrolmentConnector, S4LConnector, SubmissionConnector}
import fixtures.SubmissionFixture
import models.submission.SchemeTypesModel
import models.investorDetails._
import models.{UsedInvestmentReasonBeforeModel, YourCompanyNeedModel, _}
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneAppPerSuite
import play.api.libs.json.Json
import services.{FileUploadService, RegistrationDetailsService, SubscriptionService}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future


trait BaseSpec extends UnitSpec with OneAppPerSuite with MockitoSugar with FakeRequestHelper with SubmissionFixture with BeforeAndAfterEach {

  val mockS4lConnector = mock[S4LConnector]
  val mockEnrolmentConnector = mock[EnrolmentConnector]
  val mockSubmissionConnector = mock[SubmissionConnector]
  val mockSubscriptionService= mock[SubscriptionService]
  val mockRegistrationDetailsService = mock[RegistrationDetailsService]
  val mockFileUploadService = mock[FileUploadService]

  override def beforeEach() {
    reset(mockS4lConnector)
    reset(mockEnrolmentConnector)
    reset(mockSubmissionConnector)
  }

  def mockEnrolledRequest(selectedSchemes: Option[SchemeTypesModel] = None): Unit = {
    when(mockEnrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Option(Enrolment("HMRC-TAVC-ORG", Seq(Identifier("TavcReference", "1234")), "Activated"))))
    when(mockEnrolmentConnector.getTavcReferenceNumber(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(tavcReferenceId))
    when(mockS4lConnector.fetchAndGetFormData[SchemeTypesModel](Matchers.eq(KeystoreKeys.selectedSchemes))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(selectedSchemes))
  }

  def mockNotEnrolledRequest(): Unit = {
    when(mockEnrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any())).thenReturn(Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[SchemeTypesModel](Matchers.eq(KeystoreKeys.selectedSchemes))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
  }

  implicit val hc = HeaderCarrier()


  val applicationHubModelMax = ApplicationHubModel("Company ltd", AddressModel("1 ABCDE Street","FGHIJ Town", Some("FGHIJKL Town"),Some("MNO County"),
    Some("tf4 2ls"),"GB"), ContactDetailsModel("Firstname","Lastname",Some("0123324234234"),Some("4567324234324"),"test@test.com"))
  val applicationHubModelMin = ApplicationHubModel("Company ltd", AddressModel("1 ABCDE Street","FGHIJ Town", None,None,None,"GB"),
    ContactDetailsModel("Firstname","Lastname",None,None,"test@test.com"))

  val addressModel = AddressModel("Line 1", "Line 2", Some("Line 3"), Some("Line 4"), Some("AB1 1AB"), "GB")
  val subscriptionDetailsModel = SubscriptionDetailsModel("",contactDetailsModel,contactAddressModel)
  val companyDetailsModel = CompanyDetailsModel("Line 0", "Line 1", "Line 2", Some("Line 3"), Some("Line 4"), Some("AB1 1AB"), countryCode = "JP", Some(1))
  val companyDetailsModel1 = CompanyDetailsModel("Line 0", "Line 1", "Line 2", Some("Line 3"), Some("Line 4"), Some("AB1 1AB"), countryCode = "JP", Some(1))
  val companyDetailsModel2 = CompanyDetailsModel("Line 0", "Line 1", "Line 2", Some("Line 3"), Some("Line 4"), Some("AB1 1AB"), countryCode = "JP", Some(2))
  val companyDetailsModel3 = CompanyDetailsModel("Line 0", "Line 1", "Line 2", Some("Line 3"), Some("Line 4"), Some("AB1 1AB"), countryCode = "JP", Some(3))

  val contactDetailsModel = ContactDetailsModel("Test", "Name", Some("01111 111111"), Some("0872552488"), "test@test.com")
  val contactDetailsOneNumberModel = ContactDetailsModel("Test", "Name", None, Some("0872552488"), "test@test.com")
  val confirmContactDetailsModel = ConfirmContactDetailsModel(Constants.StandardRadioButtonYesValue, contactDetailsModel)

  val contactAddressModel = new AddressModel("ABC XYZ", "1 ABCDE Street", countryCode = "JP")
  
  val investmentGrowModel = InvestmentGrowModel("At vero eos et accusamusi et iusto odio dignissimos ducimus qui blanditiis praesentium " +
    "voluptatum deleniti atque corrupti quos dolores et quas molestias excepturi sint occaecati cupiditate non provident, similique " +
    "sunt in culpa qui officia deserunt mollitia animi, tid est laborum etttt dolorum fuga. Et harum quidem rerum facilis est et expedita distinctio. " +
    "Nam libero tempore, cum soluta nobis est eligendi optio cumque nihili impedit quo minus id quod maxime placeat facere possimus")

  val natureOfBusinessModel = NatureOfBusinessModel("Creating new products")

  val operatingCostsModel = OperatingCostsModel("4100200", "3600050", "4252500", "410020", "360005", "425250", "2006", "2005", "2004")

  val confirmCorrespondAddressModel = ConfirmCorrespondAddressModel(Constants.StandardRadioButtonYesValue, addressModel)

  val newGeographicalMarketModelYes = NewGeographicalMarketModel(Constants.StandardRadioButtonYesValue)
  val newGeographicalMarketModelNo = NewGeographicalMarketModel(Constants.StandardRadioButtonNoValue)

  val newProductMarketModelYes = NewProductModel(Constants.StandardRadioButtonYesValue)
  val newProductMarketModelNo = NewProductModel(Constants.StandardRadioButtonNoValue)

  val isKnowledgeIntensiveModelYes = IsKnowledgeIntensiveModel(Constants.StandardRadioButtonYesValue)
  val isKnowledgeIntensiveModelNo = IsKnowledgeIntensiveModel(Constants.StandardRadioButtonNoValue)

  val isCompanyKnowledgeIntensiveModelYes = IsCompanyKnowledgeIntensiveModel(Constants.StandardRadioButtonYesValue)
  val isCompanyKnowledgeIntensiveModelNo = IsCompanyKnowledgeIntensiveModel(Constants.StandardRadioButtonNoValue)

  val kiProcessingModelMet = KiProcessingModel(None, Some(true), Some(false), Some(false), Some(false))
  val kiProcessingModelNotMet = KiProcessingModel(Some(false),Some(false), Some(false), Some(false), Some(false))

  val kiProcessingModelIsKi = KiProcessingModel(Some(true), Some(true), Some(true), Some(true), Some(true), Some(true))

  val trueKIModel = KiProcessingModel(Some(true), Some(true), Some(true), Some(true), None, Some(true))
  val falseKIModel = KiProcessingModel(Some(false), Some(false), Some(false), Some(false), None, Some(false))
  val isKiKIModel = KiProcessingModel(Some(false), Some(true), Some(true), Some(true), Some(true), Some(true))
  val missingDataKIModel = KiProcessingModel(Some(true),None, Some(true), Some(true), Some(true), Some(true))

  val hadPreviousRFIModelYes = HadPreviousRFIModel(Constants.StandardRadioButtonYesValue)
  val hadPreviousRFIModelNo = HadPreviousRFIModel(Constants.StandardRadioButtonNoValue)

  val commercialSaleYear = 2004
  val commercialSaleMonth = 2
  val commercialSaleDay = 29
  val commercialSaleModelYes = CommercialSaleModel(Constants.StandardRadioButtonYesValue,
    Some(commercialSaleDay), Some(commercialSaleMonth), Some(commercialSaleYear))
  val commercialSaleModelNo = CommercialSaleModel(Constants.StandardRadioButtonNoValue, None, None, None)

  val subsidiariesModelYes = SubsidiariesModel(Constants.StandardRadioButtonYesValue)
  val subsidiariesModelNo = SubsidiariesModel(Constants.StandardRadioButtonNoValue)

  val subsidiariesNinetyOwnedModelYes = SubsidiariesNinetyOwnedModel(Constants.StandardRadioButtonYesValue)
  val subsidiariesNinetyOwnedModelNo = SubsidiariesNinetyOwnedModel(Constants.StandardRadioButtonNoValue)

  val previousBeforeDOFCSModelYes = PreviousBeforeDOFCSModel(Constants.StandardRadioButtonYesValue)
  val previousBeforeDOFCSModelNo = PreviousBeforeDOFCSModel(Constants.StandardRadioButtonNoValue)

  val percentageStaffWithMastersModelYes = PercentageStaffWithMastersModel(Constants.StandardRadioButtonYesValue)
  val percentageStaffWithMastersModelNo = PercentageStaffWithMastersModel(Constants.StandardRadioButtonNoValue)

  val subsidiariesSpendingInvestmentModelYes = SubsidiariesSpendingInvestmentModel(Constants.StandardRadioButtonYesValue)
  val subsidiariesSpendingInvestmentModelNo = SubsidiariesSpendingInvestmentModel(Constants.StandardRadioButtonNoValue)

  val proposedInvestmentAmount = 5000000
  val proposedInvestmentModel = ProposedInvestmentModel(proposedInvestmentAmount)

  val previousSchemeModel1 = PreviousSchemeModel(
    Constants.PageInvestmentSchemeEisValue, 2356, None, None, Some(4), Some(12), Some(2009), Some(1))
  val previousSchemeModel2 = PreviousSchemeModel(
    Constants.PageInvestmentSchemeSeisValue, 2356, Some(666), None, Some(4), Some(12), Some(2010), Some(3))
  val previousSchemeModel3 = PreviousSchemeModel(
    Constants.PageInvestmentSchemeAnotherValue, 2356, None, Some("My scheme"), Some(9), Some(8), Some(2010), Some(5))
  val previousSchemeVectorList = Vector(previousSchemeModel1, previousSchemeModel2, previousSchemeModel3)

  val emptyVectorList = Vector[PreviousSchemeModel]()

  val registeredAddressModel = RegisteredAddressModel("AB1 1AB")

  val taxpayerReferenceModel = TaxpayerReferenceModel("1234567891012")

  val tenYearPlanModelYes = TenYearPlanModel(Constants.StandardRadioButtonYesValue, Some("At vero eos et accusamus et iusto odio dignissimos ducimus qui " +
    "blanditiis praesentium voluptatum deleniti atque corrupti quos dolores et quas molestias excepturi sint occaecati cupiditate non provident, similique " +
    "sunt in culpa qui officia deserunt mollitia animi, id est laborum et dolorum fuga. Et harum quidem rerum facilis est et expedita distinctio. " +
    "Nam libero tempore, cum soluta nobis est eligendi optio cumque nihil impedit quo minus id quod maxime placeat facere possimus"))
  val tenYearPlanModelNo = TenYearPlanModel(Constants.StandardRadioButtonNoValue, None)

  val annualTurnoverCostsModel = AnnualTurnoverCostsModel("750000", "800000", "934000", "231000", "340000", "2004", "2005", "2006", "2007", "2008")

  val usedInvestmentReasonBeforeModelYes = UsedInvestmentReasonBeforeModel(Constants.StandardRadioButtonYesValue)
  val usedInvestmentReasonBeforeModelNo = UsedInvestmentReasonBeforeModel(Constants.StandardRadioButtonNoValue)

  val yourCompanyNeedModel = YourCompanyNeedModel("AA")

  val envelopeId: Option[String] = Some("00000000000000000000000000000000")

  val seisSchemeTypesModel = Some(SchemeTypesModel(seis = true))
  val eisSchemeTypesModel = Some(SchemeTypesModel(eis = true))
  val vctSchemeTypesModel = Some(SchemeTypesModel(vct = true))
  val eisSeisSchemeTypesModel = Some(SchemeTypesModel(seis = true, eis = true))

  val eisSeisProcessingModelWithIneligible = EisSeisProcessingModel(Some(true), Some(false), Some(false) )
  val eisSeisProcessingModelIneligiblePreviousSchemeType = EisSeisProcessingModel(Some(false), Some(true), Some(false) )
  val eisSeisProcessingModelIneligibleStartDate= EisSeisProcessingModel(Some(true), Some(false), Some(false) )
  val eisSeisProcessingModelIneligiblePreviouSchemeThreshold= EisSeisProcessingModel(Some(false), Some(false), Some(true) )
  val eisSeisProcessingModelEligible = EisSeisProcessingModel(Some(false), Some(false), Some(false) )

  val cacheMapEisSeisProcessingModelEligible: CacheMap = CacheMap("", Map("" -> Json.toJson(EisSeisProcessingModel(Some(false), Some(false), Some(false)))))


  val internalId = "Int-312e5e92-762e-423b-ac3d-8686af27fdb5"

  //val dateOfIncorporationModel = DateOfIncorporationModel(Some(3), Some(4), Some(2013))

  val isFirstTradeIModelYes = IsFirstTradeModel(Constants.StandardRadioButtonYesValue)
  val isFirstTradeModelNo = IsFirstTradeModel(Constants.StandardRadioButtonNoValue)

  val addAnotherInvestorModelYes = AddAnotherInvestorModel(Constants.StandardRadioButtonYesValue)
  val addAnotherInvestorModelNo = AddAnotherInvestorModel(Constants.StandardRadioButtonNoValue)

  val isSeventyPercentSpentModelYes = SeventyPercentSpentModel(Constants.StandardRadioButtonYesValue)
  val isSeventyPercentSpentModelNo = SeventyPercentSpentModel(Constants.StandardRadioButtonNoValue)

  val hadOtherInvestmentsModelYes = HadOtherInvestmentsModel(Constants.StandardRadioButtonYesValue)
  val hadOtherInvestmentsModelNo = HadOtherInvestmentsModel(Constants.StandardRadioButtonNoValue)

  val fileId = "1"

  val qualifyPrepareToTrade = QualifyBusinessActivityModel(Constants.qualifyPrepareToTrade)
  val qualifyResearchAndDevelopment = QualifyBusinessActivityModel(Constants.qualifyResearchAndDevelopment)

  val hasInvestmentTradeStartedYear = 2004
  val hasInvestmentTradeStartedMonth = 2
  val hasInvestmentTradeStartedDay = 29

  val researchStartedYear = 2004
  val researchStartedMonth = 2
  val researchStartedDay = 29

  val shareIssueDateYear = 2004
  val shareIssueDateMonth = 2
  val shareIssueDateDay = 29

  val hasInvestmentTradeStartedModelYes = HasInvestmentTradeStartedModel(Constants.StandardRadioButtonYesValue,
    Some(hasInvestmentTradeStartedDay), Some(hasInvestmentTradeStartedMonth), Some(hasInvestmentTradeStartedYear))
  val hasInvestmentTradeStartedModelNo = HasInvestmentTradeStartedModel(Constants.StandardRadioButtonNoValue, None, None, None)

  val researchStartDateModelYes = ResearchStartDateModel(Constants.StandardRadioButtonYesValue,
    Some(researchStartedDay), Some(researchStartedMonth), Some(researchStartedYear))
  val researchStartDateModelNo = ResearchStartDateModel(Constants.StandardRadioButtonNoValue, None, None, None)

  val shareIssuetDateModel = ShareIssueDateModel(
    Some(shareIssueDateDay), Some(shareIssueDateMonth), Some(shareIssueDateYear))
  val shareIssuetDateModelEmpty = ShareIssueDateModel(None, None, None)

  val shareDescriptionModel = ShareDescriptionModel("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent" +
    "quis odio at neque fringilla consectetur sit amet eget dolor. Morbi lectus nisl, volutpat quis ipsum.")

  val numberOfSharesModel= NumberOfSharesModel(9999999999999.00)






  //investor details
  val investorModel = AddInvestorOrNomineeModel(Constants.investor, Some(1))
  val investorModel1 = AddInvestorOrNomineeModel(Constants.investor, Some(1))
  val investorModel2 = AddInvestorOrNomineeModel(Constants.investor, Some(2))
  val investorModel3 = AddInvestorOrNomineeModel(Constants.investor, Some(3))
  val nomineeModel = AddInvestorOrNomineeModel(Constants.nominee, Some(1))
  val nomineeModel1 = AddInvestorOrNomineeModel(Constants.nominee, Some(1))
  val nomineeModel2 = AddInvestorOrNomineeModel(Constants.nominee, Some(2))
  val nomineeModel3 = AddInvestorOrNomineeModel(Constants.nominee, Some(3))
  val companyOrIndividualModel = CompanyOrIndividualModel(Constants.typeCompany, Some(1))
  val companyOrIndividualModel1 = CompanyOrIndividualModel(Constants.typeCompany, Some(1))
  val companyOrIndividualModel2 = CompanyOrIndividualModel(Constants.typeCompany, Some(2))
  val companyOrIndividualModel3 = CompanyOrIndividualModel(Constants.typeCompany, Some(3))
  val individualDetailsModel =
    IndividualDetailsModel("Joe", "Bloggs", "Line 1", "Line 2", Some("Line 3"), Some("AB1 1AB"), None, countryCode = "JP", Some(1))
  val individualDetailsModel1 =
    IndividualDetailsModel("Joe", "Bloggs", "Line 1", "Line 2", Some("Line 3"), Some("AB1 1AB"), None, countryCode = "JP", Some(1))
  val individualDetailsModel2 =
    IndividualDetailsModel("Joe", "Bloggs", "Line 1", "Line 2", Some("Line 3"), Some("AB1 1AB"), None, countryCode = "JP", Some(2))
  val individualDetailsModel3 =
    IndividualDetailsModel("Joe", "Bloggs", "Line 1", "Line 2", Some("Line 3"), Some("AB1 1AB"), None, countryCode = "JP", Some(3))
  val numberOfSharesPurchasedModel = NumberOfSharesPurchasedModel(1000, Some(1))
  val numberOfSharesPurchasedModel1 = NumberOfSharesPurchasedModel(1000, Some(1))
  val numberOfSharesPurchasedModel2 = NumberOfSharesPurchasedModel(1000, Some(2))
  val numberOfSharesPurchasedModel3 = NumberOfSharesPurchasedModel(1000, Some(3))
  val howMuchSpentOnSharesModel = HowMuchSpentOnSharesModel(1000, Some(1))
  val howMuchSpentOnSharesModel1 = HowMuchSpentOnSharesModel(1000, Some(1))
  val howMuchSpentOnSharesModel2 = HowMuchSpentOnSharesModel(1000, Some(2))
  val howMuchSpentOnSharesModel3 = HowMuchSpentOnSharesModel(1000, Some(3))
  val isExistingShareHolderModelNo = IsExistingShareHolderModel("No")
  val isExistingShareHolderModelYes = IsExistingShareHolderModel("Yes")
  val numberOfPreviouslyIssuedShares = NumberOfPreviouslyIssuedSharesModel(1,Some(1))

  //share holdings
  val investorShareIssueDateModel = InvestorShareIssueDateModel(Some(1), Some(1), Some(1980), Some(1), Some(1))
  val investorShareIssueDateModel1 = InvestorShareIssueDateModel(Some(1), Some(1), Some(1980), Some(1), Some(1))
  val investorShareIssueDateModel2 = InvestorShareIssueDateModel(Some(1), Some(1), Some(1980), Some(2), Some(2))
  val investorShareIssueDateModel3 = InvestorShareIssueDateModel(Some(1), Some(1), Some(1980), Some(3), Some(3))

  val previousShareHoldingNominalValueModel = PreviousShareHoldingNominalValueModel(1000, Some(1), Some(1))
  val previousShareHoldingNominalValueModel1 = PreviousShareHoldingNominalValueModel(1000, Some(1), Some(1))
  val previousShareHoldingNominalValueModel2 = PreviousShareHoldingNominalValueModel(1000, Some(2))
  val previousShareHoldingDescriptionModel = PreviousShareHoldingDescriptionModel("A previous shareholding", Some(1), Some(1))
  val previousShareHoldingDescriptionModel1 = PreviousShareHoldingDescriptionModel("A previous shareholding", Some(1), Some(1))
  val previousShareHoldingDescriptionModel2 = PreviousShareHoldingDescriptionModel("A previous shareholding", Some(2))

  val numberOfPreviouslyIssuedSharesModel = NumberOfPreviouslyIssuedSharesModel(1000, Some(1), Some(1))
  val numberOfPreviouslyIssuedSharesModel1 = NumberOfPreviouslyIssuedSharesModel(1000, Some(1), Some(1))
  val numberOfPreviouslyIssuedSharesModel2 = NumberOfPreviouslyIssuedSharesModel(1000, Some(2))


  val shareHoldersModel1ForInvestor2 = Vector(PreviousShareHoldingModel(Some(investorShareIssueDateModel1), Some(numberOfPreviouslyIssuedSharesModel1),
    Some(previousShareHoldingNominalValueModel1), Some(previousShareHoldingDescriptionModel1), Some(1), Some(2)))
  val shareHoldersModel2ForInvestor2 = Vector(PreviousShareHoldingModel(Some(investorShareIssueDateModel2), Some(numberOfPreviouslyIssuedSharesModel2),
    Some(previousShareHoldingNominalValueModel2), Some(previousShareHoldingDescriptionModel2), Some(2), Some(2)))

  val shareHoldersModel1ForInvestor1 = Vector(PreviousShareHoldingModel(Some(investorShareIssueDateModel1), Some(numberOfPreviouslyIssuedSharesModel1),
    Some(previousShareHoldingNominalValueModel1), Some(previousShareHoldingDescriptionModel1), Some(1), Some(1)))
  val shareHoldersModel2ForInvestor1 = Vector(PreviousShareHoldingModel(Some(investorShareIssueDateModel2), Some(numberOfPreviouslyIssuedSharesModel2),
    Some(previousShareHoldingNominalValueModel2), Some(previousShareHoldingDescriptionModel2), Some(2), Some(1)))

  val investorCompany = InvestorDetailsModel(Some(investorModel2), Some(companyOrIndividualModel2), Some(companyDetailsModel2), None,
    Some(numberOfSharesPurchasedModel2), Some(howMuchSpentOnSharesModel2), Some(isExistingShareHolderModelYes),
    Some(shareHoldersModel1ForInvestor2), Some(2))


  val validModelWithPrevShareHoldings = InvestorDetailsModel(Some(investorModel2), Some(companyOrIndividualModel2), Some(companyDetailsModel2), None,
    Some(numberOfSharesPurchasedModel2), Some(howMuchSpentOnSharesModel2), Some(isExistingShareHolderModelYes),
    Some(shareHoldersModel1ForInvestor2), Some(2))

  val validModelNoPrevShareHoldings = InvestorDetailsModel(Some(investorModel1), Some(companyOrIndividualModel1), Some(companyDetailsModel1), None,
    Some(numberOfSharesPurchasedModel1), Some(howMuchSpentOnSharesModel1), Some(isExistingShareHolderModelNo), Some(Vector()), Some(1))

  val invalidModelCompanyAndIndividualDetailsPresent = InvestorDetailsModel(Some(investorModel3), Some(companyOrIndividualModel3), Some(companyDetailsModel3),
    Some(individualDetailsModel3), Some(numberOfSharesPurchasedModel3), Some(howMuchSpentOnSharesModel3), None, Some(Vector()), Some(3))

  val previousInvestorVectorList = Vector(validModelNoPrevShareHoldings, validModelWithPrevShareHoldings, invalidModelCompanyAndIndividualDetailsPresent)
  val onlyInvestorOrNomineeVectorList = Vector(validModelNoPrevShareHoldings)

  val validModelWithPrevShareHoldingsOption1 = InvestorDetailsModel(Some(investorModel1), Some(companyOrIndividualModel1),
    Some(companyDetailsModel1), None, Some(numberOfSharesPurchasedModel1), Some(howMuchSpentOnSharesModel1), Some(isExistingShareHolderModelYes),
    Some(shareHoldersModel1ForInvestor1), Some(1))

  val listOfInvestorsComplete = Vector(validModelWithPrevShareHoldings)
  val listOfInvestorsCompleteOption1 = Vector(validModelWithPrevShareHoldingsOption1)
  val listOfInvestorsIncomplete = Vector(validModelWithPrevShareHoldings.copy(companyOrIndividualModel = None))


  //investors tets data for investor delete tests
  val investorNominee1 = AddInvestorOrNomineeModel(Constants.investor, Some(1))
  val investorNominee2 = AddInvestorOrNomineeModel(Constants.nominee, Some(2))
  val investorNominee3 = AddInvestorOrNomineeModel(Constants.investor, Some(3))
  val investorNominee4 = AddInvestorOrNomineeModel(Constants.nominee, Some(4))
  val investorNominee5 = AddInvestorOrNomineeModel(Constants.investor, Some(5))
  val investorNominee6 = AddInvestorOrNomineeModel(Constants.nominee, Some(6))
  val companyIndividual1 = CompanyOrIndividualModel(Constants.typeIndividual, Some(1))
  val companyIndividual2 = CompanyOrIndividualModel(Constants.typeCompany, Some(2))
  // no individual details details f0r 3
  val companyIndividual3 = CompanyOrIndividualModel(Constants.typeIndividual, Some(3))
  val companyIndividual4 = CompanyOrIndividualModel(Constants.typeCompany, Some(4))
  // only investor/nominee supplied
  val companyIndividual5 = CompanyOrIndividualModel(Constants.typeIndividual, Some(5))
  val companyIndividual6 = CompanyOrIndividualModel(Constants.typeCompany, Some(6))

  val individualDetail1 =
    IndividualDetailsModel("Sam", "West", "Line 1", "Line 2", Some("Line 3"),
      Some("AB1 1AB"), None, countryCode = "JP", Some(1))

  val companyDetail1 = CompanyDetailsModel("Ben's Boots Ltd.", "Line 1", "Line 2", Some("Line 3"),
    Some("AB1 1AB"), None, countryCode = "UK", Some(2))

  val individualDetail2 =
    IndividualDetailsModel("Jack", "Smith", "1 Line", "2 Line", Some("3 Line"),
      Some("QQ1 1QQ"), None, countryCode = "UK", Some(4))

  val investor1 = InvestorDetailsModel(processingId = Some(1), investorOrNomineeModel = Some(investorNominee1),
    companyOrIndividualModel = Some(companyIndividual1), individualDetailsModel = Some(individualDetail1),
    companyDetailsModel = None, previousShareHoldingModels = Some(shareHoldersModel1ForInvestor1))

  val investor2 = InvestorDetailsModel(processingId = Some(2), investorOrNomineeModel = Some(investorNominee2),
    companyOrIndividualModel = Some(companyIndividual2), individualDetailsModel = None,
    companyDetailsModel = Some(companyDetail1), previousShareHoldingModels = Some(shareHoldersModel1ForInvestor2))

  // individual specified but no details
  val investor3 = InvestorDetailsModel(processingId = Some(3), investorOrNomineeModel = Some(investorNominee3),
    companyOrIndividualModel = Some(companyIndividual3), individualDetailsModel = None, companyDetailsModel = None)

  // company specified but no details
  val investor4 = InvestorDetailsModel(processingId = Some(4), investorOrNomineeModel = Some(investorNominee4),
    companyOrIndividualModel = Some(companyIndividual4), individualDetailsModel = None, companyDetailsModel = None)

  // only investor/nominee supplied and nothing else
  val investor5 = InvestorDetailsModel(processingId = Some(5), investorOrNomineeModel = Some(investorNominee5))

  val investorListForDeleteTests = Vector(investor1, investor2, investor3, investor4, investor5)
}
