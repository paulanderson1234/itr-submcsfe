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

import auth.{MockAuthConnector, MockConfigSingleFlow}
import common.{Constants, KeystoreKeys}
import controllers.seis.AddInvestorOrNomineeController
import models.AddInvestorOrNomineeModel
import models.investorDetails.InvestorDetailsModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import views.helpers.ViewSpec

import scala.concurrent.Future

class AddInvestorOrNomineeSpec extends ViewSpec {

  val testUrl = "/test/test"
  val testUrlOther = "/test/test/testanother"
  val validModel = AddInvestorOrNomineeModel(Constants.investor, Some(1))

  object TestController extends AddInvestorOrNomineeController {
    override lazy val applicationConfig = MockConfigSingleFlow
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  def setupMocks(individualDetailsModels: Option[Vector[InvestorDetailsModel]], backLink: Option[String] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](Matchers.eq(KeystoreKeys.investorDetails))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(individualDetailsModels))

    when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkAddInvestorOrNominee))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(backLink))

    when(mockS4lConnector.saveFormData(Matchers.eq(KeystoreKeys.addInvestor),
      Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(CacheMap("", Map())))
  }

  "AddInvestorOrNominee view" should {
      "Verify that the page contains the correct elements when a valid model is passed from keystore with expected url" in new SEISSetup {
        val document: Document = {
          setupMocks(Some(onlyInvestorOrNomineeVectorList), Some(testUrl))
          val result = TestController.show(None).apply(authorisedFakeRequest)
          Jsoup.parse(contentAsString(result))
        }
        document.title() shouldBe Messages("page.investors.AddInvestorOrNominee.title")
        document.select("a.back-link").text() shouldBe Messages("common.button.back")
        document.select("a.back-link").attr("href") shouldBe testUrl

        document.select("article span").first().text shouldBe Messages("common.section.progress.company.details.four")
        document.select("h1").text() shouldBe Messages("page.investors.AddInvestorOrNominee.heading")
        document.select("article p").get(0).text() shouldBe Messages("page.investors.AddInvestorOrNominee.info.one")
        document.select("article p").get(1).text() shouldBe Messages("page.investors.AddInvestorOrNominee.info.two")
        document.getElementById("addInvestorOrNominee-investorLabel").text() shouldBe Messages("page.investors.AddInvestorOrNominee.radioButton.one")
        document.getElementById("addInvestorOrNominee-nomineeLabel").text() shouldBe Messages("page.investors.AddInvestorOrNominee.radioButton.two")
        document.getElementById("addInvestorOrNominee-legend").select(".visuallyhidden").text() shouldBe
          Messages("page.investors.AddInvestorOrNominee.heading")

        document.select("form").attr("action") shouldBe controllers.seis.routes.AddInvestorOrNomineeController.submit().url
        document.select("button").text() shouldBe Messages("common.button.snc")

      }


    "Verify that page contains the correct elements when a valid model is passed from keystore with alternate url" in new SEISSetup {
      val document: Document = {
        setupMocks(Some(onlyInvestorOrNomineeVectorList), Some(testUrlOther))
        val result = TestController.show(Some(1)).apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.investors.AddInvestorOrNominee.title")
      document.select("a.back-link").text() shouldBe Messages("common.button.back")
      document.select("a.back-link").attr("href") shouldBe testUrlOther

      document.select("article span").first().text shouldBe Messages("common.section.progress.company.details.four")
      document.select("h1").text() shouldBe Messages("page.investors.AddInvestorOrNominee.heading")
      document.select("article p").get(0).text() shouldBe Messages("page.investors.AddInvestorOrNominee.info.one")
      document.select("article p").get(1).text() shouldBe Messages("page.investors.AddInvestorOrNominee.info.two")
      document.getElementById("addInvestorOrNominee-investorLabel").text() shouldBe Messages("page.investors.AddInvestorOrNominee.radioButton.one")
      document.getElementById("addInvestorOrNominee-nomineeLabel").text() shouldBe Messages("page.investors.AddInvestorOrNominee.radioButton.two")
      document.getElementById("addInvestorOrNominee-legend").select(".visuallyhidden").text() shouldBe
        Messages("page.investors.AddInvestorOrNominee.heading")

      document.select("form").attr("action") shouldBe controllers.seis.routes.AddInvestorOrNomineeController.submit().url
      document.select("button").text() shouldBe Messages("common.button.snc")
    }

    "Verify that the page page contains the correct elements when an invalid models is passed" in new SEISSetup {
      val document: Document = {
        setupMocks(None, Some(testUrl))
        val result = TestController.submit.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.investors.AddInvestorOrNominee.title")
      document.select("a.back-link").text() shouldBe Messages("common.button.back")
      document.select("a.back-link").attr("href") shouldBe testUrl

      document.select("article span").first().text shouldBe Messages("common.section.progress.company.details.four")
      document.select("h1").text() shouldBe Messages("page.investors.AddInvestorOrNominee.heading")
      document.select("article p").get(0).text() shouldBe Messages("page.investors.AddInvestorOrNominee.info.one")
      document.select("article p").get(1).text() shouldBe Messages("page.investors.AddInvestorOrNominee.info.two")
      document.getElementById("addInvestorOrNominee-investorLabel").text() shouldBe Messages("page.investors.AddInvestorOrNominee.radioButton.one")
      document.getElementById("addInvestorOrNominee-nomineeLabel").text() shouldBe Messages("page.investors.AddInvestorOrNominee.radioButton.two")
      document.getElementById("addInvestorOrNominee-legend").select(".visuallyhidden").text() shouldBe
        Messages("page.investors.AddInvestorOrNominee.heading")

      document.select("form").attr("action") shouldBe controllers.seis.routes.AddInvestorOrNomineeController.submit().url
      document.select("button").text() shouldBe Messages("common.button.snc")
      // check error
      document.getElementById("addInvestorOrNominee-error-summary")

    }

  }
}
