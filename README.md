# General Description

A simple library to wrap common SQL commands and queries in Java/Kotlin syntax. It is dependent on the java.sql library.

The primary goal of this project is to make writing SQL in Java applications unintrusive, for people who may prefer to not write SQL generally. The other goal is make controlling your database simple and intuitive, where it may not be such in SQL.

It's currently extremely unfinished, but the first alpha versions should be accessible for you.

It's only been tested with SQLite as of now.

# Functionality Example

An example of an SQL command formatted to Java may be:

```sql
INSERT INTO <table> (col1, col2... coln) VALUES (val1, val2... valn);
```

which becomes
```java
public void databaseoperation(){
        KDBC.DBC("url").modTable("table").addRow(List.of(val1,val2,valn))
        }
```

The goal is to never produce an SQL error of any kind, instead the library should throw concise and meaningful exceptions to you, without actually performing the command.


# Roadmap
* **Ensure that other SQL drivers are fully compatible with this library,** such as Postgres and MySQL. Preferably this would be done without creating specific subclasses for any driver.
* **Expand the list of methods to support more distinct SQL commands.** the library is currently extremely small, only supporting some of the most basic SQL commands. 
* **Create an unsafe version of the class?** This would mainly exist to provide speed gains for those who may need it, but if you really need that, you should maybe consider writing your own SQL code.
* **Possibly provide a manual for use in Clojure, as well as better integration.** This library would ideally be usable from any JVM language, but Clojure seems to cause some trouble. Java/Scala/Kotlin work just fine however.


# Closing note
It should be made clear that this is not a full substitute for SQL. At the very least not now. It can make commands easy for you if you're writing some simple code to operate on a small/average database, but for more advanced databases, there are far better options than this.
