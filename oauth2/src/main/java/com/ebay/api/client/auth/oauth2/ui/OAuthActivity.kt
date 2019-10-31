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

package com.ebay.api.client.auth.oauth2.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.ebay.api.client.auth.oauth2.AuthorizationLink
import com.ebay.api.client.auth.oauth2.model.ApiConfiguration
import com.ebay.api.client.auth.oauth2.model.ApiEnvironment
import com.ebay.api.client.auth.oauth2.model.AuthorizationCode
import com.ebay.api.client.auth.oauth2.model.GrantType

class OAuthActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        init()
    }

    private fun init() {

        if (intent.data == null)
            startOauth()
        else
            processUserAuthorizationResponse()
    }

    private fun startOauth() {
        val apiConfiguration: ApiConfiguration = intent.getParcelableExtra(PARAM_API_CONFIG)
        val apiEnvironment: ApiEnvironment =
            ApiEnvironment.valueOf(intent.getStringExtra(PARAM_API_ENV))
        val grantType: GrantType = GrantType.valueOf(intent.getStringExtra(PARAM_OAUTH_GRANT_TYPE))
        val state = intent.getStringExtra(PARAM_STATE)
        val prompt = intent.getStringExtra(PARAM_PROMPT)

        // Uses native deep link when available
        val authLink = AuthorizationLink(
            this,
            apiEnvironment,
            grantType,
            apiConfiguration.clientId,
            apiConfiguration.redirectUri,
            apiConfiguration.scope,
            state,
            prompt
        )

        val authError = authLink.validate()
        if (authError != null) {
            onError(authError, null)
            return
        }

        authLink.launch(intent.getBooleanExtra(PARAM_FORCE_BROWSER, false))

    }


    private fun processUserAuthorizationResponse() {
        val uri = intent!!.data ?: return

        val error = uri.getQueryParameter(RESULT_PARAM_ERROR) ?: uri.getQueryParameter(
            RESULT_PARAM_ERROR_ID)

        val state = uri.getQueryParameter(PARAM_STATE)

        error?.let {
            onError(error, state)
            return
        }

        val code = uri.getQueryParameter(RESULT_PARAM_CODE)
        val expiresIn = uri.getQueryParameter(RESULT_PARAM_EXPIRES_IN)
        if (code == null || expiresIn == null) {
            onError("No Data", state)
            return
        }

        val intent = Intent()
        intent.putExtra(
            INTENT_KEY_RESULT,
            AuthorizationCode(code, expiresIn)
        )
        state?.let {
            intent.putExtra(INTENT_KEY_STATE, state)
        }

        setResult(RESULT_OK, intent)

        finish()
    }

    private fun onError(error: String,
                        state: String?) {

        val intent = Intent()
        intent.putExtra(INTENT_KEY_ERROR, error)
        state?.let {
            intent.putExtra(INTENT_KEY_STATE, state)
        }
        setResult(RESULT_OK, intent)

        finish()
    }


    companion object {
        private const val PARAM_API_CONFIG = "api_config"
        private const val PARAM_API_ENV = "api_env"
        private const val PARAM_OAUTH_GRANT_TYPE = "grant_type"
        private const val PARAM_STATE = "state"
        private const val PARAM_PROMPT = "prompt"
        private const val PARAM_FORCE_BROWSER = "force_browser"
        private const val RESULT_PARAM_CODE = "code"
        private const val RESULT_PARAM_ERROR = "error"
        private const val RESULT_PARAM_ERROR_ID = "errorId"
        private const val RESULT_PARAM_EXPIRES_IN = "expires_in"

        private const val INTENT_KEY_RESULT = "result"
        private const val INTENT_KEY_ERROR = "error"
        private const val INTENT_KEY_STATE = "state"

        fun createIntent(
            context: Context,
            grantType: GrantType,
            state: String?,
            apiConfiguration: ApiConfiguration,
            apiEnvironment: ApiEnvironment
        ): Intent {
            val intent = Intent(context, OAuthActivity::class.java)
            intent.putExtra(PARAM_API_CONFIG, apiConfiguration)
            intent.putExtra(PARAM_API_ENV, apiEnvironment.toString())
            intent.putExtra(PARAM_OAUTH_GRANT_TYPE, grantType.toString())
            intent.putExtra(PARAM_STATE, state)
            return intent
        }

        fun newResponseIntent(context: Context, uri: Uri?): Intent {
            val intent = Intent(context, OAuthActivity::class.java)
            intent.data = uri
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            return intent
        }
    }

}