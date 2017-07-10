import config.FrontendGlobal._
import controllers.seis.routes
import play.api.Logger
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

def getIt():Future[Option[Boolean]] = {

  //val cc: Option[Boolean] = Some(true)
  val cc: Option[Boolean] = None
  Future(cc)

}


def xxx() = {

  getIt().map {
    case Some(valid) if valid => "TODO: to total amount spent when done"
    case Some(valid) if !valid => "TODO: to Investor Nominee Page when done"
    case None => "[TotalAmountRaisedController][submit] - validateHasInvestmentTradeStartedCondition did not return expected true/false answer"
  }
}


val xx = xxx
xx
xx

