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

//Controller to handle all the cancelation functionality are declared down here

public class CancelOrderController {

    @FXML
    private TableView<Order> ordersTableView;

    @FXML
    private TableColumn<Order, String> orderNumberColumn;

    @FXML
    private TableColumn<Order, Double> totalPriceColumn;

    @FXML
    private TableColumn<Order, String> orderDateColumn;

    @FXML
    private TableColumn<Order, String> statusColumn;

    @FXML
    private Button cancelOrderButton;

    @FXML
    private Button backButton;

    private User user;
    private ObservableList<Order> orders = FXCollections.observableArrayList();


    //Sets the current user and loads their orders that can be cancelled.
    public void setUser(User user) {
        this.user = user;
        loadOrders();
    }

    @FXML
    private void initialize() {
        orderNumberColumn.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));
        totalPriceColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        orderDateColumn.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    //function used to loads the user's orders from the database.
    private void loadOrders() {
        orders.clear();

        String query = "SELECT order_number, total_price, order_date, status " +
                "FROM orders " +
                "WHERE user_id = ? AND (status = 'Awaiting For Collection' OR status = 'Pending')";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, user.getUserId());

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String orderNumber = rs.getString("order_number");
                double totalPrice = rs.getDouble("total_price");
                String orderDate = rs.getString("order_date");
                String status = rs.getString("status");

                orders.add(new Order(orderNumber, totalPrice, orderDate, status));
            }

            ordersTableView.setItems(orders);
        } catch (SQLException e) {
            e.printStackTrace();
            AppUtils.showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load orders.");
        }
    }

    // Handles the cancellation of the selected order
    @FXML
    private void handleCancelOrder(ActionEvent event) {
        Order selectedOrder = ordersTableView.getSelectionModel().getSelectedItem();
        if (selectedOrder != null) {
            deleteOrder(selectedOrder.getOrderNumber());
        } else {
            AppUtils.showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an order to cancel.");
        }
    }

    ///Deletes the specified order from the database as per the functionality
    private void deleteOrder(String orderNumber) {
        String deleteQuery = "DELETE FROM orders WHERE order_number = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
            stmt.setString(1, orderNumber);

            int rowsDeleted = stmt.executeUpdate();

            if (rowsDeleted > 0) {
                System.out.println("Order " + orderNumber + " deleted successfully.");
                AppUtils.showAlert(Alert.AlertType.INFORMATION, "Order Canceled", "Order has been canceled.");
            } else {
                System.out.println("No rows were deleted.");
            }

            loadOrders();
        } catch (SQLException e) {
            e.printStackTrace();
            AppUtils.showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to delete order.");
        }
    }

    // Navigates back to the dashboard
    @FXML
    private void handleBackToDashboard(ActionEvent event) {
        AppUtils.navigateToDashboard((Stage) backButton.getScene().getWindow(), user);
    }


    //Represents an order.
    public static class Order {
        private final String orderNumber;
        private final double totalPrice;
        private final String orderDate;
        private final String status;

        public Order(String orderNumber, double totalPrice, String orderDate, String status) {
            this.orderNumber = orderNumber;
            this.totalPrice = totalPrice;
            this.orderDate = orderDate;
            this.status = status;
        }

        public String getOrderNumber() {
            return orderNumber;
        }

        public double getTotalPrice() {
            return totalPrice;
        }

        public String getOrderDate() {
            return orderDate;
        }

        public String getStatus() {
            return status;
        }
    }
}
