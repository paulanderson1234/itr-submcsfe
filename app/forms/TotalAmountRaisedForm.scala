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

import utils.Transformers._
import utils.Validation._
import models.TotalAmountRaisedModel
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

object TotalAmountRaisedForm {

  val maxAllowableAmount: Int = 999999999
  val minAllowableAmount: Int = 1

  val totalAmountRaisedForm = Form(
    mapping(
      "amount" -> text
        .verifying(Messages("validation.common.error.fieldRequired"), mandatoryCheck)
        .verifying(Messages("page.shareDetails.totalAmountRaised.invalidAmount"), integerCheck)
        .transform[Int](stringToInteger, _.toString())
        .verifying(Messages("page.shareDetails.totalAmountRaised.OutOfRange"), minIntCheck(minAllowableAmount))
        .verifying(Messages("page.shareDetails.totalAmountRaised.OutOfRange"), maxIntCheck(maxAllowableAmount))
    )(TotalAmountRaisedModel.apply)(TotalAmountRaisedModel.unapply)
  )
}
