import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// Класс для установления соединения с сервером PostgreSQL

public class Connect {

    public static Connection connect(String url, String user, String password) {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to the PostgreSQL server successfully.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }
}
