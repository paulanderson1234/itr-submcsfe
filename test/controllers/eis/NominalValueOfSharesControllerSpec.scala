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
import config.{AppConfig, FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.BaseSpec
import models.NominalValueOfSharesModel
import org.mockito.Matchers
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import org.mockito.Mockito._
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class NominalValueOfSharesControllerSpec extends BaseSpec {

  lazy val controller = new NominalValueOfSharesController {
    override lazy val s4lConnector: S4LConnector = mockS4lConnector
    override lazy val enrolmentConnector: EnrolmentConnector = mockEnrolmentConnector
    override lazy val applicationConfig: AppConfig = MockConfig
    override lazy val authConnector: AuthConnector = MockAuthConnector
  }

  def setupMocks(model: Option[NominalValueOfSharesModel]): Unit = {
    mockEnrolledRequest(eisSchemeTypesModel)

    when(mockS4lConnector.fetchAndGetFormData[NominalValueOfSharesModel](Matchers.eq(KeystoreKeys.nominalValueOfShares))(Matchers.any(),
      Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(model))

    when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(mock[CacheMap]))
  }

  "The NominalValueOfShares controller" should {

    "use the correct auth connector" in {
      NominalValueOfSharesController.authConnector shouldBe FrontendAuthConnector
    }

    "use the correct keystore connector" in {
      NominalValueOfSharesController.s4lConnector shouldBe S4LConnector
    }

    "use the correct config" in {
      FullTimeEmployeeCountController.applicationConfig shouldBe FrontendAppConfig
    }

    "use the correct enrolment connector" in {
      NominalValueOfSharesController.enrolmentConnector shouldBe EnrolmentConnector
    }

    "return a 301 on a GET request" when {

      "no data is already stored" in {
        setupMocks(None)
        showWithSessionAndAuth(controller.show)(
          result => {
            status(result) shouldBe 303
            redirectLocation(result) shouldBe Some(controllers.eis.routes.TotalAmountRaisedController.show().url)
          }
        )
      }

      "data is already stored" in {
        setupMocks(Some(NominalValueOfSharesModel(20.0)))
        showWithSessionAndAuth(controller.show)(
          result => {
            status(result) shouldBe 303
            redirectLocation(result) shouldBe Some(controllers.eis.routes.TotalAmountRaisedController.show().url)
          }
        )
      }
    }

//    "return a 200 on a GET request" when {
//
//      "no data is already stored" in {
//        setupMocks(None)
//        showWithSessionAndAuth(controller.show)(
//          result => status(result) shouldBe 200
//        )
//      }
//
//      "data is already stored" in {
//        setupMocks(Some(NominalValueOfSharesModel(20.0)))
//        showWithSessionAndAuth(controller.show)(
//          result => status(result) shouldBe 200
//        )
//      }
//    }

    "return a 303 on a successful POST request" in {
      setupMocks(None)
      val form = Seq("nominalValueOfShares" -> "1000")
      submitWithSessionAndAuth(controller.submit, form: _*) (
        result => {
          status(result) shouldBe 303
          redirectLocation(result) shouldBe Some(controllers.eis.routes.TotalAmountRaisedController.show().url)
        }
      )
    }

    "return a 400 on a form validation failure" in {
      setupMocks(None)
      val form = Seq("nominalValueOfShares" -> "")
      submitWithSessionAndAuth(controller.submit, form: _*) (
        result => status(result) shouldBe 400
      )
    }
  }
}
