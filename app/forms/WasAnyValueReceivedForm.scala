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

import common.Constants
import models.WasAnyValueReceivedModel
import play.api.data.Form
import play.api.data.Forms._
import play.api.Play.current
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import utils.Transformers._

object WasAnyValueReceivedForm {

  val wasAnyValueReceivedForm = Form(
    mapping(
      "wasAnyValueReceived" -> nonEmptyText,
      "descriptionTextArea" -> text(maxLength = Constants.shortTextLimit)
        .transform(stringToOptionString, optionStringToString)
    )(WasAnyValueReceivedModel.apply)(WasAnyValueReceivedModel.unapply)
      .verifying(Messages("error.required"), model =>
        if (model.wasAnyValueReceived == Constants.StandardRadioButtonYesValue) model.aboutValueReceived.isDefined else true)
  )
}
