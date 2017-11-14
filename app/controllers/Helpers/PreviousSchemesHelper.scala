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
import models.{PreviousSchemeModel, ShareIssueDateModel}
import org.joda.time.DateTime
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier

object PreviousSchemesHelper extends PreviousSchemesHelper {

}

trait PreviousSchemesHelper {

  def getExistingInvestmentFromKeystore(s4lConnector: connectors.S4LConnector,
                                        modelProcessingIdToRetrieve: Int)
                                       (implicit hc: HeaderCarrier, user: TAVCUser): Future[Option[PreviousSchemeModel]] = {

    val idNotFound: Int = -1

    require(modelProcessingIdToRetrieve > 0, "The item to retrieve processingId must be an integer > 0")

    val result = s4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](KeystoreKeys.previousSchemes).map {
      case Some(data) =>
        val itemToRetrieveIndex = data.indexWhere(_.processingId.getOrElse(0) == modelProcessingIdToRetrieve)
        if (itemToRetrieveIndex != idNotFound) {
          Some(data(itemToRetrieveIndex))
        }
        else None
      case None => None
    }.recover { case _ => None }

    result
  }

  def getAllInvestmentFromKeystore(s4lConnector: connectors.S4LConnector)
                                  (implicit hc: HeaderCarrier, user: TAVCUser): Future[Vector[PreviousSchemeModel]] = {

    val result = s4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](KeystoreKeys.previousSchemes).map {
      case Some(data) => data
      case None => Vector[PreviousSchemeModel]()
    }.recover { case _ => Vector[PreviousSchemeModel]() }

    result
  }

  def previousInvestmentsExist(s4lConnector: connectors.S4LConnector)
                              (implicit hc: HeaderCarrier, user: TAVCUser): Future[Boolean] = {

    val result: Future[Boolean] = s4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](KeystoreKeys.previousSchemes).flatMap {
      case Some(data) if data.nonEmpty => Future(true)
      case Some(data) => Future(false)
      case None => Future(false)
    }.recoverWith { case _ => Future(false) }

    result
  }

  def getPreviousInvestmentTotalFromKeystore(s4lConnector: connectors.S4LConnector)
                                            (implicit hc: HeaderCarrier, user: TAVCUser): Future[Long] = {

    val result = s4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](KeystoreKeys.previousSchemes).map {
      case Some(data) => data.foldLeft(0.toLong)(_ + _.investmentAmount.toLong)
      case None => 0.toLong
    }.recover { case _ => 0.toLong }

    result
  }

  def getPreviousInvestmentsInShareIssueDateRangeTotal(s4lConnector: connectors.S4LConnector)
                                              (implicit hc: HeaderCarrier, user: TAVCUser): Future[Long] = {

    def calculateAmount(shareIssueDate: Option[ShareIssueDateModel],
                        previousInvestments: Option[Vector[PreviousSchemeModel]]): Long = {

      if (shareIssueDate.isEmpty) 0.toLong
      else {
        val shareDate = shareIssueDate.get
        val shareIssueDateTime = new DateTime(shareDate.year.get, shareDate.month.get, shareDate.day.get, 0, 0)
        previousInvestments match {
          case Some(data) => data.filter(investment =>
            isPreviousInvestmentInRange(shareIssueDateTime,investment.day.get,
              investment.month.get,investment.year.get)).foldLeft(0.toLong)(_ + _.investmentAmount.toLong)
          case _ => 0.toLong
        }
      }
    }

    for {
      shareIssueDate <- s4lConnector.fetchAndGetFormData[ShareIssueDateModel](KeystoreKeys.shareIssueDate)
      previousInvestments <- s4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](KeystoreKeys.previousSchemes).recover {
        case _ => None
      }

      amount = calculateAmount(shareIssueDate, previousInvestments)
    } yield amount

  }

  def addPreviousInvestmentToKeystore(s4lConnector: connectors.S4LConnector,
                                      previousSchemeModelToAdd: PreviousSchemeModel)
                                     (implicit hc: HeaderCarrier, user: TAVCUser): Future[CacheMap] = {
    val defaultId: Int = 1

    val result = s4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](KeystoreKeys.previousSchemes).map {
      case Some(data) =>
        val newId = data.last.processingId.get + 1
        data :+ previousSchemeModelToAdd.copy(processingId = Some(newId))
      case None => Vector.empty :+ previousSchemeModelToAdd.copy(processingId = Some(defaultId))
    }.recover { case _ => Vector.empty :+ previousSchemeModelToAdd.copy(processingId = Some(defaultId)) }

    result.flatMap(newVectorList => s4lConnector.saveFormData(KeystoreKeys.previousSchemes, newVectorList))
  }

  def updateKeystorePreviousInvestment(s4lConnector: connectors.S4LConnector,
                                       previousSchemeModelToUpdate: PreviousSchemeModel)
                                      (implicit hc: HeaderCarrier, user: TAVCUser): Future[CacheMap] = {
    val idNotFound: Int = -1

    require(previousSchemeModelToUpdate.processingId.getOrElse(0) > 0,
      "The item to update processingId must be an integer > 0")

    val result = s4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](KeystoreKeys.previousSchemes).map {
      case Some(data) =>
        val itemToUpdateIndex = data.indexWhere(_.processingId.getOrElse(0) ==
          previousSchemeModelToUpdate.processingId.getOrElse(0))
        if (itemToUpdateIndex != idNotFound) {
          data.updated(itemToUpdateIndex, previousSchemeModelToUpdate)
        }
        else data
      case None => Vector[PreviousSchemeModel]()
    }
    result.flatMap(updatedVectorList => s4lConnector.saveFormData(KeystoreKeys.previousSchemes, updatedVectorList))
  }

  def removeKeystorePreviousInvestment(s4lConnector: connectors.S4LConnector, modelProcessingIdToremove: Int)
                                      (implicit hc: HeaderCarrier, user: TAVCUser): Future[CacheMap] = {

    require(modelProcessingIdToremove > 0, "The modelProcessingIdToremove must be an integer > 0")

    val result = s4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](KeystoreKeys.previousSchemes).map {
      case Some(data) => data.filter(_.processingId.getOrElse(0) != modelProcessingIdToremove)
      case None => Vector[PreviousSchemeModel]()
    }.recover { case _ => Vector[PreviousSchemeModel]() }
    result.flatMap(deletedVectorList => s4lConnector.saveFormData(KeystoreKeys.previousSchemes, deletedVectorList))
  }

  def clearPreviousInvestments(s4lConnector: connectors.S4LConnector)
                              (implicit hc: HeaderCarrier, user: TAVCUser): Future[CacheMap] = {
    s4lConnector.saveFormData(KeystoreKeys.previousSchemes, Vector[PreviousSchemeModel]())
  }

  def updateKeystorePreviousInvestmentById(s4lConnector: connectors.S4LConnector,
                                       previousSchemeModelToUpdate: PreviousSchemeModel)
                                      (implicit hc: HeaderCarrier, user: TAVCUser): Future[PreviousSchemeModel] = {
    val idNotFound: Int = -1

    require(previousSchemeModelToUpdate.processingId.getOrElse(0) > 0,
      "The item to update processingId must be an integer > 0")

    val result = s4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](KeystoreKeys.previousSchemes).map {
      case Some(data) =>
        val itemToUpdateIndex = data.indexWhere(_.processingId.getOrElse(0) ==
          previousSchemeModelToUpdate.processingId.getOrElse(0))
        if (itemToUpdateIndex != idNotFound) {
          data.updated(itemToUpdateIndex, previousSchemeModelToUpdate)
        }
        else data
      case None => Vector[PreviousSchemeModel]()
    }
    result.flatMap(updatedVectorList => s4lConnector.saveFormData(KeystoreKeys.previousSchemes, updatedVectorList))
    val model = for {
      x <- result
    } yield x.last

    model
  }

  def addPreviousInvestmentToKeystoreById(s4lConnector: connectors.S4LConnector,
                                      previousSchemeModelToAdd: PreviousSchemeModel)
                                     (implicit hc: HeaderCarrier, user: TAVCUser): Future[PreviousSchemeModel] = {
    val defaultId: Int = 1

    val result = s4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](KeystoreKeys.previousSchemes).map {
      case Some(data) =>
        val newId = data.last.processingId.get + 1
        data :+ previousSchemeModelToAdd.copy(processingId = Some(newId))
      case None => Vector.empty :+ previousSchemeModelToAdd.copy(processingId = Some(defaultId))
    }.recover { case _ => Vector.empty :+ previousSchemeModelToAdd.copy(processingId = Some(defaultId)) }

    result.flatMap(newVectorList => s4lConnector.saveFormData(KeystoreKeys.previousSchemes, newVectorList))

    val model = for {
      x <- result
    } yield x.last

    model
  }

  private def isPreviousInvestmentInRange(shareIssueDate: DateTime, previousInvestmentDay:Int,
                                          previousInvestmentMonth:Int,
                                          previousInvestmentYear:Int): Boolean = {

    val dateToCheck = new DateTime(previousInvestmentYear, previousInvestmentMonth, previousInvestmentDay, 0, 0)

    dateToCheck.isAfter(shareIssueDate.minusYears(1)) && dateToCheck.isBefore(shareIssueDate.plusDays(1))
  }
}
