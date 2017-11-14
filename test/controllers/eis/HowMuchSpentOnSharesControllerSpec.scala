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
import config.{AppConfig, FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.BaseSpec
import models.investorDetails._
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future

class HowMuchSpentOnSharesControllerSpec extends BaseSpec  {

  lazy val controller = new HowMuchSpentOnSharesController {
    override lazy val s4lConnector: S4LConnector = mockS4lConnector
    override lazy val enrolmentConnector: EnrolmentConnector = mockEnrolmentConnector
    override lazy val applicationConfig: AppConfig = MockConfig
    override lazy val authConnector: AuthConnector = MockAuthConnector
  }

  lazy val backUrl = Some(controllers.eis.routes.NumberOfSharesPurchasedController.show(1).url)
  lazy val listOfInvestorsIncompleteHowMuchSpentOnShares =  Vector(validModelWithPrevShareHoldings.copy(amountSpentModel = None))

  def setupMocks(investorDetailsModel: Option[Vector[InvestorDetailsModel]], backURL : Option[String]): Unit = {
    mockEnrolledRequest(eisSchemeTypesModel)

    when(mockS4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](Matchers.eq(KeystoreKeys.investorDetails))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(investorDetailsModel))


    when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkHowMuchSpentOnShares))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(backURL))
  }

  "The HowMuchSpentOnShares controller" should {

    "use the correct auth connector" in {
      HowMuchSpentOnSharesController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct config" in {
      HowMuchSpentOnSharesController.applicationConfig shouldBe FrontendAppConfig
    }

    "use the correct keystore connector" in {
      HowMuchSpentOnSharesController.s4lConnector shouldBe S4LConnector
    }

    "use the correct enrolment connector" in {
      HowMuchSpentOnSharesController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

    "Sending a GET request to 'HowMuchSpentOnShares' Controller when authenticated and enrolled" should {

      "'REDIRECT' to AddInvestorOrNominee page" when {
        "there is no 'back link' present" in {
          mockEnrolledRequest(eisSchemeTypesModel)
          setupMocks(None, None)
          showWithSessionAndAuth(controller.show(1))(
            result => {
              status(result) shouldBe SEE_OTHER
              redirectLocation(result) shouldBe Some(routes.AddInvestorOrNomineeController.show(None).url)
            }
          )
        }
      }

      /* Invalid scenario as list must exist if INT in query string, redirect to AddNomineeOrInvestor */
      "Redirect to 'AddNomineeOrInvestor' page" when {
        "a 'backlink' is defined but no 'investor details list' is retrieved" in {
          mockEnrolledRequest(eisSchemeTypesModel)
          setupMocks(None, backUrl)
          showWithSessionAndAuth(controller.show(1))(
            result => {
              status(result) shouldBe SEE_OTHER
              redirectLocation(result) shouldBe Some(controllers.eis.routes.AddInvestorOrNomineeController.show(None).url)
            }
          )
        }
      }

      "Redirect to the AddInvestorOrNominee page" when {
        "a 'backlink' is defined, an 'investor details list' is retrieved and an INVALID 'id' is defined" in {
          mockEnrolledRequest(eisSchemeTypesModel)
          setupMocks(Some(listOfInvestorsComplete), backUrl)
          showWithSessionAndAuth(controller.show(Constants.obviouslyInvalidId))(
            result => {
              status(result) shouldBe SEE_OTHER
              redirectLocation(result) shouldBe Some(controllers.eis.routes.AddInvestorOrNomineeController.show(None).url)
            }
          )
        }
      }


      "return an 'OK' and load the page with a empty form" when {
        "a 'backlink' is defined, an 'investor details list' is retrieved with " +
          "an defined 'HowMuchSpentOnShares' model at position 'id'" in {
          mockEnrolledRequest(eisSchemeTypesModel)
          setupMocks(Some(listOfInvestorsComplete), backUrl)
          showWithSessionAndAuth(controller.show(2))(
            result => {
              status(result) shouldBe OK
            }
          )
        }
      }

      "return an 'OK' and load the page with a populated form" when {
        "a 'backlink' is defined, an 'investor details list' is retrieved with " +
          "an undefined 'HowMuchSpentOnShares' model at position 'id'" in {
          mockEnrolledRequest(eisSchemeTypesModel)
          setupMocks(Some(listOfInvestorsIncompleteHowMuchSpentOnShares), backUrl)
          showWithSessionAndAuth(controller.show(2))(
            result => {
              status(result) shouldBe OK
            }
          )
        }
      }

    }

    "Submitting to the HowMuchSpentOnSharesController when authenticated and enrolled" should {
      "redirect to the correct page if the form 'was not' previously populated" in {

        val formInput = "howMuchSpentOnShares" -> "10000000"
        setupMocks(Some(listOfInvestorsComplete), None)
        mockEnrolledRequest(eisSchemeTypesModel)
        submitWithSessionAndAuth(controller.submit(),formInput)(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe
              Some(controllers.eis.routes.IsExistingShareHolderController.show(listOfInvestorsComplete.head.processingId.get).url)
          }
        )
      }
    }



    "Submitting to the HowMuchSpentOnSharesController when authenticated and enrolled" should {
      "redirect to the correct page if  the form 'was' previously populated and had a processing id" in {

        val formInput = Seq("howMuchSpentOnShares" -> "10000000", "processingId" -> "2")
        setupMocks(Some(listOfInvestorsComplete), None)
        mockEnrolledRequest(eisSchemeTypesModel)
        submitWithSessionAndAuth(controller.submit(),formInput:_*)(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe
              Some(controllers.eis.routes.PreviousShareHoldingsReviewController.show(listOfInvestorsComplete.head.processingId.get).url)
          }
        )
      }
    }


    "Sending an invalid form submission with validation errors to the HowMuchSpentOnSharesController when authenticated and enrolled" should {
      "respond with a bad request" in {
        setupMocks(Some(listOfInvestorsComplete), backUrl)
        mockEnrolledRequest(eisSchemeTypesModel)
        val formInput = "howMuchSpentOnShares" -> ""
        submitWithSessionAndAuth(controller.submit(), formInput)(
          result => {
            status(result) shouldBe BAD_REQUEST
          }
        )
      }
    }

}
