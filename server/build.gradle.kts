plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("io.ktor.plugin")
}

group = "cn.xihan"
version = "0.0.1"

application {
    mainClass.set("cn.xihan.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    maven("https://maven.aliyun.com/repository/google")
    maven("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/")
    maven("https://s01.oss.sonatype.org/content/repositories/releases")
    mavenCentral()
}

val ktorVersion = extra["ktor.version"] as String

dependencies {

    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-auto-head-response-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-compression-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-network-tls-certificates:$ktorVersion")
    implementation("io.ktor:ktor-server-conditional-headers:$ktorVersion")

    implementation("ch.qos.logback:logback-classic:latest.release")

    val exposedVersion = "0.41.1"
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("mysql:mysql-connector-java:8.0.31")

    val tcnativeVersion = "2.0.56.Final"
    if (tcnativeClassifier != null) {
        implementation("io.netty:netty-tcnative-boringssl-static:$tcnativeVersion:windows-x86_64")
        implementation("io.netty:netty-tcnative-boringssl-static:$tcnativeVersion:linux-x86_64")
    } else {
        implementation("io.netty:netty-tcnative-boringssl-static:$tcnativeVersion")
    }

    testImplementation("io.ktor:ktor-server-tests-jvm:$ktorVersion")
    testImplementation(kotlin("test"))
}

val osName = System.getProperty("os.name").toLowerCase()
val tcnativeClassifier = when {
    osName.contains("win") -> "windows-x86_64"
    osName.contains("linux") -> "linux-x86_64"
    osName.contains("mac") -> "osx-x86_64"
    else -> null
}