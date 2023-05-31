// "jdbc:sqlite:./mybase1.db"
public class Main {
    public static KDBC.XDBC JDBX(String db){
        return KDBC.DBC(db);
    }
    public static void main(String[] args){
        JDBX("jdbc:sqlite:./mybase1.db").tableMod("h");
    }
}
