import org.jetbrains.annotations.NotNull;
import org.sqlite.BusyHandler;

import java.sql.SQLException;

/**
 * literally JUST instantiates busyhandler for sqlite. that's it.
 */
public class busiest{
    public static org.sqlite.BusyHandler busyman(){
        return new BusyHandler() {
            @Override
            protected int callback(int nbPrevInvok) throws SQLException {
                return 0;
            }
        };
    }
}
