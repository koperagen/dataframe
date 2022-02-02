@file:Suppress("UnstableApiUsage")

rootProject.name = "dataframe"
enableFeaturePreview("VERSION_CATALOGS")

includeBuild("generator")
include("plugins:dataframe-gradle-plugin")
include("plugins:symbol-processor")

include("examples:idea-examples:titanic")
include("examples:idea-examples:movies")

val jupyterApiTCRepo: String by settings

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven(jupyterApiTCRepo)
    }
}

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        maven(url = "https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev/")
    }
}

