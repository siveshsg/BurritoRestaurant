package aus.restaurant.models;
import aus.restaurant.controllers.DashboardController;
import aus.restaurant.models.User;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;

public class AppUtils {

    public static void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void navigateToDashboard(Stage stage, User user) {
        stage.close();
        try {
            FXMLLoader loader = new FXMLLoader(AppUtils.class.getResource("/dashboard.fxml"));
            Stage dashboardStage = new Stage();
            dashboardStage.setScene(new Scene(loader.load()));
            dashboardStage.setTitle("Dashboard");
            DashboardController controller = loader.getController();
            controller.setUser(user);
            dashboardStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to navigate to the dashboard.");
        }
    }

    // Constants
    public static final String DATABASE_URL = "jdbc:sqlite:burritoking.db";
    public static final String ORDER_STATUS_PENDING = "Pending";
    public static final String ORDER_STATUS_AWAITING = "Awaiting For Collection";
    public static final String ORDER_STATUS_COLLECTED = "Collected";
    public static final String ORDER_STATUS_CANCELLED = "Cancelled";
}
