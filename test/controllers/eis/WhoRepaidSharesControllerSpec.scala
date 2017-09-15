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

import auth.{MockAuthConnector, MockConfig}
import common.KeystoreKeys
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.BaseSpec
import models._
import models.repayments.WhoRepaidSharesModel
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class WhoRepaidSharesControllerSpec extends BaseSpec {

  val whoRepaidSharesModel = WhoRepaidSharesModel("bill", "smith")

  object TestController extends WhoRepaidSharesController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  def setupMocks(whoRepaidSharesModel: Option[WhoRepaidSharesModel] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[WhoRepaidSharesModel](Matchers.eq(KeystoreKeys.whoRepaidShares))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(whoRepaidSharesModel))

    when(mockS4lConnector.saveFormData(Matchers.eq(KeystoreKeys.whoRepaidShares),
      Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(CacheMap("", Map())))
  }

  "WhoRepaidSharesController" should {
    "use the correct storage connector" in {
      WhoRepaidSharesController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      WhoRepaidSharesController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      WhoRepaidSharesController.enrolmentConnector shouldBe EnrolmentConnector
    }
    "use the correct application config" in {
      WhoRepaidSharesController.applicationConfig shouldBe FrontendAppConfig
    }
  }

  "Sending a GET request to WhoRepaidSharesController when authenticated and enrolled" should {
    "return a 200 when a saved model is fetched from storage" in {
      setupMocks(Some(whoRepaidSharesModel))
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return an OK 200 when nothing is fetched from storage" in {
      setupMocks(None)
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show())(
        result => status(result) shouldBe OK
      )
    }
  }

  "Submitting a valid request to when authenticated and enrolled" should {
    "redirect to the correct page" in {

      val formInput = Seq(
        "forename" -> "Bill",
        "surname" -> "Smith")
      setupMocks(None)
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit(), formInput: _*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe
            Some(controllers.eis.routes.SharesRepaymentTypeController.show().url)
        }
      )
    }
  }

  "Submitting an invalid request when authenticated and enrolled" should {
    "repsond with a bad request with form errors" in {

      val formInput = Seq(
        "forename" -> "",
        "surname" -> "")

      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit(), formInput: _*)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }

  }
}
