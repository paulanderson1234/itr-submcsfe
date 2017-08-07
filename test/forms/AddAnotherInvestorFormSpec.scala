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
import forms.AddAnotherInvestorForm._
import models.AddAnotherInvestorModel
import org.scalatestplus.play.OneAppPerSuite
import play.api.i18n.Messages
import uk.gov.hmrc.play.test.UnitSpec
import play.api.i18n.Messages.Implicits._
import play.api.libs.json.Json

class AddAnotherInvestorFormSpec extends UnitSpec with OneAppPerSuite{

  val modelYes = AddAnotherInvestorModel(Constants.StandardRadioButtonYesValue)
  val modelNo = AddAnotherInvestorModel(Constants.StandardRadioButtonNoValue)

  "Creating the form for the Add Another Investor" should {
    "return a populated yes form using .fill" in {
      val form = addAnotherInvestorForm.fill(modelYes)
      form.value.get shouldBe AddAnotherInvestorModel(Constants.StandardRadioButtonYesValue)
    }

    "return a populated no form using .fill" in {
      val form = addAnotherInvestorForm.fill(modelNo)
      form.value.get shouldBe AddAnotherInvestorModel(Constants.StandardRadioButtonNoValue)
    }

    "return a Some if a model with valid yes option is supplied using .bind" in {
      val map = Map(("addAnotherInvestor", Constants.StandardRadioButtonYesValue))
      val form = addAnotherInvestorForm.bind(map)
      form.value shouldBe Some(AddAnotherInvestorModel(Constants.StandardRadioButtonYesValue))
    }

    "return a Some if a model with 'No' selection using .bind" in {
      val map = Map(("addAnotherInvestor", Constants.StandardRadioButtonNoValue))
      val form = addAnotherInvestorForm.bind(map)
      form.value shouldBe Some(AddAnotherInvestorModel(Constants.StandardRadioButtonNoValue))
      form.hasErrors shouldBe false
    }

    "when no input is selected the form" should {
      lazy val form = addAnotherInvestorForm.bind(Map(("addAnotherInvestor", "")))
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 1 form error" in {
        form.errors.length shouldBe 1
        form.errors.head.key shouldBe "addAnotherInvestor"
      }
      "associate the correct error message to the error" in {
        Messages(form.error("addAnotherInvestor").get.message) shouldBe Messages("error.required")
      }
    }

  }

  val addAnotherInvestorModelJson = """{"addAnotherInvestor":"Yes"}"""
  val addAnotherInvestorModel = AddAnotherInvestorModel("Yes")

  // model to json
  "The Form model" should {
    "load convert to JSON successfully" in {

      implicit val formats = Json.format[AddAnotherInvestorModel]

      val newProduct = Json.toJson(addAnotherInvestorModel).toString()
      newProduct shouldBe addAnotherInvestorModelJson

    }
  }

  // form model to json - apply
  "The Form model" should {
    "call apply correctly on the model" in {
      implicit val formats = Json.format[AddAnotherInvestorModel]
      val addAnotherInvestorForm = AddAnotherInvestorForm.addAnotherInvestorForm.fill(addAnotherInvestorModel)
      addAnotherInvestorForm.get.addAnotherInvestor shouldBe Constants.StandardRadioButtonYesValue
    }

    // form json to model - unapply
    "call unapply successfully to create expected Json" in {
      implicit val formats = Json.format[AddAnotherInvestorModel]
      val addAnotherInvestorForm = AddAnotherInvestorForm.addAnotherInvestorForm.fill(addAnotherInvestorModel)
      val formJson = Json.toJson(addAnotherInvestorForm.get).toString()
      formJson shouldBe addAnotherInvestorModelJson
    }
  }
}
