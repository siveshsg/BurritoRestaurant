package aus.restaurant;
import aus.restaurant.db.DbInitializer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

//The Start of Main Application
//start(Stage primaryStage) : Initializes the database and sets up the primary stage with the login scene.
//main(String[] args) : Launches the JavaFX application.


public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        DbInitializer.initialize();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
        Scene scene = new Scene(loader.load());
        primaryStage.setTitle("Burrito King Restaurant");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
