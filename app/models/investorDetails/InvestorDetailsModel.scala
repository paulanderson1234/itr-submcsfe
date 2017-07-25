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

package models.investorDetails

import models.{AddInvestorOrNomineeModel, CompanyDetailsModel, CompanyOrIndividualModel, IndividualDetailsModel}
import play.api.libs.json.Json


case class InvestorDetailsModel(investorOrNomineeModel: Option[AddInvestorOrNomineeModel] = None,
                                companyOrIndividualModel: Option[CompanyOrIndividualModel] = None,
                                companyDetailsModel: Option[CompanyDetailsModel] = None,
                                individualDetailsModel: Option[IndividualDetailsModel] = None,
                                numberOfSharesPurchasedModel: Option[NumberOfSharesPurchasedModel] = None,
                                amountSpentModel: Option[HowMuchSpentOnSharesModel] = None,
                                isExistingShareHolderModel: Option[IsExistingShareHolderModel] = None,
                                previousShareHoldingModels: Option[Vector[PreviousShareHoldingModel]] = None,
                                processingId: Option[Int] = None) {

  def validate: Boolean = {

    /*** Validates shareholdings by mapping over each shareholding and reducing the result to either true or false using 'forall'**/
    lazy val validateShareHoldings = {
      if(previousShareHoldingModels.isDefined) previousShareHoldingModels.get.forall(_.validate) else false
    }

    investorOrNomineeModel.isDefined && companyOrIndividualModel.isDefined &&
    (companyDetailsModel.isDefined ^ individualDetailsModel.isDefined) && numberOfSharesPurchasedModel.isDefined &&
    amountSpentModel.isDefined && isExistingShareHolderModel.isDefined && validateShareHoldings
  }

}

object InvestorDetailsModel{
  implicit val formats = Json.format[InvestorDetailsModel]
}