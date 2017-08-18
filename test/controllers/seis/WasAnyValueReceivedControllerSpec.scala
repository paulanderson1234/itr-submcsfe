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

package controllers.seis

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import auth.{MockAuthConnector, MockConfig}
import common.KeystoreKeys
import controllers.helpers.{BaseSpec, FakeRequestHelper}
import models.WasAnyValueReceivedModel
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class WasAnyValueReceivedControllerSpec extends BaseSpec with FakeRequestHelper {

  implicit val system = ActorSystem()
  implicit val materializer: Materializer = ActorMaterializer()

  private def setupController(previousData: Option[WasAnyValueReceivedModel] = None) = {
    mockEnrolledRequest(seisSchemeTypesModel)

    when(mockS4lConnector.fetchAndGetFormData[WasAnyValueReceivedModel](Matchers.eq(KeystoreKeys.wasAnyValueReceived))(Matchers.any(),
      Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(previousData))

    when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(mock[CacheMap]))

    new WasAnyValueReceivedController {
      override lazy val applicationConfig = MockConfig
      override lazy val authConnector = MockAuthConnector
      override lazy val enrolmentConnector = mockEnrolmentConnector
      override lazy val s4lConnector = mockS4lConnector
    }
  }

  "The WasAnyValueReceivedController" when {

    "the show method is called" which {

      "has no previous data" should {
        lazy val result = setupController().show(authorisedFakeRequest)

        "return a status of 200" in {
          status(result) shouldBe 200
        }

        "load the Was Any Value Received page" in {
          Jsoup.parse(bodyOf(result)).title() shouldBe Messages("page.investors.wasAnyValueReceived.title")
        }
      }

      "has previously saved data" should {
        lazy val result = setupController(Some(WasAnyValueReceivedModel("Yes", Some("text")))).show(authorisedFakeRequest)

        "return a status of 200" in {
          status(result) shouldBe 200
        }

        "load the Was Any Value Received page" in {
          Jsoup.parse(bodyOf(result)).title() shouldBe Messages("page.investors.wasAnyValueReceived.title")
        }
      }
    }

    "the submit method is called" which {

      "has an invalid form" should {
        lazy val result = setupController().submit(authorisedFakeRequestToPOST(("wasAnyValueReceived", ""), ("aboutValueReceived", "")))

        "return a status of 400" in {
          status(result) shouldBe 400
        }

        "reload the Was Any Value Received page" in {
          Jsoup.parse(bodyOf(result)).title() shouldBe Messages("page.investors.wasAnyValueReceived.title")
        }
      }

      "has a valid form" should {
        lazy val result = setupController().submit(authorisedFakeRequestToPOST(("wasAnyValueReceived", "No"), ("aboutValueReceived", "")))

        "return a status of 303" in {
          status(result) shouldBe 303
        }

        "redirect to the share/loan capital pages" in {
          redirectLocation(result) shouldBe Some(controllers.seis.routes.WasAnyValueReceivedController.show().url)
        }
      }
    }
  }
}
