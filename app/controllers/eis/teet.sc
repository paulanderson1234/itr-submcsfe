val x1 = Int.MaxValue
val x2 = 999999999

val x4:Int =x1 * x2

val x3 = x1 * BigInt("99999999999")

val shareIssueDateDay = 12
val shareIssueDateMonth = 3
val shareIssueDateYear = 2017
val serviceUrl = "http://tax.gov.uk"

val previousInvestmentSchemesInRangeTotal = 3546466
val totalAmountRaised = 122344

val s = s"$serviceUrl/investment-tax-relief/investments-annual-limit-checker/check-total/share-issue-day/$shareIssueDateDay/" +
  s"share-issue-month/$shareIssueDateMonth/share-issue-year/$shareIssueDateYear/"+
  s"previous-schemes-total-in-range/$previousInvestmentSchemesInRangeTotal/proposed-amount/$totalAmountRaised"

val ss= s"$serviceUrl/investment-tax-relief/compliance-statement/validate-annual-limit/" +
  s"previous-schemes-total-in-range/$previousInvestmentSchemesInRangeTotal/total-amount-raised/$totalAmountRaised"