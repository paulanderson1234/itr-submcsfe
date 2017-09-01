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
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.BaseSpec
import models.investorDetails.InvestorDetailsModel
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers.{redirectLocation, _}

import scala.concurrent.Future

class CompanyOrIndividualControllerSpec extends BaseSpec {

  lazy val validBackLink = controllers.seis.routes.AddInvestorOrNomineeController.show().url

  val listOfInvestorsIncompleteCompanyOrIndividual =  Vector(validModelWithPrevShareHoldings.copy(companyOrIndividualModel = None))

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
    "use the correct config" in {
      CompanyOrIndividualController.applicationConfig shouldBe FrontendAppConfig
    }
    "use the correct enrolment connector" in {
      CompanyOrIndividualController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  def setupMocks(individualDetailsModels: Option[Vector[InvestorDetailsModel]], backUrl: Option[String]): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](Matchers.eq(KeystoreKeys.investorDetails))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(individualDetailsModels))
    when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkCompanyOrIndividual))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(backUrl))
  }

  "Sending a GET request to CompanyOrIndividualController when authenticated and enrolled" should {

    "'REDIRECT' to TBD page" when {
      "there is no 'back link' present" in {
        mockEnrolledRequest(seisSchemeTypesModel)
        setupMocks(None,None)
        showWithSessionAndAuth(TestController.show(Constants.obviouslyInvalidId))(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some(routes.AddInvestorOrNomineeController.show().url)
          }
        )
      }
    }

    /* Invalid scenario as list must exist if INT in query string, redirect to AddNomineeOrInvestor */
    "Redirect to 'AddNomineeOrInvestor' page" when {
      "a 'backlink' is defined but no 'investor details list' is retrieved" in {
        mockEnrolledRequest(seisSchemeTypesModel)
        setupMocks(None, Some(validBackLink))
        showWithSessionAndAuth(TestController.show(Constants.obviouslyInvalidId))(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some(controllers.seis.routes.AddInvestorOrNomineeController.show(None).url)
          }
        )
      }
    }

    "Redirect to the Investor Details Review page" when {
      "a 'backlink' is defined, an 'investor details list' is retrieved and an INVALID 'id' is defined" in {
        mockEnrolledRequest(seisSchemeTypesModel)
        setupMocks(Some(listOfInvestorsComplete), Some(validBackLink))
        showWithSessionAndAuth(TestController.show(3))(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some(validBackLink)
          }
        )
      }
    }

    "return an 'OK' and load the page with a empty form" when {
      "a 'backlink' is defined, an 'investor details list' is retrieved with a defined companyOrIndividual model at position 'id'" in {
        mockEnrolledRequest(seisSchemeTypesModel)
        setupMocks(Some(listOfInvestorsComplete), Some(validBackLink))
        showWithSessionAndAuth(TestController.show(2))(
          result => {
            status(result) shouldBe OK
          }
        )
      }
    }

    "return an 'OK' and load the page with a populated form" when {
      "a 'backlink' is defined, an 'investor details list' is retrieved with an undefined companyOrIndividual model at position 'id'" in {
        mockEnrolledRequest(seisSchemeTypesModel)
        setupMocks(Some(listOfInvestorsIncompleteCompanyOrIndividual), Some(validBackLink))
        showWithSessionAndAuth(TestController.show(2))(
          result => {
            status(result) shouldBe OK
          }
        )
      }
    }

  }


  "By selecting the company submission to the CompanyOrIndividualController when authenticated and enrolled" should {
    "redirect to the correct page if a company and the form 'was not' previously populated" in {

      val formInput = "companyOrIndividual" -> Constants.typeCompany
      setupMocks(Some(listOfInvestorsComplete), Some(validBackLink))
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit(Some("Investor")),formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe
            Some(controllers.seis.routes.CompanyDetailsController.show(listOfInvestorsComplete.head.processingId.get).url)
        }
      )
    }
  }

  "By selecting the individual submission to the CompanyOrIndividualController when authenticated and enrolled" should {
    "redirect to the CompanyOrIndividual page if a nominee and the form 'was not' previously populated" in {

      val formInput = "companyOrIndividual" -> Constants.typeIndividual
      setupMocks(Some(listOfInvestorsComplete), Some(validBackLink))
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit(Some("Investor")), formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe
            Some(controllers.seis.routes.IndividualDetailsController.show(listOfInvestorsComplete.head.processingId.get).url)
        }
      )
    }
  }

  "By selecting the company submission to the CompanyOrIndividualController when authenticated and enrolled" should {
    "redirect to the correct page if a company and the form 'was' previously populated and had a processing id" in {

      val formInput = Seq("companyOrIndividual" -> Constants.typeCompany, "processingId" -> "2")
      setupMocks(Some(listOfInvestorsComplete), Some(validBackLink))
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit(Some("Investor")),formInput:_*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe
            Some(controllers.seis.routes.CompanyDetailsController.show(listOfInvestorsComplete.head.processingId.get).url)
        }
      )
    }
  }

  "By selecting the individual submission to the CompanyOrIndividualController when authenticated and enrolled" should {
    "redirect to the correct page if a individual and the form 'was' previously populated and had a processing id" in {

      val formInput = Seq("companyOrIndividual" -> Constants.typeIndividual, "processingId" -> "2")
      setupMocks(Some(listOfInvestorsComplete), Some(validBackLink))
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit(Some("Investor")),formInput:_*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe
            Some(controllers.seis.routes.IndividualDetailsController.show(listOfInvestorsComplete.head.processingId.get).url)
        }
      )
    }
  }


  "Sending an invalid form submission with validation errors to the CompanyOrIndividualController when authenticated and enrolled" should {
    "respond wih a bad request" in {
      setupMocks(Some(listOfInvestorsComplete), Some(validBackLink))
      mockEnrolledRequest(seisSchemeTypesModel)
      val formInput = "companyOrIndividual" -> ""
      submitWithSessionAndAuth(TestController.submit(Some("Investor")), formInput)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }

}
