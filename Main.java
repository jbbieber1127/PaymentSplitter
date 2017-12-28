import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    public static void main(String[] args){
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("PaymentSplitterScene.fxml"));

        Scene scene = new Scene(root, 700, 500);

        stage.setResizable(false);
        stage.setTitle("Payment Splitter");
        stage.setScene(scene);
        stage.show();
    }

}
