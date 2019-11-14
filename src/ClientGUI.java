import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Optional;

public class ClientGUI {
    private static int clientCount = 0;
    int clientNo = clientCount++;
    int roundNo = 0;
    Button[] selections;
    private Scene homeScene;
    Scene loadingScreen;
    private Scene gameScene;
    private Scene oppDisconnectedScreen;
    boolean oppDisconnected = false;
    Stage stage = new Stage();
    private HBox roundBox;
    ImageView oppThick = GUIElements.imageView("tick.png");
    Text loading = new Text("Loading . . .");
    Client client;

    private void createHomeScene() {
        GridPane grid = GUIElements.createGrid(10,25);

        Text welcomeText = new Text("Welcome Client");
        Label portLabel = new Label("Port Number");
        TextField portField = new TextField();
        Label ipLabel = new Label("IP Address");
        TextField ipField = new TextField();

        grid.add(welcomeText, 0, 0, 2, 1);
        grid.add(portLabel, 0, 2);
        grid.add(portField, 1, 2);

        grid.add(ipLabel, 0, 4);
        grid.add(ipField, 1, 4);

        Button startBtn = new Button("Start");
        startBtn.setAlignment(Pos.CENTER);
        grid.add(GUIElements.centerBox(startBtn), 0, 7, 2, 1);

        startBtn.setOnAction(e->{
            int port = Integer.parseInt(portField.getText());
            portField.clear();
            String ip = ipField.getText();
            ipField.clear();
            this.client = new Client(ip, port, action->{
                Platform.runLater(()->action.accept(this));
            });
            this.client.start();
        });

        homeScene = new Scene(grid, 500, 500);
        homeScene.getStylesheets().add(RPLS.stylesheet);
    }

    private Button quitBtn() {
        Button btn = new Button("Quit");
        btn.setOnAction(e->{
            client.close();
            stage.close();
        });
        btn.setAlignment(Pos.CENTER);
        return btn;
    }

    void updateSceneOnServerClose() {
        oppDisconnected = true;
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Server Unavailable");
        alert.setHeaderText("The server is either closed or not running");
        alert.setContentText("Game will be terminated.");
        alert.showAndWait();
        client.exit = true;
        stage.close();
    }

    void updateOppThick() {
        oppDisconnected = false;
        oppThick.setVisible(true);
        PauseTransition p = new PauseTransition(Duration.seconds(1));
        p.setOnFinished(f->{
            loading.setVisible(true);
            PauseTransition v = new PauseTransition(Duration.seconds(1));
            v.setOnFinished(g->{
                stage.setScene(gameScene);
            });
            v.play();
        });
        p.play();
    }

    private void createLoadingScreen() {

        GridPane grid = GUIElements.createGrid(10, 25);

        oppThick.setVisible(false);
        loading.setVisible(false);

        Text establishConnection = new Text("Establishing Connection . . .");
        grid.add(GUIElements.imageView("tick.png"), 0, 0);
        grid.add(establishConnection, 1, 0);

        Text opponent = new Text("Waiting for opponent to join . . .");
        grid.add(oppThick, 0, 2);
        grid.add(opponent, 1, 2);

        grid.add(loading, 1, 4);

        grid.add(GUIElements.centerBox(quitBtn()), 0, 6, 2, 1);

        loadingScreen = new Scene(grid, 750, 450);
        loadingScreen.getStylesheets().add(RPLS.stylesheet);
    }
    private void createOppDisconnectedScreen() {
        GridPane grid = GUIElements.createGrid(10, 25);
        grid.add(GUIElements.centerBox(new Text("Your Opponent Disconnected.")), 0, 0, 2, 1);
        grid.add(oppThick, 0, 2);
        grid.add(new Text("Waiting for another opponent to join ..."), 1, 2);
        grid.add(GUIElements.centerBox(loading), 1, 4, 2, 1);
        grid.add(GUIElements.centerBox(quitBtn()), 0, 6, 2, 1);
        oppDisconnectedScreen = new Scene(grid, 770, 450);
        oppDisconnectedScreen.getStylesheets().add(RPLS.stylesheet);
    }
    void doOppDisconnected() {
        ((Text) roundBox.getChildren().get(0)).setText("Round 0");
        oppThick.setVisible(false);
        loading.setVisible(false);
        roundNo = 0;
        ((Text)roundBox.getChildren().get(0)).setText("Round " + roundNo);
        if (oppDisconnectedScreen == null)
            createOppDisconnectedScreen();
        stage.setScene(oppDisconnectedScreen);
    }

    void displayResult(GameInfo info) {
        if (info.iWon && info.myScore >= 30) {
            GridPane grid = GUIElements.createGrid(20, 25);

            grid.add(GUIElements.centerBox(GUIElements.headerText("Results for Round " + roundNo)), 0, 0);

            VBox uChose = GUIElements.centerVBox(10, GUIElements.subheading("You Chose:"), ServerGUI.choices[info.iChose]);
            VBox oppChose = GUIElements.centerVBox(10, GUIElements.subheading("Opponent Chose:"), ServerGUI.choices[info.oppChose]);

            grid.add(GUIElements.centerHBox(100, uChose, oppChose), 0, 2);
            grid.add(GUIElements.centerBox(new Text("You Win!!!")), 0, 6);

            Button playAgain = new Button("Play Again");
            playAgain.setOnAction(e-> {
                ((Text)roundBox.getChildren().get(0)).setText("Round " + (++roundNo));
                stage.setScene(gameScene);
            });
            grid.add(GUIElements.centerHBox(50, playAgain, quitBtn()), 0, 8);

            Scene resultScene = new Scene(grid, 750, 450);
            resultScene.getStylesheets().add(RPLS.stylesheet);
            stage.setScene(resultScene);
        }
        else {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Game Result For Client " + clientNo);
            alert.setHeaderText(info.iWon? "You Win!!!" : "Sorry, You Lose.");

            ButtonType playAgain = new ButtonType("Play Again");
            ButtonType quit = new ButtonType("Quit");
            alert.getButtonTypes().setAll(playAgain, quit);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == playAgain) {
                Text roundText = (Text) roundBox.getChildren().get(0);
                roundText.setText("Round " + (++roundNo));
            }
            else {
                client.close();
                stage.close();
            }
        }
        for (int j = 0; j < 5; ++j)
            selections[j].setDisable(false);
    }

    private void createGameScene() {
        selections = new Button[5];
        for (int i = 0; i < 5; ++i) {
            ImageView img = new ImageView(new Image(RPLS.class.getResourceAsStream(ServerGUI.imageUrls[i])));
            img.setFitWidth(50);
            img.setPreserveRatio(true);
            selections[i] = new Button("", img);
            int finalI = i;
            selections[i].setOnAction(e-> {
                for (int j = 0; j < 5; ++j)
                    selections[j].setDisable(true);
                client.send(finalI);
            });
        }

        GridPane grid = GUIElements.createGrid(10, 25);

        roundBox = GUIElements.centerBox(GUIElements.headerText("Round 0"));
        grid.add(roundBox, 0, 0);

        HBox pick = GUIElements.centerBox(new Text("Take your pick"));
        grid.add(pick, 0, 2);

        HBox choices = new HBox(selections);
        choices.setSpacing(40);
        choices.setAlignment(Pos.CENTER);
        grid.add(choices, 0, 4);

        grid.add(GUIElements.centerBox(quitBtn()), 0, 8);

        gameScene = new Scene(grid, 750, 500);
        gameScene.getStylesheets().add(RPLS.stylesheet);
    }

    ClientGUI() {
        createHomeScene();
        createLoadingScreen();
        createGameScene();
        stage.setOnCloseRequest(e-> {
            if (client != null) {
                if (oppDisconnected)
                    client.exit = true;
                else
                    client.close();
            }
        });
        stage.setScene(homeScene);
        stage.setTitle("RPSLS Client " + clientNo);
        stage.show();
    }
}
