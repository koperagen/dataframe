package org.jetbrains.kotlinx.dataframe.api

import org.jetbrains.kotlinx.dataframe.AnyFrame
import org.jetbrains.kotlinx.dataframe.AnyRow
import org.jetbrains.kotlinx.dataframe.Column
import org.jetbrains.kotlinx.dataframe.DataColumn
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.DataRow
import org.jetbrains.kotlinx.dataframe.RowExpression
import org.jetbrains.kotlinx.dataframe.columns.ColumnGroup
import org.jetbrains.kotlinx.dataframe.columns.ColumnReference
import org.jetbrains.kotlinx.dataframe.impl.columnName
import org.jetbrains.kotlinx.dataframe.impl.owner
import org.jetbrains.kotlinx.dataframe.index
import org.jetbrains.kotlinx.dataframe.indices
import org.jetbrains.kotlinx.dataframe.ncol
import org.jetbrains.kotlinx.dataframe.nrow
import org.jetbrains.kotlinx.dataframe.type
import kotlin.experimental.ExperimentalTypeInference
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.full.withNullability

public fun AnyRow.isEmpty(): Boolean = owner.columns().all { it[index] == null }
public fun AnyRow.isNotEmpty(): Boolean = !isEmpty()

public inline fun <reified R> AnyRow.valuesOf(): List<R> = values().filterIsInstance<R>()

public data class NameValuePair<V>(val name: String, val value: V)

public inline fun <reified R> AnyRow.namedValuesOf(): List<NameValuePair<R>> =
    values().zip(columnNames()).filter { it.first is R }.map { NameValuePair(it.second, it.first as R) }

public fun AnyRow.namedValues(): List<NameValuePair<Any?>> =
    values().zip(columnNames()) { value, name -> NameValuePair(name, value) }

// region getValue

public fun <T> AnyRow.getValue(columnName: String): T = get(columnName) as T
public fun <T> AnyRow.getValue(column: ColumnReference<T>): T = get(column)
public fun <T> AnyRow.getValue(column: KProperty<T>): T = get(column)

public fun <T> AnyRow.getValueOrNull(columnName: String): T? = getOrNull(columnName) as T?
public fun <T> AnyRow.getValueOrNull(column: KProperty<T>): T? = getValueOrNull<T>(column.columnName)

// endregion

// region contains

public fun AnyRow.containsKey(columnName: String): Boolean = owner.containsColumn(columnName)
public fun AnyRow.containsKey(column: Column): Boolean = owner.containsColumn(column)
public fun AnyRow.containsKey(column: KProperty<*>): Boolean = owner.containsColumn(column)

public operator fun AnyRow.contains(column: Column): Boolean = containsKey(column)
public operator fun AnyRow.contains(column: KProperty<*>): Boolean = containsKey(column)

// endregion

@OptIn(ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public fun <T> DataRow<T>.diff(expression: RowExpression<T, Double>): Double? = prev()?.let { p -> expression(this, this) - expression(p, p) }

public fun <T> DataRow<T>.diff(expression: RowExpression<T, Int>): Int? = prev()?.let { p -> expression(this, this) - expression(p, p) }

public fun <T> DataRow<T>.diff(expression: RowExpression<T, Long>): Long? = prev()?.let { p -> expression(this, this) - expression(p, p) }

public fun <T> DataRow<T>.diff(expression: RowExpression<T, Float>): Float? = prev()?.let { p -> expression(this, this) - expression(p, p) }

public fun AnyRow.columnsCount(): Int = df().ncol
public fun AnyRow.columnNames(): List<String> = df().columnNames()
public fun AnyRow.columnTypes(): List<KType> = df().columnTypes()

public fun <T> DataRow<T>.getRow(index: Int): DataRow<T> = getRowOrNull(index)!!

public fun <T> DataRow<T>.getRows(indices: Iterable<Int>): DataFrame<T> = df().getRows(indices)
public fun <T> DataRow<T>.getRows(indices: IntRange): DataFrame<T> = df().getRows(indices)

public fun <T> DataRow<T>.getRowOrNull(index: Int): DataRow<T>? {
    val df = df()
    return if (index >= 0 && index < df.nrow) df[index] else null
}

public fun <T> DataRow<T>.prev(): DataRow<T>? {
    val index = index()
    return if (index > 0) df()[index - 1] else null
}

public fun <T> DataRow<T>.next(): DataRow<T>? {
    val index = index()
    val df = df()
    return if (index < df.nrow - 1) df[index + 1] else null
}

public fun <T> DataRow<T>.relative(relativeIndices: Iterable<Int>): DataFrame<T> =
    getRows(relativeIndices.mapNotNull { (index + it).let { if (it >= 0 && it < df().rowsCount()) it else null } })

public fun <T> DataRow<T>.relative(relativeIndices: IntRange): DataFrame<T> =
    getRows((relativeIndices.start + index).coerceIn(df().indices)..(relativeIndices.endInclusive + index).coerceIn(df().indices))

internal fun <T> DataRow<T>.movingAverage(k: Int, expression: RowExpression<T, Number>): Double {
    var count = 0
    return backwardIterable().take(k).sumByDouble {
        count++
        expression(it).toDouble()
    } / count
}

internal fun <T> DataRow<T>.duplicate(n: Int): DataFrame<T> = this.owner.columns().mapIndexed { colIndex, col ->
    when (col) {
        is ColumnGroup<*> -> DataColumn.createColumnGroup(col.name, col[index].duplicate(n))
        else -> {
            val value = col[index]
            if (value is AnyFrame) {
                DataColumn.createFrameColumn(col.name, MutableList(n) { value })
            } else DataColumn.createValueColumn(
                col.name,
                MutableList(n) { value },
                col.type.withNullability(value == null)
            )
        }
    }
}.toDataFrame().cast()
