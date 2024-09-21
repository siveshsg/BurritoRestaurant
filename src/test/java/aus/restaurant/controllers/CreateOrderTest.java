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

public class CreateOrderTest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    public void testCreateOrderAndProceedToPayment() {
        // Login
        clickOn("#usernameField").write("testsg2");
        clickOn("#passwordField").write("testsg2");
        clickOn("#loginButton");
        waitForFxEvents();
        sleep(3000);
        clickOn("#createOrderButton");
        waitForFxEvents();
        sleep(1000);
        clickOn("#burritosField").write("2");
        clickOn("#friesField").write("7");
        sleep(1000);
        clickOn("#addToBasketButton");
        clickOnDialogButton("OK");
        sleep(1000);
        clickOn("#proceedToPaymentButton");
        clickOnDialogButton("OK");
        sleep(1000);
        clickOn("#cardNumberField").write("1234123412341234");
        clickOn("#expiryDatePicker").write("12/25/2025").type(KeyCode.ENTER);
        clickOn("#cvvField").write("123");
        clickOn("#orderDatePicker").write("06/03/2024").type(KeyCode.ENTER);
        clickOn("#orderTimeField").write("12:00");
        clickOn("#submitPaymentButton");
        clickOnDialogButton("OK");
        sleep(3000);
        clickOn("#collectOrderButton");
        clickOnDialogButton("OK");
        clickOn("#collectTimeField").write("23:59");
        clickOn("#collectOrderButton");
        clickOnDialogButton("OK");
        clickOn("#backButton");
        clickOnDialogButton("OK");
        waitForFxEvents();
        clickOn("#viewOrdersButton");
        sleep(3000);
        clickOn("#backButton");
        sleep(1000);
        clickOn("#logoutButton");
        clickOnDialogButton("OK");
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
