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
import controllers.helpers.BaseSpec
import models.investorDetails.InvestorDetailsModel
import org.mockito.Matchers
import org.mockito.Mockito.when
import play.api.test.Helpers.redirectLocation
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import play.api.test.Helpers._

import scala.concurrent.Future

class InvestorShareIssueDateControllerSpec extends BaseSpec {

  lazy val controller = new InvestorShareIssueDateController {
    override lazy val s4lConnector: S4LConnector = mockS4lConnector
    override lazy val enrolmentConnector: EnrolmentConnector = mockEnrolmentConnector
    override lazy val applicationConfig: AppConfig = MockConfig
    override lazy val authConnector: AuthConnector = MockAuthConnector
  }

  val backUrl = Some(controllers.seis.routes.PreviousShareHoldingNominalValueController.show(2, 1).url)
  val obviouslyInvalidId = 9999

  val listOfInvestorsEmptyShareHoldings =  Vector(validModelWithPrevShareHoldings.copy(previousShareHoldingModels = Some(Vector())))
  val listOfInvestorsWithShareHoldings =  Vector(validModelWithPrevShareHoldings)

  def setupMocks(investorDetailsModel: Option[Vector[InvestorDetailsModel]], backURL : Option[String]): Unit = {
    mockEnrolledRequest(seisSchemeTypesModel)

    when(mockS4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](Matchers.eq(KeystoreKeys.investorDetails))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(investorDetailsModel))

    when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkInvestorShareIssueDate))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(backURL))
  }

  "The Investor Share Issue Date Controller" should {

    "use the correct auth connector" in {
      InvestorShareIssueDateController.authConnector shouldBe FrontendAuthConnector
    }

    "use the correct keystore connector" in {
      InvestorShareIssueDateController.s4lConnector shouldBe S4LConnector
    }

    "use the correct enrolment connector" in {
      InvestorShareIssueDateController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  "Sending a GET request to InvestorShareIssueDate Controller when authenticated and enrolled" should {

    "'REDIRECT' to AddInvestorOrNominee page" when {
      "there is no 'back link' present" in {
        mockEnrolledRequest(seisSchemeTypesModel)
        setupMocks(None, None)
        showWithSessionAndAuth(controller.show(2, 1))(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some(routes.AddInvestorOrNomineeController.show(None).url)
          }
        )
      }
    }

    "Redirect to 'AddNomineeOrInvestor' page" when {
      "a 'backlink' is defined but no 'investor details list' is retrieved" in {
        mockEnrolledRequest(seisSchemeTypesModel)
        setupMocks(None, backUrl)
        showWithSessionAndAuth(controller.show(2, 1))(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some(controllers.seis.routes.AddInvestorOrNomineeController.show(None).url)
          }
        )
      }
    }

    "Redirect to the AddInvestorOrNominee page" when {
      "a 'backlink' is defined, an 'investor details list' is retrieved and an INVALID investor details ID is passed" in {
        mockEnrolledRequest(seisSchemeTypesModel)
        setupMocks(Some(listOfInvestorsComplete), backUrl)
        showWithSessionAndAuth(controller.show(obviouslyInvalidId, 1))(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some(controllers.seis.routes.AddInvestorOrNomineeController.show(None).url)
          }
        )
      }
    }

    "Load an empty Investor Share Issue Date page" when {
      "a 'backlink' is defined, an 'investor details list' is retrieved, a VALID investor details " +
        "ID is passed and the investor details contains an empty list of share holdings" in {
        mockEnrolledRequest(seisSchemeTypesModel)
        setupMocks(Some(listOfInvestorsEmptyShareHoldings), backUrl)
        showWithSessionAndAuth(controller.show(2, 2))(
          result => {
            status(result) shouldBe SEE_OTHER
          }
        )
      }
    }

    "Load an empty InvestorShareIssueDate page" when {
      "a 'backlink' is defined, an 'investor details list' is retrieved, a VALID investor details " +
        "ID is defined but no share holding Id is provided" in {
        mockEnrolledRequest(seisSchemeTypesModel)
        setupMocks(Some(listOfInvestorsWithShareHoldings), backUrl)
        showWithSessionAndAuth(controller.show(2, 1))(
          result => {
            status(result) shouldBe OK
          }
        )
      }
    }

    "Load an empty InvestorShareIssueDate page" when {
      "a 'backlink' is defined, an 'investor details list' is retrieved, a VALID investor details " +
        "ID is passed and an INVALID share holding Id is provided" in {
        mockEnrolledRequest(seisSchemeTypesModel)
        setupMocks(Some(listOfInvestorsWithShareHoldings), backUrl)
        showWithSessionAndAuth(controller.show(2, obviouslyInvalidId))(
          result => {
            status(result) shouldBe SEE_OTHER
          }
        )
      }
    }


    "Load a populated InvestorShareIssueDate page" when {
      "a 'backlink' is defined, an 'investor details list' is retrieved, a VALID investor details " +
        "ID is defined and a VALID share holding Id is provided" in {
        mockEnrolledRequest(seisSchemeTypesModel)
        setupMocks(Some(listOfInvestorsWithShareHoldings), backUrl)
        showWithSessionAndAuth(controller.show(2, 1))(
          result => {
            status(result) shouldBe OK
          }
        )
      }
    }

  }

  "Submitting to the InvestorShareIssueDateController when authenticated and enrolled" should {
    "redirect to the NumberOfPreviouslyIssuedShares page if the form 'was not' previously populated" in {

      val formInput = Seq("investorShareIssueDateDay" -> "23",
        "investorShareIssueDateMonth" -> "11",
        "investorShareIssueDateYear" -> "1993")
      setupMocks(Some(listOfInvestorsComplete), backUrl)
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(controller.submit(backUrl, Some(2)), formInput:_*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe
            Some(controllers.seis.routes.NumberOfPreviouslyIssuedSharesController.show(listOfInvestorsComplete.head.processingId.get,
              listOfInvestorsComplete.head.previousShareHoldingModels.get.head.processingId.get).url)
        }
      )
    }
  }



  "Submitting to the PreviousShareHoldingNominalValueController when authenticated and enrolled" should {
    "redirect to the NumberOfPreviouslyIssuedShares page if the form 'was' previously populated and had a processing id" in {

      val formInput = Seq("investorShareIssueDateDay" -> "23",
        "investorShareIssueDateMonth" -> "11",
        "investorShareIssueDateYear" -> "1993",
        "processingId" -> "1", "investorProcessingId" -> "2")
      setupMocks(Some(listOfInvestorsComplete), backUrl)
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(controller.submit(backUrl, Some(2)),formInput:_*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe
            Some(controllers.seis.routes.NumberOfPreviouslyIssuedSharesController.show(listOfInvestorsComplete.head.processingId.get,
              listOfInvestorsComplete.head.previousShareHoldingModels.get.head.processingId.get).url)
        }
      )
    }
  }


  "Sending an invalid form submission with validation errors to the InvestorShareIssueDateController" +
    " when authenticated and enrolled" should {
    "redirect to itself" in {
      setupMocks(Some(listOfInvestorsComplete), None)
      mockEnrolledRequest(seisSchemeTypesModel)
      val formInput = "previousShareHoldingNominalValue" -> ""
      submitWithSessionAndAuth(controller.submit(backUrl, Some(2)), formInput)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }
}