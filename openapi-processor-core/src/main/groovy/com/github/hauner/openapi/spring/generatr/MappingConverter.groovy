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

package com.github.hauner.openapi.spring.generatr

import com.github.hauner.openapi.spring.converter.mapping.AddParameterTypeMapping
import com.github.hauner.openapi.spring.converter.mapping.EndpointTypeMapping
import com.github.hauner.openapi.spring.converter.mapping.Mapping
import com.github.hauner.openapi.spring.converter.mapping.ParameterTypeMapping
import com.github.hauner.openapi.spring.converter.mapping.ResponseTypeMapping
import com.github.hauner.openapi.spring.converter.mapping.TypeMapping
import com.github.hauner.openapi.spring.generatr.mapping.AdditionalParameter
import com.github.hauner.openapi.spring.generatr.mapping.Mapping as YamlMapping
import com.github.hauner.openapi.spring.generatr.mapping.Parameter
import com.github.hauner.openapi.spring.generatr.mapping.Path
import com.github.hauner.openapi.spring.generatr.mapping.RequestParameter
import com.github.hauner.openapi.spring.generatr.mapping.Response
import com.github.hauner.openapi.spring.generatr.mapping.Type

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 *  Converter for the type mapping from the mapping yaml. It converts the type mapping information
 *  into the format used by {@link com.github.hauner.openapi.spring.converter.DataTypeConverter}.
 *
 *  @author Martin Hauner
 */
class MappingConverter {
    private Pattern GENERIC_INLINE = ~/(.+?)<(.+?)>/

    List<Mapping> convert (YamlMapping source) {
        def result = new ArrayList<Mapping>()

        source?.map?.types?.each {
            result.add (convertType (it))
        }

        source?.map?.parameters?.each {
            result.add (convertParameter (it))
        }

        source?.map?.responses?.each {
            result.add (convertResponse (it))
        }

        source?.map?.paths?.each {
            result.add(convertPath (it.key, it.value))
        }

        result
    }

    private TypeMapping convertType (Type type) {
        Matcher matcher = type.to =~ GENERIC_INLINE

        def (from, format) = type.from ? (type.from as String).tokenize (':') : [null,  null]
        String to = type.to
        List<String> generics = []

        // has inline generics
        if (matcher.find ()) {
            to = matcher.group (1)
            generics = matcher
                .group (2)
                .split (',')
                .collect { it.trim () }

            // has explicit generic list
        } else if (type.generics) {
            generics = type.generics
        }

        new TypeMapping (
            sourceTypeName: from, sourceTypeFormat: format,
            targetTypeName: to, genericTypeNames: generics)
    }

    private Mapping convertParameter (Parameter source) {
        if (source instanceof RequestParameter) {
            def name = source.name
            def mapping = convertType (new Type(
                from: null,
                to: source.to,
                generics: source.generics
            ))
            new ParameterTypeMapping (parameterName: name, mapping: mapping)

        } else if (source instanceof  AdditionalParameter) {
            def name = source.add
            def mapping = convertType (new Type(
                from: null,
                to: source.to,
                generics: source.generics
            ))
            new AddParameterTypeMapping (parameterName: name, mapping: mapping)

        } else {
            throw new Exception("unknown parameter mapping $source")
        }
    }

    private ResponseTypeMapping convertResponse (Response source) {
        def content = source.content
        def mapping = convertType (new Type(
            from: null,
            to: source.to,
            generics: source.generics
        ))
        new ResponseTypeMapping(contentType: content, mapping: mapping)
    }

    private EndpointTypeMapping convertPath (String path, Path source) {
        def result = new ArrayList<Mapping>()

        source.types.each {
            result.add (convertType (it))
        }

        source.parameters.each {
            result.add (convertParameter (it))
        }

        source.responses.each {
            result.add (convertResponse (it))
        }

        new EndpointTypeMapping(path: path, exclude: source.exclude, typeMappings: result)
    }

}
