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

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.ebay.api.client.auth.oauth2.R

class ErrorDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val builder = AlertDialog.Builder(activity!!)
        arguments.let {

            builder.setTitle(arguments?.getString(KEY_TITLE))
            builder.setMessage(arguments?.getString(KEY_MESSAGE))
            builder.setPositiveButton(getString(R.string.oauth_ok), null)
        }

        return builder.create()
    }

    companion object {
        const val KEY_TITLE = "title"
        const val KEY_MESSAGE = "message"
        const val TAG = "errorDialogFragment"
    }
}