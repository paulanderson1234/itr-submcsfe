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

package forms

import common.Constants
import models.CompanyDetailsModel
import play.api.data.Form
import utils.Validation._
import play.api.data.Forms._


object CompanyDetailsForm  {
  val companyDetailsForm = Form(
    mapping(
      "companyName"  -> nonEmptyText(maxLength = Constants.CompanyDetailsMaxLength),
      "companyAddressline1" -> mandatoryAddressLineCheck,
      "companyAddressline2" -> mandatoryAddressLineCheck,
      "companyAddressline3" -> optional(optionalAddressLineCheck),
      "companyAddressline4" -> optional(addressLineFourCheck),
      "companyPostcode" -> optional(postcodeCheck),
      "countryCode" -> countryCodeCheck,
      "processingId" -> optional(number),
      "shareHolderProcessingId" -> optional(number)
    )(CompanyDetailsModel.apply)(CompanyDetailsModel.unapply).verifying(postcodeCountryCheckConstraint)
  )
}
