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
import config.{AppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.{BaseSpec, FakeRequestHelper}
import models.investorDetails.InvestorDetailsModel
import models.{IndividualDetailsModel, NominalValueOfSharesModel}
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future

class IndividualDetailsControllerSpec extends BaseSpec with FakeRequestHelper{

    lazy val TestController = new IndividualDetailsController{
    override lazy val s4lConnector: S4LConnector = mockS4lConnector
    override lazy val enrolmentConnector: EnrolmentConnector = mockEnrolmentConnector
    override lazy val applicationConfig: AppConfig = MockConfig
    override lazy val authConnector: AuthConnector = MockAuthConnector
  }

  val backUrl = Some(controllers.seis.routes.CompanyOrIndividualController.show(1).url)
  val listOfInvestorsCompleteIndividualDetails = Vector(validModelWithPrevShareHoldings.copy(companyDetailsModel = None,
    individualDetailsModel = Some(individualDetailsModel)))
  val listOfInvestorsIncompleteIndividualDetails = Vector(validModelWithPrevShareHoldings.copy(individualDetailsModel = None))

  def setupMocks(individualDetailsModels: Option[Vector[InvestorDetailsModel]], backURL: Option[String]): Unit = {
    mockEnrolledRequest(seisSchemeTypesModel)
    when(mockS4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](Matchers.eq(KeystoreKeys.investorDetails))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(individualDetailsModels))

    when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(mock[CacheMap]))
    when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkCompanyAndIndividualBoth))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(backURL))
  }

  "The IndividualDetails controller" should {

    "use the correct auth connector" in {
      IndividualDetailsController.authConnector shouldBe FrontendAuthConnector
    }

    "use the correct keystore connector" in {
      IndividualDetailsController.s4lConnector shouldBe S4LConnector
    }

    "use the correct enrolment connector" in {
      IndividualDetailsController.enrolmentConnector shouldBe EnrolmentConnector
    }

    "Sending a GET request to IndividualDEtails Controller when authenticated and enrolled" should {

      "'REDIRECT' to TBD page" when {
        "there is no 'back link' present" in {
          mockEnrolledRequest(seisSchemeTypesModel)
          setupMocks(None, None)
          showWithSessionAndAuth(TestController.show(Constants.obviouslyInvalidId))(
            result => {
              status(result) shouldBe SEE_OTHER
              redirectLocation(result) shouldBe Some(controllers.seis.routes.AddInvestorOrNomineeController.show().url)
            }
          )
        }
      }

      /* Invalid scenario as list must exist if INT in query string, redirect to AddNomineeOrInvestor */
      "Redirect to 'AddNomineeOrInvestor' page" when {
        "a 'backlink' is defined but no 'investor details list' is retrieved" in {
          mockEnrolledRequest(seisSchemeTypesModel)
          setupMocks(None, backUrl)
          showWithSessionAndAuth(TestController.show(Constants.obviouslyInvalidId))(
            result => {
              status(result) shouldBe SEE_OTHER
              redirectLocation(result) shouldBe Some(controllers.seis.routes.AddInvestorOrNomineeController.show(None).url)
            }
          )
        }
      }

      /* TODO redirect to review investor details page when the id does not exist  */
      "Redirect to the Investor Details Review page" when {
        "a 'backlink' is defined, an 'investor details list' is retrieved and an INVALID 'id' is defined" in {
          mockEnrolledRequest(seisSchemeTypesModel)
          setupMocks(Some(listOfInvestorsComplete), backUrl)
          showWithSessionAndAuth(TestController.show(3))(
            result => {
              status(result) shouldBe SEE_OTHER
              redirectLocation(result) shouldBe Some(controllers.seis.routes.AddInvestorOrNomineeController.show().url)
            }
          )
        }
      }

      "return an 'OK' and load the page with a empty form" when {
        "a 'backlink' is defined, an 'investor details list' is retrieved with a defined individualDetails model at position 'id'" in {
          mockEnrolledRequest(seisSchemeTypesModel)
          setupMocks(Some(listOfInvestorsCompleteIndividualDetails), backUrl)
          showWithSessionAndAuth(TestController.show(2))(
            result => {
              status(result) shouldBe OK
            }
          )
        }
      }

      "return an 'OK' and load the page with a populated form" when {
        "a 'backlink' is defined, an 'investor details list' is retrieved with an undefined individualDetails model at position 'id'" in {
          mockEnrolledRequest(seisSchemeTypesModel)
          setupMocks(Some(listOfInvestorsIncompleteIndividualDetails), backUrl)
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

              val formInput = Seq(
                "forename" -> "TEST",
                "surname" -> "TESTING",
                "addressline1" -> "Line 1",
                "addressline2" -> "Line 2",
                "addressline3" -> "Line 3",
                "addressline4" -> "line 4",
                "postcode" -> "AA1 1AA",
                "countryCode" -> "GB")
        setupMocks(Some(listOfInvestorsComplete), backUrl)
        mockEnrolledRequest(seisSchemeTypesModel)
        submitWithSessionAndAuth(TestController.submit(Some(routes.CompanyOrIndividualController.show(2).url)), formInput: _*)(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe
              Some(controllers.seis.routes.NumberOfSharesPurchasedController.show(listOfInvestorsComplete.head.processingId.get).url)
          }
        )
      }
    }



    "Submitting to the CompanyDetailsController when authenticated and enrolled" should {
      "redirect to the correct page and the form 'was' previously populated and had a processing id" in {

              val formInput = Seq(
                "forename" -> "TEST",
                "surname" -> "TESTING",
                "addressline1" -> "Line 1",
                "addressline2" -> "Line 2",
                "addressline3" -> "Line 3",
                "addressline4" -> "line 4",
                "postcode" -> "AA1 1AA",
                "countryCode" -> "GB",
                "processingId" -> "2")
        setupMocks(Some(listOfInvestorsComplete), backUrl)
        mockEnrolledRequest(seisSchemeTypesModel)
        submitWithSessionAndAuth(TestController.submit(Some(routes.CompanyOrIndividualController.show(2).url)), formInput: _*)(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe
              Some(controllers.seis.routes.NumberOfSharesPurchasedController.show(listOfInvestorsComplete.head.processingId.get).url)
          }
        )
      }
    }


    "Sending an invalid form submission with validation errors to the CompanyDetailsController when authenticated and enrolled" should {
      "redirect to itself" in {
        setupMocks(Some(listOfInvestorsComplete), backUrl)
        mockEnrolledRequest(seisSchemeTypesModel)
        val formInput = Seq(
                  "forename" -> "",
                  "surname" -> "",
                  "addressline1" -> "Line 1",
                  "addressline2" -> "Line 2",
                  "addressline3" -> "Line 3",
                  "addressline4" -> "line 4",
                  "postcode" -> "",
                  "countryCode" -> "GB")
        submitWithSessionAndAuth(TestController.submit(Some(routes.CompanyOrIndividualController.show(2).url)), formInput:_*)(
          result => {
            status(result) shouldBe BAD_REQUEST
          }
        )
      }
    }
  }
}
