
val penceSuffix = "00"
def getPenceSuffix(amount:String): String ={
  if (amount.matches("[0]+")) amount else amount.concat(penceSuffix)
}

def poundToPence(pounds: Either[String, Int]): String = {
  pounds match {
    case Left(poundsMatch) =>  getPenceSuffix(poundsMatch)
    case Right(poundsMatch) => (poundsMatch * 100).toString
  }
}

val amount:String = poundToPence(Left("0"))
val amount2:String = poundToPence(Left("00"))
val amount5:String = poundToPence(Left("0000"))
val amount3:String = poundToPence(Left("999999999"))
val amount4:String = poundToPence(Left("2"))

amount.matches("[0]+")
