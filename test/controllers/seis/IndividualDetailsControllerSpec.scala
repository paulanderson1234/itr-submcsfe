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
import controllers.helpers.{BaseSpec, FakeRequestHelper}
import models.investorDetails.InvestorDetailsModel
import models.{IndividualDetailsModel, NominalValueOfSharesModel}
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector

import scala.concurrent.Future

class IndividualDetailsControllerSpec extends BaseSpec with FakeRequestHelper{

    lazy val controller = new IndividualDetailsController{
    override lazy val s4lConnector: S4LConnector = mockS4lConnector
    override lazy val enrolmentConnector: EnrolmentConnector = mockEnrolmentConnector
    override lazy val applicationConfig: AppConfig = MockConfig
    override lazy val authConnector: AuthConnector = MockAuthConnector
  }

  def setupMocks(model: Option[IndividualDetailsModel], individualDetailsModels: Option[Vector[InvestorDetailsModel]]): Unit = {
    mockEnrolledRequest(seisSchemeTypesModel)

    when(mockS4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](Matchers.eq(KeystoreKeys.investorDetails))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(individualDetailsModels))
    when(mockS4lConnector.fetchAndGetFormData[IndividualDetailsModel](Matchers.eq(KeystoreKeys.individualDetails))(Matchers.any(),
      Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(model))

    when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(mock[CacheMap]))
  }

  "The IndividualDetails controller" should {

    "use the correct auth connector" in {
      IndividualDetailsController.authConnector shouldBe FrontendAuthConnector
    }

    "use the correct keystore connector" in {
      IndividualDetailsController.s4lConnector shouldBe S4LConnector
    }

    "use the correct enrolment connector" in {
      IndividualDetailsController.enrolmentConnector shouldBe EnrolmentConnector
    }

    "return a 200 on a GET request" when {

      "no data is already stored" in {
        setupMocks(None, Some(onlyInvestorOrNomineeVectorList))
        showWithSessionAndAuth(controller.show(1))(
          result => status(result) shouldBe 200
        )
      }

      "data is already stored" in {
        setupMocks(None, Some(onlyInvestorOrNomineeVectorList))
        showWithSessionAndAuth(controller.show(1))(
          result => status(result) shouldBe 200
        )
      }
    }

    "return a 303 on a successful POST request" in {
      setupMocks(None, Some(onlyInvestorOrNomineeVectorList))
      val formInput = Seq(
        "forename" -> "TEST",
        "surname" -> "TESTING",
        "addressline1" -> "Line 1",
        "addressline2" -> "Line 2",
        "addressline3" -> "Line 3",
        "addressline4" -> "line 4",
        "postcode" -> "AA1 1AA",
        "countryCode" -> "GB")

      submitWithSessionAndAuth(controller.submit, formInput: _*)(
        result => {
          status(result) shouldBe 303
          redirectLocation(result) shouldBe Some(controllers.seis.routes.IndividualDetailsController.show(1).url)
        }
      )
    }

    "return a 400 on a form validation failure" in {
      setupMocks(None, Some(onlyInvestorOrNomineeVectorList))
      val form = Seq(
        "forename" -> "",
        "surname" -> "",
        "addressline1" -> "Line 1",
        "addressline2" -> "Line 2",
        "addressline3" -> "Line 3",
        "addressline4" -> "line 4",
        "postcode" -> "",
        "countryCode" -> "GB")
      submitWithSessionAndAuth(controller.submit, form: _*) (
        result => status(result) shouldBe 400
      )
    }
  }
}
