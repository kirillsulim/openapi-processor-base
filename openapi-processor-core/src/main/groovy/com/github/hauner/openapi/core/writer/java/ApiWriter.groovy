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

import io.openapiprocessor.core.converter.ApiOptions
import com.github.hauner.openapi.core.model.Api
import io.openapiprocessor.core.model.Interface
import io.openapiprocessor.core.model.datatypes.ObjectDataType
import io.openapiprocessor.core.model.datatypes.StringEnumDataType
import com.google.googlejavaformat.java.Formatter
import com.google.googlejavaformat.java.JavaFormatterOptions
import groovy.util.logging.Slf4j

import java.nio.file.Files
import java.nio.file.Path

import static io.openapiprocessor.core.misc.URLKt.toURL

/**
 * Root writer for the generated api files.
 *
 * @author Martin Hauner
 */
@Slf4j
class ApiWriter {

    private ApiOptions options
    InterfaceWriter interfaceWriter
    DataTypeWriter dataTypeWriter
    StringEnumWriter enumWriter

    Path apiFolder
    Path modelFolder

    Formatter formatter

    ApiWriter (
        ApiOptions options,
        InterfaceWriter interfaceWriter,
        DataTypeWriter dataTypeWriter,
        StringEnumWriter enumWriter,
        boolean enableFormatter = true) {
        this.options = options
        this.interfaceWriter = interfaceWriter
        this.dataTypeWriter = dataTypeWriter
        this.enumWriter = enumWriter

        if (enableFormatter) {
            formatter = new Formatter (
                JavaFormatterOptions
                    .builder ()
                    .style (JavaFormatterOptions.Style.AOSP)
                    .build ())
        }
    }

    void write(Api api) {
        createTargetFolders ()

        api.interfaces.each {
            def target = apiFolder.resolve ("${it.interfaceName}.java")
            def writer = new BufferedWriter (new PathWriter(target))
            writeInterface (writer, it)
            writer.close ()
        }

        api.models.objectDataTypes.each {
            def target = modelFolder.resolve ("${it.name}.java")
            def writer = new BufferedWriter (new PathWriter(target))
            writeDataType (writer, it)
            writer.close ()
        }

        api.models.enumDataTypes.each {
            def target = modelFolder.resolve ("${it.name}.java")
            def writer = new BufferedWriter (new PathWriter(target))
            writeEnumDataType (writer, it)
            writer.close ()
        }
    }

    private void writeInterface (Writer writer, Interface itf) {
        def raw = new StringWriter ()
        interfaceWriter.write (raw, itf)
        writer.write (format (raw.toString ()))
    }

    private void writeDataType (Writer writer, ObjectDataType dataType) {
        def raw = new StringWriter ()
        dataTypeWriter.write (raw, dataType)
        writer.write (format (raw.toString ()))
    }

    private void writeEnumDataType (Writer writer, StringEnumDataType enumDataType) {
        def raw = new StringWriter ()
        enumWriter.write (raw, enumDataType)
        writer.write (format (raw.toString ()))
    }

    private String format (String raw) {
        if (formatter == null) {
            return raw
        }
        correctLineFeed (formatter.formatSource (raw))
    }

    private String correctLineFeed (String formatted) {
        int index = formatted.findLastIndexOf (0) {
            it == '}'
        }

        new StringBuilder ()
            .append (formatted.substring (0, index))
            .append ("\n}\n")
    }

    private void createTargetFolders () {
        def rootPkg = options.packageName.replace ('.', '/')
        def apiPkg = [rootPkg, 'api'].join ('/')
        def modelPkg = [rootPkg, 'model'].join ('/')
        log.debug ('creating target folders: {}', rootPkg)

        apiFolder = createTargetPackage (apiPkg)
        log.debug ('created target folder: {}', apiFolder.toAbsolutePath ().toString ())

        modelFolder = createTargetPackage (modelPkg)
        log.debug ('created target folder: {}', modelFolder.toAbsolutePath ().toString ())
    }

    private Path createTargetPackage (String apiPkg) {
        String root = options.targetDir
        String pkg = [root, apiPkg].join ('/')

        def target = Path.of (toURL(pkg).toURI ())
        Files.createDirectories (target)
        target
    }

}
