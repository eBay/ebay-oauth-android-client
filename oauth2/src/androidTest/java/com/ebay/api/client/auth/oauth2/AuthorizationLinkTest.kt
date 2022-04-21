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

import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import com.ebay.api.client.auth.oauth2.internal.CustomTabsHelper
import com.ebay.api.client.auth.oauth2.internal.DeepLinkHelper
import com.ebay.api.client.auth.oauth2.model.ApiEnvironment
import com.ebay.api.client.auth.oauth2.model.GrantType
import com.ebay.api.client.auth.oauth2.ui.OAuthActivity
import org.hamcrest.CoreMatchers.allOf
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthorizationLinkTest {

    @get: Rule
    var activityTestRule = ActivityTestRule<OAuthActivity>(OAuthActivity::class.java, false, false)

    @Before
    fun setup() {
        Intents.init()
    }

    @After
    fun cleanup() {
        Intents.release()
    }

    @Test
    fun testWebLaunch() {

        val expectedIntent = allOf(
            hasAction(Intent.ACTION_VIEW),
            hasData(Uri.parse("https://auth.ebay.com/oauth2/authorize?client_id=clientId&redirect_uri=https%3A%2F%2Ftest.redirect.uri.com%2Fauthdeeplink&response_type=code&scope=scope&prompt=login"))
        )

        val context = InstrumentationRegistry.getInstrumentation().context
        val authorizationLink = AuthorizationLink(
            context,
            ApiEnvironment.PRODUCTION,
            GrantType.AUTHORIZATION_CODE,
            "clientId",
            "https://test.redirect.uri.com/authdeeplink",
            "scope",
            "login",
            null,
            TestDeepLinkHelper(context, deepLinkVerify = true, ebayDeepLinkVerify = false),
            TestCustomTabsHelper(context, null)
        )

        intending(expectedIntent).respondWith(Instrumentation.ActivityResult(0, null))

        authorizationLink.launch(false)

        intended(expectedIntent)

    }


    @Test
    fun testNativeLaunch() {

        val expectedIntent = allOf(
            hasAction(Intent.ACTION_VIEW),
            hasData(Uri.parse("ebay.oauth2://authorize?client_id=clientId&redirect_uri=https%3A%2F%2Ftest.redirect.uri.com%2Fauthdeeplink&response_type=code&scope=scope&prompt=login"))
        )

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val authorizationLink = AuthorizationLink(
            context,
            ApiEnvironment.PRODUCTION,
            GrantType.AUTHORIZATION_CODE,
            "clientId",
            "https://test.redirect.uri.com/authdeeplink",
            "scope",
            "login",
            null,
            TestDeepLinkHelper(context, deepLinkVerify = true, ebayDeepLinkVerify = true),
            TestCustomTabsHelper(context, null)
        )

        intending(expectedIntent).respondWith(Instrumentation.ActivityResult(0, null))

        authorizationLink.launch(false)

        intended(expectedIntent)

    }
    @Test
    fun testSandboxWithNativeLaunch() {

        val expectedIntent = allOf(
            hasAction(Intent.ACTION_VIEW),
            hasData(Uri.parse("https://auth.sandbox.ebay.com/oauth2/authorize?client_id=clientId&redirect_uri=https%3A%2F%2Ftest.redirect.uri.com%2Fauthdeeplink&response_type=code&scope=scope&prompt=login"))
        )

        val context = InstrumentationRegistry.getInstrumentation().context
        val authorizationLink = AuthorizationLink(
            context,
            ApiEnvironment.SANDBOX,
            GrantType.AUTHORIZATION_CODE,
            "clientId",
            "https://test.redirect.uri.com/authdeeplink",
            "scope",
            "login",
            null,
            TestDeepLinkHelper(context, deepLinkVerify = true, ebayDeepLinkVerify = false),
            TestCustomTabsHelper(context, null)
        )

        intending(expectedIntent).respondWith(Instrumentation.ActivityResult(0, null))

        authorizationLink.launch(false)

        intended(expectedIntent)

    }

    @Test
    fun testChromeTabsLaunch() {

        val expectedIntent = allOf(
            hasAction(Intent.ACTION_VIEW),
            hasData(Uri.parse("https://auth.ebay.com/oauth2/authorize?client_id=clientId&redirect_uri=https%3A%2F%2Ftest.redirect.uri.com%2Fauthdeeplink&response_type=code&scope=scope&prompt=login"))
        )

        val context = InstrumentationRegistry.getInstrumentation().context
        val authorizationLink = AuthorizationLink(
            context,
            ApiEnvironment.PRODUCTION,
            GrantType.AUTHORIZATION_CODE,
            "clientId",
            "https://test.redirect.uri.com/authdeeplink",
            "scope",
            "login",
            null,
            TestDeepLinkHelper(context, deepLinkVerify = true, ebayDeepLinkVerify = false),
            TestCustomTabsHelper(context, CustomTabsHelper.STABLE_PACKAGE)
        )

        intending(expectedIntent).respondWith(Instrumentation.ActivityResult(0, null))

        authorizationLink.launch(false)

        intended(expectedIntent)

    }

    class TestDeepLinkHelper(
        context: Context,
        val deepLinkVerify: Boolean,
        val ebayDeepLinkVerify: Boolean
    ) : DeepLinkHelper(context) {
        override fun verifyDeepLinkInCurrentApp(uri: String): Boolean {
            return deepLinkVerify
        }

        override fun verifyEbayDeepLink(intent: Intent): Boolean {
            return ebayDeepLinkVerify
        }
    }

    class TestCustomTabsHelper(context: Context, private val packageName: String?) :
        CustomTabsHelper(context) {
        override fun getPackageNameToUse(): String? {
            return packageName
        }
    }
}