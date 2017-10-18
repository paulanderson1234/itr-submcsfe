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

import models.TotalAmountSpentModel
import org.scalatestplus.play.OneAppPerSuite
import uk.gov.hmrc.play.test.UnitSpec
import forms.TotalAmountSpentForm._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._

import scala.collection.immutable.Range

class TotalAmountSpentFormSpec extends UnitSpec with OneAppPerSuite {

  val maxAmount:BigDecimal = BigDecimal("999999999")
  val mimAmount = 0
  val invalidAmount = 12.12
  val negativeAmount = -1
  val largeNegativeAmount =  BigDecimal("-123.3485684756875346876538743")

  "The TotalAmountSpentForm" when {

    "provided with a model" should {
      val model = TotalAmountSpentModel(123)
      lazy val form = totalAmountSpentForm.fill(model)

      "return a valid map" in {
        form.data shouldBe Map("totalAmountSpent" -> "123")
      }
    }


    "provided with a valid map with the minimum amount" should {
      val map = Map("totalAmountSpent" -> "0")
      lazy val form = totalAmountSpentForm.bind(map)

      "contain no errors" in {
        form.errors.isEmpty shouldBe true
      }

    }

    "provided with a valid map with the maximum size" should {
      val map = Map("totalAmountSpent" -> s"$maxAmount")
      lazy val form = totalAmountSpentForm.bind(map)

      "contain no errors" in {
        form.errors.isEmpty shouldBe true
      }

      "contain the correct model" in {
        form.value shouldBe Some(TotalAmountSpentModel(maxAmount))
      }
    }

    "provided with an invalid map which is too large" should {
      val map = Map("totalAmountSpent" -> s"${maxAmount + 1}")
      lazy val form = totalAmountSpentForm.bind(map)

      "contain one error" in {
        form.errors.size shouldBe 1
      }

      "contain the too large error message" in {
        form.errors.head.message shouldBe Messages("validation.error.totalAmountSpent.size")
      }
    }

    "provided with an invalid map with a non-numeric value" should {
      val map = Map("totalAmountSpent" -> "a")
      lazy val form = totalAmountSpentForm.bind(map)

      "contain one error" in {
        form.errors.size shouldBe 1
      }

      "contain the not a number error message" in {
        form.errors.head.message shouldBe Messages("validation.error.totalAmountSpent.notANumber")
      }
    }

      "provided with an invalid map with a decimal value" should {
        val map = Map("totalAmountSpent" -> s"$invalidAmount")
        lazy val form = totalAmountSpentForm.bind(map)

        "contain one error" in {
          form.errors.size shouldBe 1
        }

        "contain the decimal place error message" in {
          form.errors.head.message shouldBe Messages("validation.error.totalAmountSpent.decimalPlaces")
        }
      }

      "provided with an invalid map with a negative value" should {
        val map = Map("totalAmountSpent" -> s"$negativeAmount")
        lazy val form = totalAmountSpentForm.bind(map)

        "contain one error" in {
          form.hasErrors shouldBe true
        }

        "contain the negative number error message" in {
          form.errors.head.message shouldBe Messages("validation.error.totalAmountSpent.negative")
        }
      }

      "provided with an invalid map with a negative decimal large value " should {
        val map = Map("totalAmountSpent" -> s"$largeNegativeAmount")
        lazy val form = totalAmountSpentForm.bind(map)

        "contain three errors" in {
          form.errors.size shouldBe 4
        }

      }

      "provided with an invalid map with an empty value" should {
      val map = Map("totalAmountSpent" -> " ")
      lazy val form = totalAmountSpentForm.bind(map)

      "contain one error" in {
        form.errors.size shouldBe 1
      }

      "contain the not a number error message" in {
        form.errors.head.message shouldBe "error.required"
      }
    }
  }
}
