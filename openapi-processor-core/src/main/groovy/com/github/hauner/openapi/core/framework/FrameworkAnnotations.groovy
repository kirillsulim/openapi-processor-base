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

package com.github.hauner.openapi.core.framework

import io.openapiprocessor.core.model.parameters.Parameter
import io.openapiprocessor.core.model.HttpMethod

/**
 * provides annotation details of the framework.
 *
 * @author Martin Hauner
 */
interface FrameworkAnnotations {

    /**
     * provides the details of the requested mapping annotation.
     *
     * @param httpMethod requested http method
     * @return annotation details
     */
    FrameworkAnnotation getAnnotation (HttpMethod httpMethod)

    /**
     * provides the details of the requested method parameter annotation.
     *
     * @param parameter requested parameter
     * @return annotation details
     */
    FrameworkAnnotation getAnnotation (Parameter parameter)

}
