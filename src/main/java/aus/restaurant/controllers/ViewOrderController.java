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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


//Controller to handle all the vieworder page functionality are declared down here
public class ViewOrderController {
    @FXML
    private TableView<Order> ordersTableView;

    @FXML
    private TableColumn<Order, String> orderDateColumn;

    @FXML
    private TableColumn<Order, Double> totalPriceColumn;

    @FXML
    private TableColumn<Order, String> statusColumn;

    @FXML
    private Button backButton;

    private User user;
    private ObservableList<Order> orders = FXCollections.observableArrayList();

    public void setUser(User user) {
        this.user = user;
        loadOrders();
    }

    @FXML
    private void initialize() {
        orderDateColumn.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        totalPriceColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void loadOrders() {
        orders.clear();

        String query = "SELECT order_date, total_price, status " +
                "FROM orders " +
                "WHERE user_id = ? AND status IN ('Awaiting For Collection', 'Collected', 'Cancelled','Pending') " +
                "ORDER BY order_date DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, user.getUserId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String orderDate = rs.getString("order_date");
                double totalPrice = rs.getDouble("total_price");
                String status = rs.getString("status");
                orders.add(new Order(orderDate, totalPrice, status));
            }
            ordersTableView.setItems(orders);
        } catch (SQLException e) {
            e.printStackTrace();
            AppUtils.showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load orders.");
        }
    }

    @FXML
    private void handleBackToDashboard(ActionEvent event) {
        AppUtils.navigateToDashboard((Stage) backButton.getScene().getWindow(), user);
    }

    public static class Order {
        private final String orderDate;
        private final double totalPrice;
        private final String status;

        public Order(String orderDate, double totalPrice, String status) {
            this.orderDate = orderDate;
            this.totalPrice = totalPrice;
            this.status = status;
        }

        public String getOrderDate() {
            return orderDate;
        }

        public double getTotalPrice() {
            return totalPrice;
        }

        public String getStatus() {
            return status;
        }
    }
}
