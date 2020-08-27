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

package com.github.hauner.openapi.core.converter

import io.openapiprocessor.core.converter.ApiOptions
import io.openapiprocessor.core.converter.mapping.AmbiguousTypeMappingException
import io.openapiprocessor.core.converter.mapping.EndpointTypeMapping
import io.openapiprocessor.core.converter.mapping.ParameterTypeMapping
import io.openapiprocessor.core.converter.mapping.ResponseTypeMapping
import io.openapiprocessor.core.converter.mapping.TypeMapping
import io.openapiprocessor.core.framework.Framework
import com.github.hauner.openapi.core.framework.FrameworkBase
import io.openapiprocessor.core.model.Api
import spock.lang.Specification
import spock.lang.Unroll

import static com.github.hauner.openapi.core.test.OpenApiParser.parse

class DataTypeConverterArrayTypeMappingSpec extends Specification {

    @Unroll
    void "maps array schema to #responseTypeName via global type mapping" () {
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
            new TypeMapping ('array', targetTypeName)
        ])
        Api api = new ApiConverter (options, Stub (Framework))
            .convert (openApi)

        then:
        def itf = api.interfaces.first ()
        def ep = itf.endpoints.first ()
        def rsp = ep.getFirstResponse ('200')
        rsp.responseType.name == responseTypeName

        where:
        targetTypeName         | responseTypeName
        'java.util.Collection' | 'Collection<String>'
        'java.util.List'       | 'List<String>'
        'java.util.Set'        | 'Set<String>'
    }

    void "throws when there are multiple global mappings for the array type" () {
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
          name: date
          required: false
          schema:
            type: array
            items: 
              type: string
      responses:
        '204':
          description: none
""")

        when:
        def options = new ApiOptions(
            packageName: 'pkg',
            typeMappings: [
                new TypeMapping (
                    'array',
                    'java.util.Collection'),
                new TypeMapping (
                    'array',
                    'java.util.Collection')
            ])

        new ApiConverter (options, Stub (Framework))
            .convert (openApi)

        then:
        def e = thrown (AmbiguousTypeMappingException)
        e.typeMappings == options.typeMappings
    }


    @Unroll
    void "throws when there are multiple mappings on the same level: #type" () {
        def openApi = parse ("""\
openapi: 3.0.2
info:
  title: API
  version: 1.0.0

paths:
  /foo:
    get:
      parameters:
        - in: query
          name: param
          required: false
          schema:
            type: array
            items: 
              type: string
      responses:
        '204':
          description: none
""")

        when:
        def options = new ApiOptions(
            packageName: 'pkg',
            typeMappings: mappings)

        new ApiConverter (options, Stub (Framework))
            .convert (openApi)

        then:
        thrown (AmbiguousTypeMappingException)

        where:
        type << [
            'global type mappings',
            'global io mappings',
            'endpoint mappings'
        ]

        mappings << [
            [
                new TypeMapping (
                    'array',
                    'java.util.Collection'),
                new TypeMapping (
                    'array',
                    'java.util.Collection')
            ],
            [
                new ParameterTypeMapping (
                    'param', new TypeMapping (
                        'array',
                        'java.util.Collection')
                ),
                new ParameterTypeMapping (
                    'param', new TypeMapping (
                        'array',
                        'java.util.Collection')
                )
            ],
            [
                new EndpointTypeMapping ('/foo', [
                        new ParameterTypeMapping (
                            'param', new TypeMapping (
                                'array',
                                'java.util.Collection')
                        ),
                        new ParameterTypeMapping (
                            'param', new TypeMapping (
                                'array',
                                'java.util.Collection')
                        )
                    ])
            ]
        ]
    }

    void "converts array response schema to #responseTypeName via endpoint type mapping" () {
        def openApi = parse ("""\
openapi: 3.0.2
info:
  title: API
  version: 1.0.0

paths:
  /foo:
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
            new EndpointTypeMapping ('/foo', [
                new TypeMapping (
                    'array',
                    targetTypeName)
            ])
        ])

        Api api = new ApiConverter (options, Stub (Framework))
            .convert (openApi)

        then:
        def itf = api.interfaces.first ()
        def ep = itf.endpoints.first ()
        def rsp = ep.getFirstResponse ('200')
        rsp.responseType.name == responseTypeName
        rsp.responseType.packageName == 'java.util'

        where:
        targetTypeName         | responseTypeName
        'java.util.Collection' | 'Collection<String>'
        'java.util.List'       | 'List<String>'
        'java.util.Set'        | 'Set<String>'
    }

    @Unroll
    void "converts array parameter schema to java type via #type" () {
        def openApi = parse ("""\
openapi: 3.0.2
info:
  title: API
  version: 1.0.0

paths:
  /foobar:
    get:
      parameters:
        - in: query
          name: foobar
          required: false
          schema:
            type: array
            items:
              type: string
      responses:
        '204':
          description: empty
""")

        when:
        def options = new ApiOptions(packageName: 'pkg', typeMappings: mappings)

        Api api = new ApiConverter (options, new FrameworkBase ())
            .convert (openApi)

        then:
        def itf = api.interfaces.first ()
        def ep = itf.endpoints.first ()
        def p = ep.parameters.first ()
        p.dataType.name == 'Collection<String>'
        p.dataType.packageName == 'java.util'

        where:
        type << [
            'endpoint parameter mapping',
            'global parameter mapping'
        ]

        mappings << [
            [
                new EndpointTypeMapping ('/foobar', [
                    new ParameterTypeMapping (
                        'foobar', new TypeMapping (
                        'array',
                        'java.util.Collection')
                    )
                ])
            ], [
                new ParameterTypeMapping (
                    'foobar', new TypeMapping (
                        'array',
                        'java.util.Collection')
                )
            ]
        ]
    }

    @Unroll
    void "converts array response schema to Collection<> via #type" () {
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
        def options = new ApiOptions(packageName: 'pkg', typeMappings: mappings)

        Api api = new ApiConverter (options, Stub (Framework))
            .convert (openApi)

        then:
        def itf = api.interfaces.first ()
        def ep = itf.endpoints.first ()
        def rsp = ep.getFirstResponse ('200')
        rsp.responseType.name == 'Collection<String>'
        rsp.responseType.imports == ['java.util.Collection', 'java.lang.String'] as Set

        where:
        type << [
            'endpoint response mapping',
            'global response mapping',
            'endpoint response mapping over endpoint type mapping',
            'endpoint type mapping'
        ]

        mappings << [
            [
                new EndpointTypeMapping ('/array-string', [
                    new ResponseTypeMapping (
                        'application/vnd.any', new TypeMapping (
                        'array',
                        'java.util.Collection')
                    )
                ])
            ], [
                new ResponseTypeMapping (
                    'application/vnd.any', new TypeMapping (
                        'array',
                        'java.util.Collection')
                )
            ], [
                new EndpointTypeMapping ('/array-string', [
                    new ResponseTypeMapping (
                        'application/vnd.any', new TypeMapping (
                        'array',
                        'java.util.Collection')
                    ),
                    new TypeMapping (
                        'array',
                        'java.util.Collection')
                ])
            ], [
                new EndpointTypeMapping ('/array-string', [
                    new TypeMapping (
                        'array',
                        'java.util.Collection')
                ])
            ]
        ]
    }

}
