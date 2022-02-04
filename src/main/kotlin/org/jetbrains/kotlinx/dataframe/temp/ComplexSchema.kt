package org.jetbrains.kotlinx.dataframe.temp

import org.jetbrains.kotlinx.dataframe.ColumnsContainer
import org.jetbrains.kotlinx.dataframe.DataColumn
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.DataRow
import org.jetbrains.kotlinx.dataframe.api.print
import org.jetbrains.kotlinx.dataframe.api.cast
import org.jetbrains.kotlinx.dataframe.api.dataFrameOf
import org.jetbrains.kotlinx.dataframe.api.filter
import org.jetbrains.kotlinx.dataframe.api.group
import org.jetbrains.kotlinx.dataframe.api.into
import org.jetbrains.kotlinx.dataframe.columns.ColumnGroup

public object ComplexSchema {
    public val DataRow<ComplexSchema>.name: DataRow<Name> get() = this["name"] as DataRow<Name>
    public val ColumnsContainer<ComplexSchema>.name: ColumnGroup<Name> get() = this["name"] as ColumnGroup<Name>

    // Придется экстеншены для всех вложенных схем дублировать в топ левел схему, потому-что filter { context (!!ComplexSchema!!) }
    // либо как-то по другому агрегировать вложенные контексты в топ левел.

    public val DataRow<Name>.firstName: String get() = this["firstName"] as String
    public val ColumnsContainer<Name>.firstName: DataColumn<String> get() = this["firstName"] as DataColumn<String>

    public val DataRow<Name>.lastName: String get() = this["lastName"] as String
    public val ColumnsContainer<Name>.lastName: DataColumn<String> get() = this["lastName"] as DataColumn<String>
}

public object Name

public fun main() {
    val typedDf: DataFrame<ComplexSchema> = dataFrameOf("firstName", "lastName")(
        "Alice", "Cooper",
        "Bob", "Dylan",
        "Charlie", "Daniels",
        "Charlie", "Chaplin",
        "Bob", "Marley",
        "Alice", "Wolf",
        "Charlie", "Byrd"
    ).group("firstName", "lastName").into("name").cast(ComplexSchema)

    typedDf.filter { (name.firstName + name.lastName).length > 12 }.print()
}
