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

class GrossAssetsAfterIssueModelSpec extends BaseSpec with MockitoSugar  {

  implicit def stringToIntConverter(string: String): BigDecimal = BigDecimal(string)

  "Calling .grossAssetsAfterIssueAmountBand on GrossAssetsModelAfterIssue" should {

    "return 'Up to £1,000,000' as a string" in {
      GrossAssetsAfterIssueModel(Constants.grossAssetsBandOne).grossAssetsAfterIssueAmountBand() shouldBe Messages("page.grossAssetsAfterIssue.option.one")
    }

    "return 'Up to £5,000,000' as a string" in {
      GrossAssetsAfterIssueModel(Constants.grossAssetsBandTwo).grossAssetsAfterIssueAmountBand() shouldBe Messages("page.grossAssetsAfterIssue.option.two")
    }

    "return 'Up to £10,000,000' as a string" in {
      GrossAssetsAfterIssueModel(Constants.grossAssetsBandThree).grossAssetsAfterIssueAmountBand() shouldBe Messages("page.grossAssetsAfterIssue.option.three")
    }

    "return 'Up to £16,000,000' as a string" in {
      GrossAssetsAfterIssueModel(Constants.grossAssetsAfterIssueBandFour).grossAssetsAfterIssueAmountBand() shouldBe Messages("page.grossAssetsAfterIssue.option.four")
    }

    "return 'Up to £16,000,001' as a string" in {
      GrossAssetsAfterIssueModel(Constants.grossAssetsAfterIssueBandFive).grossAssetsAfterIssueAmountBand() shouldBe Messages("page.grossAssetsAfterIssue.option.five")
    }


    "throw an Exception when given a non valid number" in {
      intercept[MatchError] {
        GrossAssetsAfterIssueModel("12345").grossAssetsAfterIssueAmountBand()
      }
    }

  }
}
