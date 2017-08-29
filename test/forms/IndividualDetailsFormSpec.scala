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
import forms.IndividualDetailsForm._
import models.IndividualDetailsModel
import org.scalatestplus.play.OneAppPerSuite
import play.api.i18n.Messages
import uk.gov.hmrc.play.test.UnitSpec
import play.api.i18n.Messages.Implicits._

class IndividualDetailsFormSpec extends UnitSpec with OneAppPerSuite{

  "Creating a form using an empty model" should {
    lazy val form = individualDetailsForm
    "return an empty string for forename, surname, addressline1, addressline2, adressline3, addressLine4, postcode, countryCode" in {
      form.data.isEmpty shouldBe true
    }
  }

  "Creating a form using a valid model" should {
    "return a form with the data specified in the model" in {
      val model = IndividualDetailsModel(forename =  "Bill", surname =  "Smith",  addressline1 =  "line1",
        addressline2 =  "line2", addressline3 = Some("line3"), addressline4 = Some("line4"),None,"JP", Some(1))
      lazy val form = individualDetailsForm.fill(model)

      form.data("forename") shouldBe "Bill"
      form.data("surname") shouldBe "Smith"
      form.data("addressline1") shouldBe "line1"
      form.data("addressline2") shouldBe "line2"
      form.data("addressline3") shouldBe "line3"
      form.data("addressline4") shouldBe "line4"
      form.data("countryCode") shouldBe "JP"
      form.data("processingId") shouldBe "1"
      form.errors.length shouldBe 0
      form.data.size shouldBe 8
    }
  }

  "Creating a form using an invalid post" when {
    "supplied with no data for forename" should {
      lazy val form = individualDetailsForm.bind(Map(
        "forename" -> "",
        "surname" -> "Smith",
        "addressline1" -> "line1",
        "addressline2" -> "line2",
        "addressline3" -> "",
        "addressline4" -> "",
        "postcode" -> "",
        "countryCode" -> "JP")
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
      lazy val form = individualDetailsForm.bind(Map(
        "forename" -> "Bill",
        "surname" -> "",
        "addressline1" -> "line1",
        "addressline2" -> "line2",
        "addressline3" -> "",
        "addressline4" -> "",
        "postcode" -> "",
        "countryCode" -> "JP")
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
    "supplied with no data for addressline1" should {
      lazy val form = individualDetailsForm.bind(Map(
        "forename" -> "Bill",
        "surname" -> "Smith",
        "addressline1" -> "",
        "addressline2" -> "line2",
        "addressline3" -> "",
        "addressline4" -> "",
        "postcode" -> "",
        "countryCode" -> "JP")
      )
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 1 form error" in {

        form.errors.length shouldBe 1

        form.errors.head.key shouldBe "addressline1"
      }
      "associate the correct error message to the error" in {
        form.error("addressline1").get.message shouldBe Messages("validation.error.mandatoryaddresssline")
      }
    }
  }

  "Creating a form using an invalid post" when {
    "supplied with no data for addressline2" should {
      lazy val form = individualDetailsForm.bind(Map(
        "forename" -> "Bill",
        "surname" -> "Smith",
        "addressline1" -> "line1",
        "addressline2" -> "",
        "addressline3" -> "",
        "addressline4" -> "",
        "postcode" -> "",
        "countryCode" -> "JP")
      )
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 1 form error" in {
        form.errors.length shouldBe 1
        form.errors.head.key shouldBe "addressline2"
      }
      "associate the correct error message to the error" in {
        form.error("addressline2").get.message shouldBe Messages("validation.error.mandatoryaddresssline")
      }
    }
  }

  "Creating a form using an invalid post" when {
    "supplied with no data for country" should {
      lazy val form = individualDetailsForm.bind(Map(
        "forename" -> "Bill",
        "surname" -> "Smith",
        "addressline1" -> "line1",
        "addressline2" -> "line2",
        "addressline3" -> "",
        "addressline4" -> "",
        "postcode" -> "",
        "countryCode" -> "")
      )
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 1 form error" in {
        form.errors.length shouldBe 1
        form.errors.head.key shouldBe "countryCode"
      }
      "associate the correct error message to the error" in {
        form.error("countryCode").get.message shouldBe Messages("validation.error.countryCode")
      }
    }
  }

  "Creating a form using an invalid post" when {
    "supplied with no data for forename and addressline1" should {
      lazy val form = individualDetailsForm.bind(Map(
        "forename" -> "",
        "surname" -> "Smith",
        "addressline1" -> "",
        "addressline2" -> "line2",
        "addressline3" -> "",
        "addressline4" -> "",
        "postcode" -> "",
        "countryCode" -> "JP")
      )
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 2 form errors" in {
        form.errors.length shouldBe 2
        form.errors.head.key shouldBe "forename"
        form.errors(1).key shouldBe "addressline1"
      }
      "associate the correct error message to the error" in {
        Messages(form.error("forename").get.message) shouldBe Messages("error.required")
        form.error("addressline1").get.message shouldBe Messages("validation.error.mandatoryaddresssline")
      }
    }
  }


  "Creating a form using an invalid post" when {
    "supplied with no data for addressline1 and addressline2" should {
      lazy val form = individualDetailsForm.bind(Map(
        "forename" -> "Bill",
        "surname" -> "Smith",
        "addressline1" -> "",
        "addressline2" -> "",
        "addressline3" -> "",
        "addressline4" -> "",
        "postcode" -> "",
        "countryCode" -> "JP")
      )
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 2 form errors" in {
        form.errors.length shouldBe 2
        form.errors.head.key shouldBe "addressline1"
        form.errors(1).key shouldBe "addressline2"
      }
      "associate the correct error message to the error" in {
        form.error("addressline1").get.message shouldBe Messages("validation.error.mandatoryaddresssline")
        form.error("addressline2").get.message shouldBe Messages("validation.error.mandatoryaddresssline")
      }
    }
  }

  "Creating a form using an invalid post" when {
    "supplied with no data for addressline2 and country" should {
      lazy val form = individualDetailsForm.bind(Map(
        "forename" -> "Bill",
        "surname" -> "Smith",
        "addressline1" -> "line1",
        "addressline2" -> "",
        "addressline3" -> "",
        "addressline4" -> "",
        "postcode" -> "",
        "countryCode" -> "")
      )
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 2 form errors" in {
        form.errors.length shouldBe 2
        form.errors.head.key shouldBe "addressline2"
        form.errors(1).key shouldBe "countryCode"
      }
      "associate the correct error message to the error" in {
        form.error("addressline2").get.message shouldBe Messages("validation.error.mandatoryaddresssline")
        form.error("countryCode").get.message shouldBe Messages("validation.error.countryCode")
      }
    }
  }


  "Creating a form using an invalid post" when {
    "supplied with no data for forename, addressline1 or addressline2" should {
      lazy val form = individualDetailsForm.bind(Map(
        "forename" -> "",
        "surname" -> "Smith",
        "addressline1" -> "",
        "addressline2" -> "",
        "addressline3" -> "",
        "addressline4" -> "",
        "postcode" -> "",
        "countryCode" -> "JP")
      )
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 3 form errors" in {
        form.errors.length shouldBe 3
        form.errors.head.key shouldBe "forename"
        form.errors(1).key shouldBe "addressline1"
        form.errors(2).key shouldBe "addressline2"
      }
      "associate the correct error message to the error" in {
        Messages(form.error("forename").get.message) shouldBe Messages("error.required")
        form.error("addressline1").get.message shouldBe Messages("validation.error.mandatoryaddresssline")
        form.error("addressline2").get.message shouldBe Messages("validation.error.mandatoryaddresssline")
      }
    }
  }


  "Creating a form using an invalid post" when {
    "supplied with no data for addressline1, addressline2 or country" should {
      lazy val form = individualDetailsForm.bind(Map(
        "forename" -> "Bill",
        "surname" -> "Smith",
        "addressline1" -> "",
        "addressline2" -> "",
        "addressline3" -> "",
        "addressline4" -> "",
        "postcode" -> "",
        "countryCode" -> "")
      )
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 3 form errors" in {
        form.errors.length shouldBe 3
        form.errors.head.key shouldBe "addressline1"
        form.errors(1).key shouldBe "addressline2"
        form.errors(2).key shouldBe "countryCode"
      }
      "associate the correct error message to the error" in {
        form.error("addressline1").get.message shouldBe Messages("validation.error.mandatoryaddresssline")
        form.error("addressline2").get.message shouldBe Messages("validation.error.mandatoryaddresssline")
        form.error("countryCode").get.message shouldBe Messages("validation.error.countryCode")
      }
    }
  }

  "Creating a form using an invalid post" when {
    "supplied with no data for forename, addressline1, addressline2 or country" should {
      lazy val form = individualDetailsForm.bind(Map(
        "forename" -> "",
        "surname" -> "Smith",
        "addressline1" -> "",
        "addressline2" -> "",
        "addressline3" -> "",
        "addressline4" -> "",
        "postcode" -> "",
        "countryCode" -> "")
      )
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 4 form errors" in {
        form.errors.length shouldBe 4
        form.errors.head.key shouldBe "forename"
        form.errors(1).key shouldBe "addressline1"
        form.errors(2).key shouldBe "addressline2"
        form.errors(3).key shouldBe "countryCode"
      }
      "associate the correct error message to the error" in {
        Messages(form.error("forename").get.message) shouldBe Messages("error.required")
        form.error("addressline1").get.message shouldBe Messages("validation.error.mandatoryaddresssline")
        form.error("addressline2").get.message shouldBe Messages("validation.error.mandatoryaddresssline")
        form.error("countryCode").get.message shouldBe Messages("validation.error.countryCode")
      }
    }
  }

  "supplied with empty space for forename" should {
    lazy val form = individualDetailsForm.bind(Map(
      "forename" -> " ",
      "surname" -> "Smith",
      "addressline1" -> "line1",
      "addressline2" -> "line2",
      "addressline3" -> "",
      "addressline4" -> "",
      "postcode" -> "",
      "countryCode" -> "JP")
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


  "supplied with empty space for addressline1" should {
    lazy val form = individualDetailsForm.bind(Map(
      "forename" -> "Bill",
      "surname" -> "Smith",
      "addressline1" -> "   ",
      "addressline2" -> "line2",
      "addressline3" -> "",
      "addressline4" -> "",
      "postcode" -> "",
      "countryCode" -> "JP")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "addressline1"
    }
    "associate the correct error message to the error " in {
      form.error("addressline1").get.message shouldBe Messages("validation.error.mandatoryaddresssline")
    }
  }

  "supplied with empty space for addressline2" should {
    lazy val form = individualDetailsForm.bind(Map(
      "forename" -> "Bill",
      "surname" -> "Smith",
      "addressline1" -> "line1",
      "addressline2" -> "   ",
      "addressline3" -> "",
      "addressline4" -> "",
      "postcode" -> "",
      "countryCode" -> "JP")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "addressline2"
    }
    "associate the correct error message to the error " in {
      form.error("addressline2").get.message shouldBe Messages("validation.error.mandatoryaddresssline")
    }
  }

  "supplied with empty space for addressline3" should {
    lazy val form = individualDetailsForm.bind(Map(
      "forename" -> "Bill",
      "surname" -> "Smith",
      "addressline1" -> "line1",
      "addressline2" -> "line2",
      "addressline3" -> "   ",
      "addressline4" -> "",
      "postcode" -> "",
      "countryCode" -> "JP")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "addressline3"
    }
    "associate the correct error message to the error " in {
      form.error("addressline3").get.message shouldBe Messages("validation.error.optionaladdresssline")
    }
  }

  "supplied with empty space for addressline4" should {
    lazy val form = individualDetailsForm.bind(Map(
      "forename" -> "Bill",
      "surname" -> "Smith",
      "addressline1" -> "line1",
      "addressline2" -> "line2",
      "addressline3" -> "",
      "addressline4" -> "   ",
      "postcode" -> "",
      "countryCode" -> "JP")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "addressline4"
    }
    "associate the correct error message to the error " in {
      form.error("addressline4").get.message shouldBe Messages("validation.error.linefouraddresssline")
    }
  }

  "supplied with empty space for country" should {
    lazy val form = individualDetailsForm.bind(Map(
      "forename" -> "Bill",
      "surname" -> "Smith",
      "addressline1" -> "line1",
      "addressline2" -> "line2",
      "addressline3" -> "",
      "addressline4" -> "",
      "postcode" -> "",
      "countryCode" -> "   ")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form errors" in {
      form.errors.length shouldBe 1
    }
    "associate the correct error message to the error" in {
      form.error("countryCode").get.message shouldBe Messages("validation.error.countryCode")
    }
  }

  "supplied with empty space for postcode" should {
    lazy val form = individualDetailsForm.bind(Map(
      "forename" -> "Bill",
      "surname" -> "Smith",
      "addressline1" -> "line1",
      "addressline2" -> "line2",
      "addressline3" -> "",
      "addressline4" -> "",
      "postcode" -> "   ",
      "countryCode" -> "GB")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form errors" in {
      form.errors.length shouldBe 1
    }
    "associate the correct error message to the error" in {
      form.error("postcode").get.message shouldBe Messages("validation.error.postcode")
    }
  }

  "supplied with numeric input for forename" should {
    lazy val form = individualDetailsForm.bind(Map(
      "forename" -> "Bill 260",
      "surname" -> "Smith",
      "addressline1" -> "line1",
      "addressline2" -> "line2",
      "addressline3" -> "",
      "addressline4" -> "",
      "postcode" -> "",
      "countryCode" -> "JP")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }


  "supplied with numeric input for addressline1" should {
    lazy val form = individualDetailsForm.bind(Map(
      "forename" -> "Bill",
      "surname" -> "Smith",
      "addressline1" -> "line1 86",
      "addressline2" -> "line2",
      "addressline3" -> "",
      "addressline4" -> "",
      "postcode" -> "",
      "countryCode" -> "JP")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  "supplied with numeric input for addressline2" should {
    lazy val form = individualDetailsForm.bind(Map(
      "forename" -> "Bill",
      "surname" -> "Smith",
      "addressline1" -> "line1",
      "addressline2" -> "line2 86",
      "addressline3" -> "",
      "addressline4" -> "",
      "postcode" -> "",
      "countryCode" -> "JP")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  "supplied with numeric input for addressline3" should {
    lazy val form = individualDetailsForm.bind(Map(
      "forename" -> "Bill",
      "surname" -> "Smith",
      "addressline1" -> "line1",
      "addressline2" -> "line2",
      "addressline3" -> "86",
      "addressline4" -> "",
      "postcode" -> "",
      "countryCode" -> "JP")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  "supplied with numeric input for addressline4" should {
    lazy val form = individualDetailsForm.bind(Map(
      "forename" -> "Bill",
      "surname" -> "Smith",
      "addressline1" -> "line1",
      "addressline2" -> "line2",
      "addressline3" -> "",
      "addressline4" -> "86",
      "postcode" -> "",
      "countryCode" -> "JP")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  "supplied with numeric input for postcode" should {
    lazy val form = individualDetailsForm.bind(Map(
      "forename" -> "Bill",
      "surname" -> "Smith",
      "addressline1" -> "line1",
      "addressline2" -> "line2",
      "addressline3" -> "",
      "addressline4" -> "",
      "postcode" -> "86",
      "countryCode" -> "GB")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "postcode"
    }
    "associate the correct error message to the error" in {
      form.error("postcode").get.message shouldBe Messages("validation.error.postcode")
    }
  }

  "supplied with alphanumeric input for country" should {
    lazy val form = individualDetailsForm.bind(Map(
      "forename" -> "Bill",
      "surname" -> "Smith",
      "addressline1" -> "line1",
      "addressline2" -> "line2",
      "addressline3" -> "",
      "addressline4" -> "",
      "postcode" -> "",
      "countryCode" -> "J4pan")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "countryCode"
    }
    "associate the correct error message to the error" in {
      form.error("countryCode").get.message shouldBe Messages("validation.error.countryCode")
    }
  }

  //  BVA


  "forename  and surname value supplied with the minimum allowed" should {
    lazy val form = individualDetailsForm.bind(Map(
      "forename" -> "A",
      "surname" -> "A",
      "addressline1" -> "line1",
      "addressline2" -> "line2",
      "addressline3" -> "",
      "addressline4" -> "",
      "postcode" -> "",
      "countryCode" -> "JP")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  "addressline1 value supplied with the minimum allowed" should {
    lazy val form = individualDetailsForm.bind(Map(
      "forename" -> "Bill",
      "surname" -> "Smith",
      "addressline1" -> "A",
      "addressline2" -> "line2",
      "addressline3" -> "",
      "addressline4" -> "",
      "postcode" -> "",
      "countryCode" -> "JP")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  "addressline2 value supplied with the minimum allowed" should {
    lazy val form = individualDetailsForm.bind(Map(
      "forename" -> "Bill",
      "surname" -> "Smith",
      "addressline1" -> "line1",
      "addressline2" -> "M",
      "addressline3" -> "",
      "addressline4" -> "",
      "postcode" -> "",
      "countryCode" -> "JP")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  "addressline3 value supplied with the minimum allowed" should {
    lazy val form = individualDetailsForm.bind(Map(
      "forename" -> "Bill",
      "surname" -> "Smith",
      "addressline1" -> "line1",
      "addressline2" -> "line2",
      "addressline3" -> "A",
      "addressline4" -> "",
      "postcode" -> "",
      "countryCode" -> "JP")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  "addressline4 value supplied with the minimum allowed" should {
    lazy val form = individualDetailsForm.bind(Map(
      "forename" -> "Bill",
      "surname" -> "Smith",
      "addressline1" -> "line1",
      "addressline2" -> "line2",
      "addressline3" -> "",
      "addressline4" -> "A",
      "postcode" -> "",
      "countryCode" -> "JP")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  "country value supplied with the minimum allowed" should {
    lazy val form = individualDetailsForm.bind(Map(
      "forename" -> "Bill",
      "surname" -> "Smith",
      "addressline1" -> "line1",
      "addressline2" -> "line2",
      "addressline3" -> "",
      "addressline4" -> "",
      "postcode" -> "",
      "countryCode" -> "JP")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }


  "forename and surname value supplied with the maximum allowed (on the boundary)" should {
    lazy val form = individualDetailsForm.bind(Map(
      "forename" -> MockDataGenerator.randomAlphanumericString(Constants.forenameLength),
      "surname" -> MockDataGenerator.randomAlphanumericString(Constants.forenameLength),
      "addressline1" -> "line1",
      "addressline2" -> "line2",
      "addressline3" -> "",
      "addressline4" -> "",
      "postcode" -> "",
      "countryCode" -> "JP")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }


  "all addresslines values supplied with the maximum allowed (on the boundary)" should {
    lazy val form = individualDetailsForm.bind(Map(
      "forename" -> "Bill",
      "surname" -> "Smith",
      "addressline1" -> MockDataGenerator.randomAlphanumericString(Constants.addressLineLength),
      "addressline2" -> MockDataGenerator.randomAlphanumericString(Constants.addressLineLength),
      "addressline3" -> MockDataGenerator.randomAlphanumericString(Constants.addressLineLength),
      "addressline4" -> MockDataGenerator.randomAlphanumericString(Constants.addressLineLength),
      "postcode" -> "",
      "countryCode" -> "JP")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }


  "postcode value supplied with the maximum allowed (on the boundary)" should {
    lazy val form = individualDetailsForm.bind(Map(
      "forename" -> "Bill",
      "surname" -> "Smith",
      "addressline1" -> "line1",
      "addressline2" -> "line2",
      "addressline3" -> "",
      "addressline4" -> "",
      "postcode" -> "BS98 1TL",
      "countryCode" -> "GB")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  "country value supplied with the maximum allowed (on the boundary)" should {
    lazy val form = individualDetailsForm.bind(Map(
      "forename" -> "Bill",
      "surname" -> "Smith",
      "addressline1" -> "line1",
      "addressline2" -> "line2",
      "addressline3" -> "",
      "addressline4" -> "",
      "postcode" -> "",
      "countryCode" -> "TB")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  "country value supplied over the maximum allowed (over the boundary)" should {
    lazy val form = individualDetailsForm.bind(Map(
      "forename" -> "Bill",
      "surname" -> "Smith",
      "addressline1" -> "line1",
      "addressline2" -> "line2",
      "addressline3" -> "",
      "addressline4" -> "",
      "postcode" -> "",
      "countryCode" -> "Trinidad and Tobagooo")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "countryCode"
    }
    "associate the correct error message to the error" in {
      form.error("countryCode").get.message shouldBe Messages("validation.error.countryCode")
    }
  }

  "forename value supplied over the maximum allowed (over the boundary)" should {
    lazy val form = individualDetailsForm.bind(Map(
      "forename" -> MockDataGenerator.randomAlphanumericString(Constants.forenameLength + 1),
      "surname" -> "Smith",
      "addressline1" -> "line1",
      "addressline2" -> "line2",
      "addressline3" -> "",
      "addressline4" -> "",
      "postcode" -> "",
      "countryCode" -> "JP")
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
    lazy val form = individualDetailsForm.bind(Map(
      "forename" -> "Bill",
      "surname" -> MockDataGenerator.randomAlphanumericString(Constants.surnameLength + 1),
      "addressline1" -> "line1",
      "addressline2" -> "line2",
      "addressline3" -> "",
      "addressline4" -> "",
      "postcode" -> "",
      "countryCode" -> "JP")
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

  "addressline1 value supplied over the maximum allowed (over the boundary)" should {
    lazy val form = individualDetailsForm.bind(Map(
      "forename" -> "Bill",
      "surname" -> "Smith",
      "addressline1" -> MockDataGenerator.randomAlphanumericString(Constants.addressLineLength + 1),
      "addressline2" -> "line2",
      "addressline3" -> "",
      "addressline4" -> "",
      "postcode" -> "",
      "countryCode" -> "JP")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "addressline1"
    }
    "associate the correct error message to the error" in {
      form.error("addressline1").get.message shouldBe Messages("validation.error.mandatoryaddresssline")
    }
  }

  "addressline2 value supplied over the maximum allowed (over the boundary)" should {
    lazy val form = individualDetailsForm.bind(Map(
      "forename" -> "Bill",
      "surname" -> "Smith",
      "addressline1" -> "line1",
      "addressline2" -> MockDataGenerator.randomAlphanumericString(Constants.addressLineLength + 1),
      "addressline3" -> "",
      "addressline4" -> "",
      "postcode" -> "",
      "countryCode" -> "JP")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "addressline2"
    }
    "associate the correct error message to the error" in {
      form.error("addressline2").get.message shouldBe Messages("validation.error.mandatoryaddresssline")
    }
  }

  "addressline3 value supplied over the maximum allowed (over the boundary)" should {
    lazy val form = individualDetailsForm.bind(Map(
      "forename" -> "Bill",
      "surname" -> "Smith",
      "addressline1" -> "line1",
      "addressline2" -> "line2",
      "addressline3" -> MockDataGenerator.randomAlphanumericString(Constants.addressLineLength + 1),
      "addressline4" -> "",
      "postcode" -> "",
      "countryCode" -> "JP")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "addressline3"
    }
    "associate the correct error message to the error" in {
      form.error("addressline3").get.message shouldBe Messages("validation.error.optionaladdresssline")
    }
  }

  "addressline4 value supplied over the maximum allowed (over the boundary)" should {
    lazy val form = individualDetailsForm.bind(Map(
      "forename" -> "Bill",
      "surname" -> "Smith",
      "addressline1" -> "line1",
      "addressline2" -> "line2",
      "addressline3" -> "",
      "addressline4" -> MockDataGenerator.randomAlphanumericString(Constants.addressLineLength + 1),
      "postcode" -> "",
      "countryCode" -> "JP")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "addressline4"
    }
    "associate the correct error message to the error" in {
      form.error("addressline4").get.message shouldBe Messages("validation.error.linefouraddresssline")
    }
  }

  "postcode value supplied over the maximum allowed (over the boundary) incluses whitespace in the count" should {
    lazy val form = individualDetailsForm.bind(Map(
      "forename" -> "Bill",
      "surname" -> "Smith",
      "addressline1" -> "line1",
      "addressline2" -> "line2",
      "addressline3" -> "",
      "addressline4" -> "",
      "postcode" -> "BS98 1TL ",
      "countryCode" -> "GB")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "postcode"
    }
    "associate the correct error message to the error" in {
      form.error("postcode").get.message shouldBe Messages("validation.error.postcode")
    }
  }

  "country value supplied over the maximum allowed (over the boundary) includes whitespace in the count" should {
    lazy val form = individualDetailsForm.bind(Map(
      "forename" -> "Bill",
      "surname" -> "Smith",
      "addressline1" -> "line1",
      "addressline2" -> "line2",
      "addressline3" -> "",
      "addressline4" -> "",
      "postcode" -> "",
      "countryCode" -> "United Republic of Tanzania")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "countryCode"
    }
    "associate the correct error message to the error" in {
      form.error("countryCode").get.message shouldBe Messages("validation.error.countryCode")
    }
  }

  //Postcode Regex

  "postcode value supplied with multiple white space" should {
    lazy val form = individualDetailsForm.bind(Map(
      "forename" -> "Bill",
      "surname" -> "Smith",
      "addressline1" -> "line1",
      "addressline2" -> "line2",
      "addressline3" -> "",
      "addressline4" -> "",
      "postcode" -> "BS98  1TL",
      "countryCode" -> "GB")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "postcode"
    }
    "associate the correct error message to the error" in {
      form.error("postcode").get.message shouldBe Messages("validation.error.postcode")
    }
  }

  "postcode value supplied with brackets" should {
    lazy val form = individualDetailsForm.bind(Map(
      "forename" -> "Bill",
      "surname" -> "Smith",
      "addressline1" -> "line1",
      "addressline2" -> "line2",
      "addressline3" -> "",
      "addressline4" -> "",
      "postcode" -> "BS98 (1TL)",
      "countryCode" -> "GB")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "postcode"
    }
    "associate the correct error message to the error" in {
      form.error("postcode").get.message shouldBe Messages("validation.error.postcode")
    }
  }

  "postcode value supplied with /" should {
    lazy val form = individualDetailsForm.bind(Map(
      "forename" -> "Bill",
      "surname" -> "Smith",
      "addressline1" -> "line1",
      "addressline2" -> "line2",
      "addressline3" -> "",
      "addressline4" -> "",
      "postcode" -> "BS98/9 1TL",
      "countryCode" -> "GB")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "postcode"
    }
    "associate the correct error message to the error" in {
      form.error("postcode").get.message shouldBe Messages("validation.error.postcode")
    }
  }

  "postcode value supplied with lowercase" should {
    lazy val form = individualDetailsForm.bind(Map(
      "forename" -> "Bill",
      "surname" -> "Smith",
      "addressline1" -> "line1",
      "addressline2" -> "line2",
      "addressline3" -> "",
      "addressline4" -> "",
      "postcode" -> "bs98 1tl",
      "countryCode" -> "GB")
    )
    "raise no form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  "postcode value supplied with no spaces" should {
    lazy val form = individualDetailsForm.bind(Map(
      "forename" -> "Bill",
      "surname" -> "Smith",
      "addressline1" -> "line1",
      "addressline2" -> "line2",
      "addressline3" -> "",
      "addressline4" -> "",
      "postcode" -> "BS981TL",
      "countryCode" -> "GB")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "postcode"
    }
    "associate the correct error message to the error" in {
      form.error("postcode").get.message shouldBe Messages("validation.error.postcode")
    }
  }

  "country value supplied with '" should {
    lazy val form = individualDetailsForm.bind(Map(
      "forename" -> "Bill",
      "surname" -> "Smith",
      "addressline1" -> "line1",
      "addressline2" -> "line2",
      "addressline3" -> "",
      "addressline4" -> "",
      "postcode" -> "",
      "countryCode" -> "Cote d'Ivoire")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "countryCode"
    }
    "associate the correct error message to the error" in {
      form.error("countryCode").get.message shouldBe Messages("validation.error.countryCode")
    }
  }

  "country value supplied with -" should {
    lazy val form = individualDetailsForm.bind(Map(
      "forename" -> "Bill",
      "surname" -> "Smith",
      "addressline1" -> "line1",
      "addressline2" -> "line2",
      "addressline3" -> "",
      "addressline4" -> "",
      "postcode" -> "",
      "countryCode" -> "Timor-Leste")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "countryCode"
    }
    "associate the correct error message to the error" in {
      form.error("countryCode").get.message shouldBe Messages("validation.error.countryCode")
    }
  }

  "country value supplied with ." should {
    lazy val form = individualDetailsForm.bind(Map(
      "forename" -> "Bill",
      "surname" -> "Smith",
      "addressline1" -> "line1",
      "addressline2" -> "line2",
      "addressline3" -> "",
      "addressline4" -> "",
      "postcode" -> "",
      "countryCode" -> "St. Lucia")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "countryCode"
    }
    "associate the correct error message to the error" in {
      form.error("countryCode").get.message shouldBe Messages("validation.error.countryCode")
    }
  }

  "country value supplied with a trailing space" should {
    lazy val form = individualDetailsForm.bind(Map(
      "forename" -> "Bill",
      "surname" -> "Smith",
      "addressline1" -> "line1",
      "addressline2" -> "line2",
      "addressline3" -> "",
      "addressline4" -> "",
      "postcode" -> "",
      "countryCode" -> "JP ")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 1
    }
  }

  "country value supplied with #" should {
    lazy val form = individualDetailsForm.bind(Map(
      "forename" -> "Bill",
      "surname" -> "Smith",
      "addressline1" -> "line1",
      "addressline2" -> "line2",
      "addressline3" -> "",
      "addressline4" -> "",
      "postcode" -> "",
      "countryCode" -> "#JP")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "countryCode"
    }
    "associate the correct error message to the error" in {
      form.error("countryCode").get.message shouldBe Messages("validation.error.countryCode")
    }
  }
}
