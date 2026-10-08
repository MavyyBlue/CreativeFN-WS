plugins {
    `java-library`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}
kotlin {
    jvmToolchain(21)
    compilerOptions { jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17 }
}
tasks.withType<JavaCompile>().configureEach { options.release = 17 }
dependencies { api(project(":device-catalog")); testImplementation(libs.junit) }
