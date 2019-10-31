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

package com.ebay.api.client.auth.oauth2

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ebay.api.client.auth.oauth2.model.*
import com.ebay.api.client.auth.oauth2.ui.ErrorDialogFragment
import com.ebay.api.client.auth.oauth2.ui.OAuthActivity

/**
 * Main class to perform eBay Oauth 2.0 for native apps
 */
class OAuthService(
    private val activity: AppCompatActivity,
    private val requestCode: Int = OAUTH_REQUEST_CODE
) {


    /**
     * Perform Authorization request for eBay OAuth 2.0
     *
     * @param requestCode unique code which is used to deliver authorization results
     * @param apiConfiguration client configuration details for eBay OAuth
     * @param state optional state parameter that is provided back in the results
     * @param apiEnvironment optional environment type
     * @param grantType optional Grant Type for authorization request
     *
     */
    public fun performUserAuthorization(
        requestCode: Int,
        apiConfiguration: ApiConfiguration,
        state: String? = null,
        apiEnvironment: ApiEnvironment = ApiEnvironment.PRODUCTION,
        grantType: GrantType = GrantType.AUTHORIZATION_CODE
    ) {

        val authorizationLink = AuthorizationLink(
            activity,
            apiEnvironment,
            grantType,
            apiConfiguration.clientId,
            apiConfiguration.redirectUri,
            apiConfiguration.scope
        )

        val error = authorizationLink.validate()

        error?.let {
            showErrorDialog(error)
            return
        }

        val intent: Intent = OAuthActivity.createIntent(
            activity,
            grantType,
            state,
            apiConfiguration,
            apiEnvironment
        )

        activity.startActivityForResult(intent, requestCode)
    }

    /**
     * Use this perform User Authorization
     * Once the user authenticates and approves the consent, the callback need to be
     * captured by the redirect URL setup by the app
     */
    public fun performUserAuthorization() {

        val ebayApiConfig = ApiSessionConfiguration.getInstance()

        val apiConfiguration = ebayApiConfig.apiConfiguration
            ?: run {
                showErrorDialog("ApiConfiguration is not initialized for eBay OAuth 2.0. Please use ApiSessionConfiguration.initialize() before starting OAuth")
                return
            }

        val apiEnvironment = ebayApiConfig.apiEnvironment
            ?: run {
                showErrorDialog("Api Environment is not initialized for eBay OAuth 2.0. Please use ApiSessionConfiguration.initialize() before starting OAuth")
                return
            }

        performUserAuthorization(
            requestCode,
            apiConfiguration,
            null,
            apiEnvironment
        )
    }


    /**
     * get Authorization response from result Intent. Call from Activity.onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
     */
    public fun getAuthorizationResponse(intent: Intent?): AuthorizationCode? =
        intent?.getParcelableExtra("result")

    /**
     * get Authorization error from result Intent. Call from Activity.onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
     */
    public fun getAuthorizationError(intent: Intent?): String? = intent?.getStringExtra("error")


    /**
     * get Authorization state from result Intent. Call from Activity.onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
     */
    public fun getAuthorizationState(intent:Intent?): String? = intent?.getStringExtra("state")


    private fun showErrorDialog(message: String) {
        var fragment = ErrorDialogFragment().apply {
            arguments = Bundle().apply {
                putString(ErrorDialogFragment.KEY_TITLE, activity?.getString(R.string.oauth_error))
                putString(ErrorDialogFragment.KEY_MESSAGE, message)
            }
        }

        fragment.show(activity.supportFragmentManager, ErrorDialogFragment.TAG)
    }

    companion object {
        const val OAUTH_REQUEST_CODE = 0
    }

}