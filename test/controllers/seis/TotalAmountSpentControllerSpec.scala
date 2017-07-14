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

import auth.{MockAuthConnector, MockConfig}
import common.KeystoreKeys
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.BaseSpec
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class TotalAmountSpentControllerSpec extends BaseSpec {

  lazy val controller = new TotalAmountSpentController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  val validTotalAmountSpent = 12345
  val validTotalAmountSpentModel = TotalAmountSpentModel(12345)
  val invalidTotalAmountSpent = BigDecimal(9999999999999.1)


  def setupMocks(model: Option[TotalAmountSpentModel]): Unit = {
    mockEnrolledRequest(seisSchemeTypesModel)

    when(mockS4lConnector.fetchAndGetFormData[TotalAmountSpentModel](Matchers.eq(KeystoreKeys.totalAmountSpent))(Matchers.any(),
      Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(model))

    when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(mock[CacheMap]))
  }

  "TotalAmountSpentController" should {
    "use the correct keystore connector" in {
      TotalAmountSpentController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      TotalAmountSpentController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      TotalAmountSpentController.enrolmentConnector shouldBe EnrolmentConnector
    }

    "use the correct application config" in {
      TotalAmountSpentController.applicationConfig shouldBe FrontendAppConfig
    }
  }

  "return a 200 on a GET request" when {

    "no data is already stored" in {
      setupMocks(None)
      showWithSessionAndAuth(controller.show)(
        result => status(result) shouldBe 200
      )
    }

    "data is already stored" in {
      setupMocks(Some(validTotalAmountSpentModel))
      showWithSessionAndAuth(controller.show)(
        result => status(result) shouldBe 200
      )
    }
  }

  "return a 303 on a successful POST request" in {
    setupMocks(None)
    val form = Seq("totalAmountSpent" -> s"$validTotalAmountSpent")
    submitWithSessionAndAuth(controller.submit, form: _*) (
      result => {
        status(result) shouldBe 303
        redirectLocation(result) shouldBe Some(controllers.seis.routes.AddInvestorOrNomineeController.show().url)
      }
    )
  }

  "return a 400 on a form validation failure" in {
    setupMocks(None)
    val form = Seq("totalAmountSpent" -> "")
    submitWithSessionAndAuth(controller.submit, form: _*) (
      result => status(result) shouldBe 400
    )
  }

}

