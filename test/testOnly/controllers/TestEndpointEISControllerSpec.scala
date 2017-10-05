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

package testOnly.controllers

import auth.{MockAuthConnector, TAVCUser}
import auth._
import common.{Constants, KeystoreKeys}
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.BaseSpec
import forms.NatureOfBusinessForm
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import testOnly.controllers.eis.TestEndpointEISController
import testOnly.models.TestPreviousSchemesModel

import scala.concurrent.Future

class TestEndpointEISControllerSpec extends BaseSpec {

  object TestController extends TestEndpointEISController {
    override lazy val applicationConfig = FrontendAppConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  implicit val user = TAVCUser(ggUser.allowedAuthContext,internalId)

  def setupFillFormMocks(natureOfBusinessModel: Option[NatureOfBusinessModel]): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[NatureOfBusinessModel](Matchers.eq(KeystoreKeys.natureOfBusiness))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(natureOfBusinessModel))
  }

  def setupFillPreviousSchemesFormMocks(previousSchemes: Option[Vector[PreviousSchemeModel]]): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(previousSchemes))
  }

  "TestEndpointEISController" should {
    "Use the correct s4l connector" in {
      TestEndpointEISController.s4lConnector shouldBe S4LConnector
    }
    "Use the correct auth connector" in {
      TestEndpointEISController.authConnector shouldBe FrontendAuthConnector
    }
    "Use the correct enrolment connector" in {
      TestEndpointEISController.enrolmentConnector shouldBe EnrolmentConnector
    }
    "Use the correct app config" in {
      TestEndpointEISController.applicationConfig shouldBe FrontendAppConfig
    }
  }

  "TestEndpointEISController.showPageOne" when {

    "Called as an authorised and enrolled user" should {

      "Return OK" in {
        mockEnrolledRequest()
        when(mockS4lConnector.fetchAndGetFormData[String](Matchers.any())
          (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
        showWithSessionAndAuth(TestController.showPageOne())(
          result => status(result) shouldBe OK
        )
      }

    }

  }

  "TestEndpointEISController.submitPageOne" when {

    "Called as an authorised and enrolled user" should {

      "Return OK" in {
        mockEnrolledRequest()
        submitWithSessionAndAuth(TestController.submitPageOne())(
          result => status(result) shouldBe OK
        )
      }

    }

  }

  "TestEndpointEISController.showPageTwo" when {

    "Called as an authorised and enrolled user" should {

      "Return OK" in {
        mockEnrolledRequest()
        when(mockS4lConnector.fetchAndGetFormData[String](Matchers.any())
          (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
        showWithSessionAndAuth(TestController.showPageTwo(None))(
          result => status(result) shouldBe OK
        )
      }

    }

  }

  "TestEndpointEISController.submitPageTwo" when {

    "Called as an authorised and enrolled user" should {

      "Return OK" in {
        mockEnrolledRequest()
        submitWithSessionAndAuth(TestController.submitPageTwo())(
          result => status(result) shouldBe OK
        )
      }

    }

  }


  "TestEndpointEISController.showPageThree" when {

    "Called as an authorised and enrolled user" should {

      "Return OK" in {
        mockEnrolledRequest()
        when(mockS4lConnector.fetchAndGetFormData[String](Matchers.any())
          (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
        showWithSessionAndAuth(TestController.showPageThree())(
          result => status(result) shouldBe OK
        )
      }

    }

  }

  "TestEndpointEISController.submitPageThree" when {

    "Called as an authorised and enrolled user" should {

      "Return OK" in {
        mockEnrolledRequest()
        submitWithSessionAndAuth(TestController.submitPageThree())(
          result => status(result) shouldBe OK
        )
      }

    }

  }

  "TestEndpointEISController.fillForm" when {

    "s4lConnector returns data" should {

      "Return a form filled with data from s4lConnector" in {
        setupFillFormMocks(Some(natureOfBusinessModel))
        val result = TestController.fillForm[NatureOfBusinessModel](KeystoreKeys.natureOfBusiness, NatureOfBusinessForm.natureOfBusinessForm)
        await(result).get shouldBe natureOfBusinessModel
      }

    }

    "s4lConnector returns nothing" should {

      "Return an empty form" in {
        setupFillFormMocks(None)
        val result = TestController.fillForm[NatureOfBusinessModel](KeystoreKeys.natureOfBusiness, NatureOfBusinessForm.natureOfBusinessForm)
        await(result).value shouldBe None
      }

    }

  }

  "TestEndpointEISController.fillPreviousSchemesForm" when {

    "s4lConnector returns data" should {

      "Return a form filled with data from s4lConnector" in {
        setupFillPreviousSchemesFormMocks(Some(previousSchemeVectorList))
        val result = TestController.fillPreviousSchemesForm
        await(result).get shouldBe TestPreviousSchemesModel(Some(previousSchemeVectorList))
      }

    }

    "s4lConnector returns nothing" should {

      "Return an empty form" in {
        setupFillPreviousSchemesFormMocks(None)
        val result = TestController.fillPreviousSchemesForm
        await(result).get shouldBe TestPreviousSchemesModel(None)
      }

    }

  }

  "TestEndpointEISController.bindForm" when {

    "Sent a valid form" should {

      "Return the valid form" in {
        val result = TestController.bindForm[NatureOfBusinessModel](KeystoreKeys.natureOfBusiness,
          NatureOfBusinessForm.natureOfBusinessForm)(fakeRequest.withFormUrlEncodedBody("natureofbusiness" -> "test"),user,NatureOfBusinessModel.format)
        await(result).get shouldBe NatureOfBusinessModel("test")
      }

    }

    "Sent an invalid form" should {

      "Return the invalid form with errors" in {

        val result = TestController.bindForm[NatureOfBusinessModel](KeystoreKeys.natureOfBusiness,
          NatureOfBusinessForm.natureOfBusinessForm)(fakeRequest,user,NatureOfBusinessModel.format)
        await(result).hasErrors shouldBe true
      }

    }

  }


  "TestEndpointEISController.bindKIFormOne" when {

    "Sent a valid form with Yes" should {

      "Return the valid form" in {
        val result = TestController.bindKIFormOne()(fakeRequest.withFormUrlEncodedBody("isCompanyKnowledgeIntensive" ->
          Constants.StandardRadioButtonYesValue), user)
        await(result).get shouldBe IsCompanyKnowledgeIntensiveModel(Constants.StandardRadioButtonYesValue)
      }

    }

    "Sent a valid form with No" should {

      "Return the valid form" in {
        val result = TestController.bindKIFormOne()(fakeRequest.withFormUrlEncodedBody("isCompanyKnowledgeIntensive" ->
          Constants.StandardRadioButtonNoValue), user)
        await(result).get shouldBe IsCompanyKnowledgeIntensiveModel(Constants.StandardRadioButtonNoValue)
      }

    }

    "Sent an invalid form" should {

      "Return the invalid form with errors" in {

        val result = TestController.bindKIFormOne()(fakeRequest, user)
        await(result).hasErrors shouldBe true
      }

    }
  }



  "TestEndpointEISController.bindKIFormTwo" when {

    "Sent a valid form with Yes" should {

      "Return the valid form" in {
        val result = TestController.bindKIFormTwo()(fakeRequest.withFormUrlEncodedBody("isKnowledgeIntensive" -> Constants.StandardRadioButtonYesValue),user)
        await(result).get shouldBe IsKnowledgeIntensiveModel(Constants.StandardRadioButtonYesValue)
      }

    }

    "Sent a valid form with No" should {

      "Return the valid form" in {
        val result = TestController.bindKIFormTwo()(fakeRequest.withFormUrlEncodedBody("isKnowledgeIntensive" -> Constants.StandardRadioButtonNoValue),user)
        await(result).get shouldBe IsKnowledgeIntensiveModel(Constants.StandardRadioButtonNoValue)
      }

    }

    "Sent an invalid form" should {

      "Return the invalid form with errors" in {

        val result = TestController.bindKIFormTwo()(fakeRequest,user)
        await(result).hasErrors shouldBe true
      }

    }

  }

  "TestEndpointEISController.bindPreviousSchemesForm" when {

    "Sent a valid form with a previous scheme" should {

      "Return the valid form" in {
        setupFillPreviousSchemesFormMocks(None)
        val result = TestController.bindPreviousSchemesForm()(fakeRequest.withFormUrlEncodedBody(
          "testPreviousSchemes[0].schemeTypeDesc" -> Constants.schemeTypeEis,
          "testPreviousSchemes[0].previousSchemeInvestmentAmount" -> "3",
          "testPreviousSchemes[0].previousSchemeInvestmentSpent" -> "",
          "testPreviousSchemes[0].previousSchemeOtherSchemeName" -> "",
          "testPreviousSchemes[0].previousSchemeInvestmentDay" -> "4",
          "testPreviousSchemes[0].previousSchemeInvestmentMonth" -> "5",
          "testPreviousSchemes[0].previousSchemeInvestmentYear" -> "2008",
          "testPreviousSchemes[0].previousSchemeProcessingId" -> "1"
        ), user)
        result.get shouldBe TestPreviousSchemesModel(Some(Seq(PreviousSchemeModel(Constants.schemeTypeEis,
          3, None, None, Some(4), Some(5), Some(2008), Some(1)))))
      }

    }

    "Sent an empty form" should {

      "Return the empty form" in {
        val result = TestController.bindPreviousSchemesForm()(fakeRequest, user)
        result.get shouldBe TestPreviousSchemesModel(None)
      }

    }

    "Sent an invalid form" should {

      "Return the invalid form with errors" in {
        setupFillPreviousSchemesFormMocks(None)
        val result = TestController.bindPreviousSchemesForm()(fakeRequest.withFormUrlEncodedBody(
          "testPreviousSchemes[0].schemeTypeDesc" -> Constants.schemeTypeEis
        ), user)
        result.hasErrors shouldBe true
      }

    }

  }

}
