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

package forms

import models.repayments.DateSharesRepaidModel
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages
import utils.Validation._
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

object DateSharesRepaidForm {

  val messageKey = "dateSharesRepaid"

  val dateSharesRepaidForm = Form(
    mapping(
      "dateSharesRepaidDay" -> optional(number),
      "dateSharesRepaidMonth" -> optional(number),
      "dateSharesRepaidYear" -> optional(number),
      "processingId" -> optional(number)
    )(DateSharesRepaidModel.apply)(DateSharesRepaidModel.unapply)
      .verifying(Messages("validation.error.DateNotEntered"), fields =>
        validateNonEmptyDateOptions(fields.day, fields.month, fields.year))
      .verifying(Messages("common.date.error.invalidDate"), fields =>
        isValidDateOptions(fields.day, fields.month, fields.year))
      .verifying(Messages(s"validation.error.$messageKey.Future"), fields =>
        dateNotInFutureOptions(fields.day, fields.month, fields.year))
  )
}
