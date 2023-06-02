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

        for (int i = 1; i < 15; i++) {
            JDBX(url).modTable("Ambrabam").addRow(List.of(i, "hello", (i*i)));
        }
        JDBX(url).modTable("Ambrabam").deleteRow("konzi").notInRange(600, 1200);
    }
}
