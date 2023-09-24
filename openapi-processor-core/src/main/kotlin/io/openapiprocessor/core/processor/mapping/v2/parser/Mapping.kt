/*
 * Copyright 2022 https://github.com/openapi-processor/openapi-processor-core
 * PDX-License-Identifier: Apache-2.0
 */
package io.openapiprocessor.core.processor.mapping.v2.parser

import io.openapiprocessor.core.converter.mapping.ParameterValue

class MappingType (val targetType: String, val targetGenericTypes: List<MappingType>) {}

interface Mapping {
    enum class Kind {
        TYPE, MAP, ANNOTATE
    }

    val kind: Kind?
    val sourceType: String?
    val sourceFormat: String?
    val targetType: String?
    val targetTypePrimitive: Boolean
    val targetTypePrimitiveArray: Boolean
    val targetGenericTypes: List<String>
    val targetGenericTypes2: List<MappingType>
    val annotationType: String?
    val annotationParameters: LinkedHashMap<String, ParameterValue>  // preserves order
}
