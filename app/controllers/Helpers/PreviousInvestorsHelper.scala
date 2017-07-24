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

package controllers.Helpers

import auth.TAVCUser
import common.KeystoreKeys
import models.{AddInvestorOrNomineeModel, InvestorDetailsModel}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object PreviousInvestorsHelper extends PreviousInvestorsHelper {

}

trait PreviousInvestorsHelper {

  def addPreviousInvestmentToKeystore(s4lConnector: connectors.S4LConnector,
                                          investorDetailsModel: InvestorDetailsModel)
                                         (implicit hc: HeaderCarrier, user: TAVCUser): Future[Vector[InvestorDetailsModel]] = {
    val defaultId: Int = 1

    val result = s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map {
      case Some(data) => {
        val newId = data.last.processingId.get + 1
        data :+ investorDetailsModel.copy(processingId = Some(newId))
      }
      case None => Vector.empty :+ investorDetailsModel.copy(processingId = Some(defaultId))
    }.recover { case _ => Vector.empty :+ investorDetailsModel.copy(processingId = Some(defaultId)) }

    result.flatMap(newVectorList => s4lConnector.saveFormData(KeystoreKeys.investorDetails, newVectorList))
    result
  }

  def updateKeystorePreviousInvestment(s4lConnector: connectors.S4LConnector,
                                           investorDetailsModel: InvestorDetailsModel)
                                          (implicit hc: HeaderCarrier, user: TAVCUser): Future[Vector[InvestorDetailsModel]] = {
    val idNotFound: Int = -1

    require(investorDetailsModel.processingId.getOrElse(0) > 0,
      "The item to update processingId must be an integer > 0")

    val result = s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map {
      case Some(data) => {
        val itemToUpdateIndex = data.indexWhere(_.processingId.getOrElse(0) ==
          investorDetailsModel.processingId.getOrElse(0))
        if (itemToUpdateIndex != idNotFound) {
          data.updated(itemToUpdateIndex, investorDetailsModel)
        }
        else data
      }
      case None => Vector[InvestorDetailsModel]()
    }
    result.flatMap(updatedVectorList => s4lConnector.saveFormData(KeystoreKeys.investorDetails, updatedVectorList))
    result
  }

  def removeKeystorePreviousInvestment(s4lConnector: connectors.S4LConnector, processingId: Int)
                                      (implicit hc: HeaderCarrier, user: TAVCUser): Future[Vector[InvestorDetailsModel]] = {

    require(processingId > 0, "The processingId must be an integer > 0")

    val result = s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map {
      case Some(data) => data.filter(_.processingId.getOrElse(0) != processingId)
      case None => Vector[InvestorDetailsModel]()
    }.recover { case _ => Vector[InvestorDetailsModel]() }
    result.flatMap(deletedVectorList => s4lConnector.saveFormData(KeystoreKeys.investorDetails, deletedVectorList))
    result
  }

  def clearPreviousInvestments(s4lConnector: connectors.S4LConnector)
                              (implicit hc: HeaderCarrier, user: TAVCUser): Future[CacheMap] = {
    s4lConnector.saveFormData(KeystoreKeys.investorDetails, Vector[InvestorDetailsModel]())
  }

  def addInfoToInvestorById(s4lConnector: connectors.S4LConnector,
                            addInvestorOrNomineeModel : AddInvestorOrNomineeModel)
                                         (implicit hc: HeaderCarrier, user: TAVCUser): Future[InvestorDetailsModel] = {
    val defaultId: Int = 1

    val result = s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map {
      case Some(data) => {
        val investorDetailsModel = data.last
        val itemToUpdateIndex = data.indexWhere(_.processingId.getOrElse(0) ==
          investorDetailsModel.processingId.getOrElse(0))
        data.updated(itemToUpdateIndex, investorDetailsModel.copy(investorOrNomineeModel =
          Some(addInvestorOrNomineeModel.copy(processingId = investorDetailsModel.processingId))))
      }
      case None => {
        Vector.empty :+ InvestorDetailsModel.apply(investorOrNomineeModel = Some(addInvestorOrNomineeModel.copy(processingId = Some(defaultId))),
        processingId = Some(defaultId))
      }
    }.recover { case _ => Vector.empty :+ InvestorDetailsModel.apply(investorOrNomineeModel = Some(addInvestorOrNomineeModel.copy(processingId = Some(defaultId))),
      processingId = Some(defaultId)) }

    result.flatMap(newVectorList => s4lConnector.saveFormData(KeystoreKeys.investorDetails, newVectorList))

    val model = for {
      x <- result
    } yield x.last

    model
  }

  def updateInfoToInvestorById(s4lConnector: connectors.S4LConnector,
                               addInvestorOrNomineeModel : AddInvestorOrNomineeModel)
                                          (implicit hc: HeaderCarrier, user: TAVCUser): Future[InvestorDetailsModel] = {
    val idNotFound: Int = -1

    require(addInvestorOrNomineeModel.processingId.getOrElse(0) > 0,
      "The item to update processingId must be an integer > 0")

    val result = s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map {
      case Some(data) => {
        val itemToUpdateIndex = data.indexWhere(_.processingId.getOrElse(0) ==
          addInvestorOrNomineeModel.processingId.getOrElse(0))
        if (itemToUpdateIndex != idNotFound) {
          val investorDetailsModel = data.lift(itemToUpdateIndex)
          data.updated(itemToUpdateIndex, investorDetailsModel.get.copy(investorOrNomineeModel = Some(addInvestorOrNomineeModel)))
        }
        else data
      }
      case None => Vector[InvestorDetailsModel]()
    }
    result.flatMap(updatedVectorList => s4lConnector.saveFormData(KeystoreKeys.investorDetails, updatedVectorList))
    val model = for {
      x <- result
    } yield x.lift(x.indexWhere(_.processingId.getOrElse(0) == addInvestorOrNomineeModel.processingId.getOrElse(0))).get

    model
  }

}