val sharedStore = providers.gradleProperty("researchWidgets.keystore").orNull ?: System.getenv("RESEARCH_WIDGETS_KEYSTORE")
val sharedStorePassword = providers.gradleProperty("researchWidgets.keystorePassword").orNull ?: System.getenv("RESEARCH_WIDGETS_KEYSTORE_PASSWORD")
val sharedKeyAlias = providers.gradleProperty("researchWidgets.keyAlias").orNull ?: System.getenv("RESEARCH_WIDGETS_KEY_ALIAS")
val sharedKeyPassword = providers.gradleProperty("researchWidgets.keyPassword").orNull ?: System.getenv("RESEARCH_WIDGETS_KEY_PASSWORD")

plugins { alias(libs.plugins.android.application) }

android {
    namespace = "com.placeholder2312.researchwidgets"
    compileSdk = 37
    defaultConfig {
        applicationId = "com.placeholder2312.researchwidgets"
        minSdk = 26
        targetSdk = 37
        versionCode = 5
        versionName = "1.3.1"
    }
    signingConfigs {
        if (sharedStore != null) {
            create("sharedIpc") {
                storeFile = file(sharedStore)
                storePassword = requireNotNull(sharedStorePassword) { "RESEARCH_WIDGETS_KEYSTORE_PASSWORD is required with the shared keystore" }
                keyAlias = requireNotNull(sharedKeyAlias) { "RESEARCH_WIDGETS_KEY_ALIAS is required with the shared keystore" }
                keyPassword = requireNotNull(sharedKeyPassword) { "RESEARCH_WIDGETS_KEY_PASSWORD is required with the shared keystore" }
                enableV1Signing = false
                enableV2Signing = true
                enableV3Signing = true
            }
        }
    }
    buildTypes {
        debug { if (sharedStore != null) signingConfig = signingConfigs.getByName("sharedIpc") }
        release { if (sharedStore != null) signingConfig = signingConfigs.getByName("sharedIpc") }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin { compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) } }
    testOptions { unitTests.isIncludeAndroidResources = true; unitTests.all { it.testLogging { showStandardStreams = true } } }
}
dependencies { testImplementation(libs.junit); testImplementation(libs.robolectric); testImplementation(libs.androidx.test.core) }
