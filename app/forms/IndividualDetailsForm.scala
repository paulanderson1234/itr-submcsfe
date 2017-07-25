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

import models.IndividualDetailsModel
import play.api.data.Form
import play.api.data.Forms._
import utils.Validation._

object IndividualDetailsForm {
  val individualDetailsForm = Form(
    mapping(
      "forename" -> nonEmptyText,
      "surname" -> nonEmptyText,
      "addressline1" -> mandatoryAddressLineCheck,
      "addressline2" -> mandatoryAddressLineCheck,
      "addressline3" -> optional(optionalAddressLineCheck),
      "addressline4" -> optional(addressLineFourCheck),
      "postcode" ->    optional(postcodeCheck),
      "countryCode" -> countryCodeCheck,
      "processingId" -> optional(number)
    )(IndividualDetailsModel.apply)(IndividualDetailsModel.unapply).verifying(postcodeCountryCheck))

}
