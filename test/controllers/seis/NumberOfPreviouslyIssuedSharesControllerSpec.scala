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
import models.{CompanyOrIndividualModel, ShareIssueDateModel}
import models.investorDetails.{PreviousShareHoldingModel, InvestorDetailsModel}
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import utils.DateFormatter

import scala.concurrent.Future


class NumberOfPreviouslyIssuedSharesControllerSpec extends BaseSpec{

  lazy val controller = new NumberOfPreviouslyIssuedSharesController {
    override lazy val s4lConnector: S4LConnector = mockS4lConnector
    override lazy val enrolmentConnector: EnrolmentConnector = mockEnrolmentConnector
    override lazy val applicationConfig: AppConfig = MockConfig
    override lazy val authConnector: AuthConnector = MockAuthConnector
  }

  val backUrl = Some(controllers.seis.routes.InvestorShareIssueDateController.show(2, 1).url)

  val listOfInvestorsEmptyShareHoldings =  Vector(validModelWithPrevShareHoldings.copy(previousShareHoldingModels = Some(Vector())))
  val listOfInvestorsWithShareHoldings =  Vector(validModelWithPrevShareHoldings)
  val listOfInvestorsMissingNumberOfPreviouslyIssuedShares =  Vector(validModelWithPrevShareHoldings.copy(previousShareHoldingModels =
    Some(Vector(PreviousShareHoldingModel(previousShareHoldingDescriptionModel = Some(previousShareHoldingDescriptionModel1), processingId = Some(1))))))

  def setupMocks(investorDetailsModel: Option[Vector[InvestorDetailsModel]], backURL : Option[String]): Unit = {
    mockEnrolledRequest(seisSchemeTypesModel)

    when(mockS4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](Matchers.eq(KeystoreKeys.investorDetails))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(investorDetailsModel))

    when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkNumberOfPreviouslyIssuedShares))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(backURL))
  }

  "The Number Of Previously Issued Shares controller" should {

    "use the correct auth connector" in {
      NumberOfPreviouslyIssuedSharesController.authConnector shouldBe FrontendAuthConnector
    }

    "use the correct keystore connector" in {
      NumberOfPreviouslyIssuedSharesController.s4lConnector shouldBe S4LConnector
    }

    "use the correct enrolment connector" in {
      NumberOfPreviouslyIssuedSharesController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  "Sending a GET request to NumberOfPreviouslyIssuedShares Controller when authenticated and enrolled" should {

    "Redirect to AddInvestorOrNominee page" when {
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


      "a 'backlink' is defined, an 'investor details list' is retrieved and an INVALID investor details ID is passed" in {
        mockEnrolledRequest(seisSchemeTypesModel)
        setupMocks(Some(listOfInvestorsComplete), backUrl)
        showWithSessionAndAuth(controller.show(Constants.obviouslyInvalidId, 1))(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some(controllers.seis.routes.AddInvestorOrNomineeController.show(None).url)
          }
        )
      }


      "a 'backlink' is defined, an 'investor details list' is retrieved, a VALID investor details " +
        "ID is passed and the investor details contains an empty list of share holdings" in {
        mockEnrolledRequest(seisSchemeTypesModel)
        setupMocks(Some(listOfInvestorsEmptyShareHoldings), backUrl)
        showWithSessionAndAuth(controller.show(2, 1))(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some(controllers.seis.routes.AddInvestorOrNomineeController.show(Some(2)).url)
          }
        )
      }

      "a 'backlink' is defined, an 'investor details list' is retrieved, a VALID investor details " +
        "ID is passed and an INVALID share holding Id is provided" in {
        mockEnrolledRequest(seisSchemeTypesModel)
        setupMocks(Some(listOfInvestorsWithShareHoldings), backUrl)
        showWithSessionAndAuth(controller.show(2, Constants.obviouslyInvalidId))(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some(controllers.seis.routes.AddInvestorOrNomineeController.show(Some(2)).url)
          }
        )
      }
    }


    "Load a populated ShareHoldingDescription page" when {
      "a 'backlink' is defined, an 'investor details list' is retrieved, a VALID investor details " +
        "ID is defined and a VALID share holding Id is provided and the page model does not exist" in {
        mockEnrolledRequest(seisSchemeTypesModel)
        setupMocks(Some(listOfInvestorsMissingNumberOfPreviouslyIssuedShares), backUrl)
        showWithSessionAndAuth(controller.show(2, 1))(
          result => {
            status(result) shouldBe OK
          }
        )
      }
    }

    "Load a populated ShareHoldingDescription page" when {
      "a 'backlink' is defined, an 'investor details list' is retrieved, a VALID investor details " +
        "ID is defined and a VALID share holding Id is provided and the page model exists" in {
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


  /**TODO Redirect to correct location when page created **/
  "Submitting to the NumberOfPreviouslyIssuedSharesController when authenticated and enrolled" should {
    "redirect to the NumberOfPreviouslyIssuedShares page if the form 'was not' previously populated" in {

      val formInput = "numberOfPreviouslyIssuedShares" -> "100000000"
      setupMocks(Some(listOfInvestorsComplete), None)
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(controller.submit(Some(Constants.typeCompany),backUrl),formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe
            Some(controllers.seis.routes.PreviousShareHoldingsReviewController.show(listOfInvestorsComplete.head.processingId.get,
              Some(listOfInvestorsComplete.head.previousShareHoldingModels.get.head.processingId.get)).url)
        }
      )
    }
  }



  "Submitting to the NumberOfPreviouslyIssuedSharesController when authenticated and enrolled" should {
    "redirect to the NumberOfPreviouslyIssuedShares page if the form 'was' previously populated and had a processing id" in {

      val formInput = Seq("numberOfPreviouslyIssuedShares" -> "1000000000",
        "processingId" -> "1", "investorProcessingId" -> "2")
      setupMocks(Some(listOfInvestorsComplete), None)
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(controller.submit(Some(Constants.typeCompany), backUrl),formInput:_*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe
            Some(controllers.seis.routes.PreviousShareHoldingsReviewController.show(listOfInvestorsComplete.head.processingId.get,
              Some(listOfInvestorsComplete.head.previousShareHoldingModels.get.head.processingId.get)).url)
        }
      )
    }
  }


  "Sending an invalid form submission with validation errors to the NumberOfPreviouslyIssuedSharesController when authenticated and enrolled" should {
    "redirect to itself" in {
      setupMocks(Some(listOfInvestorsComplete), None)
      mockEnrolledRequest(seisSchemeTypesModel)
      val formInput = "numberOfPreviouslyIssuedShares" -> ""
      submitWithSessionAndAuth(controller.submit(Some(Constants.typeCompany),backUrl), formInput)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }
}