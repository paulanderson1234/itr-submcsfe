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
import play.api.i18n.Messages
import utils.CostFormatter

case class GrossAssetsModel(grossAmount : BigDecimal){

  import GrossAssetsModel._
  import Constants._

  def grossAssetsAmountBand()(implicit messages: Messages): String = {

    grossAmount.toString() match{
      case `grossAssetsBandOne` => Messages("page.grossAssets.option.up.to", getAmountAsFormattedString(grossAssetsBandOne.toInt))
      case `grossAssetsBandTwo` => Messages("page.grossAssets.option.band",
        getAmountAsFormattedString(grossAssetsBandOne.toInt + 1) , getAmountAsFormattedString(grossAssetsBandTwo.toInt))
      case `grossAssetsBandThree` => Messages("page.grossAssets.option.band",
        getAmountAsFormattedString(grossAssetsBandTwo.toInt + 1), getAmountAsFormattedString(grossAssetsBandThree.toInt))
      case `grossAssetsBandFour` => Messages("page.grossAssets.option.band",
        getAmountAsFormattedString(grossAssetsBandThree.toInt + 1), getAmountAsFormattedString(grossAssetsBandFour.toInt))
      case `grossAssetsBandFive` => Messages("page.grossAssets.option.more.than",
        getAmountAsFormattedString(grossAssetsBandFour.toInt))
      case `grossAssetsSEISBandOne` => Messages("page.grossAssets.option.up.to", getAmountAsFormattedString(grossAssetsSEISBandOne.toInt))
      case `grossAssetsSEISBandTwo` => Messages("page.grossAssets.option.band",
        getAmountAsFormattedString(grossAssetsSEISBandOne.toInt + 1) , getAmountAsFormattedString(grossAssetsSEISBandTwo.toInt))
      case `grossAssetsSEISBandThree` => Messages("page.grossAssets.option.more.than", getAmountAsFormattedString(grossAssetsSEISBandTwo.toInt))
    }
  }
}

object GrossAssetsModel extends CostFormatter{
  implicit val format = Json.format[GrossAssetsModel]
  implicit val writes = Json.writes[GrossAssetsModel]
}
