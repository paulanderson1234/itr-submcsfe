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
import org.scalatestplus.play.OneAppPerSuite
import play.api.i18n.Messages
import uk.gov.hmrc.play.test.UnitSpec
import forms.ConfirmContactDetailsForm._
import models.{ConfirmContactDetailsModel, ContactDetailsModel}
import play.api.i18n.Messages.Implicits._

class ConfirmContactDetailsFormSpec extends UnitSpec with OneAppPerSuite{

  val maxEmail = "thisxx@" + ("12345678911" * 11) + ".com"

  "The test max length email" should {
    "be the ccorrect length" in {
      maxEmail.length shouldBe Constants.emailLength
    }
  }

  "Creating a form using an empty model" should {
    lazy val form = confirmContactDetailsForm
    "return an empty string for forename, surname, telephone number and email" in {
      form.data.isEmpty shouldBe true
    }
  }

  "Creating a form using a valid model" should {
    "return a form with the data specified in the model" in {
      val model = ConfirmContactDetailsModel(Constants.StandardRadioButtonYesValue,
        ContactDetailsModel("firstname", "lastname", Some("07000 111222"), None, "test@test.com"))
      val form = confirmContactDetailsForm.fill(model)
      form.data("contactDetailsUse") shouldBe Constants.StandardRadioButtonYesValue
      form.data("contactDetails.forename") shouldBe "firstname"
      form.data("contactDetails.surname") shouldBe "lastname"
      form.data("contactDetails.telephoneNumber") shouldBe "07000 111222"
      form.data("contactDetails.email") shouldBe "test@test.com"
      form.errors.length shouldBe 0
    }
  }

  "Creating a form using an invalid post" when {
    "supplied with no data for forename" should {
      lazy val form = confirmContactDetailsForm.bind(Map(
        "contactDetailsUse" -> Constants.StandardRadioButtonYesValue,
        "contactDetails.forename" -> "",
        "contactDetails.surname" -> "lastname",
        "contactDetails.telephoneNumber" -> "07000 111222",
        "contactDetails.email" -> "test@test.com")
      )
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 1 form error" in {
        form.errors.length shouldBe 1
        form.errors.head.key shouldBe "contactDetails.forename"
      }
      "associate the correct error message to the error" in {
        Messages(form.error("contactDetails.forename").get.message) shouldBe Messages("error.required")
      }
    }
  }

  "Creating a form using an invalid post" when {
    "supplied with no data for surname" should {
      lazy val form = confirmContactDetailsForm.bind(Map(
        "contactDetailsUse" -> Constants.StandardRadioButtonYesValue,
        "contactDetails.forename" -> "firstname",
        "contactDetails.surname" -> "",
        "contactDetails.telephoneNumber" -> "07000 111222",
        "contactDetails.email" -> "test@test.com")
      )
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 1 form error" in {
        form.errors.length shouldBe 1
        form.errors.head.key shouldBe "contactDetails.surname"
      }
      "associate the correct error message to the error" in {
        Messages(form.error("contactDetails.surname").get.message) shouldBe Messages("error.required")
      }
    }
  }

  "Creating a form using an invalid post" when {
    "supplied with no data for email" should {
      lazy val form = confirmContactDetailsForm.bind(Map(
        "contactDetailsUse" -> Constants.StandardRadioButtonYesValue,
        "contactDetails.forename" -> "firstname",
        "contactDetails.surname" -> "lastname",
        "contactDetails.telephoneNumber" -> "07000 111222",
        "contactDetails.email" -> "")
      )
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 1 form error" in {
        form.errors.length shouldBe 1
        form.errors.head.key shouldBe "contactDetails.email"
      }
      "associate the correct error message to the error" in {
        Messages(form.error("contactDetails.email").get.message) shouldBe Messages("validation.error.email")
      }
    }
  }

  "Creating a form using an invalid post" when {
    "supplied with no data for forename and surname" should {
      lazy val form = confirmContactDetailsForm.bind(Map(
        "contactDetailsUse" -> Constants.StandardRadioButtonYesValue,
        "contactDetails.forename" -> "",
        "contactDetails.surname" -> "",
        "contactDetails.telephoneNumber" -> "07000 111222",
        "contactDetails.email" -> "test@test.com")
      )
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 2 form errors" in {
        form.errors.length shouldBe 2
        form.errors.head.key shouldBe "contactDetails.forename"
        form.errors(1).key shouldBe "contactDetails.surname"
      }
      "associate the correct error message to the error" in {
        Messages(form.error("contactDetails.forename").get.message) shouldBe Messages("error.required")
        Messages(form.error("contactDetails.surname").get.message) shouldBe Messages("error.required")
      }
    }
  }

  "Creating a form using an invalid post" when {
    "supplied with no data for telephone number and email" should {
      lazy val form = confirmContactDetailsForm.bind(Map(
        "contactDetailsUse" -> Constants.StandardRadioButtonYesValue,
        "contactDetails.forename" -> "firstname",
        "contactDetails.surname" -> "lastname",
        "contactDetails.telephoneNumber" -> "",
        "contactDetails.email" -> "")
      )
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 1 form errors" in {
        form.errors.length shouldBe 1
        form.errors(0).key shouldBe "contactDetails.email"
      }
      "associate the correct error message to the error" in {
        form.error("contactDetails.email").get.message shouldBe Messages("validation.error.email")
      }
    }
  }

  "Creating a form using an invalid post" when {
    "supplied with no data for forename, surname or telephone number" should {
      lazy val form = confirmContactDetailsForm.bind(Map(
        "contactDetailsUse" -> Constants.StandardRadioButtonYesValue,
        "contactDetails.forename" -> "",
        "contactDetails.surname" -> "",
        "contactDetails.telephoneNumber" -> "",
        "contactDetails.email" -> "test@test.com")
      )
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 2 form errors" in {
        form.errors.length shouldBe 2
        form.errors.head.key shouldBe "contactDetails.forename"
        form.errors(1).key shouldBe "contactDetails.surname"
      }
      "associate the correct error message to the error" in {
        Messages(form.error("contactDetails.forename").get.message) shouldBe Messages("error.required")
        Messages(form.error("contactDetails.surname").get.message) shouldBe Messages("error.required")
      }
    }
  }

  "supplied with empty space for forename" should {
    lazy val form = confirmContactDetailsForm.bind(Map(
      "contactDetailsUse" -> Constants.StandardRadioButtonYesValue,
      "contactDetails.forename" -> "    ",
      "contactDetails.surname" -> "lastname",
      "contactDetails.telephoneNumber" -> "07000 111222",
      "contactDetails.email" -> "test@test.com")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "contactDetails.forename"
    }
    "associate the correct error message to the error " in {
      Messages(form.error("contactDetails.forename").get.message) shouldBe Messages("error.required")
    }
  }

  "supplied with empty space for surname" should {
    lazy val form = confirmContactDetailsForm.bind(Map(
      "contactDetailsUse" -> Constants.StandardRadioButtonYesValue,
      "contactDetails.forename" -> "firstname",
      "contactDetails.surname" -> "   ",
      "contactDetails.telephoneNumber" -> "07000 111222",
      "contactDetails.email" -> "test@test.com")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "contactDetails.surname"
    }
    "associate the correct error message to the error " in {
      Messages(form.error("contactDetails.surname").get.message) shouldBe Messages("error.required")
    }
  }

  "supplied with empty space for telephoneNumber" should {
    lazy val form = confirmContactDetailsForm.bind(Map(
      "contactDetailsUse" -> Constants.StandardRadioButtonYesValue,
      "contactDetails.forename" -> "firstname",
      "contactDetails.surname" -> "lastname",
      "contactDetails.telephoneNumber" -> "     ",
      "contactDetails.email" -> "test@test.com")
    )
    "raise no form errors" in {
      form.hasErrors shouldBe false
    }
  }

  "supplied with empty space for email" should {
    lazy val form = confirmContactDetailsForm.bind(Map(
      "contactDetailsUse" -> Constants.StandardRadioButtonYesValue,
      "contactDetails.forename" -> "firstname",
      "contactDetails.surname" -> "lastname",
      "contactDetails.telephoneNumber" -> "07000 111222",
      "contactDetails.email" -> "    ")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "contactDetails.email"
    }
    "associate the correct error message to the error " in {
      form.error("contactDetails.email").get.message shouldBe Messages("validation.error.email")
    }
  }

  "supplied with numeric input for forename" should {
    lazy val form = confirmContactDetailsForm.bind(Map(
      "contactDetailsUse" -> Constants.StandardRadioButtonYesValue,
      "contactDetails.forename" -> "firstn4me",
      "contactDetails.surname" -> "lastname",
      "contactDetails.telephoneNumber" -> "07000 111222",
      "contactDetails.email" -> "test@test.com")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  "supplied with numeric input for surname" should {
    lazy val form = confirmContactDetailsForm.bind(Map(
      "contactDetailsUse" -> Constants.StandardRadioButtonYesValue,
      "contactDetails.forename" -> "firstname",
      "contactDetails.surname" -> "lastnam3",
      "contactDetails.telephoneNumber" -> "07000 111222",
      "contactDetails.email" -> "test@test.com")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  "supplied with alphanumeric input for telephone number" should {
    lazy val form = confirmContactDetailsForm.bind(Map(
      "contactDetailsUse" -> Constants.StandardRadioButtonYesValue,
      "contactDetails.forename" -> "firstname",
      "contactDetails.surname" -> "lastname",
      "contactDetails.telephoneNumber" -> "0000 O0000I",
      "contactDetails.email" -> "test@test.com")
    )
    "raise no form errors" in {
      form.hasErrors shouldBe false
    }
  }

  "supplied with alphanumeric input for email" should {
    lazy val form = confirmContactDetailsForm.bind(Map(
      "contactDetailsUse" -> Constants.StandardRadioButtonYesValue,
      "contactDetails.forename" -> "firstname",
      "contactDetails.surname" -> "lastname",
      "contactDetails.telephoneNumber" -> "07000 111222",
      "contactDetails.email" -> "t3st@t3st.c0m")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  //  BVA

  "forename value supplied with the minimum allowed" should {
    lazy val form = confirmContactDetailsForm.bind(Map(
      "contactDetailsUse" -> Constants.StandardRadioButtonYesValue,
      "contactDetails.forename" -> "F",
      "contactDetails.surname" -> "lastname",
      "contactDetails.telephoneNumber" -> "07000 111222",
      "contactDetails.email" -> "test@test.com")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  "surname value supplied with the minimum allowed" should {
    lazy val form = confirmContactDetailsForm.bind(Map(
      "contactDetailsUse" -> Constants.StandardRadioButtonYesValue,
      "contactDetails.forename" -> "firstname",
      "contactDetails.surname" -> "L",
      "contactDetails.telephoneNumber" -> "07000 111222",
      "contactDetails.email" -> "test@test.com")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  "telephoneNumber value supplied with the minimum allowed" should {
    lazy val form = confirmContactDetailsForm.bind(Map(
      "contactDetailsUse" -> Constants.StandardRadioButtonYesValue,
      "contactDetails.forename" -> "firstname",
      "contactDetails.surname" -> "lastname",
      "contactDetails.telephoneNumber" -> "0",
      "contactDetails.email" -> "test@test.com")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  "email value supplied with the minimum allowed" should {
    lazy val form = confirmContactDetailsForm.bind(Map(
      "contactDetailsUse" -> Constants.StandardRadioButtonYesValue,
      "contactDetails.forename" -> "firstname",
      "contactDetails.surname" -> "lastname",
      "contactDetails.telephoneNumber" -> "07000 111222",
      "contactDetails.email" -> "T@t.c")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  "email value supplied over the maximum allowed (over the boundary)" should {
    lazy val form = confirmContactDetailsForm.bind(Map(
      "contactDetailsUse" -> Constants.StandardRadioButtonYesValue,
      "contactDetails.forename" -> "firstname",
      "contactDetails.surname" -> "lastname",
      "contactDetails.telephoneNumber" -> "07000 111222",
      "contactDetails.email" -> ("f" + maxEmail))
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "contactDetails.email"
    }
    "associate the correct error message to the error" in {
      form.error("contactDetails.email").get.message shouldBe Messages("validation.error.email")
    }
  }

  "forename value supplied with the maximum allowed (on the boundary)" should {
    lazy val form = confirmContactDetailsForm.bind(Map(
      "contactDetailsUse" -> Constants.StandardRadioButtonYesValue,
      "contactDetails.forename" -> MockDataGenerator.randomAlphanumericString(Constants.forenameLength),
      "contactDetails.surname" -> "lastname",
      "contactDetails.telephoneNumber" -> "07000 111222",
      "contactDetails.email" -> "test@test.com")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  "forename value supplied over the maximum allowed (over the boundary)" should {
    lazy val form = confirmContactDetailsForm.bind(Map(
      "contactDetailsUse" -> Constants.StandardRadioButtonYesValue,
      "contactDetails.forename" -> MockDataGenerator.randomNumberString(Constants.forenameLength + 1),
      "contactDetails.surname" -> "lastname",
      "contactDetails.telephoneNumber" -> "07000 111222",
      "contactDetails.email" -> "test@test.com")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "contactDetails.forename"

    }
    "associate the correct error message to the error" in {
      form.errors.head.message shouldBe "error.maxLength"
      form.errors.head.args shouldBe Array(Constants.forenameLength)
    }
  }

  "surname value supplied with the maximum allowed (on the boundary)" should {
    lazy val form = confirmContactDetailsForm.bind(Map(
      "contactDetailsUse" -> Constants.StandardRadioButtonYesValue,
      "contactDetails.forename" -> "firstname",
      "contactDetails.surname" -> MockDataGenerator.randomAlphanumericString(Constants.surnameLength),
      "contactDetails.telephoneNumber" -> "07000 111222",
      "contactDetails.email" -> "test@test.com")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  "surname value supplied over the maximum allowed (over the boundary)" should {
    lazy val form = confirmContactDetailsForm.bind(Map(
      "contactDetailsUse" -> Constants.StandardRadioButtonYesValue,
      "contactDetails.forename" -> "firstname",
      "contactDetails.surname" -> MockDataGenerator.randomNumberString(Constants.surnameLength + 1),
      "contactDetails.telephoneNumber" -> "07000 111222",
      "contactDetails.email" -> "test@test.com")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "contactDetails.surname"
    }
    "associate the correct error message to the error" in {
      form.errors.head.message shouldBe "error.maxLength"
      form.errors.head.args shouldBe Array(Constants.surnameLength)
    }
  }

  "telephoneNumber value supplied with the maximum allowed (on the boundary)" should {
    lazy val form = confirmContactDetailsForm.bind(Map(
      "contactDetailsUse" -> Constants.StandardRadioButtonYesValue,
      "contactDetails.forename" -> "firstname",
      "contactDetails.surname" -> "lastname",
      "contactDetails.telephoneNumber" -> MockDataGenerator.randomNumberString(Constants.phoneLength),
      "contactDetails.email" -> "test@test.com")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  "telephoneNumber value supplied over the maximum allowed (over the boundary)" should {
    lazy val form = confirmContactDetailsForm.bind(Map(
      "contactDetailsUse" -> Constants.StandardRadioButtonYesValue,
      "contactDetails.forename" -> "firstname",
      "contactDetails.surname" -> "lastname",
      "contactDetails.telephoneNumber" -> MockDataGenerator.randomNumberString(Constants.phoneLength + 1),
      "contactDetails.email" -> "test@test.com")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "contactDetails.telephoneNumber"
    }
    "associate the correct error message to the error" in {
      form.error("contactDetails.telephoneNumber").get.message shouldBe Messages("validation.error.telephoneNumber")
    }
  }

  "telephoneNumber value supplied over the maximum allowed (over the boundary) incluses whitespace in the count" should {
    lazy val form = confirmContactDetailsForm.bind(Map(
      "contactDetailsUse" -> Constants.StandardRadioButtonYesValue,
      "contactDetails.forename" -> "firstname",
      "contactDetails.surname" -> "lastname",
      "contactDetails.telephoneNumber" -> (MockDataGenerator.randomNumberString(Constants.phoneLength) + " "),
      "contactDetails.email" -> "test@test.com")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "contactDetails.telephoneNumber"
    }
    "associate the correct error message to the error" in {
      form.error("contactDetails.telephoneNumber").get.message shouldBe Messages("validation.error.telephoneNumber")
    }
  }

  //Telephone Number Regex

  "telephoneNumber value supplied with multiple white space" should {
    lazy val form = confirmContactDetailsForm.bind(Map(
      "contactDetailsUse" -> Constants.StandardRadioButtonYesValue,
      "contactDetails.forename" -> "firstname",
      "contactDetails.surname" -> "lastname",
      "contactDetails.telephoneNumber" -> "0 00 0 0 0 0 0 0 0 7",
      "contactDetails.email" -> "test@test.com")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  "telephoneNumber value supplied with brackets" should {
    lazy val form = confirmContactDetailsForm.bind(Map(
      "contactDetailsUse" -> Constants.StandardRadioButtonYesValue,
      "contactDetails.forename" -> "firstname",
      "contactDetails.surname" -> "lastname",
      "contactDetails.telephoneNumber" -> "(00000) 000006",
      "contactDetails.email" -> "test@test.com")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  "telephoneNumber value supplied with +44" should {
    lazy val form = confirmContactDetailsForm.bind(Map(
      "contactDetailsUse" -> Constants.StandardRadioButtonYesValue,
      "contactDetails.forename" -> "firstname",
      "contactDetails.surname" -> "lastname",
      "contactDetails.telephoneNumber" -> "+440000000005",
      "contactDetails.email" -> "test@test.com")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
    }
  }

  "telephoneNumber value supplied with /" should {
    lazy val form = confirmContactDetailsForm.bind(Map(
      "contactDetailsUse" -> Constants.StandardRadioButtonYesValue,
      "contactDetails.forename" -> "firstname",
      "contactDetails.surname" -> "lastname",
      "contactDetails.telephoneNumber" -> "0/13/84/55/33/82",
      "contactDetails.email" -> "test@test.com")
    )
    "raise no form errors" in {
      form.hasErrors shouldBe false
    }
  }

  "telephoneNumber value supplied with #" should {
    lazy val form = confirmContactDetailsForm.bind(Map(
      "contactDetailsUse" -> Constants.StandardRadioButtonYesValue,
      "contactDetails.forename" -> "firstname",
      "contactDetails.surname" -> "lastname",
      "contactDetails.telephoneNumber" -> "#00000000007",
      "contactDetails.email" -> "test@test.com")
    )
    "raise no form errors" in {
      form.hasErrors shouldBe false
    }
  }

  "telephoneNumber value supplied with *" should {
    lazy val form = confirmContactDetailsForm.bind(Map(
      "contactDetailsUse" -> Constants.StandardRadioButtonYesValue,
      "contactDetails.forename" -> "firstname",
      "contactDetails.surname" -> "lastname",
      "contactDetails.telephoneNumber" -> "#00000000008",
      "contactDetails.email" -> "test@test.com")
    )
    "raise no form errors" in {
      form.hasErrors shouldBe false
    }
  }

  "telephoneNumber value supplied with :" should {
    lazy val form = confirmContactDetailsForm.bind(Map(
      "contactDetailsUse" -> Constants.StandardRadioButtonYesValue,
      "contactDetails.forename" -> "firstname",
      "contactDetails.surname" -> "lastname",
      "contactDetails.telephoneNumber" -> "00000:000007",
      "contactDetails.email" -> "test@test.com")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "contactDetails.telephoneNumber"
    }
    "associate the correct error message to the error" in {
      form.error("contactDetails.telephoneNumber").get.message shouldBe Messages("validation.error.telephoneNumber")
    }
  }

  "telephoneNumber value supplied with - (American)" should {
    lazy val form = confirmContactDetailsForm.bind(Map(
      "contactDetailsUse" -> Constants.StandardRadioButtonYesValue,
      "contactDetails.forename" -> "firstname",
      "contactDetails.surname" -> "lastname",
      "contactDetails.telephoneNumber" -> "+1 000-000-0007",
      "contactDetails.email" -> "test@test.com")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "contactDetails.telephoneNumber"
    }
    "associate the correct error message to the error" in {
      form.error("contactDetails.telephoneNumber").get.message shouldBe Messages("validation.error.telephoneNumber")
    }
  }

  "telephoneNumber value supplied with - (France)" should {
    lazy val form = confirmContactDetailsForm.bind(Map(
      "contactDetailsUse" -> Constants.StandardRadioButtonYesValue,
      "contactDetails.forename" -> "firstname",
      "contactDetails.surname" -> "lastname",
      "contactDetails.telephoneNumber" -> "+00(0)000000008",
      "contactDetails.email" -> "test@test.com")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "contactDetails.telephoneNumber"
    }
    "associate the correct error message to the error" in {
      form.error("contactDetails.telephoneNumber").get.message shouldBe Messages("validation.error.telephoneNumber")
    }
  }

  "telephoneNumber value supplied with ext (extensions)" should {
    lazy val form = confirmContactDetailsForm.bind(Map(
      "contactDetailsUse" -> Constants.StandardRadioButtonYesValue,
      "contactDetails.forename" -> "firstname",
      "contactDetails.surname" -> "lastname",
      "contactDetails.telephoneNumber" -> "+44 0000000000 ext 123",
      "contactDetails.email" -> "test@test.com")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "contactDetails.telephoneNumber"
    }
    "associate the correct error message to the error" in {
      form.error("contactDetails.telephoneNumber").get.message shouldBe Messages("validation.error.telephoneNumber")
    }
  }

  "telephoneNumber value supplied with . " should {
    lazy val form = confirmContactDetailsForm.bind(Map(
      "contactDetailsUse" -> Constants.StandardRadioButtonYesValue,
      "contactDetails.forename" -> "firstname",
      "contactDetails.surname" -> "lastname",
      "contactDetails.telephoneNumber" -> "00000.00.00.00.00",
      "contactDetails.email" -> "test@test.com")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "contactDetails.telephoneNumber"
    }
    "associate the correct error message to the error" in {
      form.error("contactDetails.telephoneNumber").get.message shouldBe Messages("validation.error.telephoneNumber")
    }
  }

  "telephoneNumber value supplied with a leading space " should {
    lazy val form = confirmContactDetailsForm.bind(Map(
      "contactDetailsUse" -> Constants.StandardRadioButtonYesValue,
      "contactDetails.forename" -> "firstname",
      "contactDetails.surname" -> "lastname",
      "contactDetails.telephoneNumber" -> " 07000 111222",
      "contactDetails.email" -> "test@test.com")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  "telephoneNumber value supplied with a trailing space " should {
    lazy val form = confirmContactDetailsForm.bind(Map(
      "contactDetailsUse" -> Constants.StandardRadioButtonYesValue,
      "contactDetails.forename" -> "firstname",
      "contactDetails.surname" -> "lastname",
      "contactDetails.telephoneNumber" -> "07000 111222 ",
      "contactDetails.email" -> "test@test.com")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
  }

  //Email Regex

  "email supplied with multiple white spaces" should {
    lazy val form = confirmContactDetailsForm.bind(Map(
      "contactDetailsUse" -> Constants.StandardRadioButtonYesValue,
      "contactDetails.forename" -> "firstname",
      "contactDetails.surname" -> "lastname",
      "contactDetails.telephoneNumber" -> "07000 111222",
      "contactDetails.email" -> "Te st@tes t.com")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "contactDetails.email"
    }
    "associate the correct error message to the error" in {
      form.error("contactDetails.email").get.message shouldBe Messages("validation.error.email")
    }
  }

  "email supplied with multiple @" should {
    lazy val form = confirmContactDetailsForm.bind(Map(
      "contactDetailsUse" -> Constants.StandardRadioButtonYesValue,
      "contactDetails.forename" -> "firstname",
      "contactDetails.surname" -> "lastname",
      "contactDetails.telephoneNumber" -> "07000 111222",
      "contactDetails.email" -> "test@test@test.co.uk")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "contactDetails.email"
    }
    "associate the correct error message to the error" in {
      form.error("contactDetails.email").get.message shouldBe Messages("validation.error.email")
    }
  }

  "email supplied without @" should {
    lazy val form = confirmContactDetailsForm.bind(Map(
      "contactDetailsUse" -> Constants.StandardRadioButtonYesValue,
      "contactDetails.forename" -> "firstname",
      "contactDetails.surname" -> "lastname",
      "contactDetails.telephoneNumber" -> "07000 111222",
      "contactDetails.email" -> "test.com")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "contactDetails.email"
    }
    "associate the correct error message to the error" in {
      form.error("contactDetails.email").get.message shouldBe Messages("validation.error.email")
    }
  }

  "email supplied with sub domain" should {
    lazy val form = confirmContactDetailsForm.bind(Map(
      "contactDetailsUse" -> Constants.StandardRadioButtonYesValue,
      "contactDetails.forename" -> "firstname",
      "contactDetails.surname" -> "lastname",
      "contactDetails.telephoneNumber" -> "07000 111222",
      "contactDetails.email" -> "test@subdomain.ntlworld.com")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form error" in {
      form.errors.length shouldBe 0
    }
  }

  "email supplied with firstname.lastname@" should {
    lazy val form = confirmContactDetailsForm.bind(Map(
      "contactDetailsUse" -> Constants.StandardRadioButtonYesValue,
      "contactDetails.forename" -> "firstname",
      "contactDetails.surname" -> "lastname",
      "contactDetails.telephoneNumber" -> "07000 111222",
      "contactDetails.email" -> "firstname.lastname@test.com")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form error" in {
      form.errors.length shouldBe 0
    }
  }

  "email supplied with forename surname <email@example.com>" should {
    lazy val form = confirmContactDetailsForm.bind(Map(
      "contactDetailsUse" -> Constants.StandardRadioButtonYesValue,
      "contactDetails.forename" -> "firstname",
      "contactDetails.surname" -> "lastname",
      "contactDetails.telephoneNumber" -> "07000 111222",
      "contactDetails.email" -> "firstname lastname <firstname.lastname@test.com>")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "contactDetails.email"
    }
    "associate the correct error message to the error" in {
      form.error("contactDetails.email").get.message shouldBe Messages("validation.error.email")
    }
  }

  "email supplied with firstname_lastname@" should {
    lazy val form = confirmContactDetailsForm.bind(Map(
      "contactDetailsUse" -> Constants.StandardRadioButtonYesValue,
      "contactDetails.forename" -> "firstname",
      "contactDetails.surname" -> "lastname",
      "contactDetails.telephoneNumber" -> "07000 111222",
      "contactDetails.email" -> "Test_test@test.com")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form error" in {
      form.errors.length shouldBe 0
    }
  }

  "Part 1 - minimum allowed supplied for email (on boundary) " should {
    lazy val form = confirmContactDetailsForm.bind(Map(
      "contactDetailsUse" -> Constants.StandardRadioButtonYesValue,
      "contactDetails.forename" -> "firstname",
      "contactDetails.surname" -> "lastname",
      "contactDetails.telephoneNumber" -> "00000 000001",
      "contactDetails.email" -> "F@t.c")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form error" in {
      form.errors.length shouldBe 0
    }
  }

  "Part 1 - nothing supplied for first part of the email (under the boundary) " should {
    lazy val form = confirmContactDetailsForm.bind(Map(
      "contactDetailsUse" -> Constants.StandardRadioButtonYesValue,
      "contactDetails.forename" -> "firstname",
      "contactDetails.surname" -> "lastname",
      "contactDetails.telephoneNumber" -> "00000 000002",
      "contactDetails.email" -> "@t.c")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "contactDetails.email"
    }
    "associate the correct error message to the error" in {
      form.error("contactDetails.email").get.message shouldBe Messages("validation.error.email")
    }
  }

  "Part 1 - maximum allowed supplied for email (on boundary) " should {
    lazy val form = confirmContactDetailsForm.bind(Map(
      "contactDetailsUse" -> Constants.StandardRadioButtonYesValue,
      "contactDetails.forename" -> "firstname",
      "contactDetails.surname" -> "lastname",
      "contactDetails.telephoneNumber" -> "07000 111222",
      "contactDetails.email" -> maxEmail)
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form error" in {
      form.errors.length shouldBe 0
    }
  }

  "Part 1 - too many characters supplied for the first part of the email (over the boundary) " should {
    lazy val form = confirmContactDetailsForm.bind(Map(
      "contactDetailsUse" -> Constants.StandardRadioButtonYesValue,
      "contactDetails.forename" -> "firstname",
      "contactDetails.surname" -> "lastname",
      "contactDetails.telephoneNumber" -> "07000 111222",
      "contactDetails.email" -> s"1$maxEmail")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "contactDetails.email"
    }
    "associate the correct error message to the error" in {
      form.error("contactDetails.email").get.message shouldBe Messages("validation.error.email")
    }
  }

  "Part 2 - minimum allowed supplied for email (on boundary)" should {
    lazy val form = confirmContactDetailsForm.bind(Map(
      "contactDetailsUse" -> Constants.StandardRadioButtonYesValue,
      "contactDetails.forename" -> "firstname",
      "contactDetails.surname" -> "lastname",
      "contactDetails.telephoneNumber" -> "07000 111222",
      "contactDetails.email" -> "f@P.c")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form error" in {
      form.errors.length shouldBe 0
    }
  }

  "Part 2 - nothing supplied for second part of the email (under the boundary) " should {
    lazy val form = confirmContactDetailsForm.bind(Map(
      "contactDetailsUse" -> Constants.StandardRadioButtonYesValue,
      "contactDetails.forename" -> "firstname",
      "contactDetails.surname" -> "lastname",
      "contactDetails.telephoneNumber" -> "07000 111222",
      "contactDetails.email" -> "f.f@")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "contactDetails.email"
    }
    "associate the correct error message to the error" in {
      form.error("contactDetails.email").get.message shouldBe Messages("validation.error.email")
    }
  }

  "Nothing supplied for contact detail use" should {
    lazy val form = confirmContactDetailsForm.bind(Map(
      "contactDetailsUse" -> "",
      "contactDetails.forename" -> "firstname",
      "contactDetails.surname" -> "lastname",
      "contactDetails.telephoneNumber" -> "07000 111222",
      "contactDetails.email" -> "test@test.com")
    )

    "raise form error" in {
      form.hasErrors shouldBe true
    }

    "associate the correct error message with the error" in {
      form.error("contactDetailsUse").get.message shouldBe "error.required"
    }
  }

}
