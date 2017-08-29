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

package models

import connectors.SubmissionConnector
import models.seis._
import org.mockito.Matchers
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class SEISAnswersModelSpec extends UnitSpec with MockitoSugar {

  val mockSubmissionConnector = mock[SubmissionConnector]
  implicit val hc = mock[HeaderCarrier]

  def setupMockModel(validCompany: Boolean = true,
                     validSchemes: Boolean = true,
                     validShares: Boolean = true,
                     validInvestors: Boolean = true): SEISAnswersModel = {

    val mockCompany = mock[CompanyDetailsAnswersModel]
    val mockSchemes = mock[PreviousSchemesAnswersModel]
    val mockShares = mock[ShareDetailsAnswersModel]
    val mockInvestors = mock[InvestorDetailsAnswersModel]

    when(mockCompany.validate(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(validCompany))
    when(mockSchemes.validate)
      .thenReturn(validSchemes)
    when(mockShares.validate(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(validShares))
    when(mockInvestors.validate)
      .thenReturn(validInvestors)

    SEISAnswersModel(mockCompany, mockSchemes, mockShares, mockInvestors, mock[ContactDetailsAnswersModel], mock[SupportingDocumentsUploadModel])
  }

  "Calling .validate on SEISAnswersModel" should {

    "return a false" when {

      "the company details validation returns a false" in {
        await(setupMockModel(validCompany = false).validate(mockSubmissionConnector)) shouldBe false
      }

      "the previous schemes validation returns a false" in {
        await(setupMockModel(validSchemes = false).validate(mockSubmissionConnector)) shouldBe false
      }

      "the share details validation returns a false" in {
        await(setupMockModel(validShares = false).validate(mockSubmissionConnector)) shouldBe false
      }

      "the investor details validation returns a false" in {
        await(setupMockModel(validInvestors = false).validate(mockSubmissionConnector)) shouldBe false
      }
    }

    "return a true when all validation returns a true" in {
      await(setupMockModel().validate(mockSubmissionConnector)) shouldBe true
    }
  }
}
