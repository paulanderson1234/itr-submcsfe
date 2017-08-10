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

package testOnly.controllers
import uk.gov.hmrc.play.test.UnitSpec

class InvestorTestHelperSpec extends UnitSpec {

  object investorTestHelper extends InvestorTestHelper {

  }

  "Called with 2,2 (2 investors and 2 share holdings)" should {
    "Return the expected structure" in {
      val list = investorTestHelper.getInvestors(2, 2)
      list.length shouldBe 2
      list.head.previousShareHoldingModels.get.length shouldBe 2
      list.head.previousShareHoldingModels.get.forall(_.validate) shouldBe true
      list.last.previousShareHoldingModels.get.length shouldBe 2
      list.last.previousShareHoldingModels.get.forall(_.validate) shouldBe true
      list.head.validate shouldBe true
      list.last.validate shouldBe true
    }
  }

  "Called with 2,2, true (2 investors and 2 share holdings, with incomplete investor)" should {
    "Return the expected structure" in {
      val list = investorTestHelper.getInvestors(2, 2, includeIncompleteInvestor = true)
      list.length shouldBe 2
      list.head.previousShareHoldingModels.get.length shouldBe 2
      list.head.previousShareHoldingModels.get.forall(_.validate) shouldBe true
      list.last.previousShareHoldingModels shouldBe None
      list.head.validate shouldBe true
      list.last.validate shouldBe false
    }
  }

  "Called with 4,5, true (4 investors and 5 share holdings, with incomplete investor)" should {
    "Return the expected structure" in {
      val list = investorTestHelper.getInvestors(4, 5, includeIncompleteInvestor = true, includeIncompleteShareHolding = false)

      list.length shouldBe 4
      list.head.previousShareHoldingModels.get.length shouldBe 5
      list.head.previousShareHoldingModels.get.forall(_.validate) shouldBe true
      list.head.validate shouldBe true

      list(1).previousShareHoldingModels.get.length shouldBe 5
      list(1).previousShareHoldingModels.get.forall(_.validate) shouldBe true
      list(1).validate shouldBe true

      list(2).previousShareHoldingModels.get.length shouldBe 5
      list(2).previousShareHoldingModels.get.forall(_.validate) shouldBe true
      list(2).validate shouldBe true

      list.last.validate shouldBe false
      list.last.previousShareHoldingModels shouldBe None

    }
  }

  "Called with 4,5, true (4 investors and 5 share holdings, with incomplete investor)" should {
    "Return the expected structure ignoring incompleteShareholdingFlag if IncompleteInvestor flag is set" in {
      val list = investorTestHelper.getInvestors(4, 5, includeIncompleteInvestor = true, includeIncompleteShareHolding = true)

      list.length shouldBe 4
      list.head.previousShareHoldingModels.get.length shouldBe 5
      list.head.previousShareHoldingModels.get.forall(_.validate) shouldBe true
      list.head.validate shouldBe true

      list(1).previousShareHoldingModels.get.length shouldBe 5
      list(1).previousShareHoldingModels.get.forall(_.validate) shouldBe true
      list(1).validate shouldBe true

      list(2).previousShareHoldingModels.get.length shouldBe 5
      list(2).previousShareHoldingModels.get.forall(_.validate) shouldBe true
      list(2).validate shouldBe true

      list.last.previousShareHoldingModels shouldBe None
      list.last.validate shouldBe false
    }
  }

  "Called with 4,5, false, true (4 investors and 5 share holdings with incomplete shareholding)" should {
    "Return the expected structure" in {
      val list = investorTestHelper.getInvestors(4, 5,includeIncompleteInvestor = false, includeIncompleteShareHolding = true)
      list.length shouldBe 4
      list.head.previousShareHoldingModels.get.length shouldBe 5
      list.head.previousShareHoldingModels.get.forall(_.validate) shouldBe true
      list.head.validate shouldBe true

      list(1).validate shouldBe true
      list(1).previousShareHoldingModels.get.length shouldBe 5
      list(1).previousShareHoldingModels.get.forall(_.validate) shouldBe true

      list(2).validate shouldBe true
      list(2).previousShareHoldingModels.get.length shouldBe 5
      list(2).previousShareHoldingModels.get.forall(_.validate) shouldBe true

      list.last.previousShareHoldingModels.get.length shouldBe 5
      list.last.previousShareHoldingModels.get(0).validate shouldBe true
      list.last.previousShareHoldingModels.get(1).validate shouldBe  true
      list.last.previousShareHoldingModels.get(2).validate shouldBe true
      list.last.previousShareHoldingModels.get(3).validate shouldBe true
      // only the last shareholding should be invalid
      list.last.previousShareHoldingModels.get.last.validate shouldBe false
      list.last.validate shouldBe false
    }
  }

  "Called with 4, 0, false, false (4 investors and zero share holdings)" should {
    "Return the expected structure" in {
      val list = investorTestHelper.getInvestors(4, 0,includeIncompleteInvestor = false, includeIncompleteShareHolding = false)
       val check = list.forall( p => p.validate && p.previousShareHoldingModels.isEmpty) shouldBe true
    }
  }

}
