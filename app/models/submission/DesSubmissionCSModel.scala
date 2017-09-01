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
import models.seis._
import models.{CompanyDetailsModel, IndividualDetailsModel, PreviousSchemeModel}
import play.api.libs.json.Json
import utils.{Transformers, Validation}

case class DesSubmissionCSModel (
                                  acknowledgementReference: Option[String] = None,
                                  submissionType: DesSubmissionModel
                                )

case class DesSubmissionModel(
                               agentReferenceNumber: Option[String],
                               correspondenceDetails: DesCorrespondenceDetails,
                               organisationType: String,
                               submission: DesSubmission
                            )

case class DesCorrespondenceDetails(
                                     contactName: DesContactName,
                                     contactDetails: DesContactDetails,
                                     contactAddress: DesAddressType
                                   )

case class DesContactName(
                           name1: String,
                           name2: String
                         )

case class DesContactDetails(
                              phoneNumber: Option[String],
                              mobileNumber: Option[String],
                              faxNumber: Option[String],
                              emailAddress: Option[String]
                            )

case class DesAddressType(
                           addressLine1: String,
                           addressLine2: String,
                           addressLine3: Option[String],
                           addressLine4: Option[String],
                           postalCode: Option[String],
                           countryCode: String
                         )

case class DesSubmission(
                          notRequired: Option[String],
                          complianceStatement: DesComplianceStatement
                        )

case class DesComplianceStatement(
                                   schemeType: String,
                                   trade: DesTradeModel, //
                                   investment: DesInvestmentDetailsModel, // Not required for CS but required in DES scheme
                                   subsidiaryPerformingTrade: Option[DesSubsidiaryPerformingTrade],
                                   knowledgeIntensive: Option[KiModel],
                                   investorsDetails: DesInvestorDetailsModel,
                                   repayments: DesRepaymentsModel, // Not required for CS SEIS flow but required in DES scheme
                                   valueReceived: Option[String],
                                   organisation: DesOrganisationModel
                                 )

case class DesTradeModel(
                          businessActivity: Option[String],
                          baDescription: String,
                          marketInfo: Option[DesMarketInfo],
                          thirtyDayRule: Option[Boolean],
                          dateTradeCommenced: String, // Not required for CS but required in DES scheme
                          annualCosts: Option[DesAnnualCostsModel],
                          annualTurnover:  Option[DesAnnualTurnoversModel],
                          previousOwnership: Option[DesPreviousOwnershipModel]
                        )

case class DesMarketInfo(
                          newGeographicMarket: Boolean,
                          newProductMarket: Boolean,
                          marketDescription: Option[String]
                        )

case class DesAnnualCostsModel(
                             nodata:Option[String],
                             annualCost: Seq[AnnualCostModel]
                           )

case class DesAnnualTurnoversModel(
                                 nodata:Option[String],
                                 annualTurnover: Seq[TurnoverCostModel]
                               )

case class DesPreviousOwnershipModel(
                                      dateAcquired: String,
                                      prevOwnerStartDate: Option[String],
                                      previousOwner: DesCompanyOrIndividualDetailsModel
                                    )

case class DesCompanyOrIndividualDetailsModel(
                                               individualDetails: Option[DesIndividualDetailsModel],
                                               companyDetails: Option[DesCompanyDetailsModel]
                                             )

case class DesIndividualDetailsModel(
                                      individualName: DesContactName,
                                      individualAddress: DesAddressType
                                    )

case class DesCompanyDetailsModel(
                                   organisationName: String,
                                   ctUtr:Option[String],
                                   crn:Option[String],
                                   companyAddress: Option[DesAddressType]
                                 )

case class DesInvestmentDetailsModel(
                                      growthJustification: String, // required as per DES scheme but not in CS Flow
                                      unitIssue: UnitIssueModel,
                                      amountSpent: Option[CostModel],
                                      organisationStatus: Option[DesOrganisationStatusModel]
                                    )

case class DesOrganisationStatusModel(
                                       numberOfFTEmployees: BigDecimal,
                                       shareOrLoanCapitalChanges: String,
                                       grossAssetBefore: CostModel,
                                       grossAssetAfter: CostModel
                                     )

case class DesSubsidiaryPerformingTrade(
                                         ninetyPercentOwned: Boolean,
                                         companyDetails: DesCompanyDetailsModel
                                       )

case class DesInvestorDetailsModel(
                                    investor: Seq[DesInvestorModel]
                                  )

case class DesInvestorModel(
                             investorType: String,
                             investorInfo: DesInvestorInfoModel
                           )

case class DesInvestorInfoModel(
                                 investorDetails: DesCompanyOrIndividualDetailsModel,
                                 numberOfUnitsHeld: BigDecimal,
                                 investmentAmount: CostModel,
                                 existingGroupHoldings: Option[DesGroupHoldingsModel]
                               )

case class DesGroupHoldingsModel(
                                  nodata:Option[String],
                                  groupHolding: Seq[UnitIssueModel]
                                )

case class DesRepaymentsModel(
                               repayment: Seq[DesRepaymentModel]
                             )

case class DesRepaymentModel(
                              repaymentDate: Option[String],
                              repaymentAmount: CostModel,
                              unitType: Option[String],
                              holdersName: Option[DesContactName],
                              subsidiaryName: Option[String]
                             )

case class DesOrganisationModel(
                            utr:Option[String],
                            chrn:Option[String],
                            startDate:String,
                            firstDateOfCommercialSale:Option[String],
                            orgDetails: DesCompanyDetailsModel,
                            previousRFIs: Option[DesRFICostsModel]
                          )

case class DesRFICostsModel(
                             nodata:Option[String],
                             previousRFI: Seq[DesRFIModel]
                           )

case class DesRFIModel(
                     schemeType: String,
                     name: Option[String],
                     issueDate: String,
                     amount: CostModel,
                     amountSpent: Option[CostModel]
                   )

object SchemeType extends Enumeration {
  type SchemeType = Value
  val seis = Value("SEIS")
  val eis = Value("EIS")
}

object OrganisationType extends Enumeration {
  type OrganisationType = Value
  val limited = Value("Limited")
  val communityInterestCompany = Value("Community Interest Company")
  val communityBenefitsSociety = Value("Community Benefits Society")
  val charityCompany = Value("Charity - Company")
  val charityTrust = Value("Charity - Trust")
  val partnership = Value("Partnership")
  val other = Value("Other")
}

object UnitType extends Enumeration {
  type UnitType = Value
  val shares = Value("Shares")
  val debentures = Value("Debentures")
}

object InvestorType extends Enumeration {
  type InvestorType = Value
  val investor = Value("Named Investor")
  val nominee = Value("Nominee")
}

object DesSubmissionCSModel {

  def readDesSubmissionCSModel(seisAnswersModel: SEISAnswersModel): DesSubmissionCSModel = {
    DesSubmissionCSModel.apply(None, readDesSubmissionModel(seisAnswersModel))
  }

  def readDesSubmissionModel(seisAnswersModel: SEISAnswersModel): DesSubmissionModel = {
    DesSubmissionModel.apply(None, readDesCorrespondenceDetails(seisAnswersModel.contactDetailsAnswersModel),
      OrganisationType.limited.toString, readDesSubmission(seisAnswersModel))
  }

  def readDesCorrespondenceDetails(contactDetailsAnswersModel: ContactDetailsAnswersModel): DesCorrespondenceDetails = {
    DesCorrespondenceDetails.apply(readContactName(contactDetailsAnswersModel),
      readContactDetails(contactDetailsAnswersModel), readAddressDetails(contactDetailsAnswersModel))
  }

  def readContactName(contactDetailsAnswersModel: ContactDetailsAnswersModel): DesContactName = {
    DesContactName.apply(contactDetailsAnswersModel.contactDetailsModel.forename,
      contactDetailsAnswersModel.contactDetailsModel.surname)
  }

  def readContactDetails(contactDetailsAnswersModel: ContactDetailsAnswersModel): DesContactDetails = {
    DesContactDetails.apply(contactDetailsAnswersModel.contactDetailsModel.telephoneNumber,
      contactDetailsAnswersModel.contactDetailsModel.mobileNumber,
      None,
      Some(contactDetailsAnswersModel.contactDetailsModel.email))
  }

  def readAddressDetails(contactDetailsAnswersModel: ContactDetailsAnswersModel): DesAddressType = {
    DesAddressType.apply(contactDetailsAnswersModel.correspondAddressModel.address.addressline1,
      contactDetailsAnswersModel.correspondAddressModel.address.addressline2,
      contactDetailsAnswersModel.correspondAddressModel.address.addressline3,
      contactDetailsAnswersModel.correspondAddressModel.address.addressline4,
      contactDetailsAnswersModel.correspondAddressModel.address.postcode,
      contactDetailsAnswersModel.correspondAddressModel.address.countryCode)
  }

  def readDesSubmission(seisAnswersModel: SEISAnswersModel): DesSubmission = {
    DesSubmission.apply(None, readDesComplianceStatement(seisAnswersModel))
  }

  def readDesComplianceStatement(seisAnswersModel: SEISAnswersModel): DesComplianceStatement = {
    DesComplianceStatement.apply(SchemeType.seis.toString, readDesTradeModel(seisAnswersModel),
      readDesInvestmentDetailsModel(seisAnswersModel),
      readDesSubsidiaryPerformingTrade(seisAnswersModel), readDesKnowledgeIncentice(seisAnswersModel),
      readDesInvestorDetailsModel(seisAnswersModel), readDesRepaymentsModel(seisAnswersModel),
      readDesValueReceived(seisAnswersModel.investorDetailsAnswersModel),
      readDesOrganisationModel(seisAnswersModel))
  }

  def readDesTradeModel(seisAnswersModel: SEISAnswersModel): DesTradeModel = {
    DesTradeModel.apply(readDesBussinessActivity(seisAnswersModel.companyDetailsAnswersModel),
      readDesBaDescription(seisAnswersModel.companyDetailsAnswersModel),
      readDesMarketInfo(seisAnswersModel), readDesThirtyDayRule(seisAnswersModel),
      readDesTradeDateCommenced(seisAnswersModel.companyDetailsAnswersModel),
      readDesAnnualCostsModel(seisAnswersModel), readDesAnnualTurnoversModel(seisAnswersModel),
      readDesPreviousOwnershipModel(seisAnswersModel))
  }

  def readDesBussinessActivity(companyDetailsAnswersModel: CompanyDetailsAnswersModel): Option[String] = {
    Some(companyDetailsAnswersModel.qualifyBusinessActivityModel.isQualifyBusinessActivity)
  }

  def readDesBaDescription(companyDetailsAnswersModel: CompanyDetailsAnswersModel): String = {
    companyDetailsAnswersModel.natureOfBusinessModel.natureofbusiness
  }

  def readDesMarketInfo(seisAnswersModel: SEISAnswersModel): Option[DesMarketInfo] = {
    None
  }

  def readDesThirtyDayRule(seisAnswersModel: SEISAnswersModel): Option[Boolean] = {
    Some(true)
  }

  def readDesTradeDateCommenced(companyDetailsAnswersModel: CompanyDetailsAnswersModel): String = {
    if(companyDetailsAnswersModel.hasInvestmentTradeStartedModel.isDefined
      && companyDetailsAnswersModel.hasInvestmentTradeStartedModel.get.hasDate)
      Validation.dateToDesFormat(companyDetailsAnswersModel.hasInvestmentTradeStartedModel.get.hasInvestmentTradeStartedDay.get,
        companyDetailsAnswersModel.hasInvestmentTradeStartedModel.get.hasInvestmentTradeStartedMonth.get,
        companyDetailsAnswersModel.hasInvestmentTradeStartedModel.get.hasInvestmentTradeStartedYear.get)
    else
      Constants.standardIgnoreYearValue
  }

  def readDesAnnualCostsModel(seisAnswersModel: SEISAnswersModel): Option[DesAnnualCostsModel] = {
    //DesAnnualCostsModel.apply(None, readDesAnnualCostModel(seisAnswersModel))
    None
  }
  def readDesAnnualCostModel(seisAnswersModel: SEISAnswersModel): Vector[AnnualCostModel] = {
    Vector.empty :+ AnnualCostModel.apply("", readDesOperatingCost(seisAnswersModel), readDesResearchAndDevelopmentCost(seisAnswersModel))
  }

  def readDesOperatingCost(seisAnswersModel: SEISAnswersModel): CostModel = {
    CostModel.apply("")
  }

  def readDesResearchAndDevelopmentCost(seisAnswersModel: SEISAnswersModel): CostModel = {
    CostModel.apply("")
  }
  def readDesAnnualTurnoversModel(seisAnswersModel: SEISAnswersModel): Option[DesAnnualTurnoversModel] = {
    //DesAnnualTurnoversModel.apply(Some("nodata"), re)
    None
  }
  def readDesPreviousOwnershipModel(seisAnswersModel: SEISAnswersModel): Option[DesPreviousOwnershipModel] = {
    None
  }



  def readDesInvestmentDetailsModel(seisAnswersModel: SEISAnswersModel): DesInvestmentDetailsModel = {
    DesInvestmentDetailsModel.apply("growthJustification", readUnitIssueModel(seisAnswersModel),
      readTotalAmountSpent(seisAnswersModel.shareDetailsAnswersModel), readDesOrganisationStatusDetails(seisAnswersModel))
  }

  def readUnitIssueModel(seisAnswersModel: SEISAnswersModel): UnitIssueModel = {
    UnitIssueModel.apply(readShareDescription(seisAnswersModel.shareDetailsAnswersModel),
      readShareDateOfIssue(seisAnswersModel.companyDetailsAnswersModel), UnitType.shares.toString, readNominalValue(),
      seisAnswersModel.shareDetailsAnswersModel.numberOfSharesModel.numberOfShares,
      readTotalAmountRaised(seisAnswersModel.shareDetailsAnswersModel))
  }

  def readShareDescription(shareDetailsAnswersModel: ShareDetailsAnswersModel): String = {
    shareDetailsAnswersModel.shareDescriptionModel.shareDescription
  }

  def readShareDateOfIssue(companyDetailsAnswersModel: CompanyDetailsAnswersModel): String = {
    if(companyDetailsAnswersModel.shareIssueDateModel.day.isDefined)
      Validation.dateToDesFormat(companyDetailsAnswersModel.shareIssueDateModel.day.get,
        companyDetailsAnswersModel.shareIssueDateModel.month.get, companyDetailsAnswersModel.shareIssueDateModel.year.get)
    else
      Constants.standardIgnoreYearValue
  }

  def readNominalValue(): CostModel = {
    CostModel.apply("amount", "GBP")  // Missing in the source model needs to be removed
  }

  def readTotalAmountRaised(shareDetailsAnswersModel: ShareDetailsAnswersModel): CostModel = {
    CostModel.apply(Transformers.poundToPence(Left(shareDetailsAnswersModel.totalAmountRaisedModel.amount.toString())), "GBP")
  }

  def readTotalAmountSpent(shareDetailsAnswersModel: ShareDetailsAnswersModel): Option[CostModel] = {
    if(shareDetailsAnswersModel.totalAmountSpentModel.isDefined)
      Some(CostModel.apply(Transformers.poundToPence(Left(shareDetailsAnswersModel.totalAmountSpentModel.get.totalAmountSpent.toString())), "GBP"))
    else None
  }

  def readDesOrganisationStatusDetails(seisAnswersModel: SEISAnswersModel): Option[DesOrganisationStatusModel] = {
    if(seisAnswersModel.investorDetailsAnswersModel.shareCapitalChangesModel.changesDescription.isDefined)
      Some(DesOrganisationStatusModel.apply(seisAnswersModel.companyDetailsAnswersModel.fullTimeEmployeeCountModel.employeeCount,
        seisAnswersModel.investorDetailsAnswersModel.shareCapitalChangesModel.changesDescription.get,
        CostModel.apply(Transformers.poundToPence(Left(seisAnswersModel.companyDetailsAnswersModel.grossAssetsModel.grossAmount.toString())), "GBP"),
        CostModel.apply("0", "GBP")))
    else
      None
  }

  def readDesSubsidiaryPerformingTrade(seisAnswersModel: SEISAnswersModel): Option[DesSubsidiaryPerformingTrade] = {
    None
  }

  def readDesKnowledgeIncentice(seisAnswersModel: SEISAnswersModel): Option[KiModel] = {
    None
  }

  def readDesInvestorDetailsModel(seisAnswersModel: SEISAnswersModel): DesInvestorDetailsModel = {
    DesInvestorDetailsModel.apply(readDesInvestorModel(seisAnswersModel.investorDetailsAnswersModel))
  }

  def readDesInvestorModel(investorDetailsAnswersModel: InvestorDetailsAnswersModel): Vector[DesInvestorModel] = {
    investorDetailsAnswersModel.investors.foldLeft(Vector.empty[DesInvestorModel]){
      (desInvestorModel , investorDetailsModel) =>
        desInvestorModel :+ DesInvestorModel.apply(readinvestorOrNominee(investorDetailsModel.investorOrNomineeModel.get.addInvestorOrNominee),
        readDesInvestorInfoModel(investorDetailsModel))
    }
  }

  def readinvestorOrNominee(investorOrNominee: String) : String = {
    investorOrNominee match {
      case Constants.nominee => InvestorType.nominee.toString
      case _ => InvestorType.investor.toString
    }
  }

  def readDesInvestorInfoModel(investorDetailsModel: InvestorDetailsModel): DesInvestorInfoModel = {
    DesInvestorInfoModel.apply(readDesCompanyOrIndividualModel(investorDetailsModel),
      investorDetailsModel.numberOfSharesPurchasedModel.get.numberOfSharesPurchased,
      readInvestmentAmount(investorDetailsModel), readDesGroupHoldingsModel(investorDetailsModel))
  }

  def readDesCompanyOrIndividualModel(investorDetailsModel: InvestorDetailsModel) : DesCompanyOrIndividualDetailsModel = {
    investorDetailsModel.companyOrIndividualModel.get.companyOrIndividual match {
      case Constants.typeCompany =>
        DesCompanyOrIndividualDetailsModel.apply(None, readDesCompanyDetailsModel(investorDetailsModel.companyDetailsModel.get))
      case Constants.typeIndividual =>
        DesCompanyOrIndividualDetailsModel.apply(readDesIndividualDetailsModel(investorDetailsModel.individualDetailsModel.get), None)
    }
  }

  def readDesIndividualDetailsModel(individualDetailsModel: IndividualDetailsModel) : Option[DesIndividualDetailsModel] = {
    Some(DesIndividualDetailsModel.apply(readIndividualName(individualDetailsModel),
      readIndividualAddress(individualDetailsModel)))
  }

  def readIndividualName(individualDetailsModel: IndividualDetailsModel): DesContactName = {
    DesContactName.apply(individualDetailsModel.forename, individualDetailsModel.surname)
  }

  def readIndividualAddress(individualDetailsModel: IndividualDetailsModel): DesAddressType = {
    DesAddressType.apply(individualDetailsModel.addressline1, individualDetailsModel.addressline2,
      individualDetailsModel.addressline3, individualDetailsModel.addressline4,
      individualDetailsModel.postcode,individualDetailsModel.countryCode)
  }

  def readDesCompanyDetailsModel(companyDetailsModel: CompanyDetailsModel): Option[DesCompanyDetailsModel] = {
    Some(DesCompanyDetailsModel.apply(companyDetailsModel.companyName, None, None,
      readCompanyAddress(companyDetailsModel)))
  }

  def readCompanyAddress(companyDetailsModel: CompanyDetailsModel): Option[DesAddressType] = {
    Some(DesAddressType.apply(companyDetailsModel.companyAddressline1, companyDetailsModel.companyAddressline2,
      companyDetailsModel.companyAddressline3, companyDetailsModel.companyAddressline4,
      companyDetailsModel.companyPostcode, companyDetailsModel.countryCode))
  }

  def readInvestmentAmount(investorDetailsModel: InvestorDetailsModel): CostModel = {
    CostModel.apply(Transformers.poundToPence(Left(investorDetailsModel.amountSpentModel.get.amount.toString())), "GBP")
  }

  def readDesGroupHoldingsModel(investorDetailsModel: InvestorDetailsModel): Option[DesGroupHoldingsModel] = {
    if(investorDetailsModel.previousShareHoldingModels.isDefined)
      Some(DesGroupHoldingsModel.apply(None, readPreviousGroupHoldings(investorDetailsModel.previousShareHoldingModels.get)))
    else None
  }

  def readPreviousGroupHoldings(previousShareHoldingModels: Vector[PreviousShareHoldingModel]): Vector[UnitIssueModel] = {
    previousShareHoldingModels.foldLeft(Vector.empty[UnitIssueModel]){
      (unitIssueModel , shareHoldings) => unitIssueModel :+ UnitIssueModel.apply(
        readShareHoldingDescription(shareHoldings.previousShareHoldingDescriptionModel.get),
        readShareHoldingDateOfIssue(shareHoldings.investorShareIssueDateModel.get),
        UnitType.shares.toString,
        readShareHoldingNominalValue(shareHoldings.previousShareHoldingNominalValueModel.get),
        shareHoldings.numberOfPreviouslyIssuedSharesModel.get.previouslyIssuedShares,
        CostModel.apply("0", "GBP"))
    }
  }

  def readShareHoldingDescription(previousShareHoldingDescriptionModel: PreviousShareHoldingDescriptionModel): String = {
    previousShareHoldingDescriptionModel.description
  }

  def readShareHoldingDateOfIssue(investorShareIssueDateModel: InvestorShareIssueDateModel): String = {
    if(investorShareIssueDateModel.investorShareIssueDateDay.isDefined)
      Validation.dateToDesFormat(investorShareIssueDateModel.investorShareIssueDateDay.get,
        investorShareIssueDateModel.investorShareIssueDateMonth.get, investorShareIssueDateModel.investorShareIssueDateYear.get)
    else Constants.standardIgnoreYearValue
  }

  def readShareHoldingNominalValue(previousShareHoldingNominalValueModel: PreviousShareHoldingNominalValueModel): CostModel = {
    CostModel.apply(Transformers.poundToPence(Left(previousShareHoldingNominalValueModel.nominalValue.toString())), "GBP")
  }

  def readDesRepaymentsModel(seisAnswersModel: SEISAnswersModel) : DesRepaymentsModel = {
    DesRepaymentsModel.apply(Vector.empty)
  }

  def readDesValueReceived(investorDetailsAnswersModel: InvestorDetailsAnswersModel): Option[String] = {
    investorDetailsAnswersModel.valueReceivedModel.aboutValueReceived
  }

  def readDesOrganisationModel(seisAnswersModel: SEISAnswersModel): DesOrganisationModel = {
    DesOrganisationModel.apply(None, None, readDateOfIncorporation(seisAnswersModel.companyDetailsAnswersModel),
      None, readDesOrganisationDetails(seisAnswersModel), readPreviousRFICostModel(seisAnswersModel))
  }

  def readDateOfIncorporation(companyDetailsAnswersModel: CompanyDetailsAnswersModel) : String = {
    if(companyDetailsAnswersModel.dateOfIncorporationModel.day.isDefined)
      Validation.dateToDesFormat(companyDetailsAnswersModel.dateOfIncorporationModel.day.get,
        companyDetailsAnswersModel.dateOfIncorporationModel.month.get, companyDetailsAnswersModel.dateOfIncorporationModel.year.get)
    else Constants.standardIgnoreYearValue
  }

  def readDesOrganisationDetails(seisAnswersModel: SEISAnswersModel): DesCompanyDetailsModel = {
    DesCompanyDetailsModel.apply("", None, None, None)
  }

  def readPreviousRFICostModel(seisAnswersModel: SEISAnswersModel) : Option[DesRFICostsModel] = {
    Some(DesRFICostsModel.apply(None, readPreviousRFI(seisAnswersModel.previousSchemesAnswersModel)))
  }

  def readPreviousRFI(previousSchemesAnswersModel: PreviousSchemesAnswersModel) : Vector[DesRFIModel] = {
    if (previousSchemesAnswersModel.previousSchemeModel.isDefined
      && previousSchemesAnswersModel.previousSchemeModel.nonEmpty) {
      previousSchemesAnswersModel.previousSchemeModel.get.foldLeft(Vector.empty[DesRFIModel]) {
      (desRFIModel, previousSchemeModel) =>
        desRFIModel :+ DesRFIModel.apply(previousSchemeModel.schemeTypeDesc, previousSchemeModel.otherSchemeName,
          readPreviousRFIIssueDate(previousSchemeModel),
          CostModel.apply(Transformers.poundToPence(Left(previousSchemeModel.investmentAmount.toString)), "GBP"),
          readPreviousSchemesInvestmentAmountSpent(previousSchemeModel))
      }
    }
    else
      Vector.empty
  }

  def readPreviousRFIIssueDate(previousSchemeModel: PreviousSchemeModel): String = {
    if(previousSchemeModel.day.isDefined && previousSchemeModel.month.isDefined
        && previousSchemeModel.year.isDefined)
      Validation.dateToDesFormat(previousSchemeModel.day.get, previousSchemeModel.month.get, previousSchemeModel.year.get)
    else Constants.standardIgnoreYearValue
  }

  def readPreviousSchemesInvestmentAmountSpent(previousSchemeModel: PreviousSchemeModel): Option[CostModel] = {
    if(previousSchemeModel.investmentSpent.isDefined)
      Some(CostModel.apply(Transformers.poundToPence(Left(previousSchemeModel.investmentSpent.get.toString)),"GBP"))
    else
      None
  }

  def answerToBoolean(input:String): Boolean = {
    input.toLowerCase match {
      case "yes" => true
      case _ => false
    }
  }
}