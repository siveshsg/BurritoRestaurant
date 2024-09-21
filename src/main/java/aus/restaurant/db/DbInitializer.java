package aus.restaurant.db;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;


//class to create the table with create queries
public class DbInitializer {
    public static void initialize() {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {

            String usersTable = "CREATE TABLE IF NOT EXISTS users (" +
                    "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT UNIQUE NOT NULL, " +
                    "password TEXT NOT NULL, " +
                    "first_name TEXT NOT NULL, " +
                    "last_name TEXT NOT NULL, " +
                    "is_vip BOOLEAN DEFAULT FALSE, " +
                    "email TEXT UNIQUE, " +
                    "credits INTEGER DEFAULT 0)";

            String ordersTable = "CREATE TABLE IF NOT EXISTS orders (" +
                    "order_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER, " +
                    "order_number TEXT UNIQUE NOT NULL, " +
                    "total_price REAL NOT NULL, " +
                    "total_time REAL NOT NULL, " +
                    "status TEXT NOT NULL, " +
                    "order_date TEXT NOT NULL, " +
                    "collect_date TEXT, " +
                    "FOREIGN KEY (user_id) REFERENCES users(user_id))";

            String orderItemsTable = "CREATE TABLE IF NOT EXISTS order_items (" +
                    "order_item_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "order_id INTEGER, " +
                    "food_item_id INTEGER, " +
                    "quantity INTEGER NOT NULL, " +
                    "FOREIGN KEY (order_id) REFERENCES orders(order_id), " +
                    "FOREIGN KEY (food_item_id) REFERENCES food_items(food_item_id))";

            String foodItemsTable = "CREATE TABLE IF NOT EXISTS food_items (" +
                    "food_item_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT UNIQUE NOT NULL, " +
                    "price REAL NOT NULL, " +
                    "friesleft INTEGER DEFAULT 0)";

            String creditsTable = "CREATE TABLE IF NOT EXISTS credits (" +
                    "credit_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER, " +
                    "order_id INTEGER, " +
                    "credits_earned INTEGER NOT NULL, " +
                    "credits_used INTEGER NOT NULL, " +
                    "FOREIGN KEY (user_id) REFERENCES users(user_id), " +
                    "FOREIGN KEY (order_id) REFERENCES orders(order_id))";

            String paymentDetailsTable = "CREATE TABLE IF NOT EXISTS payment_details (" +
                    "payment_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "order_id INTEGER, " +
                    "card_number TEXT NOT NULL, " +
                    "expiry_date TEXT NOT NULL, " +
                    "cvv TEXT NOT NULL, " +
                    "FOREIGN KEY (order_id) REFERENCES orders(order_id))";

            stmt.execute(usersTable);
            stmt.execute(ordersTable);
            stmt.execute(orderItemsTable);
            stmt.execute(foodItemsTable);
            stmt.execute(creditsTable);
            stmt.execute(paymentDetailsTable);


            String insertFoodItems = "INSERT INTO food_items (name, price) VALUES " +
                    "('Burrito', 7.00), " +
                    "('Fries', 4.00), " +
                    "('Soda', 2.50), " +
                    "('Meal', 10.00) " +
                    "ON CONFLICT(name) DO NOTHING";

            stmt.execute(insertFoodItems);

            System.out.println("All tables created or already exist");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
