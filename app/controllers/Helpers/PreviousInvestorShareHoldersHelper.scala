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
import models.investorDetails._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.{HeaderCarrier, InternalServerException}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object PreviousInvestorShareHoldersHelper extends PreviousInvestorShareHoldersHelper {

}

trait PreviousInvestorShareHoldersHelper {

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

  // assuming this is the initial page
  def addShareClassAndDescription(s4lConnector: connectors.S4LConnector,
                                  previousShareHoldingDescriptionModel: PreviousShareHoldingDescriptionModel)
                                       (implicit hc: HeaderCarrier, user: TAVCUser): Future[PreviousShareHoldingModel] = {
    val defaultId: Int = 1
    val result = s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map {
      case Some(data) => {
          val investorDetailsModel = data.last
          if (investorDetailsModel.previousShareHoldingModels.isDefined
            && investorDetailsModel.previousShareHoldingModels.get.size > 0) {
            if (investorDetailsModel.validate) {
              val newId = investorDetailsModel.previousShareHoldingModels.get.last.processingId.get + defaultId
              data.updated(data.size -1, investorDetailsModel.copy(previousShareHoldingModels =
                Some(investorDetailsModel.previousShareHoldingModels.get :+ PreviousShareHoldingModel.apply(previousShareHoldingDescriptionModel =
                  Some(previousShareHoldingDescriptionModel.copy(processingId = Some(newId),
                    investorProcessingId = investorDetailsModel.processingId)), processingId = Some(newId),
                  investorProcessingId = investorDetailsModel.processingId))))
            }
            else {
              val previousShareHoldingModelObj = investorDetailsModel.previousShareHoldingModels.get.last
              data.updated(data.size -1, investorDetailsModel.copy(previousShareHoldingModels =
                Some(investorDetailsModel.previousShareHoldingModels.get.updated(investorDetailsModel.previousShareHoldingModels.get.size - defaultId,
                  previousShareHoldingModelObj.copy(previousShareHoldingDescriptionModel =
                    Some(previousShareHoldingDescriptionModel.copy(processingId = previousShareHoldingModelObj.processingId,
                      investorProcessingId = previousShareHoldingModelObj.investorProcessingId)))))))
            }
          }
          else {
            val newId = defaultId
            data.updated(data.size -1, investorDetailsModel.copy(previousShareHoldingModels =
              Some(investorDetailsModel.previousShareHoldingModels.get :+ PreviousShareHoldingModel.apply(previousShareHoldingDescriptionModel =
                Some(previousShareHoldingDescriptionModel.copy(processingId = Some(newId),
                  investorProcessingId = investorDetailsModel.processingId)), processingId = Some(newId),
                investorProcessingId = investorDetailsModel.processingId))))
          }
      }
      case None => throw new InternalServerException("No valid Investor information passed")
    }
    result.flatMap(newVectorList => s4lConnector.saveFormData(KeystoreKeys.investorDetails, newVectorList))
    val model = for {
      investors <- result
    } yield investors.last.previousShareHoldingModels.get.last

    model
  }

  def updateShareClassAndDescription(s4lConnector: connectors.S4LConnector,
                                     previousShareHoldingDescriptionModel: PreviousShareHoldingDescriptionModel)
                                       (implicit hc: HeaderCarrier, user: TAVCUser): Future[PreviousShareHoldingModel] = {

    val result = s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map {
      case Some(data) => {
        val itemToUpdateIndex = data.indexWhere(_.processingId.getOrElse(0) ==
          previousShareHoldingDescriptionModel.investorProcessingId.getOrElse(0))
        if (itemToUpdateIndex != -1) {
          val investorDetailsModel = data.lift(itemToUpdateIndex).get
          val shareHoldingsIndex = investorDetailsModel.previousShareHoldingModels.get.indexWhere(_.processingId.getOrElse(0) ==
            previousShareHoldingDescriptionModel.processingId.getOrElse(0))
          if (shareHoldingsIndex != -1) {
            val shareHoldingsModel = investorDetailsModel.previousShareHoldingModels.get.lift(shareHoldingsIndex).get

            data.updated(itemToUpdateIndex, investorDetailsModel.copy(previousShareHoldingModels =
              Some(investorDetailsModel.previousShareHoldingModels.get.updated(shareHoldingsIndex,
                shareHoldingsModel.copy(previousShareHoldingDescriptionModel =
                Some(previousShareHoldingDescriptionModel))))))

          }
          else throw new InternalServerException("No valid Investor information passed")
        }
        else throw new InternalServerException("No valid Investor information passed")
      }
      case None => throw new InternalServerException("No valid Investor information passed")
    }
    result.flatMap(newVectorList => s4lConnector.saveFormData(KeystoreKeys.investorDetails, newVectorList))

    val model = for {
      investors <- result
    } yield investors.lift(investors.indexWhere(_.processingId.getOrElse(0) ==
      previousShareHoldingDescriptionModel.investorProcessingId.getOrElse(0))).get.previousShareHoldingModels.get.lift(
      investors.lift(investors.indexWhere(_.processingId.getOrElse(0) ==
        previousShareHoldingDescriptionModel.investorProcessingId.getOrElse(0))).get.previousShareHoldingModels.get.
        indexWhere(_.processingId.getOrElse(0) == previousShareHoldingDescriptionModel.processingId.getOrElse(0))).get
    model
  }

  // add logic for the middle flow
  def addNumberOfPreviouslyIssuedShares(s4lConnector: connectors.S4LConnector,
                                        numberOfPreviouslyIssuedShares: NumberOfPreviouslyIssuedSharesModel)
                                       (implicit hc: HeaderCarrier, user: TAVCUser): Future[PreviousShareHoldingModel] = {
    val defaultId: Int = 1
    val result = s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map {
      case Some(data) => {
        val investorDetailsModel = data.last
        if (investorDetailsModel.previousShareHoldingModels.isDefined
          && investorDetailsModel.previousShareHoldingModels.get.size > 0) {
          val previousShareHoldingModelObj = investorDetailsModel.previousShareHoldingModels.get.last
          data.updated(data.size - 1, investorDetailsModel.copy(previousShareHoldingModels =
            Some(investorDetailsModel.previousShareHoldingModels.get.updated(investorDetailsModel.previousShareHoldingModels.get.size - 1,
              previousShareHoldingModelObj.copy(numberOfPreviouslyIssuedSharesModel =
                Some(numberOfPreviouslyIssuedShares.copy(processingId = previousShareHoldingModelObj.processingId,
                investorProcessingId = previousShareHoldingModelObj.investorProcessingId)))))))
        }
        else throw new InternalServerException("No valid Investor information passed")
      }
      case None => throw new InternalServerException("No valid Investor information passed")
    }

    result.flatMap(newVectorList => s4lConnector.saveFormData(KeystoreKeys.investorDetails, newVectorList))

    val model = for {
      investors <- result
    } yield investors.last.previousShareHoldingModels.get.last

    model
  }

  // Update these pages for the middle flow
  def updateNumberOfPreviouslyIssuedShares(s4lConnector: connectors.S4LConnector,
                                           numberOfPreviouslyIssuedSharesModel: NumberOfPreviouslyIssuedSharesModel)
                                          (implicit hc: HeaderCarrier, user: TAVCUser): Future[PreviousShareHoldingModel] = {

    val result = s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map {
      case Some(data) => {
        val itemToUpdateIndex = data.indexWhere(_.processingId.getOrElse(0) ==
          numberOfPreviouslyIssuedSharesModel.investorProcessingId.getOrElse(0))
        if (itemToUpdateIndex != -1) {
          val investorDetailsModel = data.lift(itemToUpdateIndex).get
          if (investorDetailsModel.previousShareHoldingModels.isDefined
            && investorDetailsModel.previousShareHoldingModels.get.size > 0) {
            val shareHoldingsIndex = investorDetailsModel.previousShareHoldingModels.get.indexWhere(_.processingId.getOrElse(0) ==
              numberOfPreviouslyIssuedSharesModel.processingId.getOrElse(0))
            if (shareHoldingsIndex != -1) {
              val shareHoldingsModel = investorDetailsModel.previousShareHoldingModels.get.lift(shareHoldingsIndex).get

              data.updated(itemToUpdateIndex, investorDetailsModel.copy(previousShareHoldingModels =
                Some(investorDetailsModel.previousShareHoldingModels.get.updated(shareHoldingsIndex,
                  shareHoldingsModel.copy(numberOfPreviouslyIssuedSharesModel =
                  Some(numberOfPreviouslyIssuedSharesModel))))))
            }
            else throw new InternalServerException("No valid Investor information passed")
          }
          else throw new InternalServerException("No valid Investor information passed")
        }
        else throw new InternalServerException("No valid Investor information passed")
      }
      case None => throw new InternalServerException("No valid Investor information passed")
    }

    result.flatMap(newVectorList => s4lConnector.saveFormData(KeystoreKeys.investorDetails, newVectorList))

    val model = for {
      investors <- result
    } yield investors.lift(investors.indexWhere(_.processingId.getOrElse(0) ==
      numberOfPreviouslyIssuedSharesModel.investorProcessingId.getOrElse(0))).get.previousShareHoldingModels.get.lift(
      investors.lift(investors.indexWhere(_.processingId.getOrElse(0) ==
        numberOfPreviouslyIssuedSharesModel.investorProcessingId.getOrElse(0))).get.previousShareHoldingModels.get.
        indexWhere(_.processingId.getOrElse(0) == numberOfPreviouslyIssuedSharesModel.processingId.getOrElse(0))).get

    model
  }

  // add logic for the middle flow
  def addPreviousShareHoldingNominalValue(s4lConnector: connectors.S4LConnector,
                                          previousShareHoldingNominalValueModel: PreviousShareHoldingNominalValueModel)
                                       (implicit hc: HeaderCarrier, user: TAVCUser): Future[PreviousShareHoldingModel] = {
    val defaultId: Int = 1
    val result = s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map {
      case Some(data) => {
        val investorDetailsModel = data.last
        if (investorDetailsModel.previousShareHoldingModels.isDefined
          && investorDetailsModel.previousShareHoldingModels.get.size > 0) {
          val previousShareHoldingModelObj = investorDetailsModel.previousShareHoldingModels.get.last
          data.updated(data.size - 1, investorDetailsModel.copy(previousShareHoldingModels =
            Some(investorDetailsModel.previousShareHoldingModels.get.updated(investorDetailsModel.previousShareHoldingModels.get.size - 1,
              previousShareHoldingModelObj.copy(previousShareHoldingNominalValueModel =
                Some(previousShareHoldingNominalValueModel.copy(processingId = previousShareHoldingModelObj.processingId,
                  investorProcessingId = previousShareHoldingModelObj.investorProcessingId)))))))
        }
        else throw new InternalServerException("No valid Investor information passed")
      }
      case None => throw new InternalServerException("No valid Investor information passed")
    }

    result.flatMap(newVectorList => s4lConnector.saveFormData(KeystoreKeys.investorDetails, newVectorList))

    val model = for {
      investors <- result
    } yield investors.last.previousShareHoldingModels.get.last

    model
  }

  // Update these pages for the middle flow
  def updatePreviousShareHoldingNominalValue(s4lConnector: connectors.S4LConnector,
                                             previousShareHoldingNominalValueModel: PreviousShareHoldingNominalValueModel)
                                          (implicit hc: HeaderCarrier, user: TAVCUser): Future[PreviousShareHoldingModel] = {

    val result = s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map {
      case Some(data) => {
        val itemToUpdateIndex = data.indexWhere(_.processingId.getOrElse(0) ==
          previousShareHoldingNominalValueModel.investorProcessingId.getOrElse(0))
        if (itemToUpdateIndex != -1) {
          val investorDetailsModel = data.lift(itemToUpdateIndex).get
          if (investorDetailsModel.previousShareHoldingModels.isDefined
            && investorDetailsModel.previousShareHoldingModels.get.size > 0) {
            val shareHoldingsIndex = investorDetailsModel.previousShareHoldingModels.get.indexWhere(_.processingId.getOrElse(0) ==
              previousShareHoldingNominalValueModel.processingId.getOrElse(0))
            if (shareHoldingsIndex != -1) {
              val shareHoldingsModel = investorDetailsModel.previousShareHoldingModels.get.lift(shareHoldingsIndex).get

              data.updated(itemToUpdateIndex, investorDetailsModel.copy(previousShareHoldingModels =
                Some(investorDetailsModel.previousShareHoldingModels.get.updated(shareHoldingsIndex,
                  shareHoldingsModel.copy(previousShareHoldingNominalValueModel =
                    Some(previousShareHoldingNominalValueModel))))))
            }
            else throw new InternalServerException("No valid Investor information passed")
          }
          else throw new InternalServerException("No valid Investor information passed")
        }
        else throw new InternalServerException("No valid Investor information passed")
      }
      case None => throw new InternalServerException("No valid Investor information passed")
    }

    result.flatMap(newVectorList => s4lConnector.saveFormData(KeystoreKeys.investorDetails, newVectorList))

    val model = for {
      investors <- result
    } yield investors.lift(investors.indexWhere(_.processingId.getOrElse(0) ==
      previousShareHoldingNominalValueModel.investorProcessingId.getOrElse(0))).get.previousShareHoldingModels.get.lift(
      investors.lift(investors.indexWhere(_.processingId.getOrElse(0) ==
        previousShareHoldingNominalValueModel.investorProcessingId.getOrElse(0))).get.previousShareHoldingModels.get.
        indexWhere(_.processingId.getOrElse(0) == previousShareHoldingNominalValueModel.processingId.getOrElse(0))).get

    model
  }
}
