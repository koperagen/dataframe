//package org.jetbrains.kotlinx.dataframe.testSets.person
//
//import io.kotest.matchers.shouldBe
//import org.jetbrains.kotlinx.dataframe.DataFrame
//import org.jetbrains.kotlinx.dataframe.annotations.DataSchema
//import org.jetbrains.kotlinx.dataframe.api.addId
//import org.jetbrains.kotlinx.dataframe.api.all
//import org.jetbrains.kotlinx.dataframe.api.append
//import org.jetbrains.kotlinx.dataframe.api.cast
//import org.jetbrains.kotlinx.dataframe.api.column
//import org.jetbrains.kotlinx.dataframe.api.count
//import org.jetbrains.kotlinx.dataframe.api.dataFrameOf
//import org.jetbrains.kotlinx.dataframe.api.distinct
//import org.jetbrains.kotlinx.dataframe.api.excludeJoin
//import org.jetbrains.kotlinx.dataframe.api.filter
//import org.jetbrains.kotlinx.dataframe.api.filterJoin
//import org.jetbrains.kotlinx.dataframe.api.fullJoin
//import org.jetbrains.kotlinx.dataframe.api.innerJoin
//import org.jetbrains.kotlinx.dataframe.api.leftJoin
//import org.jetbrains.kotlinx.dataframe.api.remove
//import org.jetbrains.kotlinx.dataframe.api.rightJoin
//import org.jetbrains.kotlinx.dataframe.api.select
//import org.jetbrains.kotlinx.dataframe.ncol
//import org.jetbrains.kotlinx.dataframe.nrow
//import org.junit.Test
//
//class JoinTests : BaseTest() {
//
//    val df2 = dataFrameOf("name", "origin", "grade", "age")(
//        "Alice", "London", 3, "young",
//        "Alice", "London", 5, "old",
//        "Bob", "Tokyo", 4, "young",
//        "Bob", "Paris", 5, "old",
//        "Charlie", "Moscow", 1, "young",
//        "Charlie", "Moscow", 2, "old",
//        "Bob", "Paris", 4, null
//    )
//
//// Generated Code
//
//    @DataSchema
//    interface Person2 {
//        val name: String
//        val origin: String?
//        val grade: Int
//    }
//
//    val typed2: DataFrame<Person2> = df2.cast()
//
//    @Test
//    fun `inner join`() {
//        val res = typed.innerJoin(typed2) { name and it.city.match(right.origin) }
//        res.ncol shouldBe 6
//        res.nrow shouldBe 7
//        res["age1"].hasNulls() shouldBe false
//        res.count { name == "Charlie" && city == "Moscow" } shouldBe 4
//        res.select { city and name }.distinct().nrow shouldBe 3
//        res[Person2::grade].hasNulls() shouldBe false
//    }
//
//    @Test
//    fun `left join`() {
//        val res = typed.leftJoin(typed2) { name and it.city.match(right.origin) }
//
//        res.ncol shouldBe 6
//        res.nrow shouldBe 10
//        res["age1"].hasNulls() shouldBe true
//        res.select { city and name }.distinct().nrow shouldBe 6
//        res.count { it["grade"] == null } shouldBe 3
//        res.age.hasNulls() shouldBe false
//    }
//
//    @Test
//    fun `right join`() {
//        val res = typed.rightJoin(typed2) { name and it.city.match(right.origin) }
//
//        res.ncol shouldBe 6
//        res.nrow shouldBe 9
//        res["age1"].hasNulls() shouldBe true
//        res.select { city and name }.distinct().nrow shouldBe 4
//        res[Person2::grade].hasNulls() shouldBe false
//        res.age.hasNulls() shouldBe true
//        val newEntries = res.filter { it["age"] == null }
//        newEntries.nrow shouldBe 2
//        newEntries.all { name == "Bob" && city == "Paris" && weight == null } shouldBe true
//    }
//
//    @Test
//    fun `outer join`() {
//        val res = typed.fullJoin(typed2) { name and it.city.match(right.origin) }
//        println(res)
//        res.ncol shouldBe 6
//        res.nrow shouldBe 12
//        res.name.hasNulls() shouldBe false
//        res.columns().filter { it != res.name }.all { it.hasNulls() } shouldBe true
//        res.select { city and name }.distinct().nrow shouldBe 7
//        val distinct = res.select { name and age and city and weight }.distinct()
//        val expected = typed.append("Bob", null, "Paris", null)
//        distinct shouldBe expected
//    }
//
//    @Test
//    fun `filter join`() {
//        val res = typed.filterJoin(typed2) { city.match(right.origin) }
//        val expected = typed.innerJoin(typed2.select { origin }) { city.match(right.origin) }
//        res shouldBe expected
//    }
//
//    @Test
//    fun `filter not join`() {
//        val res = typed.excludeJoin(typed2) { city.match(right.origin) }
//        res.nrow shouldBe 3
//        res.city.toSet() shouldBe typed.city.toSet() - typed2.origin.toSet()
//
//        val indexColumn = column<Int>("__index__")
//        val withIndex = typed.addId(indexColumn)
//        val joined = withIndex.filterJoin(typed2) { city.match(right.origin) }
//        val joinedIndices = joined[indexColumn].toSet()
//        val expected = withIndex.filter { !joinedIndices.contains(it[indexColumn]) }.remove(indexColumn)
//
//        res shouldBe expected
//    }
//}
