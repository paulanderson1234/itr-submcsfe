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

package testOnly.controllers


import common.Constants
import models.{CompanyDetailsModel, IndividualDetailsModel}
import models.investorDetails.InvestorShareIssueDateModel
import java.time.{YearMonth, ZoneId}
import java.util.Date

object TestDataGenerator {




  val date = new Date()
  val localDate = date.toInstant.atZone(ZoneId.systemDefault()).toLocalDate
  val todayYear: Int = localDate.getYear

  val random = new scala.util.Random

  def randomString(alphabet: String)(n: Int): String =
    Stream.continually(random.nextInt(alphabet.length)).map(alphabet).take(n).mkString

  def randomAlphanumericString(n: Int): String =
    randomString("abcdefghijklmnopqrstuvwxyz0123456789")(n)

  def randomNumberString(n: Int): String =
    randomString("1234567890")(n)

  def randomAlphaString(n: Int): String =
    randomString("ABCDEFGHIJKLMNOPQRSTUVWXZYabcdefghijklmnopqrstuvwxyz")(n)

  def randomWholeAmount(maxAmount:Int):Int = {
    random.nextInt(maxAmount)
  }

  def randomDecimalAmount = {
    random.nextDouble()
  }

  def randomIndividualDetails(investorId:Int):IndividualDetailsModel = {

    IndividualDetailsModel(
      processingId = Some(investorId),
      forename = randomAlphaString(Constants.forenameLength),
      surname = randomAlphaString(Constants.surnameLength),
      addressline1 = randomAlphanumericString(Constants.addressLineLength),
      addressline2 = randomAlphanumericString(Constants.addressLineLength),
      addressline3 = Some(randomAlphanumericString(Constants.addressLineLength)),
      addressline4 = Some(randomAlphanumericString(Constants.addressLineLength)),
      postcode= Some("QQ" + randomNumberString(2) + " " + randomNumberString(1) + "QQ"),
      countryCode = "GB"
    )
  }

  def randomCompanyDetails(investorId:Int):CompanyDetailsModel = {

    CompanyDetailsModel(
      processingId = Some(investorId),
      companyName = randomAlphaString(Constants.CompanyDetailsMaxLength),
      companyAddressline1 = randomAlphanumericString(Constants.addressLineLength),
      companyAddressline2 = randomAlphanumericString(Constants.addressLineLength),
      companyAddressline3 = Some(randomAlphanumericString(Constants.addressLineLength)),
      companyAddressline4 = Some(randomAlphanumericString(Constants.addressLineLength)),
      companyPostcode= Some("QQ" + randomNumberString(2) + " " + randomNumberString(1) + "QQ"),
      countryCode = "GB"
    )
  }

  def getRandomInvestorShareIssueDateModel(processingId: Int, investorId: Int): InvestorShareIssueDateModel = {

    InvestorShareIssueDateModel(
      dateOfIssueDay = getRandomIntBetween(1, 28),
      dateOfIssueMonth = getRandomIntBetween(1, 12),
      dateOfIssueYear =  getRandomIntBetween(2000, todayYear),
      investorProcessingId = Some(investorId),
      processingId = Some(processingId)
    )
  }


  def randomCompanyOrIndividual(investorId:Int):String = {
    if (investorId % 2 == 0) Constants.typeCompany else Constants.typeIndividual
  }


  def randomInvestorOrNominee(investorId:Int):String = {
    if (investorId % 2 == 0) Constants.investor else Constants.nominee
  }

  def randomDecimal(processingId:Int):BigDecimal = {
    if (processingId % 2 == 0) 9999999999999.00
    else BigDecimal("999999999999.999999").setScale(5, BigDecimal.RoundingMode.HALF_UP)
  }

  def getRandomIntBetween(start: Int, end: Int): Int = {
    val rnd = new scala.util.Random
    start + rnd.nextInt( (end - start) + 1 )
  }

}
