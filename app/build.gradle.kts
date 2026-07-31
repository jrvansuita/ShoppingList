import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.gms.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

android {
    namespace = "br.com.activity"
    compileSdk = 36

    defaultConfig {
        applicationId = "br.com.activity"
        minSdk = 23
        targetSdk = 36
        versionCode = 40
        versionName = "1.6"
    }

    signingConfigs {
        create("release") {
            val props = Properties()
            val localPropsFile = rootProject.file("signing.properties")
            if (localPropsFile.exists()) {
                localPropsFile.inputStream().use { props.load(it) }
            }
            storeFile = file(props.getProperty("RELEASE_STORE_FILE") ?: "")
            storePassword = props.getProperty("RELEASE_STORE_PASSWORD") ?: ""
            keyAlias = props.getProperty("RELEASE_KEY_ALIAS") ?: ""
            keyPassword = props.getProperty("RELEASE_KEY_PASSWORD") ?: ""
        }
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            buildConfigField("boolean", "ADS_ENABLED", "true")
        }
        create("withoutAds") {
            initWith(getByName("debug"))
            buildConfigField("boolean", "ADS_ENABLED", "false")
            applicationIdSuffix = ".withoutads"
            versionNameSuffix = "-withoutads"
        }
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            buildConfigField("boolean", "ADS_ENABLED", "true")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
        baseline = file("lint-baseline.xml")
    }

    bundle {
        density {
            // Ship every density in one APK, instead of one split per density.
            //
            // The drawables of this app stop at xxhdpi, and none of them has a
            // default in res/drawable. A device that installs only one density
            // split can then miss a drawable, and the theme fails to read the
            // action bar icon while the window builds. That crash happens
            // before onCreate returns, so the app cannot start.
            //
            // The drawable folders hold about 276 KB in total, so the extra
            // download is small next to the 9.4 MB bundle.
            enableSplit = false
        }
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.kotlin.stdlib)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics.ndk)
    implementation(libs.firebase.analytics)
    implementation(libs.gms.ads)
    implementation(libs.billing)
}
