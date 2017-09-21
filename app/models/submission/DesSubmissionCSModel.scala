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

package models.submission

import common.Constants
import models.investorDetails._
import models.registration.RegistrationDetailsModel
import models._
import play.api.libs.json.Json
import utils.{Converters, Transformers, Validation}


case class DesIndividualDetailsModel(
                                      individualName: DesContactName,
                                      individualAddress: DesAddressType
                                    )
object DesIndividualDetailsModel{
  implicit val formats = Json.format[DesIndividualDetailsModel]
}
case class DesCompanyDetailsModel(
                                   organisationName: String,
                                   ctUtr:Option[String],
                                   crn:Option[String],
                                   companyAddress: Option[DesAddressType]
                                 )
object DesCompanyDetailsModel{
  implicit val formats = Json.format[DesCompanyDetailsModel]
}

case class DesCompanyOrIndividualDetailsModel(
                                               individualDetails: Option[DesIndividualDetailsModel],
                                               companyDetails: Option[DesCompanyDetailsModel]
                                             )
object DesCompanyOrIndividualDetailsModel{
  implicit val formats = Json.format[DesCompanyOrIndividualDetailsModel]
}

case class DesPreviousOwnershipModel(
                                      dateAcquired: String,
                                      prevOwnerStartDate: Option[String],
                                      previousOwner: DesCompanyOrIndividualDetailsModel
                                    )
object DesPreviousOwnershipModel{
  implicit val formats = Json.format[DesPreviousOwnershipModel]
}

case class DesTradeModel(
                          businessActivity: Option[String],
                          baDescription: String,
                          marketInfo: Option[DesMarketInfo], // eis only
                          thirtyDayRule: Option[Boolean],
                          dateTradeCommenced: String, // Not required for CS but required in DES scheme
                          annualCosts: Option[DesAnnualCostsModel],
                          annualTurnover:  Option[DesAnnualTurnoversModel],
                          previousOwnership: Option[DesPreviousOwnershipModel]
                        )

object DesTradeModel{
  implicit val formats = Json.format[DesTradeModel]
}


case class DesOrganisationStatusModel(
                                       numberOfFTEmployees: BigDecimal,
                                       shareOrLoanCapitalChanges: String,
                                       grossAssetBefore: CostModel,
                                       grossAssetAfter: CostModel
                                     )
object DesOrganisationStatusModel{
  implicit val formats = Json.format[DesOrganisationStatusModel]
}

case class DesInvestmentDetailsModel(
                                      growthJustification: String, // required as per DES scheme but not in CS Flow
                                      unitIssue: UnitIssueModel,
                                      amountSpent: Option[CostModel],
                                      organisationStatus: Option[DesOrganisationStatusModel]
                                    )
object DesInvestmentDetailsModel{
  implicit val formats = Json.format[DesInvestmentDetailsModel]
}

case class DesSubsidiaryPerformingTrade(
                                         ninetyPercentOwned: Boolean,
                                         companyDetails: DesCompanyDetailsModel
                                       )
object DesSubsidiaryPerformingTrade{
  implicit val formats = Json.format[DesSubsidiaryPerformingTrade]
}
case class DesGroupHoldingsModel(
                                  nodata:Option[String],
                                  groupHolding: Seq[UnitIssueModel]
                                )
object DesGroupHoldingsModel{
  implicit val formats = Json.format[DesGroupHoldingsModel]
}
case class DesInvestorInfoModel(
                                 investorDetails: DesCompanyOrIndividualDetailsModel,
                                 numberOfUnitsHeld: BigDecimal,
                                 investmentAmount: CostModel,
                                 existingGroupHoldings: Option[DesGroupHoldingsModel]
                               )
object DesInvestorInfoModel{
  implicit val formats = Json.format[DesInvestorInfoModel]
}

case class DesInvestorModel(
                             investorType: String,
                             investorInfo: DesInvestorInfoModel
                           )
object DesInvestorModel{
  implicit val formats = Json.format[DesInvestorModel]
}

case class DesInvestorDetailsModel(
                                    investor: Seq[DesInvestorModel]
                                  )
object DesInvestorDetailsModel{
  implicit val formats = Json.format[DesInvestorDetailsModel]
}
case class DesRepaymentModel(
                              repaymentDate: Option[String],
                              repaymentAmount: CostModel,
                              unitType: Option[String],
                              holdersName: Option[DesContactName],
                              subsidiaryName: Option[String]
                            )
object DesRepaymentModel{
  implicit val formats = Json.format[DesRepaymentModel]
}

case class DesRepaymentsModel(
                               repayment: Seq[DesRepaymentModel]
                             )
object DesRepaymentsModel{
  implicit val formats = Json.format[DesRepaymentsModel]
}
case class DesRFIModel(
                        schemeType: String,
                        name: Option[String],
                        issueDate: String,
                        amount: CostModel,
                        amountSpent: Option[CostModel]
                      )
object DesRFIModel{
  implicit val formats = Json.format[DesRFIModel]
}
case class DesRFICostsModel(
                             nodata:Option[String],
                             previousRFI: Seq[DesRFIModel]
                           )
object DesRFICostsModel{
  implicit val formats = Json.format[DesRFICostsModel]
}

case class DesOrganisationModel(
                                 utr:Option[String],
                                 chrn:Option[String],
                                 startDate:String,
                                 firstDateOfCommercialSale:Option[String], //esi only
                                 orgDetails: DesCompanyDetailsModel,
                                 previousRFIs: Option[DesRFICostsModel]
                               )
object DesOrganisationModel{
  implicit val formats = Json.format[DesOrganisationModel]
}
case class KiModel(
                    skilledEmployeesConditionMet: Boolean,
                    innovationConditionMet: Option[String],
                    kiConditionMet: Option[Boolean]
                  )
object KiModel{
  implicit val formats = Json.format[KiModel]
}

case class DesComplianceStatement(
                                   schemeType: String,
                                   trade: DesTradeModel, //
                                   investment: DesInvestmentDetailsModel, // Not required for CS SEIS but required in DES scheme
                                   subsidiaryPerformingTrade: Option[DesSubsidiaryPerformingTrade],
                                   knowledgeIntensive: Option[KiModel],
                                   investorDetails: DesInvestorDetailsModel,
                                   repayments: DesRepaymentsModel, // Not required for CS SEIS flow but required in DES scheme
                                   valueReceived: Option[String],
                                   organisation: DesOrganisationModel
                                 )

object DesComplianceStatement{
  implicit val formats = Json.format[DesComplianceStatement]
}

case class DesSubmission(
                          notRequired: Option[String],
                          complianceStatement: DesComplianceStatement
                        )
object DesSubmission{
  implicit val formats = Json.format[DesSubmission]
}

case class DesSubmissionModel(
                               agentReferenceNumber: Option[String],
                               correspondenceDetails: DesCorrespondenceDetails,
                               organisationType: String,
                               submission: DesSubmission
                             )
object DesSubmissionModel{
  implicit val formats = Json.format[DesSubmissionModel]
}

case class DesSubmissionCSModel (
                                  acknowledgementReference: Option[String] = None,
                                  submissionType: DesSubmissionModel
                                )
object DesSubmissionCSModel {
  implicit val formatCSSubmission = Json.format[DesSubmissionCSModel]

  /** Entry method to build and return the target model for submitting a compliance statement  from the source answerModel.
    *
    * @param answerModel An instance of the answers model with data to be submitted.
    * @param registrationDetailsModel An instance of the registration detail model with company data to be submitted.
    */
  def readDesSubmissionCSModel(answerModel: ComplianceStatementAnswersModel,
                               registrationDetailsModel: Option[RegistrationDetailsModel]): DesSubmissionCSModel = {
    DesSubmissionCSModel(None, readDesSubmissionModel(answerModel, registrationDetailsModel))
  }

  private def readDesSubmissionModel(answerModel: ComplianceStatementAnswersModel,
                             registrationDetailsModel: Option[RegistrationDetailsModel]): DesSubmissionModel = {
    DesSubmissionModel(None, readDesCorrespondenceDetails(answerModel.contactDetailsAnswersModel),
      OrganisationType.limited.toString, readDesSubmission(answerModel, registrationDetailsModel))
  }

  private def readDesCorrespondenceDetails(contactDetailsAnswersModel: ContactDetailsAnswersModel): DesCorrespondenceDetails = {
    DesCorrespondenceDetails(readContactName(contactDetailsAnswersModel),
      readContactDetails(contactDetailsAnswersModel), readAddressDetails(contactDetailsAnswersModel))
  }

  private def readContactName(contactDetailsAnswersModel: ContactDetailsAnswersModel): DesContactName = {
    DesContactName(contactDetailsAnswersModel.contactDetailsModel.forename,
      contactDetailsAnswersModel.contactDetailsModel.surname)
  }

  private def readContactDetails(contactDetailsAnswersModel: ContactDetailsAnswersModel): DesContactDetails = {
    DesContactDetails(contactDetailsAnswersModel.contactDetailsModel.telephoneNumber,
      contactDetailsAnswersModel.contactDetailsModel.mobileNumber,
      None,
      Some(contactDetailsAnswersModel.contactDetailsModel.email))
  }

  private def readAddressDetails(contactDetailsAnswersModel: ContactDetailsAnswersModel): DesAddressType = {
    DesAddressType(contactDetailsAnswersModel.correspondAddressModel.address.addressline1,
      contactDetailsAnswersModel.correspondAddressModel.address.addressline2,
      contactDetailsAnswersModel.correspondAddressModel.address.addressline3,
      contactDetailsAnswersModel.correspondAddressModel.address.addressline4,
      contactDetailsAnswersModel.correspondAddressModel.address.postcode,
      contactDetailsAnswersModel.correspondAddressModel.address.countryCode)
  }

  private def readDesSubmission(answerModel: ComplianceStatementAnswersModel,
                        registrationDetailsModel: Option[RegistrationDetailsModel]): DesSubmission = {
    DesSubmission(None, readDesComplianceStatement(answerModel, registrationDetailsModel))
  }

  private def readDesComplianceStatement(answerModel: ComplianceStatementAnswersModel,
                                 registrationDetailsModel: Option[RegistrationDetailsModel]): DesComplianceStatement = {
    DesComplianceStatement(getSchemeType(answerModel.schemeTypes), readDesTradeModel(answerModel),
      readDesInvestmentDetailsModel(answerModel),
      readDesSubsidiaryPerformingTrade(answerModel), readDesKnowledgeIntensive(answerModel),
      readDesInvestorDetailsModel(answerModel), readDesRepaymentsModel(answerModel),
      readDesValueReceived(answerModel.investorDetailsAnswersModel),
      readDesOrganisationModel(answerModel, registrationDetailsModel))
  }

  private def readDesTradeModel(answerModel: ComplianceStatementAnswersModel): DesTradeModel = {
    DesTradeModel(readDesBusinessActivity(answerModel.companyDetailsAnswersModel),
      readDesBaDescription(answerModel.companyDetailsAnswersModel),
      readDesMarketInfo(answerModel), readDesThirtyDayRule(answerModel),
      readDesTradeDateCommenced(answerModel.companyDetailsAnswersModel),
      readDesAnnualCostsModel(answerModel), readDesAnnualTurnoversModel(answerModel),
      readDesPreviousOwnershipModel(answerModel))
  }

  private def readDesBusinessActivity(companyDetailsAnswersModel: CompanyDetailsAnswersModel): Option[String] = {
    companyDetailsAnswersModel.qualifyBusinessActivityModel.isQualifyBusinessActivity match {
      case Constants.qualifyPrepareToTrade => Some(BusinessActivity.preparingToTrade.toString)
      case Constants.qualifyResearchAndDevelopment => Some(BusinessActivity.researchAndDevelopment.toString)
      case _ => Some(BusinessActivity.trade.toString)
    }
  }

  private def readDesBaDescription(companyDetailsAnswersModel: CompanyDetailsAnswersModel): String = {
    companyDetailsAnswersModel.natureOfBusinessModel.natureofbusiness
  }

  private def readDesMarketInfo(answerModel: ComplianceStatementAnswersModel): Option[DesMarketInfo] = {
    //EIS only. only populate if both new produt market and new geographic arket = no.
    // if both no market description should not sent present either (but this is covered by previos comment)
    def getMarketDescription(marketDescriptionModel: Option[MarketDescriptionModel]) : Option[String] = {
      if(marketDescriptionModel.nonEmpty) Some(marketDescriptionModel.fold("")(_.text)) else None
    }

    answerModel.marketInfo match{
      case Some(marketInfo) => if (marketInfo.newGeographicMarket.isNewGeographicalMarket == Constants.StandardRadioButtonYesValue ||
        marketInfo.newProductMarket.isNewProduct == Constants.StandardRadioButtonYesValue)
        Some(DesMarketInfo(answerToBoolean(marketInfo.newGeographicMarket.isNewGeographicalMarket),
          answerToBoolean(marketInfo.newProductMarket.isNewProduct),getMarketDescription(marketInfo.marketDescription))) else None
      case _  => None
    }
    
  }

  private def readDesThirtyDayRule(answerModel: ComplianceStatementAnswersModel): Option[Boolean] = {
    Some(true)
  }

  private def readDesTradeDateCommenced(companyDetailsAnswersModel: CompanyDetailsAnswersModel): String = {
    companyDetailsAnswersModel.qualifyBusinessActivityModel.isQualifyBusinessActivity match {
      case Constants.qualifyPrepareToTrade => readPrepareToTradeDateCommenced(companyDetailsAnswersModel)
      case Constants.qualifyResearchAndDevelopment => readResearchTradeDateCommenced(companyDetailsAnswersModel)
      case _ => Constants.standardIgnoreYearValue
    }
  }

  private def readPrepareToTradeDateCommenced(companyDetailsAnswersModel: CompanyDetailsAnswersModel): String = {
    if(companyDetailsAnswersModel.hasInvestmentTradeStartedModel.isDefined
      && companyDetailsAnswersModel.hasInvestmentTradeStartedModel.get.hasDate)
      Validation.dateToDesFormat(companyDetailsAnswersModel.hasInvestmentTradeStartedModel.get.hasInvestmentTradeStartedDay.get,
        companyDetailsAnswersModel.hasInvestmentTradeStartedModel.get.hasInvestmentTradeStartedMonth.get,
        companyDetailsAnswersModel.hasInvestmentTradeStartedModel.get.hasInvestmentTradeStartedYear.get)
    else
      Constants.standardIgnoreYearValue
  }

  private def readResearchTradeDateCommenced(companyDetailsAnswersModel: CompanyDetailsAnswersModel): String = {
    if(companyDetailsAnswersModel.researchStartDateModel.isDefined)
      Validation.dateToDesFormat(companyDetailsAnswersModel.researchStartDateModel.get.researchStartDay.get,
        companyDetailsAnswersModel.researchStartDateModel.get.researchStartMonth.get,
        companyDetailsAnswersModel.researchStartDateModel.get.researchStartYear.get)
    else
      Constants.standardIgnoreYearValue
  }

  private def readDesAnnualCostsModel(answerModel: ComplianceStatementAnswersModel): Option[DesAnnualCostsModel] = {

    // eis only
    if(answerModel.costsAnswersModel.operatingCosts.nonEmpty &&
      answerModel.kiAnswersModel.fold(false)(kiModel => kiModel.kiProcessingModel.companyWishesToApplyKi.fold(false)(_.self)))
      answerModel.costsAnswersModel.operatingCosts.fold(Some(DesAnnualCostsModel(List.empty)))(list =>
        Some(DesAnnualCostsModel(Converters.operatingCostsToList(list)))) else None

  }
  private def readDesAnnualCostModel(answerModel: ComplianceStatementAnswersModel): Vector[AnnualCostModel] = {
    Vector.empty :+ AnnualCostModel("", readDesOperatingCost(answerModel), readDesResearchAndDevelopmentCost(answerModel))
  }

  private def readDesOperatingCost(answerModel: ComplianceStatementAnswersModel): CostModel = {
    CostModel("")
  }

  private def readDesResearchAndDevelopmentCost(answerModel: ComplianceStatementAnswersModel): CostModel = {
    CostModel("")
  }
  private def readDesAnnualTurnoversModel(answerModel: ComplianceStatementAnswersModel): Option[DesAnnualTurnoversModel] = {

    answerModel.marketInfo match{
      case Some(marketInfo) => if (marketInfo.newGeographicMarket.isNewGeographicalMarket == Constants.StandardRadioButtonYesValue ||
        marketInfo.newProductMarket.isNewProduct == Constants.StandardRadioButtonYesValue && answerModel.costsAnswersModel.turnoverCostModel.nonEmpty)
        Some(DesAnnualTurnoversModel(answerModel.costsAnswersModel.turnoverCostModel.fold(List[TurnoverCostModel]())(list => Converters.turnoverCostsToList(list))))
      else None
      case _  => None
    }
  }

  private def readDesPreviousOwnershipModel(answerModel: ComplianceStatementAnswersModel): Option[DesPreviousOwnershipModel] = {
    None
  }

  //TODO: growth justification is not required for SEIS and needs hard coding to NA but for EIS it is required
  //onstants.notApplicable neeeds consitionally changing
  private def readDesInvestmentDetailsModel(answerModel: ComplianceStatementAnswersModel): DesInvestmentDetailsModel = {
    DesInvestmentDetailsModel(Constants.notApplicable, readUnitIssueModel(answerModel),
      readTotalAmountSpent(answerModel.shareDetailsAnswersModel), readDesOrganisationStatusDetails(answerModel))
  }

  private def readUnitIssueModel(answerModel: ComplianceStatementAnswersModel): UnitIssueModel = {
    UnitIssueModel(readShareDescription(answerModel.shareDetailsAnswersModel),
      readShareDateOfIssue(answerModel.companyDetailsAnswersModel), UnitType.shares.toString, readNominalValue(),
      answerModel.shareDetailsAnswersModel.numberOfSharesModel.numberOfShares,
      readTotalAmountRaised(answerModel.shareDetailsAnswersModel))
  }

  private def readShareDescription(shareDetailsAnswersModel: ShareDetailsAnswersModel): String = {
    shareDetailsAnswersModel.shareDescriptionModel.shareDescription
  }

  private def readShareDateOfIssue(companyDetailsAnswersModel: CompanyDetailsAnswersModel): String = {
    if(companyDetailsAnswersModel.shareIssueDateModel.day.isDefined)
      Validation.dateToDesFormat(companyDetailsAnswersModel.shareIssueDateModel.day.get,
        companyDetailsAnswersModel.shareIssueDateModel.month.get, companyDetailsAnswersModel.shareIssueDateModel.year.get)
    else
      Constants.standardIgnoreYearValue
  }

  private def readNominalValue(): CostModel = {
    CostModel("0")  // Missing in the source model needs to be removed
  }

  private def readTotalAmountRaised(shareDetailsAnswersModel: ShareDetailsAnswersModel): CostModel = {
    CostModel(Transformers.poundToPence(Left(shareDetailsAnswersModel.totalAmountRaisedModel.amount.toString())))
  }

  private def readTotalAmountSpent(shareDetailsAnswersModel: ShareDetailsAnswersModel): Option[CostModel] = {
    if(shareDetailsAnswersModel.totalAmountSpentModel.isDefined)
      Some(CostModel(Transformers.poundToPence(Left(shareDetailsAnswersModel.totalAmountSpentModel.get.totalAmountSpent.toString()))))
    else None
  }

  private def readDesOrganisationStatusDetails(answerModel: ComplianceStatementAnswersModel): Option[DesOrganisationStatusModel] = {
    Some(DesOrganisationStatusModel(readFTECount(answerModel.companyDetailsAnswersModel),
      readShareChangesDescription(answerModel.investorDetailsAnswersModel.shareCapitalChangesModel),
        CostModel(Transformers.poundToPence(Left(answerModel.companyDetailsAnswersModel.grossAssetsModel.grossAmount.toString()))),
        CostModel("0")))
  }

  def readFTECount(companyDetailsAnswersModel: CompanyDetailsAnswersModel): BigDecimal ={
    companyDetailsAnswersModel.fullTimeEmployeeCountModel.employeeCount
  }

  def readShareChangesDescription(shareCapitalChangesModel: ShareCapitalChangesModel): String ={
    if(shareCapitalChangesModel.changesDescription.isDefined)
      shareCapitalChangesModel.changesDescription.get
    else Constants.notApplicable
  }

  private def readDesSubsidiaryPerformingTrade(answerModel: ComplianceStatementAnswersModel): Option[DesSubsidiaryPerformingTrade] = {
    None
  }

  private def readDesKnowledgeIntensive(answerModel: ComplianceStatementAnswersModel): Option[KiModel] = {

    // eis only
    answerModel.kiAnswersModel match {
      case Some(ki) =>
        val kiAnswersModel = answerModel.kiAnswersModel.get
        if (kiAnswersModel.kiProcessingModel.companyWishesToApplyKi.getOrElse(false))
          Some(KiModel(skilledEmployeesConditionMet = kiAnswersModel.kiProcessingModel.hasPercentageWithMasters.getOrElse(false),
            innovationConditionMet = if (kiAnswersModel.tenYearPlanModel.nonEmpty) kiAnswersModel.tenYearPlanModel.get.tenYearPlanDesc else None,
            kiConditionMet = Some(kiAnswersModel.kiProcessingModel.isKi)))
        else None
      case _ => None
    }

  }

  private def readDesInvestorDetailsModel(answerModel: ComplianceStatementAnswersModel): DesInvestorDetailsModel = {
    DesInvestorDetailsModel(readDesInvestorModel(answerModel.investorDetailsAnswersModel))
  }

  private def readDesInvestorModel(investorDetailsAnswersModel: InvestorDetailsAnswersModel): Vector[DesInvestorModel] = {
    investorDetailsAnswersModel.investors.foldLeft(Vector.empty[DesInvestorModel]){
      (desInvestorModel , investorDetailsModel) =>
        desInvestorModel :+ DesInvestorModel(readInvestorOrNominee(investorDetailsModel.investorOrNomineeModel.get.addInvestorOrNominee),
          readDesInvestorInfoModel(investorDetailsModel))
    }
  }

  private def readInvestorOrNominee(investorOrNominee: String) : String = {
    investorOrNominee match {
      case Constants.nominee => InvestorType.nominee.toString
      case _ => InvestorType.investor.toString
    }
  }

  private def readDesInvestorInfoModel(investorDetailsModel: InvestorDetailsModel): DesInvestorInfoModel = {
    DesInvestorInfoModel(readDesCompanyOrIndividualModel(investorDetailsModel),
      investorDetailsModel.numberOfSharesPurchasedModel.get.numberOfSharesPurchased,
      readInvestmentAmount(investorDetailsModel), readDesGroupHoldingsModel(investorDetailsModel))
  }

  private def readDesCompanyOrIndividualModel(investorDetailsModel: InvestorDetailsModel) : DesCompanyOrIndividualDetailsModel = {
    investorDetailsModel.companyOrIndividualModel.get.companyOrIndividual match {
      case Constants.typeCompany =>
        DesCompanyOrIndividualDetailsModel(None, readDesCompanyDetailsModel(investorDetailsModel.companyDetailsModel.get))
      case Constants.typeIndividual =>
        DesCompanyOrIndividualDetailsModel(readDesIndividualDetailsModel(investorDetailsModel.individualDetailsModel.get), None)
    }
  }

  private def readDesIndividualDetailsModel(individualDetailsModel: IndividualDetailsModel) : Option[DesIndividualDetailsModel] = {
    Some(DesIndividualDetailsModel(readIndividualName(individualDetailsModel),
      readIndividualAddress(individualDetailsModel)))
  }

  private def readIndividualName(individualDetailsModel: IndividualDetailsModel): DesContactName = {
    DesContactName(individualDetailsModel.forename, individualDetailsModel.surname)
  }

  private def readIndividualAddress(individualDetailsModel: IndividualDetailsModel): DesAddressType = {
    DesAddressType(individualDetailsModel.addressline1, individualDetailsModel.addressline2,
      individualDetailsModel.addressline3, individualDetailsModel.addressline4,
      individualDetailsModel.postcode,individualDetailsModel.countryCode)
  }

  private def readDesCompanyDetailsModel(companyDetailsModel: CompanyDetailsModel): Option[DesCompanyDetailsModel] = {
    Some(DesCompanyDetailsModel(companyDetailsModel.companyName, None, None,
      readCompanyAddress(companyDetailsModel)))
  }

  private def readCompanyAddress(companyDetailsModel: CompanyDetailsModel): Option[DesAddressType] = {
    Some(DesAddressType(companyDetailsModel.companyAddressline1, companyDetailsModel.companyAddressline2,
      companyDetailsModel.companyAddressline3, companyDetailsModel.companyAddressline4,
      companyDetailsModel.companyPostcode, companyDetailsModel.countryCode))
  }

  private def readInvestmentAmount(investorDetailsModel: InvestorDetailsModel): CostModel = {
    CostModel(Transformers.poundToPence(Left(investorDetailsModel.amountSpentModel.get.amount.toString())))
  }

  private def readDesGroupHoldingsModel(investorDetailsModel: InvestorDetailsModel): Option[DesGroupHoldingsModel] = {
    if(investorDetailsModel.previousShareHoldingModels.isDefined)
      Some(DesGroupHoldingsModel(None, readPreviousGroupHoldings(investorDetailsModel.previousShareHoldingModels.get)))
    else None
  }

  private def readPreviousGroupHoldings(previousShareHoldingModels: Vector[PreviousShareHoldingModel]): Vector[UnitIssueModel] = {
    previousShareHoldingModels.foldLeft(Vector.empty[UnitIssueModel]){
      (unitIssueModel , shareHoldings) => unitIssueModel :+ UnitIssueModel(
        readShareHoldingDescription(shareHoldings.previousShareHoldingDescriptionModel.get),
        readShareHoldingDateOfIssue(shareHoldings.investorShareIssueDateModel.get),
        UnitType.shares.toString,
        readShareHoldingNominalValue(shareHoldings.previousShareHoldingNominalValueModel.get),
        shareHoldings.numberOfPreviouslyIssuedSharesModel.get.previouslyIssuedShares,
        CostModel("0"))
    }
  }

  private def readShareHoldingDescription(previousShareHoldingDescriptionModel: PreviousShareHoldingDescriptionModel): String = {
    previousShareHoldingDescriptionModel.description
  }

  private def readShareHoldingDateOfIssue(investorShareIssueDateModel: InvestorShareIssueDateModel): String = {
    if(investorShareIssueDateModel.investorShareIssueDateDay.isDefined)
      Validation.dateToDesFormat(investorShareIssueDateModel.investorShareIssueDateDay.get,
        investorShareIssueDateModel.investorShareIssueDateMonth.get, investorShareIssueDateModel.investorShareIssueDateYear.get)
    else Constants.standardIgnoreYearValue
  }

  private def readShareHoldingNominalValue(previousShareHoldingNominalValueModel: PreviousShareHoldingNominalValueModel): CostModel = {
    CostModel(Transformers.poundToPence(Left(previousShareHoldingNominalValueModel.nominalValue.toString())))
  }

  private def readDesRepaymentsModel(answerModel: ComplianceStatementAnswersModel) : DesRepaymentsModel = {
    DesRepaymentsModel(Vector.empty)
  }

  private def readDesValueReceived(investorDetailsAnswersModel: InvestorDetailsAnswersModel): Option[String] = {
    investorDetailsAnswersModel.valueReceivedModel.aboutValueReceived
  }

  private def readDesOrganisationModel(answerModel: ComplianceStatementAnswersModel,
                               registrationDetailsModel: Option[RegistrationDetailsModel]): DesOrganisationModel = {
    DesOrganisationModel(None, None, readDateOfIncorporation(answerModel.companyDetailsAnswersModel),
      None, readDesOrganisationDetails(registrationDetailsModel), readPreviousRFICostModel(answerModel))
  }

  private def readDateOfIncorporation(companyDetailsAnswersModel: CompanyDetailsAnswersModel) : String = {
    if(companyDetailsAnswersModel.dateOfIncorporationModel.day.isDefined)
      Validation.dateToDesFormat(companyDetailsAnswersModel.dateOfIncorporationModel.day.get,
        companyDetailsAnswersModel.dateOfIncorporationModel.month.get, companyDetailsAnswersModel.dateOfIncorporationModel.year.get)
    else Constants.standardIgnoreYearValue
  }

  private def readFirstDateOfCommericailSale(companyDetailsAnswersModel: CompanyDetailsAnswersModel) : String = {
    if(companyDetailsAnswersModel.dateOfIncorporationModel.day.isDefined)
      Validation.dateToDesFormat(companyDetailsAnswersModel.dateOfIncorporationModel.day.get,
        companyDetailsAnswersModel.dateOfIncorporationModel.month.get, companyDetailsAnswersModel.dateOfIncorporationModel.year.get)
    else Constants.standardIgnoreYearValue
  }

  private def readDesOrganisationDetails(registrationDetailsModel: Option[RegistrationDetailsModel]): DesCompanyDetailsModel = {
    //TODO if no reg model why would we sens company
    require(registrationDetailsModel.nonEmpty)

    if(registrationDetailsModel.isDefined)
      DesCompanyDetailsModel(registrationDetailsModel.get.organisationName, None, None,
        readOrgAddress(registrationDetailsModel.get.addressModel))
    else
      // get rid of this
      DesCompanyDetailsModel("COMPANY", None, None, None)
  }

  private def readOrgAddress(addressModel: AddressModel): Option[DesAddressType] = {
    Some(DesAddressType(addressModel.addressline1, addressModel.addressline2,
      addressModel.addressline3, addressModel.addressline4,
      addressModel.postcode, addressModel.countryCode))
  }

  private def readPreviousRFICostModel(answerModel: ComplianceStatementAnswersModel) : Option[DesRFICostsModel] = {
    if (answerModel.previousSchemesAnswersModel.previousSchemeModel.isDefined
      && answerModel.previousSchemesAnswersModel.previousSchemeModel.get.nonEmpty)
      Some(DesRFICostsModel(None, readPreviousRFI(answerModel.previousSchemesAnswersModel)))
    else
      None

  }

  private def readPreviousRFI(previousSchemesAnswersModel: PreviousSchemesAnswersModel) : Vector[DesRFIModel] = {
    previousSchemesAnswersModel.previousSchemeModel.get.foldLeft(Vector.empty[DesRFIModel]) {
        (desRFIModel, previousSchemeModel) =>
          desRFIModel :+ DesRFIModel(previousSchemeModel.schemeTypeDesc, previousSchemeModel.otherSchemeName,
            readPreviousRFIIssueDate(previousSchemeModel),
            CostModel(Transformers.poundToPence(Left(previousSchemeModel.investmentAmount.toString))),
            readPreviousSchemesInvestmentAmountSpent(previousSchemeModel))
    }
  }

  private def readPreviousRFIIssueDate(previousSchemeModel: PreviousSchemeModel): String = {
    if(previousSchemeModel.day.isDefined && previousSchemeModel.month.isDefined
      && previousSchemeModel.year.isDefined)
      Validation.dateToDesFormat(previousSchemeModel.day.get, previousSchemeModel.month.get, previousSchemeModel.year.get)
    else Constants.standardIgnoreYearValue
  }

  private def readPreviousSchemesInvestmentAmountSpent(previousSchemeModel: PreviousSchemeModel): Option[CostModel] = {
    if(previousSchemeModel.investmentSpent.isDefined)
      Some(CostModel(Transformers.poundToPence(Left(previousSchemeModel.investmentSpent.get.toString))))
    else
      None
  }

  private def getSchemeType(schemeTypes:SchemeTypesModel) : String = {
    if(schemeTypes.eis) SchemeType.eis.toString else SchemeType.seis.toString
  }

  private def answerToBoolean(input:String): Boolean = {
    input.toLowerCase match {
      case "yes" => true
      case _ => false
    }
  }
}