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

package com.ebay.api.client.auth.oauth2.internal

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.VisibleForTesting

@VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
open class CustomTabsHelper(private val context: Context) {

    private var sPackageNameToUse: String? = null

    /**
     * Goes through all apps that handle VIEW intents. Picks
     * the one chosen by the user if there is one, otherwise makes a best effort to return a
     * valid package name.
     *
     * @return The package name recommended to use for connecting to custom tabs related components.
     */
    open fun getPackageNameToUse(): String? {
        if (sPackageNameToUse != null) return sPackageNameToUse

        val pm = context.packageManager

        // Get all apps that can handle VIEW intents.
        val activityIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.example.com"))
        val resolvedActivityList = pm.queryIntentActivities(activityIntent, 0)
        val packagesSupportingCustomTabs = arrayListOf<String>()
        for (info in resolvedActivityList) {
            val serviceIntent = Intent()
            serviceIntent.action =
                ACTION_CUSTOM_TABS_CONNECTION
            serviceIntent.setPackage(info.activityInfo.packageName)
            if (pm.resolveService(serviceIntent, 0) != null) {
                packagesSupportingCustomTabs.add(info.activityInfo.packageName)
            }
        }

        // Now packagesSupportingCustomTabs contains all apps that can handle both VIEW intents
        // and service calls.
        sPackageNameToUse = when {
            (packagesSupportingCustomTabs.size == 1) ->
                packagesSupportingCustomTabs[0]
            (STABLE_PACKAGE in packagesSupportingCustomTabs) ->
                STABLE_PACKAGE
            else -> null
        }
        return sPackageNameToUse
    }

    companion object {
        const val STABLE_PACKAGE = "com.android.chrome"
        const val ACTION_CUSTOM_TABS_CONNECTION =
            "android.support.customtabs.action.CustomTabsService"

    }
}