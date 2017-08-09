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

package models

import play.api.libs.json.{Json, OFormat}

case class CompanyDetailsModel(companyName : String,
                               companyAddressline1 : String,
                               companyAddressline2 : String,
                               companyAddressline3 : Option[String],
                               companyAddressline4 : Option[String],
                               companyPostcode : Option[String],
                               countryCode : String,
                               processingId: Option[Int]) {

  def toArray: Array[String] = Array(Some(companyAddressline1), Some(companyAddressline2),
    companyAddressline3, companyAddressline4, companyPostcode, Some(countryCode)).flatten
}

object CompanyDetailsModel {
  implicit val format: OFormat[CompanyDetailsModel] = Json.format[CompanyDetailsModel]
}
