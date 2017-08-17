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

import uk.gov.hmrc.play.test.UnitSpec
import WasAnyValueReceivedForm._
import models.WasAnyValueReceivedModel

class WasAnyValueReceivedFormSpec extends UnitSpec {

  "Creating a form" when {

    "supplied with a model" should {
      lazy val form = wasAnyValueReceivedForm.fill(WasAnyValueReceivedModel("Yes"))

      "return a form with the correct map" in {
        form.data shouldBe Map("wasAnyValueReceived" -> "Yes")
      }
    }

    "supplied with a map" which {

      "has empty data" should {
        lazy val form = wasAnyValueReceivedForm.bind(Map("wasAnyValueReceived" -> ""))

        "have a form with one error" in {
          form.errors.size shouldBe 1
        }

        "have the correct error" in {
          form.errors.head.message shouldBe "error.required"
        }
      }

      "has valid data" should {
        lazy val form = wasAnyValueReceivedForm.bind(Map("wasAnyValueReceived" -> "Yes"))

        "have no errors" in {
          form.hasErrors shouldBe false
        }

        "have a valid model" in {
          form.value shouldBe Some(WasAnyValueReceivedModel("Yes"))
        }
      }
    }
  }
}
