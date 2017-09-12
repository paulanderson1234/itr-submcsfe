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
import common.{Constants, KeystoreKeys}
import config.FrontendAuthConnector
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.BaseSpec
import models.{HadOtherInvestmentsModel, HadPreviousRFIModel, PreviousSchemeModel}
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._

import scala.concurrent.Future

class HadOtherInvestmentsControllerSpec extends BaseSpec {

  object TestController extends HadOtherInvestmentsController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  "HadOtherInvestmentsController" should {
    "use the correct keystore connector" in {
      HadOtherInvestmentsController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      HadOtherInvestmentsController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      HadOtherInvestmentsController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }
  
  def showMocks(hadOtherInvestmentsModel: Option[HadOtherInvestmentsModel] = None, hadPreviousRFIBackLink: Option[String] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[HadOtherInvestmentsModel](Matchers.eq(KeystoreKeys.hadOtherInvestments))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(hadOtherInvestmentsModel))
    when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkHadRFI))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(hadPreviousRFIBackLink))
  }

  def submitMocks(previousSchemes: Option[Vector[PreviousSchemeModel]] = None, hadPreviousRFIModel: Option[HadPreviousRFIModel]): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(previousSchemes))
    when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(hadPreviousRFIModel))
  }

  "Sending a GET request to HadOtherInvestmentsController when authenticated and enrolled for EIS" should {
    "return an OK when something is fetched from storage" in {
      showMocks(Some(hadOtherInvestmentsModelYes), Some(routes.FullTimeEmployeeCountController.show().url))
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show())(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty form and return an OK when nothing is fetched using storage for EIS" in {
      showMocks(None, Some(routes.FullTimeEmployeeCountController.show().url))
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show())(
        result => status(result) shouldBe OK
      )
    }


    "REDIRECT to the correct page in the flow when no backLink is present"  in {
      showMocks(Some(hadOtherInvestmentsModelYes), None)
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.HadPreviousRFIController.show().url)
        }
      )
    }
  }

  "Sending a valid 'Yes' form submit to the HadOtherInvestmentsController when authenticated and enrolled for EIS" should {
    "redirect to the PreviousScheme page when there are no previous schemes" in {
      submitMocks(previousSchemes =  None, hadPreviousRFIModel = Some(hadPreviousRFIModelYes))
      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = "hadOtherInvestments" -> Constants.StandardRadioButtonYesValue
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.PreviousSchemeController.show().url)
        }
      )
    }
  }

  "Sending a valid 'Yes' form submit to the HadOtherInvestmentsController when authenticated and enrolled for EIS" should {
    "redirect to the ReviewPreviousSchemes page when previous schemes exist" in {
      submitMocks(previousSchemes = Some(previousSchemesFull), hadPreviousRFIModel = Some(hadPreviousRFIModelYes))
      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = "hadOtherInvestments" -> Constants.StandardRadioButtonYesValue
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.ReviewPreviousSchemesController.show().url)
        }
      )
    }
  }

  "Sending a valid 'No' form submit to the HadOtherInvestmentsController with 'No' to the previous RFI when authenticated and enrolled for EIS" should {
    "redirect to the ShareDescription page" in {
      submitMocks(previousSchemes = None, hadPreviousRFIModel = Some(hadPreviousRFIModelNo))
      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = "hadOtherInvestments" -> Constants.StandardRadioButtonNoValue
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.ShareDescriptionController.show().url)
        }
      )
    }
  }

  "Sending a valid 'No' form submit to the HadOtherInvestmentsController with 'YES' to the previous RFI when authenticated and enrolled for EIS" should {
    "redirect to the PreviousScheme page when there are no previous schemes" in {
      submitMocks(previousSchemes = None, hadPreviousRFIModel = Some(hadPreviousRFIModelYes))
      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = "hadOtherInvestments" -> Constants.StandardRadioButtonNoValue
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.PreviousSchemeController.show().url)
        }
      )
    }
  }

  "Sending a valid 'No' form submit to the HadOtherInvestmentsController with 'YES' to the previous RFI when authenticated and enrolled for EIS" should {
    "redirect to the ReviewPreviousScheme page when previous schemes exist" in {
      submitMocks(previousSchemes = Some(previousSchemesFull), hadPreviousRFIModel = Some(hadPreviousRFIModelYes))
      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = "hadOtherInvestments" -> Constants.StandardRadioButtonNoValue
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.ReviewPreviousSchemesController.show().url)
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the HadOtherInvestmentsController when authenticated and enrolled for EIS" should {
    "reload the page with a BAD_REQUEST when a backlink is found" in {
      when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkHadRFI))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Some(controllers.eis.routes.HadPreviousRFIController.show().url)))

      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = "hadOtherInvestments" -> ""
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the HadOtherInvestmentsController when authenticated and enrolled for EIS" should {
    "redirect when a backlink is not found" in {
      when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkHadRFI))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(None))

      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = "hadOtherInvestments" -> ""
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.HadPreviousRFIController.show().url)
        }
      )
    }
  }
}
