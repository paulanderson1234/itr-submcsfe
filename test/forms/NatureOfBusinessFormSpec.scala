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
import forms.NatureOfBusinessForm.natureOfBusinessForm
import models.NatureOfBusinessModel
import org.scalatestplus.play.OneAppPerSuite
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import uk.gov.hmrc.play.test.UnitSpec

import scala.util.Random

class NatureOfBusinessFormSpec extends UnitSpec with OneAppPerSuite {

  val natureOfBusinessModel = NatureOfBusinessModel("I sell cars to car warehouse outlets in major towns")
  val invalidSizeString = s"${Random.alphanumeric take Constants.shortTextLimit + 1 mkString}"
  val validSizeString = s"${Random.alphanumeric take Constants.shortTextLimit mkString}"

  "The nature of business Form" should {
    "return an error if natureofbusiness is empty" in {
      val err = natureOfBusinessForm.bind(Map("natureofbusiness" -> "")).errors.head
      err.key shouldBe "natureofbusiness"
      Messages(err.message) shouldBe Messages("error.required")
      err.args shouldBe Array()
    }

    s"return an error if natureofbusiness is longer than ${Constants.shortTextLimit} characters" in {
      val err = natureOfBusinessForm.bind(Map("natureofbusiness" -> invalidSizeString)).errors.head
      err.key shouldBe "natureofbusiness"
      Messages(err.message) shouldBe Messages("validation.common.error.shortTextSize")
      err.args shouldBe Array()
    }

    s"return a valid model if natureofbusiness is ${Constants.shortTextLimit} characters long" in {
      val form = natureOfBusinessForm.bind(Map("natureofbusiness" -> validSizeString))
      form.errors.isEmpty shouldBe true
      form.value shouldBe Some(NatureOfBusinessModel(validSizeString))
    }

    "return a valid model if entry at the borderline condition (1 character)" in {
      val form = natureOfBusinessForm.bind(Map("natureofbusiness" -> "h"))
      form.errors.isEmpty shouldBe true
      form.value shouldBe Some(NatureOfBusinessModel("h"))
    }
  }

  "The utr Form model" should {
    "call apply correctly on the model" in {
      val form = natureOfBusinessForm.fill(natureOfBusinessModel)
      form.data shouldBe Map("natureofbusiness" -> "I sell cars to car warehouse outlets in major towns")
    }
  }
}
