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

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.hmrc.http.{ HeaderCarrier, InternalServerException }

object PreviousInvestorShareHoldersHelper extends PreviousInvestorShareHoldersHelper {

}

trait PreviousInvestorShareHoldersHelper extends ControllerHelpers {

  def removePreviousShareHolders(s4lConnector: connectors.S4LConnector, investorProcessingId: Int, processingId: Int)
                                (implicit hc: HeaderCarrier, user: TAVCUser): Future[InvestorDetailsModel] = {

    require(investorProcessingId > 0, "The investorProcessingId must be an integer > 0")

    val result = s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map {
      case Some(data) =>
        val itemToUpdateIndex = data.indexWhere(_.processingId.getOrElse(0) == investorProcessingId)
        if (itemToUpdateIndex != -1) {
          val shareHoldings = data.lift(itemToUpdateIndex).get.previousShareHoldingModels.getOrElse(Vector.empty)
          if(shareHoldings.nonEmpty) {
            data.updated(itemToUpdateIndex,
              data.lift(itemToUpdateIndex).get.copy(previousShareHoldingModels =
                Some(shareHoldings.filter(_.processingId.getOrElse(0) != processingId))))
          }
          else
            data
        }
        else throw new InternalServerException("No valid Investor information passed")
      case None => throw new InternalServerException("No valid Investor information passed")
    }

    result.flatMap(deletedVectorList => s4lConnector.saveFormData(KeystoreKeys.investorDetails, deletedVectorList))

    val investor = for {
      investors <- result
    } yield investors.lift(investors.indexWhere(_.processingId.getOrElse(0) == investorProcessingId)).get

    investor
  }

  def clearPreviousInvestments(s4lConnector: connectors.S4LConnector)
                              (implicit hc: HeaderCarrier, user: TAVCUser): Future[CacheMap] = {
    s4lConnector.saveFormData(KeystoreKeys.investorDetails, Vector[InvestorDetailsModel]())
  }

  def getInvestorDataModel(index: Int, data :Vector[InvestorDetailsModel]): InvestorDetailsModel ={
    if(index != -1)
      data.lift(index).get
    else
      data.last
  }
  // assuming this is the initial page
  def addShareClassAndDescription(s4lConnector: connectors.S4LConnector,
                                  previousShareHoldingDescriptionModel: PreviousShareHoldingDescriptionModel,
                                  investorProcessingId: Int)
                                 (implicit hc: HeaderCarrier, user: TAVCUser): Future[PreviousShareHoldingModel] = {
    val defaultId: Int = 1
    val result = s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map {
      case Some(data) =>
        val itemToUpdateIndex = getInvestorIndex(investorProcessingId, data)
        val investorDetailsModel = getInvestorDataModel(itemToUpdateIndex, data)
        if (investorDetailsModel.previousShareHoldingModels.isDefined
          && investorDetailsModel.previousShareHoldingModels.get.nonEmpty) {
          addShareClassAndDescriptionToExisting(data, previousShareHoldingDescriptionModel,investorDetailsModel)
        }
        else {
          val newId = defaultId
          data.updated(itemToUpdateIndex, investorDetailsModel.copy(previousShareHoldingModels =
            Some(investorDetailsModel.previousShareHoldingModels.get :+ PreviousShareHoldingModel.apply(previousShareHoldingDescriptionModel =
              Some(previousShareHoldingDescriptionModel.copy(processingId = Some(newId),
                investorProcessingId = investorDetailsModel.processingId)), processingId = Some(newId),
              investorProcessingId = investorDetailsModel.processingId))))
        }
      case None => throw new InternalServerException("No valid Investor information passed")
    }
    result.flatMap(newVectorList => s4lConnector.saveFormData(KeystoreKeys.investorDetails, newVectorList))
    val model = for {
      investors <- result
    } yield investors.lift(getInvestorIndex(investorProcessingId, investors)).get.previousShareHoldingModels.get.last

    model
  }

  private def addShareClassAndDescriptionToExisting(data :Vector[InvestorDetailsModel],
                                                    previousShareHoldingDescriptionModel: PreviousShareHoldingDescriptionModel,
                                                    investorDetailsModel: InvestorDetailsModel) = {
    val defaultId: Int = 1

    val newId = investorDetailsModel.previousShareHoldingModels.get.last.processingId.get + defaultId
    if (investorDetailsModel.validate) {
      data.updated(getInvestorIndex(investorDetailsModel.processingId.get, data), investorDetailsModel.copy(previousShareHoldingModels =
        Some(investorDetailsModel.previousShareHoldingModels.get :+ PreviousShareHoldingModel.apply(previousShareHoldingDescriptionModel =
          Some(previousShareHoldingDescriptionModel.copy(processingId = Some(newId),
            investorProcessingId = investorDetailsModel.processingId)), processingId = Some(newId),
          investorProcessingId = investorDetailsModel.processingId))))
    }
    else {
      val previousShareHoldingModelObj = investorDetailsModel.previousShareHoldingModels.get.last
      data.updated(getInvestorIndex(investorDetailsModel.processingId.get, data), investorDetailsModel.copy(previousShareHoldingModels =
        Some(investorDetailsModel.previousShareHoldingModels.get.updated(investorDetailsModel.previousShareHoldingModels.get.size - defaultId,
          previousShareHoldingModelObj.copy(previousShareHoldingDescriptionModel =
            Some(previousShareHoldingDescriptionModel.copy(processingId = previousShareHoldingModelObj.processingId,
              investorProcessingId = investorDetailsModel.processingId)), processingId = Some(newId),
          investorProcessingId = investorDetailsModel.processingId)))))
    }
  }

  def updateShareClassAndDescription(s4lConnector: connectors.S4LConnector,
                                     previousShareHoldingDescriptionModel: PreviousShareHoldingDescriptionModel)
                                    (implicit hc: HeaderCarrier, user: TAVCUser): Future[PreviousShareHoldingModel] = {

    val result = s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map {
      case Some(data) =>
        val itemToUpdateIndex = data.indexWhere(_.processingId.getOrElse(0) ==
          previousShareHoldingDescriptionModel.investorProcessingId.getOrElse(0))
        if (itemToUpdateIndex != -1) {
          getUpdatePreviousIssuedShares(data, itemToUpdateIndex, previousShareHoldingDescriptionModel)
        }
        else throw new InternalServerException("No valid Investor information passed")
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

  private def getUpdatePreviousIssuedShares(data :Vector[InvestorDetailsModel],itemToUpdateIndex:Int,
                                            previousShareHoldingDescriptionModel:PreviousShareHoldingDescriptionModel): Vector[InvestorDetailsModel] = {
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

  // add logic for the middle flow
  def addNumberOfPreviouslyIssuedShares(s4lConnector: connectors.S4LConnector,
                                        numberOfPreviouslyIssuedShares: NumberOfPreviouslyIssuedSharesModel,
                                        investorProcessingId: Int)
                                       (implicit hc: HeaderCarrier, user: TAVCUser): Future[PreviousShareHoldingModel] = {

    val result = s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map {
      case Some(data) =>
        val itemToUpdateIndex = getInvestorIndex(investorProcessingId, data)
        val investorDetailsModel = getInvestorDataModel(itemToUpdateIndex, data)
        if (investorDetailsModel.previousShareHoldingModels.isDefined
          && investorDetailsModel.previousShareHoldingModels.get.nonEmpty) {
          val previousShareHoldingModelObj = investorDetailsModel.previousShareHoldingModels.get.last
          data.updated(itemToUpdateIndex, investorDetailsModel.copy(previousShareHoldingModels =
            Some(investorDetailsModel.previousShareHoldingModels.get.updated(investorDetailsModel.previousShareHoldingModels.get.size - 1,
              previousShareHoldingModelObj.copy(numberOfPreviouslyIssuedSharesModel =
                Some(numberOfPreviouslyIssuedShares.copy(processingId = previousShareHoldingModelObj.processingId,
                  investorProcessingId = investorDetailsModel.processingId)))))))
        }
        else throw new InternalServerException("No valid Investor information passed")

      case None => throw new InternalServerException("No valid Investor information passed")
    }

    result.flatMap(newVectorList => s4lConnector.saveFormData(KeystoreKeys.investorDetails, newVectorList))

    val model = for {
      investors <- result
    } yield investors.lift(getInvestorIndex(investorProcessingId, investors)).get.previousShareHoldingModels.get.last

    model
  }

  // Update these pages for the middle flow
  def updateNumberOfPreviouslyIssuedShares(s4lConnector: connectors.S4LConnector,
                                           numberOfPreviouslyIssuedSharesModel: NumberOfPreviouslyIssuedSharesModel)
                                          (implicit hc: HeaderCarrier, user: TAVCUser): Future[PreviousShareHoldingModel] = {

    val result = s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map {
      case Some(data) =>
        val itemToUpdateIndex = data.indexWhere(_.processingId.getOrElse(0) ==
          numberOfPreviouslyIssuedSharesModel.investorProcessingId.getOrElse(0))
        if (itemToUpdateIndex != -1) {

          updatePreviousIssuedShares(data, itemToUpdateIndex, numberOfPreviouslyIssuedSharesModel)
        }
        else throw new InternalServerException("No valid Investor information passed")
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

  private def updatePreviousIssuedShares(data :Vector[InvestorDetailsModel],itemToUpdateIndex:Int,
                                         numberOfPreviouslyIssuedSharesModel:NumberOfPreviouslyIssuedSharesModel): Vector[InvestorDetailsModel] = {

    val investorDetailsModel = data.lift(itemToUpdateIndex).get
    if (investorDetailsModel.previousShareHoldingModels.isDefined
      && investorDetailsModel.previousShareHoldingModels.get.nonEmpty) {
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

  // add logic for the middle flow
  def addPreviousShareHoldingNominalValue(s4lConnector: connectors.S4LConnector,
                                          previousShareHoldingNominalValueModel: PreviousShareHoldingNominalValueModel,
                                          investorProcessingId: Int)
                                         (implicit hc: HeaderCarrier, user: TAVCUser): Future[PreviousShareHoldingModel] = {

    val result = s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map {
      case Some(data) =>
        val itemToUpdateIndex = getInvestorIndex(investorProcessingId, data)
        val investorDetailsModel = getInvestorDataModel(itemToUpdateIndex, data)
        if (investorDetailsModel.previousShareHoldingModels.isDefined
          && investorDetailsModel.previousShareHoldingModels.get.nonEmpty) {
          val previousShareHoldingModelObj = investorDetailsModel.previousShareHoldingModels.get.last
          data.updated(itemToUpdateIndex, investorDetailsModel.copy(previousShareHoldingModels =
            Some(investorDetailsModel.previousShareHoldingModels.get.updated(investorDetailsModel.previousShareHoldingModels.get.size - 1,
              previousShareHoldingModelObj.copy(previousShareHoldingNominalValueModel =
                Some(previousShareHoldingNominalValueModel.copy(processingId = previousShareHoldingModelObj.processingId,
                  investorProcessingId = investorDetailsModel.processingId)))))))
        }
        else throw new InternalServerException("No valid Investor information passed")
      case None => throw new InternalServerException("No valid Investor information passed")
    }

    result.flatMap(newVectorList => s4lConnector.saveFormData(KeystoreKeys.investorDetails, newVectorList))

    val model = for {
      investors <- result
    } yield investors.lift(getInvestorIndex(investorProcessingId, investors)).get.previousShareHoldingModels.get.last

    model
  }

  // Update these pages for the middle flow
  def updatePreviousShareHoldingNominalValue(s4lConnector: connectors.S4LConnector,
                                             previousShareHoldingNominalValueModel: PreviousShareHoldingNominalValueModel)
                                            (implicit hc: HeaderCarrier, user: TAVCUser): Future[PreviousShareHoldingModel] = {

    val result = s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map {
      case Some(data) =>
        val itemToUpdateIndex = data.indexWhere(_.processingId.getOrElse(0) ==
          previousShareHoldingNominalValueModel.investorProcessingId.getOrElse(0))
        if (itemToUpdateIndex != -1) {
          updatePreviousIssuedNominalValue(data, itemToUpdateIndex, previousShareHoldingNominalValueModel)
        }
        else throw new InternalServerException("No valid Investor information passed")

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

  private def updatePreviousIssuedNominalValue(data :Vector[InvestorDetailsModel],itemToUpdateIndex:Int,
                                               previousShareHoldingNominalValueModel: PreviousShareHoldingNominalValueModel): Vector[InvestorDetailsModel] = {

    val investorDetailsModel = data.lift(itemToUpdateIndex).get
    if (investorDetailsModel.previousShareHoldingModels.isDefined
      && investorDetailsModel.previousShareHoldingModels.get.nonEmpty) {
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

  // add logic for the middle flow
  def addInvestorShareIssueDate(s4lConnector: connectors.S4LConnector,
                                investorShareIssueDateModel: InvestorShareIssueDateModel,
                                investorProcessingId: Int)
                               (implicit hc: HeaderCarrier, user: TAVCUser): Future[PreviousShareHoldingModel] = {

    val result = s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map {
      case Some(data) =>
        val itemToUpdateIndex = getInvestorIndex(investorProcessingId, data)
        val investorDetailsModel = getInvestorDataModel(itemToUpdateIndex, data)
        if (investorDetailsModel.previousShareHoldingModels.isDefined
          && investorDetailsModel.previousShareHoldingModels.get.nonEmpty) {
          val previousShareHoldingModelObj = investorDetailsModel.previousShareHoldingModels.get.last
          data.updated(itemToUpdateIndex, investorDetailsModel.copy(previousShareHoldingModels =
            Some(investorDetailsModel.previousShareHoldingModels.get.updated(investorDetailsModel.previousShareHoldingModels.get.size - 1,
              previousShareHoldingModelObj.copy(investorShareIssueDateModel =
                Some(investorShareIssueDateModel.copy(processingId = previousShareHoldingModelObj.processingId,
                  investorProcessingId = investorDetailsModel.processingId)))))))
        }
        else throw new InternalServerException("No valid Investor information passed")
      case None => throw new InternalServerException("No valid Investor information passed")
    }

    result.flatMap(newVectorList => s4lConnector.saveFormData(KeystoreKeys.investorDetails, newVectorList))

    val model = for {
      investors <- result
    } yield investors.lift(getInvestorIndex(investorProcessingId, investors)).get.previousShareHoldingModels.get.last

    model
  }

  // Update these pages for the middle flow
  def updateInvestorShareIssueDate(s4lConnector: connectors.S4LConnector,
                                   investorShareIssueDateModel: InvestorShareIssueDateModel)
                                  (implicit hc: HeaderCarrier, user: TAVCUser): Future[PreviousShareHoldingModel] = {

    val result = s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map {
      case Some(data) =>
        val itemToUpdateIndex = data.indexWhere(_.processingId.getOrElse(0) ==
          investorShareIssueDateModel.investorProcessingId.getOrElse(0))
        if (itemToUpdateIndex != -1) {

          updateShareIssueDate(data, itemToUpdateIndex, investorShareIssueDateModel)
        }
        else throw new InternalServerException("No valid Investor information passed")
      case None => throw new InternalServerException("No valid Investor information passed")
    }

    result.flatMap(newVectorList => s4lConnector.saveFormData(KeystoreKeys.investorDetails, newVectorList))

    val model = for {
      investors <- result
    } yield investors.lift(investors.indexWhere(_.processingId.getOrElse(0) ==
      investorShareIssueDateModel.investorProcessingId.getOrElse(0))).get.previousShareHoldingModels.get.lift(
      investors.lift(investors.indexWhere(_.processingId.getOrElse(0) ==
        investorShareIssueDateModel.investorProcessingId.getOrElse(0))).get.previousShareHoldingModels.get.
        indexWhere(_.processingId.getOrElse(0) == investorShareIssueDateModel.processingId.getOrElse(0))).get

    model
  }

  private def updateShareIssueDate(data :Vector[InvestorDetailsModel],itemToUpdateIndex:Int,
                                   investorShareIssueDateModel: InvestorShareIssueDateModel): Vector[InvestorDetailsModel] = {

    val investorDetailsModel = data.lift(itemToUpdateIndex).get
    if (investorDetailsModel.previousShareHoldingModels.isDefined
      && investorDetailsModel.previousShareHoldingModels.get.nonEmpty) {
      val shareHoldingsIndex = investorDetailsModel.previousShareHoldingModels.get.indexWhere(_.processingId.getOrElse(0) ==
        investorShareIssueDateModel.processingId.getOrElse(0))
      if (shareHoldingsIndex != -1) {
        val shareHoldingsModel = investorDetailsModel.previousShareHoldingModels.get.lift(shareHoldingsIndex).get

        data.updated(itemToUpdateIndex, investorDetailsModel.copy(previousShareHoldingModels =
          Some(investorDetailsModel.previousShareHoldingModels.get.updated(shareHoldingsIndex,
            shareHoldingsModel.copy(investorShareIssueDateModel =
              Some(investorShareIssueDateModel))))))
      }
      else throw new InternalServerException("No valid Investor information passed")
    }
    else throw new InternalServerException("No valid Investor information passed")
  }
}
