import java.io.FileInputStream
import java.util.*

plugins {
    id("org.jetbrains.compose")
    id("com.android.application")
    kotlin("android")
    kotlin("plugin.serialization")
    id("com.google.devtools.ksp")
}

group "cn.xihan"
version "1.0-SNAPSHOT"

dependencies {
    implementation(project(":common"))

    implementation("androidx.activity:activity-compose:${rootProject.extra["activityVersion"]}")
    implementation("androidx.lifecycle:lifecycle-runtime:${rootProject.extra["lifecycleVersion"]}")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:${rootProject.extra["lifecycleVersion"]}")
    implementation("androidx.lifecycle:lifecycle-common-java8:${rootProject.extra["lifecycleVersion"]}")
//    implementation("androidx.room:room-runtime:${rootProject.extra["roomVersion"]}")
//    implementation("androidx.room:room-ktx:${rootProject.extra["roomVersion"]}")
//    implementation("androidx.startup:startup-runtime:${rootProject.extra["startVersion"]}")
//    implementation("androidx.work:work-runtime:${rootProject.extra["workRuntimeVersion"]}")
//    implementation("androidx.work:work-runtime-ktx:${rootProject.extra["workRuntimeVersion"]}")
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
keystoreProperties.load(FileInputStream(keystorePropertiesFile))

android {

    namespace = "cn.xihan.cloudsync"

    compileSdk = rootProject.extra["targetSdkVersion"] as Int

    signingConfigs {
        create("xihantest") {
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
            enableV3Signing = true
            enableV4Signing = true
        }
    }

    defaultConfig {
        minSdk = rootProject.extra["minSdkVersion"] as Int
        targetSdk = rootProject.extra["targetSdkVersion"] as Int
        versionCode = rootProject.extra["appVersionCode"] as Int
        versionName = rootProject.extra["appVersionName"] as String

//        ksp {
//            arg("room.schemaLocation", "$projectDir/schemas")
//            arg("room.incremental", "true")
//            arg("room.expandProjection", "true")
//        }

        signingConfig = signingConfigs.getByName("xihantest")

    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildTypes {
        getByName("release") {
            isDebuggable = false
            isJniDebuggable = false
            isMinifyEnabled = false
            isPseudoLocalesEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }

    kotlinOptions.jvmTarget = "11"

    lint.abortOnError = false

    packagingOptions {
        resources.excludes += mutableSetOf(
            "META-INF/*******",
            "**/*.txt",
            "**/*.xml",
            "**/*.properties",
            "DebugProbesKt.bin",
            "kotlin-tooling-metadata.json"
        )

        dex.useLegacyPackaging = true
    }

}