package org.jetbrains.kotlinx.dataframe.temp

import org.jetbrains.kotlinx.dataframe.ColumnsContainer
import org.jetbrains.kotlinx.dataframe.DataColumn
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.DataRow
import org.jetbrains.kotlinx.dataframe.annotations.DataSchema
import org.jetbrains.kotlinx.dataframe.api.print
import org.jetbrains.kotlinx.dataframe.api.cast
import org.jetbrains.kotlinx.dataframe.api.dataFrameOf
import org.jetbrains.kotlinx.dataframe.api.filter
import org.jetbrains.kotlinx.dataframe.api.group
import org.jetbrains.kotlinx.dataframe.api.into
import org.jetbrains.kotlinx.dataframe.api.select1
import org.jetbrains.kotlinx.dataframe.columns.ColumnGroup

@DataSchema
public interface PersonNested {
    public val name: Name

    // Добавлено плагином:
    public object Schema {
        public val DataRow<Schema>.name: DataRow<Name.Schema> get() = this["name"] as DataRow<Name.Schema>
        public val ColumnsContainer<Schema>.name: ColumnGroup<Name.Schema> get() = this["name"] as ColumnGroup<Name.Schema>

        // Придется экстеншены для всех вложенных схем дублировать в топ левел схему, потому-что filter { context (!!Person!!) }
        // либо как-то по другому агрегировать вложенные схемы

        public val DataRow<Name.Schema>.firstName: String get() = this["firstName"] as String
        public val ColumnsContainer<Name.Schema>.firstName: DataColumn<String> get() = this["firstName"] as DataColumn<String>

        public val DataRow<Name.Schema>.lastName: String get() = this["lastName"] as String
        public val ColumnsContainer<Name.Schema>.lastName: DataColumn<String> get() = this["lastName"] as DataColumn<String>
    }
}

@DataSchema
public interface Name {
    public val firstName: String
    public val lastName: String

    // Добавлено плагином
    public object Schema {
        public val DataRow<Schema>.firstName: String get() = this["firstName"] as String
        public val ColumnsContainer<Schema>.firstName: DataColumn<String> get() = this["firstName"] as DataColumn<String>

        public val DataRow<Schema>.lastName: String get() = this["lastName"] as String
        public val ColumnsContainer<Schema>.lastName: DataColumn<String> get() = this["lastName"] as DataColumn<String>
    }
}

public fun main() {
    val typedDf: DataFrame<PersonNested.Schema> = dataFrameOf("firstName", "lastName")(
        "Alice", "Cooper",
        "Bob", "Dylan",
        "Charlie", "Daniels",
        "Charlie", "Chaplin",
        "Bob", "Marley",
        "Alice", "Wolf",
        "Charlie", "Byrd"
    ).group("firstName", "lastName").into("name").cast(PersonNested.Schema)

    typedDf.filter { (name.firstName + name.lastName).length > 12 }.print()
    typedDf.select1 { name.firstName }.print()

}
