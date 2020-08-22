/*
 * Copyright 2019 the original authors
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

import io.openapiprocessor.core.model.datatypes.DataTypeConstraints

/**
 * OpenAPI type 'integer' with format 'int64' maps to java Long.
 *
 * @author Martin Hauner
 */
class LongDataType extends DataTypeBase {

    DataTypeConstraints constraints

    @Override
    String getName () {
        'Long'
    }

    @Override
    String getPackageName () {
        'java.lang'
    }

    @Override
    Set<String> getImports () {
        [[packageName, name].join ('.')]
    }

    @Override
    Set<String> getReferencedImports () {
        []
    }

    @Override
    DataTypeConstraints getConstraints() {
        constraints
    }

}
