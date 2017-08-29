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

import models.seis.PreviousSchemesAnswersModel
import uk.gov.hmrc.play.test.UnitSpec

class PreviousSchemesAnswersModelSpec extends UnitSpec {

  "Calling .validate on PreviousSchemesAnswersModel" should {
    val completeModel = PreviousSchemesAnswersModel(
      HadPreviousRFIModel("No"),
      HadOtherInvestmentsModel("No"),
      Some(List(PreviousSchemeModel("", 1, Some(1),Some(""), Some(1), Some(1), Some(2015), Some(1))))
    )

    "return a false" when {

      "there is no list data when previous RFI is claimed" in {
        completeModel.copy(hadPreviousRFIModel = HadPreviousRFIModel("Yes"), previousSchemeModel = None).validate shouldBe false
      }

      "there is no list data when other investments are claimed" in {
        completeModel.copy(otherInvestmentsModel = HadOtherInvestmentsModel("Yes"), previousSchemeModel = None).validate shouldBe false
      }

      "there is no list data when both other investments and previous RFI are claimed" in {
        completeModel.copy(hadPreviousRFIModel = HadPreviousRFIModel("Yes"),
          otherInvestmentsModel = HadOtherInvestmentsModel("Yes"),
          previousSchemeModel = None).validate shouldBe false
      }

      "there is an empty list when it is required" in {
        completeModel.copy(hadPreviousRFIModel = HadPreviousRFIModel("Yes"),
          otherInvestmentsModel = HadOtherInvestmentsModel("Yes"),
          previousSchemeModel = Some(List())).validate shouldBe false
      }
    }

    "return a true" when {

      "there are previous schemes when previous RFI is claimed" in {
        completeModel.copy(hadPreviousRFIModel = HadPreviousRFIModel("Yes")).validate shouldBe true
      }

      "there are previous schemes when other investments are claimed" in {
        completeModel.copy(otherInvestmentsModel = HadOtherInvestmentsModel("Yes")).validate shouldBe true
      }

      "there are previous schemes when both other investments and previous RFI are claimed" in {
        completeModel.copy(hadPreviousRFIModel = HadPreviousRFIModel("Yes"),
          otherInvestmentsModel = HadOtherInvestmentsModel("Yes")).validate shouldBe true
      }

      "there are no previous schemes when neither other investments or previous RFI are claimed" in {
        completeModel.copy(previousSchemeModel = None).validate shouldBe true
      }

      "there is an empty list previous schemes when neither other investments or previous RFI are claimed" in {
        completeModel.copy(previousSchemeModel = Some(List())).validate shouldBe true
      }
    }
  }
}
