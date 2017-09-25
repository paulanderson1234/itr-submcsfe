
val penceSuffix = "00"
def getPenceSuffix(amount:String): String ={
  if (amount.matches("[0]+")) amount else amount.concat(penceSuffix)
}


val amount:String = getPenceSuffix("0")
val amount2:String = getPenceSuffix("00")
val amount5:String = getPenceSuffix("0000")
val amount3:String = getPenceSuffix("1")
val amount4:String = getPenceSuffix("12")

amount.matches("[0]+")