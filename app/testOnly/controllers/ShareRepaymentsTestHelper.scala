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

import models.repayments._

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
      whoRepaidSharesModel = if(makeIncomplete) None else Some(WhoRepaidSharesModel(TestDataGenerator.randomAlphaString(3),
        TestDataGenerator.randomAlphaString(3), Some(shareRepaymentId))),
      sharesRepaymentTypeModel = if(makeIncomplete) None else Some(SharesRepaymentTypeModel(TestDataGenerator.randomWordString(3), Some(shareRepaymentId))),
      dateSharesRepaidModel = if (makeIncomplete) None else Some(DateSharesRepaidModel(Some(1), Some(2), Some(1990), Some(shareRepaymentId))),
      amountSharesRepaymentModel = if(makeIncomplete) None else Some(AmountSharesRepaymentModel(TestDataGenerator.randomDecimalAmount,Some(shareRepaymentId))),
      processingId = Some(shareRepaymentId))
  }

}
