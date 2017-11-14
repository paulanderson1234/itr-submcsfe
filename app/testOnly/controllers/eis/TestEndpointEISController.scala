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

package testOnly.controllers.eis

import auth.{AuthorisedAndEnrolledForTAVC, TAVCUser}
import common.{Constants, KeystoreKeys}
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.Helpers.PreviousSchemesHelper
import models._
import models.investorDetails.InvestorDetailsModel
import forms._
import models.repayments.{AnySharesRepaymentModel, SharesRepaymentDetailsModel}
import models.submission.SchemeTypesModel
import play.api.data.Form
import play.api.libs.json.Format
import play.api.mvc.{Action, AnyContent, Request}
import testOnly.models._
import testOnly.forms._
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import testOnly.controllers.{InvestorTestHelper, ShareRepaymentsTestHelper}

import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier

trait TestEndpointEISController extends FrontendController with AuthorisedAndEnrolledForTAVC {

  override val acceptedFlows = Seq()

  val s4lConnector: S4LConnector
  val defaultPreviousSchemesSize = 2
  val keyStoreKeyInvestorModelOptions = "testonly:keyStoreKeyInvestorModelOptions"
  val keyStoreKeyShareRepaymentsModelOptions = "testonly:keyStoreKeyShareRepaymentsModelOptions"

  val kiProcessingModelYes = KiProcessingModel(Some(true), Some(true), Some(true), Some(true), Some(true), Some(true), Some(true))
  val kiProcessingModelNo = KiProcessingModel(Some(false), Some(false), Some(false), Some(false), Some(false), Some(false), Some(false))

  def showPageOne: Action[AnyContent] = AuthorisedAndEnrolled.async {
    implicit user => implicit request =>

      s4lConnector.saveFormData(KeystoreKeys.kiProcessingModel, kiProcessingModelNo)
      for {
        natureOfBusinessForm <- fillForm[NatureOfBusinessModel](KeystoreKeys.natureOfBusiness, NatureOfBusinessForm.natureOfBusinessForm)
        dateOfIncorporationForm <- fillForm[DateOfIncorporationModel](KeystoreKeys.dateOfIncorporation, DateOfIncorporationForm.dateOfIncorporationForm)
        qualifyingBusinessForm <- fillForm[QualifyBusinessActivityModel](KeystoreKeys.isQualifyBusinessActivity,
          QualifyBusinessActivityForm.qualifyBusinessActivityForm)
        tradeStartDateForm <- fillForm[HasInvestmentTradeStartedModel](KeystoreKeys.hasInvestmentTradeStarted, HasInvestmentTradeStartedForm.hasInvestmentTradeStartedForm)
        researchStartDateForm <- fillForm[ResearchStartDateModel](KeystoreKeys.researchStartDate, ResearchStartDateForm.researchStartDateForm)
        commercialSaleForm <- fillForm[CommercialSaleModel](KeystoreKeys.commercialSale, CommercialSaleForm.commercialSaleForm)
        shareIssueDateForm <- fillForm[ShareIssueDateModel](KeystoreKeys.shareIssueDate, ShareIssueDateForm.shareIssueDateForm)
        grossAssetsBeforeForm <- fillForm[GrossAssetsModel](KeystoreKeys.grossAssets, GrossAssetsForm.grossAssetsForm)
        grossAssetsAfterForm <- fillForm[GrossAssetsAfterIssueModel](KeystoreKeys.grossAssetsAfterIssue, GrossAssetsAfterIssueForm.grossAssetsAfterIssueForm)
        isCompanyKnowledgeIntensiveForm <- fillForm[IsCompanyKnowledgeIntensiveModel](KeystoreKeys.isCompanyKnowledgeIntensive, IsCompanyKnowledgeIntensiveForm.isCompanyKnowledgeIntensiveForm)
        isKnowledgeIntensiveForm <- fillForm[IsKnowledgeIntensiveModel](KeystoreKeys.isKnowledgeIntensive, IsKnowledgeIntensiveForm.isKnowledgeIntensiveForm)
        operatingCostsForm <- fillForm[OperatingCostsModel](KeystoreKeys.operatingCosts, TestOperatingCostsForm.testOperatingCostsForm)
        percentageStaffWithMastersForm <- fillForm[PercentageStaffWithMastersModel](KeystoreKeys.percentageStaffWithMasters,
          PercentageStaffWithMastersForm.percentageStaffWithMastersForm)
        tenYearPlanForm <- fillForm[TenYearPlanModel](KeystoreKeys.tenYearPlan, TenYearPlanForm.tenYearPlanForm)

      } yield Ok(
        testOnly.views.html.eis.testEndpointEISPageOne(
          natureOfBusinessForm,
          dateOfIncorporationForm,
          qualifyingBusinessForm,
          tradeStartDateForm,
          researchStartDateForm,
          commercialSaleForm,
          shareIssueDateForm,
          grossAssetsBeforeForm,
          grossAssetsAfterForm,
          isCompanyKnowledgeIntensiveForm,
          isKnowledgeIntensiveForm,
          operatingCostsForm,
          percentageStaffWithMastersForm,
          tenYearPlanForm

        )
      )
  }

  def showPageTwo(schemes: Option[Int]): Action[AnyContent] = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    for {
      fullTimeEmployeeCountForm <- fillForm[FullTimeEmployeeCountModel](KeystoreKeys.fullTimeEmployeeCount,FullTimeEmployeeCountForm.fullTimeEmployeeCountForm)
      hadPreviousRFIForm <- fillForm[HadPreviousRFIModel](KeystoreKeys.hadPreviousRFI, HadPreviousRFIForm.hadPreviousRFIForm)
      previousSchemesForm <- fillPreviousSchemesForm
      hadOtherInvestmentsForm <- fillForm[HadOtherInvestmentsModel](KeystoreKeys.hadOtherInvestments, HadOtherInvestmentsForm.hadOtherInvestmentsForm)
      shareDescriptionForm <- fillForm[ShareDescriptionModel](KeystoreKeys.shareDescription,ShareDescriptionForm.shareDescriptionForm)
      numberOfSharesForm <- fillForm[NumberOfSharesModel](KeystoreKeys.numberOfShares,NumberOfSharesForm.numberOfSharesForm)
      totalAmountRaisedForm <- fillForm[TotalAmountRaisedModel](KeystoreKeys.totalAmountRaised, TotalAmountRaisedForm.totalAmountRaisedForm)
      usedInvestmentReasonBeforeForm <- fillForm[UsedInvestmentReasonBeforeModel](KeystoreKeys.usedInvestmentReasonBefore,
        UsedInvestmentReasonBeforeForm.usedInvestmentReasonBeforeForm)
      previousBeforeDoFCSForm <- fillForm[PreviousBeforeDOFCSModel](KeystoreKeys.previousBeforeDOFCS, PreviousBeforeDOFCSForm.previousBeforeDOFCSForm)
      newGeographicalMarketForm <- fillForm[NewGeographicalMarketModel](KeystoreKeys.newGeographicalMarket, NewGeographicalMarketForm.newGeographicalMarketForm)
      newProductForm <- fillForm[NewProductModel](KeystoreKeys.newProduct, NewProductForm.newProductForm)
      turnoverCostsForm <- fillForm[AnnualTurnoverCostsModel](KeystoreKeys.turnoverCosts, TestTurnoverCostsForm.testTurnoverCostsForm)
      thirtyDayRuleForm <- fillForm[ThirtyDayRuleModel](KeystoreKeys.thirtyDayRule, ThirtyDayRuleForm.thirtyDayRuleForm)
      marketDescriptionForm <- fillForm[MarketDescriptionModel](KeystoreKeys.marketDescription, MarketDescriptionForm.marketDescriptionForm)
      investmentGrowForm <- fillForm[InvestmentGrowModel](KeystoreKeys.investmentGrow, InvestmentGrowForm.investmentGrowForm)
      testInvestorModeOptionsForm <- fillForm[TestInvestorModeOptionsModel](keyStoreKeyInvestorModelOptions,
        TestInvestorModeOptionsForm.testInvestorModeOptionsForm)
    } yield Ok(
      testOnly.views.html.eis.testEndpointEISPageTwo(
        fullTimeEmployeeCountForm,
        hadPreviousRFIForm,
        previousSchemesForm,
        schemes.getOrElse(defaultPreviousSchemesSize),
        hadOtherInvestmentsForm,
        shareDescriptionForm,
        numberOfSharesForm,
        totalAmountRaisedForm,
        usedInvestmentReasonBeforeForm,
        previousBeforeDoFCSForm,
        newGeographicalMarketForm,
        newProductForm,
        turnoverCostsForm,
        thirtyDayRuleForm,
        marketDescriptionForm,
        investmentGrowForm,
        testInvestorModeOptionsForm
      )
    )
  }


  def showPageThree: Action[AnyContent] = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    for {
      anySharesRepaymentForm <- fillForm[AnySharesRepaymentModel](KeystoreKeys.anySharesRepayment,AnySharesRepaymentForm.anySharesRepaymentForm)
      testShareRepaymentsModeOptionsForm <- fillForm[TestShareRepaymentsOptionsModel](keyStoreKeyShareRepaymentsModelOptions,
        TestShareRepaymentsOptionsForm.testShareRepaymentsOptionsForm)
      valueReceivedForm <- fillForm[WasAnyValueReceivedModel](KeystoreKeys.wasAnyValueReceived,WasAnyValueReceivedForm.wasAnyValueReceivedForm)
      shareLoanCapitalForm <- fillForm[ShareCapitalChangesModel](KeystoreKeys.shareCapitalChanges,ShareCapitalChangesForm.shareCapitalChangesForm)
      confirmContactDetailsForm <- fillForm[ConfirmContactDetailsModel](KeystoreKeys.confirmContactDetails, ConfirmContactDetailsForm.confirmContactDetailsForm)
      contactDetailsForm <- fillForm[ContactDetailsModel](KeystoreKeys.manualContactDetails, ContactDetailsForm.contactDetailsForm)
      confirmCorrespondAddressForm <- fillForm[ConfirmCorrespondAddressModel](KeystoreKeys.confirmContactAddress,
        ConfirmCorrespondAddressForm.confirmCorrespondAddressForm)
      contactAddressForm <- fillForm[AddressModel](KeystoreKeys.manualContactAddress, ContactAddressForm.contactAddressForm)

    } yield Ok(
      testOnly.views.html.eis.testEndpointEISPageThree(
        anySharesRepaymentForm,
        testShareRepaymentsModeOptionsForm,
        valueReceivedForm,
        shareLoanCapitalForm,
        confirmContactDetailsForm,
        contactDetailsForm,
        confirmCorrespondAddressForm,
        contactAddressForm
      )
    )
  }


  def submitPageOne: Action[AnyContent] = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    val natureOfBusiness = bindForm[NatureOfBusinessModel](KeystoreKeys.natureOfBusiness, NatureOfBusinessForm.natureOfBusinessForm)
    val dateOfIncorporation = bindForm[DateOfIncorporationModel](KeystoreKeys.dateOfIncorporation, DateOfIncorporationForm.dateOfIncorporationForm)
    val qualifyBusinessActivity = bindForm[QualifyBusinessActivityModel](KeystoreKeys.isQualifyBusinessActivity,
      QualifyBusinessActivityForm.qualifyBusinessActivityForm)
    val tradeStartDate = bindForm[HasInvestmentTradeStartedModel](KeystoreKeys.hasInvestmentTradeStarted,HasInvestmentTradeStartedForm.hasInvestmentTradeStartedForm)
    val researchStartDate = bindForm[ResearchStartDateModel](KeystoreKeys.researchStartDate, ResearchStartDateForm.researchStartDateForm)
    val commercialSale = bindForm[CommercialSaleModel](KeystoreKeys.commercialSale, CommercialSaleForm.commercialSaleForm)
    val shareIssuDate = bindForm[ShareIssueDateModel](KeystoreKeys.shareIssueDate, ShareIssueDateForm.shareIssueDateForm)
    val grossAssetsBefore = bindForm[GrossAssetsModel](KeystoreKeys.grossAssets, GrossAssetsForm.grossAssetsForm)
    val grossAssetsAfter = bindForm[GrossAssetsAfterIssueModel](KeystoreKeys.grossAssetsAfterIssue, GrossAssetsAfterIssueForm.grossAssetsAfterIssueForm)
    val isCompanyKnowledgeIntensive = bindKIFormOne()
    val isKnowledgeIntensive = bindKIFormTwo()
    val testOperatingCosts = bindForm[OperatingCostsModel](KeystoreKeys.operatingCosts, TestOperatingCostsForm.testOperatingCostsForm)
    val percentageStaffWithMasters = bindForm[PercentageStaffWithMastersModel](KeystoreKeys.percentageStaffWithMasters,
      PercentageStaffWithMastersForm.percentageStaffWithMastersForm)
    val tenYearPlan = bindForm[TenYearPlanModel](KeystoreKeys.tenYearPlan, TenYearPlanForm.tenYearPlanForm)

    saveBackLinks()
    saveSchemeType()
    saveSubsidiaries()

    Future.successful(Ok(
      testOnly.views.html.eis.testEndpointEISPageOne(
        natureOfBusiness,
        dateOfIncorporation,
        qualifyBusinessActivity,
        tradeStartDate,
        researchStartDate,
        commercialSale,
        shareIssuDate,
        grossAssetsBefore,
        grossAssetsAfter,
        isCompanyKnowledgeIntensive,
        isKnowledgeIntensive,
        testOperatingCosts,
        percentageStaffWithMasters,
        tenYearPlan
      )
    ))
  }

  def submitPageTwo: Action[AnyContent] = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    val fullTimeEmployeeCount = bindForm[FullTimeEmployeeCountModel](KeystoreKeys.fullTimeEmployeeCount,FullTimeEmployeeCountForm.fullTimeEmployeeCountForm)
    val hadPreviousRFI = bindForm[HadPreviousRFIModel](KeystoreKeys.hadPreviousRFI, HadPreviousRFIForm.hadPreviousRFIForm)
    val testPreviousSchemes = bindPreviousSchemesForm()
    val hadOtherInvestments = bindForm[HadOtherInvestmentsModel](KeystoreKeys.hadOtherInvestments, HadOtherInvestmentsForm.hadOtherInvestmentsForm)

    val shareDescription = bindForm[ShareDescriptionModel](KeystoreKeys.shareDescription,ShareDescriptionForm.shareDescriptionForm)
    val numberOfShares = bindForm[NumberOfSharesModel](KeystoreKeys.numberOfShares,NumberOfSharesForm.numberOfSharesForm)
    val totalAmountRaised = bindForm[TotalAmountRaisedModel](KeystoreKeys.totalAmountRaised, TotalAmountRaisedForm.totalAmountRaisedForm)
    val usedInvestmentReasonBefore = bindForm[UsedInvestmentReasonBeforeModel](KeystoreKeys.usedInvestmentReasonBefore,
      UsedInvestmentReasonBeforeForm.usedInvestmentReasonBeforeForm)
    val previousBeforeDoFCS = bindForm[PreviousBeforeDOFCSModel](KeystoreKeys.previousBeforeDOFCS, PreviousBeforeDOFCSForm.previousBeforeDOFCSForm)
    val newGeographicalMarket = bindForm[NewGeographicalMarketModel](KeystoreKeys.newGeographicalMarket, NewGeographicalMarketForm.newGeographicalMarketForm)
    val newProduct = bindForm[NewProductModel](KeystoreKeys.newProduct, NewProductForm.newProductForm)
    val testTurnoverCosts = bindForm[AnnualTurnoverCostsModel](KeystoreKeys.turnoverCosts, TestTurnoverCostsForm.testTurnoverCostsForm)
    val thirtyDayRule = bindForm[ThirtyDayRuleModel](KeystoreKeys.thirtyDayRule, ThirtyDayRuleForm.thirtyDayRuleForm)
    val marketDescription = bindForm[MarketDescriptionModel](KeystoreKeys.marketDescription, MarketDescriptionForm.marketDescriptionForm)
    val investmentGrow = bindForm[InvestmentGrowModel](KeystoreKeys.investmentGrow, InvestmentGrowForm.investmentGrowForm)
    val investorModelOptions = bindForm[TestInvestorModeOptionsModel](keyStoreKeyInvestorModelOptions, TestInvestorModeOptionsForm.testInvestorModeOptionsForm)

    saveInvestorDetails(populateIvestorTestData(investorModelOptions.value.fold("1")(_.testInvestorModeOptions)))
    saveSubsidiaries()

    Future.successful(Ok(
      testOnly.views.html.eis.testEndpointEISPageTwo(
        fullTimeEmployeeCount,
        hadPreviousRFI,
        testPreviousSchemes,
        defaultPreviousSchemesSize,
        hadOtherInvestments,
        shareDescription,
        numberOfShares,
        totalAmountRaised,
        usedInvestmentReasonBefore,
        previousBeforeDoFCS,
        newGeographicalMarket,
        newProduct,
        testTurnoverCosts,
        thirtyDayRule,
        marketDescription,
        investmentGrow,
        investorModelOptions
      )
    ))
  }


  def submitPageThree: Action[AnyContent] = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    val anySharesRepayment = bindForm[AnySharesRepaymentModel](KeystoreKeys.anySharesRepayment,AnySharesRepaymentForm.anySharesRepaymentForm)

    val shareRepaymentsModelOptions = bindForm[TestShareRepaymentsOptionsModel](keyStoreKeyShareRepaymentsModelOptions,
      TestShareRepaymentsOptionsForm.testShareRepaymentsOptionsForm)

    saveShareRepaymentsDetails(populateShareRepaymentsTestData(shareRepaymentsModelOptions.value.fold("1")(_.testShareRepaymentsOptionsModel)))

    val valueReceived = bindForm[WasAnyValueReceivedModel](KeystoreKeys.wasAnyValueReceived,WasAnyValueReceivedForm.wasAnyValueReceivedForm)
    val shareLoanCapital = bindForm[ShareCapitalChangesModel](KeystoreKeys.shareCapitalChanges,ShareCapitalChangesForm.shareCapitalChangesForm)
    val confirmContactDetails = bindConfirmContactDetails()
    val contactDetails = bindForm[ContactDetailsModel](KeystoreKeys.manualContactDetails, ContactDetailsForm.contactDetailsForm)
    val confirmCorrespondAddress = bindConfirmContactAddress()
    val contactAddress = bindForm[AddressModel](KeystoreKeys.manualContactAddress, ContactAddressForm.contactAddressForm)
    saveBackLinks()
    saveSchemeType()
    saveDoUpload()
    saveSubsidiaries()
    Future.successful(Ok(
      testOnly.views.html.eis.testEndpointEISPageThree(
        anySharesRepayment,
        shareRepaymentsModelOptions,
        valueReceived,
        shareLoanCapital,
        confirmContactDetails,
        contactDetails,
        confirmCorrespondAddress,
        contactAddress
      )
    ))
  }


  private def saveInvestorDetails(investorDetails: Vector[InvestorDetailsModel])(implicit hc: HeaderCarrier, user: TAVCUser) = {
    s4lConnector.saveFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails, investorDetails)
  }

  private def saveShareRepaymentsDetails(shareRepaymentsDetails: Vector[SharesRepaymentDetailsModel])(implicit hc: HeaderCarrier, user: TAVCUser) = {
    s4lConnector.saveFormData[Vector[SharesRepaymentDetailsModel]](KeystoreKeys.sharesRepaymentDetails, shareRepaymentsDetails)
  }

  private def saveSubsidiaries()(implicit hc: HeaderCarrier, user: TAVCUser) = {
    s4lConnector.saveFormData[SubsidiariesModel](KeystoreKeys.subsidiaries, SubsidiariesModel(Constants.StandardRadioButtonNoValue))
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

  private def populateShareRepaymentsTestData(options: String = "1"): Vector[SharesRepaymentDetailsModel] = {
    options match {

      // Single Complete Share Repayment
      case "1" => ShareRepaymentsTestHelper.getShareRepayments(1)

      // Single Incomplete Share Repayment"
      case "2" => ShareRepaymentsTestHelper.getShareRepayments(1, includeIncompleteShareRepayment = true)

      //  5 Complete Share Repayments
      case "3" => ShareRepaymentsTestHelper.getShareRepayments(5)

      // 5 Share Repayments (4 Complete and 1 Incomplete)
      case "4" => ShareRepaymentsTestHelper.getShareRepayments(5, includeIncompleteShareRepayment = true)

      // catch all
      case _ => ShareRepaymentsTestHelper.getShareRepayments(1)
    }
  }


  private def saveBackLinks()(implicit hc: HeaderCarrier, user: TAVCUser) = {
    s4lConnector.saveFormData[Boolean](KeystoreKeys.applicationInProgress, true)
    s4lConnector.saveFormData[String](KeystoreKeys.backLinkFullTimeEmployeeCount, routes.TestEndpointEISController.showPageOne.url)
    s4lConnector.saveFormData[String](KeystoreKeys.backLinkShareDescription, routes.TestEndpointEISController.showPageOne.url)
    s4lConnector.saveFormData[String](KeystoreKeys.backLinkMarketDescription, routes.TestEndpointEISController.showPageOne.url)
    s4lConnector.saveFormData[String](KeystoreKeys.backLinkConfirmCorrespondence, routes.TestEndpointEISController.showPageOne.url)
    s4lConnector.saveFormData[String](KeystoreKeys.backLinkIneligibleForKI, routes.TestEndpointEISController.showPageOne.url)
    s4lConnector.saveFormData[String](KeystoreKeys.backLinkInvestmentGrow, routes.TestEndpointEISController.showPageOne.url)
    s4lConnector.saveFormData[String](KeystoreKeys.backLinkNewGeoMarket, routes.TestEndpointEISController.showPageOne.url)
    s4lConnector.saveFormData[String](KeystoreKeys.backLinkPreviousScheme, routes.TestEndpointEISController.showPageOne.url)
    s4lConnector.saveFormData[String](KeystoreKeys.backLinkProposedInvestment, routes.TestEndpointEISController.showPageOne.url)
    s4lConnector.saveFormData[String](KeystoreKeys.backLinkReviewPreviousSchemes, routes.TestEndpointEISController.showPageOne.url)
    s4lConnector.saveFormData[String](KeystoreKeys.backLinkSubsidiaries, routes.TestEndpointEISController.showPageOne.url)
    s4lConnector.saveFormData[String](KeystoreKeys.backLinkSubSpendingInvestment, routes.TestEndpointEISController.showPageOne.url)
    s4lConnector.saveFormData[String](KeystoreKeys.backLinkSupportingDocs, routes.TestEndpointEISController.showPageOne.url)

    //Investor Details
    s4lConnector.saveFormData[String](KeystoreKeys.backLinkAddInvestorOrNominee, routes.TestEndpointEISController.showPageTwo(None).url)
    s4lConnector.saveFormData[String](KeystoreKeys.backLinkCompanyOrIndividual, routes.TestEndpointEISController.showPageTwo(None).url)
    s4lConnector.saveFormData[String](KeystoreKeys.backLinkCompanyAndIndividualBoth, routes.TestEndpointEISController.showPageTwo(None).url)
    s4lConnector.saveFormData[String](KeystoreKeys.backLinkNumberOfSharesPurchased, routes.TestEndpointEISController.showPageTwo(None).url)
    s4lConnector.saveFormData[String](KeystoreKeys.backLinkHowMuchSpentOnShares, routes.TestEndpointEISController.showPageTwo(None).url)
    s4lConnector.saveFormData[String](KeystoreKeys.backLinkIsExistingShareHolder, routes.TestEndpointEISController.showPageTwo(None).url)
    s4lConnector.saveFormData[String](KeystoreKeys.backLinkShareClassAndDescription, routes.TestEndpointEISController.showPageTwo(None).url)
    s4lConnector.saveFormData[String](KeystoreKeys.backLinkNumberOfPreviouslyIssuedShares, routes.TestEndpointEISController.showPageTwo(None).url)
    s4lConnector.saveFormData[String](KeystoreKeys.backLinkIsPreviousShareHoldingNominalValue, routes.TestEndpointEISController.showPageTwo(None).url)
    s4lConnector.saveFormData[String](KeystoreKeys.backLinkInvestorShareIssueDate, routes.TestEndpointEISController.showPageTwo(None).url)

    //Share Repayments
    s4lConnector.saveFormData[String](KeystoreKeys.backLinkSharesRepaymentAmount, routes.TestEndpointEISController.showPageThree().url)
    s4lConnector.saveFormData[String](KeystoreKeys.backLinkSharesRepaymentDate, routes.TestEndpointEISController.showPageThree().url)
    s4lConnector.saveFormData[String](KeystoreKeys.backLinkSharesRepaymentType, routes.TestEndpointEISController.showPageThree().url)
    s4lConnector.saveFormData[String](KeystoreKeys.backLinkWhoRepaidShares, routes.TestEndpointEISController.showPageThree().url)

  }

  private def saveDoUpload()(implicit hc: HeaderCarrier, user: TAVCUser) = {
    s4lConnector.saveFormData[SupportingDocumentsUploadModel](KeystoreKeys.supportingDocumentsUpload,
      SupportingDocumentsUploadModel(Constants.StandardRadioButtonNoValue))
  }

  private def saveSchemeType()(implicit hc: HeaderCarrier, user: TAVCUser) = {
    s4lConnector.saveFormData[SchemeTypesModel](KeystoreKeys.selectedSchemes, SchemeTypesModel(eis = true))
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
          if (useAddress) s4lConnector.saveFormData(KeystoreKeys.contactAddress, validFormData)
        }
      )
    }

    ConfirmCorrespondAddressForm.confirmCorrespondAddressForm.bindFromRequest().fold(
      formWithErrors => {
        formWithErrors
      },
      validFormData => {
        s4lConnector.saveFormData(KeystoreKeys.confirmContactAddress, validFormData)
        if (validFormData.contactAddressUse == Constants.StandardRadioButtonYesValue) {
          s4lConnector.saveFormData(KeystoreKeys.contactAddress, validFormData.address)
          bindContactAddress(useAddress = false)
        } else bindContactAddress(useAddress = true)
        ConfirmCorrespondAddressForm.confirmCorrespondAddressForm.fill(validFormData)
      }
    )
  }


  def bindKIFormOne()(implicit request: Request[AnyContent], user: TAVCUser): Form[IsCompanyKnowledgeIntensiveModel] = {
    IsCompanyKnowledgeIntensiveForm.isCompanyKnowledgeIntensiveForm.bindFromRequest().fold(
      formWithErrors => {
        formWithErrors
      },
      validFormData => {
        if(validFormData.isCompanyKnowledgeIntensive == Constants.StandardRadioButtonYesValue)  {
          s4lConnector.saveFormData(KeystoreKeys.kiProcessingModel, kiProcessingModelYes)
          s4lConnector.saveFormData(KeystoreKeys.isCompanyKnowledgeIntensive, validFormData)
        } else {
          s4lConnector.saveFormData(KeystoreKeys.kiProcessingModel, kiProcessingModelNo)
          s4lConnector.saveFormData(KeystoreKeys.isCompanyKnowledgeIntensive, validFormData)
        }
        IsCompanyKnowledgeIntensiveForm.isCompanyKnowledgeIntensiveForm.fill(validFormData)
      }
    )
  }

  def bindKIFormTwo()(implicit request: Request[AnyContent], user: TAVCUser): Form[IsKnowledgeIntensiveModel] = {
    IsKnowledgeIntensiveForm.isKnowledgeIntensiveForm.bindFromRequest().fold(
      formWithErrors => {
        formWithErrors
      },
      validFormData => {
        if(validFormData.isKnowledgeIntensive == Constants.StandardRadioButtonYesValue)  {
          s4lConnector.saveFormData(KeystoreKeys.kiProcessingModel, kiProcessingModelYes)
          s4lConnector.saveFormData(KeystoreKeys.isKnowledgeIntensive, validFormData)
        } else {
          s4lConnector.saveFormData(KeystoreKeys.kiProcessingModel, kiProcessingModelNo)
          s4lConnector.saveFormData(KeystoreKeys.isKnowledgeIntensive, validFormData)
        }
        IsKnowledgeIntensiveForm.isKnowledgeIntensiveForm.fill(validFormData)
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

object TestEndpointEISController extends TestEndpointEISController
{
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
  override lazy val s4lConnector = S4LConnector
}
