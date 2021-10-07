package org.jetbrains.dataframe

import java.math.BigDecimal
import kotlin.reflect.KType

@JvmName("meanT?")
public fun <T : Number> Sequence<T?>.mean(type: KType, skipNa: Boolean = true): Double = filterNotNull().mean(type, skipNa)

public fun <T : Number> Iterable<T>.mean(type: KType, skipNaN: Boolean): Double = asSequence().mean(type, skipNaN)

public fun <T : Number> Sequence<T>.mean(type: KType, skipNaN: Boolean): Double {
    return when (type.classifier) {
        Double::class -> (this as Sequence<Double>).mean(skipNaN)
        Float::class -> (this as Sequence<Float>).mean(skipNaN)
        Int::class -> (this as Sequence<Int>).map { it.toDouble() }.mean(false)
        Short::class -> (this as Sequence<Short>).map { it.toDouble() }.mean(false)
        Byte::class -> (this as Sequence<Byte>).map { it.toDouble() }.mean(false)
        Long::class -> (this as Sequence<Long>).map { it.toDouble() }.mean(false)
        BigDecimal::class -> (this as Sequence<BigDecimal>).map { it.toDouble() }.mean(false)
        Number::class -> (this as Sequence<Number>).map { it.toDouble() }.mean(skipNaN)
        else -> throw IllegalArgumentException()
    }
}

public fun Sequence<Double>.mean(skipNaN: Boolean): Double {
    var count = 0
    var sum: Double = 0.toDouble()
    for (element in this) {
        if (element.isNaN()) {
            if (skipNaN) continue
            else return Double.NaN
        }
        sum += element
        count++
    }
    return if (count > 0) sum / count else Double.NaN
}

@JvmName("meanFloat")
public fun Sequence<Float>.mean(skipNaN: Boolean): Double {
    var count = 0
    var sum: Double = 0.toDouble()
    for (element in this) {
        if (element.isNaN()) {
            if (skipNaN) continue
            else return Double.NaN
        }
        sum += element
        count++
    }
    return if (count > 0) sum / count else Double.NaN
}

@JvmName("doubleMean")
public fun Iterable<Double>.mean(skipNaN: Boolean): Double = asSequence().mean(skipNaN)

@JvmName("floatMean")
public fun Iterable<Float>.mean(skipNaN: Boolean): Double = asSequence().mean(skipNaN)

@JvmName("intMean")
public fun Iterable<Int>.mean(): Double =
    if (this is Collection) {
        if (size > 0) sumOf { it.toDouble() } / size else Double.NaN
    } else {
        var count = 0
        val sum = sumOf { count++; it.toDouble() }
        if (count > 0) sum / count else Double.NaN
    }

@JvmName("shortMean")
public fun Iterable<Short>.mean(): Double =
    if (this is Collection) {
        if (size > 0) sumOf { it.toDouble() } / size else Double.NaN
    } else {
        var count = 0
        val sum = sumOf { count++; it.toDouble() }
        if (count > 0) sum / count else Double.NaN
    }

@JvmName("byteMean")
public fun Iterable<Byte>.mean(): Double =
    if (this is Collection) {
        if (size > 0) sumOf { it.toDouble() } / size else Double.NaN
    } else {
        var count = 0
        val sum = sumOf { count++; it.toDouble() }
        if (count > 0) sum / count else Double.NaN
    }

@JvmName("longMean")
public fun Iterable<Long>.mean(): Double =
    if (this is Collection) {
        if (size > 0) sumOf { it.toDouble() } / size else Double.NaN
    } else {
        var count = 0
        val sum = sumOf { count++; it.toDouble() }
        if (count > 0) sum / count else Double.NaN
    }

@JvmName("bigDecimalMean")
public fun Iterable<BigDecimal>.mean(): Double =
    if (this is Collection) {
        if (size > 0) sum().toDouble() / size else Double.NaN
    } else {
        var count = 0
        val sum = sumOf { count++; it.toDouble() }
        if (count > 0) sum / count else Double.NaN
    }
