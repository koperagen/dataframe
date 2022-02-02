package org.jetbrains.kotlinx.dataframe.examples.movies

import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.annotations.DataSchema
import org.jetbrains.kotlinx.dataframe.api.*
import org.jetbrains.kotlinx.dataframe.io.read

@DataSchema
interface Movie{
    val movieId: String
    val title: String
    val genres: String
}

private const val pathToCsv = "examples/idea-examples/movies/src/main/resources/movies.csv"

fun main() {
//    DataFrame
//        .read(pathToCsv).convertTo<Movie>()
//        .split { genres }.by("|").inplace()
//        .split { title }.by {
//            listOf(
//                """\s*\(\d{4}\)\s*$""".toRegex().replace(it, ""),
//                "\\d{4}".toRegex().findAll(it).lastOrNull()?.value?.toIntOrNull() ?: -1
//            )
//        }.into("title", "year")
//        .explode("genres")
//        .filter { "year"<Int>() >= 0 && genres != "(no genres listed)" }
//        .groupBy("year")
//        .sortBy("year")
//        .pivot("genres", inward = false)
//        .aggregate {
//            count() into "count"
//            mean() into "mean"
//        }.print(10)
}
