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
import config.{AppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.BaseSpec
import models.CompanyOrIndividualModel
import models.investorDetails._
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future

class HowMuchSpentOnSharesControllerSpec extends BaseSpec  {

  lazy val controller = new HowMuchSpentOnSharesController {
    override lazy val s4lConnector: S4LConnector = mockS4lConnector
    override lazy val enrolmentConnector: EnrolmentConnector = mockEnrolmentConnector
    override lazy val applicationConfig: AppConfig = MockConfig
    override lazy val authConnector: AuthConnector = MockAuthConnector
  }

  def setupMocks(howMuchSpentOnSharesModel: Option[HowMuchSpentOnSharesModel], companyOrIndividualModel: Option[CompanyOrIndividualModel]): Unit = {
    mockEnrolledRequest(seisSchemeTypesModel)

    when(mockS4lConnector.fetchAndGetFormData[HowMuchSpentOnSharesModel](Matchers.eq(KeystoreKeys.howMuchSpentOnShares))(Matchers.any(),
      Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(howMuchSpentOnSharesModel))

    when(mockS4lConnector.fetchAndGetFormData[CompanyOrIndividualModel](Matchers.eq(KeystoreKeys.companyOrIndividual))(Matchers.any(),
      Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(companyOrIndividualModel))

    when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(mock[CacheMap]))
  }

  "The HowMuchSpentOnShares controller" should {

    "use the correct auth connector" in {
      HowMuchSpentOnSharesController.authConnector shouldBe FrontendAuthConnector
    }

    "use the correct keystore connector" in {
      HowMuchSpentOnSharesController.s4lConnector shouldBe S4LConnector
    }

    "use the correct enrolment connector" in {
      HowMuchSpentOnSharesController.enrolmentConnector shouldBe EnrolmentConnector
    }

    "return a 200 on a GET request" when {

      "no data is already stored" in {
        setupMocks(None, Some(companyOrIndividualModel))
        mockEnrolledRequest(seisSchemeTypesModel)
        showWithSessionAndAuth(controller.show)(
          result => status(result) shouldBe 200
        )
      }

      "data is already stored" in {
        setupMocks(Some(howMuchSpentOnSharesModel), Some(companyOrIndividualModel))
        mockEnrolledRequest(seisSchemeTypesModel)
        showWithSessionAndAuth(controller.show)(
          result => status(result) shouldBe 200
        )
      }
    }

    "return a 303 on a successful POST request" in {
      setupMocks(None, Some(companyOrIndividualModel))
      mockEnrolledRequest(seisSchemeTypesModel)
      val form = Seq("howMuchSpentOnShares" -> "1000")
      submitWithSessionAndAuth(controller.submit, form: _*) (
        result => {
          status(result) shouldBe 303
          redirectLocation(result) shouldBe Some(controllers.seis.routes.HowMuchSpentOnSharesController.show().url)
        }
      )
    }

    "return a 400 on a form validation failure" in {
      setupMocks(None, Some(companyOrIndividualModel))
      mockEnrolledRequest(seisSchemeTypesModel)
      val form = Seq("howMuchSpentOnShares" -> "")
      submitWithSessionAndAuth(controller.submit, form: _*) (
        result => status(result) shouldBe 400
      )
    }
  }
}
