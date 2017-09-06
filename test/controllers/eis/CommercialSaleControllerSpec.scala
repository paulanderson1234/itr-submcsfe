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
import models.{CommercialSaleModel, DateOfIncorporationModel, KiProcessingModel}
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._

import scala.concurrent.Future

class CommercialSaleControllerSpec extends BaseSpec {

  val testBackUrl = "/test/testing"

  object TestController extends CommercialSaleController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  "CommercialSaleController" should {
    "use the correct auth connector" in {
      CommercialSaleController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct keystore connector" in {
      CommercialSaleController.s4lConnector shouldBe S4LConnector
    }
    "use the correct enrolment connector" in {
      CommercialSaleController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  def setupShowMocks(commercialSaleModel: Option[CommercialSaleModel] = None, backUrl: Option[String] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(commercialSaleModel))
    when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkCommercialSale))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(backUrl))
  }

  def setupSubmitMocks(backUrl: Option[String] = None): Unit = {

    when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkCommercialSale))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(backUrl))
  }

  "Sending a GET request to CommercialSaleController when authenticated and enrolled" should {
    "return a 200 when something is fetched from keystore" in {
      setupShowMocks(Some(commercialSaleModelYes), Some(testBackUrl))
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore" in {
      setupShowMocks(None, Some(testBackUrl))
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a GET request to CommercialSaleController when authenticated and enrolled" should {
    "redirects to the expected beginning of the flow if no backlink is found in storage" in {
      setupShowMocks(Some(commercialSaleModelYes), None)
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.QualifyBusinessActivityController.show().url)
        }
      )
    }
  }


  "Sending an invalid form submission to the CommercialSaleController when authenticated and enrolled" should {
    "respond with a bad requests if the back link is found in storage" in {
      // submit with no data
      val formInput = Seq("hasCommercialSale" -> Constants.StandardRadioButtonYesValue,
        "commercialSaleDay" -> "",
        "commercialSaleMonth" -> "",
        "commercialSaleYear" -> "")
      setupSubmitMocks(Some(testBackUrl))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit, formInput: _*)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }

  "Sending an invalid form submission to the CommercialSaleController when authenticated and enrolled" should {
    "respond with a bad request even if no back link is found in storage as it is checked on show instead" in {
      // submit with no data
      val formInput = Seq("hasCommercialSale" -> Constants.StandardRadioButtonYesValue,
        "commercialSaleDay" -> "",
        "commercialSaleMonth" -> "",
        "commercialSaleYear" -> "")
      setupSubmitMocks(None)
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit, formInput: _*)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }

  "Sending a valid Yes form submission to the CommercialSaleController when authenticated and enrolled" should {
    "redirect to the date of share issue page" in {
      val formInput = Seq(
        "hasCommercialSale" -> Constants.StandardRadioButtonYesValue,
        "commercialSaleDay" -> "23",
        "commercialSaleMonth" -> "11",
        "commercialSaleYear" -> "1993")
      setupSubmitMocks(Some(testBackUrl))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit, formInput: _*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.ShareIssueDateController.show().url)
        }
      )
    }
  }

  "Sending a valid No form submission to the CommercialSaleController when authenticated and enrolled" should {
    "redirect to the date of share issue page" in {
      val formInput = Seq(
        "hasCommercialSale" -> Constants.StandardRadioButtonNoValue,
        "commercialSaleDay" -> "",
        "commercialSaleMonth" -> "",
        "commercialSaleYear" -> "")
      setupSubmitMocks(Some(testBackUrl))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit, formInput: _*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.ShareIssueDateController.show().url)
        }
      )
    }
  }

}
