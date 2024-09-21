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
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

//Controller to handle all the vip upgrade functionality are declared down here
public class VipUpgradeController {

    @FXML
    private TextField emailField;

    @FXML
    private CheckBox promotionsCheckBox;

    @FXML
    private Button upgradeButton;

    @FXML
    private Button backButton;

    private User user;

    //Sets the current user and updates the UI based on their VIP status
    public void setUser(User user) {
        this.user = user;

        // Check if the user is already a VIP
        if (user.isVip()) {
            AppUtils.showAlert(Alert.AlertType.INFORMATION, "Already VIP", "You are already a VIP user!");
            upgradeButton.setDisable(true);
        } else {
            upgradeButton.setDisable(true);
            promotionsCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> upgradeButton.setDisable(!newValue));
        }
    }

    //Handles the VIP upgrade process
    @FXML
    private void handleUpgrade(ActionEvent event) {
        if (user.isVip()) {
            AppUtils.showAlert(Alert.AlertType.INFORMATION, "Already VIP", "You are already a VIP user!");
            return;
        }

        String email = emailField.getText().trim();
        if (email.isEmpty() || !email.contains("@")) {
            AppUtils.showAlert(Alert.AlertType.ERROR, "Invalid Email", "Please enter a valid email address.");
            return;
        }

        if (emailExists(email)) {
            AppUtils.showAlert(Alert.AlertType.ERROR, "Email Exists", "This email is already associated with another user. Please enter a different email.");
            return;
        }

        try (Connection conn = DatabaseManager.getConnection()) {
            String updateQuery = "UPDATE users SET is_vip = true, email = ? WHERE user_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
                stmt.setString(1, email);
                stmt.setInt(2, user.getUserId());
                stmt.executeUpdate();
            }
            AppUtils.showAlert(Alert.AlertType.INFORMATION, "Upgrade Successful", "You are now a VIP user!");
            user.setVip(true);
            handleBackToDashboard(event);
        } catch (SQLException e) {
            e.printStackTrace();
            AppUtils.showAlert(Alert.AlertType.ERROR, "Upgrade Failed", "An error occurred while upgrading to VIP. Please try again.");
        }
    }

    // Checks if the given email already exists in the database
    // return true if the email exists, false otherwise
    private boolean emailExists(String email) {
        String query = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    //Handles navigation back to the dashboard
    @FXML
    private void handleBackToDashboard(ActionEvent event) {
        navigateToDashboard();
    }

    //Navigates to the dashboard and closes the current window
    private void navigateToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dashboard.fxml"));
            Stage dashboardStage = new Stage();
            dashboardStage.setScene(new Scene(loader.load()));
            dashboardStage.setTitle("Dashboard");

            DashboardController controller = loader.getController();
            controller.setUser(user);

            Stage currentStage = (Stage) backButton.getScene().getWindow();
            currentStage.close(); // Close the current window before showing the new one

            dashboardStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
