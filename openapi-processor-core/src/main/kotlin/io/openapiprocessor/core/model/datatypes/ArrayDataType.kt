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

package io.openapiprocessor.core.model.datatypes

/**
 * OpenAPI type 'array' maps to java [].
 *
 * @author Martin Hauner
 * @author Bastian Wilhelm
 */
class ArrayDataType(
    override val item: DataType,
    override val constraints: DataTypeConstraints? = null,
    override val deprecated: Boolean = false
): DataType, CollectionDataType {

    override fun getName(): String {
        return "${item.getName()}[]"
    }

    override fun getPackageName(): String {
        return item.getPackageName()
    }

    override fun getImports(): Set<String> {
        return item.getImports()
    }

    override val referencedImports: Set<String>
        get() = item.referencedImports

}
