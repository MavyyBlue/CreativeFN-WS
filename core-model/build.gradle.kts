plugins {
    `java-library`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}
kotlin {
    jvmToolchain(21)
    compilerOptions { jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17 }
}
dependencies { testImplementation(libs.junit) }
dependencies { api(libs.serialization.json); api(libs.coroutines.core) }

tasks.withType<JavaCompile>().configureEach { options.release = 17 }
