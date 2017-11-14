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
import models.investorDetails.{InvestorDetailsModel, PreviousShareHoldingModel}
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._

import scala.concurrent.Future

class DeletePreviousShareHolderControllerSpec extends BaseSpec {

  object TestController extends DeletePreviousShareHolderController {
    override lazy val enrolmentConnector = mockEnrolmentConnector
    override lazy val applicationConfig = MockConfig

    override protected def authConnector = MockAuthConnector

    override lazy val s4lConnector = mockS4lConnector
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
    mockEnrolledRequest(eisSchemeTypesModel)

    when(mockS4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](Matchers.eq(KeystoreKeys.investorDetails))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(investorDetailsModel))

  }


  "DeletePreviousShareHolderController" should {
    "use the correct keystore connector" in {
      DeletePreviousShareHolderController.s4lConnector shouldBe S4LConnector
    }

    "use the correct enrollment connector" in {
      DeletePreviousShareHolderController.enrolmentConnector shouldBe EnrolmentConnector
    }

    "use the correct config" in {
      DeletePreviousShareHolderController.applicationConfig shouldBe FrontendAppConfig
    }

    "use the correct auth connector" in {
      DeletePreviousShareHolderController.authConnector shouldBe FrontendAuthConnector
    }
  }

    "no 'investor details list' is retrieved" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupMocks(None)
      showWithSessionAndAuth(TestController.show(2, 1))(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.eis.routes.AddInvestorOrNomineeController.show(None).url)
        }
      )
    }


    "an 'investor details list' is retrieved and an INVALID investor details ID is passed" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupMocks(Some(listOfInvestorsComplete))
      showWithSessionAndAuth(TestController.show(Constants.obviouslyInvalidId, 1))(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.eis.routes.AddInvestorOrNomineeController.show(None).url)
        }
      )
    }


    "an 'investor details list' is retrieved, a VALID investor details " +
      "ID is passed and the investor details contains an empty list of share holdings" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupMocks(Some(listOfInvestorsEmptyShareHoldings))
      showWithSessionAndAuth(TestController.show(2, 1))(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.eis.routes.IsExistingShareHolderController.show(2).url)
        }
      )
    }


  "Load a populated ShareHolding review page" when {
    "an 'investor details list' is retrieved, a VALID investor details " +
      "ID is defined and a VALID share holding Id is provided" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupMocks(Some(listOfInvestorsWithShareHoldings))
      showWithSessionAndAuth(TestController.show(2, 1))(
        result => {
          status(result) shouldBe OK
        }
      )
    }
  }

  "Submitting to the DeletePreviousShareHolderController when authenticated and enrolled" should {
    "redirect to the PreviousShareHoldingsReviewController page " in {

      setupMocks(Some(listOfInvestorsComplete))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit(2, 1))(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe
            Some(controllers.eis.routes.PreviousShareHoldingsReviewController.show(listOfInvestorsComplete.head.processingId.get).url)
        }
      )
    }
  }

}
