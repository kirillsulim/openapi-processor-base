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

package com.github.hauner.openapi.core.processor.mapping.v2

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty
import com.github.hauner.openapi.core.processor.mapping.VersionedMapping

/**
 * *the* v2 Schema of the mapping yaml
 *
 *  @author Martin Hauner
 */
data class Mapping(

    /**
     * mapping format version
     */
    @JsonProperty("openapi-processor-mapping")
    @JsonAlias("openapi-processor-spring") // deprecated
    val version: String,

    /**
     * general options
     */
    val options: Options = Options(),

    /**
     * the type mappings
     */
    val map: Map = Map()

): VersionedMapping {

    override fun isV2(): Boolean {
        return version.startsWith("v2")
    }

}
