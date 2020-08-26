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

package io.openapiprocessor.core.converter

import io.openapiprocessor.core.converter.mapping.Mapping

/**
 * Options of the processor.
 *
 * @author Martin Hauner
 * @author Bastian Wilhelm
 */
class ApiOptions {

    /**
     * the path to the open api yaml file.
     */
    lateinit var apiPath: String

    /**
     * the destination folder for generating interfaces & models. This is the parent of the
     * {@link #packageName} folder tree.
     */
    lateinit var targetDir: String

    /**
     * the root package of the generated interfaces/model. The package folder tree will be created
     * inside {@link #targetDir}. Interfaces and models will be placed into the "api" and "model"
     * subpackages of packageName:
     * - interfaces => "${packageName}.api"
     * - models => "${packageName}.model"
     */
    var packageName = "io.openapiprocessor.generated"

    /**
     * provide enabling Bean Validation (JSR303) annotations. Default is false (disabled)
     */
    var beanValidation = false

    /**
     * provide additional type mapping information to map OpenAPI types to java types. The list can
     * contain the following mappings:
     *
     * {@link io.openapiprocessor.core.converter.mapping.TypeMapping}: used to globally
     * override the mapping of an OpenAPI schema to a specific java type.
     *
     * {@link io.openapiprocessor.core.converter.mapping.EndpointTypeMapping}: used to
     * override parameter/response type mappings or to add additional parameters on a single
     * endpoint.
     */
    var typeMappings: List<Mapping> = emptyList()

}
