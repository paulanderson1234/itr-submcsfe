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

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import auth.{MockAuthConnector, MockConfig}
import common.KeystoreKeys
import controllers.helpers.BaseSpec
import models.investorDetails._
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._

import scala.concurrent.Future

class AddAnotherShareholdingControllerSpec extends BaseSpec {

  implicit val system = ActorSystem()
  implicit val materializer: Materializer = ActorMaterializer()

  val testController = {
    new AddAnotherShareholdingController {
      override lazy val applicationConfig = MockConfig
      override lazy val authConnector = MockAuthConnector
      override lazy val s4lConnector = mockS4lConnector
      override lazy val enrolmentConnector = mockEnrolmentConnector
    }
  }

  def setupMocks(data: Option[Vector[InvestorDetailsModel]]): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](Matchers.eq(KeystoreKeys.investorDetails))(Matchers.any(),
      Matchers.any(), Matchers.any())).thenReturn(Future.successful(data))
  }

  "Calling AddAnotherShareholdingController.show" should {
    lazy val result = {
      mockEnrolledRequest(eisSchemeTypesModel)
      setupMocks(None)
      testController.show(1)(authorisedFakeRequest)
    }

    "return a status of 200" in {
      status(result) shouldBe 200
    }

    "load the Add Another Shareholder page" in {
      Jsoup.parse(bodyOf(result)).title() shouldBe Messages("page.investors.AddAnotherShareholding.title")
    }
  }

  "Calling AddAnotherShareholdingController.submit" when {

    "an invalid POST is made" should {
      lazy val result = {
        mockEnrolledRequest(eisSchemeTypesModel)
        setupMocks(None)
        testController.submit(1)(authorisedFakeRequestToPOST(("addAnotherShareholding", "")))
      }

      "return a status of 400" in {
        status(result) shouldBe 400
      }

      "reload the Add Another Shareholder page" in {
        Jsoup.parse(bodyOf(result)).title() shouldBe Messages("page.investors.AddAnotherShareholding.title")
      }
    }

    "a valid POST with a 'No' is made" when {

      "an investor exists with all shareholdings valid" should {
        lazy val result = {
          mockEnrolledRequest(eisSchemeTypesModel)
          setupMocks(Some(Vector(validModelWithPrevShareHoldings)))
          testController.submit(2)(authorisedFakeRequestToPOST(("addAnotherShareholding", "No")))
        }

        "return a status of 303" in {
          status(result) shouldBe 303
        }

        "redirect to the Review Investor Entry page" in {
          redirectLocation(result) shouldBe Some(controllers.eis.routes.ReviewInvestorDetailsController.show(2).url)
        }
      }

      "no investors exist" should {
        lazy val result = {
          mockEnrolledRequest(eisSchemeTypesModel)
          setupMocks(None)
          testController.submit(1)(authorisedFakeRequestToPOST(("addAnotherShareholding", "No")))
        }

        "return a status of 303" in {
          status(result) shouldBe 303
        }

        "redirect to the Add Investor or Nominee page" in {
          redirectLocation(result) shouldBe Some(controllers.eis.routes.AddInvestorOrNomineeController.show().url)
        }
      }

      "the id does not match an existing investor" should {
        lazy val result = {
          mockEnrolledRequest(eisSchemeTypesModel)
          setupMocks(Some(Vector(validModelWithPrevShareHoldings)))
          testController.submit(1)(authorisedFakeRequestToPOST(("addAnotherShareholding", "No")))
        }

        "return a status of 303" in {
          status(result) shouldBe 303
        }

        "redirect to the Add Investor or Nominee page" in {
          redirectLocation(result) shouldBe Some(controllers.eis.routes.AddInvestorOrNomineeController.show().url)
        }
      }
    }

    "a valid POST with a 'Yes' is made with an Id of 1" should {
      lazy val result = {
        mockEnrolledRequest(eisSchemeTypesModel)
        setupMocks(None)
        testController.submit(1)(authorisedFakeRequestToPOST(("addAnotherShareholding", "Yes")))
      }

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the Share Description page" in {
        redirectLocation(result) shouldBe Some(controllers.eis.routes.PreviousShareHoldingDescriptionController.show(1).url)
      }
    }

    "a valid POST with a 'Yes' is made with an Id of 2" should {
      lazy val result = {
        mockEnrolledRequest(eisSchemeTypesModel)
        setupMocks(None)
        testController.submit(2)(authorisedFakeRequestToPOST(("addAnotherShareholding", "Yes")))
      }

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect to the Share Description page" in {
        redirectLocation(result) shouldBe Some(controllers.eis.routes.PreviousShareHoldingDescriptionController.show(2).url)
      }
    }
  }
}
