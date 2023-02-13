# What is OAuth 2.0
ebay-oauth-android-client library is a simple and easy to use library to integrate your Android native app with eBay OAuth and designed to be used for OAuth specifications supported at eBay. 

[OAuth 2.0](https://tools.ietf.org/html/rfc6749) is the most widely used standard for authentication and authorization for API based access. The complete end to end documentation on how eBay OAuth functions is available at [developer.ebay.com](https://developer.ebay.com/api-docs/static/oauth-tokens.html).

# Getting Started
To use this library, you must have an active eBay Developer Program account.  The account will provide OAuth 2.0 client credentials that will allow creation of access tokens. For details, see: [Creating an eBay Developer Program account](https://developer.ebay.com/api-docs/static/creating-edp-account.html)

# Supported Grant Types for OAuth on Native apps

eBay OAuth 2.0 on native apps only support `authorization_code` grant flow. Refer to [eBay Developer Portal](https://developer.ebay.com/api-docs/static/oauth-tokens.html) for additional details. 

Obtaining a user access token through (authorization code grant flow)[https://developer.ebay.com/api-docs/static/oauth-authorization-code-grant.html] generally consists of obtaining account-owner's consent and exchanging the consent for user access token. 
While obtaining account-owner's consent is performed on user device, exchanging the account-owner's consent to user access token requires the use of client_secret provided in developer portal. Native apps are considered public and cannot hold the client secret securely. Please create a backend service to perform authorization code grant request securely which can be used by native app.

Native apps performs authorization code grant flow in three steps
1. Partner app launches a consent request to get account owner’s consent
2. On user consent, Partner app calls Partner backend service to performs authorization code grant request
3. Partner backend service returns user access token to Partner app

This library helps perform step 1 on Android app.

# Obtaining Library
This library is distributed via maven central repository. To use this library, include the below as dependency in your project

```
dependencies {
    implementation 'com.ebay.auth:ebay-oauth-android-client:1.0.1'
}
```


# Application Setup
Before performing OAuth, the library should be initialized with details about your application from eBay developer portal. The library uses 
- Client ID. For details see [Getting your OAuth credentials](https://developer.ebay.com/api-docs/static/oauth-credentials.html)
- Redirect Uri (RuName). for details see [Getting your Redirect_Uri](https://developer.ebay.com/api-docs/static/oauth-redirect-uri.html)
- Url encoded list of scopes. for details see [Specifying OAuth scopes](https://developer.ebay.com/api-docs/static/oauth-scopes.html)

Use these details in `ApiSessionConfiguration.initialize()` as shown below:

```
 ApiSessionConfiguration.initialize(
                apiEnvironment = ApiEnvironment.PRODUCTION,
                apiConfiguration = ApiConfiguration(
                    <Client ID>,
                    <Redirect Uri / RuName>,
                    <space separated scopes>
                )
            )
``` 

To receive Authorization code, client app must override `OauthRedirectActivity` in the manifest with custom intent filters to match the registered redirect URI from eBay developer portal.

```
        <activity
            android:name="com.ebay.api.client.auth.oauth2.ui.OAuthRedirectActivity"
            android:exported="true"
            tools:node="replace">
            <intent-filter>
                <action android:name="android.intent.action.VIEW" />

                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />

                <data
                    android:host="<registered host>"
                    android:pathPrefix="<registered path>"
                    android:scheme="https" />
            </intent-filter>
        </activity>
```

eBay currently supports only `https` redirect URI to obtain user consent. To seamlessly navigate back to your application, please create App Links by uploading Asset json file for registered Redirect URI. Details about App Links on Android can be found in [Verify App Links](https://developer.android.com/training/app-links/verify-site-associations.html)


# Get User Consent 
To obtain a user's consent, In your app, use `performUserAuthorization` method from an activity or fragment. This method can only be called from UI Thread.
 
```
OAuthService.performUserAuthorization()
```

When no parameters are provided, default values provided under (Application Setup)[Application_setup] is used. 

To explicitly define your configuration and parameters use 

```
OAuthService.performUserAuthorization(
        <activity request code>,
        <ApiConfiguration>,
        <state, opaque value used to maintain state>,
        <ApiEnvironment = ApiEnvironment.PRODUCTION>,
        <GrantType = GrantType.AUTHORIZATION_CODE>)
``` 
 
`performUserAuthorization` method validates and initiates OAuth request to eBay web or native app. Users login to eBay and consent for your app to act on their behalf. Results of consent is provided in Activity.OnActivityResult() of your calling activity.


To obtain the `authorization_code` from the user consent use, override `OnActivityResult` as shown below 

```
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == OAuthService.OAUTH_REQUEST_CODE) {
            
            // Obtain any error from user consent
            val error = oAuthService.getAuthorizationError(data)

            // Obtain authorization code from user consent
            val code = oAuthService.getAuthorizationResponse(data)
            
        }
    }
```
 
Provide the `authorization_code` to your backend service to generate the user token with client secret as described in [Exchanging the authorization code for a User access token]( https://developer.ebay.com/api-docs/static/oauth-auth-code-grant-request.html) guide. 

# How to Use Sample Mobile Application
This code contains a Sample folder containing a sample mobile application. 

To get the sample working, there are two steps:

1. Open MainActivity.kt and edit this line of code with the application credential that you obtained from the developer portal:

```
                apiConfiguration = ApiConfiguration(
                    "<client id>",
                    "<redirect uri>",
                    "scope_1" + " " + "scope_2"
                )
```
2. Open AndroidManifest.xml and edit the redirect_uri with registered redirect_uri from developer portal
```
        <activity
            android:name="com.ebay.api.client.auth.oauth2.ui.OAuthRedirectActivity"
            android:exported="true"
            tools:node="replace">
            <intent-filter>
                <action android:name="android.intent.action.VIEW" />

                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />

                <data
                    android:host="<registered host>"
                    android:pathPrefix="<registered path>"
                    android:scheme="https" />
            </intent-filter>
```
                
At that stage, you should be able to build and deploy the sample to an Android Emulator or Android phone, and the sample will allow a user to login to eBay and obtain user consent. 


# Contributions
Contributions in terms of patches, features, or comments are always welcome. Refer to CONTRIBUTING for guidelines. Submit Github issues for any feature enhancements, bugs, or documentation problems as well as questions and comments.

# License
Copyright (c) 2019 eBay Inc. <BR>
Architect/Developer(s): Sangeetha Rao <BR>

Use of this source code is governed by a Apache 2.0 license that can be found in the LICENSE file or at https://opensource.org/licenses/Apache-2.0.
