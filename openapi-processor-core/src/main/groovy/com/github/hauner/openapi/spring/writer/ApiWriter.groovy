/*
 * Copyright 2019 https://github.com/hauner/openapi-spring-generator
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

package com.github.hauner.openapi.spring.writer

import com.github.hauner.openapi.spring.ApiOptions
import com.github.hauner.openapi.spring.model.Api
import groovy.util.logging.Slf4j

@Slf4j
class ApiWriter {

    private ApiOptions options
    InterfaceWriter interfaceWriter

    File apiFolder
    File modelFolder

    ApiWriter(ApiOptions options, InterfaceWriter interfaceWriter) {
        this.options = options
        this.interfaceWriter = interfaceWriter
    }

    void write(Api api) {
        createTargetFolders ()

        api.interfaces.each {
            def target = new File (apiFolder, "${it.interfaceName}.java")
            def writer = new FileWriter(target)
            interfaceWriter.write (writer, it)
            writer.close ()
        }
    }

    private void createTargetFolders () {
        def rootPkg = options.packageName.replace ('.', File.separator)
        def apiPkg = [rootPkg, 'api'].join (File.separator)
        def modelPkg = [rootPkg, 'model'].join (File.separator)

        apiFolder = createTargetPackage (apiPkg)
        modelFolder = createTargetPackage (modelPkg)
    }

    private File createTargetPackage (String apiPkg) {
        def folder = new File ([options.targetDir, apiPkg].join (File.separator))
        def success = folder.mkdirs ()
        if (!success) {
            log.error ('failed to create package {}', folder)
        }
        folder
    }

}
