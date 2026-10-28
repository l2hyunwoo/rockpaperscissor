plugins {
    base
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.android.application) apply false
}

val ktlint = configurations.create("ktlint") {
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
        attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.SHADOWED))
    }
}
repositories { mavenCentral() }
dependencies { ktlint(libs.ktlint.cli) }

val kotlinSources = fileTree(rootDir) {
    include("**/*.kt", "**/*.kts")
    exclude("**/build/**", "**/node_modules/**", "**/.gradle/**", "**/.kotlin/**", "**/.yarn/**", "**/vendor/**", "packages/create-ait-cmp-app/templates/**")
}

for ((taskName, format) in listOf("ktlintCheck" to false, "ktlintFormat" to true)) {
    tasks.register<JavaExec>(taskName) {
        group = "verification"
        description = if (format) "Format Kotlin sources and Gradle scripts" else "Lint Kotlin sources and Gradle scripts"
        classpath = ktlint
        mainClass.set("com.pinterest.ktlint.Main")
        if (format) args("--format")
        args(kotlinSources.files.sorted().map { it.absolutePath })
        inputs.files(kotlinSources)
        inputs.file(".editorconfig")
    }
}
tasks.named("check") { dependsOn("ktlintCheck") }
