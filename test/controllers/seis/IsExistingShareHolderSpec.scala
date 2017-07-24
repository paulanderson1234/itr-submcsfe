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
import models.investorDetails.IsExistingShareHolderModel
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._

import scala.concurrent.Future

class IsExistingShareHolderSpec extends BaseSpec {

  object TestController extends IsExistingShareHolderController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  "IsExistingShareHolderController" should {
    "use the correct keystore connector" in {
      IsExistingShareHolderController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      IsExistingShareHolderController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      IsExistingShareHolderController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  def setupMocks(isExistingShareHolderModel: Option[IsExistingShareHolderModel], companyOrIndividualModel: Option[CompanyOrIndividualModel]): Unit = {
    when(TestController.s4lConnector.fetchAndGetFormData[IsExistingShareHolderModel](Matchers.eq(KeystoreKeys.isExistingShareHolder))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(isExistingShareHolderModel))
    when(TestController.s4lConnector.fetchAndGetFormData[CompanyOrIndividualModel](Matchers.eq(KeystoreKeys.companyOrIndividual))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(companyOrIndividualModel))
  }

  "Sending a GET request to IsExistingShareHolderController when authenticated and enrolled for SEIS" should {
    "return a 200 when something is fetched from keystore" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      setupMocks(Some(IsExistingShareHolderModel(Constants.StandardRadioButtonYesValue)), Some(companyOrIndividualModel))
      showWithSessionAndAuth(TestController.show())(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore for SEIS" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      setupMocks(None,  Some(companyOrIndividualModel))
      showWithSessionAndAuth(TestController.show())(
        result => status(result) shouldBe OK
      )
    }

    "redirect to the IsExistingShareholderController when no CompanyOrIndividual model is found to use in heading" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      setupMocks(Some(IsExistingShareHolderModel(Constants.StandardRadioButtonYesValue)), None)
      showWithSessionAndAuth(TestController.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.CompanyOrIndividualController.show().url)
        }
      )
    }
  }

  "Sending a valid 'Yes' form submit to the IsExistingShareHolderController when authenticated and enrolled" should {
    "redirect to the Investor Share Issue Date page" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      val formInput = "isExistingShareHolder" -> Constants.StandardRadioButtonYesValue
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.seis.routes.IsExistingShareHolderController.show().url)
          //TODO - Navigates to the Investor Share Issue page when available
        }
      )
    }
  }

  "Sending a valid 'No' form submit to the IsExistingShareHolderController when authenticated and enrolled for SEIS" should {
    "redirect to the Review Investor page" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      val formInput = "isExistingShareHolder" -> Constants.StandardRadioButtonNoValue
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.seis.routes.IsExistingShareHolderController.show().url)
          //TODO - Naivigates to the Review Investor page when available
        }
      )
    }
  }


  "Sending an invalid form submission with validation errors to the IsExistingShareHolderController when authenticated" should {
    "redirect to itself" in {
      setupMocks(None,  Some(companyOrIndividualModel))
      mockEnrolledRequest(seisSchemeTypesModel)
      val formInput = "isExistingShareHolder" -> ""
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }
}
