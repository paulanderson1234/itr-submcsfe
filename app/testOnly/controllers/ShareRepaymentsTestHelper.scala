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
import models.repayments._
import testOnly.controllers.TestDataGenerator.randomAlphaString
import TestDataGenerator._

object ShareRepaymentsTestHelper extends ShareRepaymentsTestHelper {

}

trait ShareRepaymentsTestHelper {

  def getShareRepayments(numberToCreate: Int, includeIncompleteShareRepayment: Boolean = false): Vector[SharesRepaymentDetailsModel] = {
    (for (shareRepaymentId <- 1 to numberToCreate) yield getShareRepaymentForList(shareRepaymentId
      , numberToCreate, includeIncompleteShareRepayment, numberToCreate == shareRepaymentId)).toVector
  }

  private def getShareRepaymentForList(shareRepaymentId: Int = 1, numberToCreate: Int = 1,
                                       includeIncompleteShareRepayment: Boolean = false,
                                       isLastShareRepayment: Boolean = false): SharesRepaymentDetailsModel = {

    val makeIncomplete = includeIncompleteShareRepayment && shareRepaymentId == numberToCreate

    SharesRepaymentDetailsModel(
      whoRepaidSharesModel = if(makeIncomplete) None else Some(WhoRepaidSharesModel(randomAlphaString(Constants.forenameLength),
        randomAlphaString(Constants.surnameLength), Some(shareRepaymentId))),
      sharesRepaymentTypeModel = if(makeIncomplete) None else Some(SharesRepaymentTypeModel(randomRepaymentType(
        randomWholeAmount(Integer.MAX_VALUE)), Some(shareRepaymentId))),
      dateSharesRepaidModel = if (makeIncomplete) None else Some(randomDateSharesRepaid(shareRepaymentId)),
      amountSharesRepaymentModel = if(makeIncomplete) None else
        Some(AmountSharesRepaymentModel(randomWholeAmount(utils.Validation.financialMaxAmountLength),Some(shareRepaymentId))),
      processingId = Some(shareRepaymentId))
  }

}
