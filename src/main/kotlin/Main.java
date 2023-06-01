import java.util.ArrayList;
import java.util.Arrays;

// "jdbc:sqlite:./mybase1.db"
public class Main {
    public static KDBC.XDBC JDBX(String db){
        return KDBC.DBC(db);
    }
    public static void main(String[] args){
        String url = "jdbc:sqlite:./mybase1.db";

        JDBX(url).modTable("Ambrabam").addRow(Arrays.asList(86, "h0llo", 198));
        JDBX(url).modTable("Ambrabam").addRow(Arrays.asList(56, "h0llo", 198));
        JDBX(url).modTable("Ambrabam").addRow(Arrays.asList(26, "h0llo", 198));
        //JDBX(url).modTable("Ambrabam").deleteRow("binkin", "bruh");
    }
}
