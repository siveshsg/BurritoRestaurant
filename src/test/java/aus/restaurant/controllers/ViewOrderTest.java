package aus.restaurant.controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

public class ViewOrderTest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    public void testEditProfileAndSaveChanges() {
        // Login
        clickOn("#usernameField").write("testsg2");
        clickOn("#passwordField").write("testsg2");
        clickOn("#loginButton");
        waitForFxEvents();
        sleep(1000);
        clickOn("#viewOrdersButton");
        sleep(3000);
        clickOn("#backButton");
        clickOn("#logoutButton");
        clickOnDialogButton("OK");
        clickOn("#backButton");
        sleep(1000);
        clickOn("#logoutButton");
    }

    private void clickOnDialogButton(String buttonText) {
        waitForFxEvents();
        sleep(500);
        lookup(".button").queryAll().stream()
                .filter(node -> node.isVisible() && node instanceof Button && ((Button) node).getText().equals(buttonText))
                .findFirst()
                .ifPresent(this::clickOn);
    }
}
