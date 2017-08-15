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
import models._
import models.investorDetails._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.{HeaderCarrier, InternalServerException}

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
      case Some(data) =>
        val newId = data.last.processingId.get + 1
        data :+ investorDetailsModel.copy(processingId = Some(newId))
      case None => Vector.empty :+ investorDetailsModel.copy(processingId = Some(defaultId))
    }.recover { case _ => Vector.empty :+ investorDetailsModel.copy(processingId = Some(defaultId)) }

    result.flatMap(newVectorList => s4lConnector.saveFormData(KeystoreKeys.investorDetails, newVectorList))
    result
  }

  def updateKeystorePreviousInvestment(s4lConnector: connectors.S4LConnector,
                                           investorDetailsModel: InvestorDetailsModel)
                                          (implicit hc: HeaderCarrier, user: TAVCUser): Future[Vector[InvestorDetailsModel]] = {
    val idNotFound: Int = Constants.notFound

    require(investorDetailsModel.processingId.getOrElse(0) > 0,
      "The item to update processingId must be an integer > 0")

    val result = s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map {
      case Some(data) =>
        val itemToUpdateIndex = data.indexWhere(_.processingId.getOrElse(0) ==
          investorDetailsModel.processingId.getOrElse(0))
        if (itemToUpdateIndex != idNotFound) {
          data.updated(itemToUpdateIndex, investorDetailsModel)
        }
        else data
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

  def addInvestorOrNominee(s4lConnector: connectors.S4LConnector,
                            addInvestorOrNomineeModel : AddInvestorOrNomineeModel)
                                         (implicit hc: HeaderCarrier, user: TAVCUser): Future[InvestorDetailsModel] = {
    val defaultId: Int = 1

    val result = s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map {
      case Some(data) =>
        if (data.nonEmpty && data.last.validate) {
          val newId = data.last.processingId.get + 1
          data :+ InvestorDetailsModel.apply(investorOrNomineeModel = Some(addInvestorOrNomineeModel.copy(processingId = Some(newId))),
            processingId = Some(newId))
        }
        else{
          data.updated(data.size - 1, data.last.copy(investorOrNomineeModel =
            Some(addInvestorOrNomineeModel.copy(processingId = data.last.processingId))))
        }
      case None =>
        Vector.empty :+ InvestorDetailsModel.apply(investorOrNomineeModel = Some(addInvestorOrNomineeModel.copy(processingId = Some(defaultId))),
        processingId = Some(defaultId))
    }.recover { case _ => Vector.empty :+ InvestorDetailsModel.apply(investorOrNomineeModel = Some(addInvestorOrNomineeModel.copy(processingId = Some(defaultId))),
      processingId = Some(defaultId)) }

    result.flatMap(newVectorList => s4lConnector.saveFormData(KeystoreKeys.investorDetails, newVectorList))

    val model = for {
      investorDetails <- result
    } yield investorDetails.last

    model
  }

  def updateInvestorOrNominee(s4lConnector: connectors.S4LConnector,
                               addInvestorOrNomineeModel : AddInvestorOrNomineeModel)
                                          (implicit hc: HeaderCarrier, user: TAVCUser): Future[InvestorDetailsModel] = {
    val idNotFound: Int = Constants.notFound

    require(addInvestorOrNomineeModel.processingId.getOrElse(0) > 0,
      "The item to update processingId must be an integer > 0")

    val result = s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map {
      case Some(data) =>
        val itemToUpdateIndex = data.indexWhere(_.processingId.getOrElse(0) ==
          addInvestorOrNomineeModel.processingId.getOrElse(0))
        if (itemToUpdateIndex != idNotFound) {
          val investorDetailsModel = data.lift(itemToUpdateIndex)
          data.updated(itemToUpdateIndex, investorDetailsModel.get.copy(investorOrNomineeModel = Some(addInvestorOrNomineeModel)))
        }
        else data
      case None => Vector[InvestorDetailsModel]()
    }
    result.flatMap(updatedVectorList => s4lConnector.saveFormData(KeystoreKeys.investorDetails, updatedVectorList))
    val model = for {
      investorDetails <- result
    } yield investorDetails.lift(investorDetails.indexWhere(_.processingId.getOrElse(0) == addInvestorOrNomineeModel.processingId.getOrElse(0))).get

    model
  }

  def updateCompanyOrIndividual(s4lConnector: connectors.S4LConnector,
                                companyOrIndividualModel: CompanyOrIndividualModel)
                              (implicit hc: HeaderCarrier, user: TAVCUser): Future[InvestorDetailsModel] = {
    val idNotFound: Int = Constants.notFound

    require(companyOrIndividualModel.processingId.getOrElse(0) > 0,
      "The item to update processingId must be an integer > 0")

    val result = s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map {
      case Some(data) =>
        val itemToUpdateIndex = data.indexWhere(_.processingId.getOrElse(0) ==
          companyOrIndividualModel.processingId.getOrElse(0))
        if (itemToUpdateIndex != idNotFound) {
          val investorDetailsModel = data.lift(itemToUpdateIndex)
          data.updated(itemToUpdateIndex, investorDetailsModel.get.copy(companyOrIndividualModel = Some(companyOrIndividualModel)))
        }
        else throw new InternalServerException("No valid Investor information passed")
      case None => throw throw new InternalServerException("No valid Investor information passed")
    }
    result.flatMap(updatedVectorList => s4lConnector.saveFormData(KeystoreKeys.investorDetails, updatedVectorList))
    val model = for {
      investorDetails <- result
    } yield investorDetails.lift(investorDetails.indexWhere(_.processingId.getOrElse(0) == companyOrIndividualModel.processingId.getOrElse(0))).get

    model
  }

  def addCompanyOrIndividual(s4lConnector: connectors.S4LConnector,
                             companyOrIndividualModel: CompanyOrIndividualModel)
                           (implicit hc: HeaderCarrier, user: TAVCUser): Future[InvestorDetailsModel] = {

    val result = s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map {
      case Some(data) =>
        val investorDetailsModel = data.last
        data.updated(data.size - 1, investorDetailsModel.copy(companyOrIndividualModel =
          Some(companyOrIndividualModel.copy(processingId = Some(data.last.processingId.get)))))
      case None =>
        throw throw new InternalServerException("No valid Investor information passed")
    }

    result.flatMap(newVectorList => s4lConnector.saveFormData(KeystoreKeys.investorDetails, newVectorList))

    val model = for {
      investors <- result
    } yield investors.last

    model
  }

  def updateCompanyDetails(s4lConnector: connectors.S4LConnector,
                                companyDetailsModel: CompanyDetailsModel)
                               (implicit hc: HeaderCarrier, user: TAVCUser): Future[InvestorDetailsModel] = {
    val idNotFound: Int = Constants.notFound

    require(companyDetailsModel.processingId.getOrElse(0) > 0,
      "The item to update processingId must be an integer > 0")

    val result = s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map {
      case Some(data) =>
        val itemToUpdateIndex = data.indexWhere(_.processingId.getOrElse(0) ==
          companyDetailsModel.processingId.getOrElse(0))
        if (itemToUpdateIndex != idNotFound) {
          val investorDetailsModel = data.lift(itemToUpdateIndex)
          data.updated(itemToUpdateIndex, investorDetailsModel.get.copy(companyDetailsModel = Some(companyDetailsModel)))
        }
        else throw new InternalServerException("No valid Investor information passed")
      case None => throw throw new InternalServerException("No valid Investor information passed")
    }
    result.flatMap(updatedVectorList => s4lConnector.saveFormData(KeystoreKeys.investorDetails, updatedVectorList))
    val model = for {
      investorDetails <- result
    } yield investorDetails.lift(investorDetails.indexWhere(_.processingId.getOrElse(0) == companyDetailsModel.processingId.getOrElse(0))).get

    model
  }

  def addCompanyDetails(s4lConnector: connectors.S4LConnector,
                        companyDetailsModel: CompanyDetailsModel)
                            (implicit hc: HeaderCarrier, user: TAVCUser): Future[InvestorDetailsModel] = {

    val result = s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map {
      case Some(data) =>
        val investorDetailsModel = data.last
        data.updated(data.size - 1, investorDetailsModel.copy(companyDetailsModel =
          Some(companyDetailsModel.copy(processingId = Some(data.last.processingId.get)))))
      case None =>
        throw throw new InternalServerException("No valid Investor information passed")
    }

    result.flatMap(newVectorList => s4lConnector.saveFormData(KeystoreKeys.investorDetails, newVectorList))

    val model = for {
      investorDetails <- result
    } yield investorDetails.last

    model
  }

  def updateIndividualDetails(s4lConnector: connectors.S4LConnector,
                              individualDetailsModel: IndividualDetailsModel)
                          (implicit hc: HeaderCarrier, user: TAVCUser): Future[InvestorDetailsModel] = {
    val idNotFound: Int = Constants.notFound

    require(individualDetailsModel.processingId.getOrElse(0) > 0,
      "The item to update processingId must be an integer > 0")

    val result = s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map {
      case Some(data) =>
        val itemToUpdateIndex = data.indexWhere(_.processingId.getOrElse(0) ==
          individualDetailsModel.processingId.getOrElse(0))
        if (itemToUpdateIndex != idNotFound) {
          val investorDetailsModel = data.lift(itemToUpdateIndex)
          data.updated(itemToUpdateIndex, investorDetailsModel.get.copy(individualDetailsModel = Some(individualDetailsModel)))
        }
        else throw new InternalServerException("No valid Investor information passed")
      case None => throw new InternalServerException("No valid Investor information passed")
    }
    result.flatMap(updatedVectorList => s4lConnector.saveFormData(KeystoreKeys.investorDetails, updatedVectorList))
    val model = for {
      investorDetails <- result
    } yield investorDetails.lift(investorDetails.indexWhere(_.processingId.getOrElse(0) == individualDetailsModel.processingId.getOrElse(0))).get

    model
  }

  def addIndividualDetails(s4lConnector: connectors.S4LConnector,
                           individualDetailsModel: IndividualDetailsModel)
                       (implicit hc: HeaderCarrier, user: TAVCUser): Future[InvestorDetailsModel] = {

    val result = s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map {
      case Some(data) =>
        val investorDetailsModel = data.last
        data.updated(data.size - 1, investorDetailsModel.copy(individualDetailsModel =
          Some(individualDetailsModel.copy(processingId = Some(data.last.processingId.get)))))
      case None =>
        throw throw new InternalServerException("No valid Investor information passed")
    }

    result.flatMap(newVectorList => s4lConnector.saveFormData(KeystoreKeys.investorDetails, newVectorList))

    val model = for {
      investorDetails <- result
    } yield investorDetails.last

    model
  }

  def updateNumOfSharesPurchasedDetails(s4lConnector: connectors.S4LConnector,
                                        numberOfSharesPurchasedModel: NumberOfSharesPurchasedModel)
                             (implicit hc: HeaderCarrier, user: TAVCUser): Future[InvestorDetailsModel] = {
    val idNotFound: Int = Constants.notFound

    require(numberOfSharesPurchasedModel.processingId.getOrElse(0) > 0,
      "The item to update processingId must be an integer > 0")

    val result = s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map {
      case Some(data) =>
        val itemToUpdateIndex = data.indexWhere(_.processingId.getOrElse(0) ==
          numberOfSharesPurchasedModel.processingId.getOrElse(0))
        if (itemToUpdateIndex != idNotFound) {
          val investorDetailsModel = data.lift(itemToUpdateIndex)
          data.updated(itemToUpdateIndex, investorDetailsModel.get.copy(numberOfSharesPurchasedModel = Some(numberOfSharesPurchasedModel)))
        }
        else throw new InternalServerException("No valid Investor information passed")
      case None => throw new InternalServerException("No valid Investor information passed")
    }
    result.flatMap(updatedVectorList => s4lConnector.saveFormData(KeystoreKeys.investorDetails, updatedVectorList))
    val model = for {
      investorDetails <- result
    } yield investorDetails.lift(investorDetails.indexWhere(_.processingId.getOrElse(0) == numberOfSharesPurchasedModel.processingId.getOrElse(0))).get

    model
  }

  def addNumOfSharesPurchasedDetails(s4lConnector: connectors.S4LConnector,
                                     numberOfSharesPurchasedModel: NumberOfSharesPurchasedModel)
                          (implicit hc: HeaderCarrier, user: TAVCUser): Future[InvestorDetailsModel] = {

    val result = s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map {
      case Some(data) =>
        val investorDetailsModel = data.last
        data.updated(data.size - 1, investorDetailsModel.copy(numberOfSharesPurchasedModel =
          Some(numberOfSharesPurchasedModel.copy(processingId = Some(data.last.processingId.get)))))
      case None =>
        throw throw new InternalServerException("No valid Investor information passed")
    }

    result.flatMap(newVectorList => s4lConnector.saveFormData(KeystoreKeys.investorDetails, newVectorList))

    val model = for {
      investorDetails <- result
    } yield investorDetails.last

    model
  }

  def updateAmountSpentOnSharesDetails(s4lConnector: connectors.S4LConnector,
                                       howMuchSpentOnSharesModel: HowMuchSpentOnSharesModel)
                                       (implicit hc: HeaderCarrier, user: TAVCUser): Future[InvestorDetailsModel] = {
    val idNotFound: Int = Constants.notFound

    require(howMuchSpentOnSharesModel.processingId.getOrElse(0) > 0,
      "The item to update processingId must be an integer > 0")

    val result = s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map {
      case Some(data) =>
        val itemToUpdateIndex = data.indexWhere(_.processingId.getOrElse(0) ==
          howMuchSpentOnSharesModel.processingId.getOrElse(0))
        if (itemToUpdateIndex != idNotFound) {
          val investorDetailsModel = data.lift(itemToUpdateIndex)
          data.updated(itemToUpdateIndex, investorDetailsModel.get.copy(amountSpentModel = Some(howMuchSpentOnSharesModel)))
        }
        else throw new InternalServerException("No valid Investor information passed")
      case None => throw new InternalServerException("No valid Investor information passed")
    }
    result.flatMap(updatedVectorList => s4lConnector.saveFormData(KeystoreKeys.investorDetails, updatedVectorList))
    val model = for {
      investorDetails <- result
    } yield investorDetails.lift(investorDetails.indexWhere(_.processingId.getOrElse(0) == howMuchSpentOnSharesModel.processingId.getOrElse(0))).get

    model
  }

  def addAmountSpentOnSharesDetails(s4lConnector: connectors.S4LConnector,
                                    howMuchSpentOnSharesModel: HowMuchSpentOnSharesModel)
                                    (implicit hc: HeaderCarrier, user: TAVCUser): Future[InvestorDetailsModel] = {

    val result = s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map {
      case Some(data) =>
        val investorDetailsModel = data.last
        data.updated(data.size - 1, investorDetailsModel.copy(amountSpentModel =
          Some(howMuchSpentOnSharesModel.copy(processingId = Some(data.last.processingId.get)))))
      case None =>
        throw throw new InternalServerException("No valid Investor information passed")
    }

    result.flatMap(newVectorList => s4lConnector.saveFormData(KeystoreKeys.investorDetails, newVectorList))

    val model = for {
      investorDetails <- result
    } yield investorDetails.last

    model
  }

  def updateIsExistingShareHoldersDetails(s4lConnector: connectors.S4LConnector,
                                          isExistingShareHolderModel: IsExistingShareHolderModel)
                                      (implicit hc: HeaderCarrier, user: TAVCUser): Future[InvestorDetailsModel] = {
    val idNotFound: Int = Constants.notFound

    require(isExistingShareHolderModel.processingId.getOrElse(0) > 0,
      "The item to update processingId must be an integer > 0")

    val result = s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map {
      case Some(data) =>
        val itemToUpdateIndex = data.indexWhere(_.processingId.getOrElse(0) ==
          isExistingShareHolderModel.processingId.getOrElse(0))
        if (itemToUpdateIndex != idNotFound) {
          val investorDetailsModel = data.lift(itemToUpdateIndex)
          data.updated(itemToUpdateIndex, investorDetailsModel.get.copy(isExistingShareHolderModel = Some(isExistingShareHolderModel)))
        }
        else throw new InternalServerException("No valid Investor information passed")
      case None => throw new InternalServerException("No valid Investor information passed")
    }
    result.flatMap(updatedVectorList => s4lConnector.saveFormData(KeystoreKeys.investorDetails, updatedVectorList))
    val model = for {
      investorDetails <- result
    } yield investorDetails.lift(investorDetails.indexWhere(_.processingId.getOrElse(0) == isExistingShareHolderModel.processingId.getOrElse(0))).get

    model
  }

  def addIsExistingShareHoldersDetails(s4lConnector: connectors.S4LConnector,
                                    isExistingShareHolderModel: IsExistingShareHolderModel)
                                   (implicit hc: HeaderCarrier, user: TAVCUser): Future[InvestorDetailsModel] = {

    val result = s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map {
      case Some(data) =>
        val investorDetailsModel = data.last
        data.updated(data.size - 1, investorDetailsModel.copy(isExistingShareHolderModel =
          Some(isExistingShareHolderModel.copy(processingId = Some(data.last.processingId.get))),
          previousShareHoldingModels = Some(Vector[PreviousShareHoldingModel]())))
      case None =>
        throw throw new InternalServerException("No valid Investor information passed")
    }

    result.flatMap(newVectorList => s4lConnector.saveFormData(KeystoreKeys.investorDetails, newVectorList))

    val model = for {
      investorDetails <- result
    } yield investorDetails.last

    model
  }
}
