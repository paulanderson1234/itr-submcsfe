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

import auth.TAVCUser
import common.{Constants, KeystoreKeys}
import connectors.SubmissionConnector
import models._
import models.investorDetails.InvestorDetailsModel
import models.repayments.{AnySharesRepaymentModel, SharesRepaymentDetailsModel}
import uk.gov.hmrc.play.http.HeaderCarrier
import connectors.S4LConnector
import controllers.Helpers.TotalAmountRaisedHelper

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class ComplianceStatementAnswersModel(companyDetailsAnswersModel: CompanyDetailsAnswersModel,
                                           previousSchemesAnswersModel: PreviousSchemesAnswersModel,
                                           shareDetailsAnswersModel: ShareDetailsAnswersModel,
                                           investorDetailsAnswersModel: InvestorDetailsAnswersModel,
                                           contactDetailsAnswersModel: ContactDetailsAnswersModel,
                                           supportingDocumentsUploadModel: SupportingDocumentsUploadModel,
                                           schemeTypes: SchemeTypesModel,
                                           kiAnswersModel: Option[KiAnswersModel],
                                           marketInfo: Option[MarketInfoAnswersModel],
                                           costsAnswersModel: CostsAnswerModel,
                                           thirtyDayRuleAnswersModel: Option[ThirtyDayRuleAnswersModel],
                                           investmentGrow: Option[InvestmentGrowAnswersModel],
                                           subsidiaries: Option[SubsidiariesAnswersModel],
                                           repaidSharesAnswersModel: Option[RepaidSharesAnswersModel]

                                          ) {


  def validateSeis(submissionConnector: SubmissionConnector)(implicit hc: HeaderCarrier): Future[Boolean] = {
    for {
      companyCheck <- companyDetailsAnswersModel.validateSeis(submissionConnector)
      shareCheck <- shareDetailsAnswersModel.validateSeis(companyDetailsAnswersModel.qualifyBusinessActivityModel,
        companyDetailsAnswersModel.hasInvestmentTradeStartedModel, companyDetailsAnswersModel.researchStartDateModel, submissionConnector)
    } yield companyCheck && previousSchemesAnswersModel.validate && shareCheck && investorDetailsAnswersModel.validate
  }

  def validateEis(submissionConnector: SubmissionConnector, s4lConnector:S4LConnector)(implicit hc: HeaderCarrier, user: TAVCUser): Future[Boolean] = {
    for {
      companyCheck <- companyDetailsAnswersModel.validateEis(submissionConnector)
      marketCheck <- marketAnswersCheck(s4lConnector)
      shareCheck <- shareDetailsAnswersModel.validateEis(companyDetailsAnswersModel.qualifyBusinessActivityModel,
        companyDetailsAnswersModel.hasInvestmentTradeStartedModel, companyDetailsAnswersModel.researchStartDateModel, submissionConnector)
    } yield companyCheck && marketCheck && previousSchemesAnswersModel.validate && shareCheck && investorDetailsAnswersModel.validate  &&
      repaidSharesAnswersModel.fold(true)(_.validate) && kiAnswersModel.fold(true)(_.validateEis) && marketInfo.fold(true)(_.validateEis(costsAnswersModel, thirtyDayRuleAnswersModel))
  }

  private def marketAnswersCheck(s4lConnector: S4LConnector)(implicit hc: HeaderCarrier, user: TAVCUser): Future[Boolean] = {

    val isMarketRouteApplicable = TotalAmountRaisedHelper.checkIfMarketInfoApplies(s4lConnector)
    val prevDofcs = s4lConnector.fetchAndGetFormData[PreviousBeforeDOFCSModel](KeystoreKeys.previousBeforeDOFCS)
    val usedInvestmentReasonBefore = s4lConnector.fetchAndGetFormData[UsedInvestmentReasonBeforeModel](KeystoreKeys.usedInvestmentReasonBefore)




    def checkMarketInfo(prevDofcs: Option[PreviousBeforeDOFCSModel],
                        usedInvestmentReasonBefore: Option[UsedInvestmentReasonBeforeModel],
                        isMarketRouteApplicable:MarketRoutingCheckResult): Future[Boolean] = {

      // if this is a 'usedInvestmentReasonBefore' route - need to validate the usedInvestmentReasonBefore and
      // prevDofcs questions to see if they are populated and have valid answers
      if (isMarketRouteApplicable.reasonBeforeValidationRequired) usedInvestmentReasonBefore match {
        case None => Future.successful(false) // should not be empty for market info route
        case Some(data) if data.usedInvestmentReasonBefore == Constants.StandardRadioButtonNoValue => modelCheck(isMarketRouteApplicable)
        case Some(data) if data.usedInvestmentReasonBefore == Constants.StandardRadioButtonYesValue => modelCheckPrevDocNonEmpty(isMarketRouteApplicable, prevDofcs)
        case _ => modelCheck(isMarketRouteApplicable)
      } else modelCheck(isMarketRouteApplicable)
    }

    def modelCheck(isMarketRouteApplicable:MarketRoutingCheckResult): Future[Boolean] = {
      // if this is a route where market info should be populated the marketInfoAnswersModel should not be None
      if (isMarketRouteApplicable.isMarketInfoRoute) Future.successful(marketInfo.nonEmpty) else Future.successful(true)
    }

    def modelCheckPrevDocNonEmpty(isMarketRouteApplicable:MarketRoutingCheckResult, prevDofcs: Option[PreviousBeforeDOFCSModel]): Future[Boolean] = {
      // if used reason before is 'Yes' the prevDofcsBefore question should have been answered
      if(prevDofcs.isEmpty) Future.successful(false) else modelCheck(isMarketRouteApplicable)
    }

    for {
      prevDofcsModel <- prevDofcs
      usedInvestmentReasonBeforeModel <- usedInvestmentReasonBefore
      isMarketRouteApplicable <- isMarketRouteApplicable
      isValid <- checkMarketInfo(prevDofcsModel, usedInvestmentReasonBeforeModel, isMarketRouteApplicable)
    } yield isValid

  }
}

case class CompanyDetailsAnswersModel(natureOfBusinessModel: NatureOfBusinessModel,
                                      dateOfIncorporationModel: DateOfIncorporationModel,
                                      qualifyBusinessActivityModel: QualifyBusinessActivityModel,
                                      hasInvestmentTradeStartedModel: Option[HasInvestmentTradeStartedModel],
                                      researchStartDateModel: Option[ResearchStartDateModel],
                                      seventyPercentSpentModel: Option[SeventyPercentSpentModel],
                                      shareIssueDateModel: ShareIssueDateModel,
                                      grossAssetsModel: GrossAssetsModel,
                                      grossAssetsAfterModel: Option[GrossAssetsAfterIssueModel],
                                      fullTimeEmployeeCountModel: FullTimeEmployeeCountModel,
                                      commercialSaleModel:Option[CommercialSaleModel]) {

  def validateSeis(submissionConnector: SubmissionConnector)(implicit hc: HeaderCarrier): Future[Boolean] = {

    def validateStartCondition[T](check: String)(f: Option[Future[Boolean]]) = {
      if(check == Constants.StandardRadioButtonYesValue) f else Some(Future.successful(seventyPercentSpentModel.isDefined))
    }

    val validateSeventyPercentSpent: Option[Boolean] => Boolean = {
      case Some(true) => true
      case _ => seventyPercentSpentModel.isDefined
    }

    val validateResearch: ResearchStartDateModel => Option[Future[Boolean]] = { model =>
      validateStartCondition[ResearchStartDateModel](model.hasStartedResearch) {
        for {
          day <- model.researchStartDay
          month <- model.researchStartMonth
          year <- model.researchStartYear
        } yield submissionConnector.validateHasInvestmentTradeStartedCondition(day, month, year).map(validateSeventyPercentSpent)
      }
    }

    val validateTrade: HasInvestmentTradeStartedModel => Option[Future[Boolean]] = { model =>
      validateStartCondition[HasInvestmentTradeStartedModel](model.hasInvestmentTradeStarted) {
        for {
          day <- model.hasInvestmentTradeStartedDay
          month <- model.hasInvestmentTradeStartedMonth
          year <- model.hasInvestmentTradeStartedYear
        } yield submissionConnector.validateHasInvestmentTradeStartedCondition(day, month, year).map(validateSeventyPercentSpent)
      }
    }

    qualifyBusinessActivityModel.isQualifyBusinessActivity match {
      case Constants.qualifyResearchAndDevelopment => researchStartDateModel.flatMap(validateResearch).getOrElse(Future.successful(false))
      case Constants.qualifyTrade => hasInvestmentTradeStartedModel.flatMap(validateTrade).getOrElse(Future.successful(false))
      case _ => Future.successful(false)
    }
  }

  def validateEis(submissionConnector: SubmissionConnector)(implicit hc: HeaderCarrier): Future[Boolean] = {
    def validateStartCondition[T](check: String)(f: Option[Future[Boolean]]) = {
      if(check == Constants.StandardRadioButtonYesValue) f else Some(Future.successful(commercialSaleModel.isDefined && grossAssetsAfterModel.isDefined))
    }

    val validateApiCheck: Option[Boolean] => Boolean = {
      case Some(true) => commercialSaleModel.isDefined && grossAssetsAfterModel.isDefined
      case _ => false // hard error shouldn't be able to submit
    }

    val validateResearch: ResearchStartDateModel => Option[Future[Boolean]] = { model =>
      validateStartCondition[ResearchStartDateModel](model.hasStartedResearch) {
        for {
          day <- model.researchStartDay
          month <- model.researchStartMonth
          year <- model.researchStartYear
        } yield submissionConnector.validateHasInvestmentTradeStartedCondition(day, month, year).map(validateApiCheck)
      }
    }

    val validateTrade: HasInvestmentTradeStartedModel => Option[Future[Boolean]] = { model =>
      validateStartCondition[HasInvestmentTradeStartedModel](model.hasInvestmentTradeStarted) {
        for {
          day <- model.hasInvestmentTradeStartedDay
          month <- model.hasInvestmentTradeStartedMonth
          year <- model.hasInvestmentTradeStartedYear
        } yield submissionConnector.validateHasInvestmentTradeStartedCondition(day, month, year).map(validateApiCheck)
      }
    }

    qualifyBusinessActivityModel.isQualifyBusinessActivity match {
      case Constants.qualifyResearchAndDevelopment => researchStartDateModel.flatMap(validateResearch).getOrElse(Future.successful(false))
      case Constants.qualifyTrade => hasInvestmentTradeStartedModel.flatMap(validateTrade).getOrElse(Future.successful(false))
      case _ => Future.successful(false)
    }
  }
}

case class PreviousSchemesAnswersModel(hadPreviousRFIModel: HadPreviousRFIModel,
                                       otherInvestmentsModel: HadOtherInvestmentsModel,
                                       previousSchemeModel: Option[List[PreviousSchemeModel]]) {
  def validate: Boolean = {
    if (hadPreviousRFIModel.hadPreviousRFI == Constants.StandardRadioButtonYesValue ||
      otherInvestmentsModel.hadOtherInvestments == Constants.StandardRadioButtonYesValue) previousSchemeModel.exists(_.nonEmpty)
    else true
  }
}

case class RepaidSharesAnswersModel(anySharesRepaymentModel: AnySharesRepaymentModel,
                                    sharesRepaymentDetailsModel: Option[List[SharesRepaymentDetailsModel]]) {
  def validate: Boolean = {
    if (anySharesRepaymentModel.anySharesRepayment == Constants.StandardRadioButtonYesValue)
      sharesRepaymentDetailsModel.exists(list => list.nonEmpty && list.forall(_.validate))
    else true
  }
}


case class ShareDetailsAnswersModel(shareDescriptionModel: ShareDescriptionModel,
                                    numberOfSharesModel: NumberOfSharesModel,
                                    totalAmountRaisedModel: TotalAmountRaisedModel,
                                    totalAmountSpentModel: Option[TotalAmountSpentModel]) {

  def validateSeis(qualifyBusinessActivityModel: QualifyBusinessActivityModel,
                   hasInvestmentTradeStartedModel: Option[HasInvestmentTradeStartedModel],
                   researchStartDateModel: Option[ResearchStartDateModel],
                   submissionConnector: SubmissionConnector)(implicit hc: HeaderCarrier): Future[Boolean] = {

    def validateStartCondition[T](check: String)(f: Option[Future[Boolean]]) = {
      if(check == Constants.StandardRadioButtonYesValue) f else Some(Future.successful(totalAmountSpentModel.isDefined))
    }

    val validateTotalAmountSpent: Option[Boolean] => Boolean = {
      case Some(true) => true
      case _ => totalAmountSpentModel.isDefined
    }

    val validateResearch: ResearchStartDateModel => Option[Future[Boolean]] = { model =>
      validateStartCondition[ResearchStartDateModel](model.hasStartedResearch) {
        for {
          day <- model.researchStartDay
          month <- model.researchStartMonth
          year <- model.researchStartYear
        } yield submissionConnector.validateHasInvestmentTradeStartedCondition(day, month, year).map(validateTotalAmountSpent)
      }
    }

    val validateTrade: HasInvestmentTradeStartedModel => Option[Future[Boolean]] = { model =>
      validateStartCondition[HasInvestmentTradeStartedModel](model.hasInvestmentTradeStarted) {
        for {
          day <- model.hasInvestmentTradeStartedDay
          month <- model.hasInvestmentTradeStartedMonth
          year <- model.hasInvestmentTradeStartedYear
        } yield submissionConnector.validateHasInvestmentTradeStartedCondition(day, month, year).map(validateTotalAmountSpent)
      }
    }

    qualifyBusinessActivityModel.isQualifyBusinessActivity match {
      case Constants.qualifyResearchAndDevelopment => researchStartDateModel.flatMap(validateResearch).getOrElse(Future.successful(false))
      case Constants.qualifyTrade => hasInvestmentTradeStartedModel.flatMap(validateTrade).getOrElse(Future.successful(false))
      case _ => Future.successful(false)
    }
  }

  def validateEis(qualifyBusinessActivityModel: QualifyBusinessActivityModel,
               hasInvestmentTradeStartedModel: Option[HasInvestmentTradeStartedModel],
               researchStartDateModel: Option[ResearchStartDateModel],
               submissionConnector: SubmissionConnector)(implicit hc: HeaderCarrier): Future[Boolean] = {

    // nothing to do here. Need to validate totl raised API call but
    Future.successful(true)

  }
}

case class InvestorDetailsAnswersModel(investors: Vector[InvestorDetailsModel],
                                       valueReceivedModel: WasAnyValueReceivedModel,
                                       shareCapitalChangesModel: ShareCapitalChangesModel) {
  def validate: Boolean = investors.forall(_.validate)
}

case class ContactDetailsAnswersModel(contactDetailsModel: ContactDetailsModel,
                                      correspondAddressModel: AddressModel)

case class MarketInfoAnswersModel(newGeographicMarket: NewGeographicalMarketModel,
                                  newProductMarket: NewProductModel,
                                  marketDescription: Option[MarketDescriptionModel],
                                  isMarketRouteApplicable: MarketRoutingCheckResult,
                                  turnoverApiCheckPassed: Option[Boolean]) {

  def validateEis(costsAnswersModel:CostsAnswerModel, thirtyDayRuleAnswersModel: Option[ThirtyDayRuleAnswersModel]) :Boolean = {

    def validateTurnoverThirtyDay = {
      if (!turnoverApiCheckPassed.fold(true)(_.self)) thirtyDayRuleAnswersModel.nonEmpty else true
    }

    if(newGeographicMarket.isNewGeographicalMarket == Constants.StandardRadioButtonYesValue ||
      newProductMarket.isNewProduct == Constants.StandardRadioButtonYesValue) costsAnswersModel.turnoverCostModel.nonEmpty && validateTurnoverThirtyDay
    else true
  }

}

case class KiAnswersModel(kiProcessingModel: KiProcessingModel,
                          tenYearPlanModel: Option[TenYearPlanModel]){

  def validateEis:Boolean = {

    def isNotMissingDataIfApplyingKi(data: KiProcessingModel): Boolean = {
      data.dateConditionMet.nonEmpty && data.companyAssertsIsKi.nonEmpty &&
        data.companyWishesToApplyKi.nonEmpty && data.costsConditionMet.nonEmpty && data.secondaryCondtionsMet.nonEmpty
    }

    kiProcessingModel.dateConditionMet match{
      case Some(dateConditionMet) if dateConditionMet && kiProcessingModel.companyWishesToApplyKi.fold(false)(_.self)
        && kiProcessingModel.companyAssertsIsKi.fold(false)(_.self) => isNotMissingDataIfApplyingKi(kiProcessingModel)
      case Some(dateConditionMet) => true
      case None => false
    }
  }

}

case class CostsAnswerModel(operatingCosts: Option[OperatingCostsModel],
                            turnoverCostModel: Option[AnnualTurnoverCostsModel]) {

}

case class ThirtyDayRuleAnswersModel(thirtyDayRuleModel: ThirtyDayRuleModel, turnoverApiCheckPassed: Boolean){

}

case class InvestmentGrowAnswersModel(investmentGrowModel: InvestmentGrowModel){

}

case class SubsidiariesAnswersModel(subsidiariesSpendInvest: SubsidiariesSpendingInvestmentModel,
                                    subsidiariesNinetyOwned: SubsidiariesNinetyOwnedModel)

