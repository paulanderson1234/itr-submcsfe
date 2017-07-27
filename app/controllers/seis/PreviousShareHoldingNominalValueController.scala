///*
// * Copyright 2017 HM Revenue & Customs
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package controllers.seis
//
//import auth.{AuthorisedAndEnrolledForTAVC, SEIS}
//import common.KeystoreKeys
//import config.{AppConfig, FrontendAppConfig, FrontendAuthConnector}
//import connectors.{EnrolmentConnector, S4LConnector}
//import controllers.predicates.FeatureSwitch
//import forms.PreviousShareHoldingNominalValueForm._
//import models.investorDetails.PreviousShareHoldingNominalValueModel
//import play.api.Play.current
//import play.api.data.Form
//import play.api.i18n.Messages.Implicits._
//import play.api.mvc.{Action, AnyContent, Result}
//import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
//import uk.gov.hmrc.play.frontend.controller.FrontendController
//import views.html.seis.investors.PreviousShareHoldingNominalValue
//
//import scala.concurrent.Future
//
//object PreviousShareHoldingNominalValueController extends PreviousShareHoldingNominalValueController {
//  override lazy val enrolmentConnector: EnrolmentConnector = EnrolmentConnector
//  override lazy val applicationConfig: AppConfig = FrontendAppConfig
//  override lazy val s4lConnector: S4LConnector = S4LConnector
//  override lazy val authConnector: AuthConnector = FrontendAuthConnector
//}
//
//trait PreviousShareHoldingNominalValueController extends FrontendController with AuthorisedAndEnrolledForTAVC with FeatureSwitch {
//
//  override val acceptedFlows = Seq(Seq(SEIS))
//
//  val show: Action[AnyContent] = featureSwitch(applicationConfig.seisFlowEnabled) {
//    AuthorisedAndEnrolled.async {
//      implicit user =>
//        implicit request =>
//          s4lConnector.fetchAndGetFormData[PreviousShareHoldingNominalValueModel](KeystoreKeys.previousShareHoldingNominalValue).map {
//            case Some(data) => Ok(views.html.seis.investors.PreviousShareHoldingNominalValue(previousShareHoldingNominalValueForm.fill(data)))
//            case None => Ok(views.html.seis.investors.PreviousShareHoldingNominalValue(previousShareHoldingNominalValueForm))
//          }
//    }
//  }
//
//  val submit: Action[AnyContent] = featureSwitch(applicationConfig.seisFlowEnabled) {
//    AuthorisedAndEnrolled.async {
//      implicit user =>
//        implicit request =>
//          val success: PreviousShareHoldingNominalValueModel => Future[Result] = { model =>
//            s4lConnector.saveFormData(KeystoreKeys.previousShareHoldingNominalValue, model).map(_ =>
//              Redirect(controllers.seis.routes.PreviousShareHoldingNominalValueController.show())
//              //TODO - Navigates to the Share Issue Date page when available
//            )
//          }
//
//          val failure: Form[PreviousShareHoldingNominalValueModel] => Future[Result] = { form =>
//            Future.successful(BadRequest(views.html.seis.investors.PreviousShareHoldingNominalValue(form)))
//          }
//
//          previousShareHoldingNominalValue.bindFromRequest().fold(failure, success)
//    }
//  }
//}
