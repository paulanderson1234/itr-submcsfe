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

///*
// * Copyright 2017 HM Revenue & Customs
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package views.eis
//
//import forms.AmountSharesRepaymentForm._
//import org.jsoup.Jsoup
//import play.api.data.Form
//import play.api.i18n.Messages
//import views.helpers.ViewSpec
//import play.api.i18n.Messages.Implicits._
//import controllers.eis.routes
//import models.repayments.AmountSharesRepaymentModel
//import utils.Validation.financialMaxAmountLength
//import views.html.eis.investors.AmountSharesRepayment
//
//class AmountSharesRepaymentSpec extends ViewSpec {
//
//  val page = (form: Form[AmountSharesRepaymentModel]) =>
//    AmountSharesRepayment(form)(fakeRequest, applicationMessages)
//
//    "The AmountSharesRepayment page" should {
//    "contain the correct elements for a GET when a valid AmountSharesRepaymentModel is loaded" in new Setup {
//      val document = Jsoup.parse(page(amountSharesRepaymentForm.fill(AmountSharesRepaymentModel(1))).body)
//      document.title() shouldBe Messages("page.AmountSharesRepayment.title")
//      document.getElementById("main-heading").text() shouldBe Messages("page.AmountSharesRepayment.heading")
//      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.details.four")
//      document.body.getElementById("back-link").attr("href") shouldEqual routes.DateSharesRepaidController.show().url
//      document.getElementById("label-amount").select("span").hasClass("visuallyhidden") shouldBe true
//      document.getElementById("label-amount").select(".visuallyhidden").text() shouldBe Messages("page.AmountSharesRepayment.heading")
//      document.getElementById("next").text() shouldBe Messages("common.button.snc")
//      document.getElementsByTag("span").select(".visuallyhidden").text() shouldBe Messages("page.AmountSharesRepayment.heading")
//      document.select("input").attr("maxLength") shouldBe financialMaxAmountLength.toString
//      document.select(".error-summary").isEmpty shouldBe true
//
//    }
//
//    "contain the correct elements when the model is empty and no AmountSharesRepaymentModel is loaded" in new Setup {
//      val document = Jsoup.parse(page(amountSharesRepaymentForm).body)
//      document.title() shouldBe Messages("page.AmountSharesRepayment.title")
//      document.getElementById("main-heading").text() shouldBe Messages("page.AmountSharesRepayment.heading")
//      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.details.four")
//      document.body.getElementById("back-link").attr("href") shouldEqual routes.DateSharesRepaidController.show().url
//      document.getElementById("label-amount").select("span").hasClass("visuallyhidden") shouldBe true
//      document.getElementById("label-amount").select(".visuallyhidden").text() shouldBe Messages("page.AmountSharesRepayment.heading")
//      document.getElementById("next").text() shouldBe Messages("common.button.snc")
//      document.getElementsByTag("span").select(".visuallyhidden").text() shouldBe Messages("page.AmountSharesRepayment.heading")
//      document.select("input").attr("maxLength") shouldBe financialMaxAmountLength.toString
//      document.select(".error-summary").isEmpty shouldBe true
//
//    }
//
//    "contain the correct elements including an error sumary when an invalid AmountSharesRepaymentModel is loaded" in new Setup {
//      val document = Jsoup.parse(page(amountSharesRepaymentForm.bindFromRequest()(fakeRequest.withHeaders("" -> ""))).body)
//      document.title() shouldBe Messages("page.AmountSharesRepayment.title")
//      document.getElementById("main-heading").text() shouldBe Messages("page.AmountSharesRepayment.heading")
//      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.details.four")
//      document.body.getElementById("back-link").attr("href") shouldEqual routes.DateSharesRepaidController.show().url
//      document.getElementById("label-amount").select("span").hasClass("visuallyhidden") shouldBe true
//      document.getElementById("label-amount").select(".visuallyhidden").text() shouldBe Messages("page.AmountSharesRepayment.heading")
//      document.getElementsByTag("span").select(".visuallyhidden").text() shouldBe Messages("page.AmountSharesRepayment.heading")
//      document.getElementById("next").text() shouldBe Messages("common.button.snc")
//      document.select("input").attr("maxLength") shouldBe financialMaxAmountLength.toString
//      // Check error present:
//      document.getElementById("error-summary-display").hasClass("error-summary--show") shouldBe true
//
//    }
//
//  }
//
//}
