import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class ReaderApp extends Application {
    private ComboBox<String> bookSelector;
    private ComboBox<Integer> chapterSelector;
    private TextArea verseArea;
    private Button prevButton;
    private Button nextButton;

    private final String DATA_FOLDER = "data/";
    private final String PROGRESS_FILE = "progresso.txt";
    private Map<Integer, List<String>> currentChapters = new HashMap<>();
    private int currentVerseIndex = 0;

    private void applyStyles(Scene scene) {
        File cssFile = new File("src\\dark-theme.css");
        if (cssFile.exists()) {
            try {
                String css = cssFile.toURI().toURL().toExternalForm();
                scene.getStylesheets().add(css);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        } else{
            System.out.println("Arquivo CSS não encontrado "+ cssFile.getAbsolutePath());
        }
    }

    @Override
    public void start(Stage primaryStage) {
        bookSelector = new ComboBox<>();
        chapterSelector = new ComboBox<>();
        verseArea = new TextArea();
        prevButton = new Button("Anterior");
        nextButton = new Button("Próximo");

        verseArea.setWrapText(true);
        verseArea.setStyle("-fx-font-size: 40px;");
        verseArea.setEditable(false);

        loadBookList();
        loadProgress();

        bookSelector.setOnAction(e -> {
            String selectedBook = bookSelector.getValue();
            if (selectedBook != null) {
                loadBookContent(selectedBook);
                saveProgress();
            }
        });

        chapterSelector.setOnAction(e -> {
            Integer selectedChapter = chapterSelector.getValue();
            if (selectedChapter != null) {
                currentVerseIndex = 0;
                showVerse(selectedChapter, currentVerseIndex);
                saveProgress();
            }
        });

        prevButton.setOnAction(e -> {
            if (currentVerseIndex > 0) {
                currentVerseIndex--;
                showVerse(chapterSelector.getValue(), currentVerseIndex);
                saveProgress();
            }
        });

        nextButton.setOnAction(e -> {
            if (currentVerseIndex < currentChapters.get(chapterSelector.getValue()).size() - 1) {
                currentVerseIndex++;
                showVerse(chapterSelector.getValue(), currentVerseIndex);
                saveProgress();
            }
        });

        HBox navigationBox = new HBox(10, prevButton, nextButton);
        VBox layout = new VBox(10, new Label("Livro:"), bookSelector, new Label("Capítulo:"), chapterSelector, verseArea, navigationBox);
        layout.setStyle("-fx-padding: 20;");

        Scene scene = new Scene(layout, 800, 800);
        
        applyStyles(scene);
        
        primaryStage.setTitle("Leitor de Versículos");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void loadBookList() {
        try {
            List<String> books = Files.list(Paths.get(DATA_FOLDER))
                    .filter(p -> p.toString().endsWith(".txt"))
                    .map(p -> p.getFileName().toString().replace(".txt", ""))
                    .sorted()
                    .collect(Collectors.toList());

            bookSelector.getItems().setAll(books);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadBookContent(String bookName) {
        Path path = Paths.get(DATA_FOLDER + bookName + ".txt");
        currentChapters.clear();
        chapterSelector.getItems().clear();

        try {
            List<String> lines = Files.readAllLines(path);
            int currentChapter = -1;

            for (String line : lines) {
                if (line.matches(".*\\s\\d+$")) {
                    String[] parts = line.trim().split(" ");
                    currentChapter = Integer.parseInt(parts[parts.length - 1]);
                    currentChapters.put(currentChapter, new ArrayList<>());
                } else if (currentChapter != -1) {
                    currentChapters.get(currentChapter).add(line);
                }
            }

            chapterSelector.getItems().addAll(currentChapters.keySet());
            if (!currentChapters.isEmpty()) {
                chapterSelector.setValue(Collections.min(currentChapters.keySet()));
                currentVerseIndex = 0;
                showVerse(Collections.min(currentChapters.keySet()), currentVerseIndex);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showVerse(int chapter, int verseIndex) {
        List<String> verses = currentChapters.get(chapter);
        if (verses != null && verseIndex >= 0 && verseIndex < verses.size()) {
            verseArea.setText(verses.get(verseIndex));
        } else {
            verseArea.setText("Versículo não encontrado.");
        }
    }

    private void saveProgress() {
        String selectedBook = bookSelector.getValue();
        Integer selectedChapter = chapterSelector.getValue();

        if (selectedBook != null && selectedChapter != null) {
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(PROGRESS_FILE))) {
                writer.write(selectedBook);
                writer.newLine();
                writer.write(selectedChapter.toString());
                writer.newLine();
                writer.write(String.valueOf(currentVerseIndex));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadProgress() {
        Path path = Paths.get(PROGRESS_FILE);
        if (Files.exists(path)) {
            try (BufferedReader reader = Files.newBufferedReader(path)) {
                String savedBook = reader.readLine();
                String savedChapterStr = reader.readLine();
                String savedVerseIndexStr = reader.readLine();

                if (savedBook != null && savedChapterStr != null && savedVerseIndexStr != null) {
                    int savedChapter = Integer.parseInt(savedChapterStr);
                    int savedVerseIndex = Integer.parseInt(savedVerseIndexStr);
                    bookSelector.setValue(savedBook);
                    loadBookContent(savedBook);
                    chapterSelector.setValue(savedChapter);
                    currentVerseIndex = savedVerseIndex;
                    showVerse(savedChapter, savedVerseIndex);
                }
            } catch (IOException | NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
