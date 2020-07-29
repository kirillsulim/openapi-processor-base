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

package com.github.hauner.openapi.core.writer.java

import com.github.hauner.openapi.core.converter.ApiOptions
import com.github.hauner.openapi.core.model.datatypes.ObjectDataType
import com.github.hauner.openapi.core.model.datatypes.DataType
import com.github.hauner.openapi.core.support.Identifier
import io.openapiprocessor.core.writer.java.DefaultImportFilter

/**
 * Writer for POJO classes.
 *
 * @author Martin Hauner
 * @author Bastian Wilhelm
 */
class DataTypeWriter {
    ApiOptions apiOptions
    SimpleWriter headerWriter
    BeanValidationFactory beanValidationFactory

    void write (Writer target, ObjectDataType dataType) {
        headerWriter.write (target)
        target.write ("package ${dataType.packageName};\n\n")

        List<String> imports = collectImports (dataType.packageName, dataType)

        imports.each {
            target.write ("import ${it};\n")
        }
        if (!imports.isEmpty ()) {
            target.write ("\n")
        }

        if (dataType.deprecated) {
            target.write ("@Deprecated\n")
        }
        target.write ("public class ${dataType.type} {\n\n")

        def propertyNames = dataType.properties.keySet ()
        propertyNames.each {
            def javaPropertyName = Identifier.toCamelCase (it)
            def propDataType = dataType.getObjectProperty (it)
            target.write (getProp (it, javaPropertyName, propDataType))
        }

        propertyNames.each {
            def javaPropertyName = Identifier.toCamelCase (it)
            def propDataType = dataType.getObjectProperty (it)
            target.write (getGetter (javaPropertyName, propDataType))
            target.write (getSetter (javaPropertyName, propDataType))
        }

        target.write ("}\n")
    }

    private String getProp (String propertyName, String javaPropertyName, DataType propDataType) {
        String result = ''
        if (propDataType.deprecated) {
            result += "    @Deprecated\n"
        }

        result += "    @JsonProperty(\"${propertyName}\")\n"

        if (apiOptions.beanValidation) {
            def beanValidationAnnotations = beanValidationFactory.createAnnotations (propDataType)
            if (!beanValidationAnnotations.empty) {
                result += "    $beanValidationAnnotations\n"
            }
        }

        result += "    private ${propDataType.name} ${javaPropertyName};\n\n"
        result
    }

    private String getGetter (String propertyName, DataType propDataType) {
        String result = ''
        if (propDataType.deprecated) {
            result += "    @Deprecated\n"
        }

        result += """\
    public ${propDataType.name} get${propertyName.capitalize ()}() {
        return ${propertyName};
    }

"""
        result
    }

    private String getSetter (String propertyName, DataType propDataType) {
        String result = ''
        if (propDataType.deprecated) {
            result += "    @Deprecated\n"
        }

        result += """\
    public void set${propertyName.capitalize ()}(${propDataType.name} ${propertyName}) {
        this.${propertyName} = ${propertyName};
    }

"""
        result
    }

    List<String> collectImports (String packageName, ObjectDataType dataType) {
        Set<String> imports = []
        imports.add ('com.fasterxml.jackson.annotation.JsonProperty')

        imports.addAll (dataType.referencedImports)

        if (apiOptions.beanValidation) {
            for (DataType propDataType : dataType.properties.values ()) {
                imports.addAll (beanValidationFactory.collectImports (propDataType))
            }
        }

        new DefaultImportFilter ()
            .filter (packageName, imports)
            .sort ()
    }

}
