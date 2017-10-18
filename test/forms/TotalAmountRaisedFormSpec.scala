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

import models.TotalAmountRaisedModel
import org.scalatestplus.play.OneAppPerSuite
import uk.gov.hmrc.play.test.UnitSpec
import forms.TotalAmountRaisedForm._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._

class TotalAmountRaisedFormSpec extends UnitSpec with OneAppPerSuite{

  val maxAmount:BigDecimal = BigDecimal("999999999")
  val mimAmount = 0
  val invalidAmount = 12.12
  val negativeAmount = -1
  val largeNegativeAmount =  BigDecimal("-123.3485684756875346876538743")

  "Creating a form using an empty model" should {
    lazy val form = totalAmountRaisedForm
    "return an empty string for amount" in {
      form.data.isEmpty shouldBe true
    }
  }

  "Creating a form using a valid model" should {
    "return a form with the data specified in the model" in {
      val model = TotalAmountRaisedModel(maxAmount)
      val form = totalAmountRaisedForm.fill(model)
      form.data("amount") shouldBe s"$maxAmount"
      form.errors.length shouldBe 0
    }
  }

  "Creating a form using an invalid post" when {
    "supplied with no data for amount" should {
      lazy val form = totalAmountRaisedForm.bind(Map("amount" -> ""))
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
      lazy val form = totalAmountRaisedForm.bind(Map("amount" -> "  "))
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
      lazy val form = totalAmountRaisedForm.bind(Map("amount" -> "a"))
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 1 form error" in {
        form.errors.length shouldBe 1
        form.errors.head.key shouldBe "amount"
      }
      "associate the correct error message to the error" in {
        form.error("amount").get.message shouldBe Messages("validation.error.totalAmountRaised.notANumber")
      }
    }

    "supplied an amount with decimals" should {
      lazy val form = totalAmountRaisedForm.bind(Map("amount" -> "10.00"))
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 1 form error" in {
        form.errors.length shouldBe 1
        form.errors.head.key shouldBe "amount"
      }
      "associate the correct error message to the error" in {
        form.error("amount").get.message shouldBe Messages("validation.error.totalAmountRaised.decimalPlaces")
      }
    }

    "supplied with an amount that's greater than the max" should {
      lazy val form = totalAmountRaisedForm.bind(Map("amount" -> s"${maxAmount + 1}"))
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 1 form error" in {
        form.errors.length shouldBe 1
        form.errors.head.key shouldBe "amount"
      }
      "associate the correct error message to the error" in {
        form.errors.head.message shouldBe Messages("validation.error.totalAmountRaised.size")
      }
    }

    "supplied with an amount that's lower than the min" should {
      lazy val form = totalAmountRaisedForm.bind(Map("amount" -> s"${mimAmount - 1}"))
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 2 form error" in {
        form.errors.length shouldBe 2
        form.errors.head.key shouldBe "amount"
      }
      "associate the correct error message to the error" in {
        form.errors.head.message shouldBe  Messages("validation.error.totalAmountRaised.negative")
      }
    }
  }

  "Creating a form using a valid post" when {

    "supplied with valid amount at the maximum allowed" should {
      "not raise form error" in {
        val form = totalAmountRaisedForm.bind(Map("amount" -> s"$maxAmount"))
        form.hasErrors shouldBe false
      }
    }

    "supplied with valid amount at the minimum allowed" should {
      "not raise form error" in {
        val form = totalAmountRaisedForm.bind(Map("amount" -> s"$mimAmount"))
        form.hasErrors shouldBe false
      }
    }

  }
}
