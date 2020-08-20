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

package com.github.hauner.openapi.core.parser.openapi4j

import io.openapiprocessor.core.parser.MediaType as ParserMediaType
import io.openapiprocessor.core.parser.Response as ParserResponse
import org.openapi4j.parser.model.v3.MediaType as O4jMediaType
import org.openapi4j.parser.model.v3.Response as O4jResponse

/**
 * openapi4j Response abstraction.
 *
 * @author Martin Hauner
 */
class Response implements ParserResponse {

    private O4jResponse response

    Response (O4jResponse response) {
        this.response = response
    }

    @Override
    Map<String, ParserMediaType> getContent () {
        def content = [:] as LinkedHashMap

        response.contentMediaTypes.each { Map.Entry<String, O4jMediaType> entry ->
            content.put (entry.key, new MediaType (entry.value))
        }

        content
    }

}
