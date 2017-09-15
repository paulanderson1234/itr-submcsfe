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

import forms.GrossAssetsAfterIssueForm._
import models.GrossAssetsAfterIssueModel
import org.scalatestplus.play.OneAppPerSuite
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import uk.gov.hmrc.play.test.UnitSpec

class GrossAssetsAfterIssueFormSpec extends UnitSpec with OneAppPerSuite{

  val maxAmount = 999999999
  val minAmount = 0
  val validAmount = 10

  "Creating a form using an empty model" should {
    lazy val form = grossAssetsAfterIssueForm
    "return an empty string for amount" in {
      form.data.isEmpty shouldBe true
    }
  }

  "Creating a form using a valid model" should {
    "return a form with the data specified in the model" in {
      val model = GrossAssetsAfterIssueModel(validAmount)
      val form = grossAssetsAfterIssueForm.fill(model)
      form.data("grossAmount") shouldBe s"$validAmount"
      form.errors.length shouldBe 0
    }
  }

  "Creating a form using an invalid post" when {
    "supplied with no data for amount" should {
      lazy val form = grossAssetsAfterIssueForm.bind(Map("grossAmount" -> ""))
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 1 form error" in {
        form.errors.length shouldBe 1
        form.errors.head.key shouldBe "grossAmount"
      }
      "associate the correct error message to the error" in {
        form.errors.head.message shouldBe "error.required"
      }
    }

    "supplied with empty space for amount" should {
      lazy val form = grossAssetsAfterIssueForm.bind(Map("grossAmount" -> "  "))
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 1 form error" in {
        form.errors.length shouldBe 1
        form.errors.head.key shouldBe "grossAmount"
      }
      "associate the correct error message to the error" in {
        form.errors.head.message shouldBe "error.required"
      }
    }

    "supplied with non numeric input for amount" should {
      lazy val form = grossAssetsAfterIssueForm.bind(Map("grossAmount" -> "a"))
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 1 form error" in {
        form.errors.length shouldBe 1
        form.errors.head.key shouldBe "grossAmount"
      }
      "associate the correct error message to the error" in {
        form.error("grossAmount").get.message shouldBe Messages("validation.error.grossAssets.notANumber")
      }
    }

    "supplied an amount with decimals" should {
      lazy val form = grossAssetsAfterIssueForm.bind(Map("grossAmount" -> "10.00"))
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 1 form error" in {
        form.errors.length shouldBe 1
        form.errors.head.key shouldBe "grossAmount"
      }
      "associate the correct error message to the error" in {
        form.error("grossAmount").get.message shouldBe Messages("validation.error.grossAssets.decimalPlaces")
      }
    }

    "supplied with an amount that's greater than the max" should {
      lazy val form = grossAssetsAfterIssueForm.bind(Map("grossAmount" -> s"${maxAmount + 1}"))
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 1 form error" in {
        form.errors.length shouldBe 1
        form.errors.head.key shouldBe "grossAmount"
      }
      "associate the correct error message to the error" in {
        form.errors.head.message shouldBe Messages("validation.error.grossAssets.size")
      }
    }

    "supplied with an amount that's lower than the min" should {
      lazy val form = grossAssetsAfterIssueForm.bind(Map("grossAmount" -> s"${minAmount - 1}"))
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 2 form error" in {
        form.errors.length shouldBe 2
        form.errors.head.key shouldBe "grossAmount"
      }
      "associate the correct error message to the error" in {
        form.errors.head.message shouldBe  Messages("validation.error.grossAssets.negative")
      }
    }
  }

  "Creating a form using a valid post" when {

    "supplied with valid amount at the maximum allowed" should {
      "not raise form error" in {
        val form = grossAssetsAfterIssueForm.bind(Map("grossAmount" -> s"$maxAmount"))
        form.hasErrors shouldBe false
      }
    }

    "supplied with valid amount at the minimum allowed" should {
      "not raise form error" in {
        val form = grossAssetsAfterIssueForm.bind(Map("grossAmount" -> s"$minAmount"))
        form.hasErrors shouldBe false
      }
    }

  }
}
