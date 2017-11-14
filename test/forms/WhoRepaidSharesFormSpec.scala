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
import forms.WhoRepaidSharesForm._
import models.repayments.WhoRepaidSharesModel
import org.scalatestplus.play.OneAppPerSuite
import play.api.i18n.Messages
import uk.gov.hmrc.play.test.UnitSpec
import play.api.i18n.Messages.Implicits._

class WhoRepaidSharesFormSpec extends UnitSpec with OneAppPerSuite {

  "Creating a form using an empty model" should {
    lazy val form = whoRepaidSharesForm
    "return an empty string for forename, surname" in {
      form.data.isEmpty shouldBe true
    }
  }

  "Creating a form using a valid model" should {
    "return a form with the data specified in the model" in {
      val model = WhoRepaidSharesModel(forename = "Bill", surname = "Smith")
      lazy val form = whoRepaidSharesForm.fill(model)

      form.data("forename") shouldBe "Bill"
      form.data("surname") shouldBe "Smith"
      form.errors.length shouldBe 0
      form.data.size shouldBe 2
    }
  }

  "Creating a form using an invalid post" when {
    "supplied with no data for forename" should {
      lazy val form = whoRepaidSharesForm.bind(Map(
        "forename" -> "",
        "surname" -> "Smith")
      )
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 1 form error" in {
        form.errors.head.key shouldBe "forename"
      }
      "associate the correct error message to the error" in {
        Messages(form.error("forename").get.message) shouldBe Messages("error.required")
      }
    }
  }

  "Creating a form using an invalid post" when {
    "supplied with no data for surname" should {
      lazy val form = whoRepaidSharesForm.bind(Map(
        "forename" -> "Bill",
        "surname" -> "")
      )
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 1 form error" in {

        form.errors.length shouldBe 1

        form.errors.head.key shouldBe "surname"
      }
      "associate the correct error message to the error" in {
        Messages(form.error("surname").get.message) shouldBe Messages("error.required")
      }
    }
  }

  "Creating a form using an invalid post" when {
    "supplied with no data for firstname and surname" should {
      lazy val form = whoRepaidSharesForm.bind(Map(
        "forename" -> "",
        "surname" -> "")
      )
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 2 form errors" in {
        form.errors.length shouldBe 2
        form.errors.head.key shouldBe "forename"
        form.errors(1).key shouldBe "surname"
      }
      "associate the correct error message to the error" in {
        Messages(form.error("forename").get.message) shouldBe Messages("error.required")
        Messages(form.error("surname").get.message) shouldBe Messages("error.required")
      }
    }
  }

  "supplied with empty space for forename" should {
    lazy val form = whoRepaidSharesForm.bind(Map(
      "forename" -> " ",
      "surname" -> "Smith")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "forename"
    }
    "associate the correct error message to the error " in {
      Messages(form.error("forename").get.message) shouldBe Messages("error.required")
    }
  }


  "supplied with empty space for surname" should {
    lazy val form = whoRepaidSharesForm.bind(Map(
      "forename" -> "Bill",
      "surname" -> " ")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "surname"
    }
    "associate the correct error message to the error " in {
      Messages(form.error("surname").get.message) shouldBe Messages("error.required")
    }
  }

  "supplied with numeric input for forename" should {
    lazy val form = whoRepaidSharesForm.bind(Map(
      "forename" -> "Bill 260",
      "surname" -> "Smith")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  "forename and surname value supplied with the minimum allowed" should {
    lazy val form = whoRepaidSharesForm.bind(Map(
      "forename" -> "A",
      "surname" -> "A")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  "forename and surname value supplied with the maximum allowed (on the boundary)" should {
    lazy val form = whoRepaidSharesForm.bind(Map(
      "forename" -> MockDataGenerator.randomAlphanumericString(Constants.forenameLength),
      "surname" -> MockDataGenerator.randomAlphanumericString(Constants.surnameLength))
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  "forename value supplied over the maximum allowed (over the boundary)" should {
    lazy val form = whoRepaidSharesForm.bind(Map(
      "forename" -> MockDataGenerator.randomAlphanumericString(Constants.forenameLength + 1),
      "surname" -> "Smith")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "forename"
    }
    "associate the correct error message to the error" in {
      form.errors.head.message shouldBe "error.maxLength"
      form.errors.head.args shouldBe Array(Constants.surnameLength)
    }
  }

  "surname value supplied over the maximum allowed (over the boundary)" should {
    lazy val form = whoRepaidSharesForm.bind(Map(
      "forename" -> "Bill",
      "surname" -> MockDataGenerator.randomAlphanumericString(Constants.surnameLength + 1))
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "surname"
    }
    "associate the correct error message to the error" in {
      Messages(form.error("surname").get.message) shouldBe Messages("error.maxLength")
    }
  }

}
