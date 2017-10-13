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
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.Helpers.PreviousSchemesHelper
import models._
import models.investorDetails.InvestorDetailsModel
import models.seis.SEISCheckAnswersModel
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier
import views.html.seis.checkAndSubmit.CheckAnswers
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.mvc.{Action, AnyContent}
import services.EmailVerificationService

import scala.concurrent.Future

object CheckAnswersController extends CheckAnswersController{
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
  val emailVerificationService = EmailVerificationService
}

trait CheckAnswersController extends FrontendController with AuthorisedAndEnrolledForTAVC with PreviousSchemesHelper {

  override val acceptedFlows = Seq(Seq(SEIS))

  val emailVerificationService : EmailVerificationService

  def checkAnswersModel(implicit headerCarrier: HeaderCarrier, user: TAVCUser): Future[SEISCheckAnswersModel] = for {
    registeredAddress <- s4lConnector.fetchAndGetFormData[RegisteredAddressModel](KeystoreKeys.registeredAddress)
    dateOfIncorporation <- s4lConnector.fetchAndGetFormData[DateOfIncorporationModel](KeystoreKeys.dateOfIncorporation)
    natureOfBusiness <- s4lConnector.fetchAndGetFormData[NatureOfBusinessModel](KeystoreKeys.natureOfBusiness)
    previousSchemes <- getAllInvestmentFromKeystore(s4lConnector)
    contactDetails <- s4lConnector.fetchAndGetFormData[ContactDetailsModel](KeystoreKeys.contactDetails)
    contactAddress <- s4lConnector.fetchAndGetFormData[AddressModel](KeystoreKeys.contactAddress)
    qualifyBusinessActivity <- s4lConnector.fetchAndGetFormData[QualifyBusinessActivityModel](KeystoreKeys.isQualifyBusinessActivity)
    hasInvestmentTradeStarted <- s4lConnector.fetchAndGetFormData[HasInvestmentTradeStartedModel](KeystoreKeys.hasInvestmentTradeStarted)
    isSeventyPercentSpent <- s4lConnector.fetchAndGetFormData[SeventyPercentSpentModel](KeystoreKeys.seventyPercentSpent)
    shareIssueDate <- s4lConnector.fetchAndGetFormData[ShareIssueDateModel](KeystoreKeys.shareIssueDate)
    grossAssets <- s4lConnector.fetchAndGetFormData[GrossAssetsModel](KeystoreKeys.grossAssets)
    fullTimeEmployees <- s4lConnector.fetchAndGetFormData[FullTimeEmployeeCountModel](KeystoreKeys.fullTimeEmployeeCount)
    shareDescription <- s4lConnector.fetchAndGetFormData[ShareDescriptionModel](KeystoreKeys.shareDescription)
    numberOfShares <- s4lConnector.fetchAndGetFormData[NumberOfSharesModel](KeystoreKeys.numberOfShares)
    totalAmountRaised <- s4lConnector.fetchAndGetFormData[TotalAmountRaisedModel](KeystoreKeys.totalAmountRaised)
    totalAmountSpent <- s4lConnector.fetchAndGetFormData[TotalAmountSpentModel](KeystoreKeys.totalAmountSpent)
    investorDetails <- s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails)
    valueReceived <- s4lConnector.fetchAndGetFormData[WasAnyValueReceivedModel](KeystoreKeys.wasAnyValueReceived)
    shareCapitalChanges <- s4lConnector.fetchAndGetFormData[ShareCapitalChangesModel](KeystoreKeys.shareCapitalChanges)
    supportingDocumentsUpload <- s4lConnector.fetchAndGetFormData[SupportingDocumentsUploadModel](KeystoreKeys.supportingDocumentsUpload)
  } yield SEISCheckAnswersModel(registeredAddress, dateOfIncorporation, natureOfBusiness, previousSchemes,
    contactDetails, contactAddress, qualifyBusinessActivity, hasInvestmentTradeStarted, isSeventyPercentSpent, shareIssueDate,
    grossAssets, fullTimeEmployees, shareDescription, numberOfShares, totalAmountRaised, totalAmountSpent, investorDetails,
    valueReceived, shareCapitalChanges, supportingDocumentsUpload, applicationConfig.uploadFeatureEnabled)

  def show(envelopeId: Option[String]): Action[AnyContent] = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    if (envelopeId.fold("")(_.toString).length > 0) {
      s4lConnector.saveFormData(KeystoreKeys.envelopeId, envelopeId.getOrElse(""))
    }

    checkAnswersModel.flatMap(checkAnswers => Future.successful(Ok(CheckAnswers(checkAnswers))))
  }

  val submit = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    val verifyStatus = for {
      contactDetails <- s4lConnector.fetchAndGetFormData[ContactDetailsModel](KeystoreKeys.contactDetails)
      isVerified <- emailVerificationService.verifyEmailAddress(contactDetails.get.email)
    } yield isVerified.getOrElse(false)

    verifyStatus.flatMap {
      case true => Future.successful(Redirect(routes.DeclarationController.show()))
      case false => Future.successful(Redirect(routes.EmailVerificationController.verify(Constants.CheckAnswersReturnUrl)))
    }
  }
}