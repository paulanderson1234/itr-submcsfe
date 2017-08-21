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

package testOnly.controllers.seis

import auth.{AuthorisedAndEnrolledForTAVC, TAVCUser}
import common.{Constants, KeystoreKeys}
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.Helpers.PreviousSchemesHelper
import models._
import models.investorDetails._
import forms._
import models.investorDetails.NumberOfSharesPurchasedModel
import models.submission.SchemeTypesModel
import play.api.data.Form
import play.api.libs.json.Format
import play.api.mvc.{Action, AnyContent, Request}
import testOnly.models._
import testOnly.forms.{TestInvestorModeOptionsForm, _}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import testOnly.controllers.InvestorTestHelper

import scala.concurrent.Future

trait TestEndpointSEISController extends FrontendController with AuthorisedAndEnrolledForTAVC{

  override val acceptedFlows = Seq()

  val s4lConnector: S4LConnector
  val defaultPreviousSchemesSize = 2
  val keyStoreKeyInvestorModelOptions = "testonly:keyStoreKeyInvestorModelOptions"

  def showPageOne(schemes: Option[Int]): Action[AnyContent] = AuthorisedAndEnrolled.async {
    implicit user => implicit request =>
      for {
        seventyPercentForm <- fillForm[SeventyPercentSpentModel](KeystoreKeys.seventyPercentSpent, SeventyPercentSpentForm.seventyPercentSpentForm)
        grossAssetsForm <- fillForm[GrossAssetsModel](KeystoreKeys.grossAssets, GrossAssetsForm.grossAssetsForm)
        shareIssueDateForm <- fillForm[ShareIssueDateModel](KeystoreKeys.shareIssueDate, ShareIssueDateForm.shareIssueDateForm)
        natureOfBusinessForm <- fillForm[NatureOfBusinessModel](KeystoreKeys.natureOfBusiness, NatureOfBusinessForm.natureOfBusinessForm)
        dateOfIncorporationForm <- fillForm[DateOfIncorporationModel](KeystoreKeys.dateOfIncorporation, DateOfIncorporationForm.dateOfIncorporationForm)
        tradeStartDateForm <- fillForm[TradeStartDateModel](KeystoreKeys.tradeStartDate, TradeStartDateForm.tradeStartDateForm)
        isFirstStartDateForm <- fillForm[IsFirstTradeModel](KeystoreKeys.isFirstTrade, IsFirstTradeForm.isFirstTradeForm)
        researchStartDateForm <- fillForm[ResearchStartDateModel](KeystoreKeys.researchStartDate, ResearchStartDateForm.researchStartDateForm)
        hadPreviousRFIForm <- fillForm[HadPreviousRFIModel](KeystoreKeys.hadPreviousRFI, HadPreviousRFIForm.hadPreviousRFIForm)
        previousSchemesForm <- fillPreviousSchemesForm
        proposedInvestmentForm <- fillForm[ProposedInvestmentModel](KeystoreKeys.proposedInvestment, ProposedInvestmentForm.proposedInvestmentForm)
        confirmContactDetailsForm <- fillForm[ConfirmContactDetailsModel](KeystoreKeys.confirmContactDetails, ConfirmContactDetailsForm.confirmContactDetailsForm)
        contactDetailsForm <- fillForm[ContactDetailsModel](KeystoreKeys.manualContactDetails, ContactDetailsForm.contactDetailsForm)
        confirmCorrespondAddressForm <- fillForm[ConfirmCorrespondAddressModel](KeystoreKeys.confirmContactAddress, ConfirmCorrespondAddressForm.confirmCorrespondAddressForm)
        contactAddressForm <- fillForm[AddressModel](KeystoreKeys.manualContactAddress, ContactAddressForm.contactAddressForm)
        hadOtherInvestmentsForm <- fillForm[HadOtherInvestmentsModel](KeystoreKeys.hadOtherInvestments, HadOtherInvestmentsForm.hadOtherInvestmentsForm)
        qualifyBusinessActivityForm <- fillForm[QualifyBusinessActivityModel](KeystoreKeys.isQualifyBusinessActivity, QualifyBusinessActivityForm.qualifyBusinessActivityForm)
        hasInvestmentTradeStarted <- fillForm[HasInvestmentTradeStartedModel](KeystoreKeys.hasInvestmentTradeStarted, HasInvestmentTradeStartedForm.hasInvestmentTradeStartedForm)
        fullTimeEmployeeCount <- fillForm[FullTimeEmployeeCountModel](KeystoreKeys.fullTimeEmployeeCount, FullTimeEmployeeCountForm.fullTimeEmployeeCountForm)
      } yield Ok(
        testOnly.views.html.seis.testEndpointSEISPageOne(
          seventyPercentForm,
          grossAssetsForm,
          shareIssueDateForm,
          natureOfBusinessForm,
          dateOfIncorporationForm,
          tradeStartDateForm,
          isFirstStartDateForm,
          researchStartDateForm,
          hadPreviousRFIForm,
          previousSchemesForm,
          schemes.getOrElse(defaultPreviousSchemesSize),
          proposedInvestmentForm,
          confirmContactDetailsForm,
          contactDetailsForm,
          confirmCorrespondAddressForm,
          contactAddressForm,
          hadOtherInvestmentsForm,
          qualifyBusinessActivityForm,
          hasInvestmentTradeStarted,
          fullTimeEmployeeCount
        )
      )
  }

  def submitPageOne: Action[AnyContent] = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    val seventyPercent = bindForm[SeventyPercentSpentModel](KeystoreKeys.seventyPercentSpent, SeventyPercentSpentForm.seventyPercentSpentForm)
    val grossAssets = bindForm[GrossAssetsModel](KeystoreKeys.grossAssets, GrossAssetsForm.grossAssetsForm)
    val shareIssueDate = bindForm[ShareIssueDateModel](KeystoreKeys.shareIssueDate, ShareIssueDateForm.shareIssueDateForm)
    val natureOfBusiness = bindForm[NatureOfBusinessModel](KeystoreKeys.natureOfBusiness, NatureOfBusinessForm.natureOfBusinessForm)
    val dateOfIncorporation = bindForm[DateOfIncorporationModel](KeystoreKeys.dateOfIncorporation, DateOfIncorporationForm.dateOfIncorporationForm)
    val tradeStartDate = bindForm[TradeStartDateModel](KeystoreKeys.tradeStartDate, TradeStartDateForm.tradeStartDateForm)
    val isFirstTrade = bindForm[IsFirstTradeModel](KeystoreKeys.isFirstTrade, IsFirstTradeForm.isFirstTradeForm)
    val researchStartDate = bindForm[ResearchStartDateModel](KeystoreKeys.researchStartDate, ResearchStartDateForm.researchStartDateForm)
    val hadPreviousRFI = bindForm[HadPreviousRFIModel](KeystoreKeys.hadPreviousRFI, HadPreviousRFIForm.hadPreviousRFIForm)
    val testPreviousSchemes = bindPreviousSchemesForm()
    val proposedInvestment = bindForm[ProposedInvestmentModel](KeystoreKeys.proposedInvestment, ProposedInvestmentForm.proposedInvestmentForm)
    val confirmContactDetails = bindConfirmContactDetails()
    val contactDetails = bindForm[ContactDetailsModel](KeystoreKeys.manualContactDetails, ContactDetailsForm.contactDetailsForm)
    val confirmCorrespondAddress = bindConfirmContactAddress()
    val contactAddress = bindForm[AddressModel](KeystoreKeys.manualContactAddress, ContactAddressForm.contactAddressForm)
    val hadOtherInvestments = bindForm[HadOtherInvestmentsModel](KeystoreKeys.hadOtherInvestments, HadOtherInvestmentsForm.hadOtherInvestmentsForm)
    val qualifyBusinessActivityForm = bindForm[QualifyBusinessActivityModel](KeystoreKeys.isQualifyBusinessActivity,
      QualifyBusinessActivityForm.qualifyBusinessActivityForm)
    val hasInvestmentTradeStarted = bindForm[HasInvestmentTradeStartedModel](KeystoreKeys.hasInvestmentTradeStarted,
      HasInvestmentTradeStartedForm.hasInvestmentTradeStartedForm)
    val fullTimeEmployeeCount = bindForm[FullTimeEmployeeCountModel](KeystoreKeys.fullTimeEmployeeCount,
      FullTimeEmployeeCountForm.fullTimeEmployeeCountForm)
    saveBackLinks()
    saveSchemeType()
    Future.successful(Ok(
      testOnly.views.html.seis.testEndpointSEISPageOne(
        seventyPercent,
        grossAssets,
        shareIssueDate,
        natureOfBusiness,
        dateOfIncorporation,
        tradeStartDate,
        isFirstTrade,
        researchStartDate,
        hadPreviousRFI,
        testPreviousSchemes,
        defaultPreviousSchemesSize,
        proposedInvestment,
        confirmContactDetails,
        contactDetails,
        confirmCorrespondAddress,
        contactAddress,
        hadOtherInvestments,
        qualifyBusinessActivityForm,
        hasInvestmentTradeStarted,
        fullTimeEmployeeCount
      )
    ))
  }

  def showPageTwo: Action[AnyContent] = AuthorisedAndEnrolled.async {
    implicit user => implicit request =>
      for {

        testInvestorModeOptionsForm <- fillForm[TestInvestorModeOptionsModel](keyStoreKeyInvestorModelOptions,
          TestInvestorModeOptionsForm.testInvestorModeOptionsForm)
        numberOfSharesForm <- fillForm[NumberOfSharesModel](KeystoreKeys.numberOfShares, NumberOfSharesForm.numberOfSharesForm)
        nominalValueOfSharesForm <- fillForm[NominalValueOfSharesModel](KeystoreKeys.nominalValueOfShares, NominalValueOfSharesForm.nominalValueOfSharesForm)
        shareDescription <- fillForm[ShareDescriptionModel](KeystoreKeys.shareDescription, ShareDescriptionForm.shareDescriptionForm)
        totalAmountRaisedForm <- fillForm[TotalAmountRaisedModel](KeystoreKeys.totalAmountRaised, TotalAmountRaisedForm.totalAmountRaisedForm)
        totalAmountSpentForm <- fillForm[TotalAmountSpentModel](KeystoreKeys.totalAmountSpent, TotalAmountSpentForm.totalAmountSpentForm)
        wasAnyValueReceivedForm <- fillForm[WasAnyValueReceivedModel](KeystoreKeys.wasAnyValueReceived, WasAnyValueReceivedForm.wasAnyValueReceivedForm)
      } yield Ok(
        testOnly.views.html.seis.testEndpointSEISPageTwo(
          testInvestorModeOptionsForm,
          numberOfSharesForm,
          nominalValueOfSharesForm,
          shareDescription,
          totalAmountRaisedForm,
          totalAmountSpentForm,
          wasAnyValueReceivedForm
        )
      )

  }

  def submitPageTwo: Action[AnyContent] = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    val investorModelOptions = bindForm[TestInvestorModeOptionsModel](keyStoreKeyInvestorModelOptions, TestInvestorModeOptionsForm.testInvestorModeOptionsForm)
    val numberOfShares = bindForm[NumberOfSharesModel](KeystoreKeys.numberOfShares, NumberOfSharesForm.numberOfSharesForm)
    val nominalValueOfShares = bindForm[NominalValueOfSharesModel](KeystoreKeys.nominalValueOfShares, NominalValueOfSharesForm.nominalValueOfSharesForm)
    val shareDescription = bindForm[ShareDescriptionModel](KeystoreKeys.shareDescription, ShareDescriptionForm.shareDescriptionForm)
    val totalAmountRaised = bindForm[TotalAmountRaisedModel](KeystoreKeys.totalAmountRaised, TotalAmountRaisedForm.totalAmountRaisedForm)
    val totalAmountSpent = bindForm[TotalAmountSpentModel](KeystoreKeys.totalAmountSpent, TotalAmountSpentForm.totalAmountSpentForm)
    val wasAnyValueReceived = bindForm[WasAnyValueReceivedModel](KeystoreKeys.wasAnyValueReceived, WasAnyValueReceivedForm.wasAnyValueReceivedForm)

    saveInvestorDetails(populateIvestorTestData(investorModelOptions.value.fold("1")(_.testInvestorModeOptions)))
    saveBackLinks()
    saveSchemeType()
    Future.successful(Ok(
      testOnly.views.html.seis.testEndpointSEISPageTwo(
        investorModelOptions,
        numberOfShares,
        nominalValueOfShares,
        shareDescription,
        totalAmountRaised,
        totalAmountSpent,
        wasAnyValueReceived
      )
    ))
  }


  private def saveInvestorDetails(investorDetails: Vector[InvestorDetailsModel])(implicit hc: HeaderCarrier, user: TAVCUser) = {
    s4lConnector.saveFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails, investorDetails)
  }

  private def populateIvestorTestData(options: String = "1"): Vector[InvestorDetailsModel] = {
    options match {

      // single investor no holdings
      case "1" => InvestorTestHelper.getInvestors(1, 0)

      // Single Investor with 5 holdings"
      case "2" => InvestorTestHelper.getInvestors(1, 5)

      //  5 Investors with 4 holdings each
      case "3" => InvestorTestHelper.getInvestors(5, 4)

      // 5 Investors with 4 holdings each. Last Holding incomplete
      case "4" => InvestorTestHelper.getInvestors(5, 4, includeIncompleteShareHolding = true)

      // 5 Investors with 4 holdings each. Last Investor incomplete (0 holdings)
      case "5" => InvestorTestHelper.getInvestors(5, 4, includeIncompleteInvestor = true)

      // 20 complete Investors with 5 holdings each
      case "6" => InvestorTestHelper.getInvestors(20, 5)

      // 20 complete Investors with 0 holdings each
      case "7" => InvestorTestHelper.getInvestors(20, 0)

      // catch all
      case _ => InvestorTestHelper.getInvestors(1, 5)
    }
  }

  private def saveBackLinks()(implicit hc: HeaderCarrier, user: TAVCUser) = {
    s4lConnector.saveFormData[Boolean](KeystoreKeys.applicationInProgress, true)
    s4lConnector.saveFormData[String](KeystoreKeys.backLinkConfirmCorrespondence, routes.TestEndpointSEISController.showPageOne(None).url)
    s4lConnector.saveFormData[String](KeystoreKeys.backLinkPreviousScheme, routes.TestEndpointSEISController.showPageOne(None).url)
    s4lConnector.saveFormData[String](KeystoreKeys.backLinkProposedInvestment, routes.TestEndpointSEISController.showPageOne(None).url)
    s4lConnector.saveFormData[String](KeystoreKeys.backLinkSupportingDocs, routes.TestEndpointSEISController.showPageOne(None).url)
    s4lConnector.saveFormData[String](KeystoreKeys.backLinkShareIssueDate, routes.TestEndpointSEISController.showPageOne(None).url)
    s4lConnector.saveFormData[String](KeystoreKeys.backLinkSeventyPercentSpent, routes.TestEndpointSEISController.showPageOne(None).url)
    s4lConnector.saveFormData[String](KeystoreKeys.backLinkShareDescription, routes.TestEndpointSEISController.showPageOne(None).url)
    s4lConnector.saveFormData[String](KeystoreKeys.backLinkAddInvestorOrNominee, routes.TestEndpointSEISController.showPageOne(None).url)
    s4lConnector.saveFormData[String](KeystoreKeys.backLinkIsExistingShareHolder, routes.TestEndpointSEISController.showPageTwo().url)

    //Investor Details
    s4lConnector.saveFormData[String](KeystoreKeys.backLinkAddInvestorOrNominee, routes.TestEndpointSEISController.showPageOne(None).url)
    s4lConnector.saveFormData[String](KeystoreKeys.backLinkCompanyOrIndividual, routes.TestEndpointSEISController.showPageTwo().url)
    s4lConnector.saveFormData[String](KeystoreKeys.backLinkCompanyAndIndividualBoth, routes.TestEndpointSEISController.showPageTwo().url)
    s4lConnector.saveFormData[String](KeystoreKeys.backLinkNumberOfSharesPurchased, routes.TestEndpointSEISController.showPageTwo().url)
    s4lConnector.saveFormData[String](KeystoreKeys.backLinkHowMuchSpentOnShares, routes.TestEndpointSEISController.showPageTwo().url)
    s4lConnector.saveFormData[String](KeystoreKeys.backLinkIsExistingShareHolder, routes.TestEndpointSEISController.showPageTwo().url)
    s4lConnector.saveFormData[String](KeystoreKeys.backLinkShareClassAndDescription, routes.TestEndpointSEISController.showPageTwo().url)
    s4lConnector.saveFormData[String](KeystoreKeys.backLinkNumberOfPreviouslyIssuedShares, routes.TestEndpointSEISController.showPageTwo().url)
    s4lConnector.saveFormData[String](KeystoreKeys.backLinkIsPreviousShareHoldingNominalValue, routes.TestEndpointSEISController.showPageTwo().url)
    s4lConnector.saveFormData[String](KeystoreKeys.backLinkInvestorShareIssueDate, routes.TestEndpointSEISController.showPageTwo().url)
  }

  private def saveSchemeType()(implicit hc: HeaderCarrier, user: TAVCUser) = {
    s4lConnector.saveFormData[SchemeTypesModel](KeystoreKeys.selectedSchemes, SchemeTypesModel(seis = true))
  }

  def fillForm[A](s4lKey: String, form: Form[A])(implicit hc: HeaderCarrier, user: TAVCUser, format: Format[A]): Future[Form[A]] = {
    s4lConnector.fetchAndGetFormData[A](s4lKey).map {
      case Some(data) =>
        form.fill(data)
      case None => form
    }
  }

  def fillPreviousSchemesForm(implicit hc: HeaderCarrier, user: TAVCUser): Future[Form[TestPreviousSchemesModel]] = {
    PreviousSchemesHelper.getAllInvestmentFromKeystore(s4lConnector).map {
      data =>
        if(data.nonEmpty) {
          TestPreviousSchemesForm.testPreviousSchemesForm.fill(TestPreviousSchemesModel(Some(data)))
        } else {
          TestPreviousSchemesForm.testPreviousSchemesForm.fill(TestPreviousSchemesModel(None))
        }
    }
  }

  def bindForm[A](s4lKey: String, form: Form[A])(implicit request: Request[AnyContent], user: TAVCUser, format: Format[A]): Form[A] = {
    form.bindFromRequest().fold(
      formWithErrors => {
        formWithErrors
      },
      validFormData => {
        s4lConnector.saveFormData(s4lKey, validFormData)(hc,format,user)
        form.fill(validFormData)
      }
    )
  }

  def bindConfirmContactDetails()(implicit request: Request[AnyContent], user: TAVCUser): Form[ConfirmContactDetailsModel] = {

    def bindContactDetails(useDetails: Boolean)(implicit request: Request[AnyContent], user: TAVCUser) = {
      ContactDetailsForm.contactDetailsForm.bindFromRequest().fold(
        formWithErrors => {
          formWithErrors
        },
        validFormData => {
          if(useDetails) s4lConnector.saveFormData(KeystoreKeys.contactDetails, validFormData)
        }
      )
    }

    ConfirmContactDetailsForm.confirmContactDetailsForm.bindFromRequest().fold(
      formWithErrors => {
        formWithErrors
      },
      validFormData => {
        s4lConnector.saveFormData(KeystoreKeys.confirmContactDetails, validFormData)
        if(validFormData.contactDetailsUse == Constants.StandardRadioButtonYesValue) {
          s4lConnector.saveFormData(KeystoreKeys.contactDetails, validFormData.contactDetails)
          bindContactDetails(useDetails = false)
        } else bindContactDetails(useDetails = true)
        ConfirmContactDetailsForm.confirmContactDetailsForm.fill(validFormData)
      }
    )
  }

  def bindConfirmContactAddress()(implicit request: Request[AnyContent], user: TAVCUser): Form[ConfirmCorrespondAddressModel] = {

    def bindContactAddress(useAddress: Boolean)(implicit request: Request[AnyContent], user: TAVCUser) = {
      ContactAddressForm.contactAddressForm.bindFromRequest().fold(
        formWithErrors => {
          formWithErrors
        },
        validFormData => {
          if(useAddress) s4lConnector.saveFormData(KeystoreKeys.contactAddress, validFormData)
        }
      )
    }

    ConfirmCorrespondAddressForm.confirmCorrespondAddressForm.bindFromRequest().fold(
      formWithErrors => {
        formWithErrors
      },
      validFormData => {
        s4lConnector.saveFormData(KeystoreKeys.confirmContactAddress, validFormData)
        if(validFormData.contactAddressUse == Constants.StandardRadioButtonYesValue) {
          s4lConnector.saveFormData(KeystoreKeys.contactAddress, validFormData.address)
          bindContactAddress(useAddress = false)
        } else bindContactAddress(useAddress = true)
        ConfirmCorrespondAddressForm.confirmCorrespondAddressForm.fill(validFormData)
      }
    )
  }

  def bindPreviousSchemesForm()(implicit request: Request[AnyContent], user: TAVCUser): Form[TestPreviousSchemesModel] = {
    TestPreviousSchemesForm.testPreviousSchemesForm.bindFromRequest().fold(
      formWithErrors => {
        formWithErrors
      },
      validFormData => {
        validFormData.previousSchemes.fold(TestPreviousSchemesForm.testPreviousSchemesForm.fill(validFormData)){
          previousSchemes =>
            s4lConnector.saveFormData(KeystoreKeys.previousSchemes, previousSchemes.toVector)
            TestPreviousSchemesForm.testPreviousSchemesForm.fill(validFormData)
        }
      }
    )
  }

}

object TestEndpointSEISController extends TestEndpointSEISController
{
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
  override lazy val s4lConnector = S4LConnector
}