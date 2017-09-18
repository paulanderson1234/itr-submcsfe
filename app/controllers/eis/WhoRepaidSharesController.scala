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

import auth.{AuthorisedAndEnrolledForTAVC, EIS, VCT}
import common.KeystoreKeys
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.Helpers.{ControllerHelpers, PreviousInvestorsHelper, PreviousRepaymentsHelper}
import forms.AddInvestorOrNomineeForm.addInvestorOrNomineeForm
import forms.WhoRepaidSharesForm._
import models.repayments.{SharesRepaymentDetailsModel, WhoRepaidSharesModel}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import views.html.eis.investors.{AnySharesRepayment, WhoRepaidShares}

import scala.concurrent.Future
import play.api.mvc.{Action, AnyContent, Result}
import play.api.data.Form

import scala.reflect.macros.whitebox


object WhoRepaidSharesController extends WhoRepaidSharesController{
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait WhoRepaidSharesController extends FrontendController with AuthorisedAndEnrolledForTAVC with ControllerHelpers{

  override val acceptedFlows = Seq(Seq(EIS))

  def show(id: Option[Int]): Action[AnyContent] = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    def routeRequest(backUrl: Option[String]) = {
      if (backUrl.isDefined) {
        s4lConnector.fetchAndGetFormData[Vector[SharesRepaymentDetailsModel]](KeystoreKeys.sharesRepaymentDetails).map {
          case Some(data) if (data.nonEmpty) =>
            id match {
              case Some(idVal) => {
                redirectInvalidRepayments(getRepaymentsIndex(idVal, data)) { id =>
                  val form = fillForm(whoRepaidSharesForm, retrieveRepaymentsData(id, data)(_.whoRepaidSharesModel))
                  Ok(WhoRepaidShares(form, backUrl.get))
                }
              }
              case None => {
                val sharesRepaymentsDetailsModel = data.last
                if (sharesRepaymentsDetailsModel.validate) Ok(WhoRepaidShares(whoRepaidSharesForm, backUrl.get))
                else Ok(WhoRepaidShares(
                  whoRepaidSharesForm.fill(sharesRepaymentsDetailsModel.whoRepaidSharesModel.get), backUrl.get))
              }
            }
          case _ => Ok(WhoRepaidShares(whoRepaidSharesForm, backUrl.get))
        }
      }
      else Future.successful(Redirect(controllers.eis.routes.ReviewAllInvestorsController.show()))
    }

    for {
      link <- ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkWhoRepaidShares, s4lConnector)
      route <- routeRequest(link)
    } yield route
  }

  val submit = AuthorisedAndEnrolled.async { implicit user =>
    implicit request =>
      whoRepaidSharesForm.bindFromRequest().fold(
        formWithErrors => {
          ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkWhoRepaidShares, s4lConnector).flatMap(url =>
            Future.successful(BadRequest(WhoRepaidShares(formWithErrors, url.get))))
        },
        validFormData => {
          validFormData.processingId match {
            case Some(_) => PreviousRepaymentsHelper.updateWhoRepaidShares(s4lConnector, validFormData).map {
              sharesRepaymentDetails => {
                s4lConnector.saveFormData(KeystoreKeys.backLinkSharesRepaymentType,
                  routes.WhoRepaidSharesController.show(sharesRepaymentDetails.processingId).url)
                Redirect(routes.SharesRepaymentTypeController.show(sharesRepaymentDetails.processingId.get))
              }
            }
            case None => PreviousRepaymentsHelper.addWhoRepaidShares(s4lConnector, validFormData).map {
              sharesRepaymentDetails => {
                s4lConnector.saveFormData(KeystoreKeys.backLinkSharesRepaymentType,
                  routes.WhoRepaidSharesController.show(sharesRepaymentDetails.processingId).url)
                Redirect(routes.SharesRepaymentTypeController.show(sharesRepaymentDetails.processingId.get))
              }
            }
          }
        }
      )
  }
}

