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

package models.repayments

import play.api.i18n.Messages
import play.api.libs.json.Json
import utils.{CostFormatter, DateFormatter}
import play.api.Play.current
import play.api.i18n.Messages.Implicits._

case class SharesRepaymentDetailsModel(whoRepaidSharesModel: Option[WhoRepaidSharesModel] = None,
                                       sharesRepaymentTypeModel: Option[SharesRepaymentTypeModel] = None,
                                       dateSharesRepaidModel: Option[DateSharesRepaidModel] = None,
                                       amountSharesRepaymentModel: Option[AmountSharesRepaymentModel] = None,
                                       processingId: Option[Int] = None) {

  def validate: Boolean = {
      whoRepaidSharesModel.isDefined && sharesRepaymentTypeModel.isDefined &&
        dateSharesRepaidModel.isDefined && amountSharesRepaymentModel.isDefined
  }
}

object SharesRepaymentDetailsModel extends DateFormatter with CostFormatter{
  implicit val formats = Json.format[SharesRepaymentDetailsModel]

  def toArrayString(sharesRepaymentDetailsModel: SharesRepaymentDetailsModel): Array[String] = {
    val repaymentType = if(sharesRepaymentDetailsModel.whoRepaidSharesModel.isDefined)
      s"${Messages("page.repayments.type.label")} ${sharesRepaymentDetailsModel.sharesRepaymentTypeModel.get.sharesRepaymentType}" else ""
    val repaymentDate = if(sharesRepaymentDetailsModel.dateSharesRepaidModel.isDefined && sharesRepaymentDetailsModel.dateSharesRepaidModel.get.day.isDefined &&
      sharesRepaymentDetailsModel.dateSharesRepaidModel.get.month.isDefined && sharesRepaymentDetailsModel.dateSharesRepaidModel.get.year.isDefined)
      s"${Messages("page.repayments.date.label")} ${toDateString(sharesRepaymentDetailsModel.dateSharesRepaidModel.get.day.get,
      sharesRepaymentDetailsModel.dateSharesRepaidModel.get.month.get,sharesRepaymentDetailsModel.dateSharesRepaidModel.get.year.get)}" else ""

    val repaymentAmount = if(sharesRepaymentDetailsModel.amountSharesRepaymentModel.isDefined)
      s"${Messages("page.repayments.amount.label")} ${getAmountAsFormattedString(sharesRepaymentDetailsModel.amountSharesRepaymentModel.get.amount)}" else ""

    Array(repaymentType, repaymentDate, repaymentAmount)
  }
}
