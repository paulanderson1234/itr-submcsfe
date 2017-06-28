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
import common.{Constants, KeystoreKeys}
import config.{AppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector, SubmissionConnector}
import controllers.helpers.BaseSpec
import models.ResearchStartDateModel
import org.mockito.Matchers
import org.mockito.Mockito._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import play.api.test.Helpers._

import scala.concurrent.Future

class ResearchStartDateControllerSpec extends BaseSpec {

  def setupController(researchStartDateModel: Option[ResearchStartDateModel]): ResearchStartDateController = {

    when(mockS4lConnector.fetchAndGetFormData[ResearchStartDateModel](Matchers.eq(KeystoreKeys.researchStartDate))(Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(researchStartDateModel))

    when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(mock[CacheMap]))

    mockEnrolledRequest(seisSchemeTypesModel)

    new ResearchStartDateController {
      override lazy val submissionConnector: SubmissionConnector = mockSubmissionConnector
      override lazy val enrolmentConnector: EnrolmentConnector = mockEnrolmentConnector
      override lazy val applicationConfig: AppConfig = MockConfig
      override lazy val s4lConnector: S4LConnector = mockS4lConnector
      override lazy val authConnector: AuthConnector = MockAuthConnector
    }
  }

  "ResearchStartDateController" should {

    "use the correct auth connector" in {
      ResearchStartDateController.authConnector shouldBe FrontendAuthConnector
    }

    "use the correct keystore connector" in {
      ResearchStartDateController.s4lConnector shouldBe S4LConnector
    }

    "use the correct enrolment connector" in {
      ResearchStartDateController.enrolmentConnector shouldBe EnrolmentConnector
    }

    "use the correct submission Connector" in {
      ResearchStartDateController.submissionConnector shouldBe SubmissionConnector
    }

    "return a valid OK response on a GET with no stored data" in {
      showWithSessionAndAuth(setupController(None).show) { result =>
        status(result) shouldBe OK
      }
    }

    "return a valid OK response on a GET with stored data" in {
      val model = Some(ResearchStartDateModel(hasStartedResearch = false, None, None, None))
      showWithSessionAndAuth(setupController(model).show) { result =>
        status(result) shouldBe OK
      }
    }

    "return a valid BAD_REQUEST response on a POST with an invalid form submission" in {
      val form = Seq(
        "hasStartedResearch" -> Constants.StandardRadioButtonYesValue,
        "researchStartDay" -> "23",
        "researchStartMonth" -> "11",
        "researchStartYear" -> "")
      submitWithSessionAndAuth(setupController(None).submit, form: _*) { result =>
        status(result) shouldBe BAD_REQUEST
      }
    }

    "return a valid SEE_OTHER response on a POST with a valid form submission" in {
      val form = Seq(
        "hasStartedResearch" -> Constants.StandardRadioButtonNoValue,
        "researchStartDay" -> "",
        "researchStartMonth" -> "",
        "researchStartYear" -> "")
      submitWithSessionAndAuth(setupController(None).submit, form: _*) { result =>
        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.ResearchStartDateController.show().url)
      }
    }

  }
}
