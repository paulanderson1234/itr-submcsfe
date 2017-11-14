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
import common.KeystoreKeys
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.BaseSpec
import models.repayments.SharesRepaymentDetailsModel
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class DateSharesRepaidControllerSpec extends BaseSpec {

  lazy val validChangeBackLink = controllers.eis.routes.ReviewPreviousRepaymentsController.show().toString
  val processingId = 1
  object TestController extends DateSharesRepaidController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  def setupMocks(sharesRepaymentDetails: Option[Vector[SharesRepaymentDetailsModel]] = None, backUrl: Option[String]): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[Vector[SharesRepaymentDetailsModel]](Matchers.eq(KeystoreKeys.sharesRepaymentDetails))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(sharesRepaymentDetails)

    when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkSharesRepaymentDate))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(backUrl))


    when(mockS4lConnector.saveFormData(Matchers.eq(KeystoreKeys.dateSharesRepaid),
      Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
    .thenReturn(Future.successful(CacheMap("", Map())))
  }

  "DateSharesRepaidController" should {
    "use the correct storage connector" in {
      DateSharesRepaidController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      DateSharesRepaidController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      DateSharesRepaidController.enrolmentConnector shouldBe EnrolmentConnector
    }
    "use the correct application config" in {
      DateSharesRepaidController.applicationConfig shouldBe FrontendAppConfig
    }
  }

  "Sending a GET request to DateSharesRepaidController when authenticated and enrolled" should {
    "return a 200 when a saved model is fetched from storage" in {
     setupMocks(Some(validSharesRepaymentDetailsVector),
       Some(controllers.eis.routes.SharesRepaymentTypeController.show(processingId).toString))
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show(processingId))(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return an OK 200 when nothing is fetched from storage" in {
      setupMocks(Some(Vector.empty :+ sharesRepaymentDetailsMissingRepaymentDate),
        Some(controllers.eis.routes.SharesRepaymentTypeController.show(processingId).toString))
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show(processingId))(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a valid form submit to the DateSharesRepaidController when authenticated and enrolled" should {
    "redirect to the expected page" in {
      setupMocks(Some(validSharesRepaymentDetailsVector),
       Some(controllers.eis.routes.SharesRepaymentTypeController.show(processingId).toString))
      mockEnrolledRequest(eisSchemeTypesModel)

      val formInput = Seq(
        "dateSharesRepaidDay" -> s"$dateSharesRepaidDay",
        "dateSharesRepaidMonth" -> s"$dateSharesRepaidMonth",
        "dateSharesRepaidYear" -> s"$dateSharesRepaidYear")

      submitWithSessionAndAuth(TestController.submit,formInput:_*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.AmountSharesRepaymentController.show(processingId + 1).url)
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the DateSharesRepaidController when authenticated and enrolled" should {
    "return a bad request" in {
      setupMocks(None,
        Some(controllers.eis.routes.SharesRepaymentTypeController.show(processingId).toString))
      mockEnrolledRequest(eisSchemeTypesModel)

      val formInput = Seq(
        "dateSharesRepaidDay" -> "",
        "dateSharesRepaidMonth" -> "",
        "dateSharesRepaidYear" -> "")

      submitWithSessionAndAuth(TestController.submit,formInput:_*)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }

}

