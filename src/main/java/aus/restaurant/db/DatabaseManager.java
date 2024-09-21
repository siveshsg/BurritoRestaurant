package aus.restaurant.db;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;



//Manages the database connection for the application.

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:burritoking.db";
    static {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            if (conn != null) {
                System.out.println("Connected to the database.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    //throws SQLException if a database access error occur
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
}
