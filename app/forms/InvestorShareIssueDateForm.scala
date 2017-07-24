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


import models.investorDetails._
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages
import utils.Validation._
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

object InvestorShareIssueDateForm {

  val investorShareIssueDateForm = Form(
    mapping(
      "investorShareIssueDateDay" -> optional(number),
      "investorShareIssueDateMonth" -> optional(number),
      "investorShareIssueDateYear" -> optional(number)
    )(InvestorShareIssueDateModel.apply)(InvestorShareIssueDateModel.unapply)
      .verifying(Messages("validation.error.DateNotEntered"), x =>
        validateNonEmptyDateOptions(x.investorShareIssueDateDay, x.investorShareIssueDateMonth, x.investorShareIssueDateYear))
      .verifying(Messages("common.date.error.invalidDate"), fields =>
        isValidDateOptions(fields.investorShareIssueDateDay, fields.investorShareIssueDateMonth, fields.investorShareIssueDateYear))
      .verifying(Messages("validation.error.ShareIssueDate.Future"), fields =>
        dateNotInFutureOptions(fields.investorShareIssueDateDay, fields.investorShareIssueDateMonth, fields.investorShareIssueDateYear))
  )
}