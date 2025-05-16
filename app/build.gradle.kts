import java.util.Properties
import com.android.build.api.dsl.Packaging

// Load local.properties once
val localProperties = Properties().apply {
    val localPropsFile = rootProject.file("local.properties")
    if (localPropsFile.exists()) {
        load(localPropsFile.inputStream())
    }
}

val sendgridApiKey = localProperties.getProperty("SENDGRID_API_KEY") ?: ""
val openaiApiKey = localProperties.getProperty("OPENAI_API_KEY") ?: ""

fun escapeForJavaString(str: String): String =
    str.replace("\\", "\\\\").replace("\"", "\\\"")

val escapedSendgridKey = escapeForJavaString(sendgridApiKey)
val escapedOpenaiKey = escapeForJavaString(openaiApiKey)

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.cuoi"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.cuoi"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "0.4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    packaging {
        resources {
            excludes += "META-INF/DEPENDENCIES"
            excludes += "mozilla/public-suffix-list.txt"
        }
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        debug {
            buildConfigField("String", "SENDGRID_API_KEY", "\"$escapedSendgridKey\"")
            buildConfigField("String", "OPENAI_API_KEY", "\"$escapedOpenaiKey\"")
        }
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("String", "SENDGRID_API_KEY", "\"$escapedSendgridKey\"")
            buildConfigField("String", "OPENAI_API_KEY", "\"$escapedOpenaiKey\"")
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.common.ktx)
    implementation(libs.firebase.crashlytics.buildtools)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.gson)
    implementation(libs.sendgrid.java)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.androidx.security.crypto)
    implementation(libs.okhttp)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.analytics.ktx)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.androidx.browser)
    implementation(platform(libs.firebase.bom.v3270))
    implementation(libs.androidx.recyclerview)
    implementation(libs.material)
}

apply(plugin = "com.google.gms.google-services")
