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

package connectors

import config.{FrontendAppConfig, WSHttp}
import models.registration.RegistrationDetailsModel
import models.submission.{ComplianceStatementAnswersModel, DesSubmissionCSModel, DesSubmitAdvancedAssuranceModel, Submission}
import models._
import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http._

import scala.concurrent.Future

object SubmissionConnector extends SubmissionConnector with ServicesConfig {
  val serviceUrl = FrontendAppConfig.submissionUrl
  override lazy val http = WSHttp
}

trait SubmissionConnector {
  val serviceUrl: String
  val http: HttpGet with HttpPost with HttpPut

  def validateKiCostConditions(operatingCostYear1: Int, operatingCostYear2: Int, operatingCostYear3: Int,
                               rAndDCostsYear1: Int, rAndDCostsYear2: Int, rAndDCostsYear3: Int)
                              (implicit hc: HeaderCarrier): Future[Option[Boolean]] = {

    http.GET[Option[Boolean]](s"$serviceUrl/investment-tax-relief/knowledge-intensive/check-ki-costs/" +
      s"operating-costs/$operatingCostYear1/$operatingCostYear2/$operatingCostYear3/" +
      s"rd-costs/$rAndDCostsYear1/$rAndDCostsYear2/$rAndDCostsYear3")
  }

  def validateSecondaryKiConditions(hasPercentageWithMasters: Boolean,
                                    hasTenYearPlan: Boolean)
                                   (implicit hc: HeaderCarrier): Future[Option[Boolean]] = {

    http.GET[Option[Boolean]](s"$serviceUrl/investment-tax-relief/knowledge-intensive/check-secondary-conditions/has-percentage-with-masters/" +
      s"$hasPercentageWithMasters/has-ten-year-plan/$hasTenYearPlan")
  }

  def checkLifetimeAllowanceExceeded(hadPrevRFI: Boolean, isKi: Boolean, previousInvestmentSchemesTotal: Long,
                                     totalAmountRaised: BigInt)
                                    (implicit hc: HeaderCarrier): Future[Option[Boolean]] = {

    http.GET[Option[Boolean]](s"$serviceUrl/investment-tax-relief/lifetime-allowance/lifetime-allowance-checker/had-previous-rfi/" +
      s"$hadPrevRFI/is-knowledge-intensive/$isKi/previous-schemes-total/$previousInvestmentSchemesTotal/proposed-amount/$totalAmountRaised")

  }

  def checkAnnualLimitExceeded(previousInvestmentSchemesInRangeTotal: Long,
                                     totalAmountRaised: BigInt)
                                    (implicit hc: HeaderCarrier): Future[Option[Boolean]] = {

    http.GET[Option[Boolean]](s"$serviceUrl/investment-tax-relief/compliance-statement/validate-annual-limit/" +
      s"previous-schemes-total-in-range/$previousInvestmentSchemesInRangeTotal/total-amount-raised/$totalAmountRaised")
  }

  def checkAveragedAnnualTurnover(totalAmountRaised: TotalAmountRaisedModel, annualTurnoverCostsModel: AnnualTurnoverCostsModel)
                                 (implicit hc: HeaderCarrier): Future[Option[Boolean]] = {
    http.GET[Option[Boolean]](s"$serviceUrl/investment-tax-relief/averaged-annual-turnover/check-averaged-annual-turnover/" +
      s"proposed-investment-amount/${totalAmountRaised.amount.toLongExact}/annual-turn-over/${annualTurnoverCostsModel.amount1}" +
      s"/${annualTurnoverCostsModel.amount2}/${annualTurnoverCostsModel.amount3}/${annualTurnoverCostsModel.amount4}/${annualTurnoverCostsModel.amount5}")
  }

  def checkGrossAssetsAmountExceeded(schemeType: String, grossAssetAmount: GrossAssetsModel)
                                    (implicit hc: HeaderCarrier): Future[Option[Boolean]] = {

    http.GET[Option[Boolean]](s"$serviceUrl/investment-tax-relief/gross-assets/gross-assets-checker/check-total/gross-amount/" +
      s"$schemeType/${grossAssetAmount.grossAmount.toLongExact}")

  }

  def submitAdvancedAssurance(submissionRequest: Submission, tavcReferenceNumber: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    if(tavcReferenceNumber.isEmpty) {
      Logger.warn("[SubmissionConnector][submitAdvancedAssurance] An empty tavcReferenceNumber was passed")
    }
    require(tavcReferenceNumber.nonEmpty, "[SubmissionConnector][submitAdvancedAssurance] An empty tavcReferenceNumber was passed")

    val json = Json.toJson(submissionRequest)
    val targetSubmissionModel = Json.parse(json.toString()).as[DesSubmitAdvancedAssuranceModel]
    http.POST[JsValue, HttpResponse](s"$serviceUrl/investment-tax-relief/advanced-assurance/$tavcReferenceNumber/submit", Json.toJson(targetSubmissionModel))
  }

  def submitComplianceStatement(submissionRequest: ComplianceStatementAnswersModel, tavcReferenceNumber: String,
                                registrationDetailsModel: Option[RegistrationDetailsModel])(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    if(tavcReferenceNumber.isEmpty) {
      Logger.warn("[SubmissionConnector][submitComplainceStatement] An empty tavcReferenceNumber was passed")
    }
    require(tavcReferenceNumber.nonEmpty, "[SubmissionConnector][submitComplainceStatement] An empty tavcReferenceNumber was passed")

    http.POST[JsValue, HttpResponse](s"$serviceUrl/investment-tax-relief/compliance-statement/$tavcReferenceNumber/submit",
      Json.toJson(DesSubmissionCSModel.readDesSubmissionCSModel(submissionRequest, registrationDetailsModel)))
  }

  def getAASubmissionDetails(tavcReferenceNumber: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    if(tavcReferenceNumber.isEmpty) {
      Logger.warn("[SubmissionConnector][getAASubmissionSummary] An empty tavcReferenceNumber was passed")
    }
    require(tavcReferenceNumber.nonEmpty, "[SubmissionConnector][getAASubmissionSummary] An empty tavcReferenceNumber was passed")

    http.GET[HttpResponse](s"$serviceUrl/investment-tax-relief/advanced-assurance/$tavcReferenceNumber/submission-details")
  }

  def getRegistrationDetails(safeID: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    http.GET[HttpResponse](s"$serviceUrl/investment-tax-relief/registration/registration-details/safeid/$safeID")
  }

  def checkMarketCriteria(newGeographical: Boolean, newProduct: Boolean)(implicit hc: HeaderCarrier): Future[Option[Boolean]] = {
    http.GET[Option[Boolean]](s"$serviceUrl/investment-tax-relief/market-criteria/new-geographical/$newGeographical/new-product/$newProduct")
  }

  def validateTradeStartDateCondition(tradeStartDay: Int, tradeStartMonth: Int, tradeStartYear: Int)(implicit hc: HeaderCarrier): Future[Option[Boolean]] = {
    http.GET[Option[Boolean]](s"$serviceUrl/investment-tax-relief/trade-start-date/validate-trade-start-date/trade-start-day/$tradeStartDay/" +
      s"trade-start-month/$tradeStartMonth/trade-start-year/$tradeStartYear")
  }


  def  validateHasInvestmentTradeStartedCondition(hasInvestmentTradeStartedDay: Int, hasInvestmentTradeStartedMonth: Int, hasInvestmentTradeStartedYear: Int)
                                                (implicit hc: HeaderCarrier): Future[Option[Boolean]] = {
    http.GET[Option[Boolean]](s"$serviceUrl/investment-tax-relief/compliance-statement/has-investment-trade-started/validate-has-investment-trade-started/day" +
      s"/$hasInvestmentTradeStartedDay/month/$hasInvestmentTradeStartedMonth/year/$hasInvestmentTradeStartedYear")
  }

  def validateFullTimeEmployeeCount(schemeType: String, employeeCount: BigDecimal)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    http.GET[HttpResponse](s"$serviceUrl/investment-tax-relief/compliance-statement/full-time-equivalence-check/$schemeType/$employeeCount")
  }

  def checkGrossAssetsAfterIssueAmountExceeded(grossAssetAmount: Long)
                                    (implicit hc: HeaderCarrier): Future[Option[Boolean]] = {

    http.GET[Option[Boolean]](s"$serviceUrl/investment-tax-relief/gross-assets/gross-assets-after-issue-checker/check-total/gross-amount/$grossAssetAmount")

  }
}

