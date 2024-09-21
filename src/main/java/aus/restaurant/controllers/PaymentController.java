package aus.restaurant.controllers;
import aus.restaurant.db.DatabaseManager;
import aus.restaurant.models.AppUtils;
import aus.restaurant.models.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

//Controller to handle all the payment page functionality
public class PaymentController {

    @FXML
    private Label orderNumberLabel;

    @FXML
    private TextField cardNumberField;

    @FXML
    private DatePicker expiryDatePicker;

    @FXML
    private PasswordField cvvField;

    @FXML
    private DatePicker orderDatePicker;

    @FXML
    private TextField orderTimeField;

    @FXML
    private Label totalPriceLabel;

    @FXML
    private Label availableCreditsLabel;

    @FXML
    private Label availableCreditsValueLabel;

    @FXML
    private CheckBox useCreditsCheckBox;

    @FXML
    private Label creditsToUseLabel;

    @FXML
    private TextField creditsToUseField;

    @FXML
    private Button submitPaymentButton;

    @FXML
    private Button backButton;

    private User user;
    private String orderNumber;
    private double totalPrice;
    private int availableCredits;


    // Sets the current user and updates the UI based on the user's VIP status
    public void setUser(User user) {
        this.user = user;
        if (user.isVip()) {
            availableCreditsLabel.setVisible(true);
            availableCreditsValueLabel.setVisible(true);
            useCreditsCheckBox.setVisible(true);
            creditsToUseLabel.setVisible(true);
            creditsToUseField.setVisible(true);
            loadUserCredits();
        }
    }

    //Sets the order number and updates the UI.
    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
        orderNumberLabel.setText(orderNumber.substring(0, 4));
    }

    //Sets the total price and updates the UI
    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
        totalPriceLabel.setText(String.format("$%.2f", totalPrice));
    }

    //Loads the user's available credits from the database
    private void loadUserCredits() {
        String query = "SELECT SUM(credits_earned - credits_used) AS available_credits FROM credits WHERE user_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, user.getUserId());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    availableCredits = rs.getInt("available_credits");
                    availableCreditsValueLabel.setText(String.valueOf(availableCredits));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            AppUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to load user credits.");
        }
    }

    //Handles the action when the "Use Credits" checkbox is selected or deselected
    @FXML
    private void handleUseCreditsCheckBox(ActionEvent event) {
        if (useCreditsCheckBox.isSelected() && !user.isVip()) {
            AppUtils.showAlert(Alert.AlertType.WARNING, "VIP Only", "The option to use credits is only available for VIP users.");
            useCreditsCheckBox.setSelected(false);
        } else {
            recalculateFinalPrice();
        }
    }

    //Handles the action when the "Submit Payment" button is clicked
    @FXML
    private void handleSubmitPayment(ActionEvent event) {
        String cardNumber = cardNumberField.getText();
        LocalDate expiryDate = expiryDatePicker.getValue();
        String cvv = cvvField.getText();
        LocalDate orderDate = orderDatePicker.getValue();
        String orderTime = orderTimeField.getText();
        boolean useCredits = useCreditsCheckBox.isSelected();
        int creditsToUse = 0;

        if (useCredits && user.isVip()) {
            try {
                creditsToUse = Integer.parseInt(creditsToUseField.getText());
                if (creditsToUse > availableCredits) {
                    AppUtils.showAlert(Alert.AlertType.ERROR, "Invalid Credits", "You do not have enough credits.");
                    return;
                }
                if (creditsToUse % 100 != 0) {
                    AppUtils.showAlert(Alert.AlertType.ERROR, "Invalid Credits", "Credits to use must be a multiple of 100.");
                    return;
                }
                if (creditsToUse > totalPrice * 100) {
                    AppUtils.showAlert(Alert.AlertType.ERROR, "Invalid Credits", "Credits to use cannot exceed the total price.");
                    return;
                }
            } catch (NumberFormatException e) {
                AppUtils.showAlert(Alert.AlertType.ERROR, "Invalid Credits", "Please enter a valid number of credits.");
                return;
            }
        }

        if (validatePaymentDetails(cardNumber, expiryDate, cvv, orderDate, orderTime)) {
            double finalPrice = totalPrice;
            if (useCredits && creditsToUse >= 100) {
                finalPrice -= creditsToUse / 100.0;
                updateUserCredits(creditsToUse);
            }

            String updateOrderStatusQuery = "UPDATE orders SET status = 'Awaiting For Collection', order_date = ?, total_price = ? WHERE order_number = ?";

            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(updateOrderStatusQuery)) {
                LocalDateTime orderDateTime = LocalDateTime.of(orderDate, LocalTime.parse(orderTime));
                stmt.setString(1, orderDateTime.toString());
                stmt.setDouble(2, finalPrice);
                stmt.setString(3, orderNumber);
                stmt.executeUpdate();

                if (user.isVip()) {
                    saveUserCredits(finalPrice);
                }

                savePaymentDetails(orderNumber, cardNumber, expiryDate, cvv);

                AppUtils.showAlert(Alert.AlertType.INFORMATION, "Payment Successful", "Payment for order " + orderNumber.substring(0, 4) + " has been successfully processed.");
                handleBackToDashboard();
            } catch (SQLException e) {
                e.printStackTrace();
                AppUtils.showAlert(Alert.AlertType.ERROR, "Payment Error", "Failed to process payment. Please try again.");
            }
        }
    }

    //Updates the user's credits in the database.
    private void updateUserCredits(int creditsToUse) {
        String updateCreditsQuery = "UPDATE credits SET credits_used = credits_used + ? WHERE user_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(updateCreditsQuery)) {
            stmt.setInt(1, creditsToUse);
            stmt.setInt(2, user.getUserId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            AppUtils.showAlert(Alert.AlertType.ERROR, "Credit Update Error", "Failed to update credits. Please try again.");
        }
    }

    //Saves the user's credits in the database.
    private void saveUserCredits(double finalPrice) {
        int earnedCredits = (int) finalPrice;
        String saveCreditsQuery = "INSERT INTO credits (user_id, order_id, credits_earned, credits_used) VALUES (?, (SELECT order_id FROM orders WHERE order_number = ?), ?, 0)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(saveCreditsQuery)) {
            stmt.setInt(1, user.getUserId());
            stmt.setString(2, orderNumber);
            stmt.setInt(3, earnedCredits);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            AppUtils.showAlert(Alert.AlertType.ERROR, "Credit Save Error", "Failed to save credits. Please try again.");
        }
    }

    //Saves the payment details in the database.
    private void savePaymentDetails(String orderNumber, String cardNumber, LocalDate expiryDate, String cvv) {
        String savePaymentQuery = "INSERT INTO payment_details (order_id, card_number, expiry_date, cvv) VALUES ((SELECT order_id FROM orders WHERE order_number = ?), ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(savePaymentQuery)) {
            stmt.setString(1, orderNumber);
            stmt.setString(2, cardNumber);
            stmt.setString(3, expiryDate.toString());
            stmt.setString(4, cvv);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            AppUtils.showAlert(Alert.AlertType.ERROR, "Payment Save Error", "Failed to save payment details. Please try again.");
        }
    }

    //Validates the payment details entered by the user for cardNumber,the expiry date of the card
    //the order date,the order time,returns true if the payment details are valid, otherwise false
    private boolean validatePaymentDetails(String cardNumber, LocalDate expiryDate, String cvv, LocalDate orderDate, String orderTime) {
        if (!cardNumber.matches("\\d{16}")) {
            AppUtils.showAlert(Alert.AlertType.ERROR, "Invalid Card Number", "Card number must be 16 digits.");
            return false;
        }

        if (expiryDate == null || expiryDate.isBefore(LocalDate.now())) {
            AppUtils.showAlert(Alert.AlertType.ERROR, "Invalid Expiry Date", "Expiry date must be a future date.");
            return false;
        }

        if (!cvv.matches("\\d{3}")) {
            AppUtils.showAlert(Alert.AlertType.ERROR, "Invalid CVV", "CVV must be 3 digits.");
            return false;
        }

        try {
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime.parse(orderTime, timeFormatter);
        } catch (DateTimeParseException e) {
            AppUtils.showAlert(Alert.AlertType.ERROR, "Invalid Order Time", "Order time must be in HH:mm format.");
            return false;
        }

        return true;
    }

    //Handles the action when the "Back to Basket" button is clicked.
    @FXML
    private void handleBackToBasket(ActionEvent event) {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/basket.fxml"));
            Stage basketStage = new Stage();
            basketStage.setScene(new Scene(loader.load()));
            basketStage.setTitle("Basket");

            ViewBasketController controller = loader.getController();
            controller.setUser(user);

            basketStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Handles the action to navigate back to the dashboard.
    private void handleBackToDashboard() {
        Stage stage = (Stage) submitPaymentButton.getScene().getWindow();
        stage.close();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dashboard.fxml"));
            Stage dashboardStage = new Stage();
            dashboardStage.setScene(new Scene(loader.load()));
            dashboardStage.setTitle("Dashboard");

            DashboardController controller = loader.getController();
            controller.setUser(user);

            dashboardStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //Recalculates the final price based on the credits entered by the user and updates the UI.
    private void recalculateFinalPrice() {
        if (useCreditsCheckBox.isSelected() && user.isVip()) {
            try {
                int creditsToUse = Integer.parseInt(creditsToUseField.getText());
                if (creditsToUse <= availableCredits && creditsToUse % 100 == 0 && creditsToUse <= totalPrice * 100) {
                    double finalPrice = totalPrice - creditsToUse / 100.0;
                    totalPriceLabel.setText(String.format("$%.2f", finalPrice));
                } else {
                    totalPriceLabel.setText(String.format("$%.2f", totalPrice)); // Reset to original if invalid credits
                }
            } catch (NumberFormatException e) {
                totalPriceLabel.setText(String.format("$%.2f", totalPrice)); // Reset to original if invalid credits
            }
        } else {
            totalPriceLabel.setText(String.format("$%.2f", totalPrice)); // Reset to original if checkbox is not selected
        }
    }
}
