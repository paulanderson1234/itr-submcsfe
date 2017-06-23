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

import models.FullTimeEmployeeCountModel
import org.scalatestplus.play.OneAppPerSuite
import uk.gov.hmrc.play.test.UnitSpec
import forms.FullTimeEmployeeCountForm._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._

class FullTimeEmployeeCountFormSpec extends UnitSpec with OneAppPerSuite {

  "The FullTimeEmployeeCountForm" when {

    "provided with a model" should {
      val model = FullTimeEmployeeCountModel(12.3)
      lazy val form = fullTimeEmployeeCountForm.fill(model)

      "return a valid map" in {
        form.data shouldBe Map("employeeCount" -> "12.3")
      }
    }

    "provided with a valid map with the maximum number of decimal places" should {
      val map = Map("employeeCount" -> "1.1111111111111")
      lazy val form = fullTimeEmployeeCountForm.bind(map)

      "contain no errors" in {
        form.errors.isEmpty shouldBe true
      }

      "contain the correct model" in {
        form.value shouldBe Some(FullTimeEmployeeCountModel(1.1111111111111))
      }
    }

    "provided with a valid map with the maximum size" should {
      val map = Map("employeeCount" -> "9999999999999")
      lazy val form = fullTimeEmployeeCountForm.bind(map)

      "contain no errors" in {
        form.errors.isEmpty shouldBe true
      }

      "contain the correct model" in {
        form.value shouldBe Some(FullTimeEmployeeCountModel(9999999999999.0))
      }
    }

    "provided with an invalid map with one too many decimal places" should {
      val map = Map("employeeCount" -> "1.11111111111111")
      lazy val form = fullTimeEmployeeCountForm.bind(map)

      "contain one error" in {
        form.errors.size shouldBe 1
      }

      "contain the too many decimals error message" in {
        form.errors.head.message shouldBe Messages("validation.error.employeeCount.precision")
      }
    }

    "provided with an invalid map which is too large" should {
      val map = Map("employeeCount" -> "9999999999999.1")
      lazy val form = fullTimeEmployeeCountForm.bind(map)

      "contain one error" in {
        form.errors.size shouldBe 1
      }

      "contain the too large error message" in {
        form.errors.head.message shouldBe Messages("validation.error.employeeCount.size")
      }
    }

    "provided with an invalid map with a non-numeric value" should {
      val map = Map("employeeCount" -> "a")
      lazy val form = fullTimeEmployeeCountForm.bind(map)

      "contain one error" in {
        form.errors.size shouldBe 1
      }

      "contain the not a number error message" in {
        form.errors.head.message shouldBe Messages("validation.error.employeeCount.notANumber")
      }
    }

    "provided with an invalid map with multiple errors" should {
      val map = Map("employeeCount" -> "9999999999999.11111111111111")
      lazy val form = fullTimeEmployeeCountForm.bind(map)

      "contain two errors" in {
        form.errors.size shouldBe 2
      }
    }
  }
}
