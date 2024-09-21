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
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

//Controller to handle all the basket functionality are declared down here
public class ViewBasketController {
    @FXML
    private TableView<OrderItem> basketTableView;

    @FXML
    private TableColumn<OrderItem, String> orderNumberColumn;

    @FXML
    private TableColumn<OrderItem, String> itemNameColumn;

    @FXML
    private TableColumn<OrderItem, Integer> quantityColumn;

    @FXML
    private TableColumn<OrderItem, Double> priceColumn;

    @FXML
    private Label totalPriceLabel;

    @FXML
    private Label totalTimeLabel;

    @FXML
    private Button backButton;

    @FXML
    private Button proceedToPaymentButton;

    private User user;
    private ObservableList<OrderItem> basketItems = FXCollections.observableArrayList();
    private String orderNumber; // To store the current order number

    public void setUser(User user) {
        this.user = user;
        loadBasketItems();
    }

    @FXML
    private void initialize() {
        orderNumberColumn.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));
        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
    }

    private void loadBasketItems() {
        basketItems.clear();

        String query = "SELECT o.order_number, fi.name, oi.quantity, fi.price, o.total_price, o.total_time " +
                "FROM order_items oi " +
                "JOIN food_items fi ON oi.food_item_id = fi.food_item_id " +
                "JOIN orders o ON oi.order_id = o.order_id " +
                "WHERE o.user_id = ? AND o.status = 'Pending'";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, user.getUserId());
            ResultSet rs = stmt.executeQuery();
            double totalPrice = 0;
            int totalTime = 0;

            while (rs.next()) {
                orderNumber = rs.getString("order_number");
                String itemName = rs.getString("name");
                int quantity = rs.getInt("quantity");
                double price = rs.getDouble("price");
                totalPrice = rs.getDouble("total_price");
                totalTime = rs.getInt("total_time");
                basketItems.add(new OrderItem(orderNumber, itemName, quantity, price * quantity));
            }

            basketTableView.setItems(basketItems);
            totalPriceLabel.setText(String.format("$%.2f", totalPrice));
            totalTimeLabel.setText(totalTime + " min");
        } catch (SQLException e) {
            e.printStackTrace();
            AppUtils.showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load basket items.");
        }
    }

    @FXML
    private void handleProceedToPayment(ActionEvent event) {
        if (basketItems.isEmpty()) {
            AppUtils.showAlert(Alert.AlertType.WARNING, "No Items in Basket", "Your basket is empty. Please add items before proceeding to payment.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/payment.fxml"));
            Stage stage = (Stage) proceedToPaymentButton.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Payment");
            PaymentController controller = loader.getController();
            controller.setUser(user);
            controller.setOrderNumber(orderNumber);
            controller.setTotalPrice(Double.parseDouble(totalPriceLabel.getText().replace("$", ""))); // Pass total price to payment page
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBackToDashboard(ActionEvent event) {
        AppUtils.navigateToDashboard((Stage) backButton.getScene().getWindow(), user);
    }

    public static class OrderItem {
        private final String orderNumber;
        private final String itemName;
        private final int quantity;
        private final double price;

        public OrderItem(String orderNumber, String itemName, int quantity, double price) {
            this.orderNumber = orderNumber;
            this.itemName = itemName;
            this.quantity = quantity;
            this.price = price;
        }

        public String getOrderNumber() {
            return orderNumber;
        }

        public String getItemName() {
            return itemName;
        }

        public int getQuantity() {
            return quantity;
        }

        public double getPrice() {
            return price;
        }
    }
}
