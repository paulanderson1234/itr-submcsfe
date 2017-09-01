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

import models.submission.{UnitIssueModel, _}
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.Json
import uk.gov.hmrc.play.test.UnitSpec

object DesSubmissionCSModelSpec {

  def setupDesSubmissionCSModel(): DesSubmissionCSModel = {

    val contactName = DesContactName.apply("x", "x")
    val desContactDetails = DesContactDetails.apply(Some("1"), Some("1"), None, Some("x@x"))
    val desContactAddress = DesAddressType.apply("x", "x", Some("x"), Some("x"), Some("x"), "GB")
    val desCorrespondenceDetails =
      DesCorrespondenceDetails.apply(contactName, desContactDetails, desContactAddress)

    val desTradeModel = DesTradeModel(Some("Preparing To Trade"), "x", None, None, "2012-12-21", None, None, None)
    val unitIssueModel = UnitIssueModel("x", "2012-12-11", "Shares", CostModel("1", "GBP"), 1, CostModel("1", "GBP"))
    val desOrganisationStatus = DesOrganisationStatusModel(1, "x", CostModel("1", "GBP"), CostModel("1", "GBP"))
    val desInvestmentDetailsModel = DesInvestmentDetailsModel("x", unitIssueModel,
      Some(CostModel("1", "GBP")), Some(desOrganisationStatus))

    val desIndividualDetails = DesIndividualDetailsModel(contactName, desContactAddress)
    val companyOrIndividualDetails = DesCompanyOrIndividualDetailsModel(Some(desIndividualDetails), None)

    val sharesUnitIssueModel = Vector.empty :+ unitIssueModel :+
      UnitIssueModel("x", "2013-11-12", "Debentures", CostModel("1", "GBP"), 1, CostModel("1", "GBP"))
    val existingGroupHoldings = DesGroupHoldingsModel(None, sharesUnitIssueModel)
    val desInvestorInfo = DesInvestorInfoModel(companyOrIndividualDetails, 1, CostModel("1", "GBP"), Some(existingGroupHoldings))

    val investors = Vector.empty :+ DesInvestorModel("Named Investor", desInvestorInfo)
    val desInvestorDetails = DesInvestorDetailsModel(investors)

    val orgDetails = DesCompanyDetailsModel("x", Some("1111111111"), Some("x"), Some(desContactAddress))
    val rfiModel = Vector.empty :+ DesRFIModel("SEIS", Some("x"), "2013-11-21", CostModel("1", "GBP"), Some(CostModel("1", "GBP")))
    val rFICostsModel = DesRFICostsModel(None, rfiModel)
    val desOrganisation = DesOrganisationModel(None, None, "2012-03-31", None, orgDetails, Some(rFICostsModel))
    val desComplianceStatement = DesComplianceStatement("SEIS", desTradeModel, desInvestmentDetailsModel, None, None,
      desInvestorDetails, DesRepaymentsModel.apply(Vector.empty), Some("x"), desOrganisation)

    val desSubmissionModel = DesSubmissionModel.apply(None, desCorrespondenceDetails, "Limited",
      DesSubmission(None, desComplianceStatement))

    val desSubmissionCSModel = DesSubmissionCSModel.apply(Some("$AckRef$"), submissionType = desSubmissionModel)

    desSubmissionCSModel
  }

  println(Json.toJson(setupDesSubmissionCSModel()))
}
