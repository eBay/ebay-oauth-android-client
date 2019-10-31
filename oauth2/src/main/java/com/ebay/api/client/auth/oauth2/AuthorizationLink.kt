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

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.ebay.api.client.auth.oauth2.internal.CustomTabsHelper
import com.ebay.api.client.auth.oauth2.internal.DeepLinkHelper
import com.ebay.api.client.auth.oauth2.model.ApiEnvironment
import com.ebay.api.client.auth.oauth2.model.GrantType

class AuthorizationLink {

    private val deepLinkHelper: DeepLinkHelper
    private val customTabsHelper: CustomTabsHelper
    private val context: Context
    private val environment: ApiEnvironment
    private val grantType: GrantType
    private val clientId: String
    private val redirectUri: String
    private val scope: String
    private val prompt: String?
    private val state: String?


    constructor(
        context: Context,
        environment: ApiEnvironment,
        grantType: GrantType,
        clientId: String,
        redirectUri: String,
        scope: String,
        prompt: String? = "login",
        state: String? = null
    ) : this(
        context,
        environment,
        grantType,
        clientId,
        redirectUri,
        scope,
        prompt,
        state,
        DeepLinkHelper(context),
        CustomTabsHelper(context)
    )

    constructor(
        context: Context,
        environment: ApiEnvironment,
        grantType: GrantType,
        clientId: String,
        redirectUri: String,
        scope: String,
        prompt: String? = "login",
        state: String? = null,
        deepLinkHelper: DeepLinkHelper,
        customTabsHelper: CustomTabsHelper
    ) {
        this.context = context
        this.environment = environment
        this.grantType = grantType
        this.clientId = clientId
        this.redirectUri = redirectUri
        this.scope = scope
        this.prompt = prompt
        this.state = state
        this.deepLinkHelper = deepLinkHelper
        this.customTabsHelper = customTabsHelper
    }

    fun launch(forceWebview: Boolean) {
        var authLaunched = launchOauthNative()

        if (authLaunched)
            return

        if (!forceWebview)
            authLaunched = launchOauthChromeTabs()

        if (!authLaunched)
            launchOauthBrowser()
    }

    fun validate(): String? {

        if (clientId.isBlank())
            return String.format(missingMandatoryParameters, "clientId")
        if (redirectUri.isBlank())
            return String.format(missingMandatoryParameters, "redirectUri")
        if (scope.isBlank())
            return String.format(missingMandatoryParameters, "scope")

        // validate grant Type
        if (grantType != GrantType.AUTHORIZATION_CODE)
            return unSupportedGrantType

        // Only allow https redirect uri
        if (!redirectUri.startsWith("https"))
            return invalidRedirectUri

        // verify redirect_uri deep link
        if (!deepLinkHelper.verifyDeepLinkInCurrentApp(redirectUri))
            return String.format(missingRedirectUriDeepLink, redirectUri)

        // verify environment when native deep link
        if (environment == ApiEnvironment.SANDBOX &&
            deepLinkHelper.verifyEbayDeepLink(Intent(Intent.ACTION_VIEW, Uri.parse(userConsentDeepLink))))
            return unSupportedEnvironmentType

        return null
    }


    private fun launchOauthChromeTabs(): Boolean {
        val intent = CustomTabsIntent.Builder().build()
        val packageName = customTabsHelper.getPackageNameToUse()
        packageName.let {
            intent.intent.setPackage(packageName)
            intent.launchUrl(context, buildWebUri())
            return true
        }
        return false
    }

    private fun launchOauthBrowser(): Boolean {
        context.startActivity(Intent(Intent.ACTION_VIEW, buildWebUri()))
        return true
    }


    private fun launchOauthNative(): Boolean {
        val intent = Intent(Intent.ACTION_VIEW, buildUserConsentLink(userConsentDeepLink))

        return if (deepLinkHelper.verifyEbayDeepLink(intent)) {
            context.startActivity(intent)
            true
        } else
            false
    }

    private fun buildWebUri(): Uri {
        val url = if (environment == ApiEnvironment.PRODUCTION)
            userConsentEndpoint
        else userConsentEndpointSandbox

        return buildUserConsentLink(url)
    }


    private fun buildUserConsentLink(endPoint: String): Uri {
        val uriBuilder = Uri.parse(endPoint).buildUpon()
        uriBuilder.appendQueryParameter(PARAM_CLIENT_ID, clientId)
        uriBuilder.appendQueryParameter(PARAM_REDIRECT_URI, redirectUri)
        uriBuilder.appendQueryParameter(PARAM_RESPONSE_TYPE, grantType.value)
        uriBuilder.appendQueryParameter(PARAM_SCOPE, scope)
        if (!prompt.isNullOrBlank())
            uriBuilder.appendQueryParameter(PARAM_PROMPT, prompt)
        if (!state.isNullOrBlank())
            uriBuilder.appendQueryParameter(PARAM_STATE, state)

        return uriBuilder.build()
    }


    companion object {
        const val userConsentEndpoint =
            "https://auth.ebay.com/oauth2/authorize"
        const val userConsentEndpointSandbox = "https://auth.sandbox.ebay.com/oauth2/authorize"
        const val userConsentDeepLink = "ebay.oauth2://authorize"

        const val PARAM_CLIENT_ID = "client_id"
        const val PARAM_REDIRECT_URI = "redirect_uri"
        const val PARAM_SCOPE = "scope"
        const val PARAM_RESPONSE_TYPE = "response_type"
        const val PARAM_PROMPT = "prompt"
        const val PARAM_STATE = "state"

        const val missingMandatoryParameters =
            "Please provide a valid %1s. \n\nFor more details visit https://developer.ebay.com/api-docs/static/oauth-config-params.html"

        const val missingRedirectUriDeepLink =
            "%1s is not registered as Native App Link in this application. \n\nPlease override OAuthRedirectActivity in your application manifest file to register for App Links."

        const val invalidRedirectUri =
            "Please provide a valid redirect_uri with https scheme. \n\nFor more details visit https://developer.ebay.com/api-docs/static/oauth-authorization-code-grant.html"

        const val unSupportedGrantType =
            "Only Authorization Code grant flow is support on Native apps. \n\n" +
                    "Please see OAuth 2.0 for Native apps for details https://tools.ietf.org/html/draft-ietf-oauth-native-apps-12."

        const val unSupportedEnvironmentType =
            "Only production environment is supported when eBay native app is installed on this device."

    }


}