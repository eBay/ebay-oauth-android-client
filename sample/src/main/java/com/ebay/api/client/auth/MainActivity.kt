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

package com.ebay.api.client.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.ebay.api.client.auth.oauth2.OAuthService
import com.ebay.api.client.auth.oauth2.model.ApiConfiguration
import com.ebay.api.client.auth.oauth2.model.ApiEnvironment
import com.ebay.api.client.auth.oauth2.model.ApiSessionConfiguration

class MainActivity : AppCompatActivity() {
    private val oAuthService = OAuthService(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main)

        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener {
            ApiSessionConfiguration.initialize(
                apiEnvironment = ApiEnvironment.PRODUCTION,
                apiConfiguration = ApiConfiguration(
                    "<app id>",
                    "<redirect uri>",
                    "scope_1 " + "scope_2"
                )
            )
            oAuthService.performUserAuthorization()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == OAuthService.OAUTH_REQUEST_CODE) {

            // Obtain any error from user consent
            val error = oAuthService.getAuthorizationError(data)

            // Obtain authorization code from user consent
            val code = oAuthService.getAuthorizationResponse(data)

            val state = oAuthService.getAuthorizationState(data)
        }
    }


}