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

package com.github.hauner.openapi.core.model.datatypes

import io.openapiprocessor.core.model.datatypes.DataType

/**
 * OpenAPI no type.
 *
 * @author Martin Hauner
 */
class NoneDataType extends DataTypeBase {

    private String type = 'void'

    @Override
    String getName () {
        type
    }

    @Override
    String getPackageName () {
        null
    }

    @Override
    Set<String> getImports () {
        []
    }

    @Override
    Set<String> getReferencedImports () {
        []
    }

    /**
     * void response must be Void when it is wrapped in a generic type.
     *
     * @return
     */
    DataType wrappedInResult () {
        new NoneDataType(type: type.capitalize ())
    }

}
