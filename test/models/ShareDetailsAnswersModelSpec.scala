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

package models

import common.Constants
import connectors.SubmissionConnector
import models.submission.ShareDetailsAnswersModel
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class ShareDetailsAnswersModelSpec extends UnitSpec with MockitoSugar {

  implicit val hc = mock[HeaderCarrier]

  def setupMockConnector(response: Option[Boolean] = None): SubmissionConnector = {
    val connector = mock[SubmissionConnector]
    when(connector.validateHasInvestmentTradeStartedCondition(Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(response))

    connector
  }

  "Calling .validate on CompanyDetailsAnswersModel" should {
    val completeModel = ShareDetailsAnswersModel(
      ShareDescriptionModel(""),
      NumberOfSharesModel(1),
      TotalAmountRaisedModel(1),
      Some(TotalAmountSpentModel(1))
    )

    "return a false" when {

      "provided with no TradeStartDate data when the qualifying business activity is trade" in {
        await(completeModel
          .validate(QualifyBusinessActivityModel(Constants.qualifyPrepareToTrade), None, None, setupMockConnector())) shouldBe false
      }

      "provided with a missing TradeStartDate when one should be present" in {
        await(completeModel
          .validate(QualifyBusinessActivityModel(Constants.qualifyPrepareToTrade),
            Some(HasInvestmentTradeStartedModel("Yes", None, None, None)), None, setupMockConnector())) shouldBe false
      }

      "provided with an empty TradeStartDate and no TotalAmountSpentModel" in {
        await(completeModel.copy(totalAmountSpentModel = None)
          .validate(QualifyBusinessActivityModel(Constants.qualifyPrepareToTrade),
            Some(HasInvestmentTradeStartedModel("No", None, None, None)), None, setupMockConnector())) shouldBe false
      }

      "provided with a TradeStartDate less than four months ago with no TotalAmountSpentModel" in {
        await(completeModel.copy(totalAmountSpentModel = None)
          .validate(QualifyBusinessActivityModel(Constants.qualifyPrepareToTrade),
            Some(HasInvestmentTradeStartedModel("Yes", Some(1), Some(1), Some(2016))), None, setupMockConnector(Some(false)))) shouldBe false
      }

      "provided with no ResearchStartDate data when the qualifying business activity is research" in {
        await(completeModel
          .validate(QualifyBusinessActivityModel(Constants.qualifyResearchAndDevelopment), None, None, setupMockConnector())) shouldBe false
      }

      "provided with a missing ResearchStartDate when one should be present" in {
        await(completeModel
          .validate(QualifyBusinessActivityModel(Constants.qualifyResearchAndDevelopment), None,
            Some(ResearchStartDateModel("Yes", None, None, None)), setupMockConnector())) shouldBe false
      }

      "provided with an empty ResearchStartDate and no TotalAmountSpentModel" in {
        await(completeModel.copy(totalAmountSpentModel = None)
          .validate(QualifyBusinessActivityModel(Constants.qualifyResearchAndDevelopment), None,
            Some(ResearchStartDateModel("No", None, None, None)), setupMockConnector())) shouldBe false
      }

      "provided with a ResearchStartDate less than four months ago with no TotalAmountSpentModel" in {
        await(completeModel.copy(totalAmountSpentModel = None)
          .validate(QualifyBusinessActivityModel(Constants.qualifyResearchAndDevelopment), None,
            Some(ResearchStartDateModel("Yes", Some(1), Some(1), Some(2016))), setupMockConnector(Some(false)))) shouldBe false
      }
    }

    "return a true" when {

      "provided with an empty TradeStartDate and a TotalAmountSpentModel" in {
        await(completeModel
          .validate(QualifyBusinessActivityModel(Constants.qualifyPrepareToTrade),
            Some(HasInvestmentTradeStartedModel("No", None, None, None)), None, setupMockConnector())) shouldBe true
      }

      "provided with a TradeStartDate less than four months ago with a TotalAmountSpentModel" in {
        await(completeModel
          .validate(QualifyBusinessActivityModel(Constants.qualifyPrepareToTrade),
            Some(HasInvestmentTradeStartedModel("Yes", Some(1), Some(1), Some(2016))), None, setupMockConnector(Some(false)))) shouldBe true
      }

      "provided with a TradeStartDate more than four months ago with no TotalAmountSpentModel" in {
        await(completeModel.copy(totalAmountSpentModel = None)
          validate(QualifyBusinessActivityModel(Constants.qualifyPrepareToTrade),
          Some(HasInvestmentTradeStartedModel("Yes", Some(1), Some(1), Some(2016))), None, setupMockConnector(Some(true)))) shouldBe true
      }

      "provided with an empty ResearchStartDate and a TotalAmountSpentModel" in {
        await(completeModel
          .validate(QualifyBusinessActivityModel(Constants.qualifyResearchAndDevelopment), None,
            Some(ResearchStartDateModel("No", None, None, None)), setupMockConnector())) shouldBe true
      }

      "provided with a ResearchStartDate less than four months ago with a TotalAmountSpentModel" in {
        await(completeModel
          .validate(QualifyBusinessActivityModel(Constants.qualifyResearchAndDevelopment), None,
            Some(ResearchStartDateModel("Yes", Some(1), Some(1), Some(2016))), setupMockConnector(Some(false)))) shouldBe true
      }

      "provided with a ResearchStartDate more than four months ago with no TotalAmountSpentModel" in {
        await(completeModel.copy(totalAmountSpentModel = None)
          .validate(QualifyBusinessActivityModel(Constants.qualifyResearchAndDevelopment), None,
          Some(ResearchStartDateModel("Yes", Some(1), Some(1), Some(2016))), setupMockConnector(Some(true)))) shouldBe true
      }
    }
  }
}
