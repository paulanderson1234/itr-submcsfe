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

  import GrossAssetsAfterIssueModel._
  import Constants._

  def grossAssetsAfterIssueAmountBand()(implicit messages: Messages): String = {

    grossAmount.toString() match{
      case `grossAssetsBandOne` => Messages("page.grossAssets.option.up.to", getAmountAsFormattedString(grossAssetsBandOne.toInt))
      case `grossAssetsBandTwo` => Messages("page.grossAssets.option.band",
        getAmountAsFormattedString(grossAssetsBandOne.toInt + 1) , getAmountAsFormattedString(grossAssetsBandTwo.toInt))
      case `grossAssetsBandThree` => Messages("page.grossAssets.option.band",
        getAmountAsFormattedString(grossAssetsBandTwo.toInt + 1), getAmountAsFormattedString(grossAssetsBandThree.toInt))
      case `grossAssetsAfterIssueBandFour` => Messages("page.grossAssets.option.band",
        getAmountAsFormattedString(grossAssetsBandThree.toInt + 1), getAmountAsFormattedString(grossAssetsAfterIssueBandFour.toInt))
      case `grossAssetsAfterIssueBandFive` => Messages("page.grossAssets.option.more.than",
        getAmountAsFormattedString(grossAssetsAfterIssueBandFour.toInt))
    }
  }
}


object GrossAssetsAfterIssueModel extends CostFormatter{

  implicit val format = Json.format[GrossAssetsAfterIssueModel]
  implicit val writes = Json.writes[GrossAssetsAfterIssueModel]
}
