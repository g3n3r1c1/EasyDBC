import org.sqlite.BusyHandler
import java.sql.*
import busiest.busyman as newHandler

/**
 * A small package that provides a host of wrapper functions for common SQL queries and commands.
 * This allows for a more robust experience using SQL code within Kotlin and Java.
 * currently only designed for SQLite. Postgres and MySQL compatibility is planned.
 */
object KDBC{
    /**
     * this method simply returns an instance of XDBC(db).
     * @param[db]: this will be your database URL.
     */
    @JvmStatic
    public fun DBC(db: String): XDBC = XDBC(db)

    /**
     * class coupled with the instantiator method "DBC" from the object KDBC
     * @see[DBC]
     */
     open class XDBC(db: String) {

        private val url = db
        private fun Tctrl(sql: String): Unit {
            var conn = DriverManager.getConnection(url)
            conn.autoCommit = false
            conn.beginRequest()
            conn.createStatement().execute(sql)
            conn.commit()
            conn.endRequest()
            conn.close()
        }

        private fun Qctrl(sql: String): ResultSet {
            return DriverManager.getConnection(url).createStatement().executeQuery(sql)
        }


        /**
         * Creates a new table within the database if a table by the name does not exist.
         * The primary is an incremental integer labelled "id" by default
         * @param[Name]: specifies the name of the table.
         */
        fun createTable(Name: String): Unit {
            Tctrl("CREATE TABLE IF NOT EXISTS ${Name}(id INTEGER INCREMENT PRIMARY KEY)")
        }
        /**
         * Creates a new table within the database if a table by the name does not exist.
         * The key type will be an incremental integer by default.
         * @param[Name]: specifies the name of the table.
         * @param[PrimaryKey]: specifies the primary key name.
         * */
        fun createTable(Name: String, PrimaryKey: String): Unit {
            Tctrl("CREATE TABLE IF NOT EXISTS ${Name}(${PrimaryKey} INTEGER INCREMENT PRIMARY KEY)")
        }

        /**
         * Creates a new table within the database if a table by the name does not exist.
         * @param[Name]: specifies the name of the table.
         * @param[PrimaryKey]: specifies the primary key name.
         * @param[KeyType]:specifies a key type. may be either integer or text.
         * */
        fun createTable(Name: String, PrimaryKey: String, KeyType: String): Unit {
            var utype: String =
                when (KeyType.uppercase()){
                    in arrayOf("INT", "NUM", "NUMBER", "DIGIT", "INTEGER") -> "INTEGER"
                    in arrayOf("STRING", "ALPHA", "CHAR", "TEXT") -> "TEXT"
                    else -> throw Exception("type is not valid.")
                }
            Tctrl("CREATE TABLE IF NOT EXISTS ${Name}(${PrimaryKey} ${KeyType.uppercase()} PRIMARY KEY)")
        }
        /**
         * Drops a table within the database.
         * @param[Name]: specifies the name of the table.
         */
        fun dropTable(Name: String): Unit {
            Tctrl("DROP TABLE ${Name}")
        }

        /**
         * A subclass specifically designated for operations such as adding and removing rows and columns.
         * Here it's coupled with an instantiation method.
         * @param[Table]: Here we specify which of the tables in our database we would like to operate on.
         * @return: this.SCTMD(Table)
         */

        //TODO: Add better safety against checking for nonexistent tables
        public fun modTable(Table: String): SCTMD{
//
            try{
                return this.SCTMD(Table)
            }
            catch (e: Exception){
                throw Exception("Error: The table ${Table} possibly does not exist.")
            }
        }

        /**
         * Subclass coupled with the "tableMod" instantiation method.
         * @see[modTable]
         */
        inner class SCTMD(Table: String) {

            private val Tbl = Table

            private fun TypeCheck(param: Any, dbcol: Int): Unit{
                var RSet = Qctrl("SELECT * from ${Tbl}")
                var c = RSet.metaData.getColumnTypeName(dbcol)

                if ((param is String) && (c == "TYPE TEXT") ||
                    (param is Int || param is Long) && (c == "TYPE INTEGER"|| c == "INTEGER INCREMENT")) {
                    RSet.close()
                    return
                }
                c = if(c.startsWith("TYPE ")) c.drop(5) else c
                c = "${RSet.metaData.getColumnName(dbcol)}: ${c.lowercase()}"
                RSet.close()
                throw Exception("Error: Type mismatch between value: ${param} and column ${c}")
            }

            /**
             * Drops columns by their name. currently lacks the ability to drop by column_id.
             */
            fun dropColumn(Col: String) {
                Tctrl("ALTER TABLE ${Tbl} DROP COLUMN ${Col}")
            }

            /**
             * Adds a column to the table of choice.
             * currently the type selection does not encompass all types that can
             * exist within an SQL table, which is subject to change.
             * @param[Name]: Specifies the name that the column may be added.
             * @param[Type]: Selects type from either INTEGER or TEXT.
             */
            public fun addColumn(Name: String, Type: String) {
                var utype: String =
                when (Type.uppercase()){
                    in arrayOf("INT", "NUM", "NUMBER", "DIGIT", "INTEGER") -> "INTEGER"
                    in arrayOf("STRING", "ALPHA", "CHAR", "TEXT") -> "TEXT"
                    else -> throw Exception("type is not valid.")
                }
                    Tctrl("ALTER TABLE ${Tbl} ADD COLUMN ${Name} type ${utype}")
            }

            /**
             * Adds a row to the table defined within the subclass.
             * the columns are inferred automatically so that you only need to type out the data you
             * normally would in SQL.
             * it currently lacks the ability to skip distinct columns.
             * @param[Insertion]: is passed as a List. the order of the entries in the list must match the order
             * of columns in your table.
             * */
            public fun addRow(values: List<Any>): Unit {
                var RSet = Qctrl("SELECT * from ${Tbl}")
                if (values.size == RSet.metaData.columnCount) {
                    var cols: String = "."
                    var Insertion: String = "."
                    for (i in 1..values.size) {
                        TypeCheck(values[i - 1], i)
                        cols = "${cols}, ${RSet.metaData.getColumnName(i)}"
                        Insertion =
                            if (values[i - 1] is String) """${Insertion}, '${values[i - 1]}'"""
                            else "${Insertion}, ${values[i - 1]}"

                    }
                    cols = cols.drop(3)
                    Insertion = Insertion.drop(3)
                    RSet.close()
                    println("INSERT OR REPLACE INTO ${Tbl} (${cols}) VALUES (${Insertion})")
                    Tctrl("INSERT OR REPLACE INTO ${Tbl} (${cols}) VALUES (${Insertion})")
                    return
                }
                var counted = RSet.metaData.columnCount
                RSet.close()
                throw Exception("Error: ${values.size} arguments in addRow. ${Tbl} has ${counted} columns.")
            }

            /**
             * deletes a row where the data within the column name matches the row.
             * @param[ColName]: this is the column checked for a match by value. it is not case sensitive.
             * @param[Data]: this is the data checked for a match within the column of choice.
             * This parameter may only recieve a value of type Int/Long or String. Strings are case sensitive.
             */
            public fun deleteRow(ColName: String, Data: Any): Unit{
                var RSet = Qctrl("SELECT * from ${Tbl}")
                val final_command: String =
                when(Data){
                    is String -> """DELETE FROM ${Tbl} WHERE ${ColName} = '${Data}'"""
                    is Int -> """DELETE FROM ${Tbl} WHERE ${ColName} = ${Data}"""
                    is Long -> """DELETE FROM ${Tbl} WHERE ${ColName} = ${Data}"""
                    else -> throw Exception("Error in deleteRow: invalid type given.")
                }
                for (i in 1 .. RSet.metaData.columnCount)
                    if ((Data is String) == (RSet.metaData.getColumnTypeName(i) == "TYPE TEXT") &&
                        RSet.metaData.getColumnName(i).lowercase() == ColName.lowercase()) {
                        Tctrl(final_command)
                    }
                throw Exception("Error in deleteRow: invalid parameters.")
            }
        }
    }
}