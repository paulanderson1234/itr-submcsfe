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
//import common.{Constants, KeystoreKeys}
//import config.{FrontendAppConfig, FrontendAuthConnector}
//import connectors.{EnrolmentConnector, S4LConnector}
//import controllers.helpers.BaseSpec
//import models.repayments.SharesRepaymentTypeModel
//import org.mockito.Matchers
//import org.mockito.Mockito._
//import play.api.test.Helpers._
//
//import scala.concurrent.Future
//
//class SharesRepaymentTypeControllerSpec extends BaseSpec {
//
//  object TestController extends SharesRepaymentTypeController {
//    override lazy val applicationConfig = MockConfig
//    override lazy val authConnector = MockAuthConnector
//    override lazy val s4lConnector = mockS4lConnector
//    override lazy val enrolmentConnector = mockEnrolmentConnector
//  }
//
//  "SharesRepaymentTypeController" should {
//    "use the correct storage connector" in {
//      SharesRepaymentTypeController.s4lConnector shouldBe S4LConnector
//    }
//    "use the correct auth connector" in {
//      SharesRepaymentTypeController.authConnector shouldBe FrontendAuthConnector
//    }
//    "use the correct config" in {
//      SharesRepaymentTypeController.applicationConfig shouldBe FrontendAppConfig
//    }
//    "use the correct enrolment connector" in {
//      SharesRepaymentTypeController.enrolmentConnector shouldBe EnrolmentConnector
//    }
//  }
//
//  def setupMocks(sharesRepaymentTypeModel: Option[SharesRepaymentTypeModel] = None): Unit = {
//    when(mockS4lConnector.fetchAndGetFormData[SharesRepaymentTypeModel](Matchers.eq(KeystoreKeys.sharesRepaymentType))
//      (Matchers.any(), Matchers.any(), Matchers.any()))
//        .thenReturn(Future.successful(sharesRepaymentTypeModel))
//
//  }
//
//  "Sending a GET request to SharesRepaymentTypeController when authenticated and enrolled" should {
//    "return a 200 when something is fetched from storage" in {
//      setupMocks(Some(repaymentTypeShares))
//      mockEnrolledRequest(eisSchemeTypesModel)
//      showWithSessionAndAuth(TestController.show())(
//        result => status(result) shouldBe OK
//      )
//    }
//
//    "provide an empty model and return a 200 when nothing is fetched using storage" in {
//      setupMocks(None)
//      mockEnrolledRequest(eisSchemeTypesModel)
//      showWithSessionAndAuth(TestController.show())(
//        result => status(result) shouldBe OK
//      )
//    }
//  }
//
//  "Sending a validshares form submission to the SharesRepaymentTypeController when authenticated and enrolled" should {
//    "redirect to the expected page" in {
//      val formInput = "sharesRepaymentType" -> Constants.repaymentTypeShares
//      setupMocks()
//      mockEnrolledRequest(eisSchemeTypesModel)
//      submitWithSessionAndAuth(TestController.submit,formInput)(
//        result => {
//          status(result) shouldBe SEE_OTHER
//          redirectLocation(result) shouldBe Some(controllers.eis.routes.DateSharesRepaidController.show().url)
//        }
//      )
//    }
//  }
//
//  "Sending a valid debentures form submission to the SharesRepaymentTypeController when authenticated and enrolled" should {
//    "redirect to the expected page" in {
//      val formInput = "sharesRepaymentType" -> Constants.repaymentTypeDebentures
//      setupMocks()
//      mockEnrolledRequest(eisSchemeTypesModel)
//      submitWithSessionAndAuth(TestController.submit,formInput)(
//        result => {
//          status(result) shouldBe SEE_OTHER
//          redirectLocation(result) shouldBe Some(controllers.eis.routes.DateSharesRepaidController.show().url)
//        }
//      )
//    }
//  }
//
//  "Sending an invalid form submission with validation errors to the SharesRepaymentTypeController when authenticated and enrolled" should {
//    "respond with a bad request" in {
//      mockEnrolledRequest(eisSchemeTypesModel)
//      val formInput = "sharesRepaymentType" -> ""
//      submitWithSessionAndAuth(TestController.submit,formInput)(
//        result => {
//          status(result) shouldBe BAD_REQUEST
//        }
//      )
//    }
//  }
//
//}
