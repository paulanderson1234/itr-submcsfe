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

package controllers.helpers



import auth.{MockAuthConnector, MockConfig, TAVCUser, ggUser}
import common.KeystoreKeys
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.Helpers.ControllerHelpers
import controllers.helpers.BaseSpec
import models.{HasInvestmentTradeStartedModel, QualifyBusinessActivityModel, ResearchStartDateModel, TradeStartDateModel}
import models.fileUpload.{EnvelopeFile, Metadata}
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers.{redirectLocation, _}
import services.FileUploadService
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.http.logging.SessionId
import views.html.seis.companyDetails.QualifyBusinessActivity_Scope0.QualifyBusinessActivity_Scope1.QualifyBusinessActivity

import scala.concurrent.Future

class ControllerHelpersSpec extends BaseSpec {

  implicit val user: TAVCUser = TAVCUser(ggUser.allowedAuthContext, internalId)

  object controllerHelper extends ControllerHelpers {
  }

  def setupMocks(qualifyBusinessActivityModel: Option[QualifyBusinessActivityModel] = None,
                 researchStartDateModel: Option[ResearchStartDateModel] = None,
                 tradeStartDateModel: Option[HasInvestmentTradeStartedModel] = None
                ): Unit = {

    when(mockS4lConnector.fetchAndGetFormData[QualifyBusinessActivityModel](Matchers.eq(KeystoreKeys.isQualifyBusinessActivity))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(qualifyBusinessActivityModel))

    when(mockS4lConnector.fetchAndGetFormData[ResearchStartDateModel](Matchers.eq(KeystoreKeys.researchStartDate))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(researchStartDateModel))

    when(mockS4lConnector.fetchAndGetFormData[HasInvestmentTradeStartedModel](Matchers.eq(KeystoreKeys.hasInvestmentTradeStarted))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(tradeStartDateModel))


    when(mockS4lConnector.saveFormData(Matchers.eq(KeystoreKeys.backLinkShareIssueDate),
      Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(CacheMap("", Map())))

  }

  private def validateTradeDateMatchesResearchTestDate(tradeStartDateReturned: Option[HasInvestmentTradeStartedModel],
                                              researchTestStartDate:Option[ResearchStartDateModel]): Boolean = {

    (tradeStartDateReturned.isEmpty && researchTestStartDate.isEmpty) ||
    tradeStartDateReturned.nonEmpty && researchTestStartDate.nonEmpty &&
      tradeStartDateReturned.get.hasInvestmentTradeStarted == researchTestStartDate.get.hasStartedResearch &&
      tradeStartDateReturned.get.hasInvestmentTradeStartedDay == researchTestStartDate.get.researchStartDay &&
      tradeStartDateReturned.get.hasInvestmentTradeStartedMonth == researchTestStartDate.get.researchStartMonth &&
      tradeStartDateReturned.get.hasInvestmentTradeStartedYear == researchTestStartDate.get.researchStartYear
  }

  "ControllerHelpers.getTradeStartDate" should {
    "return None if no qualifyBusinessActivityModel exists in S4L" in {
      setupMocks(None, None, None)
      val result = controllerHelper.getTradeStartDateForBusinessActivity(mockS4lConnector)
      await(result) shouldBe None
    }
    "return None if no qualifyBusinessActivityModel exists even if the trade and research dates are present in S4L" in {
      setupMocks(None, Some(researchStartDateModelYes), Some(hasInvestmentTradeStartedModelNo))
      val result = controllerHelper.getTradeStartDateForBusinessActivity(mockS4lConnector)
      await(result) shouldBe None
    }
    "return None if no qualifyBusinessActivityModel exists even if the trade date is present in S4L" in {
      setupMocks(None, None, Some(hasInvestmentTradeStartedModelNo))
      val result = controllerHelper.getTradeStartDateForBusinessActivity(mockS4lConnector)
      await(result) shouldBe None
    }
    "return None if no qualifyBusinessActivityModel exists even if the research date is present in S4L" in {
      setupMocks(None, Some(researchStartDateModelYes), None)
      val result = controllerHelper.getTradeStartDateForBusinessActivity(mockS4lConnector)
      await(result) shouldBe None
    }
    "return None if qualifyBusinessActivityModel trade exists in S4L but start date or research date are not present in S4L" in {
      setupMocks(Some(qualifyTrade), None, None)
      val result = controllerHelper.getTradeStartDateForBusinessActivity(mockS4lConnector)
      await(result) shouldBe None
    }
    "return None if qualifyBusinessActivityModel research exists in S4L but start date or research date are not present in S4L" in {
      setupMocks(Some(qualifyResearchAndDevelopment), None, None)
      val result = controllerHelper.getTradeStartDateForBusinessActivity(mockS4lConnector)
      await(result) shouldBe None
    }
    "return the trade start date if the qualifyBusinessActivityModel exists and set to Trade in S4L" in {
      setupMocks(Some(qualifyTrade), None, Some(hasInvestmentTradeStartedModelYes))
      val result = controllerHelper.getTradeStartDateForBusinessActivity(mockS4lConnector)
      await(result) shouldEqual Some(hasInvestmentTradeStartedModelYes)
    }
    "return the trade start date if the qualifyBusinessActivityModel exists and set to Trade but both dates are in S4L" in {
      setupMocks(Some(qualifyTrade),  Some(researchStartDateModelYes), Some(hasInvestmentTradeStartedModelYes))
      val result = controllerHelper.getTradeStartDateForBusinessActivity(mockS4lConnector)
      await(result) shouldEqual Some(hasInvestmentTradeStartedModelYes)
    }
    "return the empty trade start date if the qualifyBusinessActivityModel in S4L is trade but it hasn't started yet" in {
      setupMocks(Some(qualifyTrade), None, Some(hasInvestmentTradeStartedModelNo))
      val result = controllerHelper.getTradeStartDateForBusinessActivity(mockS4lConnector)
      await(result) shouldEqual Some(hasInvestmentTradeStartedModelNo)
    }
    "return the research start date if the qualifyBusinessActivityModel exists and set to research and development in S4L" in {
      setupMocks(Some(qualifyResearchAndDevelopment), Some(researchStartDateModelYes), None)
      val result = controllerHelper.getTradeStartDateForBusinessActivity(mockS4lConnector)
      validateTradeDateMatchesResearchTestDate(await(result), Some(researchStartDateModelYes)) shouldBe true
    }
    "return the research start date if the qualifyBusinessActivityModel exists and set to Reserach but both dates are in S4L" in {
      setupMocks(Some(qualifyResearchAndDevelopment), Some(researchStartDateModelYes), None)
      val result = controllerHelper.getTradeStartDateForBusinessActivity(mockS4lConnector)
      validateTradeDateMatchesResearchTestDate(await(result), Some(researchStartDateModelYes)) shouldBe true
    }
    "return the empty research start date if the qualifyBusinessActivityModel in S4L is trade but it hasn't started yet" in {
      setupMocks(Some(qualifyResearchAndDevelopment), Some(researchStartDateModelNo), None)
      val result = controllerHelper.getTradeStartDateForBusinessActivity(mockS4lConnector)
      validateTradeDateMatchesResearchTestDate(await(result), Some(researchStartDateModelNo)) shouldBe true
    }

  }

}
