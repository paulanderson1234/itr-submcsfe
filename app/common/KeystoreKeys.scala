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

package common

object KeystoreKeys extends KeystoreKeys

trait KeystoreKeys {
  // form keys
  val taxpayerReference: String = "companyDetails:taxpayerReference"
  val yourCompanyNeed: String = "introduction:yourCompanyNeed"
  val commercialSale: String = "companyDetails:commercialSale"
  val registeredAddress: String = "introduction:registeredAddress"
  val dateOfIncorporation: String = "companyDetails:dateOfIncorporation"
  val isCompanyKnowledgeIntensive: String = "companyDetails:isCompanyKnowledgeIntensive"
  val isKnowledgeIntensive: String = "companyDetails:isKnowledgeIntensive"
  val subsidiaries: String = "companyDetails:subsidiaries"
  val natureOfBusiness: String = "companyDetails:natureOfBusiness"
  val operatingCosts: String = "companyDetails:operatingCosts"
  val turnoverCosts: String = "companyDetails:turnOverCosts"
  val grossAssets: String = "companyDetails:grossAssets"
  val percentageStaffWithMasters: String = "knowledgeIntensive:percentageStaffWithMasters"
  val tenYearPlan: String = "knowledgeIntensive:tenYearPlan"
  val proposedInvestment: String = "investment:proposedInvestment"
  val usedInvestmentReasonBefore: String = "investment:usedReasonBefore"
  val subsidiariesSpendingInvestment: String = "investment:subsidiariesSpendingInvestment"
  val newProduct: String = "investment:newProduct"
  val newGeographicalMarket: String = "investment:newGeographicalMarket"
  val subsidiariesNinetyOwned: String = "investment:subsidiariesNinetyOwned"
  val hadPreviousRFI: String = "previousInvestmentScheme:hadPreviousRFI"
  val previousBeforeDOFCS: String = "previousInvestmentScheme:previousBeforeDOFCS"
  val investmentGrow: String = "investment:investmentGrow"
  val confirmContactAddress: String = "contactInformation:confirmCorrespondAddress"
  val checkYourAnswers: String = "checkAndSubmit:checkYourAnswers"
  val manualContactAddress: String = "contactInformation:manualCorrespondAddress"
  val manualContactDetails: String = "contactInformation:manualContactDetails"
  val contactDetails: String = "contactInformation:contactDetails"
  val confirmContactDetails: String = "contactInformation:confirmContactDetails"
  val contactAddress: String = "contactInformation:contactAddress"
  val previousSchemes: String = "previousInvestmentScheme:previousInvestmentSchemes"
  val supportingDocumentsUpload: String = "attachments:supportingDocumentsUpload"
  val tradeStartDate: String = "companyDetails:tradeStartDate"
  val researchStartDate: String = "companyDetails:researchStartDate"
  val isFirstTrade: String = "companyDetails:isFirstTrade"
  val hadOtherInvestments: String = "previousInvestmentScheme:hadOtherInvestments"
  val isQualifyBusinessActivity: String = "companyDetails:qualifyBusinessActivity"
  val hasInvestmentTradeStarted: String = "companyDetails:hasInvestmentTradeStarted"
  val shareIssueDate: String = "companyDetails:shareIssueDate"
  val seventyPercentSpent: String = "companyDetails:seventyPercentSpent"
  val fullTimeEmployeeCount: String = "companyDetails:fullTimeEmployeeCount"
  val shareDescription: String = "companyDetails:shareDescription"
  val numberOfShares: String = "shares:numberOfShares"
  val totalAmountSpent: String = "shares:totalAmountSpent"
  val nominalValueOfShares: String = "shares:nominalValueofShares"
  val totalAmountRaised: String = "shares:totalAmountRaised"
  val companyOrIndividual: String = "investors: companyOrIndividual"

  // processing Keys
  val eisSeisProcessingModel: String = "processing:EisSeisProcessingModel"
  val kiProcessingModel: String = "processing:kiProcessingModel"
  val lifeTimeAllowanceExceeded: String = "processing:lifeTimeAllowanceExceeded"
  val envelopeId: String = "processing:envelopeId"

  // registration keys
  val registrationDetails: String = "registration:registrationDetails"

  // Subscription Details keys
  val subscriptionDetails: String = "subscription:subscriptionDetails"

  // backlink keys
  val backLinkSupportingDocs: String = "backLink:SupportingDocs"
  val backLinkNewGeoMarket: String = "backLink:NewGeoMarket"
  val backLinkSubSpendingInvestment: String = "backLink:SubSpendingInvestment"
  val backLinkInvestmentGrow: String = "backLink:InvestmentGrow"
  val backLinkSubsidiaries: String = "backLink:subsidiaries"
  val backLinkPreviousScheme: String = "backLink:previousScheme"
  val backLinkReviewPreviousSchemes: String = "backLink:reviewPreviousSchemes"
  val backLinkProposedInvestment: String = "backLink:proposedInvestment"
  val backLinkIneligibleForKI: String = "backLink:IneligibleForKI"
  val backLinkConfirmCorrespondence: String = "backLink:ConfirmCorrespondenceAddress"
  val backLinkSeventyPercentSpent: String = "backLink:backLinkSeventyPercentSpent"
  val backLinkShareIssueDate: String = "backLink:backLinkShareIssueDate"
  val backLinkHadRFI: String = "backLink:backLinkHadRFI"
  val backLinkShareDescription: String = "backLink:shareDescription"
  val backLinkAddInvestorOrNominee: String = "backLink:AddInvestorOrNominee"

  //application in progress key
  val applicationInProgress: String = "applicationInProgress"

  //scheme selection
  val selectedSchemes: String = "selectedScheme"

  //file upload keys
  val envelopeID: String = "fileUpload:envelopeID"

  //throttling
  val throttlingToken: String = "throttling:token"
  val isFirstTimeUsingService: String = "throttling:isFirstTimeUsingService"
  val groupsAndSubsEligibility: String = "throttling:groupsAndSubsEligibility"
  val acquiredTradeEligibility: String = "throttling:acquiredTradeEligibility"
  val isAgentEligibility: String = "throttling:isAgentEligibility"
  val throttleCheckPassed: String = "throttling:throttleCheckPassed"

  // investors
  val addInvestor: String = "investors:addInvestor"

}
