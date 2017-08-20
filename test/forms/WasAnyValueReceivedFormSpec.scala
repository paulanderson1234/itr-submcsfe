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

import common.Constants
import controllers.helpers.MockDataGenerator
import forms.WasAnyValueReceivedForm._
import models.WasAnyValueReceivedModel
import org.scalatestplus.play.OneAppPerSuite
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import uk.gov.hmrc.play.test.UnitSpec

class WasAnyValueReceivedFormSpec extends UnitSpec with OneAppPerSuite {

  lazy val maxLengthText = MockDataGenerator.randomAlphanumericString(Constants.shortTextLimit)
  lazy val overMaxLengthText = MockDataGenerator.randomAlphanumericString(Constants.shortTextLimit  + 1)

  "Creating a form" when {

    "supplied with a model" should {
      lazy val form = wasAnyValueReceivedForm.fill(WasAnyValueReceivedModel(Constants.StandardRadioButtonYesValue, Some("text")))

      "return a form with the correct map" in {
        form.data shouldBe Map(
          "wasAnyValueReceived" -> Constants.StandardRadioButtonYesValue,
          "aboutValueReceived" -> "text"
        )
      }
    }

    "supplied with a map" which {

      "has empty data" should {
        lazy val form = wasAnyValueReceivedForm.bind(Map(
          "wasAnyValueReceived" -> "",
          "aboutValueReceived" -> "")
        )

        "have a form with one error" in {
          form.errors.size shouldBe 1
        }

        "have the correct error" in {
          form.errors.head.message shouldBe "error.required"
        }
      }

      "has invalid data for a 'Yes' response" should {
        lazy val form = wasAnyValueReceivedForm.bind(Map(
          "wasAnyValueReceived" -> Constants.StandardRadioButtonYesValue,
          "aboutValueReceived" -> ""
        ))

        "have a form with one error" in {
          form.errors.size shouldBe 1
        }

        "have the correct error" in {
          form.errors.head.message shouldBe Messages("error.required")
        }
      }

      "has valid data for a 'Yes' response" should {
        lazy val form = wasAnyValueReceivedForm.bind(Map(
          "wasAnyValueReceived" -> Constants.StandardRadioButtonYesValue,
          "aboutValueReceived" -> "text"
        ))

        "have no errors" in {
          form.hasErrors shouldBe false
        }

        "have a valid model" in {
          form.value shouldBe Some(WasAnyValueReceivedModel(Constants.StandardRadioButtonYesValue, Some("text")))
        }
      }

      "has valid data for a 'No' response" should {
        lazy val form = wasAnyValueReceivedForm.bind(Map(
          "wasAnyValueReceived" -> Constants.StandardRadioButtonNoValue,
          "aboutValueReceived" -> ""
        ))

        "have no errors" in {
          form.hasErrors shouldBe false
        }

        "have a valid model" in {
          form.value shouldBe Some(WasAnyValueReceivedModel(Constants.StandardRadioButtonNoValue, None))
        }
      }

      "has valid data for a 'Yes' respone with aboutValueReceived at maximum allowed length" should {
        lazy val form = wasAnyValueReceivedForm.bind(Map(
          "wasAnyValueReceived" -> Constants.StandardRadioButtonYesValue,
          "aboutValueReceived" -> maxLengthText
        ))

        "have no errors" in {
          form.hasErrors shouldBe false
        }

        "have a valid model" in {
          form.value shouldBe Some(WasAnyValueReceivedModel(Constants.StandardRadioButtonYesValue, Some(maxLengthText)))
        }
      }

      "has valid data for a 'Yes' response with aboutValueReceived over maximum allowed length" should {
        lazy val form = wasAnyValueReceivedForm.bind(Map(
          "wasAnyValueReceived" -> Constants.StandardRadioButtonYesValue,
          "aboutValueReceived" -> overMaxLengthText
        ))

        "have no errors" in {
          form.hasErrors shouldBe true
        }

        "have a valid model" in {
          form.value shouldBe None
        }
      }
    }
  }
}
