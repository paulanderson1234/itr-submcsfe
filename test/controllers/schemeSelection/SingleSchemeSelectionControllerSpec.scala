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

package controllers.schemeSelection

import auth.{MockAuthConnector, MockConfig}
import common.{Constants, KeystoreKeys}
import config.FrontendAuthConnector
import connectors.{AdvancedAssuranceConnector, EnrolmentConnector, S4LConnector}
import controllers.helpers.BaseSpec
import models.submission.SchemeTypesModel
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class SingleSchemeSelectionControllerSpec extends BaseSpec {

  object TestController extends SingleSchemeSelectionController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
    override lazy val advancedAssuranceConnector = mockAdvancedAssuranceConnector
  }

  val cacheMapSchemeTypesEis: CacheMap = CacheMap("", Map("" -> Json.toJson(SchemeTypesModel(eis = true))))
  val cacheMapSchemeTypesSeis: CacheMap = CacheMap("", Map("" -> Json.toJson(SchemeTypesModel(seis = true))))
  val cacheMapSchemeTypesVct: CacheMap = CacheMap("", Map("" -> Json.toJson(SchemeTypesModel(vct = true))))

  "SingleSchemeSelectionController" should {
    "use the correct keystore connector" in {
      SingleSchemeSelectionController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      SingleSchemeSelectionController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      SingleSchemeSelectionController.enrolmentConnector shouldBe EnrolmentConnector
    }
    "use the correct advanced assurance connector" in {
      SingleSchemeSelectionController.advancedAssuranceConnector shouldBe AdvancedAssuranceConnector
    }
  }

  "Sending a GET request to SingleSchemeSelectionController when authenticated and enrolled" should {
    "return a 200 when something is fetched from keystore and no AA application is in progress" in {
      mockEnrolledRequest(None)
      when(TestController.advancedAssuranceConnector.getAdvancedAssuranceApplication()
      (Matchers.any(), Matchers.any())).thenReturn(Future.successful(false))
      when(mockS4lConnector.fetchAndGetFormData[SchemeTypesModel](Matchers.eq(KeystoreKeys.selectedSchemes))
        (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show())(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore and no AA application is in progress" in {
      mockEnrolledRequest(None)
      when(TestController.advancedAssuranceConnector.getAdvancedAssuranceApplication()
      (Matchers.any(), Matchers.any())).thenReturn(Future.successful(false))
      when(mockS4lConnector.fetchAndGetFormData[SchemeTypesModel](Matchers.eq(KeystoreKeys.selectedSchemes))
        (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(None)
      showWithSessionAndAuth(TestController.show())(
        result => status(result) shouldBe OK
      )
    }

    /*Todo needs to redirect to AA hub*/
    "redirect to the application hub when there is an AA application in progress" in {
      mockEnrolledRequest(None)
      when(TestController.advancedAssuranceConnector.getAdvancedAssuranceApplication()
      (Matchers.any(), Matchers.any())).thenReturn(Future.successful(true))
      showWithSessionAndAuth(TestController.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.routes.ApplicationHubController.show().url)
        }
      )
    }
  }

  "Sending a valid 'EIS' form submit to the SingleSchemeSelectionController when authenticated and enrolled" should {
    "redirect to review schemes page when no AA application is in progress" in {
      mockEnrolledRequest(None)
      when(TestController.advancedAssuranceConnector.getAdvancedAssuranceApplication()
      (Matchers.any(), Matchers.any())).thenReturn(Future.successful(false))
      when(mockS4lConnector.fetchAndGetFormData[SchemeTypesModel](Matchers.eq(KeystoreKeys.selectedSchemes))
        (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(eisSchemeTypesModel)
      when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(cacheMapSchemeTypesEis)
      val formInput = "singleSchemeSelection" -> Constants.schemeTypeEis
      submitWithSessionAndAuth(TestController.submit(),formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.eis.routes.InitialDeclarationController.show().url)
        }
      )
    }
  }

  "Sending a valid 'SEIS' form submit to the SingleSchemeSelectionController when authenticated and enrolled" should {
    "redirect to review schemes page when no AA application is in progress" in {
      mockEnrolledRequest(None)
      when(TestController.advancedAssuranceConnector.getAdvancedAssuranceApplication()
      (Matchers.any(), Matchers.any())).thenReturn(Future.successful(false))
      when(mockS4lConnector.fetchAndGetFormData[SchemeTypesModel](Matchers.eq(KeystoreKeys.selectedSchemes))
        (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(seisSchemeTypesModel)
      when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(cacheMapSchemeTypesSeis)
      val formInput = "singleSchemeSelection" -> Constants.schemeTypeSeis
      submitWithSessionAndAuth(TestController.submit(),formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.seis.routes.InitialDeclarationController.show().url)
        }
      )
    }
  }

  "Sending an invalid scheme type form submit to the SingleSchemeSelectionController when authenticated and enrolled" should {
    "respond with a bad request when no AA application is in progress" in {
      mockEnrolledRequest(None)
      when(TestController.advancedAssuranceConnector.getAdvancedAssuranceApplication()
      (Matchers.any(), Matchers.any())).thenReturn(Future.successful(false))
      when(mockS4lConnector.fetchAndGetFormData[SchemeTypesModel](Matchers.eq(KeystoreKeys.selectedSchemes))
        (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(vctSchemeTypesModel)
      when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(cacheMapSchemeTypesVct)
      val formInput = "singleSchemeSelection" -> Constants.schemeTypeVct
      submitWithSessionAndAuth(TestController.submit(),formInput)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the SingleSchemeSelectionController when authenticated and enrolled" should {
    "redirect to itself when no AA application is in progress" in {
      mockEnrolledRequest(None)
      when(TestController.advancedAssuranceConnector.getAdvancedAssuranceApplication()
      (Matchers.any(), Matchers.any())).thenReturn(Future.successful(false))
      val formInput = "singleSchemeSelection" -> ""
      submitWithSessionAndAuth(TestController.submit(),formInput)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }

  "Posting to the SingleSchemeSelectionController when authenticated and enrolled" should {
    "redirect to the application hub when an AA application is in progress" in {
      mockEnrolledRequest(None)
      when(TestController.advancedAssuranceConnector.getAdvancedAssuranceApplication()
      (Matchers.any(), Matchers.any())).thenReturn(Future.successful(true))
      submitWithSessionAndAuth(TestController.submit())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.routes.ApplicationHubController.show().url)
        }
      )
    }
  }
}
