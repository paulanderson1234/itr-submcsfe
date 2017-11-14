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
import models.ShareIssueDateModel
import models.investorDetails.InvestorDetailsModel
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import utils.DateFormatter

import scala.concurrent.Future


class NumberOfSharesPurchasedControllerSpec extends BaseSpec with DateFormatter{

  lazy val controller = new NumberOfSharesPurchasedController {
    override lazy val s4lConnector: S4LConnector = mockS4lConnector
    override lazy val enrolmentConnector: EnrolmentConnector = mockEnrolmentConnector
    override lazy val applicationConfig: AppConfig = MockConfig
    override lazy val authConnector: AuthConnector = MockAuthConnector
  }

  val backUrl = Some(controllers.eis.routes.CompanyDetailsController.show(1).url)
  val listOfInvestorsIncompleteNumberOfSharesPurchased =  Vector(validModelWithPrevShareHoldings.copy(numberOfSharesPurchasedModel = None))
  val shareIssueDate = Some(dateToStringWithNoZeroDay(shareIssuetDateModel.day.get, shareIssuetDateModel.month.get, shareIssuetDateModel.year.get))

  def setupMocks(investorDetailsModel: Option[Vector[InvestorDetailsModel]], shareIssueDateModel: Option[ShareIssueDateModel],
                 backURL : Option[String]): Unit = {
    mockEnrolledRequest(eisSchemeTypesModel)

    when(mockS4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](Matchers.eq(KeystoreKeys.investorDetails))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(investorDetailsModel))

    when(mockS4lConnector.fetchAndGetFormData[ShareIssueDateModel](Matchers.eq(KeystoreKeys.shareIssueDate))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(shareIssueDateModel))

    when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkNumberOfSharesPurchased))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(backURL))
  }

  "The Number of Shares Purchased controller" should {

    "use the correct auth connector" in {
      NumberOfSharesPurchasedController.authConnector shouldBe FrontendAuthConnector
    }

    "use the correct keystore connector" in {
      NumberOfSharesPurchasedController.s4lConnector shouldBe S4LConnector
    }

    "use the correct config" in {
      NumberOfSharesPurchasedController.applicationConfig shouldBe FrontendAppConfig
    }

    "use the correct enrolment connector" in {
      NumberOfSharesPurchasedController.enrolmentConnector shouldBe EnrolmentConnector
    }

    "Sending a GET request to NumberOfSharesPurchased Controller when authenticated and enrolled" should {

      "'REDIRECT' to AddInvestorOrNominee page" when {
        "there is no 'back link' present" in {
          mockEnrolledRequest(eisSchemeTypesModel)
          setupMocks(None,None, None)
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
          setupMocks(None, None, backUrl)
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
          setupMocks(Some(listOfInvestorsComplete), None, backUrl)
          showWithSessionAndAuth(controller.show(Constants.obviouslyInvalidId))(
            result => {
              status(result) shouldBe SEE_OTHER
              redirectLocation(result) shouldBe Some(controllers.eis.routes.AddInvestorOrNomineeController.show(None).url)
            }
          )
        }
      }

      "redirect to the 'ShareIssueDate' page" when {
        "a 'backlink' is defined, an 'investor details list' is retrieved but a 'share issue date' is not provided" in {
          mockEnrolledRequest(eisSchemeTypesModel)
          setupMocks(Some(listOfInvestorsComplete), None,  backUrl)
          showWithSessionAndAuth(controller.show(2))(
            result => {
              status(result) shouldBe SEE_OTHER
              redirectLocation(result) shouldBe Some(controllers.eis.routes.ShareIssueDateController.show().url)
            }
          )
        }
      }

      "return an 'OK' and load the page with a empty form" when {
        "a 'backlink' is defined, an 'investor details list' is retrieved, a 'share issue date' is retrieved with " +
          "an defined 'NumberOfSharesPurchased' model at position 'id'" in {
          mockEnrolledRequest(eisSchemeTypesModel)
          setupMocks(Some(listOfInvestorsComplete), Some(shareIssuetDateModel),  backUrl)
          showWithSessionAndAuth(controller.show(2))(
            result => {
              status(result) shouldBe OK
            }
          )
        }
      }

      "return an 'OK' and load the page with a populated form" when {
        "a 'backlink' is defined, an 'investor details list' is retrieved, a 'share issue date' is retrieved with " +
          "an undefined 'NumberOfSharesPurchased' model at position 'id'" in {
          mockEnrolledRequest(eisSchemeTypesModel)
          setupMocks(Some(listOfInvestorsIncompleteNumberOfSharesPurchased), Some(shareIssuetDateModel), backUrl)
          showWithSessionAndAuth(controller.show(2))(
            result => {
              status(result) shouldBe OK
            }
          )
        }
      }
    }

    "Submitting to the NumberOfSharesPurchasedController when authenticated and enrolled" should {
      "redirect to the correct page if the form 'was not' previously populated" in {

        val formInput = "numberOfSharesPurchased" -> "10000000"
        setupMocks(Some(listOfInvestorsComplete), None, None)
        mockEnrolledRequest(eisSchemeTypesModel)
        submitWithSessionAndAuth(controller.submit(shareIssueDate),formInput)(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe
              Some(controllers.eis.routes.HowMuchSpentOnSharesController.show(listOfInvestorsComplete.head.processingId.get).url)
          }
        )
      }
    }



    "Submitting to the NumberOfSharesPurchasedController when authenticated and enrolled" should {
      "redirect to the correct page if the form 'was' previously populated and had a processing id" in {

        val formInput = Seq("numberOfSharesPurchased" -> "10000000", "processingId" -> "2")
        setupMocks(Some(listOfInvestorsComplete), None, None)
        mockEnrolledRequest(eisSchemeTypesModel)
        submitWithSessionAndAuth(controller.submit(shareIssueDate),formInput:_*)(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe
              Some(controllers.eis.routes.HowMuchSpentOnSharesController.show(listOfInvestorsComplete.head.processingId.get).url)
          }
        )
      }
    }


    "Sending an invalid form submission with validation errors to the NumberOfSharesPurchasedController when authenticated and enrolled" should {
      "redirect to itself" in {
        setupMocks(Some(listOfInvestorsComplete), None, backUrl)
        mockEnrolledRequest(eisSchemeTypesModel)
        val formInput = "numberOfSharesPurchased" -> ""
        submitWithSessionAndAuth(controller.submit(shareIssueDate), formInput)(
          result => {
            status(result) shouldBe BAD_REQUEST
          }
        )
      }
    }

  }
}
