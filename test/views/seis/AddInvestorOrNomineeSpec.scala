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

package views.seis

import forms.AddInvestorOrNomineeForm._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import views.helpers.ViewSpec
import views.html.seis.investors.AddInvestorOrNominee

class AddInvestorOrNomineeSpec extends ViewSpec {

  "AddInvestorOrNominee view" should {

    "show the valid page details" in {

      val page =
        AddInvestorOrNominee(addInvestorOrNomineeForm,
          controllers.seis.routes.TotalAmountSpentController.show().toString)(authorisedFakeRequest, applicationMessages)
      val document = Jsoup.parse(page.body)

      document.title() shouldBe Messages("page.seis.investors.AddInvestorOrNominee.title")
      document.select("a.back-link").text() shouldBe Messages("common.button.back")
      document.select("a.back-link").attr("href") shouldBe "/investment-tax-relief-cs/seis/total-amount-spent"

      document.select("article span").first().text shouldBe Messages("common.section.progress.company.details.four")
      document.select("h1").text() shouldBe Messages("page.seis.investors.AddInvestorOrNominee.heading")
      document.select("article p").get(0).text() shouldBe Messages("page.seis.investors.AddInvestorOrNominee.info.one")
      document.select("article p").get(1).text() shouldBe Messages("page.seis.investors.AddInvestorOrNominee.info.two")
      document.getElementById("addInvestorOrNominee-investorLabel").text() shouldBe Messages("page.seis.investors.AddInvestorOrNominee.radioButton.one")
      document.getElementById("addInvestorOrNominee-nomineeLabel").text() shouldBe Messages("page.seis.investors.AddInvestorOrNominee.radioButton.two")
      document.getElementById("addInvestorOrNominee-legend").select(".visuallyhidden").text() shouldBe
        Messages("page.seis.investors.AddInvestorOrNominee.heading")

      document.select("form").attr("action") shouldBe controllers.seis.routes.AddInvestorOrNomineeController.submit().url
      document.select("button").text() shouldBe Messages("common.button.snc")

    }
  }
}
