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

import controllers.seis.routes
import forms.IsExistingShareHolderForm._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.FormError
import play.api.i18n.Messages
import views.helpers.ViewSpec
import play.api.i18n.Messages.Implicits._
import views.html.seis.investors.IsExistingShareHolder

class IsExistingShareHolderSpec extends ViewSpec {

  def commonTests(document: Document, templateText: String): Unit = {
    document.title() shouldBe Messages("page.investors.isExistingShareHolder.title", templateText)
    document.getElementById("main-heading").text() shouldBe Messages("page.investors.isExistingShareHolder.heading", templateText)
    document.getElementById("next").text() shouldBe Messages("common.button.snc")
    document.body.getElementById("back-link").attr("href") shouldEqual controllers.seis.routes.HowMuchSpentOnSharesController.show(1).url
    document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.details.four")

    document.select("label[for=isExistingShareHolder-yes]").text() shouldBe Messages("common.radioYesLabel")
    document.select("label[for=isExistingShareHolder-no]").text() shouldBe Messages("common.radioNoLabel")
  }

  lazy val backUrl = controllers.seis.routes.HowMuchSpentOnSharesController.show(1).url

  "The Is Existing Share Holder page" should {
    val templateText = "individual"
    "show the correct elements" when{
      "a form with an empty model is passed to the template" in {
        lazy val page = IsExistingShareHolder(templateText, isExistingShareHolderForm, backUrl)(fakeRequest, applicationMessages)
        lazy val document = Jsoup.parse(page.body)

        commonTests(document, templateText)
      }
      "a form with errors is passed to the template" in {
        val formWithErrors = isExistingShareHolderForm.bind(Map(""->""))
        lazy val page = IsExistingShareHolder(templateText, formWithErrors, backUrl)(fakeRequest, applicationMessages)
        lazy val document = Jsoup.parse(page.body)

        commonTests(document, templateText)

        document.getElementById("error-summary-display").hasClass("error-summary--show")
      }
    }
  }
}