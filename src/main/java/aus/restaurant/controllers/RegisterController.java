package aus.restaurant.controllers;
import aus.restaurant.db.DatabaseManager;
import aus.restaurant.models.AppUtils;
import aus.restaurant.models.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

//Controller to handle all the register page functionality are declared down here
public class RegisterController {
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private Button registerButton;

    @FXML
    private Button cancelButton;

    //Handles user registration
    @FXML
    private void handleRegister(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();

        if (!userExists(username)) {
            User user = new User(0, username, password, firstName, lastName, false, null, 0);
            if (registerUser(user)) {
                AppUtils.showAlert(Alert.AlertType.INFORMATION, "Registration Successful", "User registered successfully. Please log in.");
                Stage stage = (Stage) registerButton.getScene().getWindow();
                stage.close();
            } else {
                AppUtils.showAlert(Alert.AlertType.ERROR, "Registration Error", "Error occurred during registration. Please try again.");
            }
        } else {
            AppUtils.showAlert(Alert.AlertType.ERROR, "Registration Error", "Username already exists. Please choose a different username.");
        }
    }

    //Handles canceling the registration operation.

    @FXML
    private void handleCancel(ActionEvent event) {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    //Checks if a username is already taken.
    //return true if the username exists, false otherwise
    private boolean userExists(String username) {
        String query = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    //Registers the new user in the database.
    //return true if the registration was successful, false otherwise
    private boolean registerUser(User user) {
        String query = "INSERT INTO users (username, password, first_name, last_name, is_vip, email, credits) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getFirstName());
            stmt.setString(4, user.getLastName());
            stmt.setBoolean(5, user.isVip());
            stmt.setString(6, user.getEmail());
            stmt.setInt(7, user.getCredits());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
