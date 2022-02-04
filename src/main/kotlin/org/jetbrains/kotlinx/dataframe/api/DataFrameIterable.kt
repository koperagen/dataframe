package org.jetbrains.kotlinx.dataframe.api

import org.jetbrains.kotlinx.dataframe.AnyBaseColumn
import org.jetbrains.kotlinx.dataframe.AnyCol
import org.jetbrains.kotlinx.dataframe.AnyFrame
import org.jetbrains.kotlinx.dataframe.Column
import org.jetbrains.kotlinx.dataframe.ColumnSelector
import org.jetbrains.kotlinx.dataframe.ColumnsContainer
import org.jetbrains.kotlinx.dataframe.ColumnsSelector
import org.jetbrains.kotlinx.dataframe.DataColumn
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.DataRow
import org.jetbrains.kotlinx.dataframe.RowExpression
import org.jetbrains.kotlinx.dataframe.RowFilter
import org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor
import org.jetbrains.kotlinx.dataframe.columns.ColumnKind
import org.jetbrains.kotlinx.dataframe.columns.ColumnPath
import org.jetbrains.kotlinx.dataframe.columns.ColumnReference
import org.jetbrains.kotlinx.dataframe.impl.ColumnNameGenerator
import org.jetbrains.kotlinx.dataframe.impl.api.createDataFrameImpl
import org.jetbrains.kotlinx.dataframe.impl.api.mapNotNullValues
import org.jetbrains.kotlinx.dataframe.impl.asList
import org.jetbrains.kotlinx.dataframe.impl.columnName
import org.jetbrains.kotlinx.dataframe.impl.columns.guessColumnType
import org.jetbrains.kotlinx.dataframe.impl.columns.newColumn
import org.jetbrains.kotlinx.dataframe.impl.columns.newColumnWithActualType
import org.jetbrains.kotlinx.dataframe.impl.columns.toColumnSet
import org.jetbrains.kotlinx.dataframe.impl.columns.toColumns
import org.jetbrains.kotlinx.dataframe.impl.toIndices
import org.jetbrains.kotlinx.dataframe.index
import org.jetbrains.kotlinx.dataframe.indices
import org.jetbrains.kotlinx.dataframe.io.read
import org.jetbrains.kotlinx.dataframe.ncol
import org.jetbrains.kotlinx.dataframe.nrow
import org.jetbrains.kotlinx.dataframe.typeClass
import java.io.File
import java.net.URL
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.typeOf

// region DataFrame Iterable API

public fun <T> DataFrame<T>.asSequence(): Sequence<DataRow<T>> = rows().asSequence()

public fun <T> DataFrame<T>.any(predicate: RowFilter<T>): Boolean = rows().any { predicate(it, it) }
public fun <T> DataFrame<T>.all(predicate: RowFilter<T>): Boolean = rows().all { predicate(it, it) }

public fun <T, V> DataFrame<T>.associateBy(transform: RowExpression<T, V>): Map<V, DataRow<T>> =
    rows().associateBy { transform(it, it) }

public fun <T, K, V> DataFrame<T>.associate(transform: RowExpression<T, Pair<K, V>>): Map<K, V> =
    rows().associate { transform(it, it) }

public fun <T> DataFrame<T>.tail(numRows: Int = 5): DataFrame<T> = takeLast(numRows)
public fun <T> DataFrame<T>.head(numRows: Int = 5): DataFrame<T> = take(numRows)

public fun <T> DataFrame<T>.shuffle(): DataFrame<T> = getRows(indices.shuffled())

// region isEmpty

public fun AnyFrame.isEmpty(): Boolean = ncol == 0 || nrow == 0
public fun AnyFrame.isNotEmpty(): Boolean = !isEmpty()

// endregion

// region map

public fun <T> DataFrame<T>.map(body: AddDsl<T>.() -> Unit): AnyFrame {
    val dsl = AddDsl(this)
    body(dsl)
    return dataFrameOf(dsl.columns)
}

public inline fun <T, reified R> ColumnsContainer<T>.map(
    name: String,
    infer: Infer = Infer.Nulls,
    noinline body: AddExpression<T, R>
): DataColumn<R> = when (infer) {
    Infer.Type -> newColumnWithActualType(name, body)
    Infer.Nulls -> newColumn(typeOf<R>(), name, true, body)
    Infer.None -> newColumn(typeOf<R>(), name, false, body)
}

public inline fun <T, reified R> ColumnsContainer<T>.map(
    column: ColumnAccessor<R>,
    infer: Infer = Infer.Nulls,
    noinline body: AddExpression<T, R>
): DataColumn<R> = map(column.name(), infer, body)

public inline fun <T, reified R> ColumnsContainer<T>.map(
    column: KProperty<R>,
    infer: Infer = Infer.Nulls,
    noinline body: AddExpression<T, R>
): DataColumn<R> = map(column.columnName, infer, body)

// endregion

// region first/last/single

public fun <T> DataFrame<T>.firstOrNull(predicate: RowFilter<T>): DataRow<T>? = rows().firstOrNull { predicate(it, it) }
public fun <T> DataFrame<T>.first(predicate: RowFilter<T>): DataRow<T> = rows().first { predicate(it, it) }
public fun <T> DataFrame<T>.firstOrNull(): DataRow<T>? = if (nrow > 0) first() else null
public fun <T> DataFrame<T>.first(): DataRow<T> {
    if (nrow == 0) {
        throw NoSuchElementException("DataFrame has no rows. Use `firstOrNull`.")
    }
    return get(0)
}

public fun <T> DataFrame<T>.lastOrNull(predicate: RowFilter<T>): DataRow<T>? =
    rowsReversed().firstOrNull { predicate(it, it) }

public fun <T> DataFrame<T>.last(predicate: RowFilter<T>): DataRow<T> = rowsReversed().first { predicate(it, it) }
public fun <T> DataFrame<T>.lastOrNull(): DataRow<T>? = if (nrow > 0) get(nrow - 1) else null
public fun <T> DataFrame<T>.last(): DataRow<T> {
    if (nrow == 0) {
        throw NoSuchElementException("DataFrame has no rows. Use `lastOrNull`.")
    }
    return get(nrow - 1)
}

public fun <T> DataFrame<T>.single(predicate: RowExpression<T, Boolean>): DataRow<T> = rows().single { predicate(it, it) }
public fun <T> DataFrame<T>.singleOrNull(predicate: RowExpression<T, Boolean>): DataRow<T>? =
    rows().singleOrNull { predicate(it, it) }

public fun <T> DataFrame<T>.single(): DataRow<T> =
    when (nrow) {
        0 -> throw NoSuchElementException("DataFrame has no rows. Use `singleOrNull`.")
        1 -> get(0)
        else -> throw IllegalArgumentException("DataFrame has more than one row.")
    }

public fun <T> DataFrame<T>.singleOrNull(): DataRow<T>? = rows().singleOrNull()

// endregion

// region filter

public fun <T> DataFrame<T>.filter(predicate: context(T) DataRow<T>.(DataRow<T>) -> Boolean): DataFrame<T> =
    indices.filter {
        val row = get(it)
        predicate(context, row, row)
    }.let { get(it) }

public fun <T> DataFrame<T>.filterBy(column: ColumnSelector<T, Boolean>): DataFrame<T> = getRows(getColumn(column).toList().toIndices())
public fun <T> DataFrame<T>.filterBy(column: ColumnReference<Boolean>): DataFrame<T> = filterBy { column }
public fun <T> DataFrame<T>.filterBy(column: String): DataFrame<T> = filterBy { column.toColumnOf() }
public fun <T> DataFrame<T>.filterBy(column: KProperty<Boolean>): DataFrame<T> = filterBy { column.toColumnAccessor() }

// endregion

// region take/drop

/**
 * Returns a DataFrame containing first [n] rows.
 *
 * @throws IllegalArgumentException if [n] is negative.
 */
public fun <T> DataFrame<T>.take(n: Int): DataFrame<T> {
    require(n >= 0) { "Requested rows count $n is less than zero." }
    return getRows(0 until n.coerceAtMost(nrow))
}

/**
 * Returns a DataFrame containing all rows except first [n] rows.
 *
 * @throws IllegalArgumentException if [n] is negative.
 */
public fun <T> DataFrame<T>.drop(n: Int): DataFrame<T> {
    require(n >= 0) { "Requested rows count $n is less than zero." }
    return getRows(n.coerceAtMost(nrow) until nrow)
}

/**
 * Returns a DataFrame containing last [n] rows.
 *
 * @throws IllegalArgumentException if [n] is negative.
 */
public fun <T> DataFrame<T>.takeLast(n: Int): DataFrame<T> {
    require(n >= 0) { "Requested rows count $n is less than zero." }
    return drop((nrow - n).coerceAtLeast(0))
}

/**
 * Returns a DataFrame containing all rows except last [n] rows.
 *
 * @throws IllegalArgumentException if [n] is negative.
 */
public fun <T> DataFrame<T>.dropLast(n: Int = 1): DataFrame<T> {
    require(n >= 0) { "Requested rows count $n is less than zero." }
    return take((nrow - n).coerceAtLeast(0))
}

/**
 * Returns a DataFrame containing all rows except rows that satisfy the given [predicate].
 */
public fun <T> DataFrame<T>.drop(predicate: RowFilter<T>): DataFrame<T> = filter { !predicate(it, it) }

/**
 * Returns a DataFrame containing first rows that satisfy the given [predicate].
 */
public fun <T> DataFrame<T>.takeWhile(predicate: RowFilter<T>): DataFrame<T> = firstOrNull { !predicate(it, it) }?.let { take(it.index) } ?: this

/**
 * Returns a DataFrame containing all rows except first rows that satisfy the given [predicate].
 */
public fun <T> DataFrame<T>.dropWhile(predicate: RowFilter<T>): DataFrame<T> = firstOrNull { !predicate(it, it) }?.let { drop(it.index) } ?: this

// endregion

// region distinct

public fun <T> DataFrame<T>.distinct(): DataFrame<T> = distinctBy { all() }

public fun <T, C> DataFrame<T>.distinct(columns: ColumnsSelector<T, C>): DataFrame<T> = select(columns).distinct()
public fun <T> DataFrame<T>.distinct(vararg columns: KProperty<*>): DataFrame<T> = distinct {
    val set = columns.toColumns()
    set
}
public fun <T> DataFrame<T>.distinct(vararg columns: String): DataFrame<T> = distinct { columns.toColumns() }
public fun <T> DataFrame<T>.distinct(vararg columns: Column): DataFrame<T> = distinct { columns.toColumns() }

@JvmName("distinctT")
public fun <T> DataFrame<T>.distinct(columns: Iterable<String>): DataFrame<T> = distinct { columns.toColumns() }
public fun <T> DataFrame<T>.distinct(columns: Iterable<Column>): DataFrame<T> = distinct { columns.toColumnSet() }

public fun <T> DataFrame<T>.distinctBy(vararg columns: KProperty<*>): DataFrame<T> = distinctBy { columns.toColumns() }
public fun <T> DataFrame<T>.distinctBy(vararg columns: String): DataFrame<T> = distinctBy { columns.toColumns() }
public fun <T> DataFrame<T>.distinctBy(vararg columns: Column): DataFrame<T> = distinctBy { columns.toColumns() }

@JvmName("distinctByT")
public fun <T> DataFrame<T>.distinctBy(columns: Iterable<String>): DataFrame<T> = distinctBy { columns.toColumns() }
public fun <T> DataFrame<T>.distinctBy(columns: Iterable<Column>): DataFrame<T> = distinctBy { columns.toColumnSet() }

public fun <T, C> DataFrame<T>.distinctBy(columns: ColumnsSelector<T, C>): DataFrame<T> {
    val cols = get(columns)
    val distinctIndices = indices.distinctBy { i -> cols.map { it[i] } }
    return this[distinctIndices]
}

// endregion

// region countDistinct

public fun AnyFrame.countDistinct(): Int = countDistinct { all() }

public fun <T, C> DataFrame<T>.countDistinct(columns: ColumnsSelector<T, C>): Int {
    val cols = get(columns)
    return indices.distinctBy { i -> cols.map { it[i] } }.size
}

public fun <T> DataFrame<T>.countDistinct(vararg columns: String): Int = countDistinct { columns.toColumns() }
public fun <T, C> DataFrame<T>.countDistinct(vararg columns: KProperty<C>): Int = countDistinct { columns.toColumns() }
public fun <T> DataFrame<T>.countDistinct(vararg columns: Column): Int = countDistinct { columns.toColumns() }

// endregion

// region forEach

public fun <T> DataFrame<T>.forEachRow(action: RowExpression<T, Unit>): Unit = rows().forEach { action(it, it) }

public fun <T> DataFrame<T>.forEachColumn(action: (AnyCol) -> Unit): Unit = columns().forEach(action)

public fun <T> DataFrame<T>.forEachColumnIndexed(action: (Int, AnyCol) -> Unit): Unit =
    columns().forEachIndexed(action)

// endregion

// endregion

// region read DataFrame from objects

public inline fun <reified T> Iterable<T>.toDataFrame(): DataFrame<T> = toDataFrame {
    properties(depth = 1)
}

public inline fun <reified T> Iterable<T>.toDataFrame(noinline body: CreateDataFrameDsl<T>.() -> Unit): DataFrame<T> = createDataFrameImpl(T::class, body)

public inline fun <reified T> Iterable<T>.toDataFrame(vararg props: KProperty<*>, depth: Int = 1): DataFrame<T> =
    toDataFrame {
        properties(roots = props, depth = depth)
    }

public inline fun <reified T> DataColumn<T>.read(): AnyCol = when (kind()) {
    ColumnKind.Group, ColumnKind.Frame -> this
    else -> when {
        isPrimitive() -> this
        typeClass == File::class -> cast<File?>().mapNotNullValues { DataFrame.read(it) }
        typeClass == URL::class -> cast<URL?>().mapNotNullValues { DataFrame.read(it) }
        else -> values().toDataFrame().asColumnGroup(name()).asDataColumn()
    }
}

@JvmName("toDataFrameT")
public fun <T> Iterable<DataRow<T>>.toDataFrame(): DataFrame<T> {
    var uniqueDf: DataFrame<T>? = null
    for (row in this) {
        if (uniqueDf == null) uniqueDf = row.df()
        else {
            if (uniqueDf !== row.df()) {
                uniqueDf = null
                break
            }
        }
    }
    return if (uniqueDf != null) {
        val permutation = map { it.index }
        uniqueDf[permutation]
    } else map { it.toDataFrame() }.concat()
}

@JvmName("toDataFrameAnyColumnContext")
public fun <T> Iterable<AnyBaseColumn>.toDataFrameByContext(context: T): DataFrame<T> = dataFrameOf(this, context)

@JvmName("toDataFrameAnyColumn")
public fun Iterable<AnyBaseColumn>.toDataFrame(): AnyFrame = dataFrameOf(this)

@JvmName("toDataFramePairColumnPathAnyCol")
public fun <T> Iterable<Pair<ColumnPath, AnyBaseColumn>>.toDataFrameFromPairs(): DataFrame<T> {
    val nameGenerator = ColumnNameGenerator()
    val columnNames = mutableListOf<String>()
    val columnGroups = mutableListOf<MutableList<Pair<ColumnPath, AnyBaseColumn>>?>()
    val columns = mutableListOf<AnyBaseColumn?>()
    val columnIndices = mutableMapOf<String, Int>()
    val columnGroupName = mutableMapOf<String, String>()

    forEach { (path, col) ->
        when (path.size) {
            0 -> {
            }
            1 -> {
                val name = path[0]
                val uniqueName = nameGenerator.addUnique(name)
                val index = columns.size
                columnNames.add(uniqueName)
                columnGroups.add(null)
                columns.add(col.rename(uniqueName))
                columnIndices[uniqueName] = index
            }
            else -> {
                val name = path[0]
                val uniqueName = columnGroupName.getOrPut(name) {
                    nameGenerator.addUnique(name)
                }
                val index = columnIndices.getOrPut(uniqueName) {
                    columnNames.add(uniqueName)
                    columnGroups.add(mutableListOf())
                    columns.add(null)
                    columns.size - 1
                }
                val list = columnGroups[index]!!
                list.add(path.drop(1) to col)
            }
        }
    }
    columns.indices.forEach { index ->
        val group = columnGroups[index]
        if (group != null) {
            val nestedDf = group.toDataFrameFromPairs<Unit>()
            val col = DataColumn.createColumnGroup(columnNames[index], nestedDf)
            assert(columns[index] == null)
            columns[index] = col
        } else assert(columns[index] != null)
    }
    return columns.map { it!! }.toDataFrame().cast()
}

@JvmName("toDataFrameColumnPathAnyNullable")
public fun Iterable<Pair<ColumnPath, Iterable<Any?>>>.toDataFrameFromPairs(): AnyFrame {
    return map { it.first to guessColumnType(it.first.last(), it.second.asList()) }.toDataFrameFromPairs<Unit>()
}

public fun Iterable<Pair<String, Iterable<Any?>>>.toDataFrameFromPairs(): AnyFrame {
    return map { ColumnPath(it.first) to guessColumnType(it.first, it.second.asList()) }.toDataFrameFromPairs<Unit>()
}

public interface TraversePropertiesDsl {

    public fun exclude(vararg properties: KProperty<*>)

    /**
     * Skip instances of given [classes] from transformation into ColumnGroups and FrameColumns and store them in ValueColumn
     */
    public fun preserve(vararg classes: KClass<*>)
}

public inline fun <reified T> TraversePropertiesDsl.preserve(): Unit = preserve(T::class)

public abstract class CreateDataFrameDsl<T>(public val source: Iterable<T>) {

    public abstract fun add(column: AnyBaseColumn, path: ColumnPath? = null)

    public infix fun AnyBaseColumn.into(name: String): Unit = add(this, pathOf(name))

    public infix fun AnyBaseColumn.into(path: ColumnPath): Unit = add(this, path)

    public abstract fun properties(
        vararg roots: KProperty<*>,
        depth: Int = 1,
        body: (TraversePropertiesDsl.() -> Unit)? = null
    )

    public inline fun <reified R> expr(noinline expression: (T) -> R): DataColumn<R> =
        source.map { expression(it) }.toColumn()

    public inline fun <reified R> add(name: String, noinline expression: (T) -> R): Unit =
        add(source.map { expression(it) }.toColumn(name, Infer.Nulls))

    public inline infix fun <reified R> String.from(noinline expression: (T) -> R): Unit =
        add(this, expression)

    public inline infix fun <reified R> KProperty<R>.from(noinline expression: (T) -> R): Unit =
        add(columnName, expression)

    public inline infix fun <reified R> KProperty<R>.from(inferType: InferType<T, R>): Unit =
        add(DataColumn.createWithTypeInference(columnName, source.map { inferType.expression(it) }))

    public data class InferType<T, R>(val expression: (T) -> R)

    public inline fun <reified R> inferType(noinline expression: (T) -> R): InferType<T, R> = InferType(expression)

    public abstract operator fun String.invoke(builder: CreateDataFrameDsl<T>.() -> Unit)
}

// endregion

// region Create DataFrame from Map

public fun Map<String, Iterable<Any?>>.toDataFrame(): AnyFrame {
    return map { DataColumn.createWithTypeInference(it.key, it.value.asList()) }.toDataFrame()
}

@JvmName("toDataFrameColumnPathAnyNullable")
public fun Map<ColumnPath, Iterable<Any?>>.toDataFrame(): AnyFrame {
    return map { it.key to DataColumn.createWithTypeInference(it.key.last(), it.value.asList()) }.toDataFrameFromPairs<Unit>()
}

// endregion
