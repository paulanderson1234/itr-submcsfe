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
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.Helpers.{ControllerHelpers, PreviousInvestorShareHoldersHelper}
import controllers.predicates.FeatureSwitch
import forms.PreviousShareHoldingDescriptionForm._
import models.investorDetails.InvestorDetailsModel
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.seis.investors.PreviousShareHoldingDescription

import scala.concurrent.Future

object PreviousShareHoldingDescriptionController extends PreviousShareHoldingDescriptionController
{
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait PreviousShareHoldingDescriptionController extends FrontendController with AuthorisedAndEnrolledForTAVC with FeatureSwitch with ControllerHelpers {

  override val acceptedFlows = Seq(Seq(SEIS))

  // id: Option[Int] should be changed to Int once the initial pages were implemented
  def show(investorProcessingId: Int, id: Option[Int]): Action[AnyContent] = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user =>
      implicit request =>

        def process(backUrl: Option[String]) = {
          if (backUrl.isDefined) {
            s4lConnector.fetchAndGetFormData[Vector[InvestorDetailsModel]](KeystoreKeys.investorDetails).map {
              case Some(data) => {
                val itemToUpdateIndex = data.indexWhere(_.processingId.getOrElse(0) == investorProcessingId)
                if (itemToUpdateIndex != -1) {
                  val model = data.lift(itemToUpdateIndex)
                  if (model.get.previousShareHoldingModels.isDefined && model.get.previousShareHoldingModels.get.size > 0) {
                    id match {
                      case Some(idVal) => {
                        val shareHoldingsIndex = model.get.previousShareHoldingModels.get.indexWhere(_.processingId.getOrElse(0) == idVal)
                        if (shareHoldingsIndex != -1) {
                          val shareHolderModel = model.get.previousShareHoldingModels.get.lift(shareHoldingsIndex)
                          Ok(PreviousShareHoldingDescription(model.get.companyOrIndividualModel.get.companyOrIndividual,
                            previousShareHoldingDescriptionForm.fill(shareHolderModel.get.previousShareHoldingDescriptionModel.get), backUrl.get))
                        } else {
                          Ok(PreviousShareHoldingDescription(model.get.companyOrIndividualModel.get.companyOrIndividual,
                            previousShareHoldingDescriptionForm, backUrl.get))
                        }
                      }
                      case None => {
                        val shareHolderModel = model.get.previousShareHoldingModels.get.last
                        Ok(PreviousShareHoldingDescription(model.get.companyOrIndividualModel.get.companyOrIndividual,
                          previousShareHoldingDescriptionForm.fill(shareHolderModel.previousShareHoldingDescriptionModel.get), backUrl.get))
                      }
                    }
                  }
                  else Ok(PreviousShareHoldingDescription(model.get.companyOrIndividualModel.get.companyOrIndividual,
                    previousShareHoldingDescriptionForm, backUrl.get))
                }
                else Redirect(routes.AddInvestorOrNomineeController.show())
              }
              case None => Redirect(controllers.seis.routes.AddInvestorOrNomineeController.show())
            }
          }
          else Future.successful(Redirect(controllers.seis.routes.AddInvestorOrNomineeController.show()))
        }

        for {
          backUrl <- s4lConnector.fetchAndGetFormData[String](KeystoreKeys.backLinkShareClassAndDescription)
          route <- process(backUrl)
        } yield route
    }
  }

  def submit(companyOrIndividual: Option[String], backUrl: Option[String]): Action[AnyContent] = featureSwitch(applicationConfig.seisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user =>
      implicit request =>
        previousShareHoldingDescriptionForm.bindFromRequest().fold(
          formWithErrors => {
            Future.successful(BadRequest(PreviousShareHoldingDescription(companyOrIndividual.get, formWithErrors, backUrl.get)))
          },
          validFormData => {
            validFormData.processingId match {
              case Some(_) => PreviousInvestorShareHoldersHelper.updateShareClassAndDescription(s4lConnector, validFormData).map {
                data => {
                  s4lConnector.saveFormData(KeystoreKeys.backLinkIsPreviousShareHoldingNominalValue,
                    routes.PreviousShareHoldingDescriptionController.show(data.investorProcessingId.get, data.processingId).url)
                  Redirect(routes.PreviousShareHoldingNominalValueController.show(data.investorProcessingId.get, data.processingId.get))
                }
              }
              case None => PreviousInvestorShareHoldersHelper.addShareClassAndDescription(s4lConnector, validFormData).map {
                data => {
                  s4lConnector.saveFormData(KeystoreKeys.backLinkIsPreviousShareHoldingNominalValue,
                    routes.PreviousShareHoldingDescriptionController.show(data.investorProcessingId.get, data.processingId).url)
                  Redirect(routes.PreviousShareHoldingNominalValueController.show(data.investorProcessingId.get, data.processingId.get))
                }
              }
            }
          }
        )
    }
  }
}
