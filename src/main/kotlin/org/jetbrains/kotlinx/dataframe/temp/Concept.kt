package org.jetbrains.kotlinx.dataframe.temp

import org.jetbrains.kotlinx.dataframe.ColumnsContainer
import org.jetbrains.kotlinx.dataframe.DataColumn
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.select

public fun test(df: DataFrame<*>) {
    with(object {
        val ColumnsContainer<*>.age get() = this["age"] as DataColumn<Int>
    }) {
        df.select { age }
    }
}

// Идея: воткнуть в датафрейм объект, который будет определять экстеншены для доступа к данным

/*
interface DataFrame<out T> {
    val context: T
    ...
}

fun <T> DataFrame<T>.filter(predicate: context(T) DataRow<T>.(DataRow<T>) -> Boolean): DataFrame<T> =
    indices.filter {
        val row = get(it)
        predicate(context, row, row)
    }.let { get(it) }
*/
