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

package controllers.eis

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import auth.MockAuthConnector
import config.FrontendAuthConnector
import connectors.S4LConnector
import controllers.helpers.BaseSpec
import controllers.internal.InternalController
import models.internal.CSApplicationModel
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.libs.json.Json
import play.api.test.Helpers._
import services.internal.InternalService
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.http.HttpResponse

import scala.concurrent.Future

class InternalControllerSpec extends BaseSpec {

  val testCSApplicationModel = CSApplicationModel(false, None)
  val expectedJsonBody = """{"inProgress":false}"""


  object TestController extends InternalController {
    override lazy val internalService = mockInternalService
    override lazy val s4lConnector = mockS4lConnector

    override val authConnector: AuthConnector = MockAuthConnector
  }

  "InternalController" should {

    "use the correct save4later connector" in {
      InternalController.s4lConnector shouldBe S4LConnector
    }

    "use the correct internal service" in {
      InternalController.internalService shouldBe InternalService
    }

    "use the correct auth connector" in {
      InternalController.authConnector shouldBe FrontendAuthConnector
    }
  }

  "Sending a GET request to InternalController when authenticated and enrolled" should {
    "return a Ok with the expected json body" in {

      implicit val system = ActorSystem()
      implicit val materializer: Materializer = ActorMaterializer()

      mockEnrolledRequest()
      when(TestController.internalService.getCSApplicationModel(Matchers.any())(Matchers.any()))
        .thenReturn(Future.successful(testCSApplicationModel))

      lazy val result = TestController.getApplicationInProgress.apply(authorisedFakeFrontendRequest)
      status(result) shouldBe OK
      await(jsonBodyOf(result)) shouldBe Json.parse(expectedJsonBody)
    }
  }

  "Sending a POST request to InternalController when authenticated and enrolled" should {
    "return the correct HttpResponse" in {
      mockEnrolledRequest()
      when(TestController.s4lConnector.clearCache(Matchers.any())(Matchers.any())).thenReturn(HttpResponse(OK))
      lazy val result = TestController.deleteCSApplication.apply(authorisedFakeFrontendRequest)
      status(await(result)) shouldBe OK
    }
  }
}
