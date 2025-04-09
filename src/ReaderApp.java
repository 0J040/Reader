import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.nio.file.*;
import java.util.List;

public class ReaderApp extends Application {
    private List<String> verses;
    private int currentIndex = 0;
    private Label verseLabel;

    @Override
    public void start(Stage primaryStage) throws Exception {
        verses = Files.readAllLines(Paths.get("data/genesis.txt"));

        verseLabel = new Label(getCurrentVerse());
        verseLabel.setStyle("-fx-font-size: 32px; -fx-wrap-text: true;");
        Button nextButton = new Button("Próximo versículo");
        nextButton.setOnAction(e -> showNextVerse());

        VBox layout = new VBox(20, verseLabel, nextButton);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        Scene scene = new Scene(layout, 700, 500);
        primaryStage.setTitle("Leitor de Versículos");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private String getCurrentVerse() {
        if (currentIndex < verses.size()) {
            return verses.get(currentIndex);
        } else {
            return "Você terminou todos os versículos!";
        }
    }

    private void showNextVerse() {
        if (currentIndex < verses.size() - 1) {
            currentIndex++;
            verseLabel.setText(getCurrentVerse());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
 