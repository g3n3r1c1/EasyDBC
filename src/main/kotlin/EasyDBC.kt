import kotlin.text.StringBuilder
import java.sql.*


/**
 * A small package that provides a host of wrapper functions for common SQL queries and commands.
 * This allows for a more robust experience using SQL code within Java, Scala, and Kotlin.
 * currently only designed for SQLite. Postgres and MySQL compatibility is planned.
 */
object EasyDBC{
    /**
     * this method simply returns an instance of EasyDBC(db).
     * @param[db]: this will be your database URL.
     */
    @JvmStatic
    fun easyDBC(db: String): EasyDBC = EasyDBC(db)

    /**
     * class coupled with the method "dbCon" from the object EasyDBC
     * @see[easyDBC]
     */
     open class EasyDBC(db: String) {

        private val url = db
        private fun tCtrl(sql: String){
            DriverManager.getConnection(url).createStatement().execute(sql)
            DriverManager.getConnection(url).close()
        }

        private fun qCtrl(sql: String): ResultSet {
            return DriverManager.getConnection(url).createStatement().executeQuery(sql)
        }

        private fun inferredType(type: String): String{
            return when (type.uppercase()){
                in arrayOf("INT", "NUM", "NUMBER", "DIGIT", "INTEGER") -> "INTEGER"
                in arrayOf("STRING", "STR", "ALPHA", "CHAR", "TEXT") -> "TEXT"
                else -> throw Exception("type is not valid.")
            }
        }


        private fun tableList(): ArrayList<String>{
            val tables = ArrayList<String>()
            val rSet = qCtrl("SELECT tbl_name FROM sqlite_master WHERE type='table'")
            while(rSet.next())
                tables.add(rSet.getString(1))
            return tables
        }


        /**
         * Creates a new table within the database if a table by the name does not exist.
         * The primary is an incremental integer labelled "id" by default
         * @param[name]: specifies the name of the table.
         */
        fun createTable(name: String){
            tCtrl("CREATE TABLE IF NOT EXISTS ${name.replace(' ','_')}(id INTEGER INCREMENT PRIMARY KEY)")
        }
        /**
         * Creates a new table within the database if a table by the name does not exist.
         * The key type will be an incremental integer by default.
         * @param[name]: specifies the name of the table.
         * @param[primaryKey]: specifies the primary key name.
         * */
        fun createTable(name: String, primaryKey: String){
            tCtrl("CREATE TABLE IF NOT EXISTS ${name.replace(' ','_')}($primaryKey INTEGER INCREMENT PRIMARY KEY)")
        }

        /**
         * Creates a new table within the database if a table by the name does not exist.
         * @param[name]: specifies the name of the table.
         * @param[primaryKey]: specifies the primary key name.
         * @param[keyType]:specifies a key type. may be either integer or text.
         * */
        fun createTable(name: String, primaryKey: String, keyType: String){
            val inferred: String = inferredType(keyType)
            tCtrl("CREATE TABLE IF NOT EXISTS ${name.replace(' ','_')}($primaryKey $inferred PRIMARY KEY)")
        }
        /**
         * Drops a table within the database.
         * @param[name]: specifies the name of the table.
         */
        fun dropTable(name: String){
            tCtrl("DROP TABLE IF EXISTS ${name.replace(' ','_')}")
        }

        /**
         * A subclass specifically designated for operations such as adding and removing rows and columns.
         * Here it's coupled with an instantiation method.
         * @param[table]: Here we specify which of the tables in our database we would like to operate on.
         * @return: this.ModTable(Table)
         */
        fun modTable(table: String): ModTable{
            if(table.replace(' ','_') in tableList())
                return this.ModTable(table)
            throw Exception("Error: The table $table may not exist.")
        }

        /**
         * Subclass coupled with the "tableMod" instantiation method.
         * @see[modTable]
         */

        inner class ModTable(table: String) {

            private val tbl = table

            private fun typeCheck(param: Any, dbcol: Int){
                val rSet = qCtrl("SELECT * from $tbl")
                var c = rSet.metaData.getColumnTypeName(dbcol).lowercase()

                if ((param is String) && ("text" in c) ||
                    (param is Int || param is Long) && ("integer" in c)) {
                    rSet.close()
                    return
                }
                c = if(c.startsWith("TYPE ")) c.drop(5) else c
                c = "${rSet.metaData.getColumnName(dbcol)}: $c"
                rSet.close()
                throw Exception("Error: Type mismatch between value: $param and column $c")
            }

            /**
             * Drops columns by their name. currently lacks the ability to drop by column_id.
             */
            fun dropColumn(column: String){
                tCtrl("ALTER TABLE $tbl DROP COLUMN $column")
            }

            /**
             * Adds a column to the table of choice.
             * currently the type selection does not encompass all types that can
             * exist within an SQL table, which is subject to change.
             * @param[name]: Specifies the name that the column may be added.
             * @param[type]: Selects type from either INTEGER or TEXT.
             */
            fun addColumn(name: String, type: String) {
                    tCtrl("ALTER TABLE $tbl ADD COLUMN $name type ${inferredType(type)}")
            }

            /**
             * Adds a row to the table defined within the subclass.
             * the columns are inferred automatically so that you only need to type out the data you
             * normally would in SQL.
             * it currently lacks the ability to skip distinct columns.
             * @param[values]: is passed as a List. the order of the entries in the list must match the order
             * of columns in your table.
             * */
            fun addRow(values: List<Any>){
                val rSet = qCtrl("SELECT * from $tbl")
                if (values.size == rSet.metaData.columnCount) {
                    val cols: StringBuilder = StringBuilder()
                    val valSet: StringBuilder = StringBuilder()
                    for (i in 1..values.size) {
                        typeCheck(values[i - 1], i)
                        cols.append(", " + rSet.metaData.getColumnName(i))
                        valSet.append(", " + (if(values[i-1] is String) """'${values[i-1]}'""" else "${values[i-1]}"))
                    }
                    rSet.close()
                    tCtrl("INSERT OR REPLACE INTO $tbl" +
                            " (${cols.toString().drop(2)}) VALUES (${valSet.toString().drop(2)})")
                    return
                }
                val counted = rSet.metaData.columnCount
                rSet.close()
                throw Exception("Error: ${values.size} arguments in addRow. $tbl has $counted columns.")
            }

            /**
             * Returns a subclass that allows for multiple singular condition delete statements.
             * Currently, multi-conditional deletion is unsupported, this is subject to change.
             * @param[column]: this is the column checked for a condition. it is not case-sensitive.
             * If you pass this parameter with any space characters, they will be internally handled as underscores.
             */
            fun deleteRow(column: String): DeleteRow {
                 return this.DeleteRow(column)
            }
            inner class DeleteRow(nameCol: String) {
                private val colName = nameCol.replace(' ', '_')

                private fun execute(condition: String, data: Any) {
                    val rSet = qCtrl("SELECT * from $tbl")
                    for (i in 1..rSet.metaData.columnCount)
                        if ((data is String) == (rSet.metaData.getColumnTypeName(i) == "TYPE TEXT") &&
                            rSet.metaData.getColumnName(i).lowercase() == colName.lowercase()){
                            rSet.close()
                            tCtrl("DELETE FROM $tbl WHERE $colName $condition")
                            return
                        }
                    rSet.close()
                    throw Exception("Error in deleteRow: invalid parameters.")
                }

                fun isMatch(value: Any){
                    when (value) {
                        is String -> execute("""= '$value'""", value)
                        is Int -> execute("= $value", value)
                        is Long -> execute("= $value", value)
                        else -> throw Exception("Error in deleteRow: invalid type given.")
                    }
                    return
                }


                fun notMatch(value: Any){
                    when (value) {
                        is String -> execute("""<> '$value'""", value)
                        is Int -> execute("<> $value", value)
                        is Long -> execute("<> $value", value)
                        else -> throw Exception("Error in deleteRow: invalid type given.")
                    }
                    return
                }

                fun inRange(lower: Int, upper: Int){
                    if(lower >= upper)
                        throw Exception("Error: the lower bound must be less than the upper bound."
                        +" Consider using match or notInRange instead?")
                    execute("BETWEEN $lower AND $upper", Pair(lower, upper))
                }

                fun notInRange(lower: Int, upper: Int){
                    if(lower >= upper)
                        throw Exception("Error: the lower bound must be less than the upper bound."
                                +" Consider using match or InRange instead?")
                    execute("NOT BETWEEN $lower AND $upper", Pair(lower, upper))
                }

            }
        }
    }
}