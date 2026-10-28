import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose)
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
}
val hasAndroid = rootProject.file("androidApp").isDirectory
val hasIos = rootProject.file("iosApp").isDirectory
if (hasAndroid) pluginManager.apply("com.android.kotlin.multiplatform.library")

kotlin {
    targets.withType<KotlinMultiplatformAndroidLibraryTarget>().configureEach {
        namespace = "io.github.l2hyunwoo.rps.ui"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = 24
        compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) }
        androidResources { enable = true }
    }
    if (hasIos) {
        iosArm64()
        iosSimulatorArm64()
    }
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().configureEach {
        binaries.framework {
            baseName = "SharedUi"
            isStatic = true
        }
    }
    js { browser() }
    wasmJs { browser() }
    sourceSets {
        commonMain.dependencies {
            api(project(":shared-core"))
            implementation(libs.compose.resources)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.coroutines.core)
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "io.github.l2hyunwoo.rps.resources"
}
