package com.example.programming_group_java;

import javafx.application.Application;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;

/* =============================================================================
   PROGRAMMING PROJECT 4: LIBRARY DB

   GROUP MEMBERS & CONTRIBUTIONS:
   - [Team Lead / Architect Name]: Boilerplate structure, JavaFX UI wiring, integration, and error handling.
   - [Database Lead Name]: DatabaseManager implementation (JDBC connection & SQL CRUD queries).
   - [Data Models Lead Name]: Book and Author model classes with JavaFX properties.
   - [UI/UX Lead Name]: JavaFX layout design, controls, and TableView configuration.
   ============================================================================= */

public class LibraryBookManager extends Application {

    // Persistent Database Manager instance
    private static DatabaseManager dbManager;

    // UI Controls (Managed by UI/UX Lead)
    private TableView<Book> bookTable = new TableView<>();
    private TextField titleField = new TextField();
    private TextField yearField = new TextField();
    private ComboBox<Author> authorComboBox = new ComboBox<>();

    private Button btnAdd = new Button("Add");
    private Button btnUpdate = new Button("Update");
    private Button btnDelete = new Button("Delete");
    private Button btnRefresh = new Button("Refresh");

    @Override
    public void start(Stage primaryStage) {
        // Initialize persistent DB Connection
        dbManager = new DatabaseManager();

        primaryStage.setTitle("Library Book Manager");

        // --- 1. TOP SECTION: INPUT FORM (UI Lead) ---
        GridPane inputGrid = new GridPane();
        inputGrid.setHgap(10);
        inputGrid.setVgap(10);

        titleField.setPromptText("Enter Book Title");
        yearField.setPromptText("4-digit Year (e.g. 2024)");
        authorComboBox.setPromptText("Select Author");

        inputGrid.add(new Label("Title:"), 0, 0);
        inputGrid.add(titleField, 1, 0);

        inputGrid.add(new Label("Author:"), 0, 1);
        inputGrid.add(authorComboBox, 1, 1);

        inputGrid.add(new Label("Year Published:"), 0, 2);
        inputGrid.add(yearField, 1, 2);

        // --- 2. BUTTON CONTROLS (UI Lead) ---
        HBox buttonBox = new HBox(10, btnAdd, btnUpdate, btnDelete, btnRefresh);

        // --- 3. TABLE VIEW (UI Lead) ---
        // TODO UI Lead: Define TableColumns for Title, Author Name, and Year Published
        // Example:
        // TableColumn<Book, String> titleCol = new TableColumn<>("Title");
        // titleCol.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        // bookTable.getColumns().addAll(titleCol, ...);

        // --- 4. MAIN LAYOUT ASSEMBLY ---
        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(15));
        mainLayout.getChildren().addAll(inputGrid, buttonBox, bookTable);

        // --- 5. EVENT HANDLERS (Team Lead / Wiring) ---
        btnRefresh.setOnAction(e -> refreshData());
        btnAdd.setOnAction(e -> handleAddBook());
        btnUpdate.setOnAction(e -> handleUpdateBook());
        btnDelete.setOnAction(e -> handleDeleteBook());

        // Load initial data on launch
        refreshData();

        primaryStage.setScene(new Scene(mainLayout, 750, 550));
        primaryStage.show();
    }

    // --- INTEGRATION HELPERS (Team Lead) ---
    private void refreshData() {
        if (dbManager != null) {
            bookTable.setItems(dbManager.getAllBooks());
            authorComboBox.setItems(dbManager.getAllAuthors());
        }
    }

    private void handleAddBook() {
        // TODO Team Lead / UI: Validate inputs, extract values, call dbManager.addBook()
    }

    private void handleUpdateBook() {
        // TODO Team Lead / UI: Get selected Book from table, update via dbManager
    }

    private void handleDeleteBook() {
        // TODO Team Lead / UI: Get selected Book from table, delete via dbManager
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @Override
    public void stop() throws Exception {
        if (dbManager != null) {
            dbManager.closeConnection();
        }
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }

    // =========================================================================
    // 1. DATABASE MANAGER CLASS (Assigned to: Database Lead)
    // =========================================================================
    public static class DatabaseManager {
        private Connection connection;

        // Exact connection parameters required by rubric
        private static final String URL = "jdbc:mysql://localhost:3306/librarydb";
        private static final String USER = "scott";
        private static final String PASSWORD = "tiger";

        public DatabaseManager() {
            try {
                // Persistent connection throughout application lifetime
                this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Database connection established successfully.");
            } catch (SQLException e) {
                System.err.println("Database Connection Failed: " + e.getMessage());
            }
        }

        public ObservableList<Book> getAllBooks() {
            ObservableList<Book> books = FXCollections.observableArrayList();
            // TODO DB Lead: Execute SELECT JOIN query between Books and Authors tables
            // SELECT Books.BookID, Books.Title, Authors.Name AS AuthorName, Books.YearPublished
            // FROM Books JOIN Authors ON Books.AuthorID = Authors.AuthorID;
            return books;
        }

        public ObservableList<Author> getAllAuthors() {
            ObservableList<Author> authors = FXCollections.observableArrayList();
            // TODO DB Lead: Execute SELECT query on Authors table (AuthorID, Name)
            return authors;
        }

        public boolean addBook(String title, int authorId, int yearPublished) {
            // TODO DB Lead: Execute INSERT INTO Books (Title, AuthorID, YearPublished) VALUES (?, ?, ?)
            return false;
        }

        public boolean updateBook(int bookId, String title, int authorId, int yearPublished) {
            // TODO DB Lead: Execute UPDATE Books SET Title = ?, AuthorID = ?, YearPublished = ? WHERE BookID = ?
            return false;
        }

        public boolean deleteBook(int bookId) {
            // TODO DB Lead: Execute DELETE FROM Books WHERE BookID = ?
            return false;
        }

        public void closeConnection() {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                    System.out.println("Database connection closed.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // =========================================================================
    // 2. BOOK MODEL CLASS (Assigned to: Data Models Lead)
    // =========================================================================
    public static class Book {
        private final IntegerProperty bookID;
        private final StringProperty title;
        private final StringProperty authorName;
        private final IntegerProperty yearPublished;

        public Book(int bookID, String title, String authorName, int yearPublished) {
            this.bookID = new SimpleIntegerProperty(bookID);
            this.title = new SimpleStringProperty(title);
            this.authorName = new SimpleStringProperty(authorName);
            this.yearPublished = new SimpleIntegerProperty(yearPublished);
        }

        // Getters and Property methods for JavaFX TableView bindings
        public int getBookID() { return bookID.get(); }
        public IntegerProperty bookIDProperty() { return bookID; }

        public String getTitle() { return title.get(); }
        public StringProperty titleProperty() { return title; }

        public String getAuthorName() { return authorName.get(); }
        public StringProperty authorNameProperty() { return authorName; }

        public int getYearPublished() { return yearPublished.get(); }
        public IntegerProperty yearPublishedProperty() { return yearPublished; }
    }

    // =========================================================================
    // 3. AUTHOR MODEL CLASS (Assigned to: Data Models Lead)
    // =========================================================================
    public static class Author {
        private final IntegerProperty authorID;
        private final StringProperty name;

        public Author(int authorID, String name) {
            this.authorID = new SimpleIntegerProperty(authorID);
            this.name = new SimpleStringProperty(name);
        }

        public int getAuthorID() { return authorID.get(); }
        public IntegerProperty authorIDProperty() { return authorID; }

        public String getName() { return name.get(); }
        public StringProperty nameProperty() { return name; }

        // REQUIRED BY RUBRIC: Override toString() so ComboBox displays only the author's name
        @Override
        public String toString() {
            return getName();
        }
    }
}