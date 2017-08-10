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
import models.investorDetails.PreviousShareHoldingModel
import uk.gov.hmrc.play.test.UnitSpec

class InvestorTestHelperSpec extends UnitSpec {

  object investorTestHelper extends InvestorTestHelper {

  }

  "Called with 2,2 (2 investors and 2 share holdings)" should {
    "Return the expected structure" in {
      val list = investorTestHelper.getInvestors(2, 2)
      list.length shouldBe 2
      val firstInvestor = list.head


      firstInvestor.processingId shouldBe Some(1)
      firstInvestor.validate shouldBe true

      val firstInvestorHoldings = firstInvestor.previousShareHoldingModels.get

      firstInvestorHoldings.length shouldBe 2
      firstInvestorHoldings.forall(_.validate) shouldBe true
      validateHoldingIdSequence(firstInvestorHoldings) shouldBe true

      firstInvestor.processingId shouldBe Some(1)
      firstInvestor.validate shouldBe true

      val lastInvestor = list.last
      lastInvestor.processingId shouldBe Some(2)
      lastInvestor.validate shouldBe true
      val lastInvestorHoldings = lastInvestor.previousShareHoldingModels.get

      lastInvestorHoldings.length shouldBe 2
      lastInvestorHoldings.forall(_.validate) shouldBe true
      validateHoldingIdSequence(lastInvestorHoldings) shouldBe true
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

      val firstInvestor = list.head
      firstInvestor.processingId shouldBe Some(1)
      firstInvestor.validate shouldBe true

      val firstInvestorHoldings = firstInvestor.previousShareHoldingModels.get

      firstInvestorHoldings.length shouldBe 5
      firstInvestorHoldings.forall(_.validate) shouldBe true
      validateHoldingIdSequence(firstInvestorHoldings) shouldBe true

      val secondInvestor = list(1)
      secondInvestor.processingId shouldBe Some(2)
      secondInvestor.validate shouldBe true

      val secondInvestorHoldings = secondInvestor.previousShareHoldingModels.get
      secondInvestorHoldings.length shouldBe 5
      secondInvestorHoldings.forall(_.validate) shouldBe true
      validateHoldingIdSequence(firstInvestorHoldings) shouldBe true

      val thirdInvestor = list(2)
      thirdInvestor.processingId shouldBe Some(3)
      thirdInvestor.validate shouldBe true

      val thirdInvestorHoldings = thirdInvestor.previousShareHoldingModels.get

      thirdInvestorHoldings.length shouldBe 5
      thirdInvestorHoldings.forall(_.validate) shouldBe true
      validateHoldingIdSequence(thirdInvestorHoldings) shouldBe true

      val lastInvestor = list.last
      lastInvestor.validate shouldBe false
      lastInvestor.processingId shouldBe Some(4)
      lastInvestor.previousShareHoldingModels shouldBe None

    }
  }

  "Called with 4,5, true (4 investors and 5 share holdings, with incomplete investor)" should {
    "Return the expected structure ignoring incompleteShareholdingFlag if IncompleteInvestor flag is set" in {
      val list = investorTestHelper.getInvestors(4, 5, includeIncompleteInvestor = true, includeIncompleteShareHolding = true)

      list.length shouldBe 4

      val firstInvestor = list.head
      firstInvestor.processingId shouldBe Some(1)
      firstInvestor.validate shouldBe true

      val firstInvestorHoldings = firstInvestor.previousShareHoldingModels.get

      firstInvestorHoldings.length shouldBe 5
      firstInvestorHoldings.forall(_.validate) shouldBe true
      validateHoldingIdSequence(firstInvestorHoldings) shouldBe true

      val secondInvestor = list(1)
      secondInvestor.processingId shouldBe Some(2)
      secondInvestor.validate shouldBe true

      val secondInvestorHoldings = secondInvestor.previousShareHoldingModels.get
      secondInvestorHoldings.length shouldBe 5
      secondInvestorHoldings.forall(_.validate) shouldBe true
      validateHoldingIdSequence(firstInvestorHoldings) shouldBe true

      val thirdInvestor = list(2)
      thirdInvestor.processingId shouldBe Some(3)
      thirdInvestor.validate shouldBe true

      val thirdInvestorHoldings = thirdInvestor.previousShareHoldingModels.get

      thirdInvestorHoldings.length shouldBe 5
      thirdInvestorHoldings.forall(_.validate) shouldBe true
      validateHoldingIdSequence(thirdInvestorHoldings) shouldBe true

      val lastInvestor = list.last
      lastInvestor.processingId shouldBe Some(4)
      lastInvestor.previousShareHoldingModels shouldBe None
      lastInvestor.validate shouldBe false
    }
  }

  "Called with 4,5, false, true (4 investors and 5 share holdings with incomplete shareholding)" should {
    "Return the expected structure" in {
      val list = investorTestHelper.getInvestors(4, 5,includeIncompleteInvestor = false, includeIncompleteShareHolding = true)

      list.length shouldBe 4

      val firstInvestor = list.head
      firstInvestor.processingId shouldBe Some(1)
      firstInvestor.validate shouldBe true

      val firstInvestorHoldings = firstInvestor.previousShareHoldingModels.get
      firstInvestorHoldings.length shouldBe 5
      firstInvestorHoldings.forall(_.validate) shouldBe true
      validateHoldingIdSequence(firstInvestorHoldings) shouldBe true

      val secondInvestor = list(1)
      secondInvestor.processingId shouldBe Some(2)
      secondInvestor.validate shouldBe true

      val secondInvestorHoldings = secondInvestor.previousShareHoldingModels.get
      secondInvestorHoldings.length shouldBe 5
      secondInvestorHoldings.forall(_.validate) shouldBe true
      validateHoldingIdSequence(secondInvestorHoldings) shouldBe true

      val thirdInvestor = list(2)
      thirdInvestor.processingId shouldBe Some(3)
      thirdInvestor.validate shouldBe true

      val thirdInvestorHoldings = thirdInvestor.previousShareHoldingModels.get
      thirdInvestorHoldings.length shouldBe 5
      thirdInvestorHoldings.forall(_.validate) shouldBe true
      validateHoldingIdSequence(thirdInvestorHoldings) shouldBe true


      val lastInvestor = list.last
      lastInvestor.validate shouldBe false
      lastInvestor.processingId shouldBe Some(4)

      val lastInvestorHoldings = list.last.previousShareHoldingModels.get
      lastInvestorHoldings.length shouldBe 5

      lastInvestorHoldings(0).validate shouldBe true
      lastInvestorHoldings(1).validate shouldBe  true
      lastInvestorHoldings(2).validate shouldBe true
      lastInvestorHoldings(3).validate shouldBe true
      // only the last shareholding should be invalid
      lastInvestorHoldings.last.validate shouldBe false
      validateHoldingIdSequence(lastInvestorHoldings) shouldBe true

    }
  }

  "Called with 4, 0, false, false (4 investors and zero share holdings)" should {
    "Return the expected structure" in {
      val list = investorTestHelper.getInvestors(4, 0,includeIncompleteInvestor = false, includeIncompleteShareHolding = false)
       val check = list.forall( p => p.validate && p.previousShareHoldingModels.isEmpty) shouldBe true
    }
  }

  private def validateHoldingIdSequence(shareholdings:Vector[PreviousShareHoldingModel]): Boolean = {
    shareholdings.zipWithIndex.forall( { case (holding, i) => holding.processingId.get  == i + 1 })
  }

}
