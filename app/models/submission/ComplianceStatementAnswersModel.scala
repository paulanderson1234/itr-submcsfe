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
import connectors.SubmissionConnector
import models.{IsKnowledgeIntensiveModel, _}
import models.investorDetails.InvestorDetailsModel
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class ComplianceStatementAnswersModel(companyDetailsAnswersModel: CompanyDetailsAnswersModel,
                                           previousSchemesAnswersModel: PreviousSchemesAnswersModel,
                                           shareDetailsAnswersModel: ShareDetailsAnswersModel,
                                           investorDetailsAnswersModel: InvestorDetailsAnswersModel,
                                           contactDetailsAnswersModel: ContactDetailsAnswersModel,
                                           supportingDocumentsUploadModel: SupportingDocumentsUploadModel,
                                           schemeTypes: SchemeTypesModel,
                                           kiAnswersModel: Option[KiAnswersModel] = None,
                                           marketInfo: Option[MarketInfoAnswersModel] = None,
                                           costsAnswersModel: CostsAnswerModel

                                          ) {


  def validateSeis(submissionConnector: SubmissionConnector)(implicit hc: HeaderCarrier): Future[Boolean] = {
    for {
      companyCheck <- companyDetailsAnswersModel.validate(submissionConnector)
      shareCheck <- shareDetailsAnswersModel.validate(companyDetailsAnswersModel.qualifyBusinessActivityModel,
      companyDetailsAnswersModel.hasInvestmentTradeStartedModel, companyDetailsAnswersModel.researchStartDateModel, submissionConnector)
    } yield companyCheck && previousSchemesAnswersModel.validate && shareCheck && investorDetailsAnswersModel.validate
  }

  //TODO: implement this validate method fully to validate what is required for EIS when story  is played
  def validateEis(submissionConnector: SubmissionConnector)(implicit hc: HeaderCarrier): Future[Boolean] = {
    for {
      companyCheck <- companyDetailsAnswersModel.validate(submissionConnector)
      shareCheck <- shareDetailsAnswersModel.validate(companyDetailsAnswersModel.qualifyBusinessActivityModel,
      companyDetailsAnswersModel.hasInvestmentTradeStartedModel, companyDetailsAnswersModel.researchStartDateModel, submissionConnector)
    } yield companyCheck && previousSchemesAnswersModel.validate && shareCheck && investorDetailsAnswersModel.validate
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
                                      fullTimeEmployeeCountModel: FullTimeEmployeeCountModel,
                                      commercialSaleModel:Option[CommercialSaleModel]) {

  def validate(submissionConnector: SubmissionConnector)(implicit hc: HeaderCarrier): Future[Boolean] = {

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
      case Constants.qualifyPrepareToTrade => hasInvestmentTradeStartedModel.flatMap(validateTrade).getOrElse(Future.successful(false))
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

case class ShareDetailsAnswersModel(shareDescriptionModel: ShareDescriptionModel,
                                    numberOfSharesModel: NumberOfSharesModel,
                                    totalAmountRaisedModel: TotalAmountRaisedModel,
                                    totalAmountSpentModel: Option[TotalAmountSpentModel]) {
  def validate(qualifyBusinessActivityModel: QualifyBusinessActivityModel,
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
      case Constants.qualifyPrepareToTrade => hasInvestmentTradeStartedModel.flatMap(validateTrade).getOrElse(Future.successful(false))
      case _ => Future.successful(false)
    }
  }
}

case class InvestorDetailsAnswersModel(investors: Vector[InvestorDetailsModel],
                                       valueReceivedModel: WasAnyValueReceivedModel,
                                       shareCapitalChangesModel: ShareCapitalChangesModel) {
  def validate: Boolean = investors.forall(_.validate)
}

case class ContactDetailsAnswersModel(contactDetailsModel: ContactDetailsModel,
                                      correspondAddressModel: ConfirmCorrespondAddressModel)

case class MarketInfoAnswersModel(newGeographicMarket: NewGeographicalMarketModel,
                                   newProductMarket: NewProductModel,
                                   marketDescription: Option[MarketDescriptionModel]){
}

case class KiAnswersModel(kiProcessingModel: KiProcessingModel,
                          tenYearPlanModel: Option[TenYearPlanModel]){
}

case class CostsAnswerModel(operatingCosts: Option[OperatingCostsModel],
                            turnoverCostModel: Option[AnnualTurnoverCostsModel]) {

}
