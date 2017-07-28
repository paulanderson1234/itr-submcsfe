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

import controllers.helpers.BaseSpec
import models.investorDetails._

class InvestorDetailsModelSpec extends BaseSpec{

  val invalidModelEmpty = InvestorDetailsModel()

  val invalidInvestorOrNomineeMissing = InvestorDetailsModel(None, Some(companyOrIndividualModel), Some(companyDetailsModel), None,
    Some(numberOfSharesPurchasedModel), Some(howMuchSpentOnSharesModel), Some(isExistingShareHolderModelNo), Some(Vector()))
    val invalidCompanyOrIndividualMissing = InvestorDetailsModel(Some(investorModel), None, Some(companyDetailsModel), None,
    Some(numberOfSharesPurchasedModel), Some(howMuchSpentOnSharesModel), Some(isExistingShareHolderModelNo), Some(Vector()))
  val invalidCompanyAndIndividualDetailsMissing = InvestorDetailsModel(Some(investorModel), Some(companyOrIndividualModel), None, None,
    Some(numberOfSharesPurchasedModel), Some(howMuchSpentOnSharesModel), Some(isExistingShareHolderModelNo), Some(Vector()))
  val invalidNumberOfSharesPurchasedMissing = InvestorDetailsModel(Some(investorModel), Some(companyOrIndividualModel), Some(companyDetailsModel), None,
    None, Some(howMuchSpentOnSharesModel), Some(isExistingShareHolderModelNo), Some(Vector()))
  val invalidAmountSpentMissing = InvestorDetailsModel(Some(investorModel), Some(companyOrIndividualModel), Some(companyDetailsModel), None,
    Some(numberOfSharesPurchasedModel), None, Some(isExistingShareHolderModelNo), Some(Vector()))
  val invalidIsExistingShareHolderMissing = InvestorDetailsModel(Some(investorModel), Some(companyOrIndividualModel), Some(companyDetailsModel), None,
    Some(numberOfSharesPurchasedModel), Some(howMuchSpentOnSharesModel), None, Some(Vector()))
  val invalidPreviousShareHoldingsMissing = InvestorDetailsModel(Some(investorModel), Some(companyOrIndividualModel), Some(companyDetailsModel), None,
    Some(numberOfSharesPurchasedModel), Some(howMuchSpentOnSharesModel), Some(isExistingShareHolderModelNo), None)
  val invalidMissingValidPreviousShareHoldingsModel = InvestorDetailsModel(Some(investorModel), Some(companyOrIndividualModel), Some(companyDetailsModel), None,
    Some(numberOfSharesPurchasedModel), Some(howMuchSpentOnSharesModel), Some(isExistingShareHolderModelYes), Some(Vector(PreviousShareHoldingModel())))


  val invalidMissingShareIssueDate = InvestorDetailsModel(Some(investorModel), Some(companyOrIndividualModel), Some(companyDetailsModel), None,
    Some(numberOfSharesPurchasedModel), Some(howMuchSpentOnSharesModel), Some(isExistingShareHolderModelYes),
    Some(Vector(PreviousShareHoldingModel(None, Some(numberOfPreviouslyIssuedSharesModel),
      Some(previousShareHoldingNominalValueModel), Some(previousShareHoldingDescriptionModel)))))

  val invalidMissingNumberOfPreviouslyIssuedShares = InvestorDetailsModel(Some(investorModel), Some(companyOrIndividualModel), Some(companyDetailsModel), None,
    Some(numberOfSharesPurchasedModel), Some(howMuchSpentOnSharesModel), Some(isExistingShareHolderModelYes),
    Some(Vector(PreviousShareHoldingModel(Some(investorShareIssueDateModel), None,
      Some(previousShareHoldingNominalValueModel), Some(previousShareHoldingDescriptionModel)))))

  val invalidMissingNominalValue = InvestorDetailsModel(Some(investorModel), Some(companyOrIndividualModel), Some(companyDetailsModel), None,
    Some(numberOfSharesPurchasedModel), Some(howMuchSpentOnSharesModel), Some(isExistingShareHolderModelYes),
    Some(Vector(PreviousShareHoldingModel(Some(investorShareIssueDateModel), Some(numberOfPreviouslyIssuedSharesModel),
      None, Some(previousShareHoldingDescriptionModel)))))

  val invalidMissingShareDescription = InvestorDetailsModel(Some(investorModel), Some(companyOrIndividualModel), Some(companyDetailsModel), None,
    Some(numberOfSharesPurchasedModel), Some(howMuchSpentOnSharesModel), Some(isExistingShareHolderModelYes),
    Some(Vector(PreviousShareHoldingModel(Some(investorShareIssueDateModel), Some(numberOfPreviouslyIssuedSharesModel),
      Some(previousShareHoldingNominalValueModel), None))))

  val invalidAndValidShareHoldings = InvestorDetailsModel(Some(investorModel), Some(companyOrIndividualModel), Some(companyDetailsModel), None,
    Some(numberOfSharesPurchasedModel), Some(howMuchSpentOnSharesModel), Some(isExistingShareHolderModelYes),
    Some(Vector(PreviousShareHoldingModel(Some(investorShareIssueDateModel), Some(numberOfPreviouslyIssuedSharesModel),
      Some(previousShareHoldingNominalValueModel), Some(previousShareHoldingDescriptionModel)), PreviousShareHoldingModel())))

  val invalidModelCompanyAndIndividualDetailsExist = InvestorDetailsModel(Some(investorModel3), Some(companyOrIndividualModel3), Some(companyDetailsModel3),
    Some(individualDetailsModel3), Some(numberOfSharesPurchasedModel3), Some(howMuchSpentOnSharesModel3), Some(isExistingShareHolderModelNo), Some(Vector()), Some(3))

  "Calling validate" should {
    "return false" when {
      "the investor details model is empty" in {
        invalidModelEmpty.validate shouldBe false
      }
      "the investor details is missing investor or nominee model" in {
        invalidInvestorOrNomineeMissing.validate shouldBe false
      }
      "the investor details is missing company or individual model" in {
        invalidCompanyOrIndividualMissing.validate shouldBe false
      }
      "the investor details is missing both company and individual details" in {
        invalidCompanyAndIndividualDetailsMissing.validate shouldBe false
      }
      "the investor details is missing number of shares purchased model" in {
        invalidNumberOfSharesPurchasedMissing.validate shouldBe false
      }
      "the investor details is missing amount spent model" in {
        invalidAmountSpentMissing.validate shouldBe false
      }
      "the investor details is missing is existing share holder model" in {
        invalidIsExistingShareHolderMissing.validate shouldBe false
      }
      "the investor details is missing previous share holdings model" in {
        invalidPreviousShareHoldingsMissing.validate shouldBe true
      }
      "the investor details model is missing a valid list of previous share holdings" in {
        invalidMissingValidPreviousShareHoldingsModel.validate shouldBe false
      }
      "the investor details is missing the share holding share issue date" in {
        invalidMissingShareIssueDate.validate shouldBe false
      }
      "the investor details is missing the share holding number of previously issued shares" in {
        invalidMissingNumberOfPreviouslyIssuedShares.validate shouldBe false
      }
      "the investor details is missing the share holing nominal value" in {
        invalidMissingNominalValue.validate shouldBe false
      }
      "the investor details is missing the share holding share description" in {
        invalidMissingShareDescription.validate shouldBe false
      }
      "the investor details contains both valid and invalid share holding models" in {
        invalidAndValidShareHoldings.validate shouldBe false
      }
    }
    "return true" when {
      "the investor details model is fully populated with no previous share holdings" in {
        validModelNoPrevShareHoldings.validate shouldBe true
      }

      "the investor details model is fully populated with a list of previous share holdings" in {
        validModelWithPrevShareHoldings.validate shouldBe true
      }

      "the investor details contains both company details and individual details" in {
        invalidModelCompanyAndIndividualDetailsExist.validate shouldBe true
      }
    }
  }
}
