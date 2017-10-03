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

import auth._
import common.{Constants, KeystoreKeys}
import config.FrontendAuthConnector
import connectors.{EnrolmentConnector, S4LConnector, SubmissionConnector}
import controllers.helpers.BaseSpec
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.libs.json.Json
import play.api.test.Helpers._
import services.FileUploadService
import uk.gov.hmrc.play.http.HttpResponse
import auth.AuthEnrolledTestController.{INTERNAL_SERVER_ERROR => _, NO_CONTENT => _, OK => _, SEE_OTHER => _, _}
import models.investorDetails.{InvestorDetailsModel, PreviousShareHoldingModel}
import models._
import models.repayments.{AnySharesRepaymentModel, SharesRepaymentDetailsModel}
import models.submission.{SchemeTypesModel, SubmissionResponse}

import scala.concurrent.Future

class AcknowledgementControllerSpec extends BaseSpec {

  val contactValid = ContactDetailsModel("first", "last", Some("07000 111222"), None, "test@test.com")
  val contactInvalid = ContactDetailsModel("first", "last", Some("07000 111222"), None, "test@badrequest.com")
  val yourCompanyNeed = YourCompanyNeedModel("AA")
  val submissionRequestValid = SubmissionRequest(contactValid, yourCompanyNeed)
  val submissionRequestInvalid = SubmissionRequest(contactInvalid, yourCompanyNeed)
  val submissionResponse = SubmissionResponse("2014-12-17", "FBUND09889765")
  val turnoverCheckPassedTrue = true
  val turnoverCheckPassedFalse = false

  implicit val user = mock[TAVCUser]


  object TestController extends AcknowledgementController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
    override val registrationDetailsService = mockRegistrationDetailsService
    override lazy val s4lConnector = mockS4lConnector
    override val submissionConnector = mockSubmissionConnector
    override lazy val fileUploadService = mockFileUploadService
  }

  class SetupPageFull() {
    setUpMocksRegistrationService(mockRegistrationDetailsService)
  }


  "AcknowledgementController" should {
    "use the correct keystore connector" in {
      AcknowledgementController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      AcknowledgementController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct submission connector" in {
      AcknowledgementController.submissionConnector shouldBe SubmissionConnector
    }
    "use the correct enrolment connector" in {
      AcknowledgementController.enrolmentConnector shouldBe EnrolmentConnector
    }
    "use the correct file upload service" in {
      AcknowledgementController.fileUploadService shouldBe FileUploadService
    }
  }

  val shareHoldersModelForReview = Vector(PreviousShareHoldingModel(investorShareIssueDateModel = Some(investorShareIssueDateModel1),
    numberOfPreviouslyIssuedSharesModel = Some(numberOfPreviouslyIssuedSharesModel1),
    previousShareHoldingNominalValueModel = Some(previousShareHoldingNominalValueModel1),
    previousShareHoldingDescriptionModel = Some(previousShareHoldingDescriptionModel1),
    processingId = Some(1), investorProcessingId = Some(2)))

  val investorModelForReview = InvestorDetailsModel(Some(investorModel2), Some(companyOrIndividualModel2), Some(companyDetailsModel2), None,
    Some(numberOfSharesPurchasedModel2), Some(howMuchSpentOnSharesModel2), Some(isExistingShareHolderModelYes),
    previousShareHoldingModels = Some(shareHoldersModelForReview), processingId = Some(2))

  val listOfInvestorsEmptyShareHoldings = Vector(validModelWithPrevShareHoldings.copy(previousShareHoldingModels = Some(Vector())))
  val listOfInvestorsWithShareHoldings = Vector(investorModelForReview)
  val listOfInvestorsMissingNumberOfPreviouslyIssuedShares = Vector(validModelWithPrevShareHoldings.copy(previousShareHoldingModels =
    Some(Vector(PreviousShareHoldingModel(previousShareHoldingDescriptionModel = Some(previousShareHoldingDescriptionModel1), processingId = Some(1))))))

  //noinspection ScalaStyle
  def setupMocks(): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[GrossAssetsModel](Matchers.eq(KeystoreKeys.grossAssets))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(GrossAssetsModel(12345))))
    when(mockS4lConnector.fetchAndGetFormData[FullTimeEmployeeCountModel](Matchers.eq(KeystoreKeys.fullTimeEmployeeCount))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(FullTimeEmployeeCountModel(22))))
    when(mockS4lConnector.fetchAndGetFormData[ShareIssueDateModel](Matchers.eq(KeystoreKeys.shareIssueDate))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(shareIssuetDateModel)))
    when(mockSubmissionConnector.submitComplianceStatement(Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(HttpResponse(OK, Some(Json.toJson(submissionResponse)))))
    when(mockS4lConnector.fetchAndGetFormData[TradeStartDateModel](Matchers.eq(KeystoreKeys.tradeStartDate))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Some(tradeStartDateModelYes))
    when(mockS4lConnector.fetchAndGetFormData[SchemeTypesModel](Matchers.eq(KeystoreKeys.selectedSchemes))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Some(schemeTypesSEIS))
    when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(hadPreviousRFIModelYes)))
    when(mockS4lConnector.fetchAndGetFormData[HadOtherInvestmentsModel](Matchers.eq(KeystoreKeys.hadOtherInvestments))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(hadOtherInvestmentsModelYes)))
    when(mockS4lConnector.fetchAndGetFormData[List[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(List(PreviousSchemeModel("test", 1, Some(1), Some("Name"), Some(1), Some(2), Some(2015), Some(1))))))
    when(mockS4lConnector.fetchAndGetFormData[ShareDescriptionModel](Matchers.eq(KeystoreKeys.shareDescription))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(shareDescriptionModel)))
    when(mockS4lConnector.fetchAndGetFormData[NumberOfSharesModel](Matchers.eq(KeystoreKeys.numberOfShares))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(numberOfSharesModel)))
    when(mockS4lConnector.fetchAndGetFormData[TotalAmountRaisedModel](Matchers.eq(KeystoreKeys.totalAmountRaised))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(TotalAmountRaisedModel(12345))))
    when(mockS4lConnector.fetchAndGetFormData[TotalAmountSpentModel](Matchers.eq(KeystoreKeys.totalAmountSpent))(Matchers.any(),
      Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(TotalAmountSpentModel(12345))))
    when(mockS4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](Matchers.eq(KeystoreKeys.investorDetails))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(listOfInvestorsWithShareHoldings)))
    when(mockS4lConnector.fetchAndGetFormData[WasAnyValueReceivedModel](Matchers.eq(KeystoreKeys.wasAnyValueReceived))(Matchers.any(),
      Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(WasAnyValueReceivedModel(Constants.StandardRadioButtonYesValue,
        Some("text")))))
    when(mockS4lConnector.fetchAndGetFormData[ShareCapitalChangesModel](Matchers.eq(KeystoreKeys.shareCapitalChanges))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(ShareCapitalChangesModel(Constants.StandardRadioButtonYesValue, Some("test")))))
    when(mockS4lConnector.fetchAndGetFormData[SupportingDocumentsUploadModel](Matchers.eq(KeystoreKeys.supportingDocumentsUpload))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(SupportingDocumentsUploadModel("No"))))
    when(mockS4lConnector.fetchAndGetFormData[ContactDetailsModel](Matchers.eq(KeystoreKeys.contactDetails))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(contactDetailsModel)))
    when(mockS4lConnector.fetchAndGetFormData[ConfirmCorrespondAddressModel](Matchers.eq(KeystoreKeys.confirmContactAddress))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(confirmCorrespondAddressModel)))
    when(mockS4lConnector.fetchAndGetFormData[QualifyBusinessActivityModel](Matchers.eq(KeystoreKeys.isQualifyBusinessActivity))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(qualifyTrade)))
    when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(dateOfIncorporationModel)))
    when(mockS4lConnector.fetchAndGetFormData[NatureOfBusinessModel](Matchers.eq(KeystoreKeys.natureOfBusiness))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(natureOfBusinessModel)))
    when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(subsidiariesModelYes)))
    when(mockS4lConnector.fetchAndGetFormData[TradeStartDateModel](Matchers.eq(KeystoreKeys.tradeStartDate))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(tradeStartDateModelYes)))
    when(mockS4lConnector.fetchAndGetFormData[HasInvestmentTradeStartedModel](Matchers.eq(KeystoreKeys.hasInvestmentTradeStarted))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[ResearchStartDateModel](Matchers.eq(KeystoreKeys.researchStartDate))(Matchers.any(), Matchers.any(),
      Matchers.any())).thenReturn(Future.successful(Some(researchStartDateModelYes)))
    when(mockS4lConnector.fetchAndGetFormData[SeventyPercentSpentModel](Matchers.eq(KeystoreKeys.seventyPercentSpent))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Some(isSeventyPercentSpentModelYes)))
    when(mockS4lConnector.fetchAndGetFormData[SchemeTypesModel](Matchers.eq(KeystoreKeys.selectedSchemes))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(schemeTypesSEIS)))
    when(mockS4lConnector.fetchAndGetFormData[SchemeTypesModel](Matchers.eq(KeystoreKeys.selectedSchemes))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(schemeTypesSEIS)))
    when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(trueKIModel)))
    when(mockS4lConnector.fetchAndGetFormData[TenYearPlanModel](Matchers.eq(KeystoreKeys.tenYearPlan))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(tenYearPlanModelYes)))
    when(mockS4lConnector.fetchAndGetFormData[NewGeographicalMarketModel](Matchers.eq(KeystoreKeys.newGeographicalMarket))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(newGeographicalMarketModelYes)))
    when(mockS4lConnector.fetchAndGetFormData[NewProductModel](Matchers.eq(KeystoreKeys.newProduct))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(newProductMarketModelYes)))
    when(mockS4lConnector.fetchAndGetFormData[MarketDescriptionModel](Matchers.eq(KeystoreKeys.marketDescription))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(MarketDescriptionModel("test"))))
    when(mockS4lConnector.fetchAndGetFormData[Boolean](Matchers.eq(KeystoreKeys.turnoverAPiCheckPassed))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(turnoverCheckPassedTrue)))
    when(mockS4lConnector.fetchAndGetFormData[AnnualTurnoverCostsModel](Matchers.eq(KeystoreKeys.turnoverCosts))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(turnoverCostsValid)))
    when(mockS4lConnector.fetchAndGetFormData[OperatingCostsModel](Matchers.eq(KeystoreKeys.operatingCosts))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(operatingCostsValid)))
    when(mockS4lConnector.fetchAndGetFormData[ThirtyDayRuleModel](Matchers.eq(KeystoreKeys.thirtyDayRule))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(thirtyDayRuleModelYes)))
    when(mockS4lConnector.fetchAndGetFormData[InvestmentGrowModel](Matchers.eq(KeystoreKeys.investmentGrow))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(investmentGrowValid)))
    when(mockS4lConnector.fetchAndGetFormData[SubsidiariesSpendingInvestmentModel](Matchers.eq(KeystoreKeys.subsidiariesSpendingInvestment))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(subsidiariesSpendingInvestmentModelNo)))
    when(mockS4lConnector.fetchAndGetFormData[SubsidiariesNinetyOwnedModel](Matchers.eq(KeystoreKeys.subsidiariesNinetyOwned))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(subsidiariesNinetyOwnedModelYes)))
    when(mockS4lConnector.fetchAndGetFormData[AnySharesRepaymentModel](Matchers.eq(KeystoreKeys.anySharesRepayment))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(anySharesRepaymentModelYes)))
    when(mockS4lConnector.fetchAndGetFormData[List[SharesRepaymentDetailsModel]](Matchers.eq(KeystoreKeys.sharesRepaymentDetails))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(validSharesRepaymentDetailsVector.toList)
    ))
  }

}
