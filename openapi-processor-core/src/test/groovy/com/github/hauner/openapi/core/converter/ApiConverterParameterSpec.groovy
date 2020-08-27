/*
 * Copyright 2019-2020 the original authors
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

package com.github.hauner.openapi.core.converter

import io.openapiprocessor.core.converter.ApiOptions
import io.openapiprocessor.core.converter.mapping.AddParameterTypeMapping
import io.openapiprocessor.core.converter.mapping.EndpointTypeMapping
import io.openapiprocessor.core.converter.mapping.TypeMapping
import io.openapiprocessor.core.framework.FrameworkBase
import io.openapiprocessor.core.converter.mapping.UnknownParameterTypeException
import spock.lang.Ignore
import spock.lang.Specification

import static com.github.hauner.openapi.core.test.OpenApiParser.parse

class ApiConverterParameterSpec extends Specification {

    void "converts simple query parameter"() {
        def openApi = parse (
"""\
openapi: 3.0.2
info:
  title: test simple query parameter
  version: 1.0.0

paths:
  /endpoint:

    get:
      tags:
        - endpoint
      parameters:
        - name: foo
          description: query, required, string
          in: query
          required: true
          schema:
            type: string
      responses:
        '204':
          description: empty
""")

        when:
        def api = new ApiConverter (new ApiOptions(), new FrameworkBase ())
            .convert (openApi)

        then:
        def itf = api.interfaces.first ()
        def ep = itf.endpoints.first ()
        def param = ep.parameters.first ()
        param.name == 'foo'
        param.required
        param.dataType.name == 'String'
    }

    void "converts simple path parameter"() {
        def openApi = parse (
"""\
openapi: 3.0.2
info:
  title: test simple path parameter
  version: 1.0.0

paths:
  /endpoint/{foo}:

    get:
      tags:
        - endpoint
      parameters:
        - name: foo
          description: path, required, string
          in: path
          required: true
          schema:
            type: string
      responses:
        '204':
          description: empty
""")

        when:
        def api = new ApiConverter (new ApiOptions(), new FrameworkBase ())
            .convert (openApi)

        then:
        def itf = api.interfaces.first ()
        def ep = itf.endpoints.first ()
        def param = ep.parameters.first ()
        param.name == 'foo'
        param.required
        param.dataType.name == 'String'
    }

    void "converts simple header parameter"() {
        def openApi = parse (
"""\
openapi: 3.0.2
info:
  title: test simple header parameter
  version: 1.0.0

paths:
  /endpoint:

    get:
      tags:
        - endpoint
      parameters:
        - name: x-foo
          description: header, required, string
          in: header
          required: true
          schema:
            type: string
      responses:
        '204':
          description: empty
""")

        when:
        def api = new ApiConverter (new ApiOptions(), new FrameworkBase ())
            .convert (openApi)

        then:
        def itf = api.interfaces.first ()
        def ep = itf.endpoints.first ()
        def param = ep.parameters.first ()
        param.name == 'x-foo'
        param.required
        param.dataType.name == 'String'
    }

    void "converts simple cookie parameter"() {
        def openApi = parse (
"""\
openapi: 3.0.2
info:
  title: test simple cookie parameter
  version: 1.0.0

paths:
  /endpoint:

    get:
      tags:
        - endpoint
      parameters:
        - name: foo
          description: cookie, required, string
          in: cookie
          required: true
          schema:
            type: string
      responses:
        '204':
          description: empty
""")

        when:
        def api = new ApiConverter (new ApiOptions(), new FrameworkBase ())
            .convert (openApi)

        then:
        def itf = api.interfaces.first ()
        def ep = itf.endpoints.first ()
        def param = ep.parameters.first ()
        param.name == 'foo'
        param.required
        param.dataType.name == 'String'
    }

    void "adds additional request parameter from endpoint mapping" () {
        def openApi = parse (
"""\
openapi: 3.0.2
info:
  title: test additional parameters
  version: 1.0.0

paths:
  /foo:
    get:
      parameters:
        - name: foo
          description: query, required
          in: query
          required: true
          schema:
            type: string
      responses:
        '204':
          description: empty
"""
        )

        def options = new ApiOptions(packageName: 'pkg', typeMappings: [
            new EndpointTypeMapping('/foo', [
                new AddParameterTypeMapping (
                    'request', new TypeMapping (
                        null,
                        'javax.servlet.http.HttpServletRequest')
                )
            ])
        ])

        when:
        def api = new ApiConverter (options, new FrameworkBase ())
            .convert (openApi)

        then:
        def itf = api.interfaces.first ()
        def ep = itf.endpoints.first ()
        def foo = ep.parameters[0]
        def request = ep.parameters[1]

        foo.name == 'foo'
        request.name == 'request'
        request.required
        request.dataType.name == 'HttpServletRequest'
        request.dataType.packageName == 'javax.servlet.http'
        !request.withAnnotation
    }

    @Ignore("the openapi parser ignores parameters with unknown types")
    void "throws on unknown parameter"() {
        def openApi = parse (
"""\
openapi: 3.0.2
info:
  title: test unknown parameter type
  version: 1.0.0

paths:
  /endpoint:

    get:
      tags:
        - endpoint
      parameters:
        - name: foo
          description: unknown, required, string
          in: unknown
          schema:
            type: string
      responses:
        '204':
          description: empty
""")

        when:
        new ApiConverter ().convert (openApi)

        then:
        def e = thrown (UnknownParameterTypeException)
        e.name == 'foo'
        e.type == 'unknown'
    }

    void "converts deprecated parameter"() {
        def openApi = parse (
"""\
openapi: 3.0.2
info:
  title: test deprecated parameter
  version: 1.0.0

paths:
  /endpoint:

    get:
      parameters:
        - name: foo
          description: deprecated parameter
          in: query
          deprecated: true
          schema:
            type: string
      responses:
        '204':
          description: empty
""")

        when:
        def api = new ApiConverter (new ApiOptions(), new FrameworkBase ())
            .convert (openApi)

        then:
        def itf = api.interfaces.first ()
        def ep = itf.endpoints.first ()
        def param = ep.parameters.first ()
        param.deprecated
    }

}
