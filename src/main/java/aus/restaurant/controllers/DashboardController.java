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


//Controller to handle all the dashboard functionality are declared down here
public class DashboardController {
    @FXML
    private Label welcomeLabel;

    @FXML
    private Label creditsLabel;

    @FXML
    private Button editProfileButton;

    @FXML
    private Button createOrderButton;

    @FXML
    private Button viewBasketButton;

    @FXML
    private Button viewOrdersButton;

    @FXML
    private Button collectOrderButton;

    @FXML
    private Button cancelOrderButton;

    @FXML
    private Button exportOrderButton;

    @FXML
    private Button logoutButton;

    @FXML
    private Button upgradeVipButton;

    @FXML
    private TableView<ActiveOrder> activeOrdersTableView;

    @FXML
    private TableColumn<ActiveOrder, String> orderNumberColumn;

    @FXML
    private TableColumn<ActiveOrder, String> foodItemsColumn;

    @FXML
    private TableColumn<ActiveOrder, Double> totalPriceColumn;

    @FXML
    private TableColumn<ActiveOrder, String> statusColumn;

    private User user;
    private ObservableList<ActiveOrder> activeOrders = FXCollections.observableArrayList();

    //Sets the current user and loads their active orders.
    public void setUser(User user) {
        this.user = user;
        welcomeLabel.setText("Welcome, " + user.getFirstName() + " " + user.getLastName() + "!");
        loadActiveOrders();

        // Display available credits if the user is a VIP
        if (user.isVip()) {
            int credits = getUserCredits(user.getUserId());
            creditsLabel.setText("Available Credits: " + credits);
        } else {
            creditsLabel.setText("");
        }
    }

    @FXML
    private void initialize() {
        orderNumberColumn.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));
        foodItemsColumn.setCellValueFactory(new PropertyValueFactory<>("foodItems"));
        totalPriceColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    //Loads the user's active orders from the database.

    private void loadActiveOrders() {
        activeOrders.clear();

        String query = "SELECT o.order_number, GROUP_CONCAT(fi.name || ' x' || oi.quantity, ', ') AS food_items, o.total_price, o.status " +
                "FROM orders o " +
                "JOIN order_items oi ON o.order_id = oi.order_id " +
                "JOIN food_items fi ON oi.food_item_id = fi.food_item_id " +
                "WHERE o.user_id = ? AND (o.status = 'Awaiting For Collection' OR o.status = 'Pending') " +
                "GROUP BY o.order_number, o.total_price, o.status";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, user.getUserId());

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String orderNumber = rs.getString("order_number");
                if (orderNumber.length() > 8) {
                    orderNumber = orderNumber.substring(0, 8);
                }
                String foodItems = rs.getString("food_items");
                double totalPrice = rs.getDouble("total_price");
                String status = rs.getString("status");

                activeOrders.add(new ActiveOrder(orderNumber, foodItems, totalPrice, status));
            }

            activeOrdersTableView.setItems(activeOrders);
        } catch (SQLException e) {
            e.printStackTrace();
            AppUtils.showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load active orders.");
        }
    }

    // Retrieves the user's available credits from the database.
    // returns the available credits as an integer

    private int getUserCredits(int userId) {
        String query = "SELECT SUM(credits_earned - credits_used) AS available_credits FROM credits WHERE user_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("available_credits");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @FXML
    private void handleEditProfile(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/editprofile.fxml"));
            Stage stage = (Stage) editProfileButton.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Edit Profile");

            EditProfileController controller = loader.getController();
            controller.setUser(user);

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCreateOrder(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/createorder.fxml"));
            Stage stage = (Stage) createOrderButton.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Create Order");

            CreateOrderController controller = loader.getController();
            controller.setUser(user);

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleViewBasket(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/basket.fxml"));
            Stage stage = (Stage) viewBasketButton.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Basket");

            ViewBasketController controller = loader.getController();
            controller.setUser(user);

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleViewOrders(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vieworders.fxml"));
            Stage stage = (Stage) viewOrdersButton.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("View Orders");

            ViewOrderController controller = loader.getController();
            controller.setUser(user);

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCollectOrder(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/collectorder.fxml"));
            Stage stage = (Stage) collectOrderButton.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Collect Order");

            CollectOrderController controller = loader.getController();
            controller.setUser(user);

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancelOrder(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/cancelorder.fxml"));
            Stage stage = (Stage) cancelOrderButton.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Cancel Order");

            CancelOrderController controller = loader.getController();
            controller.setUser(user);

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleExportOrder(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/exportorder.fxml"));
            Stage stage = (Stage) exportOrderButton.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Export Orders");

            ExportOrderController controller = loader.getController();
            controller.setUser(user);

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        stage.close();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Stage loginStage = new Stage();
            loginStage.setScene(new Scene(loader.load()));
            loginStage.setTitle("Login");
            loginStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpgradeToVip(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vipupgrade.fxml"));
            Stage vipUpgradeStage = new Stage();
            vipUpgradeStage.setScene(new Scene(loader.load()));
            vipUpgradeStage.setTitle("Upgrade to VIP");

            VipUpgradeController controller = loader.getController();
            controller.setUser(user);

            Stage currentStage = (Stage) upgradeVipButton.getScene().getWindow();
            currentStage.close(); // Close the current dashboard window

            vipUpgradeStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Represents an active orde
    public static class ActiveOrder {
        private final String orderNumber;
        private final String foodItems;
        private final double totalPrice;
        private final String status;

        public ActiveOrder(String orderNumber, String foodItems, double totalPrice, String status) {
            this.orderNumber = orderNumber;
            this.foodItems = foodItems;
            this.totalPrice = totalPrice;
            this.status = status;
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
    }
}
