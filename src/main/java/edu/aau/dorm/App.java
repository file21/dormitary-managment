package edu.aau.dorm;

import edu.aau.dorm.ui.SceneRouter;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * JavaFX entry point.
 */
public class App extends Application {

    @Override
    public void start(Stage stage) {
        SceneRouter router = new SceneRouter(stage);
        router.goTo("login.fxml", "Dorm Management - Login");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
