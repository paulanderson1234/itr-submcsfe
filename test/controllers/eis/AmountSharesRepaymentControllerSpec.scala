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

///*
// * Copyright 2017 HM Revenue & Customs
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package controllers.eis
//
//import auth.{MockAuthConnector, MockConfig}
//import common.KeystoreKeys
//import config.{FrontendAppConfig, FrontendAuthConnector}
//import connectors.{EnrolmentConnector, S4LConnector}
//import controllers.helpers.BaseSpec
//import models._
//import models.repayments.AmountSharesRepaymentModel
//import org.mockito.Matchers
//import org.mockito.Mockito._
//import play.api.test.Helpers._
//import uk.gov.hmrc.http.cache.client.CacheMap
//
//import scala.concurrent.Future
//
//class AmountSharesRepaymentControllerSpec extends BaseSpec {
//
//  object TestController extends AmountSharesRepaymentController {
//    override lazy val applicationConfig = MockConfig
//    override lazy val authConnector = MockAuthConnector
//    override lazy val s4lConnector = mockS4lConnector
//    override lazy val enrolmentConnector = mockEnrolmentConnector
//  }
//
//  def setupMocks(amountSharesRepaymentModel: Option[AmountSharesRepaymentModel] = None): Unit = {
//    when(mockS4lConnector.fetchAndGetFormData[AmountSharesRepaymentModel](Matchers.eq(KeystoreKeys.amountSharesRepayment))
//      (Matchers.any(), Matchers.any(), Matchers.any()))
//        .thenReturn(Future.successful(amountSharesRepaymentModel))
//
//    when(mockS4lConnector.saveFormData(Matchers.eq(KeystoreKeys.amountSharesRepayment),
//      Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
//    .thenReturn(Future.successful(CacheMap("", Map())))
//  }
//
//  "AmountSharesRepaymentController" should {
//    "use the correct storage connector" in {
//      AmountSharesRepaymentController.s4lConnector shouldBe S4LConnector
//    }
//    "use the correct auth connector" in {
//      AmountSharesRepaymentController.authConnector shouldBe FrontendAuthConnector
//    }
//    "use the correct enrolment connector" in {
//      AmountSharesRepaymentController.enrolmentConnector shouldBe EnrolmentConnector
//    }
//    "use the correct application config" in {
//      AmountSharesRepaymentController.applicationConfig shouldBe FrontendAppConfig
//    }
//  }
//
//  "Sending a GET request to AmountSharesRepaymentController when authenticated and enrolled" should {
//    "return a 200 when a saved model is fetched from storage" in {
//     setupMocks(Some(amountSharesRepaymentModel))
//      mockEnrolledRequest(eisSchemeTypesModel)
//      showWithSessionAndAuth(TestController.show)(
//        result => status(result) shouldBe OK
//      )
//    }
//
//    "provide an empty model and return an OK 200 when nothing is fetched from storage" in {
//      setupMocks(None)
//      mockEnrolledRequest(eisSchemeTypesModel)
//      showWithSessionAndAuth(TestController.show())(
//        result => status(result) shouldBe OK
//      )
//    }
//  }
//
//  "Sending a valid form submit to the AmountSharesRepaymentController" should {
//    "redirect to the expected page" in {
//      setupMocks(None)
//      mockEnrolledRequest(eisSchemeTypesModel)
//      submitWithSessionAndAuth(TestController.submit,
//        "amount" -> "1")(
//        result => {
//          status(result) shouldBe SEE_OTHER
//          //TODO: navigate to correct page when available
//          redirectLocation(result) shouldBe Some(routes.AmountSharesRepaymentController.show().url)
//        }
//      )
//    }
//  }
//
//   "Sending an invalid form submission with validation errors to the AmountSharesRepaymentController when authenticated and enrolled" should {
//    "return a bad request status" in {
//      mockEnrolledRequest(eisSchemeTypesModel)
//      submitWithSessionAndAuth(TestController.submit,
//        "amount" -> "")(
//        result => {
//          status(result) shouldBe BAD_REQUEST
//        }
//      )
//    }
//  }
//
//}
//
