plugins {
    application
    kotlin("jvm")
//    kotlin("plugin.dataframe")
}

repositories {
    mavenCentral()
}

application.mainClass.set("org.jetbrains.kotlinx.dataframe.examples.titanic.ml.TitanicKt")

dependencies {
    implementation(project(":"))
    implementation("org.jetbrains.kotlinx:kotlin-deeplearning-api:0.3.0")
    implementation("org.jetbrains.kotlinx:kotlin-deeplearning-dataset:0.3.0")
}

// Make IDE aware of the generated code:
kotlin.sourceSets.getByName("main").kotlin.srcDir("build/generated/ksp/main/kotlin/")

//dataframes {
//    schema {
//        data = "src/main/resources/titanic.csv"
//        name = "org.jetbrains.kotlinx.dataframe.examples.titanic.ml.Passenger"
//        csvOptions {
//            delimiter = ';'
//        }
//    }
//}
