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
import controllers.Helpers.{ControllerHelpers, PreviousRepaymentsHelper}
import forms.AmountSharesRepaymentForm._
import forms.DateSharesRepaidForm.dateSharesRepaidForm
import models.repayments.{AmountSharesRepaymentModel, SharesRepaymentDetailsModel}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import views.html.eis.investors.AmountSharesRepayment

import scala.concurrent.Future
import play.api.mvc.{Action, AnyContent, Result}
import play.api.data.Form


object AmountSharesRepaymentController extends AmountSharesRepaymentController{
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait AmountSharesRepaymentController extends FrontendController with AuthorisedAndEnrolledForTAVC with ControllerHelpers{

  override val acceptedFlows = Seq(Seq(EIS))

  def show(id: Int): Action[AnyContent] = AuthorisedAndEnrolled.async {
    implicit user =>
      implicit request => {
        def process(backUrl: Option[String]) = {
          if (backUrl.isDefined) {
            s4lConnector.fetchAndGetFormData[Vector[SharesRepaymentDetailsModel]](KeystoreKeys.sharesRepaymentDetails).map { vector =>
              redirectIfNoRepayments(vector) { data =>
                val itemToUpdateIndex = getRepaymentsIndex(id, data)
                redirectInvalidRepayments(itemToUpdateIndex) { index =>
                  val form = fillForm(amountSharesRepaymentForm, retrieveRepaymentsData(index, data)(_.amountSharesRepaymentModel))
                  Ok(AmountSharesRepayment(form, backUrl.get))
                }
              }
            }
          }
          else Future.successful(Redirect(controllers.eis.routes.AnySharesRepaymentController.show()))
        }

        for {
          backUrl <- s4lConnector.fetchAndGetFormData[String](KeystoreKeys.backLinkSharesRepaymentAmount)
          route <- process(backUrl)
        } yield route
      }
  }

  val submit = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    amountSharesRepaymentForm.bindFromRequest().fold(
      formWithErrors => {
        ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkSharesRepaymentAmount, s4lConnector).flatMap(url =>
          Future.successful(BadRequest(AmountSharesRepayment(formWithErrors, url.get))))
      },
      validFormData => {
        validFormData.processingId match {
          case Some(_) => PreviousRepaymentsHelper.updateAmountSharesRepayment(s4lConnector, validFormData)
            Future.successful(Redirect(routes.ReviewPreviousRepaymentsController.show()))

          case None => PreviousRepaymentsHelper.addAmountSharesRepayment(s4lConnector, validFormData)
              Future.successful(Redirect(routes.ReviewPreviousRepaymentsController.show()))
        }
      }
    )
  }
}

