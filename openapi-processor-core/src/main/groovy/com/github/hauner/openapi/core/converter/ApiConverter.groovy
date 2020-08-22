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

import com.github.hauner.openapi.core.converter.mapping.MappingFinder
import com.github.hauner.openapi.core.converter.wrapper.MultiDataTypeWrapper
import com.github.hauner.openapi.core.converter.wrapper.ResultDataTypeWrapper
import com.github.hauner.openapi.core.converter.wrapper.SingleDataTypeWrapper
import com.github.hauner.openapi.core.framework.Framework
import com.github.hauner.openapi.core.model.parameters.Parameter as ModelParameter
import io.openapiprocessor.core.converter.mapping.AddParameterTypeMapping
import io.openapiprocessor.core.converter.mapping.Mapping
import io.openapiprocessor.core.converter.mapping.TargetType
import io.openapiprocessor.core.converter.mapping.TypeMapping
import com.github.hauner.openapi.core.model.Api
import com.github.hauner.openapi.core.model.DataTypes
import com.github.hauner.openapi.core.model.Endpoint
import com.github.hauner.openapi.core.model.Interface
import com.github.hauner.openapi.core.model.RequestBody as ModelRequestBody
import com.github.hauner.openapi.core.model.datatypes.MappedDataType
import com.github.hauner.openapi.core.model.datatypes.NoneDataType
import com.github.hauner.openapi.core.model.datatypes.ObjectDataType
import com.github.hauner.openapi.core.model.Response as ModelResponse
import io.openapiprocessor.core.model.datatypes.DataType
import io.openapiprocessor.core.parser.OpenApi
import groovy.util.logging.Slf4j
import io.openapiprocessor.core.converter.mapping.UnknownDataTypeException
import io.openapiprocessor.core.converter.mapping.UnknownParameterTypeException
import io.openapiprocessor.core.parser.MediaType
import io.openapiprocessor.core.parser.Operation
import io.openapiprocessor.core.parser.Parameter
import io.openapiprocessor.core.parser.Path
import io.openapiprocessor.core.parser.RefResolver
import io.openapiprocessor.core.parser.RequestBody
import io.openapiprocessor.core.parser.Response
import io.openapiprocessor.core.parser.Schema
import io.openapiprocessor.core.support.Identifier

/**
 * Converts the open api model to a new model that is better suited for generating source files
 * from the open api specification.
 *
 * @author Martin Hauner
 */
@Slf4j
class  ApiConverter {
    public static final String MULTIPART = "multipart/form-data"
    public static final String INTERFACE_DEFAULT_NAME = ''

    Framework framework

    private DataTypeConverter dataTypeConverter
    private ResultDataTypeWrapper dataTypeWrapper
    private SingleDataTypeWrapper singleDataTypeWrapper
    private MultiDataTypeWrapper multiDataTypeWrapper
    private MappingFinder mappingFinder
    private ApiOptions options

    ApiConverter(ApiOptions options, Framework framework) {
        this.options = options
        this.framework = framework

//        if (!this.options) {
//            this.options = new DefaultApiOptions()
//        }

        dataTypeConverter = new DataTypeConverter(this.options)
        dataTypeWrapper = new ResultDataTypeWrapper(this.options)
        singleDataTypeWrapper = new SingleDataTypeWrapper(this.options)
        multiDataTypeWrapper = new MultiDataTypeWrapper(this.options)
        mappingFinder = new MappingFinder (typeMappings: this.options.typeMappings)
    }

    /**
     * converts the openapi model to the source generation model
     *
     * @param api the open api model
     * @return source generation model
     */
    Api convert (OpenApi api) {
        def target = new Api ()
        createInterfaces (api, target)
        target
    }

    private void createInterfaces (OpenApi api, Api target) {
        Map<String, Interface> interfaces = new HashMap<> ()

        api.paths.each { Map.Entry<String, Path> pathEntry ->
            String path = pathEntry.key
            Path pathValue = pathEntry.value

            def operations = pathValue.operations
            operations.each { Operation op ->
                Interface itf = createInterface (path, op, interfaces)

                Endpoint ep = createEndpoint (path, op, target.models, api.refResolver)
                if (ep) {
                    itf.endpoints.add (ep)
                }
            }
        }

        target.interfaces = interfaces.values () as List<Interface>
    }

    private Interface createInterface (String path, Operation operation, Map<String, Interface> interfaces) {
        def targetInterfaceName = getInterfaceName (operation, isExcluded (path))

        def itf = interfaces.get (targetInterfaceName)
        if (itf) {
            return itf
        }

        itf = new Interface (
            pkg: [options.packageName, 'api'].join ('.'),
            name: targetInterfaceName
        )

        interfaces.put (targetInterfaceName, itf)
        itf
    }

    private Endpoint createEndpoint (String path, Operation operation, DataTypes dataTypes, RefResolver resolver) {
        Endpoint ep = new Endpoint (
            path: path, method: operation.method,
            operationId: operation.operationId,
            deprecated: operation.deprecated)

        try {
            collectParameters (operation.parameters, ep, dataTypes, resolver)
            collectRequestBody (operation.requestBody, ep, dataTypes, resolver)
            collectResponses (operation.responses, ep, dataTypes, resolver)
            ep.initEndpointResponses ()

        } catch (UnknownDataTypeException e) {
            log.error ("failed to parse endpoint {} {} because of: '{}'", ep.path, ep.method, e.message, e)
            null
        }
    }

    private void collectParameters (List<Parameter> parameters, Endpoint ep, DataTypes dataTypes, RefResolver resolver) {
        parameters.each { Parameter parameter ->
            ep.parameters.add (createParameter (ep.path, parameter, dataTypes, resolver))
        }

        List<Mapping> addMappings = mappingFinder.findAdditionalEndpointParameter (ep.path)
        addMappings.each {
            ep.parameters.add (createAdditionalParameter (ep.path, it as AddParameterTypeMapping, dataTypes, resolver))
        }
    }

    private void collectRequestBody (RequestBody requestBody, Endpoint ep, DataTypes dataTypes, RefResolver resolver) {
        if (requestBody == null) {
            return
        }

        requestBody.content.each { Map.Entry<String, MediaType> requestBodyEntry ->
            def contentType = requestBodyEntry.key
            def mediaType = requestBodyEntry.value

            def info = new SchemaInfo (
                path: ep.path,
                name: getInlineRequestBodyName (ep.path),
                schema: mediaType.schema,
                resolver: resolver)

            if (contentType == MULTIPART) {
                ep.parameters.addAll (createMultipartParameter (info, requestBody.required))
            } else {
                ep.requestBodies.add (createRequestBody (contentType, info, requestBody.required, dataTypes))
            }
        }
    }

    private collectResponses (Map<String, Response> responses, Endpoint ep, DataTypes dataTypes, RefResolver resolver) {
        responses.each { Map.Entry<String, Response> responseEntry ->
            def httpStatus = responseEntry.key
            def httpResponse = responseEntry.value

            List<ModelResponse> results = createResponses (
                ep.path,
                httpStatus,
                httpResponse,
                dataTypes,
                resolver)

            ep.addResponses (httpStatus, results)
        }

    }

    private ModelParameter createParameter (String path, Parameter parameter, DataTypes dataTypes, RefResolver resolver) {
        def info = new SchemaInfo (
            path: path,
            name: parameter.name,
            schema: parameter.schema,
            resolver: resolver)

        DataType dataType = dataTypeConverter.convert (info, dataTypes)

        switch (parameter.in) {
            case 'query':
                return framework.createQueryParameter (parameter, dataType)
            case 'path':
                return framework.createPathParameter (parameter, dataType)
            case 'header':
                return framework.createHeaderParameter (parameter, dataType)
            case 'cookie':
                return framework.createCookieParameter (parameter, dataType)
            default:
                // should not reach this, the openapi parser ignores parameters with unknown type.
                throw new UnknownParameterTypeException(parameter.name, parameter.in)
        }
    }

    private ModelParameter createAdditionalParameter (String path, AddParameterTypeMapping mapping, DataTypes dataTypes, RefResolver resolver) {
        TypeMapping tm = mapping.childMappings.first ()
        TargetType tt = tm.targetType

        def addType = new MappedDataType (
            type: tt.name,
            pkg: tt.pkg,
            genericTypes: tt.genericNames
        )

        def parameter = new Parameter () {

            String getIn () {
                'add'
            }

            String getName () {
                mapping.parameterName
            }

            Schema getSchema () {
                null
            }

            boolean isRequired () {
                true
            }

            boolean isDeprecated () {
                false
            }
        }

        framework.createAdditionalParameter (parameter, addType)
    }

    private ModelRequestBody createRequestBody (String contentType, SchemaInfo info, boolean required, DataTypes dataTypes) {
        DataType dataType = dataTypeConverter.convert (info, dataTypes)
        DataType changedType
        if (!info.isArray ()) {
            changedType = singleDataTypeWrapper.wrap (dataType, info)
        } else {
            changedType = multiDataTypeWrapper.wrap (dataType, info)
        }

        framework.createRequestBody(contentType, required, changedType)
    }

    private Collection<ModelParameter> createMultipartParameter (SchemaInfo info, boolean required) {
        DataType dataType = dataTypeConverter.convert (info, new DataTypes())
        if (! (dataType instanceof ObjectDataType)) {
            throw new MultipartResponseBodyException(info.path)
        }

        dataType.getObjectProperties ().collect {
            def parameter = new Parameter () {

                String getIn () {
                    'multipart'
                }

                String getName () {
                    it.key
                }

                Schema getSchema () {
                    null
                }

                boolean isRequired () {
                    true
                }

                boolean isDeprecated () {
                    false
                }
            }

            framework.createMultipartParameter (parameter, it.value)
        }
    }

    private List<ModelResponse> createResponses (String path, String httpStatus, Response response, DataTypes dataTypes, RefResolver resolver) {
        if (!response.content) {
            def info = new SchemaInfo (path: path)

            DataType dataType = new NoneDataType()
            DataType singleDataType = singleDataTypeWrapper.wrap (dataType, info)
            DataType resultDataType = dataTypeWrapper.wrap (singleDataType, info)

            def resp = new ModelResponse (
                responseType: resultDataType)

            return [resp]
        }

        def responses = []
        response.content.each { Map.Entry<String, MediaType> contentEntry ->
            def contentType = contentEntry.key
            def mediaType = contentEntry.value
            def schema = mediaType.schema

            def info = new SchemaInfo (
                path: path,
                contentType: contentType,
                name: getInlineResponseName (path, httpStatus),
                schema: schema,
                resolver: resolver)

            DataType dataType = dataTypeConverter.convert (info, dataTypes)
            DataType changedType
            if (!info.isArray ()) { // todo fails if ref
                changedType = singleDataTypeWrapper.wrap (dataType, info)
            } else {
                changedType = multiDataTypeWrapper.wrap (dataType, info)
            }
            DataType resultDataType = dataTypeWrapper.wrap (changedType, info)

            def resp = new ModelResponse (
                contentType: contentType,
                responseType: resultDataType)

            responses.add (resp)
        }

        responses
    }

    private String getInlineRequestBodyName (String path) {
        Identifier.toClass (path) + 'RequestBody'
    }

    private String getInlineResponseName (String path, String httpStatus) {
        Identifier.toClass (path) + 'Response' + httpStatus
    }

    private boolean isExcluded (String path) {
        mappingFinder.isExcludedEndpoint (path)
    }

    private String getInterfaceName (def op, boolean excluded) {
        String targetInterfaceName = INTERFACE_DEFAULT_NAME

        if ((op.hasTags())) {
            targetInterfaceName = op.firstTag
        }

        if (excluded) {
            targetInterfaceName += 'Excluded'
        }

        targetInterfaceName
    }

}
