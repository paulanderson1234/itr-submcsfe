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
import models.AddInvestorOrNomineeModel
import models.investorDetails.InvestorDetailsModel
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._

import scala.concurrent.Future

class ReviewInvestorDetailsControllerSpec extends BaseSpec {

  implicit val system = ActorSystem()
  implicit val materializer: Materializer = ActorMaterializer()

  def testController: ReviewInvestorDetailsController = {
    mockEnrolledRequest(eisSchemeTypesModel)
    new ReviewInvestorDetailsController {
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

  "When a GET request is made to ReviewInvestorsDetailsController" which {

    "does not find any investors" should {
      lazy val result = {
        setupMocks(None)
        testController.show(1)(authorisedFakeRequest)
      }

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect the user to the add investor or nominee page" in {
        redirectLocation(result) shouldBe Some(controllers.eis.routes.AddInvestorOrNomineeController.show().url)
      }
    }

    "does not match any existing investors" should {
      lazy val result = {
        setupMocks(Some(Vector(InvestorDetailsModel(processingId = Some(2)))))
        testController.show(1)(authorisedFakeRequest)
      }

      "return a status of 303" in {
        status(result) shouldBe 303
      }

      "redirect the user to the review investors page" in {
        redirectLocation(result) shouldBe Some(controllers.eis.routes.AddInvestorOrNomineeController.show().url)
      }
    }

    "does have an id matching an investor" should {
      lazy val result = {
        setupMocks(Some(Vector(InvestorDetailsModel(Some(AddInvestorOrNomineeModel("Investor", Some(1))), processingId = Some(1)))))
        testController.show(1)(authorisedFakeRequest)
      }

      "return a status of 200" in {
        status(result)
      }

      "load the Review Investor page" in {
        Jsoup.parse(bodyOf(result)).title() shouldBe Messages("page.investors.reviewInvestorDetails.title", "investor")
      }
    }
  }
}
