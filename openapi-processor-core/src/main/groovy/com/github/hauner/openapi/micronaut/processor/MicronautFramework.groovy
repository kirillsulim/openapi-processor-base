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

package com.github.hauner.openapi.micronaut.processor

import com.github.hauner.openapi.core.framework.FrameworkBase
import com.github.hauner.openapi.core.model.datatypes.DataType
import com.github.hauner.openapi.core.model.parameters.Parameter
import com.github.hauner.openapi.micronaut.model.parameters.QueryParameter
import com.github.hauner.openapi.core.parser.Parameter as ParserParameter

/**
 * Micronaut model factory.
 *
 * @author Martin Hauner
 */
class MicronautFramework extends FrameworkBase {

    @Override
        Parameter createQueryParameter (ParserParameter parameter, DataType dataType) {
        new QueryParameter (
            name: parameter.name,
            required: parameter.required,
            dataType: dataType)
    }

}
