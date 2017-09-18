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
import common.{Constants, KeystoreKeys}
import models.repayments._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.{HeaderCarrier, InternalServerException}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object PreviousRepaymentsHelper extends PreviousRepaymentsHelper {

}

trait PreviousRepaymentsHelper {

  def addPreviousRepaymentsToKeystore(s4lConnector: connectors.S4LConnector,
                                      sharesRepaymentDetailsModel: SharesRepaymentDetailsModel)
                                         (implicit hc: HeaderCarrier, user: TAVCUser): Future[Vector[SharesRepaymentDetailsModel]] = {
    val defaultId: Int = 1

    val result = s4lConnector.fetchAndGetFormData[Vector[SharesRepaymentDetailsModel]](KeystoreKeys.sharesRepaymentDetails).map {
      case Some(data) =>
        val newId = data.last.processingId.get + 1
        data :+ sharesRepaymentDetailsModel.copy(processingId = Some(newId))
      case None => Vector.empty :+ sharesRepaymentDetailsModel.copy(processingId = Some(defaultId))
    }.recover { case _ => Vector.empty :+ sharesRepaymentDetailsModel.copy(processingId = Some(defaultId)) }

    result.flatMap(newVectorList => s4lConnector.saveFormData(KeystoreKeys.sharesRepaymentDetails, newVectorList))
    result
  }

  def updateKeystorePreviousInvestment(s4lConnector: connectors.S4LConnector,
                                       sharesRepaymentDetailsModel: SharesRepaymentDetailsModel)
                                      (implicit hc: HeaderCarrier, user: TAVCUser): Future[Vector[SharesRepaymentDetailsModel]] = {
    val idNotFound: Int = Constants.notFound

    require(sharesRepaymentDetailsModel.processingId.getOrElse(0) > 0,
      "The item to update processingId must be an integer > 0")

    val result = s4lConnector.fetchAndGetFormData[Vector[SharesRepaymentDetailsModel]](KeystoreKeys.sharesRepaymentDetails).map {
      case Some(data) =>
        val itemToUpdateIndex = data.indexWhere(_.processingId.getOrElse(0) ==
          sharesRepaymentDetailsModel.processingId.getOrElse(0))
        if (itemToUpdateIndex != idNotFound) {
          data.updated(itemToUpdateIndex, sharesRepaymentDetailsModel)
        }
        else data
      case None => Vector[SharesRepaymentDetailsModel]()
    }
    result.flatMap(updatedVectorList => s4lConnector.saveFormData(KeystoreKeys.sharesRepaymentDetails, updatedVectorList))
    result
  }

  def removeKeystorePreviousInvestment(s4lConnector: connectors.S4LConnector, processingId: Int)
                                      (implicit hc: HeaderCarrier, user: TAVCUser): Future[Vector[SharesRepaymentDetailsModel]] = {

    require(processingId > 0, "The processingId must be an integer > 0")

    val result = s4lConnector.fetchAndGetFormData[Vector[SharesRepaymentDetailsModel]](KeystoreKeys.sharesRepaymentDetails).map {
      case Some(data) => data.filter(_.processingId.getOrElse(0) != processingId)
      case None => Vector[SharesRepaymentDetailsModel]()
    }.recover { case _ => Vector[SharesRepaymentDetailsModel]() }
    result.flatMap(deletedVectorList => s4lConnector.saveFormData(KeystoreKeys.sharesRepaymentDetails, deletedVectorList))
    result
  }

  def clearPreviousInvestments(s4lConnector: connectors.S4LConnector)
                              (implicit hc: HeaderCarrier, user: TAVCUser): Future[CacheMap] = {
    s4lConnector.saveFormData(KeystoreKeys.sharesRepaymentDetails, Vector[SharesRepaymentDetailsModel]())
  }

  def addWhoRepaidShares(s4lConnector: connectors.S4LConnector,
                         whoRepaidSharesModel: WhoRepaidSharesModel)
                                         (implicit hc: HeaderCarrier, user: TAVCUser): Future[SharesRepaymentDetailsModel] = {
    val defaultId: Int = 1

    val result = s4lConnector.fetchAndGetFormData[Vector[SharesRepaymentDetailsModel]](KeystoreKeys.sharesRepaymentDetails).map {
      case Some(data) =>
        if (data.nonEmpty && data.last.validate) {
          val newId = data.last.processingId.get + 1
          data :+ SharesRepaymentDetailsModel.apply(whoRepaidSharesModel = Some(whoRepaidSharesModel.copy(processingId = Some(newId))),
            processingId = Some(newId))
        }
        else{
          data.updated(data.size - 1, data.last.copy(whoRepaidSharesModel =
            Some(whoRepaidSharesModel.copy(processingId = data.last.processingId))))
        }
      case None =>
        Vector.empty :+ SharesRepaymentDetailsModel.apply(whoRepaidSharesModel = Some(whoRepaidSharesModel.copy(processingId = Some(defaultId))),
        processingId = Some(defaultId))
    }.recover { case _ => Vector.empty :+ SharesRepaymentDetailsModel.apply(whoRepaidSharesModel = Some(whoRepaidSharesModel.copy(processingId = Some(defaultId))),
      processingId = Some(defaultId)) }

    result.flatMap(newVectorList => s4lConnector.saveFormData(KeystoreKeys.sharesRepaymentDetails, newVectorList))

    val model = for {
      sharesRepaymentDetails <- result
    } yield sharesRepaymentDetails.last

    model
  }

  def updateWhoRepaidShares(s4lConnector: connectors.S4LConnector,
                            whoRepaidSharesModel: WhoRepaidSharesModel)
                           (implicit hc: HeaderCarrier, user: TAVCUser): Future[SharesRepaymentDetailsModel] = {
    val idNotFound: Int = Constants.notFound

    require(whoRepaidSharesModel.processingId.getOrElse(0) > 0,
      "The item to update processingId must be an integer > 0")

    val result = s4lConnector.fetchAndGetFormData[Vector[SharesRepaymentDetailsModel]](KeystoreKeys.sharesRepaymentDetails).map {
      case Some(data) =>
        val itemToUpdateIndex = data.indexWhere(_.processingId.getOrElse(0) ==
          whoRepaidSharesModel.processingId.getOrElse(0))
        if (itemToUpdateIndex != idNotFound) {
          val sharesRepaymentDetails = data.lift(itemToUpdateIndex)
          data.updated(itemToUpdateIndex, sharesRepaymentDetails.get.copy(whoRepaidSharesModel = Some(whoRepaidSharesModel)))
        }
        else data
      case None => Vector[SharesRepaymentDetailsModel]()
    }
    result.flatMap(updatedVectorList => s4lConnector.saveFormData(KeystoreKeys.sharesRepaymentDetails, updatedVectorList))
    val model = for {
      sharesRepaymentDetails <- result
    } yield sharesRepaymentDetails.lift(sharesRepaymentDetails.indexWhere(_.processingId.getOrElse(0) == whoRepaidSharesModel.processingId.getOrElse(0))).get

    model
  }

  def addSharesRepaymentType(s4lConnector: connectors.S4LConnector,
                             sharesRepaymentTypeModel: SharesRepaymentTypeModel)
                           (implicit hc: HeaderCarrier, user: TAVCUser): Future[SharesRepaymentDetailsModel] = {

    val result = s4lConnector.fetchAndGetFormData[Vector[SharesRepaymentDetailsModel]](KeystoreKeys.sharesRepaymentDetails).map {
      case Some(data) =>
        val sharesRepaymentDetails = data.last
        data.updated(data.size - 1, sharesRepaymentDetails.copy(sharesRepaymentTypeModel =
          Some(sharesRepaymentTypeModel.copy(processingId = Some(data.last.processingId.get)))))
      case None =>
        throw throw new InternalServerException("No valid Investor information passed")
    }

    result.flatMap(newVectorList => s4lConnector.saveFormData(KeystoreKeys.sharesRepaymentDetails, newVectorList))

    val model = for {
      sharesRepaymentDetails <- result
    } yield sharesRepaymentDetails.last

    model
  }

  def updateSharesRepaymentType(s4lConnector: connectors.S4LConnector,
                                sharesRepaymentTypeModel: SharesRepaymentTypeModel)
                               (implicit hc: HeaderCarrier, user: TAVCUser): Future[SharesRepaymentDetailsModel] = {
    val idNotFound: Int = Constants.notFound

    require(sharesRepaymentTypeModel.processingId.getOrElse(0) > 0,
      "The item to update processingId must be an integer > 0")

    val result = s4lConnector.fetchAndGetFormData[Vector[SharesRepaymentDetailsModel]](KeystoreKeys.sharesRepaymentDetails).map {
      case Some(data) =>
        val itemToUpdateIndex = data.indexWhere(_.processingId.getOrElse(0) ==
          sharesRepaymentTypeModel.processingId.getOrElse(0))
        if (itemToUpdateIndex != idNotFound) {
          val sharesRepaymentDetails = data.lift(itemToUpdateIndex)
          data.updated(itemToUpdateIndex, sharesRepaymentDetails.get.copy(sharesRepaymentTypeModel = Some(sharesRepaymentTypeModel)))
        }
        else throw new InternalServerException("No valid Investor information passed")
      case None => throw throw new InternalServerException("No valid Investor information passed")
    }
    result.flatMap(updatedVectorList => s4lConnector.saveFormData(KeystoreKeys.sharesRepaymentDetails, updatedVectorList))
    val model = for {
      sharesRepaymentDetails <- result
    } yield sharesRepaymentDetails.lift(sharesRepaymentDetails.indexWhere(_.processingId.getOrElse(0) == sharesRepaymentTypeModel.processingId.getOrElse(0))).get

    model
  }

  def addDateSharesRepaid(s4lConnector: connectors.S4LConnector,
                        dateSharesRepaidModel: DateSharesRepaidModel)
                            (implicit hc: HeaderCarrier, user: TAVCUser): Future[SharesRepaymentDetailsModel] = {

    val result = s4lConnector.fetchAndGetFormData[Vector[SharesRepaymentDetailsModel]](KeystoreKeys.sharesRepaymentDetails).map {
      case Some(data) =>
        val sharesRepaymentDetails = data.last
        data.updated(data.size - 1, sharesRepaymentDetails.copy(dateSharesRepaidModel =
          Some(dateSharesRepaidModel.copy(processingId = Some(data.last.processingId.get)))))
      case None =>
        throw throw new InternalServerException("No valid Investor information passed")
    }

    result.flatMap(newVectorList => s4lConnector.saveFormData(KeystoreKeys.sharesRepaymentDetails, newVectorList))

    val model = for {
      sharesRepaymentDetails <- result
    } yield sharesRepaymentDetails.last

    model
  }

  def updateDateSharesRepaid(s4lConnector: connectors.S4LConnector,
                             dateSharesRepaidModel: DateSharesRepaidModel)
                          (implicit hc: HeaderCarrier, user: TAVCUser): Future[SharesRepaymentDetailsModel] = {
    val idNotFound: Int = Constants.notFound

    require(dateSharesRepaidModel.processingId.getOrElse(0) > 0,
      "The item to update processingId must be an integer > 0")

    val result = s4lConnector.fetchAndGetFormData[Vector[SharesRepaymentDetailsModel]](KeystoreKeys.sharesRepaymentDetails).map {
      case Some(data) =>
        val itemToUpdateIndex = data.indexWhere(_.processingId.getOrElse(0) ==
          dateSharesRepaidModel.processingId.getOrElse(0))
        if (itemToUpdateIndex != idNotFound) {
          val sharesRepaymentDetails = data.lift(itemToUpdateIndex)
          data.updated(itemToUpdateIndex, sharesRepaymentDetails.get.copy(dateSharesRepaidModel = Some(dateSharesRepaidModel)))
        }
        else throw new InternalServerException("No valid Investor information passed")
      case None => throw throw new InternalServerException("No valid Investor information passed")
    }
    result.flatMap(updatedVectorList => s4lConnector.saveFormData(KeystoreKeys.sharesRepaymentDetails, updatedVectorList))
    val model = for {
      sharesRepaymentDetails <- result
    } yield sharesRepaymentDetails.lift(sharesRepaymentDetails.indexWhere(_.processingId.getOrElse(0) == dateSharesRepaidModel.processingId.getOrElse(0))).get

    model
  }

  def addAmountSharesRepayment(s4lConnector: connectors.S4LConnector,
                               amountSharesRepaymentModel: AmountSharesRepaymentModel)
                              (implicit hc: HeaderCarrier, user: TAVCUser): Future[SharesRepaymentDetailsModel] = {

    val result = s4lConnector.fetchAndGetFormData[Vector[SharesRepaymentDetailsModel]](KeystoreKeys.sharesRepaymentDetails).map {
      case Some(data) =>
        val sharesRepaymentDetails = data.last
        data.updated(data.size - 1, sharesRepaymentDetails.copy(amountSharesRepaymentModel =
          Some(amountSharesRepaymentModel.copy(processingId = Some(data.last.processingId.get)))))
      case None =>
        throw throw new InternalServerException("No valid Investor information passed")
    }

    result.flatMap(newVectorList => s4lConnector.saveFormData(KeystoreKeys.sharesRepaymentDetails, newVectorList))

    val model = for {
      sharesRepaymentDetails <- result
    } yield sharesRepaymentDetails.last

    model
  }

  def updateAmountSharesRepayment(s4lConnector: connectors.S4LConnector,
                              amountSharesRepaymentModel: AmountSharesRepaymentModel)
                          (implicit hc: HeaderCarrier, user: TAVCUser): Future[SharesRepaymentDetailsModel] = {
    val idNotFound: Int = Constants.notFound

    require(amountSharesRepaymentModel.processingId.getOrElse(0) > 0,
      "The item to update processingId must be an integer > 0")

    val result = s4lConnector.fetchAndGetFormData[Vector[SharesRepaymentDetailsModel]](KeystoreKeys.sharesRepaymentDetails).map {
      case Some(data) =>
        val itemToUpdateIndex = data.indexWhere(_.processingId.getOrElse(0) ==
          amountSharesRepaymentModel.processingId.getOrElse(0))
        if (itemToUpdateIndex != idNotFound) {
          val sharesRepaymentDetails = data.lift(itemToUpdateIndex)
          data.updated(itemToUpdateIndex, sharesRepaymentDetails.get.copy(amountSharesRepaymentModel = Some(amountSharesRepaymentModel)))
        }
        else throw new InternalServerException("No valid Investor information passed")
      case None => throw new InternalServerException("No valid Investor information passed")
    }
    result.flatMap(updatedVectorList => s4lConnector.saveFormData(KeystoreKeys.sharesRepaymentDetails, updatedVectorList))
    val model = for {
      sharesRepaymentDetails <- result
    } yield sharesRepaymentDetails.lift(sharesRepaymentDetails.indexWhere(_.processingId.getOrElse(0) == amountSharesRepaymentModel.processingId.getOrElse(0))).get

    model
  }
}
