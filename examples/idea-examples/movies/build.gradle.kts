import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm")
//    kotlin("plugin.dataframe")
}

repositories {
    mavenCentral()
}

kotlin.sourceSets.getByName("main").kotlin.srcDir("build/generated/ksp/main/kotlin/")

application.mainClass.set("org.jetbrains.kotlinx.dataframe.examples.movies.MoviesWithDataClassKt")

dependencies {
    implementation(project(":"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

