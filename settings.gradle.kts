pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "rockpaperscissor"
include(":shared-core", ":shared-ui", ":appsInTossApp")
if (file("webApp").isDirectory) include(":webApp")
if (file("androidApp").isDirectory) include(":androidApp")
