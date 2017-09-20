import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import models.{MarketDescriptionModel, NewGeographicalMarketModel, NewProductModel}

import scala.concurrent.duration.Duration



case class MarketInfoAnswersModel(newGeographicMarket: NewGeographicalMarketModel,
                                  newProductMarket: NewProductModel,
                                  marketDescription: Option[MarketDescriptionModel]){


}

import scala.concurrent.Future



def getMarketInfoAnswerModel(newGeographicalMarketModel: Option[NewGeographicalMarketModel],
                             newProductModel: Option[NewProductModel], mktDescriptionModel: Option[MarketDescriptionModel]
                             ): Future[Option[MarketInfoAnswersModel]] = {
  //val correspondenceAddress = s4lConnector.fetchAndGetFormData[ConfirmCorrespondAddressModel](KeystoreKeys.confirmContactAddress)

  val newGeographicalMarket = Future(newGeographicalMarketModel)
  val newProduct = Future(newProductModel)
  val marketDescription =  Future(mktDescriptionModel)

  def createModel(newGeographicalMarketModel: Option[NewGeographicalMarketModel],
                  newProductModel: Option[NewProductModel], marketDescriptionModel:Option[MarketDescriptionModel]) = {
    for {
      newGeographicalMarketModel <- newGeographicalMarketModel
      newProductModel <- newProductModel
    } yield MarketInfoAnswersModel(newGeographicalMarketModel, newProductModel,marketDescriptionModel)
  }

  for {
    newGeographicalMarketModel <- newGeographicalMarket
    newProductModel <- newProduct
    marketDescriptionModel <- marketDescription
  } yield createModel(newGeographicalMarketModel, newProductModel,marketDescriptionModel)
}

val emptyGeo = None
val emptynewProd = None
val emptyMktDesc = None

val prod = NewProductModel("test")
val geo = NewGeographicalMarketModel("test")

def testIt : Future[Boolean] ={

//  def checkIt(model:Option[MarketInfoAnswersModel]) :Boolean = {
//
//    model match{
//      case Some(data) => true
//      case _ => false
//    }
//  }

//  getMarketInfoAnswerModel(emptyGeo, emptynewProd, emptyMktDesc).map( res => {
//    if (res.isEmpty)  println("empty") else println("not empty")
//  })

  Future.successful(true)
//
//  for{
//    model <- getMarketInfoAnswerModel(emptyGeo, emptynewProd, emptyMktDesc)
//    result <- checkIt(model)
//  } yield result


}

val empty = getMarketInfoAnswerModel(emptyGeo, emptynewProd, emptyMktDesc).map { res => {
  if (res.isEmpty)  "Its EMPTY" else "ITS NOT EMPTY"
}
}

val x = Await.result[String](empty, Duration(1000, duration.MILLISECONDS))


x

val nonempty = getMarketInfoAnswerModel(Some(geo), Some(prod), emptyMktDesc).map { res => {
  if (res.isEmpty)  "Its EMPTY" else "ITS NOT EMPTY"
}
}


val xyz = Await.result[String](nonempty, Duration(1000, duration.MILLISECONDS))


xyz



//val test1 = getMarketInfoAnswerModel(emptyGeo, emptynewProd, emptyMktDesc).map {
//  result => print(s"Result is None: ${result.isEmpty}")
//}.map{result => result}

