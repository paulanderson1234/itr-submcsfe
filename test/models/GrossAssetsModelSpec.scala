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
import controllers.helpers.BaseSpec
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._

class GrossAssetsModelSpec extends BaseSpec with MockitoSugar  {

  implicit def stringToIntConverter(string: String): BigDecimal = BigDecimal(string)

  "Calling .grossAssetsAmountBandEIS on GrossAssetsModel" should {

    "return 'Up to £1,000,000' as a string" in {
      GrossAssetsModel(Constants.grossAssetsBandOne).grossAssetsAmountBandEIS() shouldBe Messages("page.grossAssets.option.one")
    }

    "return 'Up to £5,000,000' as a string" in {
      GrossAssetsModel(Constants.grossAssetsBandTwo).grossAssetsAmountBandEIS() shouldBe Messages("page.grossAssets.option.two")
    }

    "return 'Up to £10,000,000' as a string" in {
      GrossAssetsModel(Constants.grossAssetsBandThree).grossAssetsAmountBandEIS() shouldBe Messages("page.grossAssets.option.three")
    }

    "return 'Up to £15,000,000' as a string" in {
      GrossAssetsModel(Constants.grossAssetsBandFour).grossAssetsAmountBandEIS() shouldBe Messages("page.grossAssets.option.four")
    }

    "return 'Up to £15,000,001' as a string" in {
      GrossAssetsModel(Constants.grossAssetsBandFive).grossAssetsAmountBandEIS() shouldBe Messages("page.grossAssets.option.five")
    }


    "throw an Exception when given a non valid number" in {
      intercept[MatchError] {
        GrossAssetsModel("12345").grossAssetsAmountBandEIS()
      }
    }

  }

}
