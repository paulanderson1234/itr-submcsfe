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

import auth.{AuthorisedAndEnrolledForTAVC, EIS, TAVCUser}
import common.{Constants, KeystoreKeys}
import config.FrontendGlobal._
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector, SubmissionConnector}
import controllers.Helpers.TotalAmountRaisedHelper
import controllers.feedback
import models._
import models.investorDetails.InvestorDetailsModel
import models.registration.RegistrationDetailsModel
import models.repayments.{AnySharesRepaymentModel, SharesRepaymentDetailsModel}
import models.submission._
import play.Logger
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Action, AnyContent, Request, Result}
import services.{FileUploadService, RegistrationDetailsService}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier
import utils.Validation

import scala.concurrent.Future

object AcknowledgementController extends AcknowledgementController {
  override lazy val s4lConnector = S4LConnector
  override lazy val submissionConnector = SubmissionConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
  val registrationDetailsService: RegistrationDetailsService = RegistrationDetailsService
  override lazy val fileUploadService = FileUploadService
}

trait AcknowledgementController extends FrontendController with AuthorisedAndEnrolledForTAVC {

  override val acceptedFlows = Seq(Seq(EIS))
  val submissionConnector: SubmissionConnector
  val registrationDetailsService: RegistrationDetailsService
  val fileUploadService: FileUploadService

  //noinspection ScalaStyle
  def getAnswers(implicit hc: HeaderCarrier, user: TAVCUser): Future[Option[ComplianceStatementAnswersModel]] = {
    val companyDetailsAnswers = getCompanyDetailsAnswers
    val previousSchemesAnswers = getPreviousSchemesAnswersModel
    val shareDetailsAnswers = getShareDetailsAnswersModel
    val investorDetailsAnswers = getInvestorDetailsAnswersModel
    val contactDetailsAnswers = getContactDetailsAnswerModel
    val supportingDocumentsUpload = s4lConnector.fetchAndGetFormData[SupportingDocumentsUploadModel](KeystoreKeys.supportingDocumentsUpload)
    val schemeType = s4lConnector.fetchAndGetFormData[SchemeTypesModel](KeystoreKeys.selectedSchemes)
    val kiAnswers = getKiProcessingAnswerModel
    val marketInfoAnswers = getMarketInfoAnswerModel
    val costAnswers = getCostAnswerModel
    val thirtyDayRuleAnswers = getThirtyDayRuleAnswerModel
    val investmentGrowAnswers = getInvestmentGrowAnswersModel
    val subsidiariesAnswers = getSubsidiariesAnswersModel
    val repaidSharesAnswers = getRepaidSharesAnswersModel

    def createModel(companyDetailsAnswersModel: Option[CompanyDetailsAnswersModel],
                    previousSchemesAnswersModel: Option[PreviousSchemesAnswersModel],
                    shareDetailsAnswersModel: Option[ShareDetailsAnswersModel],
                    investorDetailsAnswersModel: Option[InvestorDetailsAnswersModel],
                    contactDetailsAnswersModel: Option[ContactDetailsAnswersModel],
                    supportingDocumentsUploadModel: Option[SupportingDocumentsUploadModel],
                    schemeTypeModel: Option[SchemeTypesModel],
                    marketInfoAnswersModel: Option[MarketInfoAnswersModel],
                    kiAnswersModel: Option[KiAnswersModel],
                    costsAnswersModel: CostsAnswerModel,
                    thirtyDayRuleAnswersModel: Option[ThirtyDayRuleAnswersModel],
                    investmentGrowAnswersModel: Option[InvestmentGrowAnswersModel],
                    subsidiariesAnswersModel: Option[SubsidiariesAnswersModel],
                    repaidSharesAnswersModel:Option[RepaidSharesAnswersModel]) = {

      for {
        companyDetailsAnswersModel <- companyDetailsAnswersModel
        previousSchemesAnswersModel <- previousSchemesAnswersModel
        shareDetailsAnswersModel <- shareDetailsAnswersModel
        investorDetailsAnswersModel <- investorDetailsAnswersModel
        contactDetailsAnswersModel <- contactDetailsAnswersModel
        supportingDocumentsUploadModel <- supportingDocumentsUploadModel
        schemeTypeModel <- schemeTypeModel


      } yield ComplianceStatementAnswersModel(companyDetailsAnswersModel, previousSchemesAnswersModel, shareDetailsAnswersModel, investorDetailsAnswersModel,
        contactDetailsAnswersModel,supportingDocumentsUploadModel, schemeTypeModel, kiAnswersModel, marketInfoAnswersModel, costsAnswersModel, thirtyDayRuleAnswersModel,
        investmentGrowAnswersModel, subsidiariesAnswersModel, repaidSharesAnswersModel)
    }

    for {
      companyDetailsAnswersModel <- companyDetailsAnswers
      previousSchemesAnswersModel <- previousSchemesAnswers
      shareDetailsAnswersModel <- shareDetailsAnswers
      investorDetailsAnswersModel <- investorDetailsAnswers
      contactDetailsAnswersModel <- contactDetailsAnswers
      supportingDocumentsUploadModel <- supportingDocumentsUpload
      schemeTypeModel <- schemeType
      marketInfoAnswersModel <- marketInfoAnswers
      kiAnswersModel <- kiAnswers
      costsAnswersModel <- costAnswers
      thirtyDayRuleAnswersModel <- thirtyDayRuleAnswers
      investmentGrowAnswersModel <- investmentGrowAnswers
      subsidiariesAnswersModel <- subsidiariesAnswers
      repaidSharesAnswersModel <- repaidSharesAnswers
    } yield createModel(companyDetailsAnswersModel, previousSchemesAnswersModel, shareDetailsAnswersModel, investorDetailsAnswersModel,
      contactDetailsAnswersModel, supportingDocumentsUploadModel, schemeTypeModel, marketInfoAnswersModel, kiAnswersModel,costsAnswersModel, thirtyDayRuleAnswersModel,
      investmentGrowAnswersModel, subsidiariesAnswersModel, repaidSharesAnswersModel)
  }

  def getCompanyDetailsAnswers(implicit hc: HeaderCarrier, user: TAVCUser): Future[Option[CompanyDetailsAnswersModel]] = {
    val natureOfBusiness = s4lConnector.fetchAndGetFormData[NatureOfBusinessModel](KeystoreKeys.natureOfBusiness)
    val dateOfIncorporation = s4lConnector.fetchAndGetFormData[DateOfIncorporationModel](KeystoreKeys.dateOfIncorporation)
    val qualifyingBusinessActivity = s4lConnector.fetchAndGetFormData[QualifyBusinessActivityModel](KeystoreKeys.isQualifyBusinessActivity)
    val hasInvestmentTradeStartedModel = s4lConnector.fetchAndGetFormData[HasInvestmentTradeStartedModel](KeystoreKeys.hasInvestmentTradeStarted)
    val researchStartDate = s4lConnector.fetchAndGetFormData[ResearchStartDateModel](KeystoreKeys.researchStartDate)
    val shareIssueDate = s4lConnector.fetchAndGetFormData[ShareIssueDateModel](KeystoreKeys.shareIssueDate)
    val grossAssets = s4lConnector.fetchAndGetFormData[GrossAssetsModel](KeystoreKeys.grossAssets)
    val grossAssetsAfter = s4lConnector.fetchAndGetFormData[GrossAssetsAfterIssueModel](KeystoreKeys.grossAssetsAfterIssue)
    val fullTimeEmployeeCount = s4lConnector.fetchAndGetFormData[FullTimeEmployeeCountModel](KeystoreKeys.fullTimeEmployeeCount)
    val commercialSale = s4lConnector.fetchAndGetFormData[CommercialSaleModel](KeystoreKeys.commercialSale)

    def createModel(natureOfBusinessModel: Option[NatureOfBusinessModel],
                    dateOfIncorporationModel: Option[DateOfIncorporationModel],
                    qualifyingBusinessActivityModel: Option[QualifyBusinessActivityModel],
                    hasInvestmentTradeStartedModel: Option[HasInvestmentTradeStartedModel],
                    researchStartDateModel: Option[ResearchStartDateModel],
                    shareIssueDateModel: Option[ShareIssueDateModel],
                    grossAssetsModel: Option[GrossAssetsModel],
                    grossAssetsAfterModel: Option[GrossAssetsAfterIssueModel],
                    fullTimeEmployeeCountModel: Option[FullTimeEmployeeCountModel],
                    commercialSaleModel: Option[CommercialSaleModel]) = {
      for {
        natureOfBusinessModel <- natureOfBusinessModel
        dateOfIncorporationModel <- dateOfIncorporationModel
        qualifyingBusinessActivityModel <- qualifyingBusinessActivityModel
        shareIssueDateModel <- shareIssueDateModel
        grossAssetsModel <- grossAssetsModel
        fullTimeEmployeeCountModel <- fullTimeEmployeeCountModel
      } yield {
        CompanyDetailsAnswersModel(natureOfBusinessModel, dateOfIncorporationModel, qualifyingBusinessActivityModel, hasInvestmentTradeStartedModel,
          researchStartDateModel,seventyPercentSpentModel = None, shareIssueDateModel, grossAssetsModel, grossAssetsAfterModel,fullTimeEmployeeCountModel, commercialSaleModel)
      }
    }

    for {
      natureOfBusinessModel <- natureOfBusiness
      dateOfIncorporationModel <- dateOfIncorporation
      qualifyingBusinessActivityModel <- qualifyingBusinessActivity
      tradeStartDateModel <- hasInvestmentTradeStartedModel
      researchStartDateModel <- researchStartDate
      shareIssueDateModel <- shareIssueDate
      grossAssetsModel <- grossAssets
      grossAssetsAfterModel <- grossAssetsAfter
      fullTimeEmployeeCountModel <- fullTimeEmployeeCount
      commercialSaleModel <- commercialSale
    } yield {
      createModel(natureOfBusinessModel, dateOfIncorporationModel, qualifyingBusinessActivityModel, tradeStartDateModel, researchStartDateModel,
        shareIssueDateModel, grossAssetsModel, grossAssetsAfterModel, fullTimeEmployeeCountModel, commercialSaleModel)
    }
  }

  def getPreviousSchemesAnswersModel(implicit hc: HeaderCarrier, user: TAVCUser): Future[Option[PreviousSchemesAnswersModel]] = {
    val hadPreviousRFI = s4lConnector.fetchAndGetFormData[HadPreviousRFIModel](KeystoreKeys.hadPreviousRFI)
    val otherInvestments = s4lConnector.fetchAndGetFormData[HadOtherInvestmentsModel](KeystoreKeys.hadOtherInvestments)
    val previousScheme = s4lConnector.fetchAndGetFormData[List[PreviousSchemeModel]](KeystoreKeys.previousSchemes)

    def createModel(hadPreviousRFIModel: Option[HadPreviousRFIModel],
                    otherInvestmentsModel: Option[HadOtherInvestmentsModel],
                    previousSchemeModel: Option[List[PreviousSchemeModel]]) = {
      for {
        hadPreviousRFIModel <- hadPreviousRFIModel
        otherInvestmentsModel <- otherInvestmentsModel
      } yield PreviousSchemesAnswersModel(hadPreviousRFIModel, otherInvestmentsModel, previousSchemeModel)
    }
    for {
      hadPreviousRFIModel <- hadPreviousRFI
      otherInvestmentsModel <- otherInvestments
      previousSchemeModel <- previousScheme
    } yield createModel(hadPreviousRFIModel, otherInvestmentsModel, previousSchemeModel)
  }

  def getRepaidSharesAnswersModel(implicit hc: HeaderCarrier, user: TAVCUser): Future[Option[RepaidSharesAnswersModel]] = {

    val anySharesRepayment = s4lConnector.fetchAndGetFormData[AnySharesRepaymentModel](KeystoreKeys.anySharesRepayment)
    val shareRepayments = s4lConnector.fetchAndGetFormData[List[SharesRepaymentDetailsModel]](KeystoreKeys.sharesRepaymentDetails)

    def createModel(anySharesRepaymentModel: Option[AnySharesRepaymentModel],
                    shareRepaymentsModel: Option[List[SharesRepaymentDetailsModel]]) = {
      for {
        anySharesRepaymentModel <- anySharesRepaymentModel
      } yield RepaidSharesAnswersModel(anySharesRepaymentModel, shareRepaymentsModel)
    }
    for {
      anySharesRepaymentModel <- anySharesRepayment
      shareRepaymentsModel <- shareRepayments
    } yield createModel(anySharesRepaymentModel, shareRepaymentsModel)
  }

  def getShareDetailsAnswersModel(implicit hc: HeaderCarrier, user: TAVCUser): Future[Option[ShareDetailsAnswersModel]] = {
    val shareDescription = s4lConnector.fetchAndGetFormData[ShareDescriptionModel](KeystoreKeys.shareDescription)
    val numberOfShares = s4lConnector.fetchAndGetFormData[NumberOfSharesModel](KeystoreKeys.numberOfShares)
    val totalAmountRaised = s4lConnector.fetchAndGetFormData[TotalAmountRaisedModel](KeystoreKeys.totalAmountRaised)

    def createModel(shareDescriptionModel: Option[ShareDescriptionModel],
                    numberOfSharesModel: Option[NumberOfSharesModel],
                    totalAmountRaisedModel: Option[TotalAmountRaisedModel]) = {
      for {
        shareDescriptionModel <- shareDescriptionModel
        numberOfSharesModel <- numberOfSharesModel
        totalAmountRaisedModel <- totalAmountRaisedModel
      } yield ShareDetailsAnswersModel(shareDescriptionModel, numberOfSharesModel, totalAmountRaisedModel, totalAmountSpentModel = None)
    }

    for {
      shareDescriptionModel <- shareDescription
      numberOfSharesModel <- numberOfShares
      totalAmountRaisedModel <- totalAmountRaised
    } yield createModel(shareDescriptionModel, numberOfSharesModel, totalAmountRaisedModel)
  }

  def getInvestorDetailsAnswersModel(implicit hc: HeaderCarrier, user: TAVCUser): Future[Option[InvestorDetailsAnswersModel]] = {
    val investors = s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails)
    val valueReceived = s4lConnector.fetchAndGetFormData[WasAnyValueReceivedModel](KeystoreKeys.wasAnyValueReceived)
    val shareCapitalChanges = s4lConnector.fetchAndGetFormData[ShareCapitalChangesModel](KeystoreKeys.shareCapitalChanges)

    def createModel(investorList: Option[Vector[InvestorDetailsModel]],
                    valueReceivedModel: Option[WasAnyValueReceivedModel],
                    shareCapitalChangesModel: Option[ShareCapitalChangesModel]) = {
      for {
        investorList <- investorList
        valueReceivedModel <- valueReceivedModel
        shareCapitalChangesModel <- shareCapitalChangesModel
      } yield InvestorDetailsAnswersModel(investorList, valueReceivedModel, shareCapitalChangesModel)
    }

    for {
      investorsModel <- investors
      valueReceivedModel <- valueReceived
      shareCapitalChangesModel <- shareCapitalChanges
    } yield createModel(investorsModel, valueReceivedModel, shareCapitalChangesModel)
  }

  def getContactDetailsAnswerModel(implicit hc: HeaderCarrier, user: TAVCUser): Future[Option[ContactDetailsAnswersModel]] = {
    val contactDetails = s4lConnector.fetchAndGetFormData[ContactDetailsModel](KeystoreKeys.contactDetails)
    val correspondenceAddress = s4lConnector.fetchAndGetFormData[AddressModel](KeystoreKeys.contactAddress)

    def createModel(contactDetailsModel: Option[ContactDetailsModel],
                    correspondenceAddressModel: Option[AddressModel]) = {
      for {
        contactDetailsModel <- contactDetailsModel
        correspondenceAddressModel <- correspondenceAddressModel
      } yield ContactDetailsAnswersModel(contactDetailsModel, correspondenceAddressModel)
    }

    for {
      contactDetailsModel <- contactDetails
      correspondenceAddressModel <- correspondenceAddress
    } yield createModel(contactDetailsModel, correspondenceAddressModel)
  }

  def getMarketInfoAnswerModel(implicit hc: HeaderCarrier, user: TAVCUser): Future[Option[MarketInfoAnswersModel]] = {
    val newGeographicalMarket = s4lConnector.fetchAndGetFormData[NewGeographicalMarketModel](KeystoreKeys.newGeographicalMarket)
    val newProduct = s4lConnector.fetchAndGetFormData[NewProductModel](KeystoreKeys.newProduct)
    val marketDescription = s4lConnector.fetchAndGetFormData[MarketDescriptionModel](KeystoreKeys.marketDescription)
    val isMarketRouteApplicable = TotalAmountRaisedHelper.checkIfMarketInfoApplies(s4lConnector)
    val turnoverApiCheckPassed = s4lConnector.fetchAndGetFormData[Boolean](KeystoreKeys.turnoverAPiCheckPassed)

    def createModel(newGeographicalMarketModel: Option[NewGeographicalMarketModel],
                    newProductModel: Option[NewProductModel], marketDescriptionModel: Option[MarketDescriptionModel],
                    isMarketRouteApplicable: MarketRoutingCheckResult,
                    turnoverApiCheckPassedModel:Option[Boolean]) = {
      for {
        newGeographicalMarketModel <- newGeographicalMarketModel
        newProductModel <- newProductModel
      } yield MarketInfoAnswersModel(newGeographicalMarketModel, newProductModel, marketDescriptionModel, isMarketRouteApplicable, turnoverApiCheckPassedModel)
    }

    for {
      newGeographicalMarketModel <- newGeographicalMarket
      newProductModel <- newProduct
      marketDescriptionModel <- marketDescription
      isMarketRouteApplicable <- isMarketRouteApplicable
      turnoverApiCheckPassedModel <- turnoverApiCheckPassed
    } yield createModel(newGeographicalMarketModel, newProductModel, marketDescriptionModel, isMarketRouteApplicable, turnoverApiCheckPassedModel)
  }

  def getCostAnswerModel(implicit hc: HeaderCarrier, user: TAVCUser): Future[CostsAnswerModel] = {

    val turnoverCosts = s4lConnector.fetchAndGetFormData[AnnualTurnoverCostsModel](KeystoreKeys.turnoverCosts)
    val operatingCosts = s4lConnector.fetchAndGetFormData[OperatingCostsModel](KeystoreKeys.operatingCosts)
    for {
      turnoverCostsModel <- turnoverCosts
      operatingCostsModel <- operatingCosts
    } yield CostsAnswerModel(operatingCostsModel, turnoverCostsModel)
  }

  def getKiProcessingAnswerModel(implicit hc: HeaderCarrier, user: TAVCUser): Future[Option[KiAnswersModel]] = {

    val tenYearPlan = s4lConnector.fetchAndGetFormData[TenYearPlanModel](KeystoreKeys.tenYearPlan)
    val Ki = s4lConnector.fetchAndGetFormData[KiProcessingModel](KeystoreKeys.kiProcessingModel)

    def createModel(tenYearPlanModel: Option[TenYearPlanModel],
                    kiProcessingModel: Option[KiProcessingModel]) = {
      for {
        kiProcessingModel <- kiProcessingModel
      } yield KiAnswersModel(kiProcessingModel, tenYearPlanModel)
    }

    for {
      tenYearPlanModel <- tenYearPlan
      kiProcessingModel <- Ki
    } yield createModel(tenYearPlanModel, kiProcessingModel)
  }

  def getSubsidiariesAnswersModel(implicit hc: HeaderCarrier, user: TAVCUser): Future[Option[SubsidiariesAnswersModel]] = {

    //TODO: Include address/sunsidiary trade name when available. Will be hard coded in target model for now.
    val subsidiariesSpendInvest = s4lConnector.fetchAndGetFormData[SubsidiariesSpendingInvestmentModel](KeystoreKeys.subsidiariesSpendingInvestment)
    val subsidiariesNinetyOwned = s4lConnector.fetchAndGetFormData[SubsidiariesNinetyOwnedModel](KeystoreKeys.subsidiariesNinetyOwned)

    def createModel(subsidiariesSpendInvestModel: Option[SubsidiariesSpendingInvestmentModel],
                    subsidiariesNinetyOwnedModel: Option[SubsidiariesNinetyOwnedModel]) = {
      for {
        subsidiariesSpendInvestModel <- subsidiariesSpendInvestModel
        subsidiariesNinetyOwnedModel <- subsidiariesNinetyOwnedModel
      } yield SubsidiariesAnswersModel(subsidiariesSpendInvestModel, subsidiariesNinetyOwnedModel)
    }

    for {
      subsidiariesSpendInvestModel <- subsidiariesSpendInvest
      subsidiariesNinetyOwnedModel <- subsidiariesNinetyOwned
    } yield createModel(subsidiariesSpendInvestModel, subsidiariesNinetyOwnedModel)
  }


  def getThirtyDayRuleAnswerModel(implicit hc: HeaderCarrier, user: TAVCUser): Future[Option[ThirtyDayRuleAnswersModel]] = {

    val thirtyYearRule = s4lConnector.fetchAndGetFormData[ThirtyDayRuleModel](KeystoreKeys.thirtyDayRule)
    val turnoverApiCheckPassed = s4lConnector.fetchAndGetFormData[Boolean](KeystoreKeys.turnoverAPiCheckPassed)

    def createModel(thirtyYearRuleModel: Option[ThirtyDayRuleModel], turnoverApiCheckPassedModel:Option[Boolean]) = {
      for {
        thirtyYearRuleModel <- thirtyYearRuleModel
        turnoverApiCheckPassedModel <- turnoverApiCheckPassedModel
      } yield ThirtyDayRuleAnswersModel(thirtyYearRuleModel, turnoverApiCheckPassedModel)
    }

    for {
      thirtyYearRuleModel <- thirtyYearRule
      turnoverApiCheckPassedModel <- turnoverApiCheckPassed
    } yield createModel(thirtyYearRuleModel, turnoverApiCheckPassedModel)
  }

  def getInvestmentGrowAnswersModel(implicit hc: HeaderCarrier, user: TAVCUser): Future[Option[InvestmentGrowAnswersModel]] = {
    val investmentGrow = s4lConnector.fetchAndGetFormData[InvestmentGrowModel](KeystoreKeys.investmentGrow)

    def createModel(investmentGrowModel: Option[InvestmentGrowModel]) = {
      for {
        investmentGrowModel <- investmentGrowModel
      } yield InvestmentGrowAnswersModel(investmentGrowModel)
    }

    for {
      investmentGrowModel <- investmentGrow
    } yield createModel(investmentGrowModel)
  }

  def processResult(answerModel: ComplianceStatementAnswersModel, tavcReferenceNumber: String,
                    registrationDetailsModel: Option[RegistrationDetailsModel])
                   (implicit hc: HeaderCarrier, user: TAVCUser, request: Request[AnyContent]): Future[Result] = {
    submissionConnector.submitComplianceStatement(answerModel, tavcReferenceNumber, registrationDetailsModel).map { submissionResponse =>
      submissionResponse.status match {
        case OK =>
          s4lConnector.clearCache()
          Ok(views.html.eis.checkAndSubmit.Acknowledgement(submissionResponse.json.as[SubmissionResponse]))
        case _ => {
          Logger.warn(s"[AcknowledgementController][getSubsidiariesAnswersModel] - " +
            s"HTTP Submission failed. Response Code: ${submissionResponse.status}")
          InternalServerError
        }
      }
    }
  }.recover {
    case e: Exception => {
      Logger.warn(s"[AcknowledgementController][submit] - Exception submitting application: ${e.getMessage}")
      InternalServerError(internalServerErrorTemplate)
    }
  }

  def processResultUpload(answersModel: ComplianceStatementAnswersModel, tavcReferenceNumber: String,
                          registrationDetailsModel: Option[RegistrationDetailsModel])
                         (implicit hc: HeaderCarrier, user: TAVCUser, request: Request[AnyContent]): Future[Result] = {
    submissionConnector.submitComplianceStatement(answersModel, tavcReferenceNumber, registrationDetailsModel).flatMap { submissionResponse =>
      submissionResponse.status match {
        case OK =>
          s4lConnector.fetchAndGetFormData[String](KeystoreKeys.envelopeId).flatMap {
            envelopeId => fileUploadService.closeEnvelope(tavcReferenceNumber, envelopeId.fold("")(_.toString)).map {
              _ => s4lConnector.clearCache()
                Ok(views.html.eis.checkAndSubmit.Acknowledgement(submissionResponse.json.as[SubmissionResponse]))
            }
          }
        case _ => {
          Logger.warn(s"[AcknowledgementController][processResultUpload] - " +
            s"HTTP Submission failed. Response Code: ${submissionResponse.status}")
          Future.successful(InternalServerError)
        }
      }
    }
  }.recover {
    case e: Exception => {
      Logger.warn(s"[AcknowledgementController][submit] - Exception submitting application: ${e.getMessage}")
      InternalServerError(internalServerErrorTemplate)
    }
  }

  //noinspection ScalaStyle
  val show = AuthorisedAndEnrolled.async { implicit user =>
    implicit request =>

      val sourceWithRef = for {
        tavcReferenceNumber <- getTavCReferenceNumber()
        answersModel <- getAnswers
        isValid <- answersModel.fold(Future.successful(false))(_.validateEis(submissionConnector, s4lConnector))
        registrationDetailsModel <- registrationDetailsService.getRegistrationDetails(tavcReferenceNumber)
      } yield if (isValid) (answersModel, tavcReferenceNumber, registrationDetailsModel) else (None, tavcReferenceNumber, registrationDetailsModel)

      sourceWithRef.flatMap {
        case (Some(answersModel), tavcReferenceNumber, registrationDetailsModel) => {
          if (fileUploadService.getUploadFeatureEnabled) processResultUpload(answersModel, tavcReferenceNumber, registrationDetailsModel)
          else processResult(answersModel, tavcReferenceNumber, registrationDetailsModel)
        }
        case (None, _, _) => Future.successful(Redirect(controllers.routes.ApplicationHubController.show()))
      }

  }

  def submit: Action[AnyContent] = AuthorisedAndEnrolled.apply { implicit user => implicit request =>
    Redirect(feedback.routes.FeedbackController.show().url)
  }

  private def getTradeStartDate(tradeStartDateModel: TradeStartDateModel): String = {
    if (tradeStartDateModel.hasTradeStartDate.equals(Constants.StandardRadioButtonYesValue)) {
      Validation.dateToDesFormat(tradeStartDateModel.tradeStartDay.get, tradeStartDateModel.tradeStartMonth.get, tradeStartDateModel.tradeStartYear.get)
    } else {
      Constants.standardIgnoreYearValue
    }
  }

}