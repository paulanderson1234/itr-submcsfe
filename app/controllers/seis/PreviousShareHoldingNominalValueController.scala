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
import common.KeystoreKeys
import config.{AppConfig, FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.Helpers.PreviousInvestorShareHoldersHelper
import controllers.predicates.FeatureSwitch
import forms.PreviousShareHoldingNominalValueForm._
import models.investorDetails.{InvestorDetailsModel, PreviousShareHoldingNominalValueModel}
import play.api.Play.current
import play.api.data.Form
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Action, AnyContent, Result}
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.seis.investors.PreviousShareHoldingNominalValue

import scala.concurrent.Future

object PreviousShareHoldingNominalValueController extends PreviousShareHoldingNominalValueController {
  override lazy val enrolmentConnector: EnrolmentConnector = EnrolmentConnector
  override lazy val applicationConfig: AppConfig = FrontendAppConfig
  override lazy val s4lConnector: S4LConnector = S4LConnector
  override lazy val authConnector: AuthConnector = FrontendAuthConnector
}

trait PreviousShareHoldingNominalValueController extends FrontendController with AuthorisedAndEnrolledForTAVC with FeatureSwitch {

  override val acceptedFlows = Seq(Seq(SEIS))

  def show(investorProcessingId: Int, id: Int): Action[AnyContent] = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async {
      implicit user =>
        implicit request =>
          def process(backUrl: Option[String]) = {
            if (backUrl.isDefined) {
              s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map {
                case Some(data) => {
                  val itemToUpdateIndex = data.indexWhere(_.processingId.getOrElse(0) == investorProcessingId)
                  if (itemToUpdateIndex != -1) {
                    val model = data.lift(itemToUpdateIndex)
                    if (model.get.previousShareHoldingModels.isDefined && model.get.previousShareHoldingModels.get.size > 0) {
                      val shareHoldingsIndex = model.get.previousShareHoldingModels.get.indexWhere(_.processingId.getOrElse(0) == id)
                      if (shareHoldingsIndex != -1) {
                        val shareHolderModel = model.get.previousShareHoldingModels.get.lift(shareHoldingsIndex)
                        if(shareHolderModel.get.previousShareHoldingNominalValueModel.isDefined) {
                          Ok(PreviousShareHoldingNominalValue(
                            previousShareHoldingNominalValueForm.fill(shareHolderModel.get.previousShareHoldingNominalValueModel.get),
                            backUrl.get))
                        }
                        else
                          Ok(PreviousShareHoldingNominalValue(previousShareHoldingNominalValueForm, backUrl.get))
                      }
                      else Redirect(routes.AddInvestorOrNomineeController.show(model.get.processingId))
                    }
                    else Redirect(routes.AddInvestorOrNomineeController.show(model.get.processingId))
                  }
                  else Redirect(routes.AddInvestorOrNomineeController.show())
                }
                case None => Redirect(controllers.seis.routes.AddInvestorOrNomineeController.show())
              }
            }
            else Future.successful(Redirect(controllers.seis.routes.AddInvestorOrNomineeController.show()))
          }

          for {
            backUrl <- s4lConnector.fetchAndGetFormData[String](KeystoreKeys.backLinkIsPreviousShareHoldingNominalValue)
            route <- process(backUrl)
          } yield route
    }
  }

  def submit(backUrl: Option[String]): Action[AnyContent] = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async {
      implicit user =>
        implicit request =>
          val success: PreviousShareHoldingNominalValueModel => Future[Result] = { model =>
            model.processingId match {
              case Some(_) => PreviousInvestorShareHoldersHelper.updatePreviousShareHoldingNominalValue(s4lConnector, model).map {
                data => {
                  s4lConnector.saveFormData(KeystoreKeys.backLinkInvestorShareIssueDate,
                    routes.PreviousShareHoldingNominalValueController.show(data.investorProcessingId.get, data.processingId.get).url)
                  Redirect(routes.InvestorShareIssueDateController.show(data.investorProcessingId.get, data.processingId.get))
                }
              }
              case None => PreviousInvestorShareHoldersHelper.addPreviousShareHoldingNominalValue(s4lConnector, model).map {
                data => {
                  s4lConnector.saveFormData(KeystoreKeys.backLinkInvestorShareIssueDate,
                    routes.PreviousShareHoldingNominalValueController.show(data.investorProcessingId.get, data.processingId.get).url)
                  Redirect(routes.InvestorShareIssueDateController.show(data.investorProcessingId.get, data.processingId.get))
                }
              }
            }
          }

          val failure: Form[PreviousShareHoldingNominalValueModel] => Future[Result] = { form =>
            Future.successful(BadRequest(PreviousShareHoldingNominalValue(form, backUrl.get)))
          }

          previousShareHoldingNominalValueForm.bindFromRequest().fold(failure, success)
    }
  }
}
