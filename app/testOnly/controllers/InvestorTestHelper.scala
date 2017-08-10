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

package testOnly.controllers

import common.Constants
import models.{AddInvestorOrNomineeModel, CompanyOrIndividualModel}
import models.investorDetails._

object InvestorTestHelper extends InvestorTestHelper {

}

trait InvestorTestHelper {

  def getInvestors(numberToCreate: Int, numberOfShareholdings: Int = 1, includeIncompleteInvestor: Boolean = false,
                   includeIncompleteShareHolding: Boolean = false): Vector[InvestorDetailsModel] = {
    (for (investorId <- 1 to numberToCreate) yield getInvestorForList(investorId, numberToCreate,
      numberOfShareholdings, includeIncompleteInvestor, includeIncompleteShareHolding, numberToCreate == investorId)).toVector
  }

  private def getInvestorForList(investorId: Int = 1, numberToCreate: Int = 1, numberOfShareholdings: Int,
                                 includeIncompleteInvestor: Boolean = false, includeIncompleteShareHolding: Boolean = false,
                                 isLastInvestor: Boolean = false): InvestorDetailsModel = {

    val makeIncomplete = includeIncompleteInvestor && investorId == numberToCreate

    val companyIndividualModel = CompanyOrIndividualModel(TestDataGenerator.randomCompanyOrIndividual(investorId), Some(investorId))
    val isCompany = companyIndividualModel.companyOrIndividual == Constants.typeCompany

    InvestorDetailsModel(
      investorOrNomineeModel = Some(AddInvestorOrNomineeModel(TestDataGenerator.randomInvestorOrNominee(investorId), Some(investorId))),
      companyOrIndividualModel = Some(companyIndividualModel),
      numberOfSharesPurchasedModel = if (makeIncomplete) None else Some(NumberOfSharesPurchasedModel(TestDataGenerator.randomDecimal(investorId), Some(investorId))),
      amountSpentModel = Some(HowMuchSpentOnSharesModel(TestDataGenerator.randomWholeAmount(utils.Validation.financialMaxAmountLength), Some(investorId))),
      companyDetailsModel = if (isCompany) Some(TestDataGenerator.randomCompanyDetails(investorId)) else None,
      individualDetailsModel = if (isCompany) None else Some(TestDataGenerator.randomIndividualDetails(investorId)),
      isExistingShareHolderModel = if (numberOfShareholdings > 0 && !makeIncomplete)
        Some(IsExistingShareHolderModel(Constants.StandardRadioButtonYesValue))
      else Some(IsExistingShareHolderModel(Constants.StandardRadioButtonNoValue)),
      previousShareHoldingModels = if (makeIncomplete || numberOfShareholdings == 0) None
      else Some(getShareholdingsList(numberOfShareholdings, investorId, includeIncompleteShareHolding, investorId == numberToCreate)),
      processingId = Some(investorId))
  }

  private def getShareholdingsList(numberToCreate: Int, investorId: Int, includeIncomplete: Boolean,
                                   isLastInvestor: Boolean = false): Vector[PreviousShareHoldingModel] = {
    (for (index <- 1 to numberToCreate) yield getShareHoldingForList(includeIncomplete, index, investorId, numberToCreate, isLastInvestor)).toVector
  }

  private def getShareHoldingForList(includeIncomplete: Boolean = false, processingId: Int = 1, investorProcessingId: Int = 1,
                                     numberToCreate: Int = 1, isLastInvestor: Boolean = false): PreviousShareHoldingModel = {
    val makeIncomplete = includeIncomplete && processingId == numberToCreate && isLastInvestor

    PreviousShareHoldingModel(
      investorShareIssueDateModel = Some(TestDataGenerator.getRandomInvestorShareIssueDateModel(processingId, investorProcessingId)),
      numberOfPreviouslyIssuedSharesModel = if (makeIncomplete) None
      else Some(NumberOfPreviouslyIssuedSharesModel(TestDataGenerator.randomDecimal(processingId), Some(processingId))),
      previousShareHoldingNominalValueModel =
        Some(PreviousShareHoldingNominalValueModel(TestDataGenerator.randomWholeAmount(utils.Validation.financialMaxAmountLength), Some(processingId))),
      previousShareHoldingDescriptionModel = if (makeIncomplete) None
      else
        Some(PreviousShareHoldingDescriptionModel(TestDataGenerator.randomWordString(Constants.shortTextLimit), processingId = Some(processingId)))
      , processingId = Some(processingId), investorProcessingId = Some(investorProcessingId))
  }

}
