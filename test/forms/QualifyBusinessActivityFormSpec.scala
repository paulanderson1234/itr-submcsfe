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
import models.QualifyBusinessActivityModel
import org.scalatestplus.play.OneAppPerSuite
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.UnitSpec

class QualifyBusinessActivityFormSpec extends UnitSpec with OneAppPerSuite{

  private def bindSuccess(request: FakeRequest[AnyContentAsFormUrlEncoded]) = {
    QualifyBusinessActivityForm.qualifyBusinessActivityForm.bindFromRequest()(request).fold(
      formWithErrors => None,
      userData => Some(userData)
    )
  }

  private def bindWithError(request: FakeRequest[AnyContentAsFormUrlEncoded]): Option[FormError] = {
    QualifyBusinessActivityForm.qualifyBusinessActivityForm.bindFromRequest()(request).fold(
      formWithErrors => Some(formWithErrors.errors(0)),
      userData => None
    )
  }

  val qualifyBusinessActivityJson = """{"isQualifyBusinessActivity":"Trade"}"""
  val qualifyBusinessActivityModel = QualifyBusinessActivityModel("Trade")

  "The Qualifying Business Activity Form" should {
    "Return an error if no radio button is selected" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "isQualifyBusinessActivity" -> ""
      )
      bindWithError(request) match {
        case Some(err) => {
          err.key shouldBe "isQualifyBusinessActivity"
          Messages(err.message) shouldBe Messages("error.required")
          err.args shouldBe Array()
        }
        case _ => {
          fail("Missing error")
        }
      }
    }
  }

  "The Qualifying Business Activity Form" should {
    "not return an error if the 'Doing business or getting ready to do business' option is selected" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "isQualifyBusinessActivity" -> Constants.qualifyTrade
      )
      bindWithError(request) match {
        case Some(err) => {
          fail("Validation error not expected")
        }
        case _ => ()
      }
    }
  }

  "The Qualifying Business Activity Form" should {
    "not return an error if the 'Research and development to do with your business activity' option is selected" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "isQualifyBusinessActivity" -> Constants.qualifyResearchAndDevelopment
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
  "The Qualifying Business Activity model" should {
    "load convert to JSON successfully" in {

      implicit val formats = Json.format[QualifyBusinessActivityModel]

      val qualifyBusinessActivity = Json.toJson(qualifyBusinessActivityModel).toString()
      qualifyBusinessActivity shouldBe qualifyBusinessActivityJson

    }
  }

  // form model to json - apply
  "The Qualifying Business Activity Form model" should {
    "call apply correctly on the model" in {
      implicit val formats = Json.format[QualifyBusinessActivityModel]
      val qualifyBusinessActivityForm = QualifyBusinessActivityForm.qualifyBusinessActivityForm.fill(qualifyBusinessActivityModel)
      qualifyBusinessActivityForm.get.isQualifyBusinessActivity shouldBe Constants.qualifyTrade
    }

    // form json to model - unapply
    "call unapply successfully to create expected Json" in {
      implicit val formats = Json.format[QualifyBusinessActivityModel]
      val qualifyBusinessActivityForm = QualifyBusinessActivityForm.qualifyBusinessActivityForm.fill(qualifyBusinessActivityModel)
      val formJson = Json.toJson(qualifyBusinessActivityForm.get).toString()
      formJson shouldBe qualifyBusinessActivityJson
    }
  }
}
