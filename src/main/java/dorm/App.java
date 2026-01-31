package dorm;

import dorm.service.DormRepository;
import dorm.service.DormService;
import dorm.ui.LoginView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) {
        DormService service = new DormService(new DormRepository());
        LoginView loginView = new LoginView(service, stage);

        Scene scene = new Scene(loginView.getRoot(), 900, 600);
        stage.setTitle("Dormitory Management System");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
