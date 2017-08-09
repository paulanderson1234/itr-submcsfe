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

import auth.{AuthorisedAndEnrolledForTAVC, SEIS}
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.predicates.FeatureSwitch
import models.{AddInvestorOrNomineeModel, CompanyDetailsModel, CompanyOrIndividualModel, IndividualDetailsModel}
import models.investorDetails._
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.Play.current
import play.api.i18n.Messages.Implicits._

import scala.concurrent.Future

object ReviewInvestorDetailsController extends ReviewInvestorDetailsController {
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait ReviewInvestorDetailsController extends FrontendController with AuthorisedAndEnrolledForTAVC with FeatureSwitch {

  override val acceptedFlows = Seq(Seq(SEIS))

  val testModel = InvestorDetailsModel(
    Some(AddInvestorOrNomineeModel("Nominee", Some(1))),
    Some(CompanyOrIndividualModel("Company", Some(1))),
    Some(CompanyDetailsModel("Company Name", "72", "Redwood Close", Some("Albrook"), None, None, "DE", Some(1))),
    Some(IndividualDetailsModel("James", "Forster", "68", "Purbeck Dale", Some("Telford"), None, Some("TF4 2QW"), "GB", Some(1))),
    Some(NumberOfSharesPurchasedModel(100, Some(1))),
    Some(HowMuchSpentOnSharesModel(1000, Some(1))),
    Some(IsExistingShareHolderModel("Yes")),
    Some(Vector(
      PreviousShareHoldingModel(
        Some(InvestorShareIssueDateModel(Some(1), Some(2), Some(2016), Some(1), Some(1))),
        Some(NumberOfPreviouslyIssuedSharesModel(1000, Some(1), Some(1))),
        Some(PreviousShareHoldingNominalValueModel(10000, Some(1), Some(1))),
        Some(PreviousShareHoldingDescriptionModel("Class 1 Share", Some(1), Some(1))),
        Some(1),
        Some(1)
      ),
      PreviousShareHoldingModel(
        Some(InvestorShareIssueDateModel(Some(1), Some(2), Some(2016), Some(1), Some(1))),
        Some(NumberOfPreviouslyIssuedSharesModel(1000, Some(1), Some(1))),
        Some(PreviousShareHoldingNominalValueModel(10000, Some(1), Some(1))),
        Some(PreviousShareHoldingDescriptionModel("Class 2 Share", Some(2), Some(1))),
        Some(2),
        Some(1)
      )
    )),
    Some(1)
  )

  val show: Int => Action[AnyContent] = id => featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>
      Future.successful(Ok(views.html.seis.investors.ReviewInvestorDetails(testModel)))
    }
  }
}
