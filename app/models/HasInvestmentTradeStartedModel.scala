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

package models

import common.Constants
import play.api.libs.json.Json
import utils.DateFormatter

case class HasInvestmentTradeStartedModel(hasInvestmentTradeStarted : String, hasInvestmentTradeStartedDay: Option[Int],
                                          hasInvestmentTradeStartedMonth: Option[Int], hasInvestmentTradeStartedYear: Option[Int]) {
                                            val toDate = if(hasInvestmentTradeStartedDay.isDefined && hasInvestmentTradeStartedMonth.isDefined &&
                                              hasInvestmentTradeStartedYear.isDefined) s"${hasInvestmentTradeStartedDay.get}-${hasInvestmentTradeStartedMonth.get}-${hasInvestmentTradeStartedYear.get}"
                                            else ""

  val hasDate = {
    hasInvestmentTradeStarted == Constants.StandardRadioButtonYesValue && hasInvestmentTradeStartedDay.isDefined && hasInvestmentTradeStartedMonth.isDefined &&
      hasInvestmentTradeStartedYear.isDefined
  }

                                          }

object HasInvestmentTradeStartedModel extends DateFormatter{
  implicit val format = Json.format[HasInvestmentTradeStartedModel]
}
