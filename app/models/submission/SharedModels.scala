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

package models.submission

import play.api.libs.json.Json

case class DesAddressType(
                           addressLine1: String,
                           addressLine2: String,
                           addressLine3: Option[String],
                           addressLine4: Option[String],
                           postalCode: Option[String],
                           countryCode: String
                         )
object DesAddressType{
  implicit val formats = Json.format[DesAddressType]
}

case class DesContactDetails(
                              phoneNumber: Option[String],
                              mobileNumber: Option[String],
                              faxNumber: Option[String],
                              emailAddress: Option[String]
                            )
object DesContactDetails{
  implicit val formats = Json.format[DesContactDetails]
}

case class DesContactName(
                           name1: String,
                           name2: String
                         )
object DesContactName{
  implicit val formats = Json.format[DesContactName]
}

case class DesCorrespondenceDetails(
                                     contactName: DesContactName,
                                     contactDetails: DesContactDetails,
                                     contactAddress: DesAddressType
                                   )

object DesCorrespondenceDetails{
  implicit val formats = Json.format[DesCorrespondenceDetails]
}

case class DesMarketInfo(
                          newGeographicMarket: Boolean,
                          newProductMarket: Boolean,
                          marketDescription: Option[String]
                        )

object DesMarketInfo{
  implicit val formats = Json.format[DesMarketInfo]
}
case class CostModel(
                      amount : String,
                      currency: String = "GBP"
                    )
object CostModel{
  implicit val formats = Json.format[CostModel]
}
case class UnitIssueModel(
                           description: String,
                           dateOfIssue: String,
                           unitType: String, // Mandatory as per schema
                           nominalValue: CostModel,
                           numberUnitsIssued: BigDecimal,
                           totalAmount: CostModel
                         )
object UnitIssueModel{
  implicit val formats = Json.format[UnitIssueModel]
}

case class AnnualCostModel(
                            periodEnding: String,
                            operatingCost: CostModel,
                            researchAndDevelopmentCost: CostModel
                          )
object AnnualCostModel{
  implicit val formats = Json.format[AnnualCostModel]
}
case class DesAnnualCostsModel(
                                nodata:Option[String],
                                annualCost: Seq[AnnualCostModel]
                              )
object DesAnnualCostsModel{
  implicit val formats = Json.format[DesAnnualCostsModel]
}
case class TurnoverCostModel(
                              periodEnding: String,
                              turnover: CostModel
                            )
object TurnoverCostModel{
  implicit val formats = Json.format[TurnoverCostModel]
}
case class DesAnnualTurnoversModel(
                                    nodata:Option[String],
                                    annualTurnover: Seq[TurnoverCostModel]
                                  )
object DesAnnualTurnoversModel{
  implicit val formats = Json.format[DesAnnualTurnoversModel]
}

object SchemeType extends Enumeration {
  type SchemeType = Value
  val seis = Value("SEIS")
  val eis = Value("EIS")
}

object OrganisationType extends Enumeration {
  type OrganisationType = Value
  val limited = Value("Limited")
  val communityInterestCompany = Value("Community Interest Company")
  val communityBenefitsSociety = Value("Community Benefits Society")
  val charityCompany = Value("Charity - Company")
  val charityTrust = Value("Charity - Trust")
  val partnership = Value("Partnership")
  val other = Value("Other")
}

object UnitType extends Enumeration {
  type UnitType = Value
  val shares = Value("Shares")
  val debentures = Value("Debentures")
}

object InvestorType extends Enumeration {
  type InvestorType = Value
  val investor = Value("Named Investor")
  val nominee = Value("Nominee")
}
