/*
 * Copyright 2019 the original authors
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

import com.github.hauner.openapi.spring.generatr.ApiOptions
import com.github.hauner.openapi.spring.generatr.mapping.EndpointTypeMapping
import com.github.hauner.openapi.spring.generatr.mapping.ResponseTypeMapping
import com.github.hauner.openapi.spring.generatr.mapping.TypeMapping
import com.github.hauner.openapi.spring.model.Api
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll

import static com.github.hauner.openapi.spring.support.OpenApiParser.parse


class DataTypeConverterTypeMappingSpec extends Specification {

    @Unroll
    void "converts simple array schema to #responseTypeName set via global array mapping" () {
        def openApi = parse ("""\
openapi: 3.0.2
info:
  title: API
  version: 1.0.0

paths:
  /array-string:
    get:
      responses:
        '200':
          content:
            application/vnd.any:
              schema:
                type: array
                items:
                  type: string
          description: none              
""")
        when:
        def options = new ApiOptions(packageName: 'pkg', typeMappings: [
            new TypeMapping (sourceTypeName: 'array', targetTypeName: targetTypeName)
        ])
        Api api = new ApiConverter (options).convert (openApi)

        then:
        def itf = api.interfaces.first ()
        def ep = itf.endpoints.first ()
        ep.response.responseType.name == responseTypeName

        where:
        targetTypeName         | responseTypeName
        'java.util.Collection' | 'Collection<String>'
        'java.util.List'       | 'List<String>'
        'java.util.Set'        | 'Set<String>'
    }

    void "converts simple array response schema to Collection<> via endpoint response type array mapping" () {
        def openApi = parse ("""\
openapi: 3.0.2
info:
  title: API
  version: 1.0.0

paths:
  /array-string:
    get:
      responses:
        '200':
          content:
            application/vnd.any:
              schema:
                type: array
                items:
                  type: string
          description: none              
""")

        when:
        def options = new ApiOptions(
            packageName: 'pkg',
            typeMappings: [
                new EndpointTypeMapping (path: '/array-string',
                    typeMappings: [
                        new ResponseTypeMapping (contentType: 'application/vnd.any', sourceTypeName: 'object', targetTypeName: 'pkg.TargetClass'),
                        new ResponseTypeMapping (contentType: 'application/vnd.any', sourceTypeName: 'array', targetTypeName: 'java.util.Collection')
                    ])
                ])
        Api api = new ApiConverter (options).convert (openApi)

        then:
        def itf = api.interfaces.first ()
        def ep = itf.endpoints.first ()
        ep.response.responseType.name == 'Collection<String>'
    }

    void "converts simple array response schema to Collection<> via global response type array mapping" () {
        def openApi = parse ("""\
openapi: 3.0.2
info:
  title: API
  version: 1.0.0

paths:
  /array-string:
    get:
      responses:
        '200':
          content:
            application/vnd.any:
              schema:
                type: array
                items:
                  type: string
          description: none              
""")

        when:
        def options = new ApiOptions(
            packageName: 'pkg',
            typeMappings: [
                new ResponseTypeMapping (contentType: 'application/vnd.any', sourceTypeName: 'array', targetTypeName: 'java.util.Collection')
            ])
        Api api = new ApiConverter (options).convert (openApi)

        then:
        def itf = api.interfaces.first ()
        def ep = itf.endpoints.first ()
        ep.response.responseType.name == 'Collection<String>'
    }

    void "converts simple object response schema to java type via endpoint mapping" () {
        def openApi = parse ("""\
openapi: 3.0.2
info:
  title: API
  version: 1.0.0

paths:
  /object:
    get:
      responses:
        '200':
          content:
            application/vnd.any:
              schema:
                type: object
                properties:
                  prop:
                    type: string
          description: none              
""")

        when:
        def options = new ApiOptions(
            packageName: 'pkg',
            typeMappings: [
                new EndpointTypeMapping (path: '/object',
                    typeMappings: [
                         new ResponseTypeMapping (contentType: 'application/vnd.any', sourceTypeName: 'object', targetTypeName: 'pkg.TargetClass')
                    ])
                ])
        Api api = new ApiConverter (options).convert (openApi)

        then:
        def itf = api.interfaces.first ()
        def ep = itf.endpoints.first ()
        ep.response.responseType.name == 'TargetClass'
    }

    void "converts named schemas to java type via global type mapping" () {
        def openApi = parse ("""\
openapi: 3.0.2
info:
  title: API
  version: 1.0.0

paths:
  /page:
    get:
      parameters:
        - in: query
          name: pageable
          required: false
          schema:
            \$ref: '#/components/schemas/Pageable'
      responses:
        '200':
          description: none
          content:
            application/json:
              schema:
                \$ref: '#/components/schemas/StringPage'

components:
  schemas:

    Pageable:
      description: minimal Pageable query parameters
      type: object
      properties:
        page:
          type: integer
        size:
          type: integer

    Page:
      description: minimal Page response without content property
      type: object
      properties:
        number:
          type: integer
        size:
          type: integer

    StringContent:
      description: specific content List of the Page response
      type: object
      properties:
        content:
          type: array
          items:
            type: string

    StringPage:
      description: typed Page
      type: object
      allOf:
        - \$ref: '#/components/schemas/Page'
        - \$ref: '#/components/schemas/StringContent'
""")

        when:
        def options = new ApiOptions(
            packageName: 'pkg',
            typeMappings: [
                new TypeMapping (
                    sourceTypeName: 'Pageable',
                    targetTypeName: 'org.springframework.data.domain.Pageable'),
                new TypeMapping (
                    sourceTypeName: 'StringPage',
                    targetTypeName: 'org.springframework.data.domain.Page<String>')
            ])
        Api api = new ApiConverter (options).convert (openApi)

        then:
        def itf = api.interfaces.first ()
        def ep = itf.endpoints.first ()
        def parameter = ep.parameters.first ()
        def response = ep.response
        parameter.dataType.pkg == 'org.springframework.data.domain'
        parameter.dataType.name == 'Pageable'
        response.responseType.pkg == 'org.springframework.data.domain'
        response.responseType.name == 'Page<String>'
    }

}
