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

import forms.CompanyDetailsForm._
import models.CompanyDetailsModel
import org.scalatestplus.play.OneAppPerSuite
import play.api.i18n.Messages
import uk.gov.hmrc.play.test.UnitSpec
import play.api.i18n.Messages.Implicits._

class CompanyDetailsFormSpec extends UnitSpec with OneAppPerSuite{

  "Creating a form using an empty model" should {
    lazy val form = companyDetailsForm
    "return an empty string for companyName, companyAddressline1, companyAddressline2, telephone number and email" in {
      form.data.isEmpty shouldBe true
    }
  }

  "Creating a form using a valid model" should {
    "return a form with the data specified in the model" in {
      val model = CompanyDetailsModel("line0","line1","line2",None,None,None,"JP")
      val form = companyDetailsForm.fill(model)

      form.data("companyName") shouldBe "line0"
      form.data("companyAddressline1") shouldBe "line1"
      form.data("companyAddressline2") shouldBe "line2"
      form.data("countryCode") shouldBe "JP"
      form.errors.length shouldBe 0
      form.data.size shouldBe 4
    }
  }

  "Creating a form using an invalid post" when {
    "supplied with no data for companyName" should {
      lazy val form = companyDetailsForm.bind(Map(
        "companyName" -> "",
        "companyAddressline1" -> "line1",
        "companyAddressline2" -> "line2",
        "companyAddressline3" -> "",
        "companyAddressline4" -> "",
        "companyPostcode" -> "",
        "countryCode" -> "JP")
      )
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 1 form error" in {

        form.errors.length shouldBe 1

        form.errors.head.key shouldBe "companyName"
      }
      "associate the correct error message to the error" in {
        Messages(form.error("companyName").get.message) shouldBe Messages("error.required")
      }
    }
  }


  "Creating a form using an invalid post" when {
    "supplied with no data for companyAddressline1" should {
      lazy val form = companyDetailsForm.bind(Map(
        "companyName" -> "line0",
        "companyAddressline1" -> "",
        "companyAddressline2" -> "line2",
        "companyAddressline3" -> "",
        "companyAddressline4" -> "",
        "companyPostcode" -> "",
        "countryCode" -> "JP")
      )
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 1 form error" in {

        form.errors.length shouldBe 1

        form.errors.head.key shouldBe "companyAddressline1"
      }
      "associate the correct error message to the error" in {
        form.error("companyAddressline1").get.message shouldBe Messages("validation.error.mandatoryaddresssline")
      }
    }
  }

  "Creating a form using an invalid post" when {
    "supplied with no data for companyAddressline2" should {
      lazy val form = companyDetailsForm.bind(Map(
        "companyName" -> "line0",
        "companyAddressline1" -> "line1",
        "companyAddressline2" -> "",
        "companyAddressline3" -> "",
        "companyAddressline4" -> "",
        "companyPostcode" -> "",
        "countryCode" -> "JP")
      )
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 1 form error" in {
        form.errors.length shouldBe 1
        form.errors.head.key shouldBe "companyAddressline2"
      }
      "associate the correct error message to the error" in {
        form.error("companyAddressline2").get.message shouldBe Messages("validation.error.mandatoryaddresssline")
      }
    }
  }

  "Creating a form using an invalid post" when {
    "supplied with no data for country" should {
      lazy val form = companyDetailsForm.bind(Map(
        "companyName" -> "line0",
        "companyAddressline1" -> "line1",
        "companyAddressline2" -> "line2",
        "companyAddressline3" -> "",
        "companyAddressline4" -> "",
        "companyPostcode" -> "",
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
    "supplied with no data for companyName and companyAddressline1" should {
      lazy val form = companyDetailsForm.bind(Map(
        "companyName" -> "",
        "companyAddressline1" -> "",
        "companyAddressline2" -> "line2",
        "companyAddressline3" -> "",
        "companyAddressline4" -> "",
        "companyPostcode" -> "",
        "countryCode" -> "JP")
      )
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 2 form errors" in {
        form.errors.length shouldBe 2
        form.errors.head.key shouldBe "companyName"
        form.errors(1).key shouldBe "companyAddressline1"
      }
      "associate the correct error message to the error" in {
        Messages(form.error("companyName").get.message) shouldBe Messages("error.required")
        form.error("companyAddressline1").get.message shouldBe Messages("validation.error.mandatoryaddresssline")
      }
    }
  }


  "Creating a form using an invalid post" when {
    "supplied with no data for companyAddressline1 and companyAddressline2" should {
      lazy val form = companyDetailsForm.bind(Map(
        "companyName" -> "line0",
        "companyAddressline1" -> "",
        "companyAddressline2" -> "",
        "companyAddressline3" -> "",
        "companyAddressline4" -> "",
        "companyPostcode" -> "",
        "countryCode" -> "JP")
      )
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 2 form errors" in {
        form.errors.length shouldBe 2
        form.errors.head.key shouldBe "companyAddressline1"
        form.errors(1).key shouldBe "companyAddressline2"
      }
      "associate the correct error message to the error" in {
        form.error("companyAddressline1").get.message shouldBe Messages("validation.error.mandatoryaddresssline")
        form.error("companyAddressline2").get.message shouldBe Messages("validation.error.mandatoryaddresssline")
      }
    }
  }

  "Creating a form using an invalid post" when {
    "supplied with no data for companyAddressline2 and country" should {
      lazy val form = companyDetailsForm.bind(Map(
        "companyName" -> "line0",
        "companyAddressline1" -> "line1",
        "companyAddressline2" -> "",
        "companyAddressline3" -> "",
        "companyAddressline4" -> "",
        "companyPostcode" -> "",
        "countryCode" -> "")
      )
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 2 form errors" in {
        form.errors.length shouldBe 2
        form.errors.head.key shouldBe "companyAddressline2"
        form.errors(1).key shouldBe "countryCode"
      }
      "associate the correct error message to the error" in {
        form.error("companyAddressline2").get.message shouldBe Messages("validation.error.mandatoryaddresssline")
        form.error("countryCode").get.message shouldBe Messages("validation.error.countryCode")
      }
    }
  }


  "Creating a form using an invalid post" when {
    "supplied with no data for companyName, companyAddressline1 or companyAddressline2" should {
      lazy val form = companyDetailsForm.bind(Map(
        "companyName" -> "",
        "companyAddressline1" -> "",
        "companyAddressline2" -> "",
        "companyAddressline3" -> "",
        "companyAddressline4" -> "",
        "companyPostcode" -> "",
        "countryCode" -> "JP")
      )
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 3 form errors" in {
        form.errors.length shouldBe 3
        form.errors.head.key shouldBe "companyName"
        form.errors(1).key shouldBe "companyAddressline1"
        form.errors(2).key shouldBe "companyAddressline2"
      }
      "associate the correct error message to the error" in {
        Messages(form.error("companyName").get.message) shouldBe Messages("error.required")
        form.error("companyAddressline1").get.message shouldBe Messages("validation.error.mandatoryaddresssline")
        form.error("companyAddressline2").get.message shouldBe Messages("validation.error.mandatoryaddresssline")
      }
    }
  }


  "Creating a form using an invalid post" when {
    "supplied with no data for companyAddressline1, companyAddressline2 or country" should {
      lazy val form = companyDetailsForm.bind(Map(
        "companyName" -> "line0",
        "companyAddressline1" -> "",
        "companyAddressline2" -> "",
        "companyAddressline3" -> "",
        "companyAddressline4" -> "",
        "companyPostcode" -> "",
        "countryCode" -> "")
      )
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 3 form errors" in {
        form.errors.length shouldBe 3
        form.errors.head.key shouldBe "companyAddressline1"
        form.errors(1).key shouldBe "companyAddressline2"
        form.errors(2).key shouldBe "countryCode"
      }
      "associate the correct error message to the error" in {
        form.error("companyAddressline1").get.message shouldBe Messages("validation.error.mandatoryaddresssline")
        form.error("companyAddressline2").get.message shouldBe Messages("validation.error.mandatoryaddresssline")
        form.error("countryCode").get.message shouldBe Messages("validation.error.countryCode")
      }
    }
  }

  "Creating a form using an invalid post" when {
    "supplied with no data for companyName, companyAddressline1, companyAddressline2 or country" should {
      lazy val form = companyDetailsForm.bind(Map(
        "companyName" -> "",
        "companyAddressline1" -> "",
        "companyAddressline2" -> "",
        "companyAddressline3" -> "",
        "companyAddressline4" -> "",
        "companyPostcode" -> "",
        "countryCode" -> "")
      )
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 4 form errors" in {
        form.errors.length shouldBe 4
        form.errors.head.key shouldBe "companyName"
        form.errors(1).key shouldBe "companyAddressline1"
        form.errors(2).key shouldBe "companyAddressline2"
        form.errors(3).key shouldBe "countryCode"
      }
      "associate the correct error message to the error" in {
        Messages(form.error("companyName").get.message) shouldBe Messages("error.required")
        form.error("companyAddressline1").get.message shouldBe Messages("validation.error.mandatoryaddresssline")
        form.error("companyAddressline2").get.message shouldBe Messages("validation.error.mandatoryaddresssline")
        form.error("countryCode").get.message shouldBe Messages("validation.error.countryCode")
      }
    }
  }

  "supplied with empty space for companyName" should {
    lazy val form = companyDetailsForm.bind(Map(
      "companyName" -> "   ",
      "companyAddressline1" -> "line1",
      "companyAddressline2" -> "line2",
      "companyAddressline3" -> "",
      "companyAddressline4" -> "",
      "companyPostcode" -> "",
      "countryCode" -> "JP")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "companyName"
    }
    "associate the correct error message to the error " in {
      Messages(form.error("companyName").get.message) shouldBe Messages("error.required")
    }
  }


  "supplied with empty space for companyAddressline1" should {
    lazy val form = companyDetailsForm.bind(Map(
      "companyName" -> "line0",
      "companyAddressline1" -> "   ",
      "companyAddressline2" -> "line2",
      "companyAddressline3" -> "",
      "companyAddressline4" -> "",
      "companyPostcode" -> "",
      "countryCode" -> "JP")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "companyAddressline1"
    }
    "associate the correct error message to the error " in {
      form.error("companyAddressline1").get.message shouldBe Messages("validation.error.mandatoryaddresssline")
    }
  }

  "supplied with empty space for companyAddressline2" should {
    lazy val form = companyDetailsForm.bind(Map(
      "companyName" -> "line0",
      "companyAddressline1" -> "line1",
      "companyAddressline2" -> "   ",
      "companyAddressline3" -> "",
      "companyAddressline4" -> "",
      "companyPostcode" -> "",
      "countryCode" -> "JP")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "companyAddressline2"
    }
    "associate the correct error message to the error " in {
      form.error("companyAddressline2").get.message shouldBe Messages("validation.error.mandatoryaddresssline")
    }
  }

  "supplied with empty space for companyAddressline3" should {
    lazy val form = companyDetailsForm.bind(Map(
      "companyName" -> "line0",
      "companyAddressline1" -> "line1",
      "companyAddressline2" -> "line2",
      "companyAddressline3" -> "   ",
      "companyAddressline4" -> "",
      "companyPostcode" -> "",
      "countryCode" -> "JP")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "companyAddressline3"
    }
    "associate the correct error message to the error " in {
      form.error("companyAddressline3").get.message shouldBe Messages("validation.error.optionaladdresssline")
    }
  }

  "supplied with empty space for companyAddressline4" should {
    lazy val form = companyDetailsForm.bind(Map(
      "companyName" -> "line0",
      "companyAddressline1" -> "line1",
      "companyAddressline2" -> "line2",
      "companyAddressline3" -> "",
      "companyAddressline4" -> "   ",
      "companyPostcode" -> "",
      "countryCode" -> "JP")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "companyAddressline4"
    }
    "associate the correct error message to the error " in {
      form.error("companyAddressline4").get.message shouldBe Messages("validation.error.linefouraddresssline")
    }
  }

  "supplied with empty space for country" should {
    lazy val form = companyDetailsForm.bind(Map(
      "companyName" -> "line0",
      "companyAddressline1" -> "line1",
      "companyAddressline2" -> "line2",
      "companyAddressline3" -> "",
      "companyAddressline4" -> "",
      "companyPostcode" -> "",
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

  "supplied with empty space for companyPostcode" should {
    lazy val form = companyDetailsForm.bind(Map(
      "companyName" -> "line0",
      "companyAddressline1" -> "line1",
      "companyAddressline2" -> "line2",
      "companyAddressline3" -> "",
      "companyAddressline4" -> "",
      "companyPostcode" -> "   ",
      "countryCode" -> "GB")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form errors" in {
      form.errors.length shouldBe 1
    }
    "associate the correct error message to the error" in {
      form.error("companyPostcode").get.message shouldBe Messages("validation.error.postcode")
    }
  }

  "supplied with numeric input for companyName" should {
    lazy val form = companyDetailsForm.bind(Map(
      "companyName" -> "line0 260",
      "companyAddressline1" -> "line1",
      "companyAddressline2" -> "line2",
      "companyAddressline3" -> "",
      "companyAddressline4" -> "",
      "companyPostcode" -> "",
      "countryCode" -> "JP")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }


  "supplied with numeric input for companyAddressline1" should {
    lazy val form = companyDetailsForm.bind(Map(
      "companyName" -> "line0",
      "companyAddressline1" -> "line1 86",
      "companyAddressline2" -> "line2",
      "companyAddressline3" -> "",
      "companyAddressline4" -> "",
      "companyPostcode" -> "",
      "countryCode" -> "JP")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  "supplied with numeric input for companyAddressline2" should {
    lazy val form = companyDetailsForm.bind(Map(
      "companyName" -> "line0",
      "companyAddressline1" -> "line1",
      "companyAddressline2" -> "line2 86",
      "companyAddressline3" -> "",
      "companyAddressline4" -> "",
      "companyPostcode" -> "",
      "countryCode" -> "JP")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  "supplied with numeric input for companyAddressline3" should {
    lazy val form = companyDetailsForm.bind(Map(
      "companyName" -> "line0",
      "companyAddressline1" -> "line1",
      "companyAddressline2" -> "line2",
      "companyAddressline3" -> "86",
      "companyAddressline4" -> "",
      "companyPostcode" -> "",
      "countryCode" -> "JP")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  "supplied with numeric input for companyAddressline4" should {
    lazy val form = companyDetailsForm.bind(Map(
      "companyName" -> "line0",
      "companyAddressline1" -> "line1",
      "companyAddressline2" -> "line2",
      "companyAddressline3" -> "",
      "companyAddressline4" -> "86",
      "companyPostcode" -> "",
      "countryCode" -> "JP")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  "supplied with numeric input for companyPostcode" should {
    lazy val form = companyDetailsForm.bind(Map(
      "companyName" -> "line0",
      "companyAddressline1" -> "line1",
      "companyAddressline2" -> "line2",
      "companyAddressline3" -> "",
      "companyAddressline4" -> "",
      "companyPostcode" -> "86",
      "countryCode" -> "GB")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "companyPostcode"
    }
    "associate the correct error message to the error" in {
      form.error("companyPostcode").get.message shouldBe Messages("validation.error.postcode")
    }
  }

  "supplied with alphanumeric input for country" should {
    lazy val form = companyDetailsForm.bind(Map(
      "companyName" -> "line0",
      "companyAddressline1" -> "line1",
      "companyAddressline2" -> "line2",
      "companyAddressline3" -> "",
      "companyAddressline4" -> "",
      "companyPostcode" -> "",
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


  "companyName value supplied with the minimum allowed" should {
    lazy val form = companyDetailsForm.bind(Map(
      "companyName" -> "A",
      "companyAddressline1" -> "line1",
      "companyAddressline2" -> "line2",
      "companyAddressline3" -> "",
      "companyAddressline4" -> "",
      "companyPostcode" -> "",
      "countryCode" -> "JP")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  "companyAddressline1 value supplied with the minimum allowed" should {
    lazy val form = companyDetailsForm.bind(Map(
      "companyName" -> "line0",
      "companyAddressline1" -> "A",
      "companyAddressline2" -> "line2",
      "companyAddressline3" -> "",
      "companyAddressline4" -> "",
      "companyPostcode" -> "",
      "countryCode" -> "JP")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  "companyAddressline2 value supplied with the minimum allowed" should {
    lazy val form = companyDetailsForm.bind(Map(
      "companyName" -> "line0",
      "companyAddressline1" -> "line1",
      "companyAddressline2" -> "M",
      "companyAddressline3" -> "",
      "companyAddressline4" -> "",
      "companyPostcode" -> "",
      "countryCode" -> "JP")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  "companyAddressline3 value supplied with the minimum allowed" should {
    lazy val form = companyDetailsForm.bind(Map(
      "companyName" -> "line0",
      "companyAddressline1" -> "line1",
      "companyAddressline2" -> "line2",
      "companyAddressline3" -> "A",
      "companyAddressline4" -> "",
      "companyPostcode" -> "",
      "countryCode" -> "JP")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  "companyAddressline4 value supplied with the minimum allowed" should {
    lazy val form = companyDetailsForm.bind(Map(
      "companyName" -> "line0",
      "companyAddressline1" -> "line1",
      "companyAddressline2" -> "line2",
      "companyAddressline3" -> "",
      "companyAddressline4" -> "A",
      "companyPostcode" -> "",
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
    lazy val form = companyDetailsForm.bind(Map(
      "companyName" -> "line0",
      "companyAddressline1" -> "line1",
      "companyAddressline2" -> "line2",
      "companyAddressline3" -> "",
      "companyAddressline4" -> "",
      "companyPostcode" -> "",
      "countryCode" -> "JP")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }


  "companyName value supplied with the maximum allowed (on the boundary)" should {
    lazy val form = companyDetailsForm.bind(Map(
      "companyName" -> "line0                                                   ",
      "companyAddressline1" -> "line1",
      "companyAddressline2" -> "line2",
      "companyAddressline3" -> "",
      "companyAddressline4" -> "",
      "companyPostcode" -> "",
      "countryCode" -> "JP")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }


  "companyAddressline1 value supplied with the maximum allowed (on the boundary)" should {
    lazy val form = companyDetailsForm.bind(Map(
      "companyName" -> "line0",
      "companyAddressline1" -> "line1          ",
      "companyAddressline2" -> "line2",
      "companyAddressline3" -> "",
      "companyAddressline4" -> "",
      "companyPostcode" -> "",
      "countryCode" -> "JP")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  "companyAddressline2 value supplied with the maximum allowed (on the boundary)" should {
    lazy val form = companyDetailsForm.bind(Map(
      "companyName" -> "line0",
      "companyAddressline1" -> "line1",
      "companyAddressline2" -> "line2                  ",
      "companyAddressline3" -> "",
      "companyAddressline4" -> "",
      "companyPostcode" -> "",
      "countryCode" -> "JP")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  "companyAddressline3 value supplied with the maximum allowed (on the boundary)" should {
    lazy val form = companyDetailsForm.bind(Map(
      "companyName" -> "line0",
      "companyAddressline1" -> "line1",
      "companyAddressline2" -> "line2",
      "companyAddressline3" -> "A                          ",
      "companyAddressline4" -> "",
      "companyPostcode" -> "",
      "countryCode" -> "JP")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  "companyAddressline4 value supplied with the maximum allowed (on the boundary)" should {
    lazy val form = companyDetailsForm.bind(Map(
      "companyName" -> "line0",
      "companyAddressline1" -> "line1",
      "companyAddressline2" -> "line2",
      "companyAddressline3" -> "",
      "companyAddressline4" -> "A                 ",
      "companyPostcode" -> "",
      "countryCode" -> "JP")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  "companyPostcode value supplied with the maximum allowed (on the boundary)" should {
    lazy val form = companyDetailsForm.bind(Map(
      "companyName" -> "line0",
      "companyAddressline1" -> "line1",
      "companyAddressline2" -> "line2",
      "companyAddressline3" -> "",
      "companyAddressline4" -> "",
      "companyPostcode" -> "BS98 1TL",
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
    lazy val form = companyDetailsForm.bind(Map(
      "companyName" -> "line0",
      "companyAddressline1" -> "line1",
      "companyAddressline2" -> "line2",
      "companyAddressline3" -> "",
      "companyAddressline4" -> "",
      "companyPostcode" -> "",
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
    lazy val form = companyDetailsForm.bind(Map(
      "companyName" -> "line0",
      "companyAddressline1" -> "line1",
      "companyAddressline2" -> "line2",
      "companyAddressline3" -> "",
      "companyAddressline4" -> "",
      "companyPostcode" -> "",
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

  "companyName value supplied over the maximum allowed (over the boundary)" should {
    lazy val form = companyDetailsForm.bind(Map(
      "companyName" -> "123456789012345678901234567890123456789012345678932095703932",
      "companyAddressline1" -> "line1",
      "companyAddressline2" -> "line2",
      "companyAddressline3" -> "",
      "companyAddressline4" -> "",
      "companyPostcode" -> "",
      "countryCode" -> "JP")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "companyName"
    }
    "associate the correct error message to the error" in {
      Messages(form.error("companyName").get.message) shouldBe Messages("error.maxLength")
    }
  }

  "companyAddressline1 value supplied over the maximum allowed (over the boundary)" should {
    lazy val form = companyDetailsForm.bind(Map(
      "companyName" -> "line0",
      "companyAddressline1" -> "1234567890123456789012345678901234567890",
      "companyAddressline2" -> "line2",
      "companyAddressline3" -> "",
      "companyAddressline4" -> "",
      "companyPostcode" -> "",
      "countryCode" -> "JP")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "companyAddressline1"
    }
    "associate the correct error message to the error" in {
      form.error("companyAddressline1").get.message shouldBe Messages("validation.error.mandatoryaddresssline")
    }
  }

  "companyAddressline2 value supplied over the maximum allowed (over the boundary)" should {
    lazy val form = companyDetailsForm.bind(Map(
      "companyName" -> "line0",
      "companyAddressline1" -> "line1",
      "companyAddressline2" -> "1234567890123456789012345678901234567890",
      "companyAddressline3" -> "",
      "companyAddressline4" -> "",
      "companyPostcode" -> "",
      "countryCode" -> "JP")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "companyAddressline2"
    }
    "associate the correct error message to the error" in {
      form.error("companyAddressline2").get.message shouldBe Messages("validation.error.mandatoryaddresssline")
    }
  }

  "companyAddressline3 value supplied over the maximum allowed (over the boundary)" should {
    lazy val form = companyDetailsForm.bind(Map(
      "companyName" -> "line0",
      "companyAddressline1" -> "line1",
      "companyAddressline2" -> "line2",
      "companyAddressline3" -> "1234567890123456789012345678901234567890",
      "companyAddressline4" -> "",
      "companyPostcode" -> "",
      "countryCode" -> "JP")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "companyAddressline3"
    }
    "associate the correct error message to the error" in {
      form.error("companyAddressline3").get.message shouldBe Messages("validation.error.optionaladdresssline")
    }
  }

  "companyAddressline4 value supplied over the maximum allowed (over the boundary)" should {
    lazy val form = companyDetailsForm.bind(Map(
      "companyName" -> "line0",
      "companyAddressline1" -> "line1",
      "companyAddressline2" -> "line2",
      "companyAddressline3" -> "",
      "companyAddressline4" -> "1234567890123456789012345678901234567890",
      "companyPostcode" -> "",
      "countryCode" -> "JP")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "companyAddressline4"
    }
    "associate the correct error message to the error" in {
      form.error("companyAddressline4").get.message shouldBe Messages("validation.error.linefouraddresssline")
    }
  }

  "companyPostcode value supplied over the maximum allowed (over the boundary) incluses whitespace in the count" should {
    lazy val form = companyDetailsForm.bind(Map(
      "companyName" -> "line0",
      "companyAddressline1" -> "line1",
      "companyAddressline2" -> "line2",
      "companyAddressline3" -> "",
      "companyAddressline4" -> "",
      "companyPostcode" -> "BS98 1TL ",
      "countryCode" -> "GB")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "companyPostcode"
    }
    "associate the correct error message to the error" in {
      form.error("companyPostcode").get.message shouldBe Messages("validation.error.postcode")
    }
  }

  "country value supplied over the maximum allowed (over the boundary) includes whitespace in the count" should {
    lazy val form = companyDetailsForm.bind(Map(
      "companyName" -> "line0",
      "companyAddressline1" -> "line1",
      "companyAddressline2" -> "line2",
      "companyAddressline3" -> "",
      "companyAddressline4" -> "",
      "companyPostcode" -> "",
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

  "companyPostcode value supplied with multiple white space" should {
    lazy val form = companyDetailsForm.bind(Map(
      "companyName" -> "line0",
      "companyAddressline1" -> "line1",
      "companyAddressline2" -> "line2",
      "companyAddressline3" -> "",
      "companyAddressline4" -> "",
      "companyPostcode" -> "BS98  1TL",
      "countryCode" -> "GB")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "companyPostcode"
    }
    "associate the correct error message to the error" in {
      form.error("companyPostcode").get.message shouldBe Messages("validation.error.postcode")
    }
  }

  "companyPostcode value supplied with brackets" should {
    lazy val form = companyDetailsForm.bind(Map(
      "companyName" -> "line0",
      "companyAddressline1" -> "line1",
      "companyAddressline2" -> "line2",
      "companyAddressline3" -> "",
      "companyAddressline4" -> "",
      "companyPostcode" -> "BS98 (1TL)",
      "countryCode" -> "GB")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "companyPostcode"
    }
    "associate the correct error message to the error" in {
      form.error("companyPostcode").get.message shouldBe Messages("validation.error.postcode")
    }
  }

  "companyPostcode value supplied with /" should {
    lazy val form = companyDetailsForm.bind(Map(
      "companyName" -> "line0",
      "companyAddressline1" -> "line1",
      "companyAddressline2" -> "line2",
      "companyAddressline3" -> "",
      "companyAddressline4" -> "",
      "companyPostcode" -> "BS98/9 1TL",
      "countryCode" -> "GB")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "companyPostcode"
    }
    "associate the correct error message to the error" in {
      form.error("companyPostcode").get.message shouldBe Messages("validation.error.postcode")
    }
  }

  "companyPostcode value supplied with lowercase" should {
    lazy val form = companyDetailsForm.bind(Map(
      "companyName" -> "line0",
      "companyAddressline1" -> "line1",
      "companyAddressline2" -> "line2",
      "companyAddressline3" -> "",
      "companyAddressline4" -> "",
      "companyPostcode" -> "bs98 1tl",
      "countryCode" -> "GB")
    )
    "raise no form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  "companyPostcode value supplied with no spaces" should {
    lazy val form = companyDetailsForm.bind(Map(
      "companyName" -> "line0",
      "companyAddressline1" -> "line1",
      "companyAddressline2" -> "line2",
      "companyAddressline3" -> "",
      "companyAddressline4" -> "",
      "companyPostcode" -> "BS981TL",
      "countryCode" -> "GB")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "companyPostcode"
    }
    "associate the correct error message to the error" in {
      form.error("companyPostcode").get.message shouldBe Messages("validation.error.postcode")
    }
  }

  "country value supplied with '" should {
    lazy val form = companyDetailsForm.bind(Map(
      "companyName" -> "line0",
      "companyAddressline1" -> "line1",
      "companyAddressline2" -> "line2",
      "companyAddressline3" -> "",
      "companyAddressline4" -> "",
      "companyPostcode" -> "",
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
    lazy val form = companyDetailsForm.bind(Map(
      "companyName" -> "line0",
      "companyAddressline1" -> "line1",
      "companyAddressline2" -> "line2",
      "companyAddressline3" -> "",
      "companyAddressline4" -> "",
      "companyPostcode" -> "",
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
    lazy val form = companyDetailsForm.bind(Map(
      "companyName" -> "line0",
      "companyAddressline1" -> "line1",
      "companyAddressline2" -> "line2",
      "companyAddressline3" -> "",
      "companyAddressline4" -> "",
      "companyPostcode" -> "",
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
    lazy val form = companyDetailsForm.bind(Map(
      "companyName" -> "line0",
      "companyAddressline1" -> "line1",
      "companyAddressline2" -> "line2",
      "companyAddressline3" -> "",
      "companyAddressline4" -> "",
      "companyPostcode" -> "",
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
    lazy val form = companyDetailsForm.bind(Map(
      "companyName" -> "line0",
      "companyAddressline1" -> "line1",
      "companyAddressline2" -> "line2",
      "companyAddressline3" -> "",
      "companyAddressline4" -> "",
      "companyPostcode" -> "",
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
