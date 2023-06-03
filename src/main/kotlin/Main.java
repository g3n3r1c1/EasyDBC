import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
// "jdbc:sqlite:./mybase1.db"
public class Main {
    public static KDBC.XDBC JDBX(String db){
        return KDBC.DBC(db);
    }
    public static void main(String[] args){
        String url = "jdbc:sqlite:./mybase1.db";

        for (int i = 16; i < 30; i++) {
            JDBX(url).modTable("Ambrabam").addRow(List.of(i, "hello", (i*i)));
        }
        JDBX(url).modTable("Ambrabam").deleteRow("binkin").isMatch("hello");
        JDBX(url).modTable("Ambrabam").addRow(List.of(100, "bruh", 100));
    }
}
