This document describes the steps to create a new release for this Library.  <The release process includes publishing the new updated artifacts to central m>aven repository and also keeping github in sync with the published release.    


# Update the release version
The current version of library is provided by `VERSION_NAME` variable in [gradle.properties](https://github.com/eBay/ebay-oauth-android-client/blob/master/gradle.properties). The library follow semantic versioning. Please update `VERSION_NAME` with updated minor or major version depending on the changes.


# Upload AAR to Maven
This repository is already configured to upload the artifacts to Maven from Sonatype repository management software. To upload new release to maven, one can simply run the provided gradle task, `UploadArchives`. This task is set up to upload artifacts to maven group and artifact ID as provided in [gradle.properties](https://github.com/eBay/ebay-oauth-android-client/blob/master/gradle.properties). This gradle task also uses properties that are local to the user like Sonatype and GPG signing. Listed below are steps to set up Sonatype account and create GPG signing keys.  

## Create Sonatype Account
To upload to Maven from Sonatype, you must have a Sonatype userID which has been authorized to access maven group `com.ebay.auth`. 

If you require to create a new Sonatype account follow these simple steps  

- Create a new [Sonatype Jira account](https://issues.sonatype.org/secure/Signup!default.jspa)
- Create a new ticket [requesting access to group](https://issues.sonatype.org/secure/CreateIssue.jspa?issuetype=21&pid=10134)
 
## Create GPG key
Every artifact that is deployed to Sonatype should be signed. The official document to generate keys can be found [here](http://blog.sonatype.com/2010/01/how-to-generate-pgp-signatures-with-maven/).

to brief, If you don't have a GPG key, create one using 

```
# generate keys if they don't exist yet
gpg2 --gen-key
```

Upload the generated key to public server, which will be used by Sonatype to verify the pushed artifacts. Note that UI based tools like GPG Keychain tool publishes to public server but takes time to sync the key with different servers. It is more foolproof to push using command below.

```
# list keys on the machine
gpg2 --list-keys

# upload to a key server; I used keyserver.ubuntu.com
gpg2 --send-keys --keyserver keyserver.ubuntu.com <KEY ID>

```

Export the key to file which will be later referenced in gradle.properties 
```
# I had my keys in backup, so I had to export them:
gpg2 --import path_to_keys/secret-keys.gpg
```


## Update local Gradle properties
Upon creation of Sonatype account and GPG keys, these values need to be provided to `UploadArchives` gradle task as gradle properties. Since these are private data, these values should be set only in local gradle.properties file.  

```
signing.keyId=<GPG KEY ID>
signing.password=<KEY Passphrase>
signing.secretKeyRingFile=<File path_to_keys>/secret-keys.gpg
sonatypeUsername=<SONATYPE USER NAME>
sonatypePassword=<SONATYPE PASSWORD or ACCESS TOKEN>
``` 

## Upload artifacts to Sonatype staging


To upload archives to Sonatype staging, simply run the gradle task as shown below: 

```
./gradlew :oauth2:uploadArchives
```

This task should create the release aar and upload the artifacts to Sonatype's staging repository.

## Releasing artifact to Maven from Sonatype
Detailed instructions to promote an artifact from staging to release is provided in [Sontaype help document](https://help.sonatype.com/repomanager2/staging-releases/managing-staging-repositories). 
 
To summarize, the steps are: 
- Go to Sonatype [Repository Manager](https://oss.sonatype.org) and login with the same credentials used to upload the artifacts. 
- locate the artifact that was uploaded. A simple search by group `com.ebay.auth` is helpful.
- select and `close` the open staging repository to proceed to release. This step also runs validations and actions to promote the staging repository to release. 
- Once all tasks for 'close` are completed, use `release` button to release artifacts to hosted repository


Note that the latest artifacts takes a couple of hours to show up in the maven search index. The dependency update and download should work instantly.   

# Create a Release in Github
Now that new artifacts is released to Maven, Github should be updated to reflect the same.

## Update Readme file

Update the [Readme.md](https://github.com/eBay/ebay-oauth-android-client/blob/master/Readme.md) to indicate the latest available release version in Maven repository. 


## Tag release history
Commit changes to readme file and tag the branch for release using [git tag commands](https://git-scm.com/book/en/v2/Git-Basics-Tagging). Tag should have the same version name as specified b VERSION_NAME in gradle.properties file. 

use the following command to push tags to remote server 
```
git push origin <Version number>
```

## Create release

Create a release branch by following instruction provided in [Github guide] (https://help.github.com/en/github/administering-a-repository/creating-releases). Use the same tag created above to create a release branch. Do not forget to upload aar file. This can be downloaded from the released version in [Sonatype release manager](https://oss.sonatype.org/)



The release to Maven repository and Github source control is now complete. Github repository should be open for additional changes. 