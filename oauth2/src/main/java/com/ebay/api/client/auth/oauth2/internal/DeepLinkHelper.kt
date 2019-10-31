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
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import androidx.annotation.VisibleForTesting

@VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
open class DeepLinkHelper(private val context: Context) {
    private val packageName = "com.ebay.mobile"

    open fun verifyEbayDeepLink(intent: Intent): Boolean {
        val newIntent = Intent(intent.action, intent.data)
        newIntent.setPackage(packageName)
        val resolveInfoList: List<ResolveInfo> =
            context.packageManager.queryIntentActivities(intent, 0)
        for (resolveInfo in resolveInfoList) {
            val activityInfo = resolveInfo.activityInfo
            if (activityInfo != null) {
                return true
            }
        }
        return false
    }

    open fun verifyDeepLinkInCurrentApp(uri: String): Boolean {
        val redirectIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        redirectIntent.setPackage(context.packageName)
        val resolveInfoList = context.packageManager.queryIntentActivities(
            redirectIntent, PackageManager.MATCH_DEFAULT_ONLY
        )
        return resolveInfoList.isNotEmpty()
    }


}