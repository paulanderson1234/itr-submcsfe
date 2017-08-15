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
import models.investorDetails.{InvestorDetailsModel, PreviousShareHoldingModel}
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future


class PreviousShareHoldingsReviewControllerSpec extends BaseSpec{

  lazy val controller = new PreviousShareHoldingsReviewController {
    override lazy val s4lConnector: S4LConnector = mockS4lConnector
    override lazy val enrolmentConnector: EnrolmentConnector = mockEnrolmentConnector
    override lazy val applicationConfig: AppConfig = MockConfig
    override lazy val authConnector: AuthConnector = MockAuthConnector
  }

  val shareHoldersModelForReview = Vector(PreviousShareHoldingModel(investorShareIssueDateModel = Some(investorShareIssueDateModel1),
    numberOfPreviouslyIssuedSharesModel = Some (numberOfPreviouslyIssuedSharesModel1),
    previousShareHoldingNominalValueModel = Some(previousShareHoldingNominalValueModel1),
    previousShareHoldingDescriptionModel = Some(previousShareHoldingDescriptionModel1),
    processingId = Some(1), investorProcessingId = Some(2)))

  val investorModelForReview = InvestorDetailsModel(Some(investorModel2), Some(companyOrIndividualModel2), Some(companyDetailsModel2), None,
    Some(numberOfSharesPurchasedModel2), Some(howMuchSpentOnSharesModel2), Some(isExistingShareHolderModelYes),
    previousShareHoldingModels = Some(shareHoldersModelForReview), processingId = Some(2))

  val listOfInvestorsEmptyShareHoldings =  Vector(validModelWithPrevShareHoldings.copy(previousShareHoldingModels = Some(Vector())))
  val listOfInvestorsWithShareHoldings =  Vector(investorModelForReview)
  val listOfInvestorsMissingNumberOfPreviouslyIssuedShares =  Vector(validModelWithPrevShareHoldings.copy(previousShareHoldingModels =
    Some(Vector(PreviousShareHoldingModel(previousShareHoldingDescriptionModel = Some(previousShareHoldingDescriptionModel1), processingId = Some(1))))))

  def setupMocks(investorDetailsModel: Option[Vector[InvestorDetailsModel]]): Unit = {
    mockEnrolledRequest(seisSchemeTypesModel)

    when(mockS4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](Matchers.eq(KeystoreKeys.investorDetails))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(investorDetailsModel))

  }

  "The Number Of Previously Issued Shares controller" should {

    "use the correct auth connector" in {
      PreviousShareHoldingsReviewController.authConnector shouldBe FrontendAuthConnector
    }

    "use the correct keystore connector" in {
      PreviousShareHoldingsReviewController.s4lConnector shouldBe S4LConnector
    }

    "use the correct enrolment connector" in {
      PreviousShareHoldingsReviewController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  "Sending a GET request to PreviousShareHoldingsReview Controller when authenticated and enrolled" should {

    "Redirect to IsExistingShareHolder page" when {
      "there is no share holders present" in {
        mockEnrolledRequest(seisSchemeTypesModel)
        setupMocks(Some(onlyInvestorOrNomineeVectorList))
        showWithSessionAndAuth(controller.show(2))(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some(routes.AddInvestorOrNomineeController.show().url)
          }
        )
      }

      "no 'investor details list' is retrieved" in {
        mockEnrolledRequest(seisSchemeTypesModel)
        setupMocks(None)
        showWithSessionAndAuth(controller.show(2))(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some(controllers.seis.routes.AddInvestorOrNomineeController.show(None).url)
          }
        )
      }


      "an 'investor details list' is retrieved and an INVALID investor details ID is passed" in {
        mockEnrolledRequest(seisSchemeTypesModel)
        setupMocks(Some(listOfInvestorsComplete))
        showWithSessionAndAuth(controller.show(Constants.obviouslyInvalidId))(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some(controllers.seis.routes.AddInvestorOrNomineeController.show(None).url)
          }
        )
      }


      "an 'investor details list' is retrieved, a VALID investor details " +
        "ID is passed and the investor details contains an empty list of share holdings" in {
        mockEnrolledRequest(seisSchemeTypesModel)
        setupMocks(Some(listOfInvestorsEmptyShareHoldings))
        showWithSessionAndAuth(controller.show(2))(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some(controllers.seis.routes.IsExistingShareHolderController.show(2).url)
          }
        )
      }
    }


    "Load a populated ShareHolding review page" when {
      "an 'investor details list' is retrieved, a VALID investor details " +
        "ID is defined and a VALID share holding Id is provided" in {
        mockEnrolledRequest(seisSchemeTypesModel)
        setupMocks(Some(listOfInvestorsWithShareHoldings))
        showWithSessionAndAuth(controller.show(2))(
          result => {
            status(result) shouldBe OK
          }
        )
      }
    }

  }

  "Submitting to the PreviousShareHoldingsReviewController when authenticated and enrolled" should {
    "redirect to the AddInvestorOrNominee page if the form 'was not' previously populated" in {

      setupMocks(Some(listOfInvestorsComplete))
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(controller.submit(1))(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe
            Some(controllers.seis.routes.AddAnotherShareholdingController.show(1).url)
        }
      )
    }
  }

  "By removing the share holder the PreviousShareHoldingsReviewController when authenticated and enrolled" should {
    "redirect to the DeletePreviousShareHolderController page " in {
      setupMocks(Some(listOfInvestorsWithShareHoldings))
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(controller.remove(2, 1))(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe
            Some(controllers.seis.routes.DeletePreviousShareHolderController.show(2, 1).url)
        }
      )
    }
  }
}