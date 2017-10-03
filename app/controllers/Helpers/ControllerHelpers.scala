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
import controllers.seis.routes
import models._
import models.investorDetails.{InvestorDetailsModel, PreviousShareHoldingModel}
import models.repayments.SharesRepaymentDetailsModel
import models.submission.SchemeTypesModel
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.Results._
import play.api.mvc.{AnyContent, Request, Result}
import uk.gov.hmrc.play.http.HeaderCarrier
import utils.Validation

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object ControllerHelpers extends ControllerHelpers {

}

trait ControllerHelpers {

  def getSavedBackLink(keystoreKey: String, s4lConnector: connectors.S4LConnector)
                      (implicit hc: HeaderCarrier, user: TAVCUser): Future[Option[String]] = {
    s4lConnector.fetchAndGetFormData[String](keystoreKey).flatMap {
      case Some(data) => Future.successful(Some(data))
      case None => Future.successful(None)
    }
  }

  /** Helper method to return the correct trade start date depending on whether the business activity is tarde or R&D.
    * A single TradeStartDateModel is returned form checking S4L.
    * If expected values are not found None is returned
    *
    * @param s4lConnector An instance of the Save4Later Connector.
    */
  def getTradeStartDateForBusinessActivity(s4lConnector: connectors.S4LConnector)
                                          (implicit hc: HeaderCarrier, user: TAVCUser): Future[Option[HasInvestmentTradeStartedModel]] = {

    def getDate(businessActivity: Option[QualifyBusinessActivityModel],
                researchDateStarted: Option[ResearchStartDateModel],
                tradeStartDateStarted: Option[HasInvestmentTradeStartedModel]) = {

      if (businessActivity.nonEmpty) {
        businessActivity.get.isQualifyBusinessActivity match {
          case Constants.qualifyTrade => Future.successful(tradeStartDateStarted)
          case Constants.qualifyResearchAndDevelopment => if (researchDateStarted.nonEmpty)
            Future.successful(
              Some(HasInvestmentTradeStartedModel(researchDateStarted.get.hasStartedResearch, researchDateStarted.get.researchStartDay,
                researchDateStarted.get.researchStartMonth, researchDateStarted.get.researchStartYear)))
          else Future.successful(None)
        }
      } else {
        Future.successful(None)
      }
    }

    for {
      businessActivity <- s4lConnector.fetchAndGetFormData[QualifyBusinessActivityModel](KeystoreKeys.isQualifyBusinessActivity)
      researchDate <- s4lConnector.fetchAndGetFormData[ResearchStartDateModel](KeystoreKeys.researchStartDate)
      tradeStartDate <- s4lConnector.fetchAndGetFormData[HasInvestmentTradeStartedModel](KeystoreKeys.hasInvestmentTradeStarted)
      date <- getDate(businessActivity, researchDate, tradeStartDate)

    } yield date

  }

  def routeToScheme(schemeTypesModel: SchemeTypesModel)(implicit request: Request[AnyContent]): String = {
    schemeTypesModel match {
      //EIS Flow
      case SchemeTypesModel(true, false, false, false) => controllers.eis.routes.NatureOfBusinessController.show().url
      //SEIS Flow
      case SchemeTypesModel(false, true, false, false) => controllers.seis.routes.NatureOfBusinessController.show().url
      //VCT Flow
      case SchemeTypesModel(false, false, false, true) => controllers.eis.routes.NatureOfBusinessController.show().url
      //EIS VCT Flow
      case SchemeTypesModel(true, false, false, true) => controllers.eis.routes.NatureOfBusinessController.show().url
      //Assume EIS
      case _ => controllers.eis.routes.NatureOfBusinessController.show().url
    }
  }

  def schemeDescriptionFromTypes(schemeTypesModel: Option[SchemeTypesModel])(implicit request: Request[AnyContent], messages: Messages): String = {
    schemeTypesModel match {
      //EIS Flow
      case Some(SchemeTypesModel(true, false, false, false)) => Messages("page.introduction.hub.existing.advanced.assurance.type")
      //SEIS Flow
      case Some(SchemeTypesModel(false, true, false, false)) => Messages("page.introduction.hub.existing.seis.type")
      //Assume EIS
      case Some(_) => Messages("page.introduction.hub.existing.advanced.assurance.type")
      //Assume EIS
      case None => Messages("page.introduction.hub.existing.advanced.assurance.type")
    }
  }

  def useInvestorOrNomineeValueAsHeadingText(investorOrNominee: AddInvestorOrNomineeModel): String = investorOrNominee.addInvestorOrNominee.toLowerCase

  def redirectNoInvestors(vector: Option[Vector[InvestorDetailsModel]])(f: Vector[InvestorDetailsModel] => Result): Result = {
    vector match {
      case Some(data) if data.nonEmpty => f(data)
      case _ => Redirect(controllers.seis.routes.AddInvestorOrNomineeController.show())
    }
  }

  def redirectEisNoInvestors(vector: Option[Vector[InvestorDetailsModel]])(f: Vector[InvestorDetailsModel] => Result): Result = {
    vector match {
      case Some(data) if data.nonEmpty => f(data)
      case _ => Redirect(controllers.eis.routes.AddInvestorOrNomineeController.show())
    }
  }

  def getInvestorIndex(targetIndex: Int, data: Vector[InvestorDetailsModel]): Int = {
    data.indexWhere(_.processingId.getOrElse(0) == targetIndex)
  }

  def getShareIndex(targetIndex: Int, data: Vector[PreviousShareHoldingModel]): Int = {
    data.indexWhere(_.processingId.getOrElse(0) == targetIndex)
  }

  def redirectInvalidInvestor(index: Int)(f: Int => Result): Result = {
    if (index != Constants.notFound) {
      f(index)
    } else {
      Redirect(routes.AddInvestorOrNomineeController.show())
    }
  }

  def redirectEisInvalidInvestor(index: Int)(f: Int => Result): Result = {
    if (index != Constants.notFound) {
      f(index)
    } else {
      Redirect(controllers.eis.routes.AddInvestorOrNomineeController.show())
    }
  }

  def retrieveInvestorData[T](index: Int, investors: Vector[InvestorDetailsModel])(f: InvestorDetailsModel => Option[T]): Option[T] = {
    investors.lift(index).flatMap(f)
  }

  def retrieveShareData[T](index: Int, shares: Option[Vector[PreviousShareHoldingModel]])(f: PreviousShareHoldingModel => Option[T]): Option[T] = {
    shares.flatMap(_.lift(index).flatMap(f))
  }

  def fillForm[T](form: Form[T], data: Option[T]): Form[T] = {
    data match {
      case Some(model) => form.fill(model)
      case _ => form
    }
  }

  def redirectInvalidPreviousShareHolding(id: Int, index: Int, shares: Option[Vector[PreviousShareHoldingModel]])(f: Int => Result): Result = {
    shares.map { data =>
      if (data.nonEmpty && id != -1)
        f(id)
      else None
    } match {
      case Some(result: Result) => result
      case _ => Redirect(routes.AddInvestorOrNomineeController.show(Some(index)))
    }
  }

  def redirectEisInvalidPreviousShareHolding(id: Int, index: Int, shares: Option[Vector[PreviousShareHoldingModel]])(f: Int => Result): Result = {
    shares.map { data =>
      if (data.nonEmpty && id != -1)
        f(id)
      else None
    } match {
      case Some(result: Result) => result
      case _ => Redirect(controllers.eis.routes.AddInvestorOrNomineeController.show(Some(index)))
    }
  }

  def redirectGrossAssetsAfterIssue(dateOfIncorporationModel: Option[DateOfIncorporationModel],
                                    s4lConnector: connectors.S4LConnector)(implicit hc: HeaderCarrier, user: TAVCUser): Result = {
    if(dateOfIncorporationModel.isDefined && Validation.dateAfterIncorporationRule(dateOfIncorporationModel.get.day.get,
      dateOfIncorporationModel.get.month.get, dateOfIncorporationModel.get.year.get)){
      s4lConnector.saveFormData(KeystoreKeys.backLinkFullTimeEmployeeCount,
        controllers.eis.routes.GrossAssetsAfterIssueController.show().url)
      Redirect(controllers.eis.routes.FullTimeEmployeeCountController.show)
    }
    else Redirect(controllers.eis.routes.IsCompanyKnowledgeIntensiveController.show)
  }

  def getRepaymentsIndex(targetIndex: Int, data: Vector[SharesRepaymentDetailsModel]): Int = {
    data.indexWhere(_.processingId.getOrElse(0) == targetIndex)
  }

  def redirectInvalidRepayments(index: Int)(f: Int => Result): Result = {
    if (index != Constants.notFound) {
      f(index)
    } else {
      Redirect(controllers.eis.routes.AnySharesRepaymentController.show())
    }
  }

  def retrieveRepaymentsData[T](index: Int, repayments: Vector[SharesRepaymentDetailsModel])(f: SharesRepaymentDetailsModel => Option[T]): Option[T] = {
    repayments.lift(index).flatMap(f)
  }

  def redirectIfNoRepayments(vector: Option[Vector[SharesRepaymentDetailsModel]])(f: Vector[SharesRepaymentDetailsModel] => Result): Result = {
    vector match {
      case Some(data) if data.nonEmpty => f(data)
      case _ => Redirect(controllers.eis.routes.AnySharesRepaymentController.show())
    }
  }
}
