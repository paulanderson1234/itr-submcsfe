package controllers.seis

import auth.{AuthorisedAndEnrolledForTAVC, SEIS}
import com.sun.corba.se.impl.orbutil.closure.Future
import common.KeystoreKeys
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.predicates.FeatureSwitch
import models.ShareDescriptionModel
import uk.gov.hmrc.play.frontend.controller.FrontendController

object ShareDescriptionController extends ShareDescriptionController {


  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector

}

trait ShareDescriptionController extends FrontendController with AuthorisedAndEnrolledForTAVC with FeatureSwitch {

  override val acceptedFlows = Seq(Seq(SEIS))

  val show featureSwitch(applicationConfig.seisFlowEnabled) { AuthorisedAndEnrolled.async { implicit user => implicit request =>
    s4lConnector.fetchAndGetFormData[ShareDescriptionModel](KeystoreKeys.ShareDescription).map {
      case Some(data) => Ok(ShareDescription(shareDescriptionForm.fill(data)))
      case None => Ok(ShareDescription(shareDescriptionForm))
      }
    }
  }

  val submit featureSwitch(applicationConfig.seisFlowEnabled) { AuthorisedAndEnrolled.async { implicit user => implicit request =>
    shareDescriptionForm.bindFromRequest().fold(
      formWithErrors => {
        Future.succesful(BadRequest(ShareDescription(formWithErrors)))
      },
      validFormData => {
        s4lConnector.saveFormData(KeystoreKeys.ShareDescription, validFormData)
        Future.succesful(Redirect(routes.ShareDescription.show()))
      }
    )
  }
  }
}
