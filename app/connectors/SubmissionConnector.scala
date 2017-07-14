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
import models.submission.{DesSubmitAdvancedAssuranceModel, Submission}
import models.{AnnualTurnoverCostsModel, GrossAssetsModel, ProposedInvestmentModel}
import play.api.Logger
import play.api.http.Status.OK
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http._
import scala.concurrent.ExecutionContext.Implicits.global
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

  def checkLifetimeAllowanceExceeded(hadPrevRFI: Boolean, isKi: Boolean, previousInvestmentSchemesTotal: Int,
                                     proposedAmount: Int)
                                    (implicit hc: HeaderCarrier): Future[Option[Boolean]] = {

    http.GET[Option[Boolean]](s"$serviceUrl/investment-tax-relief/lifetime-allowance/lifetime-allowance-checker/had-previous-rfi/" +
      s"$hadPrevRFI/is-knowledge-intensive/$isKi/previous-schemes-total/$previousInvestmentSchemesTotal/proposed-amount/$proposedAmount")

  }

  def checkAveragedAnnualTurnover(proposedInvestmentAmount: ProposedInvestmentModel, annualTurnoverCostsModel: AnnualTurnoverCostsModel)
                                 (implicit hc: HeaderCarrier): Future[Option[Boolean]] = {
    http.GET[Option[Boolean]](s"$serviceUrl/investment-tax-relief/averaged-annual-turnover/check-averaged-annual-turnover/" +
      s"proposed-investment-amount/${proposedInvestmentAmount.investmentAmount}/annual-turn-over/${annualTurnoverCostsModel.amount1}" +
      s"/${annualTurnoverCostsModel.amount2}/${annualTurnoverCostsModel.amount3}/${annualTurnoverCostsModel.amount4}/${annualTurnoverCostsModel.amount5}")
  }

  def checkGrossAssetsAmountExceeded(grossAssetAmount: GrossAssetsModel)
                                    (implicit hc: HeaderCarrier): Future[Option[Boolean]] = {

    http.GET[Option[Boolean]](s"$serviceUrl/investment-tax-relief/gross-assets/gross-assets-checker/check-total/gross-amount/" +
      s"${grossAssetAmount.grossAmount.toIntExact}")

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

  def validateFullTimeEmployeeCount(employeeCount: BigDecimal)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    http.GET[HttpResponse](s"$serviceUrl/investment-tax-relief/compliance-statement/full-time-equivalence-check/$employeeCount")
  }

}

