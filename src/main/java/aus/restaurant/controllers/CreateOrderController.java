package aus.restaurant.controllers;
import aus.restaurant.db.DatabaseManager;
import aus.restaurant.models.AppUtils;
import aus.restaurant.models.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;


//Controller to handle all the cretion functionality are declared down here
public class CreateOrderController {
    @FXML
    private TextField burritosField;

    @FXML
    private TextField friesField;

    @FXML
    private TextField sodaField;

    @FXML
    private TextField mealField;

    @FXML
    private Button addToBasketButton;

    @FXML
    private Button backButton;

    private User user;
    private String existingOrderNumber;

    //Sets the current user and loads their existing order if any
    public void setUser(User user) {
        this.user = user;
        mealField.setVisible(user.isVip());
        loadExistingOrder();
    }

    //Loads the existing order for the user if any
    private void loadExistingOrder() {
        String query = "SELECT o.order_number, oi.food_item_id, oi.quantity FROM orders o " +
                "JOIN order_items oi ON o.order_id = oi.order_id " +
                "WHERE o.user_id = ? AND o.status = 'Pending'";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, user.getUserId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                existingOrderNumber = rs.getString("order_number");
                int foodItemId = rs.getInt("food_item_id");
                int quantity = rs.getInt("quantity");

                switch (foodItemId) {
                    case 1:
                        burritosField.setText(String.valueOf(quantity));
                        break;
                    case 2:
                        friesField.setText(String.valueOf(quantity));
                        break;
                    case 3:
                        sodaField.setText(String.valueOf(quantity));
                        break;
                    case 4:
                        mealField.setText(String.valueOf(quantity));
                        break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Handles the addition of the order to the basket
    @FXML
    private void handleAddToBasket(ActionEvent event) {
        try {
            int burritos = parseQuantity(burritosField.getText());
            int fries = parseQuantity(friesField.getText());
            int sodas = parseQuantity(sodaField.getText());
            int meals = user.isVip() ? parseQuantity(mealField.getText()) : 0;

            if ((burritos + fries + sodas + meals) > 0) {
                String orderNumber = existingOrderNumber != null ? existingOrderNumber : UUID.randomUUID().toString();

                double totalPrice = calculateTotalPrice(burritos, fries, sodas, meals);
                int totalTime = calculateTotalTime(burritos, fries);

                try (Connection conn = DatabaseManager.getConnection()) {
                    if (existingOrderNumber == null) {
                        String orderQuery = "INSERT INTO orders (user_id, order_number, total_price, total_time, status, order_date) VALUES (?, ?, ?, ?, ?, datetime('now'))";
                        try (PreparedStatement orderStmt = conn.prepareStatement(orderQuery)) {
                            orderStmt.setInt(1, user.getUserId());
                            orderStmt.setString(2, orderNumber);
                            orderStmt.setDouble(3, totalPrice);
                            orderStmt.setInt(4, totalTime);
                            orderStmt.setString(5, "Pending");
                            orderStmt.executeUpdate();
                        }
                    } else {
                        String orderQuery = "UPDATE orders SET total_price = ?, total_time = ? WHERE order_number = ?";
                        try (PreparedStatement orderStmt = conn.prepareStatement(orderQuery)) {
                            orderStmt.setDouble(1, totalPrice);
                            orderStmt.setInt(2, totalTime);
                            orderStmt.setString(3, orderNumber);
                            orderStmt.executeUpdate();
                        }

                        String deleteOrderItemsQuery = "DELETE FROM order_items WHERE order_id = (SELECT order_id FROM orders WHERE order_number = ?)";
                        try (PreparedStatement deleteStmt = conn.prepareStatement(deleteOrderItemsQuery)) {
                            deleteStmt.setString(1, orderNumber);
                            deleteStmt.executeUpdate();
                        }
                    }

                    int orderId;
                    String orderIdQuery = "SELECT order_id FROM orders WHERE order_number = ?";
                    try (PreparedStatement orderIdStmt = conn.prepareStatement(orderIdQuery)) {
                        orderIdStmt.setString(1, orderNumber);
                        try (ResultSet rs = orderIdStmt.executeQuery()) {
                            rs.next();
                            orderId = rs.getInt("order_id");
                        }
                    }

                    String orderItemQuery = "INSERT INTO order_items (order_id, food_item_id, quantity) VALUES (?, ?, ?)";
                    try (PreparedStatement orderItemStmt = conn.prepareStatement(orderItemQuery)) {
                        insertOrderItem(orderItemStmt, orderId, 1, burritos);
                        insertOrderItem(orderItemStmt, orderId, 2, fries);
                        insertOrderItem(orderItemStmt, orderId, 3, sodas);
                        if (user.isVip()) {
                            insertOrderItem(orderItemStmt, orderId, 4, meals);
                        }
                    }

                    AppUtils.showAlert(Alert.AlertType.INFORMATION, "Order Added", "Your order has been added to the basket.");
                    navigateToBasket();
                } catch (SQLException e) {
                    e.printStackTrace();
                    AppUtils.showAlert(Alert.AlertType.ERROR, "Order Error", "Failed to add order to the basket. Please try again.");
                }
            } else {
                AppUtils.showAlert(Alert.AlertType.WARNING, "Invalid Input", "Please enter a valid quantity for at least one item.");
            }
        } catch (NumberFormatException e) {
            AppUtils.showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter valid numeric quantities.");
        }
    }

    //Parses the quantity from the given text
    private int parseQuantity(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Invalid quantity: " + text);
        }
    }

    // Navigates back to the dashboard
    @FXML
    private void handleBackToDashboard(ActionEvent event) {
        AppUtils.navigateToDashboard((Stage) backButton.getScene().getWindow(), user);
    }

    //Calculates the total price of the order.
    //returns the total price

    private double calculateTotalPrice(int burritos, int fries, int sodas, int meals) {
        double totalPrice = burritos * 7.00 + fries * 4.00 + sodas * 2.50 + meals * 10.00;
        if (user.isVip() && meals > 0) {
            totalPrice -= meals * 3.00;
        }
        return totalPrice;
    }

    //Calculates the total time required for the order.
    //return the total time

    private int calculateTotalTime(int burritos, int fries) {
        int friesLeft = 0;

        String friesLeftQuery = "SELECT friesleft FROM food_items WHERE food_item_id = 2";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(friesLeftQuery);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                friesLeft = rs.getInt("friesleft");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        int friesTime = 0;
        if (fries > friesLeft) {
            friesTime = ((fries - friesLeft + 4) / 5) * 8;
            friesLeft = ((fries - friesLeft + 4) / 5) * 5 - (fries - friesLeft);
        } else {
            friesLeft -= fries;
        }

        String updateFriesLeftQuery = "UPDATE food_items SET friesleft = ? WHERE food_item_id = 2";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(updateFriesLeftQuery)) {
            stmt.setInt(1, friesLeft);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        int burritoTime = (burritos + 1) / 2 * 9;

        return Math.max(burritoTime, friesTime);
    }

    //Inserts the order item into the database
    //throws SQLException if a database access error occurs

    private void insertOrderItem(PreparedStatement stmt, int orderId, int foodItemId, int quantity) throws SQLException {
        if (quantity > 0) {
            stmt.setInt(1, orderId);
            stmt.setInt(2, foodItemId);
            stmt.setInt(3, quantity);
            stmt.executeUpdate();
        }
    }

    //Navigates to the basket page
    private void navigateToBasket() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/basket.fxml"));
            Stage stage = (Stage) addToBasketButton.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Basket");

            ViewBasketController controller = loader.getController();
            controller.setUser(user);

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            AppUtils.showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to navigate to the basket page.");
        }
    }

}
