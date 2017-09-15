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
import models.repayments.SharesRepaymentTypeModel
import org.scalatestplus.play.OneAppPerSuite
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.UnitSpec

class SharesRepaymentTypeFormSpec extends UnitSpec with OneAppPerSuite{

  private def bindSuccess(request: FakeRequest[AnyContentAsFormUrlEncoded]) = {
    SharesRepaymentTypeForm.sharesRepaymentTypeForm.bindFromRequest()(request).fold(
      formWithErrors => None,
      userData => Some(userData)
    )
  }

  private def bindWithError(request: FakeRequest[AnyContentAsFormUrlEncoded]): Option[FormError] = {
    SharesRepaymentTypeForm.sharesRepaymentTypeForm.bindFromRequest()(request).fold(
      formWithErrors => Some(formWithErrors.errors(0)),
      userData => None
    )
  }

  val sharesRepaymentTypeJson = s"""{"sharesRepaymentType":"${Constants.repaymentTypeShares}"}"""
  val sharesRepaymentTypeModel = SharesRepaymentTypeModel(Constants.repaymentTypeShares)

  "The Shares Repayment Type Form" should {
    "Return an error if no radio button is selected" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "sharesRepaymentType" -> ""
      )
      bindWithError(request) match {
        case Some(err) => {
          err.key shouldBe "sharesRepaymentType"
          Messages(err.message) shouldBe Messages("error.required")
          err.args shouldBe Array()
        }
        case _ => {
          fail("Missing error")
        }
      }
    }
  }

  "The Shares Repayment Type Form" should {
    "not return an error if the 'Shares' option is selected" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "sharesRepaymentType" -> Constants.repaymentTypeShares
      )
      bindWithError(request) match {
        case Some(err) => {
          fail("Validation error not expected")
        }
        case _ => ()
      }
    }
  }

  "The Shares Repayment Type Form" should {
    "not return an error if the 'Debentures' option is selected" in {
      val request = FakeRequest("GET", "/").withFormUrlEncodedBody(
        "sharesRepaymentType" -> Constants.repaymentTypeDebentures
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
  "The Shares Repayment Type model" should {
    "load convert to JSON successfully" in {

      implicit val formats = Json.format[SharesRepaymentTypeModel]

      val sharesRepaymentType = Json.toJson(sharesRepaymentTypeModel).toString()
      sharesRepaymentType shouldBe sharesRepaymentTypeJson

    }
  }

  // form model to json - apply
  "The Shares Repayment Type Form model" should {
    "call apply correctly on the model" in {
      implicit val formats = Json.format[SharesRepaymentTypeModel]
      val sharesRepaymentTypeForm = SharesRepaymentTypeForm.sharesRepaymentTypeForm.fill(sharesRepaymentTypeModel)
      sharesRepaymentTypeForm.get.sharesRepaymentType shouldBe Constants.repaymentTypeShares
    }

    // form json to model - unapply
    "call unapply successfully to create expected Json" in {
      implicit val formats = Json.format[SharesRepaymentTypeModel]
      val sharesRepaymentTypeForm = SharesRepaymentTypeForm.sharesRepaymentTypeForm.fill(sharesRepaymentTypeModel)
      val formJson = Json.toJson(sharesRepaymentTypeForm.get).toString()
      formJson shouldBe sharesRepaymentTypeJson
    }
  }
}
