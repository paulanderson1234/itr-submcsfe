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
import forms.PreviousShareHoldingDescriptionForm.previousShareHoldingDescriptionForm
import models.investorDetails.PreviousShareHoldingDescriptionModel
import org.scalatestplus.play.OneAppPerSuite
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.UnitSpec

class PreviousShareholdingDescriptionFormSpec extends UnitSpec with OneAppPerSuite{

  private def bindSuccess(request: FakeRequest[AnyContentAsFormUrlEncoded]) = {
    previousShareHoldingDescriptionForm.bindFromRequest()(request).fold(
      formWithErrors => None,
      userData => Some(userData)
    )
  }

  private def bindWithError(request: FakeRequest[AnyContentAsFormUrlEncoded]): Option[FormError] = {
    previousShareHoldingDescriptionForm.bindFromRequest()(request).fold(
      formWithErrors => Some(formWithErrors.errors(0)),
      userData => None
    )
  }
  val previousShareHoldingModel = PreviousShareHoldingDescriptionModel("Ordinary shares", Some(1), Some(1))


  "The share description Form" should {
    "return an error if share description is empty" in {
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

  "The share description Form" should {
    "not return an error if entry at the borderline condition (1 character)" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "descriptionTextArea" -> "h"
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
    "not return an error if entry is above the suggested 20 word limit (21 words)" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "descriptionTextArea" -> "this is more than 20 words to see if that amount is suggested but not enforced and it isn't if this passes so there"
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
    "not return an error if entry is on boundary (250)" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "descriptionTextArea" -> MockDataGenerator.randomAlphanumericString(Constants.shortTextLimit)
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
    "return an error if entry is greater than 250 words in length" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "descriptionTextArea" -> MockDataGenerator.randomAlphanumericString(Constants.shortTextLimit + 1)
      )
      bindWithError(request) match {
        case Some(err) => {
          err.key shouldBe "descriptionTextArea"
          Messages(err.message) shouldBe Messages("error.maxLength")
          err.args shouldBe Array(Constants.shortTextLimit)
        }
        case _ => {
          fail("Missing error")
        }
      }
    }
  }

}
