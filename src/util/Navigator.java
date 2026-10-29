package util;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Navigator {

    public static void switchTo(Node anyNode, String fxmlFile, String title) throws IOException {
        Stage stage = (Stage) anyNode.getScene().getWindow();
        Parent root = FXMLLoader.load(Navigator.class.getResource("/view/" + fxmlFile));
        stage.setScene(new Scene(root));
        stage.setTitle(title);
        stage.sizeToScene();
        stage.centerOnScreen();
    }
}