package aus.restaurant.controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;


public class RegisterControllerTest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/register.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    public void testRegister() {
        clickOn("#usernameField").write("testsg");
        clickOn("#passwordField").write("testpswrd");
        clickOn("#firstNameField").write("testfirstname");
        clickOn("#lastNameField").write("hehe");
        clickOn("#registerButton");

    }
}
