package tn.finora.finorainves;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        // ✅ IMPORTANT : donner le stage au SceneNavigator
        SceneNavigator.setStage(stage);

        FXMLLoader loader = new FXMLLoader(
                HelloApplication.class.getResource("/tn/finora/finorainves/ui/investment_cards.fxml")
        );

        Scene scene = new Scene(loader.load(), 900, 500);
        stage.setTitle("Investment - List");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
