package org.jetbrains.kotlinx.dataframe.temp.dfTypes

import org.jetbrains.kotlinx.dataframe.DataColumn

// Идея: конкретные строчки и датафреймы определяют схему данных
public open class CarsRow : DataRow {
    public val year: Int = TODO()
}

public open class CarDF : DataFrame<CarsRow> {
    public val year: DataColumn<Int> = TODO()
}

public fun filterOldCars_goal(df: CarDF) {
    val df1 = df
        .add("age") { 2022 - year }
        .run {
            class CarsRow1 : CarsRow() {
                val age: Int = TODO()
            }
            object : DataFrame<CarsRow1> {
                /*override*/ fun create(): CarsRow1 = CarsRow1()
                val columnAge: DataColumn<Int> = TODO()
            }
        }

    df1.columnAge
    df1.filter { age == 20 }

    df1.writeCSV("old_cars.csv")

    // columnAge потерялся после фильтра!
//    df1.filter { age == 20 }.columnAge
}

public fun <D: DataFrame<R>, R: DataRow> D.filter(predicate: R.(R) -> Boolean): DataFrame<R> =
    (0 until nrow()).filter {
        val row = get(it)
        predicate(row, row)
    }.let { get(it) }


public interface DataRow {}

public interface DataFrame<R: DataRow> {
    public fun get(index: Int): R = TODO()
    public fun get(index: List<Int>): DataFrame<R> = TODO()

    public fun nrow(): Int = 42
}

public fun <D: DataFrame<R>, R: DataRow, V> D.add(name: String, addDsl: R.(R) -> V): DataFrame<R> = TODO()
public fun <D: DataFrame<R>, R: DataRow> D.writeCSV(pat: String): Unit = TODO()
