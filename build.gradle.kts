group = "cn.xihan"
version = "1.0-SNAPSHOT"

buildscript {
    val appVersionName by extra("1.0.0")
    val appVersionCode by extra(100)
    val minSdkVersion by extra(21)
    val targetSdkVersion by extra(33)
    val coreVersion by extra("1.9.0")
    val appcompatVersion by extra("1.7.0-alpha01")
    val materialVersion by extra("1.8.0-rc01")
    val material3Version by extra("1.1.0-alpha03")
    val activityVersion by extra("1.7.0-alpha03")
    val lifecycleVersion by extra("2.6.0-alpha04")
    val roomVersion by extra("2.5.0")
    val startVersion by extra("1.2.0-alpha02")
    val kotlinJsonVersion by extra("1.4.1")
    val mmkvVersion by extra("1.2.15")
    val coroutinesVersion by extra("1.6.4")
    val workRuntimeVersion by extra("2.8.0-rc01")
    val okhttpVersion by extra("5.0.0-SNAPSHOT")
    val dialogVersion by extra("0.0.47")
    val landscapistVersion by extra("2.1.1")
    val coilVersion by extra("2.2.2")
    val composeViewsVersion by extra("1.3.5")
    val koinVersion by extra("3.3.2")
    val accompanistVersion by extra("0.28.0")
}

allprojects {
    repositories {
        maven("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/")
        maven("https://s01.oss.sonatype.org/content/repositories/releases")
        maven("https://maven.aliyun.com/repository/google")
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://jitpack.io")
    }
}

plugins {
    kotlin("multiplatform") apply false
    kotlin("android") apply false
    kotlin("kapt") apply false
    kotlin("jvm") apply false
    kotlin("plugin.serialization") apply false
    id("com.android.application") apply false
    id("com.android.library") apply false
    id("org.jetbrains.compose") apply false
    id("com.google.devtools.ksp") apply false
}