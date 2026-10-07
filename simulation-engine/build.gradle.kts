plugins { alias(libs.plugins.kotlin.jvm) }
kotlin {
    jvmToolchain(21)
    compilerOptions { jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17 }
}
dependencies { testImplementation(libs.junit) }
dependencies { implementation(project(":core-model")) }

tasks.withType<JavaCompile>().configureEach { options.release = 17 }
