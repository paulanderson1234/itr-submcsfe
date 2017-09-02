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

import models.DateOfIncorporationModel
import forms.DateOfIncorporationForm._
import org.scalatestplus.play.OneAppPerSuite
import uk.gov.hmrc.play.test.UnitSpec
import java.time.{LocalDate, ZoneId}
import java.util.Date

import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._

class DateOfIncorporationFormSpec extends UnitSpec with OneAppPerSuite {

  // set up border line conditions of today and future date (tomorrow)
  val date = new Date()
  val localDate:LocalDate = date.toInstant.atZone(ZoneId.systemDefault()).toLocalDate
  val tomorrow:LocalDate = localDate.plusDays(1)
  val tomorrowDay: String = tomorrow.getDayOfMonth.toString
  val tomorrowMonth: String = tomorrow.getMonthValue.toString
  val tomorrowYear: String = tomorrow.getYear.toString

  val todayDay: String = localDate.getDayOfMonth.toString
  val todayMonth: String = localDate.getMonthValue.toString
  val todayYear: String = localDate.getYear.toString

  "Creating the form for the date of incorporation date" should {
    "return a populated form using .fill" in {
      val model = DateOfIncorporationModel(Some(10), Some(2), Some(2016))
      val form = dateOfIncorporationForm.fill(model)
      form.value.get shouldBe DateOfIncorporationModel(Some(10), Some(2), Some(2016))
    }

    "return a Some if a model with valid inputs is supplied using .bind" in {
      val map = Map(("incorporationDay", "10"), ("incorporationMonth", "3"), ("incorporationYear", "2016"))
      val form = dateOfIncorporationForm.bind(map)
      form.value shouldBe Some(DateOfIncorporationModel(Some(10), Some(3), Some(2016)))
    }

    "return a None if a model with non-numeric inputs is supplied using .bind" in {
      val map = Map(("incorporationDay", "a"), ("incorporationMonth", "b"), ("incorporationYear", "c"))
      val form = dateOfIncorporationForm.bind(map)
      form.hasErrors shouldBe true
    }


    "return a None if a model with a single digit year is supplied using .bind" in {
      val map = Map(("incorporationDay", "2"), ("incorporationMonth", "2"), ("incorporationYear", "2"))
      val form = dateOfIncorporationForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a double digit year is supplied using .bind" in {
      val map = Map(("incorporationDay", "2"), ("incorporationMonth", "2"), ("incorporationYear", "22"))
      val form = dateOfIncorporationForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a triple digit year is supplied using .bind" in {
      val map = Map(("incorporationDay", "2"), ("incorporationMonth", "2"), ("incorporationYear", "222"))
      val form = dateOfIncorporationForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a day of 32 is supplied using .bind" in {
      val map = Map(("incorporationDay", "32"), ("incorporationMonth", "3"), ("incorporationYear", "1980"))
      val form = dateOfIncorporationForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a 29th Feb in a non leap year is supplied using .bind" in {
      val map = Map(("incorporationDay", "29"), ("incorporationMonth", "2"), ("incorporationYear", "1981"))
      val form = dateOfIncorporationForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a Some if a model with a 29th Feb in a leap year is supplied using .bind" in {
      val map = Map(("incorporationDay", "29"), ("incorporationMonth", "2"), ("incorporationYear", "2004"))
      val form = dateOfIncorporationForm.bind(map)
      form.hasErrors shouldBe false
    }

    "return a None if a model with a 31st june is supplied using .bind and return error message" in {
      val map = Map(("incorporationDay", "31"), ("incorporationMonth", "6"), ("incorporationYear", "1981"))
      val form = dateOfIncorporationForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a 31st september is supplied using .bind" in {
      val map = Map(("incorporationDay", "31"), ("incorporationMonth", "9"), ("incorporationYear", "1981"))
      val form = dateOfIncorporationForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a 31st November is supplied using .bind" in {
      val map = Map(("incorporationDay", "31"), ("incorporationMonth", "11"), ("incorporationYear", "1981"))
      val form = dateOfIncorporationForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a 31st April is supplied using .bind" in {
      val map = Map(("incorporationDay", "31"), ("incorporationMonth", "4"), ("incorporationYear", "1981"))
      val form = dateOfIncorporationForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a non-valid date input is supplied using .bind" in {
      val map = Map(("incorporationDay", "32"), ("incorporationMonth", "4"), ("incorporationYear", "2016"))
      val form = dateOfIncorporationForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with an empty day input is supplied using .bind" in {
      val map = Map(("incorporationDay", ""), ("incorporationMonth", "4"), ("incorporationYear", "2016"))
      val form = dateOfIncorporationForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with an empty month input is supplied using .bind" in {
      val map = Map(("incorporationDay", "4"), ("incorporationMonth", ""), ("incorporationYear", "2016"))
      val form = dateOfIncorporationForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with an empty yer input is supplied using .bind" in {
      val map = Map(("incorporationDay", "4"), ("incorporationMonth", "5"), ("incorporationYear", ""))
      val form = dateOfIncorporationForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a date in the future is supplied using .bind" in {
      val map = Map(("incorporationDay", tomorrowDay), ("incorporationMonth", tomorrowMonth), ("incorporationYear", tomorrowYear))
      val form = dateOfIncorporationForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a Some date if a model with a non future date (today) is supplied using .bind" in {
      val map = Map(("incorporationDay", todayDay), ("incorporationMonth", todayMonth), ("incorporationYear", todayYear))
      val form = dateOfIncorporationForm.bind(map)
      form.hasErrors shouldBe false
    }

  }

  "Creating a form with an invalid map" which {
    val form = dateOfIncorporationForm

    "contains invalid day" should {
      lazy val result = form.bind(Map(("incorporationDay", "32"), ("incorporationMonth", "1"), ("incorporationYear", "2012")))

      "return a form with errors" in {
        result.errors.isEmpty shouldBe false
      }

      "return a form with a single error" in {
        result.errors.size shouldBe 1
      }

      "return the correct error message" in {
        result.errors.head.message shouldBe Messages("common.date.error.invalidDate")
      }
    }

    "contains future year" should {
      lazy val result = form.bind(Map(("incorporationDay", "1"), ("incorporationMonth", "1"), ("incorporationYear", "9999")))

      "return a form with errors" in {
        result.errors.isEmpty shouldBe false
      }

      "return a form with a single error" in {
        result.errors.size shouldBe 1
      }

      "return the correct error message" in {
        result.errors.head.message shouldBe Messages("validation.error.DateOfIncorporation.Future")
      }
    }

    "does not contain a date" should {
      lazy val result = form.bind(Map(("incorporationDay", ""), ("incorporationMonth", ""), ("incorporationYear", "")))

      "return a form with errors" in {
        result.errors.isEmpty shouldBe false
      }

      "return a form with a single error" in {
        result.errors.size shouldBe 1
      }

      "return the correct error message" in {
        result.errors.head.message shouldBe Messages("validation.error.DateNotEntered")
      }
    }

    "contains an invalid date and future year" should {
      lazy val result = form.bind(Map(("incorporationDay", "33"), ("incorporationMonth", "15"), ("incorporationYear", "9999")))

      "return a form with errors" in {
        result.errors.isEmpty shouldBe false
      }

      "return a form with a single error" in {
        result.errors.size shouldBe 2
      }

      "return the correct error message" in {
        result.errors(0).message shouldBe Messages("common.date.error.invalidDate")
        result.errors(1).message shouldBe Messages("validation.error.DateOfIncorporation.Future")
      }
    }

  }

  "Creating a form with a valid map" which {
    val form = dateOfIncorporationForm

    "contains valid date" should {
      lazy val result = form.bind(Map(("incorporationDay", "31"), ("incorporationMonth", "12"), ("incorporationYear", "2016")))

      "return a form with no errors" in {
        result.errors.isEmpty shouldBe true
      }

      "return a form with the correct data model" in {
        result.value shouldBe Some(DateOfIncorporationModel(Some(31),Some(12),Some(2016)))
      }
    }


  }
}


