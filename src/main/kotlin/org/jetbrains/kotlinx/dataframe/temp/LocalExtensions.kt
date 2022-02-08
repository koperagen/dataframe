import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.DataRow
import org.jetbrains.kotlinx.dataframe.annotations.DataSchema
import org.jetbrains.kotlinx.dataframe.api.add
import org.jetbrains.kotlinx.dataframe.api.cast
import org.jetbrains.kotlinx.dataframe.api.filter
import org.jetbrains.kotlinx.dataframe.io.writeCSV

// Расширяемые датафреймы - калька с extensible records.
// Фича позволяет добавлять новые поля в запись в компайл тайме,
// т.е. сразу после добавления поле можно прочитать типизированно

@DataSchema
public interface Cars {
    public val year: Int
}

// Как хотелось бы
//fun filterOldCars_goal(cars: DataFrame<Cars>) {
//    val df1 = cars
//        .add("age") { 2022 - year }
//        .filter { age > 20 }
//
//    df1.writeCSV("old_cars.csv")
//}

// Проблема #1: нет локальных экстеншен пропертей
//fun filterOldCars_methods(cars: DataFrame<Cars>) {
//    val df1 = cars.add("age") { 2022 - year }
//        .filter { age() > 20 }
//
//    df1.writeCSV("old_cars.csv")
//}

// Во что это можно трансформировать
//fun filterOldCars_after(cars: DataFrame<Cars>) {
//    val temp1 = cars.add("age") { 2022 - year }
//    // Супертип Cars нужен, чтобы сохранить возможность объявлять полиморфные функции
//    // и вызывать их для производных датафреймов
//    open class Cars1 : Cars {
//        override val year: Int
//            get() = error("should not be called")
//    }
//    fun DataRow<Cars1>.age(): Int = TODO()
//    val temp2 = temp1.cast<Cars1>().filter { age() > 20 }
//
//    val df1 = temp2
//
//    df1.writeCSV("old_cars.csv")
//}
// Проблема #2: Как я понимаю, взять и заменить цепочку вызовов
// cars.add("age") { 2022 - year }.filter { age() > 20 }
// на список из переменных, локальных деклараций классов, функций на фронтенде нельзя - фундаментальное ограничение
// Проблема #3: Нужен доступ к  АПИ резолюшен скоупов для добавления новых классов, функций.
// Такое пока разрешать не хотят

// Финальная версия
// Особенность: После решения этих трех проблем останется ещё тот факт,
// что локальные экстеншены видимы только внутри одной функции.
//fun filterOldCars_final(cars: DataFrame<Cars>) {
//    val df1 = cars.add("age") { 2022 - year }
//    val df2 = df1.filter { age() > 20 }
//
//    df2.writeCSV("old_cars.csv")
//}

// Но в скриптинге (в ноутбуках) правила другие, и там, возможно, получится реализовать самый первый вариант
// С расчетом на то, что это будет работать и внутри ячейки, и между ячейками
//fun filterOldCars_goal(cars: DataFrame<Cars>) {
//    val df1 = cars
//        .add("age") { 2022 - year }
//        .filter { age > 20 }
//
//    df1.writeCSV("old_cars.csv")
//}

