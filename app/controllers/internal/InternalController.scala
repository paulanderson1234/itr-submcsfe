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

package controllers.internal
import auth.{AuthorisedAndEnrolledForTAVC, Flow}
import common.KeystoreKeys
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import models.internal.CSApplicationModel
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Results}
import services.InternalService
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.Future

object InternalController extends InternalController{
  override lazy val s4lConnector = S4LConnector
  override lazy val internalService = InternalService
}


trait InternalController extends FrontendController{

  val s4lConnector: S4LConnector
  val internalService: InternalService

  def getApplicationInProgress(internalId: String): Action[AnyContent] = Action.async {
    implicit request =>{
      internalService.getCSApplicationModel(internalId).map(csApplication => Ok(Json.toJson(csApplication)))
    }
  }

  def deleteCSApplication(internalId: String): Action[AnyContent] = Action.async {
    implicit request =>{
      s4lConnector.clearCache(internalId).map{
        response => Status(response.status)
      }
    }
  }

}