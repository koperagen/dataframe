package org.jetbrains.kotlinx.dataframe.temp

import org.jetbrains.kotlinx.dataframe.ColumnsContainer
import org.jetbrains.kotlinx.dataframe.DataColumn
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.DataRow
import org.jetbrains.kotlinx.dataframe.Selector
import org.jetbrains.kotlinx.dataframe.api.add
import org.jetbrains.kotlinx.dataframe.api.cast
import org.jetbrains.kotlinx.dataframe.api.dataFrameOf
import org.jetbrains.kotlinx.dataframe.api.filter
import org.jetbrains.kotlinx.dataframe.api.print
import org.jetbrains.kotlinx.dataframe.api.select
import org.jetbrains.kotlinx.dataframe.api.select1
import org.jetbrains.kotlinx.dataframe.columns.SingleColumn
import org.jetbrains.kotlinx.dataframe.temp.PlainSchema.firstName
import org.jetbrains.kotlinx.dataframe.temp.PlainSchema.lastName

public object PlainSchema {
        public val DataRow<PlainSchema>.firstName: String get() = this["firstName"] as String
        public val ColumnsContainer<PlainSchema>.firstName: DataColumn<String> get() = this["firstName"] as DataColumn<String>

        public val DataRow<PlainSchema>.lastName: String get() = this["lastName"] as String
        public val ColumnsContainer<PlainSchema>.lastName: DataColumn<String> get() = this["lastName"] as DataColumn<String>

        public val DataRow<PlainSchema>.age: Int get() = this["age"] as Int
        public val ColumnsContainer<PlainSchema>.age: DataColumn<Int> get() = this["age"] as DataColumn<Int>

        public val DataRow<PlainSchema>.city: String? get() = this["city"] as String?
        public val ColumnsContainer<PlainSchema>.city: DataColumn<String?> get() = this["city"] as DataColumn<String?>

        public val DataRow<PlainSchema>.weight: Int? get() = this["weight"] as Int?
        public val ColumnsContainer<PlainSchema>.weight: DataColumn<Int?> get() = this["weight"] as DataColumn<Int?>

        public val DataRow<PlainSchema>.isHappy: Boolean get() = this["isHappy"] as Boolean
        public val ColumnsContainer<PlainSchema>.isHappy: DataColumn<Boolean> get() = this["isHappy"] as DataColumn<Boolean>
}


public fun main() {
    val typedDf: DataFrame<PlainSchema> = dataFrameOf("firstName", "lastName", "age", "city", "weight", "isHappy")(
        "Alice", "Cooper", 15, "London", 54, true,
        "Bob", "Dylan", 45, "Dubai", 87, true,
        "Charlie", "Daniels", 20, "Moscow", null, false,
        "Charlie", "Chaplin", 40, "Milan", null, true,
        "Bob", "Marley", 30, "Tokyo", 68, true,
        "Alice", "Wolf", 20, null, 55, false,
        "Charlie", "Byrd", 30, "Moscow", 90, true
    ).cast(PlainSchema)

    typedDf.filter { isHappy }.print()
    typedDf.select1 { cols(firstName, lastName) }.print()

    // Столбцы начальной схемы резолвятся, потому-что IDE их находит и импортирует на топ левел
    typedDf.firstName.print()
    typedDf[0].firstName

    // Примерно такой код позволяет после изменения схемы подменить контекст у датафрейма
    val updatedDf = typedDf
        .add("fullName") { firstName + lastName }
        .run {
            class This {
                val DataRow<This>.fullName: String get() = this["fullName"] as String
                val ColumnsContainer<This>.fullName: DataColumn<String> get() = this["fullName"] as DataColumn<String>
            }
            cast(This())
        }.filter { fullName.length > 12 }
        .also { it.print() }

    // Но со сгенерированными столбцами и строками обходить датафрейм а-ля объект не получится :(
    //updatedDf.fullName
    //updatedDf[0].fullName

    // Есть подозрение, что можно весь граф датафрейма замапить в дерево объектов

    // Гарантированно работающий костыль - сделать аналог select, какой-то там pick { columnGroup.intColumn[0] }

    updatedDf.select1 { fullName }.print()

    // Пример с вложенными схемами см. в ComplexSchema
}
