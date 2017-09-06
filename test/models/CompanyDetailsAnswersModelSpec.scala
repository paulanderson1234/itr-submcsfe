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
import models.submission.CompanyDetailsAnswersModel
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class CompanyDetailsAnswersModelSpec extends UnitSpec with MockitoSugar {

  implicit val hc = mock[HeaderCarrier]

  def setupMockConnector(response: Option[Boolean] = None): SubmissionConnector = {
    val connector = mock[SubmissionConnector]
    when(connector.validateHasInvestmentTradeStartedCondition(Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(response))

    connector
  }

  "Calling .validate on CompanyDetailsAnswersModel" should {
    val completeModel = CompanyDetailsAnswersModel(NatureOfBusinessModel(""),
      DateOfIncorporationModel(Some(1), Some(2), Some(2015)),
      QualifyBusinessActivityModel(Constants.qualifyPrepareToTrade),
      Some(HasInvestmentTradeStartedModel("Yes", Some(1), Some(2), Some(2016))),
      Some(ResearchStartDateModel("Yes", Some(1), Some(2), Some(2016))),
      Some(SeventyPercentSpentModel("Yes")),
      ShareIssueDateModel(Some(1), Some(2), Some(2015)),
      GrossAssetsModel(1000),
      FullTimeEmployeeCountModel(1)
    )

    "return a false" when {

      "provided with no TradeStartDate data when the qualifying business activity is trade" in {
        await(completeModel.copy(hasInvestmentTradeStartedModel = None)
          .validate(setupMockConnector())) shouldBe false
      }

      "provided with a missing TradeStartDate when one should be present" in {
        await(completeModel.copy(hasInvestmentTradeStartedModel = Some(HasInvestmentTradeStartedModel("Yes", None, None, None)))
          .validate(setupMockConnector())) shouldBe false
      }

      "provided with an empty TradeStartDate and no SeventyPercentSpentModel" in {
        await(completeModel.copy(hasInvestmentTradeStartedModel = Some(HasInvestmentTradeStartedModel("No", None, None, None)),
          seventyPercentSpentModel = None)
          .validate(setupMockConnector())) shouldBe false
      }

      "provided with a TradeStartDate less than four months ago with no SeventyPercentSpentModel" in {
        await(completeModel.copy(seventyPercentSpentModel = None)
          .validate(setupMockConnector(Some(false)))) shouldBe false
      }

      "provided with no ResearchStartDate data when the qualifying business activity is research" in {
        await(completeModel.copy(qualifyBusinessActivityModel = QualifyBusinessActivityModel(Constants.qualifyResearchAndDevelopment),
          researchStartDateModel = None)
          .validate(setupMockConnector())) shouldBe false
      }

      "provided with a missing ResearchStartDate when one should be present" in {
        await(completeModel.copy(qualifyBusinessActivityModel = QualifyBusinessActivityModel(Constants.qualifyResearchAndDevelopment),
          researchStartDateModel = Some(ResearchStartDateModel("Yes", None, None, None)))
          .validate(setupMockConnector())) shouldBe false
      }

      "provided with an empty ResearchStartDate and no SeventyPercentSpentModel" in {
        await(completeModel.copy(qualifyBusinessActivityModel = QualifyBusinessActivityModel(Constants.qualifyResearchAndDevelopment),
          researchStartDateModel = Some(ResearchStartDateModel("No", None, None, None)),
          seventyPercentSpentModel = None)
          .validate(setupMockConnector())) shouldBe false
      }

      "provided with a ResearchStartDate less than four months ago with no SeventyPercentSpentModel" in {
        await(completeModel.copy(qualifyBusinessActivityModel = QualifyBusinessActivityModel(Constants.qualifyResearchAndDevelopment),
          seventyPercentSpentModel = None)
          .validate(setupMockConnector(Some(false)))) shouldBe false
      }
    }

    "return a true" when {

      "provided with an empty TradeStartDate and a SeventyPercentSpentModel" in {
        await(completeModel.copy(hasInvestmentTradeStartedModel = Some(HasInvestmentTradeStartedModel("No", None, None, None)))
          .validate(setupMockConnector())) shouldBe true
      }

      "provided with a TradeStartDate less than four months ago with a SeventyPercentSpentModel" in {
        await(completeModel
          .validate(setupMockConnector(Some(false)))) shouldBe true
      }

      "provided with a TradeStartDate more than four months ago with no SeventyPercentSpentModel" in {
        await(completeModel.copy(seventyPercentSpentModel = None)
          .validate(setupMockConnector(Some(true)))) shouldBe true
      }

      "provided with an empty ResearchStartDate and a SeventyPercentSpentModel" in {
        await(completeModel.copy(qualifyBusinessActivityModel = QualifyBusinessActivityModel(Constants.qualifyResearchAndDevelopment),
          researchStartDateModel = Some(ResearchStartDateModel("No", None, None, None)))
          .validate(setupMockConnector())) shouldBe true
      }

      "provided with a ResearchStartDate less than four months ago with a SeventyPercentSpentModel" in {
        await(completeModel.copy(qualifyBusinessActivityModel = QualifyBusinessActivityModel(Constants.qualifyResearchAndDevelopment))
          .validate(setupMockConnector(Some(false)))) shouldBe true
      }

      "provided with a ResearchStartDate more than four months ago with no SeventyPercentSpentModel" in {
        await(completeModel.copy(qualifyBusinessActivityModel = QualifyBusinessActivityModel(Constants.qualifyResearchAndDevelopment),
          seventyPercentSpentModel = None)
          .validate(setupMockConnector(Some(true)))) shouldBe true
      }
    }
  }
}
