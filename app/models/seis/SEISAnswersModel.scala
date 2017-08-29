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

case class SEISAnswersModel(
                             natureOfBusinessModel: NatureOfBusinessModel,
                             dateOfIncorporationModel: DateOfIncorporationModel,
                             qualifyBusinessActivityModel: QualifyBusinessActivityModel,
                             researchStartDateModel: Option[ResearchStartDateModel],
                             seventyPercentSpentModel: Option[SeventyPercentSpentModel],
                             shareIssueDateModel: ShareIssueDateModel,
                             grossAssetsModel: GrossAssetsModel,
                             fullTimeEmployeeCountModel: FullTimeEmployeeCountModel,
                             hadPreviousRFIModel: HadPreviousRFIModel,
                             otherInvestmentsModel: HadOtherInvestmentsModel,
                             previousSchemeModel: Option[List[PreviousSchemeModel]],
                             shareDescriptionModel: ShareDescriptionModel,
                             numberOfSharesModel: NumberOfSharesModel,
                             totalAmountRaisedModel: TotalAmountRaisedModel,
                             totalAmountSpentModel: Option[TotalAmountSpentModel],
                             investors: List[InvestorDetailsModel],
                             valueReceivedModel: WasAnyValueReceivedModel,
                             shareCapitalChangesModel: ShareCapitalChangesModel,
                             contactDetailsModel: ContactDetailsModel,
                             correspondAddressModel: ConfirmCorrespondAddressModel,
                             supportingDocumentsUploadModel: SupportingDocumentsUploadModel
                           )
