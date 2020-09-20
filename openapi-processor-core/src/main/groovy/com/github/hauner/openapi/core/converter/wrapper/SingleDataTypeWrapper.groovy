/*
 * Copyright 2020 the original authors
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

package com.github.hauner.openapi.core.converter.wrapper

import io.openapiprocessor.core.converter.ApiOptions
import com.github.hauner.openapi.core.converter.mapping.MappingFinder
import io.openapiprocessor.core.converter.SchemaInfo
import io.openapiprocessor.core.converter.mapping.AmbiguousTypeMappingException
import io.openapiprocessor.core.converter.mapping.Mapping
import io.openapiprocessor.core.converter.mapping.TargetType
import io.openapiprocessor.core.converter.mapping.TargetTypeMapping
import io.openapiprocessor.core.model.datatypes.DataType
import io.openapiprocessor.core.model.datatypes.NoneDataType
import io.openapiprocessor.core.model.datatypes.SingleDataType

/**
 * wraps the data type with the 'singe' data mapping.
 *
 * Used to wrap Responses or RequestBody's with {@code Mono<>} or similar types.
 *
 * @author Martin Hauner
 */
class SingleDataTypeWrapper {

    private ApiOptions options
    private MappingFinder finder

    SingleDataTypeWrapper (ApiOptions options) {
        this.options = options
        this.finder = new MappingFinder(typeMappings: options.typeMappings)
    }

    /**
     * wraps a (converted) non-array data type with the configured single data type like
     * {@code Mono<>} etc.
     *
     * If the configuration for the single type is 'plain' or not defined the source data type
     * is not wrapped.
     *
     * @param dataType the data type to wrap
     * @param schemaInfo the open api type with context information
     * @return the resulting java data type
     */
    DataType wrap (DataType dataType, SchemaInfo schemaInfo) {
        def targetType = getSingleResultDataType (schemaInfo)
        if (!targetType || schemaInfo.isArray ()) {
            return dataType
        }

        if (targetType.typeName == 'plain') {
            return dataType
        }

        def wrappedType = new SingleDataType (
            targetType.name,
            targetType.pkg,
            checkNone (dataType)
        )

        return wrappedType
    }

    private TargetType getSingleResultDataType (SchemaInfo info) {
        // check endpoint single mapping
        List<Mapping> endpointMatches = finder.findEndpointSingleMapping (info)

        if (!endpointMatches.empty) {

            if (endpointMatches.size () != 1) {
                throw new AmbiguousTypeMappingException (endpointMatches)
            }

            TargetType target = (endpointMatches.first() as TargetTypeMapping).targetType
            if (target) {
                return target
            }
        }

        // find global single mapping
        List<Mapping> typeMatches = finder.findSingleMapping (info)
        if (typeMatches.isEmpty ()) {
            return null
        }

        if (typeMatches.size () != 1) {
            throw new AmbiguousTypeMappingException (typeMatches)
        }

        def match = typeMatches.first () as TargetTypeMapping
        return match.targetType
    }

    private DataType checkNone (DataType dataType) {
        if (dataType instanceof NoneDataType) {
            return dataType.wrappedInResult ()
        }

        dataType
    }

}
