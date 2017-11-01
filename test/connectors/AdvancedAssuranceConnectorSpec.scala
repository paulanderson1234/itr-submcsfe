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

/**
  * Copyright 2016 HM Revenue & Customs
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIED OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

package connectors

import auth.{MockConfig, TAVCUser, ggUser}
import controllers.helpers.{BaseSpec, FakeRequestHelper}
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.http.logging.SessionId
import config.WSHttp
import fixtures.SubmissionFixture
import org.mockito.Matchers
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneAppPerSuite
import play.api.test.Helpers.OK
import uk.gov.hmrc.play.http.ws.WSHttp
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class AdvancedAssuranceConnectorSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with OneAppPerSuite with SubmissionFixture {

  object TestAdvancedAssuranceConnector extends AdvancedAssuranceConnector with FakeRequestHelper{
    override val serviceUrl: String = MockConfig.internalAASubmissionUrl
    override val http = mock[WSHttp]
  }

  implicit val headerCarrier: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("1013")))
  implicit val user: TAVCUser = TAVCUser(ggUser.allowedAuthContext, internalId = "Int-312e5e92-762e-423b-ac3d-8686af27fdb5")

  "AdvancedAssuranceConnector" should {
    "use correct http client" in {
      AdvancedAssuranceConnector.http shouldBe WSHttp
    }
  }

  "Calling getAdvancedAssuranceApplication" when {
    "expecting a successful response" should {
      lazy val result = TestAdvancedAssuranceConnector.getAdvancedAssuranceApplication()
      "return a valid boolean response" in {
        when(TestAdvancedAssuranceConnector.http.GET[Boolean](
          Matchers.eq(s"${TestAdvancedAssuranceConnector.serviceUrl}/internal/aa-application-in-progress"))
          (Matchers.any(), Matchers.any())).thenReturn(Future.successful(false))
        await(result) match {
          case response => response shouldBe false
          case _ => fail("No response was received, when one was expected")
        }
      }
    }
  }
}
