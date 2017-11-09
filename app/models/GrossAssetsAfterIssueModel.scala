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
import play.api.i18n.Messages
import play.api.libs.json.Json
import utils.CostFormatter

case class GrossAssetsAfterIssueModel(grossAmount : BigDecimal){
  def grossAssetsAfterIssueAmountBand()(implicit messages: Messages): String = {
    grossAmount.toString() match{
      case Constants.grossAssetsBandOne => Messages("page.grossAssetsAfterIssue.option.one")
      case Constants.grossAssetsBandTwo => Messages("page.grossAssetsAfterIssue.option.two")
      case Constants.grossAssetsBandThree => Messages("page.grossAssetsAfterIssue.option.three")
      case Constants.grossAssetsAfterIssueBandFour => Messages("page.grossAssetsAfterIssue.option.four")
      case Constants.grossAssetsAfterIssueBandFive => Messages("page.grossAssetsAfterIssue.option.five")
    }
  }
}


object GrossAssetsAfterIssueModel extends CostFormatter{

  implicit val format = Json.format[GrossAssetsAfterIssueModel]
  implicit val writes = Json.writes[GrossAssetsAfterIssueModel]
}
