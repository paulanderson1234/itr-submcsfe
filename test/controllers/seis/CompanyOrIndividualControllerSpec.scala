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
import play.api.test.Helpers._

import scala.concurrent.Future

class CompanyOrIndividualControllerSpec extends BaseSpec {

  object TestController extends CompanyOrIndividualController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  "CompanyOrIndividualController" should {
    "use the correct keystore connector" in {
      CompanyOrIndividualController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      CompanyOrIndividualController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      CompanyOrIndividualController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  def setupMocks(companyOrIndividualModel: Option[CompanyOrIndividualModel], addInvestorOrNomineeModel: Option[AddInvestorOrNomineeModel]): Unit = {
    when(TestController.s4lConnector.fetchAndGetFormData[CompanyOrIndividualModel](Matchers.eq(KeystoreKeys.companyOrIndividual))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(companyOrIndividualModel))
    when(TestController.s4lConnector.fetchAndGetFormData[AddInvestorOrNomineeModel](Matchers.eq(KeystoreKeys.addInvestor))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(addInvestorOrNomineeModel))
  }

  "Sending a GET request to CompanyOrIndividualController when authenticated and enrolled for SEIS" should {
    "return a 200 when something is fetched from keystore" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      setupMocks(Some(CompanyOrIndividualModel(Constants.typeCompany)), Some(investorModel))
      showWithSessionAndAuth(TestController.show())(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore for SEIS" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      setupMocks(None,  Some(investorModel))
      showWithSessionAndAuth(TestController.show())(
        result => status(result) shouldBe OK
      )
    }

    "redirect to the AddInvestorOrNomineePage when no InvestorOrNominee model is found to use in heading" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      setupMocks(Some(CompanyOrIndividualModel(Constants.typeCompany)), None)
      showWithSessionAndAuth(TestController.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.AddInvestorOrNomineeController.show().url)
        }
      )
    }
  }

  "Sending a valid 'Company' form submit to the CompanyOrIndividualController when authenticated and enrolled" should {
    "redirect to the company details page" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      val formInput = "companyOrIndividual" -> Constants.typeCompany
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.seis.routes.CompanyDetailsController.show().url)
        }
      )
    }
  }

  "Sending a valid 'Individual' form submit to the CompanyOrIndividualController when authenticated and enrolled for SEIS" should {
    "redirect to the individual details page" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      val formInput = "companyOrIndividual" -> Constants.typeIndividual
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.seis.routes.IndividualDetailsController.show().url)
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the CompanyOrIndividualController when authenticated" should {
    "redirect to itself" in {
      setupMocks(None,  Some(investorModel))
      mockEnrolledRequest(seisSchemeTypesModel)
      val formInput = "companyOrIndividual" -> ""
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }
}
