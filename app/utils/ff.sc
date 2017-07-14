import common.Constants
import utils.Transformers

def getAmountAsFormattedString(value: AnyVal): String = {
  val transformedValue = Transformers.numberToFormattedNumber(value)
  Constants.amountFormattedAnswer(transformedValue)
}

val ff = BigDecimal(12.00)

val c = Transformers.bigDecimalToIntegerString(ff)