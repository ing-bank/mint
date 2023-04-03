# 🌿 [MINT](/README.md) | [Demo](demo.md) | [Features](manual.md) | [Contributing](contributing.md) | Get Started

Here we will explain you on how to use MINT in your Android app. If you are looking on how to contribute to MINT itself, please see [this document](contributing.md). 

## Setting up MINT
The steps you need to do in order to configure MINT are the following:    

### Configure the MINT dependency in your project:
Configuration for the MINT test dependency in the application module's build.gradle

```gradle
dependencies {
    def mint_version = 'x.y.z-hash'
    androidTestImplementation "org.mint:android:${mint_version}"
}
```

### Android Test Orchestrator
***Optional*** (but recommended): configure the [Android Test Orchestrator](https://developer.android.com/training/testing/instrumented-tests/androidx-test-libraries/runner#use-android) to make sure there is test isolation.

### MINT Gradle plugin
We have created a MINT gradle plugin that supports you in working with MINT.

#### Resolving the Mint plugin
Configuration for the MINT plugin in the top-level build.gradle:

```gradle
buildscript {
    dependencies {
        def mint_plugin_version = 'x.y.z-hash'
        classpath "org.mint.tooling:mint-gradle-plugin:${mint_plugin_version}"
    }
}
```

Configuration for the MINT plugin dependency in the application module's build.gradle. Add this as much to the top as possible to prevent conflicts with other plugins. 

```gradle
apply plugin: 'mint-tooling'

mintTooling {
    // mandatory, the application's package name
    packageName "org.example.app.debug"
    // optional, the location where MINT output will be stored
    targetDir "${project.buildDir}/mint-reports/"
}
```

In your `android` block in the application module's build.gradle, add an exclusion for META-INF/DEPENDENCIES:

```gradle
android {
    packagingOptions {
        exclude 'META-INF/DEPENDENCIES'
    }
}
```

* Create a MINT report based on newly executed tests
   `./gradlew mintReport`

* Just create a MINT report based on historic test data
   `./gradlew mintReport -x connectedDebugAndroidTest`

* Just create a MINT report based on historic test data at specified location
   `./gradlew mintReport -x connectedDebugAndroidTest --target build/mint/tmp --no-pull`

* Collect historic MINT data from the device (at the location specified by 'targetDir', if configured)
   `./gradlew collectReportingData`

* Clean any MINT data on the test device
   `./gradlew mintClean`

### Creating your first test
```kotlin
package org.mint.exampleapp

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mint.MINTRule

@RunWith(AndroidJUnit4::class)
class ExampleMintTest  {
    @Rule
    @JvmField
    var activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @Rule
    @JvmField
    var mint = MINTRule()

    @Test
    fun mintExampleTestRun() {
        mint.explore()
    }
}
```

Or, when you want to execute some manual step(s) before starting to explore (i.e. a hybrid test setup of Espresso and MINT):
```kotlin
    @Test
    fun mintExampleTestRun() {
        mint.step {
            onView(withId(R.id.notes_button)).perform(click())
            onView(withId(R.id.add_note_button)).check(matches(isNotEnabled()))
        }.step {
            onView(withId(R.id.add_note_title_edit_text)).perform(typeText("title1"))
            onView(withId(R.id.add_note_button)).check(matches(isEnabled()))
        }.step {
            onView(withId(R.id.add_note_description_edit_text)).perform(typeText("desc"))
            onView(withId(R.id.add_note_button)).perform(click())
        }.explore()
```
