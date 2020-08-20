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

package com.github.hauner.openapi.core.processor

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.github.hauner.openapi.core.processor.mapping.Mapping
import com.github.hauner.openapi.core.processor.mapping.VersionedMapping
import com.github.hauner.openapi.core.processor.mapping.Parameter
import com.github.hauner.openapi.core.processor.mapping.ParameterDeserializer
import io.openapiprocessor.core.converter.mapping.v2.Mapping as MappingV2
import io.openapiprocessor.core.converter.mapping.v2.Parameter as ParameterV2
import io.openapiprocessor.core.converter.mapping.v2.ParameterDeserializer as ParameterDeserializerV2
import groovy.util.logging.Slf4j
import io.openapiprocessor.core.processor.mapping.version.Mapping as VersionMapping

/**
 *  Reader for mapping yaml.
 *
 *  @author Martin Hauner
 */
@Slf4j
class MappingReader {

    VersionedMapping read (String typeMappings) {
        if (typeMappings == null || typeMappings.empty) {
            return null
        }

        def mapping
        if (isUrl (typeMappings)) {
            mapping = new URL (typeMappings).text
        } else if (isFileName (typeMappings)) {
            mapping = new File (typeMappings).text
        } else {
            mapping = typeMappings
        }

        def versionMapper = createVersionParser ()
        VersionMapping version = versionMapper.readValue (mapping, VersionMapping)

        if (version.isDeprecatedVersionKey ()) {
            log.warn ('the mapping version key "openapi-processor-spring" is deprecated, please use "openapi-processor-mapping"')
        }

        if (version.v2) {
            def mapper = createV2Parser ()
            mapper.readValue (mapping, MappingV2)
        } else {
            // assume v1
            log.info ('please update the mapping to the latest format')
            log.info ('see https://docs.openapiprocessor.io/spring/mapping/structure.html')

            def mapper = createYamlParser ()
            mapper.readValue (mapping, Mapping)
        }
    }

    private ObjectMapper createV2Parser () {
        SimpleModule module = new SimpleModule ()
        module.addDeserializer (ParameterV2, new ParameterDeserializerV2 ())

        def kotlinModule = new KotlinModule.Builder()
            .nullIsSameAsDefault (true)
            .build ()

        new ObjectMapper (new YAMLFactory ())
            .configure (DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setPropertyNamingStrategy (PropertyNamingStrategy.KEBAB_CASE)
            .registerModules (kotlinModule, module)
    }

    private ObjectMapper createYamlParser () {
        SimpleModule module = new SimpleModule ()
        module.addDeserializer (Parameter, new ParameterDeserializer ())

        new ObjectMapper (new YAMLFactory ())
            .configure (DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setPropertyNamingStrategy (PropertyNamingStrategy.KEBAB_CASE)
            .registerModule (module)
    }

    private ObjectMapper createVersionParser () {
        new ObjectMapper (new YAMLFactory ())
            .configure (DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setPropertyNamingStrategy (PropertyNamingStrategy.KEBAB_CASE)
            .registerModule (new KotlinModule ())
    }

    private boolean isFileName (String name) {
        name.endsWith ('.yaml') || name.endsWith ('.yml')
    }

    private boolean isUrl (String source) {
        try {
            new URL (source)
        } catch (MalformedURLException ignore) {
            false
        }
    }

}
