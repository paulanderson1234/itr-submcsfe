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
import models.InvestmentGrowModel
import org.scalatestplus.play.OneAppPerSuite
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.UnitSpec
import play.api.i18n.Messages.Implicits._

class InvestmentGrowFormSpec extends UnitSpec with OneAppPerSuite{

  lazy val SuggestedMaxLengthText: String = MockDataGenerator.randomAlphanumericString(Constants.SuggestedTextMaxLength)
  lazy val overSuggestedMaxLengthText: String = MockDataGenerator.randomAlphanumericString(Constants.SuggestedTextMaxLength  + 1)

  private def bindSuccess(request: FakeRequest[AnyContentAsFormUrlEncoded]) = {
    InvestmentGrowForm.investmentGrowForm.bindFromRequest()(request).fold(
      formWithErrors => None,
      userData => Some(userData)
    )
  }

  private def bindWithError(request: FakeRequest[AnyContentAsFormUrlEncoded]): Option[FormError] = {
    InvestmentGrowForm.investmentGrowForm.bindFromRequest()(request).fold(
      formWithErrors => Some(formWithErrors.errors(0)),
      userData => None
    )
  }

  val investmentGrowJson = """{"investmentGrowDesc":"I intend to use this investment to grow the company by 50%."}"""
  val investmentGrowModel = InvestmentGrowModel("I intend to use this investment to grow the company by 50%.")

    "The Investment Grow Form" should {
    "return an error if descriptionTextArea is empty" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "descriptionTextArea" -> ""
      )
      bindWithError(request) match {
        case Some(err) => {
          err.key shouldBe "descriptionTextArea"

          Messages(err.message) shouldBe Messages("error.required")
          err.args shouldBe Array()
        }
        case _ => {
          fail("Missing error")
        }
      }
    }
  }

  "The Investment Grow Form" should {
    "not return an error if entry at the borderline condition (1 character)" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "descriptionTextArea" -> "a"
      )
      bindWithError(request) match {
        case Some(err) => {
          fail("Validation error not expected")
        }
        case _ => ()
      }
    }
  }

  "The Investment Grow Form" should {
    "not return an error if entry is on boundary (2048)" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "descriptionTextArea" -> SuggestedMaxLengthText
      )
      bindWithError(request) match {
        case Some(err) => {
          fail("Validation error not expected")
        }
        case _ => ()
      }
    }
  }

  "The share description Form" should {
    "return an error if entry is greater than Suggested Max Length Text" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "descriptionTextArea" -> overSuggestedMaxLengthText
      )
      bindWithError(request) match {
        case Some(err) => {
          err.key shouldBe "descriptionTextArea"
          Messages(err.message) shouldBe Messages("error.maxLength")
          err.args shouldBe Array(Constants.SuggestedTextMaxLength)
        }
        case _ => {
          fail("Missing error")
        }
      }
    }
  }

  // model to json
  "The Investment Grow Form model" should {
    "load convert to JSON successfully" in {
      implicit val formats = Json.format[InvestmentGrowModel]
      val utrJson = Json.toJson(investmentGrowModel).toString()
      utrJson shouldBe investmentGrowJson
    }
  }

  // form model to json - apply
  "The Investment Grow Form model" should {
    "call apply correctly on the model" in {
      implicit val formats = Json.format[InvestmentGrowModel]
      val investmentGrowForm = InvestmentGrowForm.investmentGrowForm.fill(investmentGrowModel)
      investmentGrowForm.get.investmentGrowDesc shouldBe "I intend to use this investment to grow the company by 50%."
    }

    // form json to model - unapply
    "call unapply successfully to create expected Json" in {
      implicit val formats = Json.format[InvestmentGrowModel]
      val investmentGrowForm = InvestmentGrowForm.investmentGrowForm.fill(investmentGrowModel)
      val formJson = Json.toJson(investmentGrowForm.get).toString()
      formJson shouldBe investmentGrowJson
    }
  }
}
