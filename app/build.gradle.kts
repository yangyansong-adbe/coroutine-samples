plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.yy.coroutine"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.yy.coroutine"
        minSdk = 34
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(project(":sdk"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.kotlinx.coroutines.core)
    implementation(platform("com.adobe.marketing.mobile:sdk-bom:3.7.0"))
    implementation("com.adobe.marketing.mobile:edgeidentity"){
        exclude("com.adobe.marketing.mobile", "core")
    }
    implementation("com.adobe.marketing.mobile:userprofile"){
        exclude("com.adobe.marketing.mobile", "core")
    }
    implementation("com.adobe.marketing.mobile:lifecycle"){
        exclude("com.adobe.marketing.mobile", "core")
    }
    implementation("com.adobe.marketing.mobile:assurance"){
        exclude("com.adobe.marketing.mobile", "core")
    }
    implementation("com.adobe.marketing.mobile:optimize"){
        exclude("com.adobe.marketing.mobile", "core")
        exclude("com.adobe.marketing.mobile", "edge")
    }
    implementation("com.adobe.marketing.mobile:messaging"){
        exclude("com.adobe.marketing.mobile", "core")
        exclude("com.adobe.marketing.mobile", "edge")
    }
    implementation("com.adobe.marketing.mobile:edgeconsent"){
        exclude("com.adobe.marketing.mobile", "core")
        exclude("com.adobe.marketing.mobile", "edge")
    }
}