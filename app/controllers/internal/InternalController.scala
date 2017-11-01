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
import auth.FrontendAuthorised
import config.FrontendAuthConnector
import connectors.S4LConnector
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}
import services.internal.InternalService
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

object InternalController extends InternalController{
  override lazy val s4lConnector = S4LConnector
  override lazy val internalService = InternalService
  override lazy val authConnector = FrontendAuthConnector
}


trait InternalController extends FrontendController with FrontendAuthorised{

  val s4lConnector: S4LConnector
  val internalService: InternalService

  def getApplicationInProgress: Action[AnyContent] = FrontendAuthorised.async {
        implicit userIds => implicit request =>{
          internalService.getCSApplicationModel(userIds.internalId).map(csApplication => Ok(Json.toJson(csApplication)))
        }
      }

  def deleteCSApplication: Action[AnyContent] = FrontendAuthorised.async {
    implicit userIds => implicit request => {
      s4lConnector.clearCache(userIds.internalId).map{
        res => Status(res.status)
      }
    }
  }

}