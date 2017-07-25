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
import config.FrontendAuthConnector
import connectors.EnrolmentConnector
import controllers.helpers.BaseSpec
import models.PreviousSchemeModel
import org.mockito.Matchers
import org.mockito.Mockito.when
import play.api.test.Helpers._

import scala.concurrent.Future

class InvalidPreviousSchemeControllerSpec extends BaseSpec {

  val schemeId = 1
  object TestController extends InvalidPreviousSchemeController{
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
    override lazy val s4lConnector = mockS4lConnector
  }

  def setupMocks(previousSchemeVectorList: Option[Vector[PreviousSchemeModel]] = None, previousScheme: Option[PreviousSchemeModel] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(previousSchemeVectorList))
  }

  "InvalidPreviousSchemeController" should {
    "use the correct auth connector" in {
      InvalidPreviousSchemeController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      InvalidPreviousSchemeController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  "Sending a GET request to InvalidPreviousSchemeController when authenticated and enrolled" should {
    "return a 200" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show(schemeId))(
        result => status(result) shouldBe OK
      )
    }
  }

  "Continue to the ReviewPreviousSchemeController when authenticated and enrolled" should {
    "redirect Review previous screen page " in {
      setupMocks(Some(previousSchemeVectorList), Some(previousSchemeModel1))
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief-cs/seis/review-previous-schemes")
        }
      )
    }
  }
}
