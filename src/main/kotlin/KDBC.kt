import java.sql.*

/**
 * A small package that provides a host of wrapper functions for common SQL queries and commands.
 * This allows for a more robust experience using SQL code within Kotlin and Java.
 */
object KDBC {
    /**
     * this method simply returns an instance of XDBC(db).
     * @param[db]: this will be your database URL.
     */
    @JvmStatic
    public fun DBC(db: String): XDBC = XDBC(db)

    /**
     * class coupled with the instantiator method "DBC" from the object.
     * @see[DBC]
     */
    class XDBC(db: String) {

        private val url = db
        private fun Tctrl(sql: String) {
            DriverManager.getConnection(url).createStatement().execute(sql)
            //ctrlr.commit()
            DriverManager.getConnection(url).close()
        }

        private fun Qctrl(sql: String): ResultSet {
            return DriverManager.getConnection(url).createStatement().executeQuery(sql)
        }

        /**
         * Creates a new table within the database. Uses IF NOT EXISTS for safety.
         * Currently initializes an incremental integer id by default.
         * This is subject to change.
         */
        public fun CreateTable(Name: String) =
            Tctrl("CREATE TABLE IF NOT EXISTS ${Name}(id INTEGER INCREMENT PRIMARY KEY)")

        /**
         * Drops a table within the database.
         */
        public fun DropTable(Name: String) = Tctrl("DROP TABLE ${Name}")

        /**
         * A subclass specifically designated for operations such as adding and removing rows and columns.
         * Here it's coupled with an instantiation method
         * @param[Table]: Here we specify which of the tables in our database we would like to operate on.
         * @return: this.Sc_tmd(Table)
         */
        public fun tableMod(Table: String): Sc_tmd = this.Sc_tmd(Table)

        /**
         * Subclass coupled with the "tableMod" instantiation method.
         * @see[tableMod]
         */
        inner class Sc_tmd(Table: String) {
            private val Tbl = Table

            /**
             * Drops columns by their name. currently lacks the ability to drop by column_id.
             */
            fun dropColumn(Col: String) = Tctrl("ALTER TABLE ${Tbl} DROP COLUMN ${Col}")

            /**
             * Adds a column to the table of choice.
             */
            public fun addColumn(Name: String, Type: String) {
                val utype: String = Type.uppercase()
                if (utype == "TEXT" || utype == "INTEGER")
                    Tctrl("ALTER TABLE ${Tbl} ADD COLUMN ${Name} type ${utype}")
                else
                    throw Exception("type is not valid.")
            }

            /**
             * Adds a row to the table defined within the subclass.
             * the columns are inferred automatically so that you only need to type out the data you
             * normally would in SQL.
             * it currently lacks the ability to skip distinct columns.
             * @param[Insertion]: must be encased within three speech marks. substrings within the
             * complete body of text must be accompanied by their own speech marks. e.g ("""1, "string"""")
             * for a two column table where column 1 is of type integer, and column 2 is of type text.
             * */
            public fun addRow(Insertion: String) {
                val RSMD = Qctrl("SELECT * from ${Tbl}").metaData
                var cols: String = RSMD.getColumnName(1)

                for (i in 2..RSMD.columnCount)
                    cols = "${cols}, ${RSMD.getColumnName(i)}"

                Tctrl("""INSERT INTO ${Tbl} (${cols}) VALUES (${Insertion}) """)
            }

            /**
             * deletes a row where the data within the column name matches the row.
             * @param[ColName]: this is the column checked for a match by name, not by id.
             * @param[Data]: this is the data checked for a match within the column of choice.
             */
            public fun deleteRow(ColName: String, Data: String) {
            }
        }
    }
}
//hello


/*
ResultSet rs = stmt.executeQuery("SELECT a, b, c FROM TABLE2");
ResultSetMetaData rsmd = rs.getMetaData();
int columnCount = rsmd.getColumnCount();

// The column count starts from 1
for (int i = 1; i <= columnCount; i++ ) {
  String name = rsmd.getColumnName(i);
  // Do stuff with name
}
*/