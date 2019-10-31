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
import com.ebay.api.client.auth.oauth2.internal.CustomTabsHelper
import com.ebay.api.client.auth.oauth2.internal.DeepLinkHelper
import com.ebay.api.client.auth.oauth2.model.ApiEnvironment
import com.ebay.api.client.auth.oauth2.model.GrantType
import com.nhaarman.mockitokotlin2.mock
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.Mockito

@RunWith(Parameterized::class)
class AuthorizationLinkUnitTest(
    environment: ApiEnvironment,
    grantType: GrantType,
    clientId: String,
    redirectUri: String,
    scope: String,
    nativelink: Boolean,
    private val result: String?
) {

    private val context: Context = mock()
    private val deepLinkHelper: DeepLinkHelper = mock()
    private val customTabsHelper: CustomTabsHelper = mock()

    private val uut =
        AuthorizationLink(
            context,
            environment,
            grantType,
            clientId,
            redirectUri,
            scope,
            null,
            null,
            deepLinkHelper,
            customTabsHelper
        )

    companion object {
        private const val clientId = "clientId"
        private const val scope = "scope"
        private const val redirectUri = "https://test.redirect.uri.com/authdeeplink"

        @JvmStatic
        @Parameterized.Parameters
        fun data() = listOf(
            arrayOf(
                ApiEnvironment.PRODUCTION,
                GrantType.AUTHORIZATION_CODE,
                clientId,
                redirectUri,
                scope,
                false,
                null
            ),
            arrayOf(
                ApiEnvironment.PRODUCTION,
                GrantType.AUTHORIZATION_CODE,
                "",
                "redirectUri",
                "scope",
                false,
                String.format(AuthorizationLink.missingMandatoryParameters, "clientId")
            ),
            arrayOf(
                ApiEnvironment.PRODUCTION,
                GrantType.AUTHORIZATION_CODE,
                "clientId",
                "",
                "scope",
                false,
                String.format(AuthorizationLink.missingMandatoryParameters, "redirectUri")
            ),
            arrayOf(
                ApiEnvironment.PRODUCTION,
                GrantType.AUTHORIZATION_CODE,
                "clientId",
                "redirectUri",
                "",
                false,
                String.format(AuthorizationLink.missingMandatoryParameters, "scope")
            ),
            arrayOf(
                ApiEnvironment.PRODUCTION,
                GrantType.AUTHORIZATION_CODE,
                "clientId",
                "redirectUri",
                "scope",
                false,
                AuthorizationLink.invalidRedirectUri
            ),
            arrayOf(
                ApiEnvironment.PRODUCTION,
                GrantType.CLIENT_CREDENTIALS,
                "clientId",
                "redirectUri",
                "scope",
                false,
                AuthorizationLink.unSupportedGrantType
            ),
            arrayOf(
                ApiEnvironment.PRODUCTION,
                GrantType.AUTHORIZATION_CODE,
                "clientId",
                "https://test.com",
                "scope",
                true,
                null
            )
        )
    }


    @Test
    fun testValidate() {
        Mockito.`when`(deepLinkHelper.verifyDeepLinkInCurrentApp(Mockito.anyString())).thenReturn(true)
        Mockito.`when`(deepLinkHelper.verifyEbayDeepLink(any())).thenReturn(true)
        val validationMessage = uut.validate()

        assertThat(validationMessage, `is`(result))
    }

    fun <T> any(): T = Mockito.any<T>()
}