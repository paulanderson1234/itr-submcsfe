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

package services

import auth.TAVCUser
import connectors.SubmissionConnector
import models.submission.SubmissionDetailsModel
import play.api.Logger
import play.api.libs.json.{JsError, JsSuccess}
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait SubmissionService {

  val submissionConnector: SubmissionConnector

  def getEtmpReturnsSummary(tavcRef: String)(implicit hc: HeaderCarrier, user: TAVCUser): Future[Option[SubmissionDetailsModel]] = {
    submissionConnector.getReturnsSummary(tavcRef) map {
      submissionDetails =>
        submissionDetails.json.validate[SubmissionDetailsModel] match {
          case data: JsSuccess[SubmissionDetailsModel] =>
            Some(data.value)
          case e: JsError =>
            Logger.warn(s"[SubmissionService][getEtmpReturnsSummary] - Failed to parse JSON response. Errors=${e.errors}")
            None
        }
    }
  }

  def validateFullTimeEmployeeCount(schemeType: String, employeeCount: BigDecimal)(implicit hc: HeaderCarrier, user: TAVCUser): Future[Boolean] ={
    submissionConnector.validateFullTimeEmployeeCount(schemeType, employeeCount) map {
      employeeCountValidation =>
        employeeCountValidation.json.validate[Boolean] match {
          case data: JsSuccess[Boolean] => data.value
          case e: JsError =>
            Logger.warn(s"[SubmissionService][validateFullTimeEmployeeCount] - Failed to parse JSON response. Errors=${e.errors}")
            false
        }
    }
  }
}

object SubmissionService extends SubmissionService {
  val submissionConnector = SubmissionConnector
}
