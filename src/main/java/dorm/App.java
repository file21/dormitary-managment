package dorm;

import dorm.dao.DaoFactory;
import dorm.service.DatabaseDormService;
import dorm.ui.LoginViewDb;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main application entry point for Dormitory Management System.
 * 
 * This application demonstrates:
 * - OOP principles (Encapsulation, Abstraction, Inheritance, Polymorphism)
 * - SOLID principles (SRP, OCP, LSP, ISP, DIP)
 * - JavaFX for GUI
 * - CSV files for data persistence (stored in 'data' directory)
 */
public class App extends Application {
    @Override
    public void start(Stage stage) {
        // Create database-backed service using factory pattern
        DatabaseDormService service = new DatabaseDormService(
            DaoFactory.createUserRepository(),
            DaoFactory.createStudentRepository(),
            DaoFactory.createApplicationRepository(),
            DaoFactory.createAnnouncementRepository(),
            DaoFactory.createMessageRepository(),
            DaoFactory.createBuildingRepository()
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
