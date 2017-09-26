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

package models

import models.investorDetails.InvestorDetailsModel
import models.repayments.{AnySharesRepaymentModel, SharesRepaymentDetailsModel}

case class CheckAnswersModel(
                              registeredAddressModel: Option[RegisteredAddressModel],
                              dateOfIncorporationModel: Option[DateOfIncorporationModel],
                              natureOfBusinessModel: Option[NatureOfBusinessModel],
                              commercialSaleModel: Option[CommercialSaleModel],
                              isCompanyKnowledgeIntensiveModel: Option[IsCompanyKnowledgeIntensiveModel],
                              isKnowledgeIntensiveModel: Option[IsKnowledgeIntensiveModel],
                              operatingCostsModel: Option[OperatingCostsModel],
                              percentageStaffWithMastersModel: Option[PercentageStaffWithMastersModel],
                              tenYearPlanModel: Option[TenYearPlanModel],
                              hadPreviousRFIModel: Option[HadPreviousRFIModel],
                              previousSchemes: Vector[PreviousSchemeModel],
                              totalAmountRaisedModel: Option[TotalAmountRaisedModel],
                              thirtyDayRuleModel: Option[ThirtyDayRuleModel],
                              anySharesRepaymentModel : Option[AnySharesRepaymentModel],
                              newGeographicalMarketModel: Option[NewGeographicalMarketModel],
                              newProductModel: Option[NewProductModel],
                              contactDetailsModel: Option[ContactDetailsModel],
                              contactAddressModel: Option[AddressModel],
                              investmentGrowModel: Option[InvestmentGrowModel],
                              qualifyBusinessActivity: Option[QualifyBusinessActivityModel],
                              hasInvestmentTradeStarted: Option[HasInvestmentTradeStartedModel],
                              shareIssueDate: Option[ShareIssueDateModel],
                              grossAssets: Option[GrossAssetsModel],
                              fullTimeEmployees: Option[FullTimeEmployeeCountModel],
                              shareDescription: Option[ShareDescriptionModel],
                              numberOfShares: Option[NumberOfSharesModel],
                              investorDetails: Option[Vector[InvestorDetailsModel]],
                              valueReceived: Option[WasAnyValueReceivedModel],
                              shareCapitalChanges: Option[ShareCapitalChangesModel],
                              marketDescription: Option[MarketDescriptionModel],
                              repaymentDetails: Option[Vector[SharesRepaymentDetailsModel]],
                              grossAssetsAfterIssue: Option[GrossAssetsAfterIssueModel],
                              turnoverCosts: Option[AnnualTurnoverCostsModel],
                              researchStartDateModel: Option[ResearchStartDateModel],
                              attachmentsEnabled: Boolean
                              )
