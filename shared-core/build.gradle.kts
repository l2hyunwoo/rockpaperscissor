import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
}
val hasAndroid = rootProject.file("androidApp").isDirectory
val hasIos = rootProject.file("iosApp").isDirectory
if (hasAndroid) pluginManager.apply("com.android.kotlin.multiplatform.library")

kotlin {
    targets.withType<KotlinMultiplatformAndroidLibraryTarget>().configureEach {
        namespace = "io.github.l2hyunwoo.rps.core"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = 24
        compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) }
    }
    if (hasIos) {
        iosArm64()
        iosSimulatorArm64()
    }
    jvm()
    js {
        browser()
        binaries.library()
        generateTypeScriptDefinitions()
    }
    wasmJs { browser() }
    sourceSets {
        commonMain.dependencies { implementation(libs.serialization.json) }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.coroutines.test)
        }
    }
}
