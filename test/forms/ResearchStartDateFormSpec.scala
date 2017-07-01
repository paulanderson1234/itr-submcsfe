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

import java.time.ZoneId
import java.util.Date

import common.Constants
import forms.ResearchStartDateForm._
import models.ResearchStartDateModel
import org.scalatestplus.play.OneAppPerSuite
import uk.gov.hmrc.play.test.UnitSpec

/**
  * Created by james-forster on 22/06/17.
  */
class ResearchStartDateFormSpec extends UnitSpec with OneAppPerSuite {

  // set up border line conditions of today and future date (tomorrow)
  val date = new Date();
  val localDate = date.toInstant.atZone(ZoneId.systemDefault()).toLocalDate;
  val tomorrow = localDate.plusDays(1)
  val tomorrowDay: String = tomorrow.getDayOfMonth.toString
  val tomorrowMonth: String = tomorrow.getMonthValue.toString
  val tomorrowYear: String = tomorrow.getYear.toString

  val todayDay:String = localDate.getDayOfMonth.toString
  val todayMonth: String = localDate.getMonthValue.toString
  val todayYear: String = localDate.getYear.toString

  "Creating the form for the research start date" should {
    "return a populated yes form using .fill" in {
      val model = ResearchStartDateModel(Constants.StandardRadioButtonYesValue, Some(10), Some(2), Some(2016))
      val form = researchStartDateForm.fill(model)
      form.value.get shouldBe ResearchStartDateModel(Constants.StandardRadioButtonYesValue, Some(10), Some(2), Some(2016))
    }

    "return a populated no form using .fill" in {
      val model = ResearchStartDateModel(Constants.StandardRadioButtonNoValue, None, None, None)
      val form = researchStartDateForm.fill(model)
      form.value.get shouldBe ResearchStartDateModel(Constants.StandardRadioButtonNoValue, None, None, None)
    }

    "return a Some if a model with valid inputs is supplied using .bind" in {
      val map = Map(("hasStartedResearch",Constants.StandardRadioButtonYesValue),("researchStartDay", "10"), ("researchStartMonth", "3"), ("researchStartYear", "2016"))
      val form = researchStartDateForm.bind(map)
      form.value shouldBe Some(ResearchStartDateModel(Constants.StandardRadioButtonYesValue, Some(10), Some(3), Some(2016)))
    }

    "return a None if a model with non-numeric inputs is supplied using .bind" in {
      val map = Map(("hasStartedResearch",Constants.StandardRadioButtonYesValue),("researchStartDay", "a"), ("researchStartMonth", "b"), ("researchStartYear", "c"))
      val form = researchStartDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with both a 'No' selection and date present using .bind" in {
      val map = Map(("hasStartedResearch",Constants.StandardRadioButtonNoValue),("researchStartDay", "2"), ("researchStartMonth", "4"), ("researchStartYear", "2006"))
      val form = researchStartDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with both a 'No' selection and partial date with day present using .bind" in {
      val map = Map(("hasStartedResearch",Constants.StandardRadioButtonNoValue),("researchStartDay", "2"), ("researchStartMonth", ""), ("researchStartYear", ""))
      val form = researchStartDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with both a 'No' selection and partial date with month present using .bind" in {
      val map = Map(("hasStartedResearch",Constants.StandardRadioButtonNoValue),("researchStartDay", ""), ("researchStartMonth", "2"), ("researchStartYear", ""))
      val form = researchStartDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with both a 'No' selection and partial date with year present using .bind" in {
      val map = Map(("hasStartedResearch",Constants.StandardRadioButtonNoValue),("researchStartDay", ""), ("researchStartMonth", ""), ("researchStartYear", "2006"))
      val form = researchStartDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with both a 'No' selection and partial date with invalid day present using .bind" in {
      val map = Map(("hasStartedResearch",Constants.StandardRadioButtonNoValue),("researchStartDay", "f"), ("researchStartMonth", ""), ("researchStartYear", ""))
      val form = researchStartDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with both a 'No' selection and partial date with invalid month present using .bind" in {
      val map = Map(("hasStartedResearch",Constants.StandardRadioButtonNoValue),("researchStartDay", ""), ("researchStartMonth", "z"), ("researchStartYear", ""))
      val form = researchStartDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with both a 'No' selection and partial date with invalid year present using .bind" in {
      val map = Map(("hasStartedResearch",Constants.StandardRadioButtonNoValue),("researchStartDay", ""), ("researchStartMonth", ""), ("researchStartYear", "q"))
      val form = researchStartDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with both a 'No' selection and partial date with year in future using .bind" in {
      val map = Map(("hasStartedResearch",Constants.StandardRadioButtonNoValue),("researchStartDay", ""), ("researchStartMonth", ""), ("researchStartYear", "9999"))
      val form = researchStartDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a single digit year is supplied using .bind" in {
      val map = Map(("hasStartedResearch",Constants.StandardRadioButtonYesValue),("researchStartDay", "2"), ("researchStartMonth", "2"), ("researchStartYear", "2"))
      val form = researchStartDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a double digit year is supplied using .bind" in {
      val map = Map(("hasStartedResearch",Constants.StandardRadioButtonYesValue),("researchStartDay", "2"), ("researchStartMonth", "2"), ("researchStartYear", "22"))
      val form = researchStartDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a triple digit year is supplied using .bind" in {
      val map = Map(("hasStartedResearch",Constants.StandardRadioButtonYesValue),("researchStartDay", "2"), ("researchStartMonth", "2"), ("researchStartYear", "222"))
      val form = researchStartDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a day of 32 is supplied using .bind" in {
      val map = Map(("hasStartedResearch",Constants.StandardRadioButtonYesValue),("researchStartDay", "32"), ("researchStartMonth", "3"), ("researchStartYear", "1980"))
      val form = researchStartDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a 29th Feb in a non leap year is supplied using .bind" in {
      val map = Map(("hasStartedResearch",Constants.StandardRadioButtonYesValue),("researchStartDay", "29"), ("researchStartMonth", "2"), ("researchStartYear", "1981"))
      val form = researchStartDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a Some if a model with a 29th Feb in a leap year is supplied using .bind" in {
      val map = Map(("hasStartedResearch",Constants.StandardRadioButtonYesValue),("researchStartDay", "29"), ("researchStartMonth", "2"), ("researchStartYear", "2004"))
      val form = researchStartDateForm.bind(map)
      form.hasErrors shouldBe false
    }

    "return a None if a model with a 31st june is supplied using .bind" in {
      val map = Map(("hasStartedResearch",Constants.StandardRadioButtonYesValue),("researchStartDay", "31"), ("researchStartMonth", "6"), ("researchStartYear", "1981"))
      val form = researchStartDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a 31st september is supplied using .bind" in {
      val map = Map(("hasStartedResearch",Constants.StandardRadioButtonYesValue),("researchStartDay", "31"), ("researchStartMonth", "9"), ("researchStartYear", "1981"))
      val form = researchStartDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a 31st November is supplied using .bind" in {
      val map = Map(("hasStartedResearch",Constants.StandardRadioButtonYesValue),("researchStartDay", "31"), ("researchStartMonth", "11"), ("researchStartYear", "1981"))
      val form = researchStartDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a 31st April is supplied using .bind" in {
      val map = Map(("hasStartedResearch",Constants.StandardRadioButtonYesValue),("researchStartDay", "31"), ("researchStartMonth", "4"), ("researchStartYear", "1981"))
      val form = researchStartDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a non-valid date input is supplied using .bind" in {
      val map = Map(("hasStartedResearch",Constants.StandardRadioButtonYesValue),("researchStartDay", "32"), ("researchStartMonth", "4"), ("researchStartYear", "2016"))
      val form = researchStartDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with an empty day input is supplied using .bind" in {

      val map = Map(("hasStartedResearch",Constants.StandardRadioButtonYesValue),("researchStartDay", ""), ("researchStartMonth", "4"), ("researchStartYear", "2016"))
      val form = researchStartDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with an empty month input is supplied using .bind" in {

      val map = Map(("hasStartedResearch",Constants.StandardRadioButtonYesValue),("researchStartDay", "5"), ("researchStartMonth", ""), ("researchStartYear", "2016"))
      val form = researchStartDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with an empty year input is supplied using .bind" in {

      val map = Map(("hasStartedResearch",Constants.StandardRadioButtonYesValue),("researchStartDay", "5"), ("researchStartMonth", "10"), ("researchStartYear", ""))
      val form = researchStartDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a date in the future is supplied using .bind" in {
      val map = Map(("hasStartedResearch",Constants.StandardRadioButtonYesValue),("researchStartDay", tomorrowDay), ("researchStartMonth", tomorrowMonth), ("researchStartYear", tomorrowYear))
      val form = researchStartDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a Some date if a model with a non future date (today) is supplied using .bind" in {
      val map = Map(("hasStartedResearch",Constants.StandardRadioButtonYesValue),("researchStartDay", todayDay), ("researchStartMonth", todayMonth), ("researchStartYear", todayYear))
      val form = researchStartDateForm.bind(map)
      form.hasErrors shouldBe false
    }

    "return a Some date if a model with no date an 'No' option is supplied using .bind" in {
      val map = Map(("hasStartedResearch",Constants.StandardRadioButtonNoValue),("researchStartDay", ""), ("researchStartMonth", ""), ("researchStartYear", ""))
      val form = researchStartDateForm.bind(map)
      form.hasErrors shouldBe false
    }

  }
}
