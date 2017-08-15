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

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import auth.{MockAuthConnector, MockConfig}
import common.KeystoreKeys
import config.FrontendAuthConnector
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.BaseSpec
import models.AddInvestorOrNomineeModel
import models.investorDetails.InvestorDetailsModel
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._

import scala.concurrent.Future

class ReviewAllInvestorsControllerSpec extends BaseSpec {

  implicit val system = ActorSystem()
  implicit val materializer: Materializer = ActorMaterializer()

  def testController: ReviewAllInvestorsController = {
    mockEnrolledRequest(seisSchemeTypesModel)
    new ReviewAllInvestorsController {
      override lazy val applicationConfig = MockConfig
      override lazy val authConnector = MockAuthConnector
      override lazy val s4lConnector = mockS4lConnector
      override lazy val enrolmentConnector = mockEnrolmentConnector
    }
  }

  def setupMocks(investorDetails: Option[Vector[InvestorDetailsModel]]): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](Matchers.eq(KeystoreKeys.investorDetails))(Matchers.any(),
      Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(investorDetails))
  }

  "ReviewAllInvestorsController" should {
    "use the correct auth connector" in {
      ReviewAllInvestorsController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct keystore connector" in {
      ReviewAllInvestorsController.s4lConnector shouldBe S4LConnector
    }
    "use the correct enrolment connector" in {
      ReviewAllInvestorsController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }


  "When a GET request is made to ReviewAllInvestorsController show method" which {

    "does not find any investors" should {
      lazy val result = {
        setupMocks(None)
        testController.show()(authorisedFakeRequest)
      }

      "return a status of 303" in {
        status(result) shouldBe SEE_OTHER
      }

      "redirect the user to the add investor or nominee page" in {
        redirectLocation(result) shouldBe Some(controllers.seis.routes.AddInvestorOrNomineeController.show().url)
      }
    }


    "finds previous investor details" should {
      lazy val result = {
        setupMocks(Some(Vector(InvestorDetailsModel(Some(AddInvestorOrNomineeModel("Investor", Some(1))), processingId = Some(1)))))
        testController.show()(authorisedFakeRequest)
      }

      "return a status of 200" in {
        status(result) shouldBe OK
      }

      "load the Review Investor page" in {
        Jsoup.parse(bodyOf(result)).title() shouldBe Messages("page.investors.reviewAllInvestors.title")
      }
    }
  }

  "When a POST request is made to ReviewAllInvestorsController submit method" which {

    "does not find any investors" should {
      lazy val result = {
        setupMocks(None)
        testController.submit()(authorisedFakeRequest)
      }

      "return a status of 303" in {
        status(result) shouldBe SEE_OTHER
      }

      "redirect the user to the add investor or nominee page" in {
        redirectLocation(result) shouldBe Some(controllers.seis.routes.AddInvestorOrNomineeController.show().url)
      }
    }


    "finds previous investor details which are all Valid" should {
      lazy val result = {
        setupMocks(Some(Vector(validModelNoPrevShareHoldings)))
        testController.submit()(authorisedFakeRequest)
      }

      "return a status of 303" in {
        status(result) shouldBe SEE_OTHER
      }

      "Redirect to the AddAnotherInvestorOrNominee page" in {
        redirectLocation(result) shouldBe Some(controllers.seis.routes.AddAnotherInvestorController.show().url)
      }

    }

    "finds previous investor details which contains invalid investor details" should {
      lazy val result = {
        setupMocks(Some(Vector(InvestorDetailsModel(Some(AddInvestorOrNomineeModel("Investor", Some(1))), processingId = Some(1)))))
        testController.submit()(authorisedFakeRequest)
      }

      "return a status of 303" in {
        status(result) shouldBe SEE_OTHER
      }

      "Redirect to itself" in {
        redirectLocation(result) shouldBe Some(controllers.seis.routes.ReviewAllInvestorsController.show().url)
      }

    }
  }

  "When a GET request is made to ReviewAllInvestorsController change method" which {

    "does not find any investors" should {
      lazy val result = {
        setupMocks(None)
        testController.change(1)(authorisedFakeRequest)
      }

      "return a status of 303" in {
        status(result) shouldBe SEE_OTHER
      }

      "redirect the user to the add investor or nominee page" in {
        redirectLocation(result) shouldBe Some(controllers.seis.routes.AddInvestorOrNomineeController.show().url)
      }
    }


    "finds previous investor details which are all valid" should {
      lazy val result = {
        setupMocks(Some(Vector(validModelWithPrevShareHoldings)))
        testController.change(2)(authorisedFakeRequest)
      }

      "return a status of 303" in {
        status(result) shouldBe SEE_OTHER
      }

      "Redirect to the AddNomineeOrInvestor page" in {
        redirectLocation(result) shouldBe Some(controllers.seis.routes.ReviewInvestorDetailsController.show(2).url)
      }
    }

    "finds previous investor details, some of which are invalid" should {
      lazy val result = {
        setupMocks(Some(Vector(validModelWithPrevShareHoldings.copy(isExistingShareHolderModel = None))))
        testController.change(2)(authorisedFakeRequest)
      }

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "Redirect to the itself page" in {
        redirectLocation(result) shouldBe Some(controllers.seis.routes.ReviewInvestorDetailsController.show(2).url)
      }
    }
  }

  "When a GET request is made to ReviewAllInvestorsController add method" which {

    "does not find any investors" should {
      lazy val result = {
        setupMocks(None)
        testController.add()(authorisedFakeRequest)
      }

      "return a status of 303" in {
        status(result) shouldBe SEE_OTHER
      }

      "redirect the user to the add investor or nominee page" in {
        redirectLocation(result) shouldBe Some(controllers.seis.routes.AddInvestorOrNomineeController.show().url)
      }
    }

    "finds an empty list of investors" should {
      lazy val result = {
          setupMocks(Some(Vector()))
        testController.add()(authorisedFakeRequest)
      }

      "return a status of 303" in {
        status(result) shouldBe SEE_OTHER
      }

      "redirect the user to the add investor or nominee page" in {
        redirectLocation(result) shouldBe Some(controllers.seis.routes.AddInvestorOrNomineeController.show().url)
      }
    }


    "finds previous investor details which are all valid" should {
      lazy val result = {
        setupMocks(Some(Vector(validModelWithPrevShareHoldings)))
        testController.add()(authorisedFakeRequest)
      }

      "return a status of 303" in {
        status(result) shouldBe SEE_OTHER
      }

      "Redirect to the AddNomineeOrInvestor page" in {
        redirectLocation(result) shouldBe Some(controllers.seis.routes.AddInvestorOrNomineeController.show().url)
      }
    }

    "finds previous investor details, some of which are invalid" should {
      lazy val result = {
        setupMocks(Some(Vector(validModelWithPrevShareHoldings.copy(isExistingShareHolderModel = None))))
        testController.add()(authorisedFakeRequest)
      }

      "return a status of 303" in {
        status(result) shouldBe SEE_OTHER
      }

      "Redirect to the itself page" in {
        redirectLocation(result) shouldBe Some(controllers.seis.routes.ReviewAllInvestorsController.show().url)
      }
    }
  }

  "Making a GET request to the ReviewAllInvestorsController remove method" should {

    lazy val result = {
      testController.remove(1)(authorisedFakeRequest)
    }

    "return a status of 303" in {
      status(result) shouldBe SEE_OTHER
    }

    "redirect to the remove investor page" in {
        redirectLocation(result) shouldBe Some(controllers.seis.routes.DeleteInvestorController.show(1).url)
      }

  }
}
