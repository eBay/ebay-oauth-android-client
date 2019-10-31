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

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Represents the Api Configuration details for eBay OAuth 2.0.
 *
 * <p>Configuration details can be obtained from <a href="https://developer.ebay.com/api-docs/static/oauth-config-params.html">Developer portal</a></p>
 */
@Parcelize
data class ApiConfiguration(
    val clientId: String,
    val redirectUri: String,
    val scope: String
) : Parcelable