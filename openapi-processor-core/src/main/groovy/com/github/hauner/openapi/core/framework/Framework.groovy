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

import com.github.hauner.openapi.core.model.RequestBody
import com.github.hauner.openapi.core.model.datatypes.AnnotationDataType
import com.github.hauner.openapi.core.model.parameters.Parameter
import com.github.hauner.openapi.core.model.datatypes.DataType
import com.github.hauner.openapi.core.parser.Parameter as ParserParameter

/**
 * factory for framework model objects.
 */
interface Framework {

    /**
     * create a model query parameter.
     *
     * @param parameter an OpenAPI query parameter
     * @param dataType data type of the parameter
     * @return a query {@link Parameter}
     */
    Parameter createQueryParameter(ParserParameter parameter, DataType dataType)

    /**
     * create a model header parameter.
     *
     * @param parameter an OpenAPI header parameter
     * @param dataType data type of the parameter
     * @return a header {@link Parameter}
     */
    Parameter createHeaderParameter(ParserParameter parameter, DataType dataType)

    /**
     * create a model cookie parameter.
     *
     * @param parameter an OpenAPI cookie parameter
     * @param dataType data type of the parameter
     * @return a cookie {@link Parameter}
     */
    Parameter createCookieParameter(ParserParameter parameter, DataType dataType)

    /**
     * create a model path parameter.
     *
     * @param parameter an OpenAPI path parameter
     * @param dataType data type of the parameter
     * @return a path {@link Parameter}
     */
    Parameter createPathParameter(ParserParameter parameter, DataType dataType)

    /**
     * create a model multipart parameter.
     *
     * @param parameter an OpenAPI multipart parameter
     * @param dataType data type of the parameter
     * @return a multipart {@link Parameter}
     */
    Parameter createMultipartParameter(ParserParameter parameter, DataType dataType)

    /**
     * create a model additional parameter.
     *
     * @param parameter an OpenAPI additional parameter
     * @param dataType data type of the parameter
     * @return an additional {@link Parameter}
     */
    Parameter createAdditionalParameter(ParserParameter parameter, DataType dataType,
        AnnotationDataType annotationDataType)

    /**
     * create a model request body.
     *
     * @param contentType an OpenAPI request body content type
     * @param required the request body is required
     * @param dataType data type of the request body
     * @return an additional {@link RequestBody}
     */
    RequestBody createRequestBody(String contentType, boolean required, DataType dataType)

}
