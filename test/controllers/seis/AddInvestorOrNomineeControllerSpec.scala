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
import models._
import models.investorDetails.InvestorDetailsModel
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
    "use the correct config" in {
      AddInvestorOrNomineeController.applicationConfig shouldBe FrontendAppConfig
    }
    "use the correct enrolment connector" in {
      AddInvestorOrNomineeController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  def setupMocks(individualDetailsModels: Option[Vector[InvestorDetailsModel]], backUrl: Option[String]): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](Matchers.eq(KeystoreKeys.investorDetails))
      (Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(individualDetailsModels))
    when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkAddInvestorOrNominee))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(backUrl))
  }

  "Sending a GET request to AddInvestorOrNomineeController when authenticated and enrolled" should {

    "'REDIRECT' to the 'ShareDescription' page" when {
      "there is no 'back link' present" in {
        mockEnrolledRequest(seisSchemeTypesModel)
        setupMocks(None,None)
        showWithSessionAndAuth(TestController.show(None))(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some(controllers.seis.routes.ShareDescriptionController.show().url)
          }
        )
      }
    }

    "return an 'OK' and load the page with an empty form" when {
      "a 'backlink' is defined but no 'investor details list' is retrieved" in {
        mockEnrolledRequest(seisSchemeTypesModel)
        setupMocks(None, Some(validBackLink))
        showWithSessionAndAuth(TestController.show(None))(
          result => {
            status(result) shouldBe OK
          }
        )
      }
    }

    "return an 'OK' and load the page with a populated form" when {
      "a 'backlink' is defined, an 'investor details list' is retrieved and VALID 'id' is defined" in {
        mockEnrolledRequest(seisSchemeTypesModel)
        setupMocks(Some(listOfInvestorsComplete), Some(validBackLink))
        showWithSessionAndAuth(TestController.show(Some(2)))(
          result => {
            status(result) shouldBe OK
          }
        )
      }
    }

    /* TODO redirect to review investor details page when the id does not exist  */
    "Redirect to the Investor Details Review page" when {
      "a 'backlink' is defined, an 'investor details list' is retrieved and an INVALID 'id' is defined" in {
        mockEnrolledRequest(seisSchemeTypesModel)
        setupMocks(Some(listOfInvestorsComplete), Some(validBackLink))
        showWithSessionAndAuth(TestController.show(Some(3)))(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some(routes.AddInvestorOrNomineeController.show().url)
          }
        )
      }
    }

    "return an 'OK' and load the page with a empty form" when {
      "a 'backlink' is defined, an 'investor details list' is retrieved with all 'COMPLETE' investor details and no 'ID' is passed" in {
        mockEnrolledRequest(seisSchemeTypesModel)
        setupMocks(Some(listOfInvestorsComplete), Some(validBackLink))
        showWithSessionAndAuth(TestController.show(None))(
          result => {
            status(result) shouldBe OK
          }
        )
      }
    }

    "return an 'OK' and load the page with a populated form" when {
      "a 'backlink' is defined, an 'investor details list' is retrieved with an 'INCOMPLETE' investor detail and no 'ID' is passed" in {
        mockEnrolledRequest(seisSchemeTypesModel)
        setupMocks(Some(listOfInvestorsIncomplete), Some(validBackLink))
        showWithSessionAndAuth(TestController.show(None))(
          result => {
            status(result) shouldBe OK
          }
        )
      }
    }

  }


  "By selecting the investor submission to the AddInvestorOrNomineeController when authenticated and enrolled" should {
    "redirect to the correct page if an investor and the form 'was not' previously populated" in {

      val formInput = "addInvestorOrNominee" -> Constants.investor
      setupMocks(Some(listOfInvestorsComplete), Some(validBackLink))
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe
            Some(controllers.seis.routes.CompanyOrIndividualController.show(listOfInvestorsComplete.head.processingId.get + 1).url)
        }
      )
    }
  }

  "By selecting the investor submission to the AddInvestorOrNomineeController when authenticated and enrolled" should {
    "redirect to the CompanyOrIndividual page if a nominee and the form 'was not' previously populated" in {

      val formInput = "addInvestorOrNominee" -> Constants.investor
      setupMocks(Some(listOfInvestorsComplete), Some(validBackLink))
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit, formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe
            Some(controllers.seis.routes.CompanyOrIndividualController.show(listOfInvestorsComplete.head.processingId.get + 1).url)
        }
      )
    }
  }

  "By selecting the investor submission to the AddInvestorOrNomineeController when authenticated and enrolled" should {
    "redirect to the correct page if an investor and the form 'was' previously populated and had a processing id" in {

      val formInput = Seq("addInvestorOrNominee" -> Constants.investor, "processingId" -> "2")
      setupMocks(Some(listOfInvestorsComplete), Some(validBackLink))
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,formInput:_*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe
            Some(controllers.seis.routes.CompanyOrIndividualController.show(listOfInvestorsComplete.head.processingId.get).url)
        }
      )
    }
  }

  "By selecting the investor submission to the AddInvestorOrNomineeController when authenticated and enrolled" should {
    "redirect to the correct page if a nominee and the form 'was' previously populated and had a processing id" in {
      when(TestController.s4lConnector.saveFormData[Vector[IndividualDetailsModel]](Matchers.eq(KeystoreKeys.addInvestor), Matchers.any())
        (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(CacheMap("",Map()))

      val formInput = Seq("addInvestorOrNominee" -> Constants.nominee, "processingId" -> "2")
      setupMocks(Some(listOfInvestorsComplete), Some(validBackLink))
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,formInput:_*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe
            Some(controllers.seis.routes.CompanyOrIndividualController.show(listOfInvestorsComplete.head.processingId.get).url)
        }
      )
    }
  }


  "Sending an invalid form submission with validation errors to the AddInvestorOrNomineeController when authenticated and enrolled" should {
    "respond wih a bad request" in {
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
