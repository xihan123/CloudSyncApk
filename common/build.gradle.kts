plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("org.jetbrains.compose")
    id("com.android.library")
    id("com.google.devtools.ksp")
}

group = "cn.xihan"
version = "1.0-SNAPSHOT"

val ktorfitVersion = extra["ktorfit.version"] as String
val ktorVersion = extra["ktor.version"] as String

kotlin {
    android {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
    }
    jvm("desktop") {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(compose.runtime)
                api(compose.foundation)
                api(compose.animation)
                //api(compose.material)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                api(compose.material3)
                api(compose.ui)
                api(compose.uiTooling)
                api(compose.preview)
                api(compose.materialIconsExtended)


                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${rootProject.extra["coroutinesVersion"]}")
                api("org.jetbrains.kotlinx:kotlinx-serialization-core:${rootProject.extra["kotlinJsonVersion"]}")
                api("org.jetbrains.kotlinx:kotlinx-serialization-json:${rootProject.extra["kotlinJsonVersion"]}")
                api("org.jetbrains.kotlinx:kotlinx-serialization-json-okio:${rootProject.extra["kotlinJsonVersion"]}")
                api("io.ktor:ktor-client-core:$ktorVersion")
                api("io.ktor:ktor-client-okhttp:$ktorVersion")
                api("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                api("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
                // https://github.com/Foso/Ktorfit
                api("de.jensklingenberg.ktorfit:ktorfit-lib:$ktorfitVersion") {
                    exclude(group = "io.ktor")
                }
                // https://github.com/Tlaster/PreCompose
                api("moe.tlaster:precompose:1.3.13")
                // https://github.com/ltttttttttttt/ComposeViews/blob/main/README_CN.md
                api("com.github.ltttttttttttt.ComposeViews:core:${rootProject.extra["composeViewsVersion"]}")
                // https://github.com/qdsfdhvh/compose-imageloader
//                api("io.github.qdsfdhvh:image-loader:1.2.7") {
//                    exclude(group = "io.coil-kt")
//                }
//                api("io.github.qdsfdhvh:image-loader-batik:1.2.7") {
//                    exclude(group = "io.coil-kt")
//                }
                // https://github.com/russhwolf/multiplatform-settings
                api("com.russhwolf:multiplatform-settings-no-arg:1.0.0")

                api("io.coil-kt:coil:${rootProject.extra["coilVersion"]}")
                api("com.squareup.okhttp3:logging-interceptor:${rootProject.extra["okhttpVersion"]}")

                api("io.insert-koin:koin-core:${rootProject.extra["koinVersion"]}")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidMain by getting {
            dependencies {
                api("androidx.core:core-ktx:${rootProject.extra["coreVersion"]}")
                api("androidx.appcompat:appcompat:${rootProject.extra["appcompatVersion"]}")
                api("com.google.android.material:material:${rootProject.extra["materialVersion"]}")
                api("com.google.accompanist:accompanist-systemuicontroller:${rootProject.extra["accompanistVersion"]}")
                api("com.google.accompanist:accompanist-themeadapter-material3:${rootProject.extra["accompanistVersion"]}")
                api("com.google.accompanist:accompanist-flowlayout:${rootProject.extra["accompanistVersion"]}")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-android:${rootProject.extra["coroutinesVersion"]}")
                // https://github.com/ltttttttttttt/ComposeViews/blob/main/README_CN.md
                api("com.github.ltttttttttttt.ComposeViews:maven_android:${rootProject.extra["composeViewsVersion"]}")
                // https://github.com/kongzue/DialogX/wiki
                api("com.github.kongzue.DialogX:DialogX:${rootProject.extra["dialogVersion"]}") {
                    exclude(group = "com.android.support")
                    exclude(group = "com.google.android.material")
                }
                api("com.github.kongzue.DialogX:DialogXMaterialYou:${rootProject.extra["dialogVersion"]}") {
                    exclude(group = "com.android.support")
                    exclude(group = "com.google.android.material")
                }
                // https://github.com/skydoves/Landscapist#coil
                api( "com.github.skydoves:landscapist-coil:2.1.1"){
                    exclude(group = "io.coil-kt")
                }
                // https://github.com/getActivity/XXPermissions
                api("com.github.getActivity:XXPermissions:latest.release") {
                    exclude(group = "com.android.support")
                }
                // https://dylancaicoding.github.io/Longan/#/
                api("com.github.DylanCaiCoding.Longan:longan:latest.release")
                // https://github.com/liangjingkanji/Channel
                api("com.github.liangjingkanji:Channel:latest.release")
                api("io.insert-koin:koin-android:${rootProject.extra["koinVersion"]}")
                api("io.insert-koin:koin-androidx-compose:3.4.1")
            }
        }
        val desktopMain by getting {
            dependencies {
                // https://github.com/ltttttttttttt/ComposeViews/blob/main/README_CN.md
                api("com.github.ltttttttttttt.ComposeViews:maven_desktop:${rootProject.extra["composeViewsVersion"]}")
            }
        }
        val desktopTest by getting
    }
}

android {
    compileSdk = rootProject.extra["targetSdkVersion"] as Int
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = rootProject.extra["minSdkVersion"] as Int
        targetSdk = rootProject.extra["targetSdkVersion"] as Int
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    add("kspCommonMainMetadata", "de.jensklingenberg.ktorfit:ktorfit-ksp:$ktorfitVersion")
    add("kspDesktop", "de.jensklingenberg.ktorfit:ktorfit-ksp:$ktorfitVersion")
    add("kspAndroid", "de.jensklingenberg.ktorfit:ktorfit-ksp:$ktorfitVersion")
    //add("kspAndroid", "androidx.room:room-compiler:${rootProject.extra["roomVersion"]}")
}