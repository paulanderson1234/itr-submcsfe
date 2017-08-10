
import java.time.ZoneId
import java.util.Date

import org.joda.time.{LocalDate, YearMonth}
import play.api.libs.json.Json
import testOnly.controllers.InvestorTestHelper

import scala.util.Random


val date = new Date()
val localDate = date.toInstant.atZone(ZoneId.systemDefault()).toLocalDate
val todayYear: Int = localDate.getYear

def getRandomIntBetween(start: Int, end: Int): Int = {
  val rnd = new scala.util.Random
  start + rnd.nextInt( (end - start) + 1 )
}
//
val xz = getRandomIntBetween(2005, 2017)
xz

val x = "ee"
x


case class InvestorShareIssueDateModel(dateOfIssueDay: Int, dateOfIssueMonth: Int, dateOfIssueYear: Int,
                                      processingId: Option[Int] = None,
                                      investorProcessingId: Option[Int] = None)
object InvestorShareIssueDateModel{
  implicit val formats = Json.format[InvestorShareIssueDateModel]
}


val c = getRandomInvestorShareIssueDateModel(2, 3)
c
//////
def getRandomInvestorShareIssueDateModel(processingId: Int, investorId: Int): InvestorShareIssueDateModel = {

  InvestorShareIssueDateModel(
    dateOfIssueDay = getRandomIntBetween(1, 28),
    dateOfIssueMonth = getRandomIntBetween(1, 12),
    dateOfIssueYear =  getRandomIntBetween(2000, todayYear),
    investorProcessingId = Some(investorId),
    processingId = Some(processingId)
  )
}

//def getMaxDay(month: Int, year:Int):Int = {
//
//  val localDate = LocalDate.fromDateFields(new Date(year, month, 1))
//  val endOfMonth = localDate.dayOfMonth().withMaximumValue()
//  endOfMonth.getDayOfMonth
//}
//
//val gggg = getMaxDay(13, 2015)
//gggg
///

//1 to 10 map {i =>
//
//
//}

//val x = 1 to 10
//
//1 to 10 foreach( i => i
//
//  )

//val lengths = for (i <- 1 to 10) yield {
//      // imagine that this required multiple lines of code
//
//   }
//
//case class Person(age:Int, name:String)
//
//val d = for (f <- 1 to 3) yield getPerson(f)
//d
////
//def getPerson(age: Int) = {
//  Person(age, "ff")
//}

//val random = new scala.util.Random
//
//val  num = Random.nextInt(999999999)
//num
//
//def randomWholeAmount(maxAmount:Int):Int = {
//  random.nextInt(maxAmount)
//}
////
//def randomAlphaString(n: Int): String =
//  randomString("ABCDEFGHIJKLMNOPQRSTUVWXZYabcdefghijklmnopqrstuvwxyz")(n)
//
//val num2 = randomWholeAmount(9999)
//
//val xx:Int = 12
//val cc = BigDecimal(xx)
//
//
//def checkkk:Int = {
//
//
//     random.nextInt(2)
//}
//
//def randomString(alphabet: String)(n: Int): String =
//  Stream.continually(random.nextInt(alphabet.length)).map(alphabet).take(n).mkString
//
//def randomNumberString(n: Int): String =
//  randomString("1234567890")(n)
//
//def randomDecimal = {
//  random.nextInt(999999999)
//}
//
//
//
//def randomAlphanumericString(n: Int): String =
//  randomString("abcdefghijklmnopqrstuvwxyz0123456789")(n)
//
//def randomCompnayOrIndividualCC(id:Int):String = {
//  if (id % 2 == 0) "Company" else "Individual"
//}
//
////////
////val ffff  = randomCompnayOrIndividual
//val kkk = checkkk
//
//val corI = randomCompnayOrIndividualCC(3)
//corI
//
//val f =  random.nextFloat()
//f
//
//def randomDecimal(processingId:Int):BigDecimal = {
//  if (processingId % 2 == 0) 9999999999999.00
//  else BigDecimal("999999999999.999999").setScale(5, BigDecimal.RoundingMode.HALF_UP)
//}
//
//val rand = randomDecimal(3)
//
//val letter = random.alphanumeric.filter(_.isLetter).head
//
//val numve = "QQ" + randomNumberString(2) + " " + randomNumberString(1) + "QQ"
//
//val forename = randomAlphaString(35)
//val surname = randomAlphanumericString(35)
/////////

//val list = InvestorTestHelper.getInvestors(2, 2)
//val hold = list.size
//hold

