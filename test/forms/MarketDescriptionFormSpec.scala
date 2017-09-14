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
import models.MarketDescriptionModel
import org.scalatestplus.play.OneAppPerSuite
import uk.gov.hmrc.play.test.UnitSpec
import forms.MarketDescriptionForm._
import controllers.helpers.MockDataGenerator

class MarketDescriptionFormSpec extends UnitSpec with OneAppPerSuite{

  val maxAmount:String = MockDataGenerator.randomAlphanumericString(Constants.SuggestedTextMaxLength)
  val overMaxAmount:String = maxAmount + "x"
  val mixedChars : String = """a$£!*&^'% *@~[]¬|;'./\#`+=-<>"?~() 1234567890 ABCDEFGHIJKLMNOPQRSTUVWXYZ abcdefghijklmnopqrstuvwxyz"""

  "The MarketDescriptionForm" when {

    "provided with a model" should {
      val model = MarketDescriptionModel("test")
      lazy val form = marketDescriptionForm.fill(model)

      "return a valid map" in {
        form.data shouldBe Map("descriptionTextArea" -> "test")
      }
    }

    "provided with a valid map with the maximum size" should {
      val map = Map("descriptionTextArea" -> s"$maxAmount")
      lazy val form = marketDescriptionForm.bind(map)

      "contain no errors" in {
        form.errors.isEmpty shouldBe true
      }

      "contain the correct model" in {
        form.value shouldBe Some(MarketDescriptionModel(maxAmount))
      }
    }

    "provided with a valid map the range of allowable charcter types" should {
      val map = Map("descriptionTextArea" -> s"$mixedChars")
      lazy val form = marketDescriptionForm.bind(map)

      "contain no errors" in {
        form.errors.isEmpty shouldBe true
      }

      "contain the correct model" in {
        form.value shouldBe Some(MarketDescriptionModel(mixedChars))
      }
    }

    "provided with an invalid map which is too large" should {
      val map = Map("descriptionTextArea" -> s"$overMaxAmount")
      lazy val form = marketDescriptionForm.bind(map)

      "contain one error" in {
        form.errors.size shouldBe 1
      }

      "contain the too large error message" in {
        form.errors.head.message shouldBe "error.maxLength"
      }
    }

    "provided with an invalid map with an empty value" should {
      val map = Map("descriptionTextArea" -> " ")
      lazy val form = marketDescriptionForm.bind(map)

      "contain one error" in {
        form.errors.size shouldBe 1
      }

      "contain the not a number error message" in {
        form.errors.head.message shouldBe "error.required"
      }
    }
  }
}
