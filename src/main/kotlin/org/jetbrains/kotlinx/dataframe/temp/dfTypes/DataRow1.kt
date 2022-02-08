package sdffds

import org.jetbrains.kotlinx.dataframe.DataColumn

public interface DataRow {}

// Рекурсивный типовой параметр, чтобы определять функции, возвращающие сам датафрейм
public interface DataFrame<Me : DataFrame<Me, R>, out R: DataRow> {
    public fun get(index: List<Int>): Me
    public fun get(index: Int): R = TODO()

    public fun nrow(): Int = 42
}

public interface Cars {
    public val year: Int

    // Сгенерировано плагином
    public interface Row : DataRow {
        public val year: Int get() = TODO()
    }

    public interface Frame : DataFrame<Frame, Row> {
        public val year: DataColumn<Int> get() = TODO()

        override fun get(index: Int): Row = object : Row { }

        override fun get(index: List<Int>): Frame {
            return FrameImpl()
        }
    }

    private class FrameImpl : Frame
}

public fun filterOldCars_goal(df: Cars.Frame) {
    val df1 = df
        .add("age") { 2022 - year }
        .run {
            class CarsRow1 : Cars.Row {
                val age: Int = TODO()
            }
            class DF : DataFrame<DF, CarsRow1> {
                /*override*/ fun create(): CarsRow1 = CarsRow1()
                val columnAge: DataColumn<Int> = TODO()

                override fun get(index: List<Int>): DF {
                    return DF()
                }
            }
            DF()
        }

    df1.columnAge
    df1.filter { age == 20 }

    df1.writeCSV("old_cars.csv")

    df1.filter { age == 20 }.columnAge
}

public fun <D: DataFrame<D, R>, R: DataRow> D.filter(predicate: R.(R) -> Boolean): D =
    (0 until nrow()).filter {
        val row = get(it)
        predicate(row, row)
    }.let { get(it) }


public fun <D: DataFrame<D, R>, R: DataRow, V> D.add(name: String, addDsl: R.(R) -> V): D = TODO()
public fun <D: DataFrame<D, R>, R: DataRow> D.writeCSV(pat: String): Unit = TODO()


/*
Сигнатура агрегейта сейчас:
public interface Grouped<out T> : Aggregatable<T> {

    public fun <R> aggregate(body: AggregateGroupedBody<T, R>): DataFrame<T>
}
AggregateGroupedBody<T, R> = AggregateGroupedDsl<T>.(AggregateGroupedDsl<T>) → R

AggregateGroupedDsl<T> : DataFrame<T>, поэтому на this доступны колонки

Мы не можем создать AggregateGroupedDsl, который бы расширял Cars.Frame, но вроде можно так:
AggregateGroupedBody<D, R> = context(AggregateGroupedDsl) D.(D) -> R where D : DataFrame<D, Row>, Row : DataRow
*/
