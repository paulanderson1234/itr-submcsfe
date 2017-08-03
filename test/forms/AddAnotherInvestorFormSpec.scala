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
import models.CompanyOrIndividualModel
import models.AddAnotherInvestorModel
import org.scalatestplus.play.OneAppPerSuite
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.UnitSpec

class AddAnotherInvestorFormSpec extends UnitSpec with OneAppPerSuite{

  private def bindSuccess(request: FakeRequest[AnyContentAsFormUrlEncoded]) = {
    AddAnotherInvestorForm.addAnotherInvestorForm.bindFromRequest()(request).fold(
      formWithErrors => None,
      userData => Some(userData)
    )
  }

  private def bindWithError(request: FakeRequest[AnyContentAsFormUrlEncoded]): Option[FormError] = {
    AddAnotherInvestorForm.addAnotherInvestorForm.bindFromRequest()(request).fold(
      formWithErrors => Some(formWithErrors.errors(0)),
      userData => None
    )
  }

  val addAnotherInvestorJson = """{"addAnotherInvestor":"Yes"}"""
  val addAnotherInvestorModel = AddAnotherInvestorModel("Yes")


  "The Existing Share Holder Form" should {
    "Return an error if no radio button is selected" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "addAnotherInvestor" -> ""
      )
      bindWithError(request) match {
        case Some(err) => {
          err.key shouldBe "addAnotherInvestor"
          Messages(err.message) shouldBe Messages("error.required")
          err.args shouldBe Array()
        }
        case _ => {
          fail("Missing error")
        }
      }
    }
  }

  "The Is Existing Share Holder Form" should {
    "not return an error if the 'Yes' option is selected" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "addAnotherInvestor" -> Constants.StandardRadioButtonYesValue
      )
      bindWithError(request) match {
        case Some(err) => {
          fail("Validation error not expected")
        }
        case _ => ()
      }
    }
  }


  "The Is Existing Share Holder Form" should {
    "not return an error if the 'No' option is selected" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "addAnotherInvestor" -> Constants.StandardRadioButtonNoValue
      )
      bindWithError(request) match {
        case Some(err) => {
          fail("Validation error not expected")
        }
        case _ => ()
      }
    }
  }

  // model to json
  "The Is Existing Share Holder Form model" should {
    "load convert to JSON successfully" in {

      implicit val formats = Json.format[AddAnotherInvestorModel]

      val addAnotherInvestor= Json.toJson(addAnotherInvestorModel).toString()
      addAnotherInvestor shouldBe addAnotherInvestorJson

    }
  }

  // form model to json - apply
  "The Is Existing Share Holder Form model" should {
    "call apply correctly on the model" in {
      implicit val formats = Json.format[AddAnotherInvestorModel]
      val addAnotherInvestorForm =AddAnotherInvestorForm.addAnotherInvestorForm.fill(addAnotherInvestorModel)
      addAnotherInvestorForm.get.addAnotherInvestor shouldBe Constants.StandardRadioButtonYesValue
    }

    // form json to model - unapply
    "call unapply successfully to create expected Json" in {
      implicit val formats = Json.format[AddAnotherInvestorModel]
      val addAnotherInvestorForm = AddAnotherInvestorForm.addAnotherInvestorForm.fill(addAnotherInvestorModel)
      val formJson = Json.toJson(addAnotherInvestorForm.get).toString()
      formJson shouldBe addAnotherInvestorJson
    }
  }
}
