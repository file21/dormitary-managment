package dorm;

import dorm.dao.DaoFactory;
import dorm.service.DatabaseDormService;
import dorm.ui.LoginViewDb;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) {
        DatabaseDormService service = new DatabaseDormService(
            DaoFactory.createUserRepository(),
            DaoFactory.createStudentRepository(),
            DaoFactory.createApplicationRepository(),
            DaoFactory.createAnnouncementRepository(),
            DaoFactory.createMessageRepository()
        );
        
        LoginViewDb loginView = new LoginViewDb(service, stage);

        Scene scene = new Scene(loginView.getRoot(), 900, 600);
        stage.setTitle("Dormitory Management System");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
