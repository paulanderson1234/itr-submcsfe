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

class ShareRepaymentsTestHelperSpec extends UnitSpec {

  object shareRepaymentsTestHelper extends ShareRepaymentsTestHelper {

  }

  "Called with 1 complete share repayment" should {
    "Return the expected structure" in {
      val list = shareRepaymentsTestHelper.getShareRepayments(1)
      list.length shouldBe 1

      list.head.processingId shouldBe Some(1)
      list.head.validate shouldBe true
    }
  }

  "Called with 1 incomplete share repayment" should {
    "Return the expected structure" in {
      val list = shareRepaymentsTestHelper.getShareRepayments(1, includeIncompleteShareRepayment = true)
      list.length shouldBe 1

      list.head.processingId shouldBe Some(1)
      list.head.validate shouldBe false
    }
  }

  "Called with 3 complete share repayments" should {
    "Return the expected structure" in {
      val list = shareRepaymentsTestHelper.getShareRepayments(3)
      list.length shouldBe 3

      list.head.processingId shouldBe Some(1)
      list.head.validate shouldBe true
      list(1).processingId shouldBe Some(2)
      list(1).validate shouldBe true
      list.last.processingId shouldBe Some(3)
      list.last.validate shouldBe true
    }
  }

  "Called with 2 complete and 1 incomplete share repayment" should {
    "Return the expected structure" in {
      val list = shareRepaymentsTestHelper.getShareRepayments(3, includeIncompleteShareRepayment = true)
      list.length shouldBe 3

      list.head.processingId shouldBe Some(1)
      list.head.validate shouldBe true
      list(1).processingId shouldBe Some(2)
      list(1).validate shouldBe true
      list.last.processingId shouldBe Some(3)
      list.last.validate shouldBe false
    }
  }

}
