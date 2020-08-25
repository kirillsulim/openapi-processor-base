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

package com.github.hauner.openapi.core.model

import io.openapiprocessor.core.model.datatypes.DataType
import io.openapiprocessor.core.model.datatypes.MappedDataType
import com.github.hauner.openapi.core.model.datatypes.ObjectDataType
import io.openapiprocessor.core.model.datatypes.StringEnumDataType

/**
 * Container for Java data types from OpenAPI '#/component/schemas'.
 *
 * @author Martin Hauner
 */
class DataTypes {

    private Map<String, DataType> types = [:]
    private Map<String, MappedDataType> mappedTypes = [:]

    /**
     * provides all named data types (including simple data types) used by the api endpoint.
     *
     * @return list of object data types
     */
    List<DataType> getDataTypes () {
        types.values () as List<DataType>
    }

    /**
     * provides the object data types (model classes) used by the api endpoints. For this object
     * the generatr will create POJOs classes.
     *
     * @return list of object data types
     */
    List<ObjectDataType> getObjectDataTypes () {
        types.values ().findAll {
          it instanceof ObjectDataType
        } as List<ObjectDataType>
    }

    /**
     * provides the enum data types (model classes) used by the api endpoints. For this object
     * the generatr will create enum classes.
     *
     * @return list of enum data types
     */
    List<StringEnumDataType> getEnumDataTypes () {
        types.values ().findAll {
          it instanceof StringEnumDataType
        } as List<StringEnumDataType>
    }

    void add (List<DataType> dataTypes) {
        dataTypes.each {
            types.put (it.name, it)
        }
    }

    /**
     * store data type.
     *
     * @param dataType the source data type
     */
    void add (DataType dataType) {
        add (dataType.name, dataType)
    }

    /**
     * store data type.
     *
     * @param name name of the data type
     * @param dataType the source data type
     */
    void add (String name, DataType dataType) {
        if (dataType instanceof MappedDataType) {
            mappedTypes.put (name, dataType)
        } else {
            types.put (name, dataType)
        }
    }

    /**
     * find data type by name.
     *
     * @param name the name
     * @return the data type or null if not found
     */
    DataType find (String name) {
        def type = types.get (name)
        if (type) {
            return type
        } else {
            return mappedTypes.get (name)
        }
    }

    /**
     * find data type by $ref name.
     *
     * @param ref the OpenAPI $ref
     * @return the data type or null if not found
     */
    DataType findRef (String ref) {
        def idx = ref.lastIndexOf ('/')
        def path = ref.substring (0, idx + 1)
        def name = ref.substring (idx + 1)

        if (path != '#/components/schemas/') {
            return null
        }

        types.get (name)
    }

    int size () {
        types.size ()
    }

}
