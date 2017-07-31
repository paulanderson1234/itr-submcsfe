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

import auth.{MockAuthConnector, MockConfig}
import common.{Constants, KeystoreKeys}
import config.{AppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.BaseSpec
import models.{CompanyOrIndividualModel, ShareIssueDateModel}
import models.investorDetails.InvestorDetailsModel
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import utils.DateFormatter

import scala.concurrent.Future


class IsExistingShareHolderControllerSpec extends BaseSpec with DateFormatter{

  lazy val controller = new IsExistingShareHolderController {
    override lazy val s4lConnector: S4LConnector = mockS4lConnector
    override lazy val enrolmentConnector: EnrolmentConnector = mockEnrolmentConnector
    override lazy val applicationConfig: AppConfig = MockConfig
    override lazy val authConnector: AuthConnector = MockAuthConnector
  }

  val backUrl = Some(controllers.seis.routes.HowMuchSpentOnSharesController.show(1).url)
  val obviouslyInvalidId = 9999

  val listOfInvestorsIncompleteIsExistingShareModelMissing =  Vector(validModelWithPrevShareHoldings.copy(isExistingShareHolderModel = None))
  val listOfInvestorsIncompleteCompanyOrIndividualMissing =  Vector(validModelWithPrevShareHoldings.copy(companyOrIndividualModel = None,
    isExistingShareHolderModel = None))

  def setupMocks(investorDetailsModel: Option[Vector[InvestorDetailsModel]],
                 backURL : Option[String]): Unit = {
    mockEnrolledRequest(seisSchemeTypesModel)

    when(mockS4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](Matchers.eq(KeystoreKeys.investorDetails))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(investorDetailsModel))

    when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkIsExistingShareHolder))
      (Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(backURL))
  }

  "TheIs Existing Share Holder controller" should {

    "use the correct auth connector" in {
      IsExistingShareHolderController.authConnector shouldBe FrontendAuthConnector
    }

    "use the correct keystore connector" in {
      IsExistingShareHolderController.s4lConnector shouldBe S4LConnector
    }

    "use the correct enrolment connector" in {
      IsExistingShareHolderController.enrolmentConnector shouldBe EnrolmentConnector
    }

    "Sending a GET request to IsExistingShareHolder Controller when authenticated and enrolled" should {

      "'REDIRECT' to AddInvestorOrNominee page" when {
        "there is no 'back link' present" in {
          mockEnrolledRequest(seisSchemeTypesModel)
          setupMocks(None, None)
          showWithSessionAndAuth(controller.show(1))(
            result => {
              status(result) shouldBe SEE_OTHER
              redirectLocation(result) shouldBe Some(routes.AddInvestorOrNomineeController.show(None).url)
            }
          )
        }
      }

      /* Invalid scenario as list must exist if INT in query string, redirect to AddNomineeOrInvestor */
      "Redirect to 'AddNomineeOrInvestor' page" when {
        "a 'backlink' is defined but no 'investor details list' is retrieved" in {
          mockEnrolledRequest(seisSchemeTypesModel)
          setupMocks(None, backUrl)
          showWithSessionAndAuth(controller.show(1))(
            result => {
              status(result) shouldBe SEE_OTHER
              redirectLocation(result) shouldBe Some(controllers.seis.routes.AddInvestorOrNomineeController.show(None).url)
            }
          )
        }
      }

      "Redirect to the AddInvestorOrNominee page" when {
        "a 'backlink' is defined, an 'investor details list' is retrieved and an INVALID 'id' is defined" in {
          mockEnrolledRequest(seisSchemeTypesModel)
          setupMocks(Some(listOfInvestorsComplete), backUrl)
          showWithSessionAndAuth(controller.show(obviouslyInvalidId))(
            result => {
              status(result) shouldBe SEE_OTHER
              redirectLocation(result) shouldBe Some(controllers.seis.routes.AddInvestorOrNomineeController.show(None).url)
            }
          )
        }
      }

      "redirect to the 'CompanyOrIndividual' page" when {
        "a 'backlink' is defined, an 'investor details list' is retrieved but a 'company or individual' is not provided" in {
          mockEnrolledRequest(seisSchemeTypesModel)
          setupMocks(Some(listOfInvestorsIncompleteCompanyOrIndividualMissing),  backUrl)
          showWithSessionAndAuth(controller.show(2))(
            result => {
              status(result) shouldBe SEE_OTHER
              redirectLocation(result) shouldBe Some(controllers.seis.routes.AddInvestorOrNomineeController.show(None).url)
            }
          )
        }
      }

      "return an 'OK' and load the page with a empty form" when {
        "a 'backlink' is defined, an 'investor details list' is retrieved, a 'share issue date' is retrieved with " +
          "an defined 'NumberOfSharesPurchased' model at position 'id'" in {
          mockEnrolledRequest(seisSchemeTypesModel)
          setupMocks(Some(listOfInvestorsComplete),  backUrl)
          showWithSessionAndAuth(controller.show(2))(
            result => {
              status(result) shouldBe OK
            }
          )
        }
      }

      "return an 'OK' and load the page with a populated form" when {
        "a 'backlink' is defined, an 'investor details list' is retrieved, a 'company or individual' is retrieved with " +
          "an undefined 'IsExistingShareHolder' model at position 'id'" in {
          mockEnrolledRequest(seisSchemeTypesModel)
          setupMocks(Some(listOfInvestorsIncompleteIsExistingShareModelMissing), backUrl)
          showWithSessionAndAuth(controller.show(2))(
            result => {
              status(result) shouldBe OK
            }
          )
        }
      }
    }

    "Submitting to the IsExistingShareHolder Controller when authenticated and enrolled" should {
      "redirect to the correct page if the form 'was not' previously populated" in {

        val formInput = "isExistingShareHolder" -> "Yes"
        setupMocks(Some(listOfInvestorsComplete), None)
        mockEnrolledRequest(seisSchemeTypesModel)
        submitWithSessionAndAuth(controller.submit(Some(Constants.typeCompany),backUrl),formInput)(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe
              Some(controllers.seis.routes.PreviousShareHoldingDescriptionController.show(listOfInvestorsComplete.head.processingId.get).url)
          }
        )
      }
    }



    "Submitting to the IsExistingShareHolder when authenticated and enrolled" should {
      "redirect to the correct page the form 'was' previously populated and had a processing id" in {

        val formInput = Seq("isExistingShareHolder" -> "Yes", "processingId" -> "2")
        setupMocks(Some(listOfInvestorsComplete), None)
        mockEnrolledRequest(seisSchemeTypesModel)
        submitWithSessionAndAuth(controller.submit(Some(Constants.typeCompany),backUrl), formInput:_*)(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe
              Some(controllers.seis.routes.PreviousShareHoldingDescriptionController.show(listOfInvestorsComplete.head.processingId.get).url)
          }
        )
      }
    }


    "Sending an invalid form submission with validation errors to the IsExistingShareHolder when authenticated and enrolled" should {
      "redirect to itself" in {
        setupMocks(Some(listOfInvestorsComplete), None)
        mockEnrolledRequest(seisSchemeTypesModel)
        val formInput = "isExistingShareHolder" -> ""
        submitWithSessionAndAuth(controller.submit(Some(Constants.typeCompany),backUrl), formInput)(
          result => {
            status(result) shouldBe BAD_REQUEST
          }
        )
      }
    }

  }
}