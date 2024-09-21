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
import java.sql.ResultSet;
import java.sql.SQLException;

//Controller to handle all the logincontroller functionality are declared down here
public class LoginController {
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;

    @FXML
    private Button cancelButton;

    //Handles user login.
    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        if (userExists(username)) {
            User user = loginUser(username, password);
            if (user != null) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/dashboard.fxml"));
                    Stage stage = (Stage) loginButton.getScene().getWindow();
                    stage.setScene(new Scene(loader.load()));
                    stage.setTitle("Dashboard");

                    DashboardController controller = loader.getController();
                    controller.setUser(user);

                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                AppUtils.showAlert(Alert.AlertType.ERROR, "Login Error", "Invalid username or password.");
            }
        } else {
            AppUtils.showAlert(Alert.AlertType.INFORMATION, "User Not Found", "Username not found. Please register.");
        }
    }

    //Handles navigation to the registration page
    @FXML
    private void handleRegister(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/register.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Register");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Handles canceling the login operation
    @FXML
    private void handleCancel(ActionEvent event) {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    //Checks if a user exists in the database
    private boolean userExists(String username) {
        String query = "SELECT COUNT(*) AS count FROM users WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt("count");
                    return count > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    //Authenticates the user and retrieves user details.
    //return the authenticated user, or null if authentication fails
    private User loginUser(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt("user_id");
                    String firstName = rs.getString("first_name");
                    String lastName = rs.getString("last_name");
                    boolean isVip = rs.getBoolean("is_vip");
                    String email = rs.getString("email");
                    int credits = rs.getInt("credits");
                    return new User(userId, username, password, firstName, lastName, isVip, email, credits);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
