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

import forms.DateSharesRepaidForm._
import org.scalatestplus.play.OneAppPerSuite
import uk.gov.hmrc.play.test.UnitSpec
import java.time.ZoneId
import java.util.Date
import models.repayments.DateSharesRepaidModel
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._

class DateSharesRepaidFormSpec extends UnitSpec with OneAppPerSuite{

  // set up border line conditions of today and future date (tomorrow)
  val date = new Date()
  val localDate = date.toInstant.atZone(ZoneId.systemDefault()).toLocalDate;
  val tomorrow = localDate.plusDays(1)
  val tomorrowDay: String = tomorrow.getDayOfMonth.toString
  val tomorrowMonth: String = tomorrow.getMonthValue.toString
  val tomorrowYear: String = tomorrow.getYear.toString

  val todayDay:String = localDate.getDayOfMonth.toString
  val todayMonth: String = localDate.getMonthValue.toString
  val todayYear: String = localDate.getYear.toString

  "Creating the form for the date of shareIssue date" should {
    "return a populated form using .fill" in {
      val model = DateSharesRepaidModel(Some(10), Some(2), Some(2016))
      val form = dateSharesRepaidForm.fill(model)
      form.value.get shouldBe DateSharesRepaidModel(Some(10), Some(2), Some(2016))
      form.hasErrors shouldBe false
    }

    "return a Some if a model with valid inputs is supplied using .bind" in {
      val map = Map(("dateSharesRepaidDay", "10"), ("dateSharesRepaidMonth", "3"), ("dateSharesRepaidYear", "2016"))
      val form = dateSharesRepaidForm.bind(map)
      form.value shouldBe Some(DateSharesRepaidModel(Some(10), Some(3), Some(2016)))
      form.hasErrors shouldBe false
    }

    "return a None if a model with non-numeric inputs is supplied using .bind" in {
      val map = Map(("dateSharesRepaidDay", "a"), ("dateSharesRepaidMonth", "b"), ("dateSharesRepaidYear", "c"))
      val form = dateSharesRepaidForm.bind(map)
      form.hasErrors shouldBe true
      form.errors.head.key shouldBe "dateSharesRepaidDay"
      form.errors.head.message shouldBe "error.number"

    }

    "return a None if a model with a single digit year is supplied using .bind" in {
      val map = Map(("dateSharesRepaidDay", "2"), ("dateSharesRepaidMonth", "2"), ("dateSharesRepaidYear", "2"))
      val form = dateSharesRepaidForm.bind(map)
      form.hasErrors shouldBe true
      form.errors.head.message shouldBe Messages("common.date.error.invalidDate")
    }

    "return a None if a model with a double digit year is supplied using .bind" in {
      val map = Map(("dateSharesRepaidDay", "2"), ("dateSharesRepaidMonth", "2"), ("dateSharesRepaidYear", "22"))
      val form = dateSharesRepaidForm.bind(map)
      form.hasErrors shouldBe true
      form.errors.head.message shouldBe Messages("common.date.error.invalidDate")
    }

    "return a None if a model with a triple digit year is supplied using .bind" in {
      val map = Map(("dateSharesRepaidDay", "2"), ("dateSharesRepaidMonth", "2"), ("dateSharesRepaidYear", "222"))
      val form = dateSharesRepaidForm.bind(map)
      form.hasErrors shouldBe true
      form.errors.head.message shouldBe Messages("common.date.error.invalidDate")
    }

    "return a None if a model with a day of 32 is supplied using .bind" in {
      val map = Map(("dateSharesRepaidDay", "32"), ("dateSharesRepaidMonth", "3"), ("dateSharesRepaidYear", "1980"))
      val form = dateSharesRepaidForm.bind(map)
      form.hasErrors shouldBe true
      form.errors.head.message shouldBe Messages("common.date.error.invalidDate")
    }

    "return a None if a model with a 29th Feb in a non leap year is supplied using .bind" in {
      val map = Map(("dateSharesRepaidDay", "29"), ("dateSharesRepaidMonth", "2"), ("dateSharesRepaidYear", "1981"))
      val form = dateSharesRepaidForm.bind(map)
      form.hasErrors shouldBe true
      form.errors.head.message shouldBe Messages("common.date.error.invalidDate")
    }

    "return a Some if a model with a 29th Feb in a leap year is supplied using .bind" in {
      val map = Map(("dateSharesRepaidDay", "29"), ("dateSharesRepaidMonth", "2"), ("dateSharesRepaidYear", "2004"))
      val form = dateSharesRepaidForm.bind(map)
      form.hasErrors shouldBe false
    }

    "return a None if a model with a 31st june is supplied using .bind" in {
      val map = Map(("dateSharesRepaidDay", "31"), ("dateSharesRepaidMonth", "6"), ("dateSharesRepaidYear", "1981"))
      val form = dateSharesRepaidForm.bind(map)
      form.hasErrors shouldBe true
      form.errors.head.message shouldBe Messages("common.date.error.invalidDate")
    }

    "return a None if a model with a 31st september is supplied using .bind" in {
      val map = Map(("dateSharesRepaidDay", "31"), ("dateSharesRepaidMonth", "9"), ("dateSharesRepaidYear", "1981"))
      val form = dateSharesRepaidForm.bind(map)
      form.hasErrors shouldBe true
      form.errors.head.message shouldBe Messages("common.date.error.invalidDate")
    }

    "return a None if a model with a 31st November is supplied using .bind" in {
      val map = Map(("dateSharesRepaidDay", "31"), ("dateSharesRepaidMonth", "11"), ("dateSharesRepaidYear", "1981"))
      val form = dateSharesRepaidForm.bind(map)
      form.hasErrors shouldBe true
      form.errors.head.message shouldBe Messages("common.date.error.invalidDate")
    }

    "return a None if a model with a 31st April is supplied using .bind" in {
      val map = Map(("dateSharesRepaidDay", "31"), ("dateSharesRepaidMonth", "4"), ("dateSharesRepaidYear", "1981"))
      val form = dateSharesRepaidForm.bind(map)
      form.hasErrors shouldBe true
      form.errors.head.message shouldBe Messages("common.date.error.invalidDate")
    }

    "return a None if a model with an empty day input is supplied using .bind" in {
      val map = Map(("dateSharesRepaidDay", ""), ("dateSharesRepaidMonth", "4"), ("dateSharesRepaidYear", "2016"))
      val form = dateSharesRepaidForm.bind(map)
      form.hasErrors shouldBe true
      form.errors.head.message shouldBe Messages("validation.error.DateNotEntered")
    }

    "return a None if a model with an empty month input is supplied using .bind" in {
      val map = Map(("dateSharesRepaidDay", "4"), ("dateSharesRepaidMonth", ""), ("dateSharesRepaidYear", "2016"))
      val form = dateSharesRepaidForm.bind(map)
      form.hasErrors shouldBe true
      form.errors.head.message shouldBe Messages("validation.error.DateNotEntered")
    }

    "return a None if a model with an empty yer input is supplied using .bind" in {
      val map = Map(("dateSharesRepaidDay", "4"), ("dateSharesRepaidMonth", "5"), ("dateSharesRepaidYear", ""))
      val form = dateSharesRepaidForm.bind(map)
      form.hasErrors shouldBe true
      form.errors.head.message shouldBe Messages("validation.error.DateNotEntered")
    }

    "return a None if a model with a date in the future is supplied using .bind" in {
      val map = Map(("dateSharesRepaidDay", tomorrowDay), ("dateSharesRepaidMonth", tomorrowMonth), ("dateSharesRepaidYear", tomorrowYear))
      val form = dateSharesRepaidForm.bind(map)
      form.hasErrors shouldBe true
      form.errors.head.message shouldBe Messages("validation.error.dateSharesRepaid.Future")
    }

    "return a Some date if a model with a non future date (today) is supplied using .bind" in {
      val map = Map(("dateSharesRepaidDay", todayDay), ("dateSharesRepaidMonth", todayMonth), ("dateSharesRepaidYear", todayYear))
      val form = dateSharesRepaidForm.bind(map)
      form.hasErrors shouldBe false
    }

  }
}
