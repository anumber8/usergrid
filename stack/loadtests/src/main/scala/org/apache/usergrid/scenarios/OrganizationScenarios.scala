/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 package org.apache.usergrid.scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
 import org.apache.usergrid.datagenerators.FeederGenerator
 import org.apache.usergrid.settings.{Settings, Headers}
 import scala.concurrent.duration._

/**
 * Performs organization registration
 *
 *
 * Produces:
 *
 * orgName The name of the created organization
 * userName  The user name of the admin to log in with
 * password The password of the admin to use
 */
object OrganizationScenarios {

  //register the org with the randomly generated org
  val createOrgAndAdmin =
    exec(http("Create Organization")
      .post(Settings.baseUrl + "/management/organizations")
      .headers(Headers.jsonAnonymous)
      .body(StringBody("{\"organization\":\"" + Settings.org + "\",\"username\":\"" + Settings.admin + "\",\"name\":\"${entityName}\",\"email\":\"${entityName}@apigee.com\",\"password\":\"" + Settings.password + "\"}"))
      .check(status.in(200 to 400))
    )
  val createOrgBatch =
    feed(FeederGenerator.generateRandomEntityNameFeeder("org", 1))
      .exec(OrganizationScenarios.createOrgAndAdmin)
      .exec(TokenScenarios.getManagementToken)
      .exec(session => {
      // print the Session for debugging, don't do that on real Simulations
      println(session)
      session
    })
      .exec(ApplicationScenarios.createApplication)
      .exec(NotifierScenarios.createNotifier)

  val createOrgScenario = scenario("Create org")
    .exec(OrganizationScenarios.createOrgBatch)
    .inject(atOnceUsers(1))
    .protocols(http.baseURL(Settings.baseUrl))

}
