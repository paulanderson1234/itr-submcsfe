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
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers.{redirectLocation, _}
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class AddInvestorOrNomineeControllerSpec extends BaseSpec {

  val validBackLink = controllers.seis.routes.TotalAmountSpentController.show().toString

  object TestController extends AddInvestorOrNomineeController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  "AddInvestorOrNomineeController" should {
    "use the correct keystore connector" in {
      AddInvestorOrNomineeController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      AddInvestorOrNomineeController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      AddInvestorOrNomineeController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  def setupMocks(addInvestorOrNomineeModel : Option[AddInvestorOrNomineeModel] = None, backUrl: Option[String] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[AddInvestorOrNomineeModel](Matchers.eq(KeystoreKeys.addInvestor))
      (Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(addInvestorOrNomineeModel))
    when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkAddInvestorOrNominee))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(backUrl))
  }

  "Sending a GET request to AddInvestorOrNomineeController when authenticated and enrolled" should {
    "return a 200 when something is fetched from keystore" in {
      setupMocks(Some(investorModel), Some(validBackLink))
      mockEnrolledRequest(seisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show())(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore" in {
      setupMocks(None, Some(validBackLink))
      mockEnrolledRequest(seisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show())(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and no back url return a 200 and redirect to Share Description details page" in {
      setupMocks(None, None)
      mockEnrolledRequest(seisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.seis.routes.ShareDescriptionController.show().url)
        }
      )
    }
  }

  "By selecting the investor submission to the AddInvestorOrNomineeController when authenticated and enrolled" should {
    "redirect to the correct page if an investor" in {
      when(TestController.s4lConnector.saveFormData[AddInvestorOrNomineeModel](Matchers.eq(KeystoreKeys.addInvestor), Matchers.any())
        (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(CacheMap("",Map()))

      val formInput = "addInvestorOrNominee" -> Constants.investor
      setupMocks(None, Some(validBackLink))
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.seis.routes.CompanyOrIndividualController.show().url)
        }
      )
    }
  }

  "By selecting the investor submission to the AddInvestorOrNomineeController when authenticated and enrolled" should {
    "redirect to the correct page if a nominee" in {
      when(TestController.s4lConnector.saveFormData[AddInvestorOrNomineeModel](Matchers.eq(KeystoreKeys.addInvestor), Matchers.any())
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(CacheMap("",Map()))

      val formInput = "addInvestorOrNominee" -> Constants.nominee
      setupMocks(None, Some(validBackLink))
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.seis.routes.CompanyOrIndividualController.show().url)
        }
      )
    }
  }


  "Sending an invalid form submission with validation errors to the AddInvestorOrNomineeController when authenticated and enrolled" should {
    "redirect to itself" in {
      setupMocks(None, Some(validBackLink))
      mockEnrolledRequest(seisSchemeTypesModel)
      val formInput = "addInvestorOrNominee" -> ""
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }

}
