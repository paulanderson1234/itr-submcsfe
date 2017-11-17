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

  "Calling .grossAssetsAmountBand on GrossAssetsModel" should {

    "return 'Up to £1,000,000' as a string" in {
      GrossAssetsModel(Constants.grossAssetsBandOne).grossAssetsAmountBand() shouldBe "Up to £1,000,000"
    }

    "return '£1,000,001 to £5,000,000' as a string" in {
      GrossAssetsModel(Constants.grossAssetsBandTwo).grossAssetsAmountBand() shouldBe "£1,000,001 to £5,000,000"
    }

    "return '£5,000,001 to £10,000,000' as a string" in {
      GrossAssetsModel(Constants.grossAssetsBandThree).grossAssetsAmountBand() shouldBe "£5,000,001 to £10,000,000"
    }

    "return '£10,000,001 to £15,000,000' as a string" in {
      GrossAssetsModel(Constants.grossAssetsBandFour).grossAssetsAmountBand() shouldBe "£10,000,001 to £15,000,000"
    }

    "return 'More than £15,000,000' as a string" in {
      GrossAssetsModel(Constants.grossAssetsBandFive).grossAssetsAmountBand() shouldBe "More than £15,000,000"
    }

    "return 'Up to £100,000' as a string" in {
      GrossAssetsModel(Constants.grossAssetsSEISBandOne).grossAssetsAmountBand() shouldBe "Up to £100,000"
    }

    "return '£100,001 to £200,000' as a string" in {
      GrossAssetsModel(Constants.grossAssetsSEISBandTwo).grossAssetsAmountBand() shouldBe "£100,001 to £200,000"
    }

    "return 'More than £200,000' as a string" in {
      GrossAssetsModel(Constants.grossAssetsSEISBandThree).grossAssetsAmountBand() shouldBe "More than £200,000"
    }

    "throw an Exception when given a non valid number" in {
      intercept[MatchError] {
        GrossAssetsModel("12345").grossAssetsAmountBand()
      }
    }

  }


}
