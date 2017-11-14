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

package views.eis

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import views.helpers.ViewSpec
import views.html.eis.checkAndSubmit.TermsAndConditions

class TermsAndConditionsSpec extends ViewSpec {

  "The Terms and Conditions page" should {

    val requestType = "GET"

    "Verify that the declaration page has the correct elements" in {
      val document: Document = {
        val page = TermsAndConditions()(fakeRequest, applicationMessages)
        Jsoup.parse(page.body)
      }
      document.title() shouldBe Messages("page.termsAndConditions.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.termsAndConditions.heading")
      document.getElementById("declaration").text() shouldBe Messages("page.termsAndConditions.declaration")

      document.getElementById("declarationOne").text() shouldBe Messages("page.termsAndConditions.declaration.one.heading")
      document.getElementById("help-bullet-declarationOne-one").text() shouldBe Messages("page.termsAndConditions.declaration.one.bullet.one")
      document.getElementById("help-bullet-declarationOne-two").text() shouldBe Messages("page.termsAndConditions.eis.declaration.one.bullet.two")


      document.getElementById("declarationTwo").text() shouldBe Messages("page.termsAndConditions.declaration.two.heading")
      document.getElementById("help-bullet-declarationTwo-one").text() shouldBe Messages("page.termsAndConditions.declaration.two.bullet.one")
      document.getElementById("help-bullet-declarationTwo-two").text() shouldBe Messages("page.termsAndConditions.declaration.two.bullet.two")
      document.getElementById("help-bullet-declarationTwo-three").text() shouldBe Messages("page.termsAndConditions.declaration.two.bullet.three")
      document.getElementById("help-bullet-declarationTwo-four").text() shouldBe Messages("page.termsAndConditions.declaration.two.bullet.four")
      document.getElementById("help-bullet-declarationTwo-five").text() shouldBe Messages("page.termsAndConditions.eis.declaration.two.bullet.five")
      document.getElementById("help-bullet-declarationTwo-six").text() shouldBe Messages("page.termsAndConditions.eis.declaration.two.bullet.six")
      document.getElementById("help-bullet-declarationTwo-seven").text() shouldBe Messages("page.termsAndConditions.eis.declaration.two.bullet.seven")

      document.getElementById("declarationThree").text() shouldBe Messages("page.termsAndConditions.eis.declaration.three.heading")
      document.getElementById("help-bullet-declarationThree-one").text() shouldBe Messages("page.termsAndConditions.declaration.three.bullet.one")
      document.getElementById("help-bullet-declarationThree-two").text() shouldBe Messages("page.termsAndConditions.declaration.three.bullet.two")
      document.getElementById("help-bullet-declarationThree-three").text() shouldBe Messages("page.termsAndConditions.eis.declaration.three.bullet.three")
      document.getElementById("help-bullet-declarationThree-four").text() shouldBe Messages("page.termsAndConditions.eis.declaration.three.bullet.four")
      document.getElementById("help-bullet-declarationThree-five").text() shouldBe Messages("page.termsAndConditions.eis.declaration.three.bullet.five")
      document.getElementById("help-bullet-declarationThree-six").text() shouldBe Messages("page.termsAndConditions.eis.declaration.three.bullet.six")
      document.getElementById("help-bullet-declarationThree-seven").text() shouldBe Messages("page.termsAndConditions.eis.declaration.three.bullet.seven")

    }
  }

}
