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

import models.AddAnotherShareholdingModel
import org.scalatestplus.play.OneAppPerSuite
import uk.gov.hmrc.play.test.UnitSpec
import forms.AddAnotherShareholdingForm._
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._

class AddAnotherShareholdingFormSpec extends UnitSpec with OneAppPerSuite {

  "Creating a form using a valid model" should {
    val form = addAnotherShareholdingForm

    "return a valid map from a true" in {
      val model = AddAnotherShareholdingModel(true)

      form.fill(model).data shouldBe Map("addAnotherShareholding" -> "Yes")
    }

    "return a valid map from a false" in {
      val model = AddAnotherShareholdingModel(false)

      form.fill(model).data shouldBe Map("addAnotherShareholding" -> "No")
    }
  }

  "Creating a form with a valid map" which {
    val form = addAnotherShareholdingForm

    "contains a value of 'Yes'" should {
      val result = form.bind(Map("addAnotherShareholding" -> "Yes"))

      "return a form with no errors" in {
        result.errors.isEmpty shouldBe true
      }

      "return a form with the correct data model" in {
        result.value shouldBe Some(AddAnotherShareholdingModel(true))
      }
    }

    "contains a value of 'No'" should {
      val result = form.bind(Map("addAnotherShareholding" -> "No"))

      "return a form with no errors" in {
        result.errors.isEmpty shouldBe true
      }

      "return a form with the correct data model" in {
        result.value shouldBe Some(AddAnotherShareholdingModel(false))
      }
    }
  }

  "Creating a form with an invalid map" should {
    val result = addAnotherShareholdingForm.bind(Map("addAnotherShareholding" -> ""))

    "return a form with a single error" in {
      result.errors.size shouldBe 1
    }

    "return the correct error message" in {
      result.errors.head.message shouldBe "error.required"
    }
  }
}
