import java.util.List;

public class Main {
    public static String url = "jdbc:sqlite:./mybase1.db";
    public static EasyDBC.EasyDBC example(String url){
        return EasyDBC.easyDBC(url);
    }
    public static void main(String[] args){
        //here are some examples of methods that format sql commands for you.
        example(url).dropTable("ExampleTable");
        example(url).createTable("ExampleTable","name", "str");
        example(url).modTable("ExampleTable").addColumn("password", "text");
        example(url).modTable("ExampleTable").addRow(List.of("user","password"));
        //the line below throws an exception because the columns don't match the number of entries.
        example(url).modTable("ExampleTable").addRow(List.of("user2","password2", "thirdentry?"));
    }
}