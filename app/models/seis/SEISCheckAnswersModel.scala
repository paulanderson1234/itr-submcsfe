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

package models.seis

import models._
import models.investorDetails.InvestorDetailsModel

case class SEISCheckAnswersModel(
                                  registeredAddressModel: Option[RegisteredAddressModel],
                                  dateOfIncorporationModel: Option[DateOfIncorporationModel],
                                  natureOfBusinessModel: Option[NatureOfBusinessModel],
                                  previousSchemes: Vector[PreviousSchemeModel],
                                  contactDetailsModel: Option[ContactDetailsModel],
                                  contactAddressModel: Option[AddressModel],
                                  qualifyBusinessActivity: Option[QualifyBusinessActivityModel],
                                  hasInvestmentTradeStarted: Option[HasInvestmentTradeStartedModel],
                                  isSeventyPercentSpent: Option[SeventyPercentSpentModel],
                                  shareIssueDate: Option[ShareIssueDateModel],
                                  grossAssets: Option[GrossAssetsModel],
                                  fullTimeEmployees: Option[FullTimeEmployeeCountModel],
                                  shareDescription: Option[ShareDescriptionModel],
                                  numberOfShares: Option[NumberOfSharesModel],
                                  totalAmountRaised: Option[TotalAmountRaisedModel],
                                  totalAmountSpent: Option[TotalAmountSpentModel],
                                  investorDetails: Option[Vector[InvestorDetailsModel]],
                                  valueReceived: Option[WasAnyValueReceivedModel],
                                  shareCapitalChanges: Option[ShareCapitalChangesModel],
                                  supportingDocumentsUpload: Option[SupportingDocumentsUploadModel]
                                )
