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
import org.mockito.Matchers
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import org.mockito.Mockito._
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future
import models.investorDetails.{InvestorDetailsModel, NumberOfPreviouslyIssuedSharesModel}


class NumberOfPreviouslyIssuedSharesControllerSpec extends BaseSpec {

  lazy val controller = new NumberOfPreviouslyIssuedSharesController {
    override lazy val s4lConnector: S4LConnector = mockS4lConnector
    override lazy val enrolmentConnector: EnrolmentConnector = mockEnrolmentConnector
    override lazy val applicationConfig: AppConfig = MockConfig
    override lazy val authConnector: AuthConnector = MockAuthConnector
  }

  val backUrl = Some(controllers.seis.routes.PreviousShareHoldingDescriptionController.show(1, Some(1)).url)

  def setupMocks(investorDetailsModel: Option[Vector[InvestorDetailsModel]]): Unit = {
    mockEnrolledRequest(seisSchemeTypesModel)

    when(mockS4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](Matchers.eq(KeystoreKeys.investorDetails))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(investorDetailsModel))

    when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkNumberOfPreviouslyIssuedShares))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(backUrl))
  }

  "The Number of Previously Issued Shares controller" should {

    "use the correct auth connector" in {
      NumberOfPreviouslyIssuedSharesController.authConnector shouldBe FrontendAuthConnector
    }

    "use the correct keystore connector" in {
      NumberOfPreviouslyIssuedSharesController.s4lConnector shouldBe S4LConnector
    }

    "use the correct enrolment connector" in {
      NumberOfPreviouslyIssuedSharesController.enrolmentConnector shouldBe EnrolmentConnector
    }

    "return a 200 on a GET request" when {

      "no data is already stored" in {
        setupMocks(Some(listOfInvestorsCompleteOption1))
        showWithSessionAndAuth(controller.show(1, 1))(
          result => status(result) shouldBe 200
        )
      }

      "data is already stored" in {
        setupMocks(Some(listOfInvestorsCompleteOption1))
        showWithSessionAndAuth(controller.show(1, 1))(
          result => status(result) shouldBe 200
        )
      }
    }

    /* TODO Back logic with looping logic */
    "return a 303 on a successful POST request" in {
      setupMocks(Some(listOfInvestorsCompleteOption1))
      val form = Seq("numberOfPreviouslyIssuedShares" -> "20", "processingId" -> "1", "investorProcessingId" -> "1")
      submitWithSessionAndAuth(controller.submit(Some(""), Some("")), form: _*) (
        result => {
          status(result) shouldBe 303
          redirectLocation(result) shouldBe Some(controllers.seis.routes.NumberOfPreviouslyIssuedSharesController.show(1, 1).url)
        }
      )
    }
    "return a 400 on a form validation failure" in {
      setupMocks(Some(listOfInvestorsCompleteOption1))
      val form = Seq("numberOfSharesPurchased" -> "")
      submitWithSessionAndAuth(controller.submit(Some(""), Some("")), form: _*) (
        result => status(result) shouldBe 400
      )
    }
  }
}