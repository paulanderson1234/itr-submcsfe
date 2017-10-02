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

import auth.{TAVCUser, ggUser}
import common.KeystoreKeys
import controllers.Helpers.TotalAmountRaisedHelper
import models.{DateOfIncorporationModel, PreviousSchemeModel, _}
import models.submission.MarketRoutingCheckResult
import org.mockito.Matchers
import org.mockito.Mockito._

import scala.concurrent.Future

class TotalAmountRaisedHelperSpec extends BaseSpec {

  implicit val user: TAVCUser = TAVCUser(ggUser.allowedAuthContext, internalId)

  object totalAmountRaisedHelper extends TotalAmountRaisedHelper {
  }

  def setupMocks(
                  kiProcessingModel: Option[KiProcessingModel] = None,
                  hadPreviousRFIModel: Option[HadPreviousRFIModel] = None,
                  hadOtherInvestmentsModel: Option[HadOtherInvestmentsModel] = None,
                  commercialSaleModel: Option[CommercialSaleModel] = None,
                  subsidiariesModel: Option[SubsidiariesModel] = None,
                  dateOfIncorporationModel: Option[DateOfIncorporationModel] = None,
                  previousSchemeModel: Option[Vector[PreviousSchemeModel]],
                  usedInvestmentReasonBeforeModel: Option[UsedInvestmentReasonBeforeModel] = None,
                  previousBeforeDOFCSModel: Option[PreviousBeforeDOFCSModel] = None): Unit = {

    when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(kiProcessingModel))
    when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(hadPreviousRFIModel))
    when(mockS4lConnector.fetchAndGetFormData[HadOtherInvestmentsModel](Matchers.eq(KeystoreKeys.hadOtherInvestments))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(hadOtherInvestmentsModel))
    when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(commercialSaleModel))
    when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(subsidiariesModel))
    when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(),
      Matchers.any(), Matchers.any())).thenReturn(Future.successful(dateOfIncorporationModel))
    when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(),
      Matchers.any(), Matchers.any())).thenReturn(Future.successful(previousSchemeModel))
    when(mockS4lConnector.fetchAndGetFormData[UsedInvestmentReasonBeforeModel](Matchers.eq(KeystoreKeys.usedInvestmentReasonBefore))(Matchers.any(),
      Matchers.any(), Matchers.any())).thenReturn(Future.successful(usedInvestmentReasonBeforeModel))
    when(mockS4lConnector.fetchAndGetFormData[PreviousBeforeDOFCSModel](Matchers.eq(KeystoreKeys.previousBeforeDOFCS))(Matchers.any(),
      Matchers.any(), Matchers.any())).thenReturn(Future.successful(previousBeforeDOFCSModel))
    mockEnrolledRequest(eisSchemeTypesModel)
  }

  "For a first investment when more than 7 years from " +
    "Commercial sale date when not deemed knowledge intensive the TotalAmountRaisedHelper.checkIfMarketInfoApplies method" should {
    "return the expected MarketRoutingCheckResult (true, true) with extra validation false" in {
      setupMocks(Some(falseKIModel), Some(keyStoreSavedHadPreviousRFINo), Some(hadOtherInvestmentsModelNo), Some(keyStoreSavedCommercialSale7YearsOneDay),
        Some(keyStoreSavedSubsidiariesYes), Some(keyStoreSavedDOI3YearsLessOneDay), Some(previousSchemeTrueKIVectorList), None, None)
      val result = totalAmountRaisedHelper.checkIfMarketInfoApplies(mockS4lConnector)
      await(result) shouldBe MarketRoutingCheckResult(isMarketInfoRoute = true, reasonBeforeValidationRequired = false)
    }
  }


  "For first investment when more than 10 years from " +
    "Commercial sale date and Ki the TotalAmountRaisedHelper.checkIfMarketInfoApplies method" should {
    "return the expected MarketRoutingCheckResult (true, true) with extra validation false" in {
      setupMocks(Some(trueKIModel), Some(keyStoreSavedHadPreviousRFINo), Some(hadOtherInvestmentsModelNo), Some(keyStoreSavedCommercialSale10YearsOneDay),
        Some(keyStoreSavedSubsidiariesYes), Some(keyStoreSavedDOI3YearsLessOneDay), Some(previousSchemeTrueKIVectorList))
      mockEnrolledRequest(eisSchemeTypesModel)
      val result = totalAmountRaisedHelper.checkIfMarketInfoApplies(mockS4lConnector)
      await(result) shouldBe MarketRoutingCheckResult(isMarketInfoRoute = true, reasonBeforeValidationRequired = false)
    }
  }

  "For first investment when NOT more than 7 years from " +
    "Commercial sale date when not deemed knowledge intensive with subsidiaries the TotalAmountRaisedHelper.checkIfMarketInfoApplies method" should {
    "return the expected MarketRoutingCheckResult (false, false)" in {

      setupMocks(Some(falseKIModel), Some(keyStoreSavedHadPreviousRFINo), Some(hadOtherInvestmentsModelNo), Some(keyStoreSavedCommercialSale7YearsLessOneDay),
        Some(keyStoreSavedSubsidiariesYes), Some(keyStoreSavedDOI3YearsLessOneDay), Some(previousSchemeTrueKIVectorList))
      mockEnrolledRequest(eisSchemeTypesModel)
      val result = totalAmountRaisedHelper.checkIfMarketInfoApplies(mockS4lConnector)
      await(result) shouldBe MarketRoutingCheckResult(isMarketInfoRoute = false, reasonBeforeValidationRequired = false)
    }
  }

  "For a first investment when EXACTLY 7 years from " +
    "Commercial sale date when not deemed knowledge intensive with subsidiaries the TotalAmountRaisedHelper.checkIfMarketInfoApplies method" should {
    "return the expected MarketRoutingCheckResult" in {
      setupMocks(Some(falseKIModel), Some(keyStoreSavedHadPreviousRFINo), Some(hadOtherInvestmentsModelNo), Some(keyStoreSavedCommercialSale7Years),
        Some(keyStoreSavedSubsidiariesYes), Some(keyStoreSavedDOI3YearsLessOneDay), Some(previousSchemeTrueKIVectorList), None, None)
      val result = totalAmountRaisedHelper.checkIfMarketInfoApplies(mockS4lConnector)
      await(result) shouldBe MarketRoutingCheckResult(isMarketInfoRoute = false, reasonBeforeValidationRequired = false)
    }
  }

  "For a first investment when NOT more than 10 years from " +
    "Commercial sale date when it is deemed knowledge intensive with subsidiaries the TotalAmountRaisedHelper.checkIfMarketInfoApplies method" should {
    "return the expected MarketRoutingCheckResult" in {
      setupMocks(Some(trueKIModel), Some(keyStoreSavedHadPreviousRFINo), Some(hadOtherInvestmentsModelNo), Some(keyStoreSavedCommercialSale7YearsLessOneDay),
        Some(keyStoreSavedSubsidiariesYes), Some(keyStoreSavedDOI3Years), Some(previousSchemeTrueKIVectorList), None, None)
      val result = totalAmountRaisedHelper.checkIfMarketInfoApplies(mockS4lConnector)
      await(result) shouldBe MarketRoutingCheckResult(isMarketInfoRoute = false, reasonBeforeValidationRequired = false)
    }
  }

  "For first investment when EXACTLY 10 years from " +
    "Commercial sale date when it is deemed knowledge intensive with subsidiaries the TotalAmountRaisedHelper.checkIfMarketInfoApplies method" should {
    "return the expected MarketRoutingCheckResult" in {
      setupMocks(Some(trueKIModel), Some(keyStoreSavedHadPreviousRFINo), Some(hadOtherInvestmentsModelNo), Some(keyStoreSavedCommercialSale7Years),
        Some(keyStoreSavedSubsidiariesYes), Some(keyStoreSavedDOI3Years), Some(previousSchemeTrueKIVectorList), None, None)
      val result = totalAmountRaisedHelper.checkIfMarketInfoApplies(mockS4lConnector)
      await(result) shouldBe MarketRoutingCheckResult(isMarketInfoRoute = false, reasonBeforeValidationRequired = false)
    }
  }

  "For first investment NOT more than 7 years from " +
    "Commercial sale date when it is NOT deemed knowledge intensive and without any subsidiaries the " +
    "TotalAmountRaisedHelper.checkIfMarketInfoApplies method" should {
    "return the expected MarketRoutingCheckResult" in {
      setupMocks(Some(falseKIModel), Some(keyStoreSavedHadPreviousRFINo), Some(hadOtherInvestmentsModelNo), Some(keyStoreSavedCommercialSale7YearsLessOneDay),
        Some(keyStoreSavedSubsidiariesNo), Some(keyStoreSavedDOI3YearsLessOneDay), Some(previousSchemeTrueKIVectorList), None, None)
      val result = totalAmountRaisedHelper.checkIfMarketInfoApplies(mockS4lConnector)
      await(result) shouldBe MarketRoutingCheckResult(isMarketInfoRoute = false, reasonBeforeValidationRequired = false)
    }
  }

  "For first investment EXACTLY 7 years from the Commercial sale date when it is NOT deemed knowledge intensive and without any subsidiaries " +
    "the TotalAmountRaisedHelper.checkIfMarketInfoApplies method" should {
    "return the expected MarketRoutingCheckResult" in {
      setupMocks(Some(falseKIModel), Some(keyStoreSavedHadPreviousRFINo), Some(hadOtherInvestmentsModelNo), Some(keyStoreSavedCommercialSale7Years),
        Some(keyStoreSavedSubsidiariesNo), Some(keyStoreSavedDOI3YearsOneDay), Some(previousSchemeTrueKIVectorList), None, None)
      val result = totalAmountRaisedHelper.checkIfMarketInfoApplies(mockS4lConnector)
      await(result) shouldBe MarketRoutingCheckResult(isMarketInfoRoute = false, reasonBeforeValidationRequired = false)
    }
  }

  "For first investment NOT more than 10 years from the Commercial sale date and when it IS deemed knowledge intensive and without any subsidiaries " +
    "the TotalAmountRaisedHelper.checkIfMarketInfoApplies method" should {
    "return the expected MarketRoutingCheckResult" in {
      setupMocks(Some(trueKIModel), Some(keyStoreSavedHadPreviousRFINo), Some(hadOtherInvestmentsModelNo), Some(keyStoreSavedCommercialSale10YearsLessOneDay),
        Some(keyStoreSavedSubsidiariesNo), Some(keyStoreSavedDOI3Years), Some(previousSchemeTrueKIVectorList), None, None)
      val result = totalAmountRaisedHelper.checkIfMarketInfoApplies(mockS4lConnector)
      await(result) shouldBe MarketRoutingCheckResult(isMarketInfoRoute = false, reasonBeforeValidationRequired = false)
    }
  }

  "For first investment when no commercial sale has been made and when it IS deemed knowledge intensive and has subsidiaries " +
    "the TotalAmountRaisedHelper.checkIfMarketInfoApplies method" should {
    "return the expected MarketRoutingCheckResult" in {
      setupMocks(Some(trueKIModel), Some(keyStoreSavedHadPreviousRFINo), Some(hadOtherInvestmentsModelNo), Some(keyStoreSavedCommercialSaleNo),
        Some(keyStoreSavedSubsidiariesYes), Some(keyStoreSavedDOI3Years), Some(previousSchemeTrueKIVectorList), None, None)
      val result = totalAmountRaisedHelper.checkIfMarketInfoApplies(mockS4lConnector)
      await(result) shouldBe MarketRoutingCheckResult(isMarketInfoRoute = false, reasonBeforeValidationRequired = false)
    }
  }

  "For first investment when no commercial sale has been made and when it IS deemed knowledge intensive and does has subsidiaries " +
    "the TotalAmountRaisedHelper.checkIfMarketInfoApplies method" should {
    "return the expected MarketRoutingCheckResult" in {
      setupMocks(Some(trueKIModel), Some(keyStoreSavedHadPreviousRFINo), Some(hadOtherInvestmentsModelNo), Some(keyStoreSavedCommercialSaleNo),
        Some(keyStoreSavedSubsidiariesYes), Some(keyStoreSavedDOI3Years), Some(previousSchemeTrueKIVectorList), None, None)
      val result = totalAmountRaisedHelper.checkIfMarketInfoApplies(mockS4lConnector)
      await(result) shouldBe MarketRoutingCheckResult(isMarketInfoRoute = false, reasonBeforeValidationRequired = false)
    }
  }

  "For first investment when no commercial sale has been made and when it IS deemed knowledge intensive and does not have subsidiaries " +
    "the TotalAmountRaisedHelper.checkIfMarketInfoApplies method" should {
    "return the expected MarketRoutingCheckResult" in {
      setupMocks(Some(trueKIModel), Some(keyStoreSavedHadPreviousRFINo), Some(hadOtherInvestmentsModelNo), Some(keyStoreSavedCommercialSaleNo),
        Some(keyStoreSavedSubsidiariesNo), Some(keyStoreSavedDOI3Years), Some(previousSchemeTrueKIVectorList), None, None)
      val result = totalAmountRaisedHelper.checkIfMarketInfoApplies(mockS4lConnector)
      await(result) shouldBe MarketRoutingCheckResult(isMarketInfoRoute = false, reasonBeforeValidationRequired = false)
    }
  }

  "For first investment when no commercial sale has been made and when it IS NOT deemed knowledge intensive and does not have subsidiaries " +
    "the TotalAmountRaisedHelper.checkIfMarketInfoApplies method" should {
    "return the expected MarketRoutingCheckResult" in {
      setupMocks(Some(falseKIModel), Some(keyStoreSavedHadPreviousRFINo), Some(hadOtherInvestmentsModelNo), Some(keyStoreSavedCommercialSaleNo),
        Some(keyStoreSavedSubsidiariesNo), Some(keyStoreSavedDOI3YearsLessOneDay), Some(previousSchemeTrueKIVectorList), None, None)
      val result = totalAmountRaisedHelper.checkIfMarketInfoApplies(mockS4lConnector)
      await(result) shouldBe MarketRoutingCheckResult(isMarketInfoRoute = false, reasonBeforeValidationRequired = false)
    }
  }

  "For a first investment when no commercial sale has been made and it IS NOT deemed knowledge intensive and has subsidiaries " +
    "the TotalAmountRaisedHelper.checkIfMarketInfoApplies method" should {
    "return the expected MarketRoutingCheckResult" in {
      setupMocks(Some(falseKIModel), Some(keyStoreSavedHadPreviousRFINo), Some(hadOtherInvestmentsModelNo), Some(keyStoreSavedCommercialSaleNo),
        Some(keyStoreSavedSubsidiariesYes), Some(keyStoreSavedDOI3YearsLessOneDay), Some(previousSchemeTrueKIVectorList), None, None)
      val result = totalAmountRaisedHelper.checkIfMarketInfoApplies(mockS4lConnector)
      await(result) shouldBe MarketRoutingCheckResult(isMarketInfoRoute = false, reasonBeforeValidationRequired = false)
    }
  }

  "For a subsequent investment (used previously) and a commercial sale exists and is Ki and subsidiaries is None and has date that is not in range " +
    "the TotalAmountRaisedHelper.checkIfMarketInfoApplies method" should {
    "return the expected MarketRoutingCheckResult" in {
      setupMocks(Some(trueKIModel), Some(keyStoreSavedHadPreviousRFIYes), Some(hadOtherInvestmentsModelYes), Some(keyStoreSavedCommercialSale1Year),
        subsidiariesModel = None, Some(keyStoreSavedDOI3Years), Some(previousSchemeTrueKIVectorList), None, None)
      val result = totalAmountRaisedHelper.checkIfMarketInfoApplies(mockS4lConnector)
      await(result) shouldBe MarketRoutingCheckResult(isMarketInfoRoute = false, reasonBeforeValidationRequired = false)
    }
  }

  "For a subsequent investment (used previously) and a commercial sale exists and is not Ki and has subsidiaries " +
    "the TotalAmountRaisedHelper.checkIfMarketInfoApplies method" should {
    "return the expected MarketRoutingCheckResult (true, true) as this is a usedinvestmentBefore route (mktInfoYes) " +
      "but usedInvestmentReasonBefore and previousBeforeDOFCSModel are empty so extra validation is required to determine the full route logic" in {
      setupMocks(Some(falseKIModel), Some(keyStoreSavedHadPreviousRFIYes), Some(hadOtherInvestmentsModelYes), Some(keyStoreSavedCommercialSale7YearsOneDay),
        Some(keyStoreSavedSubsidiariesYes), Some(keyStoreSavedDOI3YearsLessOneDay), Some(previousSchemeTrueKIVectorList), None, previousBeforeDOFCSModel = None)
      val result = totalAmountRaisedHelper.checkIfMarketInfoApplies(mockS4lConnector)
      await(result) shouldBe MarketRoutingCheckResult(isMarketInfoRoute = true, reasonBeforeValidationRequired = true)
    }
  }

  "For a subsequent investment (used previously) and a commercial sale exists and is not Ki and has subsidiaries " +
    "(different date of incorp and commercial sale) the TotalAmountRaisedHelper.checkIfMarketInfoApplies method" should {
    "return the expected MarketRoutingCheckResult (true, true) as this is a usedinvestmentBefore route (mktInfoYes) " +
      "but usedInvestmentReasonBefore and previousBeforeDOFCSModel are empty so extra validation is required to determine the full route logic" in {
      setupMocks(Some(falseKIModel), Some(keyStoreSavedHadPreviousRFIYes), Some(hadOtherInvestmentsModelYes), Some(keyStoreSavedCommercialSale10YearsOneDay),
        Some(keyStoreSavedSubsidiariesYes), Some(keyStoreSavedDOI3Years), Some(previousSchemeTrueKIVectorList), None, None)
      val result = totalAmountRaisedHelper.checkIfMarketInfoApplies(mockS4lConnector)
      await(result) shouldBe MarketRoutingCheckResult(isMarketInfoRoute = true, reasonBeforeValidationRequired = true)
    }
  }

  "For a first investment with empty PrevRFI/Date of incorporation that is deemed knowledge intensive and has subsidiaries " +
    "the TotalAmountRaisedHelper.checkIfMarketInfoApplies method" should {
    "return the expected MarketRoutingCheckResult" in {
      setupMocks(Some(trueKIModel), hadPreviousRFIModel = None, Some(hadOtherInvestmentsModelNo), Some(keyStoreSavedCommercialSale7Years),
        Some(keyStoreSavedSubsidiariesYes), dateOfIncorporationModel = None, Some(previousSchemeTrueKIVectorList), None, None)
      val result = totalAmountRaisedHelper.checkIfMarketInfoApplies(mockS4lConnector)
      await(result) shouldBe MarketRoutingCheckResult(isMarketInfoRoute = false, reasonBeforeValidationRequired = false)
    }
  }

  "For a first investment with empty HasOtherInvestments/Date of incorporation that is deemed knowledge intensive and has subsidiaries " +
    "the TotalAmountRaisedHelper.checkIfMarketInfoApplies method" should {
    "return the expected MarketRoutingCheckResult" in {
      setupMocks(Some(trueKIModel), Some(keyStoreSavedHadPreviousRFINo), hadOtherInvestmentsModel = None, Some(keyStoreSavedCommercialSale7Years),
        Some(keyStoreSavedSubsidiariesYes), dateOfIncorporationModel = None, Some(previousSchemeTrueKIVectorList), None, None)
      val result = totalAmountRaisedHelper.checkIfMarketInfoApplies(mockS4lConnector)
      await(result) shouldBe MarketRoutingCheckResult(isMarketInfoRoute = false, reasonBeforeValidationRequired = false)
    }
  }

  "For a first investment with empty Commercial Sale/empty subsidiaries/empty date of incorp that is deemed knowledge " +
    "the TotalAmountRaisedHelper.checkIfMarketInfoApplies method" should {
    "return the expected MarketRoutingCheckResult" in {
      setupMocks(Some(trueKIModel), Some(keyStoreSavedHadPreviousRFIYes), Some(hadOtherInvestmentsModelYes), commercialSaleModel = None,
        subsidiariesModel = None, dateOfIncorporationModel = None, Some(previousSchemeTrueKIVectorList), None, None)
      val result = totalAmountRaisedHelper.checkIfMarketInfoApplies(mockS4lConnector)
      await(result) shouldBe MarketRoutingCheckResult(isMarketInfoRoute = false, reasonBeforeValidationRequired = false)
    }
  }

  "For a first investment with empty Kimodel with subsidiaries " +
    "the TotalAmountRaisedHelper.checkIfMarketInfoApplies method" should {
    "return the expected MarketRoutingCheckResult" in {
      setupMocks(kiProcessingModel = None, Some(keyStoreSavedHadPreviousRFIYes), Some(hadOtherInvestmentsModelYes), Some(keyStoreSavedCommercialSale7Years),
        Some(keyStoreSavedSubsidiariesYes), Some(keyStoreSavedDOI3Years), Some(previousSchemeTrueKIVectorList), None, None)
      val result = totalAmountRaisedHelper.checkIfMarketInfoApplies(mockS4lConnector)
      await(result) shouldBe MarketRoutingCheckResult(isMarketInfoRoute = false, reasonBeforeValidationRequired = false)
    }
  }

  // used investment route bypass market question if previousBeforeDOFCSModel and usedInvestmentReasonBeforeModel are both yes
  "For a subsequent investment (used previously) and a commercial sale exists and is not Ki and has subsidiaries " +
    "the TotalAmountRaisedHelper.checkIfMarketInfoApplies method" should {
    "return the expected MarketRoutingCheckResult (false, fasle) as this is a usedinvestmentBefore route (mktInfoYes) " +
      "but usedInvestmentReasonBefore and previousBeforeDOFCSModel are both yes meaning market info questions are skipped" in {
      setupMocks(Some(falseKIModel), Some(keyStoreSavedHadPreviousRFIYes), Some(hadOtherInvestmentsModelYes), Some(keyStoreSavedCommercialSale7YearsOneDay),
        Some(keyStoreSavedSubsidiariesYes), Some(keyStoreSavedDOI3YearsLessOneDay), Some(previousSchemeTrueKIVectorList), Some(usedInvestmentReasonBeforeModelYes), Some(previousBeforeDOFCSModelYes))
      val result = totalAmountRaisedHelper.checkIfMarketInfoApplies(mockS4lConnector)
      await(result) shouldBe MarketRoutingCheckResult(isMarketInfoRoute = false, reasonBeforeValidationRequired = false)
    }
  }

  "For a subsequent investment (used previously) and a commercial sale exists and is not Ki and has subsidiaries and has date that IS Not within range " +
    "the TotalAmountRaisedHelper.checkIfMarketInfoApplies method" should {
    "return the expected MarketRoutingCheckResult (true. false) as this is a direct geomarket route with no questions validation required" in {
      setupMocks(Some(falseKIModel), Some(keyStoreSavedHadPreviousRFINo), Some(hadOtherInvestmentsModelNo), Some(keyStoreSavedCommercialSale7YearsLessOneDay),
        Some(keyStoreSavedSubsidiariesYes), Some(keyStoreSavedDOI3Years), Some(previousSchemeTrueKIVectorList), None, None)
      val result = totalAmountRaisedHelper.checkIfMarketInfoApplies(mockS4lConnector)
      await(result) shouldBe MarketRoutingCheckResult(isMarketInfoRoute = false, reasonBeforeValidationRequired = false)
    }
  }

  // geo market tests for second route
  "For a subsequent investment (used previously) and a commercial sale exists and is not Ki and has subsidiaries " +
    "the TotalAmountRaisedHelper.checkIfMarketInfoApplies method" should {
    "return the expected MarketRoutingCheckResult (false, fasle) as this is a usedinvestmentBefore route (mktInfoYes) " +
      "but usedInvestmentReasonBefore and previousBeforeDOFCSModel are NOT BOTH  yes (usedInvestmentReasonBeforeModelNo) meaning market info questions are asked and extra validation required" in {
      setupMocks(Some(falseKIModel), Some(keyStoreSavedHadPreviousRFIYes), Some(hadOtherInvestmentsModelYes), Some(keyStoreSavedCommercialSale7YearsOneDay),
        Some(keyStoreSavedSubsidiariesYes), Some(keyStoreSavedDOI3YearsLessOneDay), Some(previousSchemeTrueKIVectorList),
        Some(usedInvestmentReasonBeforeModelNo), Some(previousBeforeDOFCSModelYes))
      val result = totalAmountRaisedHelper.checkIfMarketInfoApplies(mockS4lConnector)
      await(result) shouldBe MarketRoutingCheckResult(isMarketInfoRoute = true, reasonBeforeValidationRequired = true)
    }
  }

  "For a subsequent investment (used previously) and a commercial sale exists and is not Ki and has subsidiaries " +
    "the TotalAmountRaisedHelper.checkIfMarketInfoApplies method" should {
    "return the expected MarketRoutingCheckResult (false, fasle) as this is a usedinvestmentBefore route (mktInfoYes) " +
      "but usedInvestmentReasonBefore and previousBeforeDOFCSModel are NOT BOTH  yes (usedInvestmentReasonBeforeModel = None) meaning market info questions are asked and extra validation required" in {
      setupMocks(Some(falseKIModel), Some(keyStoreSavedHadPreviousRFIYes), Some(hadOtherInvestmentsModelYes), Some(keyStoreSavedCommercialSale7YearsOneDay),
        Some(keyStoreSavedSubsidiariesYes), Some(keyStoreSavedDOI3YearsLessOneDay), Some(previousSchemeTrueKIVectorList),
        usedInvestmentReasonBeforeModel = None, Some(previousBeforeDOFCSModelYes))
      val result = totalAmountRaisedHelper.checkIfMarketInfoApplies(mockS4lConnector)
      await(result) shouldBe MarketRoutingCheckResult(isMarketInfoRoute = true, reasonBeforeValidationRequired = true)
    }
  }

  "For a subsequent investment (used previously) and a commercial sale exists and is not Ki and has subsidiaries " +
    "the TotalAmountRaisedHelper.checkIfMarketInfoApplies method" should {
    "return the expected MarketRoutingCheckResult (false, fasle) as this is a usedinvestmentBefore route (mktInfoYes) " +
      "but usedInvestmentReasonBefore and previousBeforeDOFCSModel are NOT BOTH  yes (previousBeforeDOFCSModelNo) meaning market info questions are asked and extra validation required" in {
      setupMocks(Some(falseKIModel), Some(keyStoreSavedHadPreviousRFIYes), Some(hadOtherInvestmentsModelYes), Some(keyStoreSavedCommercialSale7YearsOneDay),
        Some(keyStoreSavedSubsidiariesYes), Some(keyStoreSavedDOI3YearsLessOneDay), Some(previousSchemeTrueKIVectorList),
        Some(usedInvestmentReasonBeforeModelYes), Some(previousBeforeDOFCSModelNo))
      val result = totalAmountRaisedHelper.checkIfMarketInfoApplies(mockS4lConnector)
      await(result) shouldBe MarketRoutingCheckResult(isMarketInfoRoute = true, reasonBeforeValidationRequired = true)
    }
  }

  "For a subsequent investment (used previously) and a commercial sale exists and is not Ki and has subsidiaries " +
    "the TotalAmountRaisedHelper.checkIfMarketInfoApplies method" should {
    "return the expected MarketRoutingCheckResult (false, fasle) as this is a usedinvestmentBefore route (mktInfoYes) " +
      "but usedInvestmentReasonBefore and previousBeforeDOFCSModel are NOT BOTH  yes (previousBeforeDOFCSModel = None) meaning market info questions are asked and extra validation required" in {
      setupMocks(Some(falseKIModel), Some(keyStoreSavedHadPreviousRFIYes), Some(hadOtherInvestmentsModelYes), Some(keyStoreSavedCommercialSale7YearsOneDay),
        Some(keyStoreSavedSubsidiariesYes), Some(keyStoreSavedDOI3YearsLessOneDay), Some(previousSchemeTrueKIVectorList),
        Some(usedInvestmentReasonBeforeModelYes), previousBeforeDOFCSModel = None)
      val result = totalAmountRaisedHelper.checkIfMarketInfoApplies(mockS4lConnector)
      await(result) shouldBe MarketRoutingCheckResult(isMarketInfoRoute = true, reasonBeforeValidationRequired = true)
    }
  }

  "For a subsequent investment (used previously) and a commercial sale exists and is not Ki and has subsidiaries " +
    "the TotalAmountRaisedHelper.checkIfMarketInfoApplies method" should {
    "return the expected MarketRoutingCheckResult (false, fasle) as this is a usedinvestmentBefore route (mktInfoYes) " +
      "but usedInvestmentReasonBefore and previousBeforeDOFCSModel are NOT BOTH  yes (previousBeforeDOFCSModelNo and usedInvestmentReasonBeforeModelNo) meaning market info questions are asked and extra validation required" in {
      setupMocks(Some(falseKIModel), Some(keyStoreSavedHadPreviousRFIYes), Some(hadOtherInvestmentsModelYes), Some(keyStoreSavedCommercialSale7YearsOneDay),
        Some(keyStoreSavedSubsidiariesYes), Some(keyStoreSavedDOI3YearsLessOneDay), Some(previousSchemeTrueKIVectorList),
        Some(usedInvestmentReasonBeforeModelNo), Some(previousBeforeDOFCSModelNo))
      val result = totalAmountRaisedHelper.checkIfMarketInfoApplies(mockS4lConnector)
      await(result) shouldBe MarketRoutingCheckResult(isMarketInfoRoute = true, reasonBeforeValidationRequired = true)
    }
  }

  "For a subsequent investment (used previously) and a commercial sale exists and is not Ki and has subsidiaries " +
    "the TotalAmountRaisedHelper.checkIfMarketInfoApplies method" should {
    "return the expected MarketRoutingCheckResult (false, fasle) as this is a usedinvestmentBefore route (mktInfoYes) " +
      "but usedInvestmentReasonBefore and previousBeforeDOFCSModel are NOT BOTH  yes (previousBeforeDOFCSModel = None and usedInvestmentReasonBeforeModel = None) meaning market info questions are asked and extra validation required" in {
      setupMocks(Some(falseKIModel), Some(keyStoreSavedHadPreviousRFIYes), Some(hadOtherInvestmentsModelYes), Some(keyStoreSavedCommercialSale7YearsOneDay),
        Some(keyStoreSavedSubsidiariesYes), Some(keyStoreSavedDOI3YearsLessOneDay), Some(previousSchemeTrueKIVectorList),
        usedInvestmentReasonBeforeModel = None, previousBeforeDOFCSModel = None)
      val result = totalAmountRaisedHelper.checkIfMarketInfoApplies(mockS4lConnector)
      await(result) shouldBe MarketRoutingCheckResult(isMarketInfoRoute = true, reasonBeforeValidationRequired = true)
    }
  }

  "For a subsequent investment (used previously) and a commercial sale exists and is Ki and has subsidiaries and has date that IS within range " +
    "the TotalAmountRaisedHelper.checkIfMarketInfoApplies method" should {
    "return the expected MarketRoutingCheckResult (true. false) as this is a direct geomarket route with no questions validation required" in {
      setupMocks(Some(trueKIModel), Some(keyStoreSavedHadPreviousRFINo), Some(hadOtherInvestmentsModelNo), Some(keyStoreSavedCommercialSale10YearsOneDay),
        Some(keyStoreSavedSubsidiariesYes), Some(keyStoreSavedDOI3Years), Some(previousSchemeTrueKIVectorList), None, None)
      val result = totalAmountRaisedHelper.checkIfMarketInfoApplies(mockS4lConnector)
      await(result) shouldBe MarketRoutingCheckResult(isMarketInfoRoute = true, reasonBeforeValidationRequired = false)
    }
  }

  "For a subsequent investment (used previously) and a commercial sale exists and is not Ki and has subsidiaries and has date that IS within range " +
    "the TotalAmountRaisedHelper.checkIfMarketInfoApplies method" should {
    "return the expected MarketRoutingCheckResult (true. false) as this is a direct geomarket route with no questions validation required" in {
      setupMocks(Some(falseKIModel), Some(keyStoreSavedHadPreviousRFINo), Some(hadOtherInvestmentsModelNo), Some(keyStoreSavedCommercialSale7YearsOneDay),
        Some(keyStoreSavedSubsidiariesYes), Some(keyStoreSavedDOI3Years), Some(previousSchemeTrueKIVectorList), None, None)
      val result = totalAmountRaisedHelper.checkIfMarketInfoApplies(mockS4lConnector)
      await(result) shouldBe MarketRoutingCheckResult(isMarketInfoRoute = true, reasonBeforeValidationRequired = false)
    }
  }

  "For a subsequent investment (used previously) and a commercial sale exists and is Ki and subsidiaries is None and has date that IS within range " +
    "the TotalAmountRaisedHelper.checkIfMarketInfoApplies method" should {
    "return the expected MarketRoutingCheckResult" in {
      setupMocks(Some(trueKIModel), Some(keyStoreSavedHadPreviousRFIYes), Some(hadOtherInvestmentsModelYes), Some(keyStoreSavedCommercialSale1Year),
        Some(keyStoreSavedSubsidiariesYes), Some(keyStoreSavedDOI3Years), Some(previousSchemeTrueKIVectorList), None, None)
      val result = totalAmountRaisedHelper.checkIfMarketInfoApplies(mockS4lConnector)
      await(result) shouldBe MarketRoutingCheckResult(isMarketInfoRoute = false, reasonBeforeValidationRequired = false)
    }
  }

}