import java.util.ArrayList;

// "jdbc:sqlite:./mybase1.db"
public class Main {
    public static KDBC.XDBC JDBX(String db){
        return KDBC.DBC(db);
    }
    public static void main(String[] args){
        ArrayList<Object> hi = new ArrayList<>();
        hi.add(4);
        hi.add("bruh");
        hi.add(17);
        JDBX("jdbc:sqlite:./mybase1.db").modTable("Ambrabam").deleteRow("binkin", "bruh");
    }
}
