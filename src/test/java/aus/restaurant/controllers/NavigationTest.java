package aus.restaurant.controllers;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;
import static org.testfx.api.FxAssert.verifyThat;



public class NavigationTest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
    @Test
    public void testNavigationAndLogout() {
        // Logging in
        clickOn("#usernameField").write("testsg");
        clickOn("#passwordField").write("testpswrd");
        clickOn("#loginButton");
        clickOn("#editProfileButton");
        clickOn("#backButton");
        clickOn("#createOrderButton");
        clickOn("#backButton");
        clickOn("#viewBasketButton");
        clickOn("#backButton");
        clickOn("#viewOrdersButton");
        clickOn("#backButton");
        clickOn("#collectOrderButton");
        clickOn("#backButton");
        clickOn("#cancelOrderButton");
        clickOn("#backButton");
        clickOn("#exportOrderButton");
        clickOn("#backButton");
        clickOn("#upgradeVipButton");
        clickOn("#backButton");
        clickOn("#logoutButton");
        verifyThat("#loginButton", NodeMatchers.isVisible());
    }
}
