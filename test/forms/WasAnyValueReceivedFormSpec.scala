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

import forms.WasAnyValueReceivedForm._
import models.WasAnyValueReceivedModel
import org.scalatestplus.play.OneAppPerSuite
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import uk.gov.hmrc.play.test.UnitSpec

class WasAnyValueReceivedFormSpec extends UnitSpec with OneAppPerSuite {

  "Creating a form" when {

    "supplied with a model" should {
      lazy val form = wasAnyValueReceivedForm.fill(WasAnyValueReceivedModel("Yes", Some("text")))

      "return a form with the correct map" in {
        form.data shouldBe Map(
          "wasAnyValueReceived" -> "Yes",
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
          "wasAnyValueReceived" -> "Yes",
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
          "wasAnyValueReceived" -> "Yes",
          "aboutValueReceived" -> "text"
        ))

        "have no errors" in {
          form.hasErrors shouldBe false
        }

        "have a valid model" in {
          form.value shouldBe Some(WasAnyValueReceivedModel("Yes", Some("text")))
        }
      }

      "has valid data for a 'No' response" should {
        lazy val form = wasAnyValueReceivedForm.bind(Map(
          "wasAnyValueReceived" -> "No",
          "aboutValueReceived" -> ""
        ))

        "have no errors" in {
          form.hasErrors shouldBe false
        }

        "have a valid model" in {
          form.value shouldBe Some(WasAnyValueReceivedModel("No", None))
        }
      }
    }
  }
}
