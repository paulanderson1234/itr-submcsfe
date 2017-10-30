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

package connectors

import auth.TAVCUser
import config.{FrontendAppConfig, WSHttp}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http._

import scala.concurrent.Future

object AdvancedAssuranceConnector extends AdvancedAssuranceConnector with ServicesConfig {
  val serviceUrl = FrontendAppConfig.internalAASubmissionUrl
  val http = WSHttp
}

trait AdvancedAssuranceConnector {

  val serviceUrl: String
  val http: HttpGet

  def getAdvancedAssuranceApplication()(implicit hc: HeaderCarrier, user: TAVCUser): Future[Boolean] = {
    val headerCarrier = hc.copy(extraHeaders = hc.extraHeaders ++ Seq("CSRF-Token" -> "nocheck"))
    http.GET[Boolean](s"$serviceUrl/internal/${user.internalId}/aa-application-in-progress")(implicitly[HttpReads[Boolean]], headerCarrier)
  }
}
