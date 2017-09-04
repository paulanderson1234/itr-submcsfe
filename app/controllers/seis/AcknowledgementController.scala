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

import auth.{AuthorisedAndEnrolledForTAVC, SEIS, TAVCUser}
import common.{Constants, KeystoreKeys}
import config.FrontendGlobal._
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector, SubmissionConnector}
import controllers.Helpers.PreviousSchemesHelper
import controllers.feedback
import models._
import models.investorDetails.InvestorDetailsModel
import models.registration.RegistrationDetailsModel
import models.seis._
import models.submission._
import play.Logger
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Action, AnyContent, Request, Result}
import services.{FileUploadService, RegistrationDetailsService}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}
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

  override val acceptedFlows = Seq(Seq(SEIS))

  val submissionConnector: SubmissionConnector
  val registrationDetailsService: RegistrationDetailsService
  val fileUploadService: FileUploadService

  def getCompanyDetailsAnswers(implicit hc: HeaderCarrier, user: TAVCUser): Future[Option[CompanyDetailsAnswersModel]] = {
    val natureOfBusiness = s4lConnector.fetchAndGetFormData[NatureOfBusinessModel](KeystoreKeys.natureOfBusiness)
    val dateOfIncorporation = s4lConnector.fetchAndGetFormData[DateOfIncorporationModel](KeystoreKeys.dateOfIncorporation)
    val qualifyingBusinessActivity = s4lConnector.fetchAndGetFormData[QualifyBusinessActivityModel](KeystoreKeys.isQualifyBusinessActivity)
    val hasInvestmentTradeStartedModel = s4lConnector.fetchAndGetFormData[HasInvestmentTradeStartedModel](KeystoreKeys.hasInvestmentTradeStarted)
    val researchStartDate = s4lConnector.fetchAndGetFormData[ResearchStartDateModel](KeystoreKeys.researchStartDate)
    val seventyPercent = s4lConnector.fetchAndGetFormData[SeventyPercentSpentModel](KeystoreKeys.seventyPercentSpent)
    val shareIssueDate = s4lConnector.fetchAndGetFormData[ShareIssueDateModel](KeystoreKeys.shareIssueDate)
    val grossAssets = s4lConnector.fetchAndGetFormData[GrossAssetsModel](KeystoreKeys.grossAssets)
    val fullTimeEmployeeCount = s4lConnector.fetchAndGetFormData[FullTimeEmployeeCountModel](KeystoreKeys.fullTimeEmployeeCount)

    def createModel(natureOfBusinessModel: Option[NatureOfBusinessModel],
                    dateOfIncorporationModel: Option[DateOfIncorporationModel],
                    qualifyingBusinessActivityModel: Option[QualifyBusinessActivityModel],
                    hasInvestmentTradeStartedModel: Option[HasInvestmentTradeStartedModel],
                    researchStartDateModel: Option[ResearchStartDateModel],
                    seventyPercentSpentModel: Option[SeventyPercentSpentModel],
                    shareIssueDateModel: Option[ShareIssueDateModel],
                    grossAssetsModel: Option[GrossAssetsModel],
                    fullTimeEmployeeCountModel: Option[FullTimeEmployeeCountModel]) = {

      for {
        natureOfBusinessModel <- natureOfBusinessModel
        dateOfIncorporationModel <- dateOfIncorporationModel
        qualifyingBusinessActivityModel <- qualifyingBusinessActivityModel
        shareIssueDateModel <- shareIssueDateModel
        grossAssetsModel <- grossAssetsModel
        fullTimeEmployeeCountModel <- fullTimeEmployeeCountModel
      } yield {
        CompanyDetailsAnswersModel(natureOfBusinessModel, dateOfIncorporationModel, qualifyingBusinessActivityModel,
          hasInvestmentTradeStartedModel, researchStartDateModel, seventyPercentSpentModel, shareIssueDateModel, grossAssetsModel, fullTimeEmployeeCountModel)
      }
    }

    for {
      natureOfBusinessModel <- natureOfBusiness
      dateOfIncorporationModel <- dateOfIncorporation
      qualifyingBusinessActivityModel <- qualifyingBusinessActivity
      tradeStartDateModel <- hasInvestmentTradeStartedModel
      researchStartDateModel <- researchStartDate
      seventyPercentModel <- seventyPercent
      shareIssueDateModel <- shareIssueDate
      grossAssetsModel <- grossAssets
      fullTimeEmployeeCountModel <- fullTimeEmployeeCount
    } yield {
      createModel(natureOfBusinessModel, dateOfIncorporationModel, qualifyingBusinessActivityModel, tradeStartDateModel, researchStartDateModel,
        seventyPercentModel, shareIssueDateModel, grossAssetsModel, fullTimeEmployeeCountModel)
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

  def getShareDetailsAnswersModel(implicit hc: HeaderCarrier, user: TAVCUser): Future[Option[ShareDetailsAnswersModel]] = {
    val shareDescription = s4lConnector.fetchAndGetFormData[ShareDescriptionModel](KeystoreKeys.shareDescription)
    val numberOfShares = s4lConnector.fetchAndGetFormData[NumberOfSharesModel](KeystoreKeys.numberOfShares)
    val totalAmountRaised = s4lConnector.fetchAndGetFormData[TotalAmountRaisedModel](KeystoreKeys.totalAmountRaised)
    val totalAmountSpent = s4lConnector.fetchAndGetFormData[TotalAmountSpentModel](KeystoreKeys.totalAmountSpent)

    def createModel(shareDescriptionModel: Option[ShareDescriptionModel],
                    numberOfSharesModel: Option[NumberOfSharesModel],
                    totalAmountRaisedModel: Option[TotalAmountRaisedModel],
                    totalAmountSpentModel: Option[TotalAmountSpentModel]) = {
      for {
        shareDescriptionModel <- shareDescriptionModel
        numberOfSharesModel <- numberOfSharesModel
        totalAmountRaisedModel <- totalAmountRaisedModel
      } yield ShareDetailsAnswersModel(shareDescriptionModel, numberOfSharesModel, totalAmountRaisedModel, totalAmountSpentModel)
    }

    for {
      shareDescriptionModel <- shareDescription
      numberOfSharesModel <- numberOfShares
      totalAmountRaisedModel <- totalAmountRaised
      totalAmountSpentModel <- totalAmountSpent
    } yield createModel(shareDescriptionModel, numberOfSharesModel, totalAmountRaisedModel, totalAmountSpentModel)
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
    val correspondenceAddress = s4lConnector.fetchAndGetFormData[ConfirmCorrespondAddressModel](KeystoreKeys.confirmContactAddress)

    def createModel(contactDetailsModel: Option[ContactDetailsModel],
                    correspondenceAddressModel: Option[ConfirmCorrespondAddressModel]) = {
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

  def getAnswers(implicit hc: HeaderCarrier, user: TAVCUser): Future[Option[SEISAnswersModel]] = {
    val companyDetailsAnswers = getCompanyDetailsAnswers
    val previousSchemesAnswers = getPreviousSchemesAnswersModel
    val shareDetailsAnswers = getShareDetailsAnswersModel
    val investorDetailsAnswers = getInvestorDetailsAnswersModel
    val contactDetailsAnswers = getContactDetailsAnswerModel
    val supportingDocumentsUpload = s4lConnector.fetchAndGetFormData[SupportingDocumentsUploadModel](KeystoreKeys.supportingDocumentsUpload)

    def createModel(companyDetailsAnswersModel: Option[CompanyDetailsAnswersModel],
                    previousSchemesAnswersModel: Option[PreviousSchemesAnswersModel],
                    shareDetailsAnswersModel: Option[ShareDetailsAnswersModel],
                    investorDetailsAnswersModel: Option[InvestorDetailsAnswersModel],
                    contactDetailsAnswersModel: Option[ContactDetailsAnswersModel],
                    supportingDocumentsUploadModel: Option[SupportingDocumentsUploadModel]) = {
      for {
        companyDetailsAnswersModel <- companyDetailsAnswersModel
        previousSchemesAnswersModel <- previousSchemesAnswersModel
        shareDetailsAnswersModel <- shareDetailsAnswersModel
        investorDetailsAnswersModel <- investorDetailsAnswersModel
        contactDetailsAnswersModel <- contactDetailsAnswersModel
        supportingDocumentsUploadModel <- supportingDocumentsUploadModel
      } yield SEISAnswersModel(companyDetailsAnswersModel, previousSchemesAnswersModel, shareDetailsAnswersModel, investorDetailsAnswersModel,
        contactDetailsAnswersModel, supportingDocumentsUploadModel)
    }

    for {
      companyDetailsAnswersModel <- companyDetailsAnswers
      previousSchemesAnswersModel <- previousSchemesAnswers
      shareDetailsAnswersModel <- shareDetailsAnswers
      investorDetailsAnswersModel <- investorDetailsAnswers
      contactDetailsAnswersModel <- contactDetailsAnswers
      supportingDocumentsUploadModel <- supportingDocumentsUpload
    } yield createModel(companyDetailsAnswersModel, previousSchemesAnswersModel, shareDetailsAnswersModel, investorDetailsAnswersModel,
      contactDetailsAnswersModel, supportingDocumentsUploadModel
    )
  }

  def processResult(seisAnswersModel: SEISAnswersModel, tavcReferenceNumber: String)
                   (implicit hc: HeaderCarrier, user: TAVCUser, request: Request[AnyContent]): Future[Result] = {
    submissionConnector.submitComplainceStatement(seisAnswersModel, tavcReferenceNumber).map { submissionResponse =>
      submissionResponse.status match {
        case OK =>
          s4lConnector.clearCache()
          Ok(views.html.seis.checkAndSubmit.Acknowledgement(submissionResponse.json.as[SubmissionResponse]))
        case _ => {
          Logger.warn(s"[AcknowledgementController][createSubmissionDetailsModel] - " +
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

  def processResultUpload(seisAnswersModel: SEISAnswersModel, tavcReferenceNumber: String)
                         (implicit hc: HeaderCarrier, user: TAVCUser, request: Request[AnyContent]): Future[Result] = {
    submissionConnector.submitComplainceStatement(seisAnswersModel, tavcReferenceNumber).flatMap { submissionResponse =>
      submissionResponse.status match {
        case OK =>
          s4lConnector.fetchAndGetFormData[String](KeystoreKeys.envelopeId).flatMap {
            envelopeId => fileUploadService.closeEnvelope(tavcReferenceNumber, envelopeId.fold("")(_.toString)).map {
              _ => s4lConnector.clearCache()
                Ok(views.html.seis.checkAndSubmit.Acknowledgement(submissionResponse.json.as[SubmissionResponse]))
            }
          }
        case _ => {
          Logger.warn(s"[AcknowledgementController][createSubmissionDetailsModel] - " +
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
        seisAnswersModel <- getAnswers
        isValid <- seisAnswersModel.get.validate(submissionConnector)
      } yield if (isValid) (seisAnswersModel, tavcReferenceNumber) else (None, tavcReferenceNumber)

      sourceWithRef.flatMap{
        case (Some(seisAnswersModel), tavcReferenceNumber) =>
          if (fileUploadService.getUploadFeatureEnabled) processResultUpload(seisAnswersModel, tavcReferenceNumber)
          else processResult(seisAnswersModel, tavcReferenceNumber)
        case (None, _) => Future.successful(Redirect(controllers.routes.ApplicationHubController.show()))
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

  private def buildSubsidiaryPerformingTrade(subsidiariesSpendInvest: Option[SubsidiariesSpendingInvestmentModel],
                                             subsidiariesNinetyOwned: Option[SubsidiariesNinetyOwnedModel],
                                             tradeName: String,
                                             tradeAddress: Option[AddressModel]): Option[SubsidiaryPerformingTradeModel] = {

    if (subsidiariesSpendInvest.fold("")(_.subSpendingInvestment) == Constants.StandardRadioButtonYesValue)
      Some(SubsidiaryPerformingTradeModel(ninetyOwnedModel = subsidiariesNinetyOwned.get,
        organisationName = tradeName, companyAddress = tradeAddress,
        ctUtr = None, crn = None))
    else None
  }

  private def buildOrganisationDetails(commercialSale: Option[CommercialSaleModel],
                                       dateOfIncorporation: DateOfIncorporationModel,
                                       companyName: String,
                                       companyAddress: AddressModel,
                                       previousSchemes: List[PreviousSchemeModel]): OrganisationDetailsModel = {

    OrganisationDetailsModel(utr = None, organisationName = companyName, chrn = None, startDate = dateOfIncorporation,
      firstDateOfCommercialSale = if (commercialSale.fold("")(_.hasCommercialSale) == Constants.StandardRadioButtonYesValue) {
        val date = commercialSale.get
        Some(Validation.dateToDesFormat(date.commercialSaleDay.get, date.commercialSaleMonth.get, date.commercialSaleYear.get))
      } else None,
      ctUtr = None, crn = None, companyAddress = Some(companyAddress),
      previousRFIs = if (previousSchemes.nonEmpty) Some(previousSchemes) else None)
  }

}

