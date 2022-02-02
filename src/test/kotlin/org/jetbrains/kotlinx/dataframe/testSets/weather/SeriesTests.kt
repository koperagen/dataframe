package org.jetbrains.kotlinx.dataframe.testSets.weather

import io.kotest.matchers.shouldBe
import org.jetbrains.kotlinx.dataframe.annotations.DataSchema
import org.jetbrains.kotlinx.dataframe.api.add
import org.jetbrains.kotlinx.dataframe.api.cast
import org.jetbrains.kotlinx.dataframe.api.concat
import org.jetbrains.kotlinx.dataframe.api.dataFrameOf
import org.jetbrains.kotlinx.dataframe.api.diff
import org.jetbrains.kotlinx.dataframe.api.groupBy
import org.jetbrains.kotlinx.dataframe.api.movingAverage
import org.jetbrains.kotlinx.dataframe.api.rows
import org.jetbrains.kotlinx.dataframe.api.sortBy
import org.junit.Test

class SeriesTests {

    val df = dataFrameOf("city", "day", "temp")(
        "Moscow", 1, 14,
        "London", 1, 10,
        "Moscow", 3, 18,
        "London", 3, 16,
        "Moscow", 6, 16,
        "London", 6, 23,
        "Moscow", 4, 13,
        "London", 4, 22,
        "Moscow", 2, 20,
        "London", 2, 15,
        "Moscow", 5, 10,
        "London", 5, 18
    )

    // Generated code

//    @DataSchema
//    interface Weather {
//        val city: String
//        val day: Int
//        val temp: Int
//    }

//    val typed = df.cast<Weather>()

//    @Test
//    fun `diff test`() {
//        val withDiff = typed
//            .sortBy { city and day }
//            .groupBy { city }
//            .add("diff") { diff { temp } ?: 0 }
//            .concat()
//
//        val srcData = typed.rows().map { (it.city to it.day) to it.temp }.toMap()
//        val expected = typed.sortBy { city and day }.rows().map { row -> srcData[row.city to (row.day - 1)]?.let { row.temp - it } ?: 0 }
//        withDiff["diff"].toList() shouldBe expected
//    }

//    @Test
//    fun `movingAverage`() {
//        val k = 3
//        val withMa = typed
//            .groupBy { city }
//            .sortBy { city and day }
//            .add("ma_temp") { it.movingAverage(k) { it.temp } }
//            .concat()
//
//        val srcData = typed.rows().map { (it.city to it.day) to it.temp }.toMap()
//        val expected = typed
//            .sortBy { city and day }
//            .rows()
//            .map { row -> (0 until k).map { srcData[row.city to row.day - it] }.filterNotNull().let { it.sum().toDouble() / it.size } }
//
//        withMa["ma_temp"].toList() shouldBe expected
//    }
}
