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
import common.KeystoreKeys
import config.{FrontendAppConfig, FrontendAuthConnector}
import config.FrontendGlobal.internalServerErrorTemplate
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.Helpers.PreviousSchemesHelper
import models._
import models.investorDetails.InvestorDetailsModel
import models.repayments.{AnySharesRepaymentModel, SharesRepaymentDetailsModel}
import models.submission.SchemeTypesModel
import play.api.Logger
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier
import views.html.eis.checkAndSubmit.CheckAnswers
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.mvc.{Action, AnyContent}

import scala.concurrent.Future

object CheckAnswersController extends CheckAnswersController{
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait CheckAnswersController extends FrontendController with AuthorisedAndEnrolledForTAVC with PreviousSchemesHelper {

  override val acceptedFlows = Seq(Seq(EIS))



  def checkAnswersModel(implicit headerCarrier: HeaderCarrier, user: TAVCUser) : Future[CheckAnswersModel] = for {
    registeredAddress <- s4lConnector.fetchAndGetFormData[RegisteredAddressModel](KeystoreKeys.registeredAddress)
    dateOfIncorporation <- s4lConnector.fetchAndGetFormData[DateOfIncorporationModel](KeystoreKeys.dateOfIncorporation)
    natureOfBusiness <- s4lConnector.fetchAndGetFormData[NatureOfBusinessModel](KeystoreKeys.natureOfBusiness)
    commercialSale <- s4lConnector.fetchAndGetFormData[CommercialSaleModel](KeystoreKeys.commercialSale)
    isCompanyKnowledgeIntensive <- s4lConnector.fetchAndGetFormData[IsCompanyKnowledgeIntensiveModel](KeystoreKeys.isCompanyKnowledgeIntensive)
    isKnowledgeIntensive <- s4lConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](KeystoreKeys.isKnowledgeIntensive)
    operatingCosts <- s4lConnector.fetchAndGetFormData[OperatingCostsModel](KeystoreKeys.operatingCosts)
    percentageStaffWithMasters <- s4lConnector.fetchAndGetFormData[PercentageStaffWithMastersModel](KeystoreKeys.percentageStaffWithMasters)
    tenYearPlan <- s4lConnector.fetchAndGetFormData[TenYearPlanModel](KeystoreKeys.tenYearPlan)
    hadPreviousRFI <- s4lConnector.fetchAndGetFormData[HadPreviousRFIModel](KeystoreKeys.hadPreviousRFI)
    previousSchemes <- getAllInvestmentFromKeystore(s4lConnector)
    totalAmountRaised <- s4lConnector.fetchAndGetFormData[TotalAmountRaisedModel](KeystoreKeys.totalAmountRaised)
    thirtyDayRuleModel <- s4lConnector.fetchAndGetFormData[ThirtyDayRuleModel](KeystoreKeys.thirtyDayRule)
    anySharesRepaymentModel <- s4lConnector.fetchAndGetFormData[AnySharesRepaymentModel](KeystoreKeys.anySharesRepayment)
    newGeographicalMarket <- s4lConnector.fetchAndGetFormData[NewGeographicalMarketModel](KeystoreKeys.newGeographicalMarket)
    newProduct <- s4lConnector.fetchAndGetFormData[NewProductModel](KeystoreKeys.newProduct)
    contactDetails <- s4lConnector.fetchAndGetFormData[ContactDetailsModel](KeystoreKeys.contactDetails)
    contactAddress <- s4lConnector.fetchAndGetFormData[AddressModel](KeystoreKeys.contactAddress)
    investmentGrowModel <- s4lConnector.fetchAndGetFormData[InvestmentGrowModel](KeystoreKeys.investmentGrow)
    qualifyBusinessActivity <- s4lConnector.fetchAndGetFormData[QualifyBusinessActivityModel](KeystoreKeys.isQualifyBusinessActivity)
    hasInvestmentTradeStarted <- s4lConnector.fetchAndGetFormData[HasInvestmentTradeStartedModel](KeystoreKeys.hasInvestmentTradeStarted)
    shareIssueDate <- s4lConnector.fetchAndGetFormData[ShareIssueDateModel](KeystoreKeys.shareIssueDate)
    grossAssets <- s4lConnector.fetchAndGetFormData[GrossAssetsModel](KeystoreKeys.grossAssets)
    fullTimeEmployees <- s4lConnector.fetchAndGetFormData[FullTimeEmployeeCountModel](KeystoreKeys.fullTimeEmployeeCount)
    shareDescription <- s4lConnector.fetchAndGetFormData[ShareDescriptionModel](KeystoreKeys.shareDescription)
    numberOfShares <- s4lConnector.fetchAndGetFormData[NumberOfSharesModel](KeystoreKeys.numberOfShares)
    investorDetails <- s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails)
    valueReceived <- s4lConnector.fetchAndGetFormData[WasAnyValueReceivedModel](KeystoreKeys.wasAnyValueReceived)
    shareCapitalChanges <- s4lConnector.fetchAndGetFormData[ShareCapitalChangesModel](KeystoreKeys.shareCapitalChanges)
    marketDescription <- s4lConnector.fetchAndGetFormData[MarketDescriptionModel](KeystoreKeys.marketDescription)
    repaymentDetails <- s4lConnector.fetchAndGetFormData[Vector[SharesRepaymentDetailsModel]](KeystoreKeys.sharesRepaymentDetails)
    grossAssetsAfterIssue <- s4lConnector.fetchAndGetFormData[GrossAssetsAfterIssueModel](KeystoreKeys.grossAssetsAfterIssue)
    researchStartDateModel <- s4lConnector.fetchAndGetFormData[ResearchStartDateModel](KeystoreKeys.researchStartDate)
    turnoverCosts <- s4lConnector.fetchAndGetFormData[AnnualTurnoverCostsModel](KeystoreKeys.turnoverCosts)
  }yield new CheckAnswersModel(registeredAddress,dateOfIncorporation,natureOfBusiness,commercialSale,isCompanyKnowledgeIntensive, isKnowledgeIntensive,
    operatingCosts,percentageStaffWithMasters,tenYearPlan,hadPreviousRFI, previousSchemes, totalAmountRaised,
    thirtyDayRuleModel,anySharesRepaymentModel,newGeographicalMarket,newProduct,contactDetails,contactAddress,
    investmentGrowModel, qualifyBusinessActivity,hasInvestmentTradeStarted, shareIssueDate,grossAssets,fullTimeEmployees,
    shareDescription, numberOfShares,investorDetails,valueReceived,shareCapitalChanges, marketDescription,repaymentDetails,
    grossAssetsAfterIssue,turnoverCosts,researchStartDateModel, applicationConfig.uploadFeatureEnabled)


  def show (envelopeId: Option[String]) : Action[AnyContent]= AuthorisedAndEnrolled.async { implicit user => implicit request =>
    if(envelopeId.fold("")(_.toString).length > 0) {
        s4lConnector.saveFormData(KeystoreKeys.envelopeId, envelopeId.getOrElse(""))
      }

    checkAnswersModel.flatMap {
      checkAnswers =>
        Future.successful(Ok(CheckAnswers(checkAnswers)))
    }.recover {
      case e: Exception => Logger.warn(s"[CheckAnswersController][show] Exception calling checkAnswersModel: ${e.getMessage}")
        InternalServerError(internalServerErrorTemplate)
    }
  }

  val submit = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    Future.successful(Redirect(controllers.eis.routes.DeclarationController.show()))
  }


}
