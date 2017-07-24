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

package controllers.seis

import auth.{MockAuthConnector, MockConfig}
import common.{Constants, KeystoreKeys}
import config.FrontendAuthConnector
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.BaseSpec
import models._
import models.investorDetails._
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class InvestorShareIssueDateControllerSpec extends BaseSpec {

  object TestController extends InvestorShareIssueDateController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  "InvestorShareIssueDateController" should {
    "use the correct keystore connector" in {
      InvestorShareIssueDateController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      InvestorShareIssueDateController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      InvestorShareIssueDateController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  def setupMocks(investorShareIssueDateModel: Option[InvestorShareIssueDateModel] = None, backLink: Option[String] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[InvestorShareIssueDateModel](Matchers.eq(KeystoreKeys.investorShareIssueDate))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(investorShareIssueDateModel))

    when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(CacheMap("", Map())))

    when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkInvestorShareIssueDate))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(backLink))

    when(mockS4lConnector.saveFormData(Matchers.eq(KeystoreKeys.backLinkInvestorShareIssueDate),
      Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(CacheMap("", Map())))

  }

  "Sending a GET request to InvestorShareIssueDateController when authenticated and enrolled" should {
    "return a 200 when something is fetched from keystore and back link returned" in {
      setupMocks(Some(investorShareIssueDateModel), Some("/test/test"))
      mockEnrolledRequest(seisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show())(
        result => status(result) shouldBe OK
      )
    }

    "return a 200 when something is fetched from keystore and back link is None" in {
      setupMocks(Some(investorShareIssueDateModel), None)
      mockEnrolledRequest(seisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.seis.routes.QualifyBusinessActivityController.show().url)
        }
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore" in {
      setupMocks(None, Some("/test/test/"))
      mockEnrolledRequest(seisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show())(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a valid form submit to the InvestorShareIssueDateController when authenticated and enrolled" should {
    "redirect to first trade start date page" in {
      setupMocks(Some(investorShareIssueDateModel))
      mockEnrolledRequest(seisSchemeTypesModel)

      val formInput = Seq(
        "shareIssueDay" -> "23",
        "shareIssueMonth" -> "11",
        "shareIssueYear" -> "1993")

      submitWithSessionAndAuth(TestController.submit,formInput:_*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.GrossAssetsController.show().url)
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the InvestorShareIssueDateController when authenticated and enrolled" should {
    "return a bad request" in {
      setupMocks(None, Some("/test/test"))
      mockEnrolledRequest(seisSchemeTypesModel)
      val formInput = Seq(
        "investorShareIssueDateDay" -> "",
        "investorShareIssueDateMonth" -> "",
        "investorShareIssueDateYear" -> "")

      submitWithSessionAndAuth(TestController.submit,formInput:_*)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }

}

