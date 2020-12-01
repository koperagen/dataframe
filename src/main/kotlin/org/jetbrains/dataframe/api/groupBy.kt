package org.jetbrains.dataframe

import org.jetbrains.dataframe.impl.GroupedDataFrameImpl
import org.jetbrains.dataframe.impl.dfsTopNotNull
import kotlin.reflect.KProperty

fun <T> DataFrame<T>.groupBy(cols: ColumnsSelector<T, *>) = groupBy(getColumns(cols))
fun <T> DataFrame<T>.groupBy(vararg cols: KProperty<*>) = groupBy(getColumns(cols))
fun <T> DataFrame<T>.groupBy(vararg cols: String) = groupBy(getColumns(cols))
fun <T> DataFrame<T>.groupBy(vararg cols: Column) = groupBy(cols.toList())
fun <T> DataFrame<T>.groupBy(cols: Iterable<Column>): GroupedDataFrame<T, T> {

   val tree = collectTree(cols)

   val nodes = tree.dfsTopNotNull().map { ColumnWithPath(it.data, it.pathFromRoot()) }.shortenPaths()

   val columns = nodes.map { it.source }

   val groups = (0 until nrow)
           .map { index -> columns.map { it[index] } to index }
           .groupBy({ it.first }) { it.second }.toList()

   val keyIndices = groups.map { it.second[0] }

   val keyColumnsToInsert = nodes.map {
       val column = it.source[keyIndices]
       val path = it.path
       ColumnToInsert(path, null, column)
   }

   val keyColumnsDf = insertColumns(keyColumnsToInsert).typed<T>()

   val permutation = groups.flatMap { it.second }
   val sorted = getRows(permutation)

   var lastIndex = 0
   val startIndices = groups.map {
       val start = lastIndex
       lastIndex += it.second.size
       start
   }

   val groupedColumn = ColumnData.createTable(columnForGroupedData.name, sorted, startIndices)

   val df = keyColumnsDf + groupedColumn
   return GroupedDataFrameImpl(df, groupedColumn)
}

inline fun <T, reified R> DataFrame<T>.groupByNew(name: String = "key", noinline expression: RowSelector<T, R?>) =
        add(name, expression).groupBy(name)

internal val columnForGroupedData by column<DataFrame<*>>("DataFrame")