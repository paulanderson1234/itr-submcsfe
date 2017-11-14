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

import auth.{AuthorisedAndEnrolledForTAVC, EIS}
import common.{Constants, KeystoreKeys}
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.Helpers.{ControllerHelpers, PreviousRepaymentsHelper}
import forms.SharesRepaymentTypeForm._
import models.repayments.{SharesRepaymentDetailsModel, SharesRepaymentTypeModel}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.mvc.{Action, AnyContent}
import views.html.eis.investors.SharesRepaymentType

import scala.concurrent.Future

object SharesRepaymentTypeController extends SharesRepaymentTypeController
{
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait SharesRepaymentTypeController extends FrontendController with AuthorisedAndEnrolledForTAVC with ControllerHelpers{

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
                  val form = fillForm(sharesRepaymentTypeForm, retrieveRepaymentsData(index, data)(_.sharesRepaymentTypeModel))
                  Ok(SharesRepaymentType(form, backUrl.get))
                }
              }
            }
          }
          else Future.successful(Redirect(controllers.eis.routes.AnySharesRepaymentController.show()))
        }

        for {
          backUrl <- s4lConnector.fetchAndGetFormData[String](KeystoreKeys.backLinkSharesRepaymentType)
          route <- process(backUrl)
        } yield route
      }
  }

  val submit = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    sharesRepaymentTypeForm.bindFromRequest().fold(
      formWithErrors => {
        ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkSharesRepaymentType, s4lConnector).flatMap(url =>
          Future.successful(BadRequest(SharesRepaymentType(formWithErrors, url.get))))
      },
      validFormData => {
        validFormData.processingId match {
          case Some(_) => PreviousRepaymentsHelper.updateSharesRepaymentType(s4lConnector, validFormData).map {
            sharesRepaymentDetails => {
              s4lConnector.saveFormData(KeystoreKeys.backLinkSharesRepaymentDate,
                routes.SharesRepaymentTypeController.show(sharesRepaymentDetails.processingId.get).url)
              Redirect(routes.DateSharesRepaidController.show(sharesRepaymentDetails.processingId.get))
            }
          }
          case None => PreviousRepaymentsHelper.addSharesRepaymentType(s4lConnector, validFormData).map {
            sharesRepaymentDetails => {
              s4lConnector.saveFormData(KeystoreKeys.backLinkSharesRepaymentDate,
                routes.SharesRepaymentTypeController.show(sharesRepaymentDetails.processingId.get).url)
              Redirect(routes.DateSharesRepaidController.show(sharesRepaymentDetails.processingId.get))
            }
          }
        }
      }
    )
  }
}
