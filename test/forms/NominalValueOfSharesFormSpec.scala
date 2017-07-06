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

import org.scalatestplus.play.OneAppPerSuite
import uk.gov.hmrc.play.test.UnitSpec
import NominalValueOfSharesForm._
import models.NominalValueOfSharesModel

class NominalValueOfSharesFormSpec extends UnitSpec with OneAppPerSuite {

  "NominalValueOfSharesForm" when {

    "supplied with a model" should {
      lazy val form = nominalValueOfSharesForm.fill(NominalValueOfSharesModel(1000))

      "return a map with the data held" in {
        form.data shouldBe Map("value" -> "1000")
      }
    }

    "supplied with a valid map" which {

      "contains a thirteen digit value" should {
        lazy val form = nominalValueOfSharesForm.bind(Map("value" -> "9999999999999"))

        "contain no errors" in {
          form.errors.isEmpty shouldBe true
        }

        "contain a valid model" in {
          form.value shouldBe Some(NominalValueOfSharesModel(BigDecimal("9999999999999")))
        }
      }

      "contains a zero value" should {
        lazy val form = nominalValueOfSharesForm.bind(Map("value" -> "0"))

        "contain no errors" in {
          form.errors.isEmpty shouldBe true
        }

        "contain a valid model" in {
          form.value shouldBe Some(NominalValueOfSharesModel(0))
        }
      }
    }

    "supplied with an invalid map" which {

      "contains an empty field" should {
        lazy val form = nominalValueOfSharesForm.bind(Map("value" -> ""))

        "contain one error" in {
          form.errors.size shouldBe 1
        }

        "contain an error message for empty values" in {
          form.errors.head.message shouldBe "error.required"
        }
      }

      "contains a non-numeric value" should {
        lazy val form = nominalValueOfSharesForm.bind(Map("value" -> "a"))

        "contain one error" in {
          form.errors.size shouldBe 1
        }

        "contain an error message for non-numeric values" in {
          form.errors.head.message shouldBe "validation.error.nominalValueOfShares.notANumber"
        }
      }

      "contains decimal places" should {
        lazy val form = nominalValueOfSharesForm.bind(Map("value" -> "2.3"))

        "contain one error" in {
          form.errors.size shouldBe 1
        }

        "contain an error message for values with decimal places" in {
          form.errors.head.message shouldBe "validation.error.nominalValueOfShares.decimalPlaces"
        }
      }

      "contains more than thirteen digits" should {
        lazy val form = nominalValueOfSharesForm.bind(Map("value" -> "99999999999999"))

        "contain one error" in {
          form.errors.size shouldBe 1
        }

        "contain an error message for values with too many digits" in {
          form.errors.head.message shouldBe "validation.error.nominalValueOfShares.size"
        }
      }

      "contains a negative number" should {
        lazy val form = nominalValueOfSharesForm.bind(Map("value" -> "-1"))

        "contain one error" in {
          form.errors.size shouldBe 1
        }

        "contain an error message for values with a negative value" in {
          form.errors.head.message shouldBe "validation.error.nominalValueOfShares.negative"
        }
      }

      "contains multiple form errors" should {
        lazy val form = nominalValueOfSharesForm.bind(Map("value" -> "-9999999999999.0"))

        "contain three errors" in {
          form.errors.size shouldBe 3
        }
      }
    }
  }
}
