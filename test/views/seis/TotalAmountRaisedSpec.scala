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

import forms.TotalAmountRaisedForm._
import models.TotalAmountRaisedModel
import org.jsoup.Jsoup
import play.api.data.Form
import play.api.i18n.Messages
import views.helpers.ViewSpec
import play.api.i18n.Messages.Implicits._
import controllers.seis.routes
import views.html.seis.shareDetails.TotalAmountRaised

class TotalAmountRaisedSpec extends ViewSpec {

  val page = (form: Form[TotalAmountRaisedModel]) =>
    TotalAmountRaised(form)(fakeRequest, applicationMessages)

    "The TotalAmountRaised page" should {
    "contain the correct elements for a GET when a valid TotalAmountRaisedModel is loaded" in new SEISSetup {
      val document = Jsoup.parse(page(totalAmountRaisedForm.fill(TotalAmountRaisedModel(1))).body)
      document.title() shouldBe Messages("page.shareDetails.totalAmountRaised.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.shareDetails.totalAmountRaised.heading")
      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.three")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.NominalValueOfSharesController.show().url
      document.getElementById("label-amount").select("span").hasClass("visuallyhidden") shouldBe true
      document.getElementById("label-amount").select(".visuallyhidden").text() shouldBe Messages("page.shareDetails.totalAmountRaised.heading")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      
      // secondary paragraph:
      document.body.getElementById("description-one").text shouldBe Messages("page.shareDetails.totalAmountRaised.descriptionOne.text")
    }
	
    "contain the correct elements when the model is empty and no TotalAmountRaisedModel is loaded" in new SEISSetup {
      val document = Jsoup.parse(page(totalAmountRaisedForm).body)
      document.title() shouldBe Messages("page.shareDetails.totalAmountRaised.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.shareDetails.totalAmountRaised.heading")
      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.three")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.NominalValueOfSharesController.show().url
      document.getElementById("label-amount").select("span").hasClass("visuallyhidden") shouldBe true
      document.getElementById("label-amount").select(".visuallyhidden").text() shouldBe Messages("page.shareDetails.totalAmountRaised.heading")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      
      // secondary paragraph:
      document.body.getElementById("description-one").text shouldBe Messages("page.shareDetails.totalAmountRaised.descriptionOne.text")
    }

    "contain the correct elements including an error sumary when an invalid TotalAmountRaisedModel is loaded" in new SEISSetup {
      val document = Jsoup.parse(page(totalAmountRaisedForm.bindFromRequest()(fakeRequest.withHeaders("" -> ""))).body)
      document.title() shouldBe Messages("page.shareDetails.totalAmountRaised.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.shareDetails.totalAmountRaised.heading")
      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.three")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.NominalValueOfSharesController.show().url
      document.getElementById("label-amount").select("span").hasClass("visuallyhidden") shouldBe true
      document.getElementById("label-amount").select(".visuallyhidden").text() shouldBe Messages("page.shareDetails.totalAmountRaised.heading")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      // Check error present:
      document.getElementById("error-summary-display").hasClass("error-summary--show")
      
      // secondary paragraph:
      document.body.getElementById("description-one").text shouldBe Messages("page.shareDetails.totalAmountRaised.descriptionOne.text")
     
    }
   
  }

}
