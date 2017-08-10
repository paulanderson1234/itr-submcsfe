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

import com.sun.xml.internal.ws.developer.MemberSubmissionAddressing.Validation
import common.Constants
import models.{AddInvestorOrNomineeModel, CompanyOrIndividualModel, IndividualDetailsModel, TotalAmountSpentModel}
import models.investorDetails.{InvestorShareIssueDateModel, _}

object InvestorTestHelper extends InvestorTestHelper {

}

trait InvestorTestHelper {

  def getInvestors(numberToCreate: Int, numberOfShareholdings: Int = 1, includeIncompleteInvestor:Boolean = false,
                  includeIncompleteShareHolding: Boolean = false):Vector[InvestorDetailsModel] = {
    (for (investorId <- 1 to numberToCreate) yield getInvestorForList(investorId, numberToCreate,
      numberOfShareholdings, includeIncompleteInvestor, includeIncompleteShareHolding, numberToCreate == investorId)).toVector
  }

  def getInvestorForList(investorId: Int = 1, numberToCreate: Int = 1,numberOfShareholdings:Int,
                         includeIncompleteInvestor:Boolean = false, includeIncompleteShareHolding:Boolean = false,
                         isLastInvestor:Boolean = false): InvestorDetailsModel = {

    val makeIncomplete = includeIncompleteInvestor && investorId == numberToCreate

    println("=============================================CREATE INVESTOR================================================================")
    println(s"creating investor $investorId")
    println(s"Make Incomplete $makeIncomplete")
    println("=============================================================================================================================")

    val companyIndividualModel = CompanyOrIndividualModel(TestDataGenerator.randomCompanyOrIndividual(investorId), Some(investorId))
    val isCompany = companyIndividualModel.companyOrIndividual == Constants.typeCompany

    InvestorDetailsModel(
      investorOrNomineeModel = Some(AddInvestorOrNomineeModel(TestDataGenerator.randomInvestorOrNominee(investorId), Some(investorId))),
      companyOrIndividualModel = Some(companyIndividualModel),
      numberOfSharesPurchasedModel = if(makeIncomplete) None else Some(NumberOfSharesPurchasedModel(12.3, Some(investorId))),
      amountSpentModel = Some(HowMuchSpentOnSharesModel(TestDataGenerator.randomWholeAmount(utils.Validation.financialMaxAmountLength), Some(investorId))),
      companyDetailsModel = if (isCompany) Some(TestDataGenerator.randomCompanyDetails(investorId)) else None,
      individualDetailsModel = if(isCompany) None else Some(TestDataGenerator.randomIndividualDetails(investorId)),
      isExistingShareHolderModel = if(numberOfShareholdings > 0 && !makeIncomplete)
        Some(IsExistingShareHolderModel(Constants.StandardRadioButtonYesValue)) else Some(IsExistingShareHolderModel(Constants.StandardRadioButtonNoValue)),
      previousShareHoldingModels = if(makeIncomplete || numberOfShareholdings == 0) None
        else Some(getShareholdingsList(numberOfShareholdings, investorId, includeIncompleteShareHolding, investorId == numberToCreate)),
      processingId = Some(investorId))
  }

  def getShareholdingsList(numberToCreate:Int, investorId:Int, includeIncomplete:Boolean, isLastInvestor:Boolean = false): Vector[PreviousShareHoldingModel] = {
    (for (index <- 1 to numberToCreate) yield getShareHoldingForList(includeIncomplete, index, investorId, numberToCreate, isLastInvestor)).toVector
  }


  def getShareHoldingForList(includeIncomplete:Boolean = false, processingId: Int = 1, investorProcessingId: Int = 1,
                              numberToCreate: Int = 1, isLastInvestor:Boolean = false): PreviousShareHoldingModel = {
    val makeIncomplete = includeIncomplete && processingId == numberToCreate && isLastInvestor

    println("=============================================CREATE SAHREHOLDING================================================================")
    println(s"investorId: $investorProcessingId. Number To Create: $numberToCreate. Is Last investor: $isLastInvestor")
    println(s"creating hareholdingId $processingId for investor $investorProcessingId. Is Last investor: $isLastInvestor")
    println(s"make sahreholding incomplete: $makeIncomplete")
    println("================================================================================================================================")

    PreviousShareHoldingModel(
      investorShareIssueDateModel = Some(TestDataGenerator.getRandomInvestorShareIssueDateModel(processingId, investorProcessingId)),
      numberOfPreviouslyIssuedSharesModel = if (makeIncomplete) None
      else Some(NumberOfPreviouslyIssuedSharesModel(TestDataGenerator.randomDecimal(processingId), Some(processingId))),
      previousShareHoldingNominalValueModel =
        Some(PreviousShareHoldingNominalValueModel(TestDataGenerator.randomWholeAmount(utils.Validation.financialMaxAmountLength), Some(processingId))),
      previousShareHoldingDescriptionModel = if(makeIncomplete) None else
        Some(PreviousShareHoldingDescriptionModel(TestDataGenerator.randomAlphanumericString(Constants.shortTextLimit), Some(processingId)))
      ,processingId = Some(investorProcessingId))
  }

  def getSingleInvestorAndShareHolding: Vector[InvestorDetailsModel] = {

    val shareHolding = PreviousShareHoldingModel(previousShareHoldingDescriptionModel =
      Some(PreviousShareHoldingDescriptionModel("", Some(1))), processingId = Some(1))

    val x =  Vector(InvestorDetailsModel(
        Some(AddInvestorOrNomineeModel(Constants.investor, Some(1))), Some(CompanyOrIndividualModel(Constants.typeCompany, Some(1))),
        isExistingShareHolderModel = Some(IsExistingShareHolderModel(Constants.StandardRadioButtonYesValue)),
            previousShareHoldingModels = Some(Vector(shareHolding)),
        processingId = Some(1)))

    x

  }


  def getSingleInvestorAndShareHoldingd: Vector[InvestorDetailsModel] = {

    val x =  Vector(InvestorDetailsModel(
      Some(AddInvestorOrNomineeModel(Constants.investor, Some(1))), Some(CompanyOrIndividualModel(Constants.typeCompany, Some(1))),
      isExistingShareHolderModel = Some(IsExistingShareHolderModel(Constants.StandardRadioButtonYesValue)),
      previousShareHoldingModels = Some(Vector(getShareHolding())),
      processingId = Some(1)))

    x

  }

//  private def calculateDate(date: DateTime, dateActions: List[String]): DateTime = {
//    @tailrec
//    def actionAccumulator(actions: List[String], accumulator: DateTime): DateTime = {
//      actions match {
//        case Nil => accumulator
//        case dateAction :: tail => actionAccumulator(tail, processDateAction(dateAction, accumulator))
//      }
//    }
//
//    actionAccumulator(dateActions, DateTime.now())
//  }
//
//  private def processDateAction(dateAction: String, date: DateTime): DateTime = {
//    val actionNumber = dateAction.substring(1).replace("(", "").replace(")", "").toInt
//    dateAction.charAt(0) match {
//      case 'D' => addDays(date, actionNumber)
//      case 'M' => addMonths(date, actionNumber)
//      case 'Y' => addYears(date, actionNumber)
//      case _ => date
//    }
//  }





  def getShareHolding(processingId: Int = 1, investorProcessingId: Int = 1): PreviousShareHoldingModel = {
    PreviousShareHoldingModel(previousShareHoldingDescriptionModel =
      Some(PreviousShareHoldingDescriptionModel("", Some(processingId))), processingId = Some(investorProcessingId))
  }

}
