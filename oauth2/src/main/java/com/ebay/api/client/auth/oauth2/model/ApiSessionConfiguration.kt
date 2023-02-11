/*
 * Copyright 2019 eBay Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ebay.api.client.auth.oauth2.model

class ApiSessionConfiguration private constructor() {

    public var apiEnvironment: ApiEnvironment? = ApiEnvironment.PRODUCTION
        private set

    public var apiConfiguration: ApiConfiguration? = null
        private set


    companion object {
        private val instance: ApiSessionConfiguration = ApiSessionConfiguration()

        @Synchronized
        @JvmStatic fun getInstance(): ApiSessionConfiguration {
            return instance
        }

        @JvmStatic fun initialize(apiEnvironment: ApiEnvironment, apiConfiguration: ApiConfiguration) : ApiSessionConfiguration {
            instance.apiEnvironment = apiEnvironment
            instance.apiConfiguration = apiConfiguration
            return instance
        }
    }

}