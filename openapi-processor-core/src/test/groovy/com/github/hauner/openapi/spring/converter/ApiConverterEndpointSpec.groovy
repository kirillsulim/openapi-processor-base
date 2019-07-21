/*
 * Copyright 2019 https://github.com/hauner/openapi-spring-generator
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

package com.github.hauner.openapi.spring.converter

import com.github.hauner.openapi.spring.model.Api
import com.github.hauner.openapi.spring.model.HttpMethod
import com.github.hauner.openapi.spring.support.ModelAsserts
import spock.lang.Specification

import static com.github.hauner.openapi.spring.support.OpenApiParser.parse

class ApiConverterEndpointSpec extends Specification implements ModelAsserts {

    void "creates model for an endpoint without parameters and a single response content type" () {
        def openApi = parse ("""\
openapi: 3.0.2
info:
  title: Ping API
  version: 1.0.0

paths:
  /ping:
    get:
      tags:
        - ping
      responses:
        '200':
          description: string result
          content:
            text/plain:
              schema:
                type: string
""")

        when:
        Api api = new ApiConverter ().convert (openApi)

        then:
        api.interfaces.size () == 1
        api.interfaces.get(0).endpoints.size () == 1

        and:
        def itf = api.interfaces.get (0)
        def ep = itf.endpoints.get(0)
        ep.path == '/ping'
        ep.method == HttpMethod.GET
        ep.response.contentType == 'text/plain'
        ep.response.responseType.type == 'string'
    }

}
