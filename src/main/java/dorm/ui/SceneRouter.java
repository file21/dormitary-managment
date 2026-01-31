package dorm.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

/**
 * Central scene navigation helper.
 */
public final class SceneRouter {
    private final Stage stage;

    public SceneRouter(Stage stage) {
        this.stage = stage;
    }

    public void goTo(String fxmlName, String title) {
        try {
            URL url = getClass().getResource("/dorm/ui/" + fxmlName);
            if (url == null) throw new IllegalStateException("FXML not found: " + fxmlName);
            Parent root = FXMLLoader.load(url);
            stage.setScene(new Scene(root));
            stage.setTitle(title);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load scene: " + fxmlName, e);
        }
    }
}
