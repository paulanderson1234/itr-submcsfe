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

package services

import auth.MockAuthConnector
import common.{Constants, KeystoreKeys}
import config.FrontendAuthConnector
import connectors.S4LConnector
import models.internal.CSApplicationModel
import models.submission.SchemeTypesModel
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneServerPerSuite
import services.internal.InternalService
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier

class InternalServiceSpec extends UnitSpec with MockitoSugar with OneServerPerSuite{

  val seisSchemeTypesModel = Some(SchemeTypesModel(seis = true))
  val eisSchemeTypesModel = Some(SchemeTypesModel(eis = true))

  implicit val hc = HeaderCarrier()
  val internalId = "Int-312e5e92-762e-423b-ac3d-8686af27fdb5"



  object TestInternalService extends InternalService{
    override val s4lConnector: S4LConnector = mock[S4LConnector]
    override val authConnector: AuthConnector = MockAuthConnector
  }

  "InternalService" should {
    "use the correct s4lConnector" in {
      InternalService.s4lConnector shouldBe S4LConnector
    }

    "use the correct auth connector" in {
      InternalService.authConnector shouldBe FrontendAuthConnector
    }
  }

  def setupMocks(isApplicationInProgress: Option[Boolean], schemeTypesModel: Option[SchemeTypesModel]): Unit = {
    when(TestInternalService.s4lConnector.fetchAndGetFormData[Boolean](Matchers.any(), Matchers.eq(KeystoreKeys.applicationInProgress))
      (Matchers.any(), Matchers.any())).thenReturn(isApplicationInProgress)
    when(TestInternalService.s4lConnector.fetchAndGetFormData[SchemeTypesModel](Matchers.any(), Matchers.eq(KeystoreKeys.selectedSchemes))
      (Matchers.any(), Matchers.any())).thenReturn(schemeTypesModel)
  }

  "Calling getCSApplicationModel" should {

    "return a valid model when an EIS application is in progress" in {
      setupMocks(Some(true), eisSchemeTypesModel)
      lazy val result = TestInternalService.getCSApplicationModel(internalId)
      await(result) shouldBe CSApplicationModel(true, Some(Constants.schemeTypeEis))
    }
    "return a valid model when an SEIS application is in progress" in {
      setupMocks(Some(true), seisSchemeTypesModel)
      lazy val result = TestInternalService.getCSApplicationModel(internalId)
      await(result) shouldBe CSApplicationModel(true, Some(Constants.schemeTypeSeis))
    }

    "return a valid model when there is no application in progress (Flag set to false in storage)" in {
      setupMocks(Some(false), None)
      lazy val result = TestInternalService.getCSApplicationModel(internalId)
      await(result) shouldBe CSApplicationModel(false, None)
    }

    "return a valid model when there is no application in progress (Nothing found in storage)" in {
      setupMocks(None, None)
      lazy val result = TestInternalService.getCSApplicationModel(internalId)
      await(result) shouldBe CSApplicationModel(false, None)
    }
  }
}
