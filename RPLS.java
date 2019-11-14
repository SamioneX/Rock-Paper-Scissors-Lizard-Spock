import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.Reflection;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class RPLS extends Application{
	static String stylesheet= RPLS.class.getResource("style.css").toExternalForm();

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		Button clientBtn = new Button("New Client Window");
		clientBtn.setOnAction(e -> new ClientGUI());

		Button serverBtn = new Button("New Server Window");
		serverBtn.setOnAction(e -> new ServerGUI());

		VBox root = new VBox(serverBtn, clientBtn);
		root.setSpacing(50);

		root.setAlignment(Pos.CENTER);
		root.setId("primaryRoot");

		Scene startScene = new Scene(root, 500, 500);
		startScene.getStylesheets().add(stylesheet);

		primaryStage.setTitle("RPSLS");
		primaryStage.setScene(startScene);
		primaryStage.show();
	}
}
