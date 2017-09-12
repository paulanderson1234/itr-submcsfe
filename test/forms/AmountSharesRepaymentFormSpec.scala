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

package forms

import models.AmountSharesRepaymentModel
import org.scalatestplus.play.OneAppPerSuite
import uk.gov.hmrc.play.test.UnitSpec
import forms.AmountSharesRepaymentForm._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._

class AmountSharesRepaymentFormSpec extends UnitSpec with OneAppPerSuite{

  val maxAmount:BigDecimal = BigDecimal("99999999999")
  val minAmount = 0
  val validAmount = 15
  val decimalAmount = BigDecimal(10.02)
  val negativeAmount = -1

  "Creating a form using an empty model" should {
    lazy val form = amountSharesRepaymentForm
    "return an empty string for amount" in {
      form.data.isEmpty shouldBe true
    }
  }

  "Creating a form using a valid model" should {
    "return a form with the data specified in the model" in {
      val model = AmountSharesRepaymentModel(validAmount)
      val form = amountSharesRepaymentForm.fill(model)
      form.data("amount") shouldBe s"$validAmount"
      form.errors.length shouldBe 0
    }
  }

  "Creating a form using an invalid post" when {
    "supplied with no data for amount" should {
      lazy val form = amountSharesRepaymentForm.bind(Map("amount" -> ""))
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 1 form error" in {
        form.errors.length shouldBe 1
        form.errors.head.key shouldBe "amount"
      }
      "associate the correct error message to the error" in {
        form.errors.head.message shouldBe "error.required"
      }
    }

    "supplied with empty space for amount" should {
      lazy val form = amountSharesRepaymentForm.bind(Map("amount" -> "  "))
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 1 form error" in {
        form.errors.length shouldBe 1
        form.errors.head.key shouldBe "amount"
      }
      "associate the correct error message to the error" in {
        form.errors.head.message shouldBe "error.required"
      }
    }

    "supplied with non numeric input for amount" should {
      lazy val form = amountSharesRepaymentForm.bind(Map("amount" -> "a"))
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 1 form error" in {
        form.errors.length shouldBe 1
        form.errors.head.key shouldBe "amount"
      }
      "associate the correct error message to the error" in {
        form.error("amount").get.message shouldBe Messages("validation.error.amountSharesRepayment.notANumber")
      }
    }

    "supplied an amount with decimals" should {
      lazy val form = amountSharesRepaymentForm.bind(Map("amount" -> s"$decimalAmount"))
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 1 form error" in {
        form.errors.length shouldBe 1
        form.errors.head.key shouldBe "amount"
      }
      "associate the correct error message to the error" in {
        form.error("amount").get.message shouldBe Messages("validation.error.amountSharesRepayment.decimalPlaces")
      }
    }

    "supplied with an amount that's greater than the max" should {
      lazy val form = amountSharesRepaymentForm.bind(Map("amount" -> s"${maxAmount + 1}"))
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 1 form error" in {
        form.errors.length shouldBe 1
        form.errors.head.key shouldBe "amount"
      }
      "associate the correct error message to the error" in {
        form.error("amount").get.message shouldBe Messages("validation.error.amountSharesRepayment.size")
      }
    }

    "supplied with an amount that's lower than the min" should {
      lazy val form = amountSharesRepaymentForm.bind(Map("amount" -> s"${minAmount -1}"))
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 1 form error" in {
        form.errors.length shouldBe 2
        form.errors.head.key shouldBe "amount"
      }
      "associate the correct error message to the error" in {
        // form.error("amount").get.message shouldBe Messages("validation.error.amountSharesRepayment.size")
        // will be negative in this case
        form.error("amount").get.message shouldBe Messages("validation.error.amountSharesRepayment.negative")
      }
    }
	
    "supplied with an amount that's negative " should {
      lazy val form = amountSharesRepaymentForm.bind(Map("amount" -> s"${negativeAmount}"))
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 1 form error" in {
        form.errors.length shouldBe 2
        form.errors.head.key shouldBe "amount"
      }
      "associate the correct error message to the error" in {       
		form.error("amount").get.message shouldBe Messages("validation.error.amountSharesRepayment.negative")
      }
    }
  }

  "Creating a form using a valid post" when {
    "supplied with valid amount at the maximum allowed" should {
      "not raise form error" in {
        val form = amountSharesRepaymentForm.bind(Map("amount" -> s"$maxAmount"))
        form.hasErrors shouldBe false
      }
    }

    "supplied with valid amount at the minimum allowed" should {
      "not raise form error" in {
        val form = amountSharesRepaymentForm.bind(Map("amount" -> s"$minAmount"))
        form.hasErrors shouldBe false
      }
    }
  }
}
