package aus.restaurant.controllers;
import aus.restaurant.db.DatabaseManager;
import aus.restaurant.models.AppUtils;
import aus.restaurant.models.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;


//Controller to handle all the collection functionality are declared down here
public class CollectOrderController {

    @FXML
    private TableView<Order> ordersTableView;

    @FXML
    private TableColumn<Order, String> orderNumberColumn;

    @FXML
    private TableColumn<Order, String> foodItemsColumn;

    @FXML
    private TableColumn<Order, Double> totalPriceColumn;

    @FXML
    private TableColumn<Order, String> statusColumn;

    @FXML
    private TableColumn<Order, Integer> totalTimeColumn;

    @FXML
    private TableColumn<Order, String> orderDateColumn;

    @FXML
    private TextField collectTimeField;

    @FXML
    private Button collectOrderButton;

    @FXML
    private Button backButton;

    private User user;
    private ObservableList<Order> orders = FXCollections.observableArrayList();

    //sets the current user and loads their orders that can be collected
    public void setUser(User user) {
        this.user = user;
        loadOrders();
    }

    @FXML
    private void initialize() {
        orderNumberColumn.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));
        foodItemsColumn.setCellValueFactory(new PropertyValueFactory<>("foodItems"));
        totalPriceColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        totalTimeColumn.setCellValueFactory(new PropertyValueFactory<>("totalTime"));
        orderDateColumn.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
    }

    // Loads the user's orders from the database
    private void loadOrders() {
        orders.clear();

        String query = "SELECT order_number, GROUP_CONCAT(fi.name || ' x' || oi.quantity, ', ') AS food_items, total_price, status, total_time, order_date " +
                "FROM orders o " +
                "JOIN order_items oi ON o.order_id = oi.order_id " +
                "JOIN food_items fi ON oi.food_item_id = fi.food_item_id " +
                "WHERE o.user_id = ? AND (status = 'Awaiting For Collection') " +
                "GROUP BY order_number, total_price, status, total_time, order_date";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, user.getUserId());

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String orderNumber = rs.getString("order_number");
                String foodItems = rs.getString("food_items");
                double totalPrice = rs.getDouble("total_price");
                String status = rs.getString("status");
                int totalTime = rs.getInt("total_time");
                String orderDate = rs.getString("order_date");

                orders.add(new Order(orderNumber, foodItems, totalPrice, status, totalTime, orderDate));
            }

            ordersTableView.setItems(orders);
        } catch (SQLException e) {
            e.printStackTrace();
            AppUtils.showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load orders.");
        }
    }

    //Handles the collection of the selected order
    @FXML
    private void handleCollectOrder(ActionEvent event) {
        Order selectedOrder = ordersTableView.getSelectionModel().getSelectedItem();
        if (selectedOrder != null) {
            String collectTime = collectTimeField.getText();
            if (isValidCollectTime(selectedOrder, collectTime)) {
                updateOrderStatus(selectedOrder.getOrderNumber(), collectTime);
            } else {
                AppUtils.showAlert(Alert.AlertType.ERROR, "Invalid Time", "Collect time must be after order time plus total cooking time.");
            }
        } else {
            AppUtils.showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an order to collect.");
        }
    }

    //Validates the collect time
    private boolean isValidCollectTime(Order order, String collectTime) {
        try {
            // Parse the order date and time
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
            LocalDateTime orderDateTime = LocalDateTime.parse(order.getOrderDate(), dateTimeFormatter);
            orderDateTime = orderDateTime.plusMinutes(order.getTotalTime());
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime collectLocalTime = LocalTime.parse(collectTime, timeFormatter);

            // Create a LocalDateTime for collect time on the same date as the order date
            LocalDateTime collectDateTime = LocalDateTime.of(orderDateTime.toLocalDate(), collectLocalTime);
            return !collectDateTime.isBefore(orderDateTime);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //Updates the status of the specified order to "Collected"
    private void updateOrderStatus(String orderNumber, String collectTime) {
        String updateQuery = "UPDATE orders SET status = ?, collect_date = ? WHERE order_number = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
            stmt.setString(1, "Collected");
            stmt.setString(2, collectTime);
            stmt.setString(3, orderNumber);

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                AppUtils.showAlert(Alert.AlertType.INFORMATION, "Order Collected", "Order has been marked as collected.");
            } else {
                AppUtils.showAlert(Alert.AlertType.ERROR, "Update Failed", "Failed to update order status.");
            }
            loadOrders(); // Reload orders after update
        } catch (SQLException e) {
            e.printStackTrace();
            AppUtils.showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update order status.");
        }
    }

    //Navigates back to the dashboard
    @FXML
    private void handleBackToDashboard(ActionEvent event) {
        AppUtils.navigateToDashboard((Stage) backButton.getScene().getWindow(), user);
    }

    //Represents an order
    public static class Order {
        private final String orderNumber;
        private final String foodItems;
        private final double totalPrice;
        private final String status;
        private final int totalTime;
        private final String orderDate;

        public Order(String orderNumber, String foodItems, double totalPrice, String status, int totalTime, String orderDate) {
            this.orderNumber = orderNumber;
            this.foodItems = foodItems;
            this.totalPrice = totalPrice;
            this.status = status;
            this.totalTime = totalTime;
            this.orderDate = orderDate;
        }

        public String getOrderNumber() {
            return orderNumber;
        }

        public String getFoodItems() {
            return foodItems;
        }

        public double getTotalPrice() {
            return totalPrice;
        }

        public String getStatus() {
            return status;
        }

        public int getTotalTime() {
            return totalTime;
        }

        public String getOrderDate() {
            return orderDate;
        }
    }
}
