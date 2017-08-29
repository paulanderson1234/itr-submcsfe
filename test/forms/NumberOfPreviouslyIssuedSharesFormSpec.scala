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

import forms.NumberOfPreviouslyIssuedSharesForm._
import models.investorDetails.NumberOfPreviouslyIssuedSharesModel
import org.scalatestplus.play.OneAppPerSuite
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import uk.gov.hmrc.play.test.UnitSpec

class NumberOfPreviouslyIssuedSharesFormSpec extends UnitSpec with OneAppPerSuite {

  val minimumValue:Int = 1

  "The NumberOfPreviouslyIssuedSharesForm" when {

    "provided with a model" should {
      val model = NumberOfPreviouslyIssuedSharesModel(12.3, Some(1))
      lazy val form = numberOfPreviouslyIssuedSharesForm.fill(model)

      "return a valid map" in {
        form.data shouldBe Map("numberOfPreviouslyIssuedShares" -> "12.3", "processingId" -> "1")
      }
    }

    "provided with a valid map with the maximum number of decimal places" should {
      val map = Map("numberOfPreviouslyIssuedShares" -> "1.1111111111111", "processingId" -> "1")
      lazy val form = numberOfPreviouslyIssuedSharesForm.bind(map)

      "contain no errors" in {
        form.errors.isEmpty shouldBe true
      }

      "contain the correct model limited to 5 decimal places" in {
        form.value shouldBe Some(NumberOfPreviouslyIssuedSharesModel(1.11111, Some(1)))
      }
    }

    "provided with a valid map with the minimum amount" should {
      val map = Map("numberOfPreviouslyIssuedShares" -> "1.00000000000", "processingId" -> "2")
      lazy val form = numberOfPreviouslyIssuedSharesForm.bind(map)

      "contain no errors" in {
        form.errors.isEmpty shouldBe true
      }

      "contain the correct model limited to 5 decimal places" in {
        form.value shouldBe Some(NumberOfPreviouslyIssuedSharesModel(1.00000, Some(2)))
      }
    }

    "provided with a valid map with the maximum size" should {
      val map = Map("numberOfPreviouslyIssuedShares" -> "9999999999999", "processingId" -> "1")
      lazy val form = numberOfPreviouslyIssuedSharesForm.bind(map)

      "contain no errors" in {
        form.errors.isEmpty shouldBe true
      }

      "contain the correct model" in {
        form.value shouldBe Some(NumberOfPreviouslyIssuedSharesModel(9999999999999.0, Some(1)))
      }
    }

    "provided with an invalid map which is too large" should {
      val map = Map("numberOfPreviouslyIssuedShares" -> "9999999999999.1")
      lazy val form = numberOfPreviouslyIssuedSharesForm.bind(map)

      "contain one error" in {
        form.errors.size shouldBe 1
      }

      "contain the too large error message" in {
        form.errors.head.message shouldBe Messages("validation.error.numberOfPreviouslyIssuedShares.size", minimumValue)
      }
    }

    "provided with an invalid map with a non-numeric value" should {
      val map = Map("numberOfPreviouslyIssuedShares" -> "a")
      lazy val form = numberOfPreviouslyIssuedSharesForm.bind(map)

      "contain one error" in {
        form.errors.size shouldBe 1
      }

      "contain the not a number error message" in {
        form.errors.head.message shouldBe Messages("validation.error.numberOfPreviouslyIssuedShares.notANumber")
      }
    }

    "provided with an invalid map with an empty value" should {
      val map = Map("numberOfPreviouslyIssuedShares" -> " ")
      lazy val form = numberOfPreviouslyIssuedSharesForm.bind(map)

      "contain one error" in {
        form.errors.size shouldBe 1
      }

      "contain the not a number error message" in {
        form.errors.head.message shouldBe "error.required"
      }
    }
  }
}
