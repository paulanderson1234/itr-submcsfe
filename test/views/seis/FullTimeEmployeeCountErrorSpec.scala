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

import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits.applicationMessages
import views.helpers.ViewSpec
import views.html.seis.companyDetails.FullTimeEmployeeCountError

class FullTimeEmployeeCountErrorSpec extends ViewSpec {

  "The FullTimeEmployeeCountError view" should {
    lazy val doc = Jsoup.parse(FullTimeEmployeeCountError()(fakeRequest, applicationMessages).body)

    "contain a title with the correct text" in {
      doc.title() shouldBe Messages("page.seis.companyDetails.fullTimeEmployeeCountError.title")
    }

    "contain a back link" which {
      lazy val backLink = doc.select("article a").first()

      "which has the back text" in {
        backLink.text() shouldBe Messages("common.button.back")
      }

      "has a link to FullTimeEmployeeCount page" in {
        backLink.attr("href") shouldBe "/investment-tax-relief-cs/seis/full-time-employee-count"
      }
    }

    "contain a heading with the correct text" in {
      doc.select("h1").text() shouldBe Messages("page.seis.companyDetails.fullTimeEmployeeCountError.title")
    }

    "contain a paragraph explaining the error" in {
      doc.select("article p").first().text() shouldBe Messages("page.seis.companyDetails.fullTimeEmployeeCountError.error")
    }

    "contain a sub-heading for what to do next" in {
      doc.select("h2").text() should include(Messages("page.seis.companyDetails.fullTimeEmployeeCountError.whatNext"))
    }

    "contain a paragraph explaining what actions should be taken" in {
      doc.select("article p").get(1).text() shouldBe Messages("page.seis.companyDetails.fullTimeEmployeeCountError.guidance")
    }

    "contain a change link" which {
      lazy val change = doc.select("article div span")

      "has the correct guidance text" in {
        change.text() shouldBe s"${Messages("page.seis.companyDetails.fullTimeEmployeeCountError.incorrect")} ${Messages("page.seis.companyDetails.fullTimeEmployeeCountError.incorrect.link")}."
      }

      "has a link with an href to FullTimeEmployeeCount page" in {
        change.select("a").attr("href") shouldBe "/investment-tax-relief-cs/seis/full-time-employee-count"
      }

      "has the correct link component text" in {
        change.select("a").text() shouldBe Messages("page.seis.companyDetails.fullTimeEmployeeCountError.incorrect.link")
      }
    }
  }
}
