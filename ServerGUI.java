import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ServerGUI {
    private static int serverCount = 0;
    int serverNo = serverCount++;
    Scene homeScene;
    Scene loadingScreen;
    HBox playersJoined;
    PauseTransition pause = new PauseTransition(Duration.seconds(2));
    Scene  gameScene;
    Stage stage = new Stage();
    HBox roundBox;
    static String[] imageUrls = {"rock.png", "paper.png", "scissors.png", "lizard.png", "spock.png"};
    static ImageView[] choices;
    ImageView on, off;
    ImageView[] blanks;
    GridPane[] infoGrids;
    Server theServer;
    Node[][] gridInfo = new Node[2][8];

    private void createHomeScene() {
        GridPane grid = GUIElements.createGrid(10,25);

        Text server = new Text("Server Station");
        grid.add(server, 0, 0, 2, 1);

        Label portLabel = new Label("Port Number");
        TextField portField = new TextField();

        grid.add(portLabel, 0, 1);
        grid.add(portField, 1, 1);

        Button startBtn = new Button("Start");
        grid.add(GUIElements.centerBox(startBtn), 0, 5, 2, 1);

        startBtn.setOnAction(e-> {
            try {
                int port = Integer.parseInt(portField.getText());
                theServer = new Server(port, serverNo, action-> Platform.runLater(()->action.accept(this)));
            } catch (NumberFormatException ex) {
                System.out.println("Invalid Port Number");
            }
            portField.clear();
        });

        homeScene = new Scene(grid, 500, 500);
        homeScene.getStylesheets().add(RPLS.stylesheet);
    }
    private static HBox playerJoinedBox(String player) {
        HBox hBox = new HBox(GUIElements.imageView("tick.png"), GUIElements.subheading(player + " connected"));
        hBox.setSpacing(10);
        hBox.setVisible(false);
        return hBox;
    }
    void resetAll() {

    }

    private Button quitBtn() {
        Button returnBtn = new Button("Quit");
        returnBtn.setOnAction(e-> {
            theServer.close();
            stage.close();
        });
        returnBtn.setAlignment(Pos.CENTER);
        return returnBtn;
    }

    private void createLoadingScreen() {
        GridPane grid = GUIElements.createGrid(20, 25);

        pause.setOnFinished(e->{
            stage.setScene(gameScene);
        });

        grid.add(GUIElements.centerBox(new Text("Waiting for 2 connections . . .")), 0, 0);

        playersJoined = new HBox(playerJoinedBox("Player1"), playerJoinedBox("Player2"));
        playersJoined.setSpacing(50);
        grid.add(playersJoined, 0, 2);

        grid.add(GUIElements.centerBox(quitBtn()), 0, 4);

        loadingScreen = new Scene(grid, 800, 450);
        loadingScreen.getStylesheets().add(RPLS.stylesheet);
    }

    private GridPane createGameInfo(int count) {
        GridPane grid = GUIElements.createGrid(10, 25);

        gridInfo[count][0] = GUIElements.largeLabel("Player " + count);
        gridInfo[count][1] = GUIElements.boxWithText("0 Points");
        gridInfo[count][2] = GUIElements.mediumLabel("Connection");
        gridInfo[count][3] = GUIElements.imageView("success.png");
        gridInfo[count][4] = GUIElements.mediumLabel("Played");
        gridInfo[count][5] = blanks[count * 2];
        gridInfo[count][6] = GUIElements.mediumLabel("Won?");
        gridInfo[count][7] = blanks[count * 2 + 1];

        grid.add(gridInfo[count][0], 0, 2);
        grid.add(gridInfo[count][1], 1, 2);
        grid.add(gridInfo[count][2], 0, 3);
        grid.add(gridInfo[count][3], 1, 3);
        grid.add(gridInfo[count][4], 0, 4);
        grid.add(gridInfo[count][5], 1, 4);
        grid.add(gridInfo[count][6], 0, 5);
        grid.add(gridInfo[count][7], 1, 5);

        return grid;
    }
    void updateConnection(int count) {
        playersJoined.getChildren().get(count).setVisible(false);
        updatePlayersScore(count, 0);
        resetPlayerInfo(count);
        updateRoundInfo(0);
        stage.setScene(loadingScreen);
    }
    void updatePlayerPlayed(int count, int played) {
        infoGrids[count].getChildren().remove(gridInfo[count][5]);
        gridInfo[count][5] = choices[played];
        infoGrids[count].add(gridInfo[count][5], 1, 4);
    }
    void updateWhoWon(int count, boolean won) {
        infoGrids[count].getChildren().remove(gridInfo[count][7]);
        gridInfo[count][7] = won? on : off;
        infoGrids[count].add(gridInfo[count][7], 1, 5);
    }
    void resetPlayerInfo(int count) {
        infoGrids[count].getChildren().remove(gridInfo[count][5]);
        gridInfo[count][5] = blanks[count*2];
        infoGrids[count].add(gridInfo[count][5], 1, 4);

        infoGrids[count].getChildren().remove(gridInfo[count][7]);
        gridInfo[count][7] = blanks[count*2+1];
        infoGrids[count].add(gridInfo[count][7], 1, 5);
    }
    void updatePlayersScore(int count, int score) {
        ((Text) ((HBox)gridInfo[count][1]).getChildren().get(0)).setText(score + " Points");
    }
    void updateRoundInfo(int roundNo) {
        ((Text) roundBox.getChildren().get(0)).setText("Round " + roundNo);
    }
    private void createGameScene() {
        if (choices == null) {
            choices = new ImageView[5];
            for (int i = 0; i < 5; ++i)
                choices[i] = GUIElements.imageView(imageUrls[i]);
        }

        on = GUIElements.imageView("success.png");
        off = GUIElements.imageView("failure.png");
        blanks = new ImageView[4];
        for (int i = 0; i < 4; ++i)
            blanks[i] = GUIElements.imageView("blank-square.png");

        GridPane grid = GUIElements.createGrid(20, 25);
        roundBox = GUIElements.centerBox(GUIElements.headerText("Round 0"));
        grid.add(roundBox, 0, 0);

        infoGrids = new GridPane[]{createGameInfo(0), createGameInfo(1)};

        HBox infoBox = new HBox(infoGrids[0], infoGrids[1]);
        infoBox.setSpacing(70);
        grid.add(infoBox, 0, 2);

        grid.add(GUIElements.centerBox(quitBtn()), 0, 6);

        gameScene = new Scene(grid, 750, 600);
        gameScene.getStylesheets().add(RPLS.stylesheet);
    }

    ServerGUI() {
        createHomeScene();
        createLoadingScreen();
        createGameScene();
        stage.setOnCloseRequest(e-> {
            if (theServer != null)
                theServer.close();
        });
        stage.setScene(homeScene);
        stage.setTitle("RPSLS Server " + serverNo);
        stage.show();
    }

}
