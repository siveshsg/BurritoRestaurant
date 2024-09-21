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
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


//Controller to handle all the edit profile functionality are declared down here
public class EditProfileController {
    @FXML
    private TextField usernameField;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button saveButton;

    @FXML
    private Button backButton;

    private User user;

    //Sets the current user and populates the profile fields with their details.
    public void setUser(User user) {
        this.user = user;
        usernameField.setText(user.getUsername());
        firstNameField.setText(user.getFirstName());
        lastNameField.setText(user.getLastName());
        passwordField.setText(user.getPassword());
    }

    // Handles saving changes to the user's profile
    @FXML
    private void handleSaveChanges(ActionEvent event) {
        String newFirstName = firstNameField.getText();
        String newLastName = lastNameField.getText();
        String newPassword = passwordField.getText();

        if (updateUserProfile(user.getUserId(), newFirstName, newLastName, newPassword)) {
            AppUtils.showAlert(Alert.AlertType.INFORMATION, "Profile Updated", "Your profile has been updated successfully.");
        } else {
            AppUtils.showAlert(Alert.AlertType.ERROR, "Update Failed", "Failed to update profile. Please try again.");
        }
    }

    // Navigates back to the dashboard.
    @FXML
    private void handleBackToDashboard(ActionEvent event) {
        AppUtils.navigateToDashboard((Stage) backButton.getScene().getWindow(), user);
    }

   //Updates the user's profile in the database.
   //return true if the update was successful, false otherwise
    private boolean updateUserProfile(int userId, String newFirstName, String newLastName, String newPassword) {
        String query = "UPDATE users SET first_name = ?, last_name = ?, password = ? WHERE user_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, newFirstName);
            stmt.setString(2, newLastName);
            stmt.setString(3, newPassword);
            stmt.setInt(4, userId);
            stmt.executeUpdate();
            user.setFirstName(newFirstName);
            user.setLastName(newLastName);
            user.setPassword(newPassword);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
