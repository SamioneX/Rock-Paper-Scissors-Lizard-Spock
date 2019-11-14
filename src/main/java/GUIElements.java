import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

class GUIElements {
    static GridPane createGrid(int vap, int insets) {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(vap);
        grid.setVgap(vap);
        grid.setPadding(new Insets(insets, insets, insets, insets));
        return grid;
    }
    static ImageView imageView(String url) {
        ImageView img = new ImageView(new Image(RPLS.class.getResourceAsStream(url)));
        img.setFitWidth(50);
        img.setPreserveRatio(true);
        return img;
    }
    static HBox centerBox(Node n) {
        HBox hBox = new HBox(n);
        hBox.setAlignment(Pos.CENTER);
        return hBox;
    }
    static Text headerText(String s) {
        Text t = new Text(s);
        t.getStyleClass().add("header-text");
        return t;
    }
    static Text subheading(String s) {
        Text t = new Text(s);
        t.getStyleClass().add("subheading-text");
        return t;
    }
    static Label largeLabel(String s) {
        Label l = new Label(s);
        l.getStyleClass().add("large-label");
        return l;
    }
    static Label mediumLabel(String s) {
        Label l = new Label(s);
        l.getStyleClass().add("medium-label");
        return l;
    }
    static HBox boxWithText(String s) {
        Text t = new Text(s);
        t.getStyleClass().add("small-blue-text");
        HBox hBox = new HBox(t);
        hBox.setAlignment(Pos.CENTER);
        hBox.setMinSize(100, 10);
        hBox.getStyleClass().add("box-text");
        return hBox;
    }
    static VBox centerVBox(int spacing, Node... nodes) {
        VBox vBox = new VBox(nodes);
        vBox.setSpacing(spacing);
        vBox.setAlignment(Pos.CENTER);
        return vBox;
    }
    static HBox centerHBox(int spacing, Node... nodes) {
        HBox hBox = new HBox(nodes);
        hBox.setSpacing(spacing);
        hBox.setAlignment(Pos.CENTER);
        return hBox;
    }

}
