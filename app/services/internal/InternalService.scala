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

package services.internal

import auth.authModels.UserIDs
import common.KeystoreKeys
import config.FrontendAuthConnector
import connectors.S4LConnector
import models.internal.CSApplicationModel
import models.submission.{SchemeTypesModel, SingleSchemeTypesModel}
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.frontend.auth.connectors.domain.Authority
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


object InternalService extends InternalService {
  override lazy val s4lConnector = S4LConnector
  override lazy val authConnector = FrontendAuthConnector
}

trait InternalService {

  val s4lConnector: S4LConnector
  val authConnector: AuthConnector

  def getCSApplicationModel(internalId: String)(implicit headerCarrier: HeaderCarrier): Future[CSApplicationModel] = {

    def getSchemeType(schemeTypesModel: SchemeTypesModel): String = SingleSchemeTypesModel.convertToSingleScheme(schemeTypesModel).schemeType

    for {
      isApplicationInProgress <- s4lConnector.fetchAndGetFormData[Boolean](internalId, KeystoreKeys.applicationInProgress)
      schemeTypesModel <- {
        if (isApplicationInProgress.getOrElse(false)) s4lConnector.fetchAndGetFormData[SchemeTypesModel](internalId, KeystoreKeys.selectedSchemes)
        else Future.successful(None)
      }
    }yield CSApplicationModel(isApplicationInProgress.getOrElse(false),
      if(schemeTypesModel.isDefined) Some(getSchemeType(schemeTypesModel.get)) else None)
  }

  def deleteCSApplication(internalId: String)(implicit headerCarrier: HeaderCarrier): Future[HttpResponse] = {
    s4lConnector.clearCache(internalId)
  }

}
