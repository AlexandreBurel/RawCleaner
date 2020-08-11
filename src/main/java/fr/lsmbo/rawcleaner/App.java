package fr.lsmbo.rawcleaner;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App extends Application {

    protected static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // let the whole app know that the gui is visible
        AppInfo.defineGuiAsVisible();
        // window title
        primaryStage.setTitle(AppInfo.getAppName() + " " + AppInfo.getAppVersion());

        try {
            Global.loadSettings();
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(App.class.getResource("Window.fxml"));
            StackPane page = loader.load();
            Scene scene = new Scene(page);
            primaryStage.setScene(scene);
            Window controller = loader.getController();
            controller.setStage(primaryStage);

            // display frame
            primaryStage.show();

        } catch(Throwable t) {
            logger.error(t.getMessage(), t);
            t.printStackTrace();
        }
    }
}
