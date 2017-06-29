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
import forms.SeventyPercentSpentForm._
import models.SeventyPercentSpentModel
import org.scalatestplus.play.OneAppPerSuite
import play.api.i18n.Messages
import uk.gov.hmrc.play.test.UnitSpec
import play.api.i18n.Messages.Implicits._
import play.api.libs.json.Json

class SeventyPercentSpentFormSpec extends UnitSpec with OneAppPerSuite{

  val modelYes = SeventyPercentSpentModel(Constants.StandardRadioButtonYesValue)
  val modelNo = SeventyPercentSpentModel(Constants.StandardRadioButtonNoValue)
  
  "Creating the form for the Seventy Percent Spent" should {
    "return a populated yes form using .fill" in {
      val form = seventyPercentSpentForm.fill(modelYes)
      form.value.get shouldBe SeventyPercentSpentModel(Constants.StandardRadioButtonYesValue)
    }

    "return a populated no form using .fill" in {
      val form = seventyPercentSpentForm.fill(modelNo)
      form.value.get shouldBe SeventyPercentSpentModel(Constants.StandardRadioButtonNoValue)
    }

    "return a Some if a model with valid yes option is supplied using .bind" in {
      val map = Map(("isSeventyPercentSpent", Constants.StandardRadioButtonYesValue))
      val form = seventyPercentSpentForm.bind(map)
      form.value shouldBe Some(SeventyPercentSpentModel(Constants.StandardRadioButtonYesValue))
    }

    "return a Some if a model with 'No' selection using .bind" in {
      val map = Map(("isSeventyPercentSpent", Constants.StandardRadioButtonNoValue))
      val form = seventyPercentSpentForm.bind(map)
      form.value shouldBe Some(SeventyPercentSpentModel(Constants.StandardRadioButtonNoValue))
      form.hasErrors shouldBe false
    }

    "when no input is selected the form" should {
      lazy val form = seventyPercentSpentForm.bind(Map(("isSeventyPercentSpent", "")))
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 1 form error" in {
        form.errors.length shouldBe 1
        form.errors.head.key shouldBe "isSeventyPercentSpent"
      }
      "associate the correct error message to the error" in {
        Messages(form.error("isSeventyPercentSpent").get.message) shouldBe Messages("error.required")
      }
    }
    
  }

  val seventyPercentSpentModelJson = """{"isSeventyPercentSpent":"Yes"}"""
  val seventyPercentSpentModel = SeventyPercentSpentModel("Yes")

  // model to json
  "The Form model" should {
    "load convert to JSON successfully" in {

      implicit val formats = Json.format[SeventyPercentSpentModel]

      val newProduct = Json.toJson(seventyPercentSpentModel).toString()
      newProduct shouldBe seventyPercentSpentModelJson

    }
  }

  // form model to json - apply
  "The Form model" should {
    "call apply correctly on the model" in {
      implicit val formats = Json.format[SeventyPercentSpentModel]
      val seventyPercentForm = SeventyPercentSpentForm.seventyPercentSpentForm.fill(seventyPercentSpentModel)
      seventyPercentForm.get.isSeventyPercentSpent shouldBe Constants.StandardRadioButtonYesValue
    }

    // form json to model - unapply
    "call unapply successfully to create expected Json" in {
      implicit val formats = Json.format[SeventyPercentSpentModel]
      val seventyPercentForm = SeventyPercentSpentForm.seventyPercentSpentForm.fill(seventyPercentSpentModel)
      val formJson = Json.toJson(seventyPercentForm.get).toString()
      formJson shouldBe seventyPercentSpentModelJson
    }
  }
}
