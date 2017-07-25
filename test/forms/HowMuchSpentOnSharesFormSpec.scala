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

import forms.HowMuchSpentOnSharesForm._
import models.investorDetails._
import org.scalatestplus.play.OneAppPerSuite
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import uk.gov.hmrc.play.test.UnitSpec

class HowMuchSpentOnSharesFormSpec extends UnitSpec with OneAppPerSuite {

  val maxAmount:BigDecimal = BigDecimal("99999999999")
  val mimAmount = 0
  val invalidAmount = 12.12
  val negativeAmount = -1
  val largeNegativeAmount =  BigDecimal("-123.3485684756875346876538743")

  "The HowMuchSpentOnSharesForm" when {

    "provided with a model" should {
      val model = HowMuchSpentOnSharesModel(123, Some(1))
      lazy val form = howMuchSpentOnSharesForm.fill(model)

      "return a valid map" in {
        form.data shouldBe Map("howMuchSpentOnShares" -> "123", "processingId" -> "1")
      }
    }


    "provided with a valid map with the minimum amount" should {
      val map = Map("howMuchSpentOnShares" -> "0", "processingId" -> "1")
      lazy val form = howMuchSpentOnSharesForm.bind(map)

      "contain no errors" in {
        form.errors.isEmpty shouldBe true
      }

    }

    "provided with a valid map with the maximum size" should {
      val map = Map("howMuchSpentOnShares" -> s"$maxAmount", "processingId" -> "1")
      lazy val form = howMuchSpentOnSharesForm.bind(map)

      "contain no errors" in {
        form.errors.isEmpty shouldBe true
      }

      "contain the correct model" in {
        form.value shouldBe Some(HowMuchSpentOnSharesModel(maxAmount, Some(1)))
      }
    }

    "provided with an invalid map which is too large" should {
      val map = Map("howMuchSpentOnShares" -> s"${maxAmount + 1}", "processingId" -> "1")
      lazy val form = howMuchSpentOnSharesForm.bind(map)

      "contain one error" in {
        form.errors.size shouldBe 1
      }

      "contain the too large error message" in {
        form.errors.head.message shouldBe Messages("validation.error.howMuchSpentOnShares.size")
      }
    }

    "provided with an invalid map with a non-numeric value" should {
      val map = Map("howMuchSpentOnShares" -> "a", "processingId" -> "1")
      lazy val form = howMuchSpentOnSharesForm.bind(map)

      "contain one error" in {
        form.errors.size shouldBe 1
      }

      "contain the not a number error message" in {
        form.errors.head.message shouldBe Messages("validation.error.howMuchSpentOnShares.notANumber")
      }
    }

    "provided with an invalid map with a decimal value" should {
      val map = Map("howMuchSpentOnShares" -> s"$invalidAmount", "processingId" -> "1")
      lazy val form = howMuchSpentOnSharesForm.bind(map)

      "contain one error" in {
        form.errors.size shouldBe 1
      }

      "contain the decimal place error message" in {
        form.errors.head.message shouldBe Messages("validation.error.howMuchSpentOnShares.decimalPlaces")
      }
    }

    "provided with an invalid map with a negative value" should {
      val map = Map("howMuchSpentOnShares" -> s"$negativeAmount", "processingId" -> "1")
      lazy val form = howMuchSpentOnSharesForm.bind(map)

      "contain one error" in {
        form.hasErrors shouldBe true
      }

      "contain the negative number error message" in {
        form.errors.head.message shouldBe Messages("validation.error.howMuchSpentOnShares.negative")
      }
    }

    "provided with an invalid map with a negative decimal large value " should {
      val map = Map("howMuchSpentOnShares" -> s"$largeNegativeAmount", "processingId" -> "1")
      lazy val form = howMuchSpentOnSharesForm.bind(map)

      "contain three errors" in {
        form.errors.size shouldBe 4
      }

    }

    "provided with an invalid map with an empty value" should {
      val map = Map("howMuchSpentOnShares" -> " ", "processingId" -> "1")
      lazy val form = howMuchSpentOnSharesForm.bind(map)

      "contain one error" in {
        form.errors.size shouldBe 1
      }

      "contain the not a number error message" in {
        form.errors.head.message shouldBe "error.required"
      }
    }
  }
}
