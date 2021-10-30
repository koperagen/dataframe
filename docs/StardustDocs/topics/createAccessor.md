[//]: # (title: Create ColumnAccessor)
<!---IMPORT org.jetbrains.kotlinx.dataframe.samples.api.Create-->

### by column

Creates [column accessor](DataColumn.md#column-accessors). Column [`type`](DataColumn.md#column-properties) should be passed as type argument, column [`name`](DataColumn.md#column-properties) is taken from the variable name.

<!---FUN createColumnAccessor-->

```kotlin
val name by column<String>()
```

<!---END-->

To assign column name explicitly, pass it as an argument and replace `by` with `=`.

<!---FUN createColumnAccessorRenamed-->

```kotlin
val accessor = column<String>("complex column name")
```

<!---END-->

You can create also column accessors to access [ColumnGroup](DataColumn.md#columngroup) or [FrameColumn](DataColumn.md#framecolumn)

<!---FUN createGroupOrFrameColumnAccessor-->

```kotlin
val columns by columnGroup()
val frames by frameColumn()
```

<!---END-->

### Deep column accessors

Deep column accessor references nested columns inside [ColumnGroups](DataColumn.md#columngroup).

<!---FUN createDeepColumnAccessor-->

```kotlin
val name by columnGroup()
val firstName by name.column<String>()
```

<!---END-->

### Computed column accessors

Computed column accessor evaluates custom expression on every data access.

<!---FUN columnAccessorComputed-->
<tabs>
<tab title="Properties">

```kotlin
val fullName by df.column { name.firstName + " " + name.lastName }

df[fullName]
```

</tab>
<tab title="Accessors">

```kotlin
val name by columnGroup()
val firstName by name.column<String>()
val lastName by name.column<String>()

val fullName by column { firstName() + " " + lastName() }

df[fullName]
```

</tab>
<tab title="Strings">

```kotlin
val fullName by column { "name"["firstName"]<String>() + " " + "name"["lastName"]<String>() }

df[fullName]
```

</tab></tabs>
<!---END-->

When expression depends only on one other column, use `map`:

<!---FUN columnAccessorMap-->

```kotlin
val age by column<Int>()
val year by age.map { 2021 - it }

df.filter { year > 2000 }
```

<!---END-->