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

case class DesSubmissionCSModel (
                                  acknowledgementReference: Option[String] = None,
                                  submissionType: DesSubmissionType
                                )

case class DesSubmissionType(
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
                                   trade: DesTradeModel,
                                   investment: DesInvestmentDetailsModel, // Not required for CS but required in DES scheme
                                   subsidiaryPerformingTrade: Option[DesSubsidiaryPerformingTrade],
                                   knowledgeIntensive: Option[KiModel],
                                   investorsDetails: DesInvestorDetailsModel,
                                   repayments: DesRepaymentsModel,
                                   valueReceived: Option[String],
                                   organisation: DesOrganisationModel
                                 )

case class DesTradeModel(
                          businessActivity: Option[String],
                          baDescription: String,
                          marketInfo: Option[DesMarketInfo],
                          thirtyDayRule: Option[Boolean],
                          dateTradeCommenced: String,
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
                                      growthJustification: String,
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
                                 existingGroupHoldings: DesGroupHoldingsModel
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
                     amount: DesCostModel,
                     amountSpent: Option[CostModel]
                   )