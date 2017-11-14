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

package controllers.eis

import auth.{MockAuthConnector, MockConfig}
import common.{Constants, KeystoreKeys}
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.BaseSpec
import models.investorDetails.InvestorDetailsModel
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import services.SubscriptionService

import scala.concurrent.Future

class CompanyDetailsControllerSpec extends BaseSpec {

  object TestController extends CompanyDetailsController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
    val subscriptionService = SubscriptionService
  }

  val backUrl = Some(controllers.eis.routes.CompanyDetailsController.show(1).url)
  val listOfInvestorsIncompleteCompanyDetails =  Vector(validModelWithPrevShareHoldings.copy(companyDetailsModel = None))

  "CompanyDetailsController" should {
    "use the correct auth connector" in {
      CompanyDetailsController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct keystore connector" in {
      CompanyDetailsController.s4lConnector shouldBe S4LConnector
    }
    "use the correct config" in {
      CompanyDetailsController.applicationConfig shouldBe FrontendAppConfig
    }
    "use the correct enrolment connector" in {
      CompanyDetailsController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  def setupMocks(individualDetailsModels: Option[Vector[InvestorDetailsModel]], backURL: Option[String] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](Matchers.eq(KeystoreKeys.investorDetails))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(individualDetailsModels))
    when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkCompanyAndIndividualBoth))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(backURL))
  }

  "Sending a GET request to CompanyDetailsController when authenticated and enrolled" should {

    "'REDIRECT' to TBD page" when {
      "there is no 'back link' present" in {
        mockEnrolledRequest(eisSchemeTypesModel)
        setupMocks(None,None)
        showWithSessionAndAuth(TestController.show(Constants.obviouslyInvalidId))(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some(controllers.eis.routes.AddInvestorOrNomineeController.show(None).url)
          }
        )
      }
    }

    /* Invalid scenario as list must exist if INT in query string, redirect to AddNomineeOrInvestor */
    "Redirect to 'AddNomineeOrInvestor' page" when {
      "a 'backlink' is defined but no 'investor details list' is retrieved" in {
        mockEnrolledRequest(eisSchemeTypesModel)
        setupMocks(None, backUrl)
        showWithSessionAndAuth(TestController.show(Constants.obviouslyInvalidId))(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some(controllers.eis.routes.AddInvestorOrNomineeController.show(None).url)
          }
        )
      }
    }

    "Redirect to the Investor Details Review page" when {
      "a 'backlink' is defined, an 'investor details list' is retrieved and an INVALID 'id' is defined" in {
        mockEnrolledRequest(eisSchemeTypesModel)
        setupMocks(Some(listOfInvestorsComplete), backUrl)
        showWithSessionAndAuth(TestController.show(3))(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some(controllers.eis.routes.AddInvestorOrNomineeController.show(None).url)
          }
        )
      }
    }

    "return an 'OK' and load the page with a empty form" when {
      "a 'backlink' is defined, an 'investor details list' is retrieved with a defined companyDetails model at position 'id'" in {
        mockEnrolledRequest(eisSchemeTypesModel)
        setupMocks(Some(listOfInvestorsComplete), backUrl)
        showWithSessionAndAuth(TestController.show(2))(
          result => {
            status(result) shouldBe OK
          }
        )
      }
    }

    "return an 'OK' and load the page with a populated form" when {
      "a 'backlink' is defined, an 'investor details list' is retrieved with an undefined companyDetails model at position 'id'" in {
        mockEnrolledRequest(eisSchemeTypesModel)
        setupMocks(Some(listOfInvestorsIncompleteCompanyDetails), backUrl)
        showWithSessionAndAuth(TestController.show(2))(
          result => {
            status(result) shouldBe OK
          }
        )
      }
    }
  }

  "Submitting to the CompanyDetailsController when authenticated and enrolled" should {
    "redirect to the correct page if the form 'was not' previously populated" in {

      val formInput =
              Seq("companyName" -> "Line 0",
                "companyAddressline1" -> "Line 1",
                "companyAddressline2" -> "Line 2",
                "companyAddressline3" -> "Line 3",
                "companyAddressline4" -> "line 4",
                "companyPostcode" -> "AA1 1AA",
                "countryCode" -> "GB")
      setupMocks(Some(listOfInvestorsComplete), backUrl)
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit(),formInput:_*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe
            Some(controllers.eis.routes.NumberOfSharesPurchasedController.show(listOfInvestorsComplete.head.processingId.get).url)
        }
      )
    }
  }



  "Submitting to the CompanyDetailsController when authenticated and enrolled" should {
    "redirect to the correct page if the form 'was' previously populated and had a processing id" in {

      val formInput =
        Seq("companyName" -> "Line 0",
          "companyAddressline1" -> "Line 1",
          "companyAddressline2" -> "Line 2",
          "companyAddressline3" -> "Line 3",
          "companyAddressline4" -> "line 4",
          "companyPostcode" -> "AA1 1AA",
          "countryCode" -> "GB", "processingId" -> "2")
      setupMocks(Some(listOfInvestorsComplete), backUrl)
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit(),formInput:_*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe
            Some(controllers.eis.routes.NumberOfSharesPurchasedController.show(listOfInvestorsComplete.head.processingId.get).url)
        }
      )
    }
  }


  "Sending an invalid form submission with validation errors to the CompanyDetailsController when authenticated and enrolled" should {
    "respond wih a bad request" in {
      setupMocks(Some(listOfInvestorsComplete), backUrl)
      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = "companyAddressline1" -> ""
      submitWithSessionAndAuth(TestController.submit(), formInput)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }


}
