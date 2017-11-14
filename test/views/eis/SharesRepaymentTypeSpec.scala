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
import play.api.data.Form
import play.api.i18n.Messages
import views.helpers.ViewSpec
import play.api.i18n.Messages.Implicits._
import views.html.eis.investors.SharesRepaymentType
import forms.SharesRepaymentTypeForm._
import models.repayments.SharesRepaymentTypeModel

class SharesRepaymentTypeSpec extends ViewSpec {

  val page = (form: Form[SharesRepaymentTypeModel]) =>
    SharesRepaymentType(form, controllers.eis.routes.WhoRepaidSharesController.show(Some(1)).url)(fakeRequest, applicationMessages)

  "The share repayment type page" should {

    "contain the correct elements when a valid model is passed to the page" in new Setup {
      val document = Jsoup.parse(page(sharesRepaymentTypeForm.fill(repaymentTypeShares)).body)
      document.title() shouldBe Messages("page.sharesRepaymentType.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.sharesRepaymentType.heading")
      document.getElementById("main-heading").hasClass("heading-xlarge") shouldBe true
      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.details.four")
      document.getElementById("sharesRepaymentType-sharesLabel").text() shouldBe Messages("page.sharesRepaymentType.shares")
      document.getElementById("sharesRepaymentType-debenturesLabel").text() shouldBe Messages("page.sharesRepaymentType.debentures")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.WhoRepaidSharesController.show(Some(1)).url
      document.getElementById("sharesRepaymentType-legend").hasClass("visuallyhidden") shouldBe true
      document.getElementById("sharesRepaymentType-legend").text shouldBe Messages("page.sharesRepaymentType.heading")
      document.select(".error-summary").isEmpty shouldBe true
    }

    "contain the correct elements when the model is empty and no SharesRepaymentTypeModel is loaded" in new Setup {
      val document = Jsoup.parse(page(sharesRepaymentTypeForm).body)
      document.title() shouldBe Messages("page.sharesRepaymentType.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.sharesRepaymentType.heading")
      document.getElementById("main-heading").hasClass("heading-xlarge") shouldBe true
      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.details.four")
      document.getElementById("sharesRepaymentType-sharesLabel").text() shouldBe Messages("page.sharesRepaymentType.shares")
      document.getElementById("sharesRepaymentType-debenturesLabel").text() shouldBe Messages("page.sharesRepaymentType.debentures")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.WhoRepaidSharesController.show(Some(1)).url
      document.getElementById("sharesRepaymentType-legend").hasClass("visuallyhidden") shouldBe true
      document.getElementById("sharesRepaymentType-legend").text shouldBe Messages("page.sharesRepaymentType.heading")
      document.select(".error-summary").isEmpty shouldBe true
    }

    "contains the correct elements when the page is bound to a request that is invalid and has model errors" in new Setup {
      val document = Jsoup.parse(page(sharesRepaymentTypeForm.bindFromRequest()(fakeRequest.withHeaders("" -> ""))).body)
      document.title() shouldBe Messages("page.sharesRepaymentType.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.sharesRepaymentType.heading")
      document.getElementById("main-heading").hasClass("heading-xlarge") shouldBe true
      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.details.four")
      document.getElementById("sharesRepaymentType-sharesLabel").text() shouldBe Messages("page.sharesRepaymentType.shares")
      document.getElementById("sharesRepaymentType-debenturesLabel").text() shouldBe Messages("page.sharesRepaymentType.debentures")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.WhoRepaidSharesController.show(Some(1)).url
      document.getElementById("sharesRepaymentType-legend").hasClass("visuallyhidden") shouldBe true
      document.getElementById("sharesRepaymentType-legend").text shouldBe Messages("page.sharesRepaymentType.heading")
      document.getElementById("error-summary-display").hasClass("error-summary--show") shouldBe true
      document.getElementById("error-summary-heading").text shouldBe Messages("common.error.summary.heading")
      document.getElementById("sharesRepaymentType-error-summary").text shouldBe Messages("validation.common.error.fieldRequired")
      document.getElementsByClass("error-notification").text shouldBe Messages("validation.common.error.fieldRequired")

    }

  }

}
