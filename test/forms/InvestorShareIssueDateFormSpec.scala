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

import models.ShareIssueDateModel
import models.investorDetails._
import forms.InvestorShareIssueDateForm._
import org.scalatestplus.play.OneAppPerSuite
import uk.gov.hmrc.play.test.UnitSpec
import java.time.ZoneId
import java.util.Date

class InvestorShareIssueDateFormSpec extends UnitSpec with OneAppPerSuite{

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

  "Creating the form for the date of investorShareIssue date" should {
    "return a populated form using .fill" in {
      val model = InvestorShareIssueDateModel(Some(10), Some(2), Some(2016))
      val form = investorShareIssueDateForm.fill(model)
      form.value.get shouldBe InvestorShareIssueDateModel(Some(10), Some(2), Some(2016))
    }

    "return a Some if a model with valid inputs is supplied using .bind" in {
      val map = Map(("investorShareIssueDateDay", "10"), ("investorShareIssueDateMonth", "3"), ("investorShareIssueDateYear", "2016"))
      val form = investorShareIssueDateForm.bind(map)
      form.value shouldBe Some(InvestorShareIssueDateModel(Some(10), Some(3), Some(2016)))
    }

    "return a None if a model with non-numeric inputs is supplied using .bind" in {
      val map = Map(("investorShareIssueDateDay", "a"), ("investorShareIssueDateMonth", "b"), ("investorShareIssueDateYear", "c"))
      val form = investorShareIssueDateForm.bind(map)
      form.hasErrors shouldBe true
    }


    "return a None if a model with a single digit year is supplied using .bind" in {
      val map = Map(("investorShareIssueDateDay", "2"), ("investorShareIssueDateMonth", "2"), ("investorShareIssueDateYear", "2"))
      val form = investorShareIssueDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a double digit year is supplied using .bind" in {
      val map = Map(("investorShareIssueDateDay", "2"), ("investorShareIssueDateMonth", "2"), ("investorShareIssueDateYear", "22"))
      val form = investorShareIssueDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a triple digit year is supplied using .bind" in {
      val map = Map(("investorShareIssueDateDay", "2"), ("investorShareIssueDateMonth", "2"), ("investorShareIssueDateYear", "222"))
      val form = investorShareIssueDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a day of 32 is supplied using .bind" in {
      val map = Map(("investorShareIssueDateDay", "32"), ("investorShareIssueDateMonth", "3"), ("investorShareIssueDateYear", "1980"))
      val form = investorShareIssueDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a 29th Feb in a non leap year is supplied using .bind" in {
      val map = Map(("investorShareIssueDateDay", "29"), ("investorShareIssueDateMonth", "2"), ("investorShareIssueDateYear", "1981"))
      val form = investorShareIssueDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a Some if a model with a 29th Feb in a leap year is supplied using .bind" in {
      val map = Map(("investorShareIssueDateDay", "29"), ("investorShareIssueDateMonth", "2"), ("investorShareIssueDateYear", "2004"))
      val form = investorShareIssueDateForm.bind(map)
      form.hasErrors shouldBe false
    }

    "return a None if a model with a 31st june is supplied using .bind" in {
      val map = Map(("investorShareIssueDateDay", "31"), ("investorShareIssueDateMonth", "6"), ("investorShareIssueDateYear", "1981"))
      val form = investorShareIssueDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a 31st september is supplied using .bind" in {
      val map = Map(("investorShareIssueDateDay", "31"), ("investorShareIssueDateMonth", "9"), ("investorShareIssueDateYear", "1981"))
      val form = investorShareIssueDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a 31st November is supplied using .bind" in {
      val map = Map(("investorShareIssueDateDay", "31"), ("investorShareIssueDateMonth", "11"), ("investorShareIssueDateYear", "1981"))
      val form = investorShareIssueDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a 31st April is supplied using .bind" in {
      val map = Map(("investorShareIssueDateDay", "31"), ("investorShareIssueDateMonth", "4"), ("investorShareIssueDateYear", "1981"))
      val form = investorShareIssueDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a non-valid date input is supplied using .bind" in {
      val map = Map(("investorShareIssueDateDay", "32"), ("investorShareIssueDateMonth", "4"), ("investorShareIssueDateYear", "2016"))
      val form = investorShareIssueDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with an empty day input is supplied using .bind" in {
      val map = Map(("investorShareIssueDateDay", ""), ("investorShareIssueDateMonth", "4"), ("investorShareIssueDateYear", "2016"))
      val form = investorShareIssueDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with an empty month input is supplied using .bind" in {
      val map = Map(("investorShareIssueDateDay", "4"), ("investorShareIssueDateMonth", ""), ("investorShareIssueDateYear", "2016"))
      val form = investorShareIssueDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with an empty yer input is supplied using .bind" in {
      val map = Map(("investorShareIssueDateDay", "4"), ("investorShareIssueDateMonth", "5"), ("investorShareIssueDateYear", ""))
      val form = investorShareIssueDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a date in the future is supplied using .bind" in {
      val map = Map(("investorShareIssueDateDay", tomorrowDay), ("investorShareIssueDateMonth", tomorrowMonth), ("investorShareIssueDateYear", tomorrowYear))
      val form = investorShareIssueDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a Some date if a model with a non future date (today) is supplied using .bind" in {
      val map = Map(("investorShareIssueDateDay", todayDay), ("investorShareIssueDateMonth", todayMonth), ("investorShareIssueDateYear", todayYear))
      val form = investorShareIssueDateForm.bind(map)
      form.hasErrors shouldBe false
    }

  }
}
