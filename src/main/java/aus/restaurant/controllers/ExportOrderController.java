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
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

//Controller to handle all the export order functionality are declared down here
public class ExportOrderController {

    @FXML
    private TableView<Order> exportOrdersTableView;

    @FXML
    private TableColumn<Order, String> orderNumberColumn;

    @FXML
    private TableColumn<Order, String> dateTimeColumn;

    @FXML
    private TableColumn<Order, String> collectTimeColumn;

    @FXML
    private TableColumn<Order, Double> totalPriceColumn;

    @FXML
    private TableColumn<Order, String> statusColumn;

    @FXML
    private CheckBox orderNumberCheckBox;

    @FXML
    private CheckBox priceCheckBox;

    @FXML
    private CheckBox statusCheckBox;

    @FXML
    private CheckBox orderTimeCheckBox;

    @FXML
    private CheckBox collectTimeCheckBox;

    @FXML
    private TextField fileNameField;

    @FXML
    private Button chooseDestinationButton;

    @FXML
    private Button exportButton;

    @FXML
    private Button backButton;

    private User user;
    private ObservableList<Order> orders = FXCollections.observableArrayList();
    private File selectedDirectory;

    //Sets the current user and loads their orders for export
    public void setUser(User user) {
        this.user = user;
        loadOrders();
    }

    @FXML
    private void initialize() {
        orderNumberColumn.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));
        dateTimeColumn.setCellValueFactory(new PropertyValueFactory<>("orderTime"));
        collectTimeColumn.setCellValueFactory(new PropertyValueFactory<>("collectTime"));
        totalPriceColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    //Loads the user's orders from the database
    private void loadOrders() {
        orders.clear();

        String query = "SELECT order_number, total_price, status, order_date, collect_date " +
                "FROM orders " +
                "WHERE user_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, user.getUserId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String orderNumber = rs.getString("order_number");
                double totalPrice = rs.getDouble("total_price");
                String status = rs.getString("status");
                String orderDate = rs.getString("order_date");
                String collectDate = rs.getString("collect_date");
                orders.add(new Order(orderNumber, totalPrice, status, orderDate, collectDate));
            }
            exportOrdersTableView.setItems(orders);
        } catch (SQLException e) {
            e.printStackTrace();
            AppUtils.showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load orders.");
        }
    }

    //Opens a directory chooser for selecting the export destination.
    @FXML
    private void handleChooseDestination(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        selectedDirectory = directoryChooser.showDialog(chooseDestinationButton.getScene().getWindow());
    }

    //Exports the selected orders to a CSV file.
    @FXML
    private void handleExportToCSV(ActionEvent event) {
        if (selectedDirectory == null || fileNameField.getText().isEmpty()) {
            AppUtils.showAlert(Alert.AlertType.ERROR, "Input Error", "Please select a destination and enter a file name.");
            return;
        }
        File file = new File(selectedDirectory, fileNameField.getText() + ".csv");
        try (FileWriter writer = new FileWriter(file)) {
            writer.append("Order Number,Order Time,Collect Time,Total Price,Status\n");
            for (Order order : orders) {
                writer.append(order.getOrderNumber()).append(",")
                        .append(order.getOrderTime()).append(",")
                        .append(order.getCollectTime()).append(",")
                        .append(String.valueOf(order.getTotalPrice())).append(",")
                        .append(order.getStatus()).append("\n");
            }
            AppUtils.showAlert(Alert.AlertType.INFORMATION, "Success", "Orders exported successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            AppUtils.showAlert(Alert.AlertType.ERROR, "File Error", "Failed to write to file.");
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
        private final double totalPrice;
        private final String status;
        private final String orderTime;
        private final String collectTime;

        public Order(String orderNumber, double totalPrice, String status, String orderTime, String collectTime) {
            this.orderNumber = orderNumber;
            this.totalPrice = totalPrice;
            this.status = status;
            this.orderTime = orderTime;
            this.collectTime = collectTime;
        }

        public String getOrderNumber() {
            return orderNumber;
        }

        public double getTotalPrice() {
            return totalPrice;
        }

        public String getStatus() {
            return status;
        }

        public String getOrderTime() {
            return orderTime;
        }

        public String getCollectTime() {
            return collectTime;
        }
    }
}
