package com.example.programming_group_java;

import javafx.application.Application;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;

/* =============================================================================
   PROGRAMMING PROJECT 4: LIBRARY DB

   GROUP MEMBERS & CONTRIBUTIONS:
   - [Galyn Ridley]: Boilerplate structure, JavaFX UI wiring, integration, custom dark theme, and split-pane layout.
   - [Sergio Madrid]: DatabaseManager implementation (JDBC connection & SQL CRUD queries).
   - [Nicole Izquierdo]: Book and Author model classes with JavaFX properties.
   - [Elio Machin]: JavaFX layout design, controls, and TableView configuration.
   ============================================================================= */

/* LibraryBookManager class runs the application
and instantiates the database connection,
UI/UX interface, and contains nested
SQL and model classes
 */

public class LibraryBookManager extends Application {

    // Persistent Database Manager instance
    private static DatabaseManager dbManager;

    // UI Controls
    private final TableView<Book> bookTable = new TableView<>();
    private final TextField titleField = new TextField();
    private final TextField yearField = new TextField();
    private final ComboBox<Author> authorComboBox = new ComboBox<>();

    private final Button btnAdd = new Button("Add");
    private final Button btnUpdate = new Button("Update");
    private final Button btnDelete = new Button("Delete");
    private final Button btnRefresh = new Button("Refresh");
    private final Button btnSpecial = new Button("Special Button");

    // Tracks which Book row is currently selected in the table, so
    // Update/Delete know which BookID to act on. (Needed to support the
    // "Get selected Book from table" TODOs below.)
    private Book selectedBook = null;

    @Override
    public void start(Stage primaryStage) {
        // Initialize persistent DB Connection
        dbManager = new DatabaseManager();

        if (dbManager.connection == null) {
            showAlert("Database Connection Failed",
                    "Could not connect to the database. Please verify the MySQL server is running.");
            return;
        }

        primaryStage.setTitle("Library Book Manager - Sleek Edition");

        // --- 1. LEFT PANEL: INPUT FORM & BUTTONS ---
        GridPane inputGrid = new GridPane();
        inputGrid.setHgap(10);
        inputGrid.setVgap(12);

        titleField.setPromptText("Enter Book Title");
        yearField.setPromptText("4-digit Year (e.g. 2024)");
        authorComboBox.setPromptText("Select Author");
        authorComboBox.setMaxWidth(Double.MAX_VALUE);

        // Styling Form Labels
        Label lblTitle = new Label("Title:");
        Label lblAuthor = new Label("Author:");
        Label lblYear = new Label("Year Published:");

        String labelStyle = "-fx-text-fill: #E0E0E0; -fx-font-weight: bold;";
        lblTitle.setStyle(labelStyle);
        lblAuthor.setStyle(labelStyle);
        lblYear.setStyle(labelStyle);

        // Styling Input Fields
        String fieldStyle = "-fx-background-color: #2A2A2A; -fx-text-fill: white; -fx-prompt-text-fill: #888888; -fx-border-color: #444444; -fx-border-radius: 4; -fx-background-radius: 4;";
        titleField.setStyle(fieldStyle);
        yearField.setStyle(fieldStyle);

        // ComboBox specific dark style (ensures selected text and popup menu items are light text)
        authorComboBox.setStyle(
                "-fx-background-color: #2A2A2A; " +
                        "-fx-mark-color: #E0E0E0; " + // Arrow icon color
                        "-fx-border-color: #444444; " +
                        "-fx-border-radius: 4; " +
                        "-fx-background-radius: 4;"
        );

        // Ensures selected text in ComboBox reads clearly as light grey/white
        authorComboBox.setCellFactory(lv -> new ListCell<Author>() {
            @Override
            protected void updateItem(Author item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: #2A2A2A; -fx-text-fill: white;");
                } else {
                    setText(item.getName());
                    setStyle("-fx-background-color: #2A2A2A; -fx-text-fill: white;");
                }
            }
        });

        authorComboBox.setButtonCell(new ListCell<Author>() {
            @Override
            protected void updateItem(Author item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(authorComboBox.getPromptText());
                    setStyle("-fx-text-fill: #888888;"); // Prompt text color
                } else {
                    setText(item.getName());
                    setStyle("-fx-text-fill: white;"); // Selected text color
                }
            }
        });

        inputGrid.add(lblTitle, 0, 0);
        inputGrid.add(titleField, 0, 1);

        inputGrid.add(lblAuthor, 0, 2);
        inputGrid.add(authorComboBox, 0, 3);

        inputGrid.add(lblYear, 0, 4);
        inputGrid.add(yearField, 0, 5);

        // --- 2. COLORED BUTTONS ---
        applyButtonStyles();

        // 2x2 Grid for Buttons on the left side
        GridPane buttonGrid = new GridPane();
        buttonGrid.setHgap(10);
        buttonGrid.setVgap(10);

        btnAdd.setMaxWidth(Double.MAX_VALUE);
        btnUpdate.setMaxWidth(Double.MAX_VALUE);
        btnDelete.setMaxWidth(Double.MAX_VALUE);
        btnRefresh.setMaxWidth(Double.MAX_VALUE);
        btnSpecial.setMaxWidth(Double.MAX_VALUE);

        // Spans 2 columns on row 2 so it stretches underneath the other 4 buttons
        buttonGrid.add(btnSpecial, 0, 2, 2, 1);
        buttonGrid.add(btnAdd, 0, 0);
        buttonGrid.add(btnUpdate, 1, 0);
        buttonGrid.add(btnDelete, 0, 1);
        buttonGrid.add(btnRefresh, 1, 1);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        buttonGrid.getColumnConstraints().addAll(col1, col2);

        Label headerLabel = new Label("Book Management");
        headerLabel.setStyle("-fx-text-fill: #FFFFFF; -fx-font-size: 18px; -fx-font-weight: bold;");

        VBox leftPanel = new VBox(20);
        leftPanel.setPadding(new Insets(20));
        leftPanel.setStyle("-fx-background-color: #1E1E1E;");
        leftPanel.getChildren().addAll(headerLabel, inputGrid, new Separator(), buttonGrid);

        // --- 3. RIGHT PANEL: TABLE VIEW ---
        TableColumn<Book, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(cellData -> cellData.getValue().titleProperty());

        TableColumn<Book, String> authorCol = new TableColumn<>("Author Name");
        authorCol.setCellValueFactory(cellData -> cellData.getValue().authorNameProperty());

        TableColumn<Book, Number> yearCol = new TableColumn<>("Year Published");
        yearCol.setCellValueFactory(cellData -> cellData.getValue().yearPublishedProperty());

        bookTable.getColumns().add(titleCol);
        bookTable.getColumns().add(authorCol);
        bookTable.getColumns().add(yearCol);
        bookTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Dark theme for TableView
        bookTable.setStyle("-fx-background-color: #121212; -fx-base: #1E1E1E; -fx-control-inner-background: #1E1E1E; -fx-table-cell-border-color: #333333;");

        bookTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            selectedBook = newSel;
            populateFormFromSelection(newSel);
        });

        VBox rightPanel = new VBox(10);
        rightPanel.setPadding(new Insets(15));
        rightPanel.setStyle("-fx-background-color: #121212;");
        VBox.setVgrow(bookTable, Priority.ALWAYS);
        rightPanel.getChildren().add(bookTable);

        // --- 4. SPLIT PANE MAIN LAYOUT ---
        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(leftPanel, rightPanel);
        splitPane.setDividerPositions(0.35); // 35% left panel, 65% table panel
        splitPane.setStyle("-fx-background-color: #121212;");

        // --- 5. EVENT HANDLERS ---
        btnRefresh.setOnAction(e -> refreshData());
        btnAdd.setOnAction(e -> handleAddBook());
        btnUpdate.setOnAction(e -> handleUpdateBook());
        btnDelete.setOnAction(e -> handleDeleteBook());

        btnSpecial.setOnAction(e -> getHostServices().showDocument("https://www.youtube.com/watch?v=SPc9RyoRO20"));

        // Load initial data
        refreshData();

        Scene scene = new Scene(splitPane, 850, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void applyButtonStyles() {
        String baseBtnStyle = "-fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand; -fx-padding: 8 15 8 15;";

        // Light Green
        btnAdd.setStyle(baseBtnStyle + "-fx-background-color: #A8E6CF; -fx-text-fill: #113F2A;");
        // Light Blue
        btnUpdate.setStyle(baseBtnStyle + "-fx-background-color: #AED9E0; -fx-text-fill: #1A3B40;");
        // Light Red
        btnDelete.setStyle(baseBtnStyle + "-fx-background-color: #FF8B94; -fx-text-fill: #4A121A;");
        // Light Yellow
        btnRefresh.setStyle(baseBtnStyle + "-fx-background-color: #FFF5BA; -fx-text-fill: #4A4312;");
        // Gold Style
        btnSpecial.setStyle(baseBtnStyle + "-fx-background-color: #FFD700; -fx-text-fill: #3E2723;");
    }

    // --- INTEGRATION HELPERS ---
    private void refreshData() {
        if (dbManager != null) {
            bookTable.setItems(dbManager.getAllBooks());
            authorComboBox.setItems(dbManager.getAllAuthors());
        }
    }

    // error handling method for adding a book
    private void handleAddBook() {
        String title = titleField.getText();
        Author selectedAuthor = authorComboBox.getSelectionModel().getSelectedItem();
        String yearText = yearField.getText();

        if (title == null || title.trim().isEmpty()) {
            showAlert("Missing Title", "Please enter a book title.");
            return;
        }
        if (selectedAuthor == null) {
            showAlert("Missing Author", "Please select an author.");
            return;
        }
        if (yearText == null || !yearText.trim().matches("\\d{4}")) {
            showAlert("Invalid Year", "Please enter a valid 4-digit year (e.g. 2024).");
            return;
        }

        int year = Integer.parseInt(yearText.trim());
        boolean success = dbManager.addBook(title.trim(), selectedAuthor.getAuthorID(), year);

        if (success) {
            refreshData();
            clearForm();
        } else {
            showAlert("Add Failed", "Could not add the book. Please try again.");
        }
    }

    // Handles updating book on application
    private void handleUpdateBook() {
        if (selectedBook == null) {
            showAlert("No Selection", "Please select a book to update.");
            return;
        }

        String title = titleField.getText();
        Author selectedAuthor = authorComboBox.getSelectionModel().getSelectedItem();
        String yearText = yearField.getText();

        if (title == null || title.trim().isEmpty()) {
            showAlert("Missing Title", "Please enter a book title.");
            return;
        }
        if (selectedAuthor == null) {
            showAlert("Missing Author", "Please select an author.");
            return;
        }
        if (yearText == null || !yearText.trim().matches("\\d{4}")) {
            showAlert("Invalid Year", "Please enter a valid 4-digit year (e.g. 2024).");
            return;
        }

        int year = Integer.parseInt(yearText.trim());
        boolean success = dbManager.updateBook(
                selectedBook.getBookID(), title.trim(), selectedAuthor.getAuthorID(), year);

        if (success) {
            refreshData();
            clearForm();
        } else {
            showAlert("Update Failed", "Could not update the book. Please try again.");
        }
    }

    // Dynamic handling for deleting book from table
    private void handleDeleteBook() {
        if (selectedBook == null) {
            showAlert("No Selection", "Please select a book to delete.");
            return;
        }

        boolean success = dbManager.deleteBook(selectedBook.getBookID());

        if (success) {
            refreshData();
            clearForm();
        } else {
            showAlert("Delete Failed", "Could not delete the book. Please try again.");
        }
    }

    // Populate input form when you click on corresponding table tuple
    private void populateFormFromSelection(Book book) {
        if (book == null) {
            clearForm();
            return;
        }

        titleField.setText(book.getTitle());
        yearField.setText(String.valueOf(book.getYearPublished()));

        for (Author author : authorComboBox.getItems()) {
            if (author.getName().equals(book.getAuthorName())) {
                authorComboBox.getSelectionModel().select(author);
                break;
            }
        }
    }

    // Resets user interface back to inital blank slate after action is completed
    private void clearForm() {
        titleField.clear();
        yearField.clear();
        authorComboBox.getSelectionModel().clearSelection();
        bookTable.getSelectionModel().clearSelection();
        selectedBook = null;
    }

    // Displays a popup alert window with a custom title and error message
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Database connection error handling method
    @Override
    public void stop() throws Exception {
        if (dbManager != null) {
            dbManager.closeConnection();
        }
        super.stop();
    }

    // Boots application
    public static void main(String[] args) {
        launch(args);
    }

    // =========================================================================
    // 1. DATABASE MANAGER CLASS
    // =========================================================================

    /* Runs all the SQL operations
    for insertion, deletion, updating,
    and refreshing the DBMS
     */

    public static class DatabaseManager {

        private Connection connection;

        // MySQL parameters for connected to localhost DBMS
        private static final String URL = "jdbc:mysql://localhost:3306/librarydb";
        private static final String USER = "scott";
        private static final String PASSWORD = "tiger";

        // DBMS connection instance
        public DatabaseManager() {
            try {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Connected to database.");
            } catch (SQLException e) {
                System.out.println("Database connection error: " + e.getMessage());
            }
        }

        // Accessor method for loading all books within DBMS
        public ObservableList<Book> getAllBooks() {
            ObservableList<Book> books = FXCollections.observableArrayList();
            String sql = "SELECT Books.BookID, Books.Title, Authors.Name, Books.YearPublished " +
                    "FROM Books JOIN Authors ON Books.AuthorID = Authors.AuthorID";

            try {
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet results = statement.executeQuery();

                while (results.next()) {
                    books.add(new Book(
                            results.getInt("BookID"),
                            results.getString("Title"),
                            results.getString("Name"),
                            results.getInt("YearPublished")
                    ));
                }

                results.close();
                statement.close();
            } catch (SQLException e) {
                System.out.println("Error loading books: " + e.getMessage());
            }

            return books;
        }

        // Accessor method for Authors registered within the DBMS
        public ObservableList<Author> getAllAuthors() {
            ObservableList<Author> authors = FXCollections.observableArrayList();
            String sql = "SELECT AuthorID, Name FROM Authors";

            try {
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet results = statement.executeQuery();

                while (results.next()) {
                    authors.add(new Author(
                            results.getInt("AuthorID"),
                            results.getString("Name")
                    ));
                }

                results.close();
                statement.close();
            } catch (SQLException e) {
                System.out.println("Error loading authors: " + e.getMessage());
            }

            return authors;
        }

        // Add book method with safety handling
        public boolean addBook(String title, int authorId, int yearPublished) {
            String sql = "INSERT INTO Books (Title, AuthorID, YearPublished) VALUES (?, ?, ?)";

            try {
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setString(1, title);
                statement.setInt(2, authorId);
                statement.setInt(3, yearPublished);

                int rowsChanged = statement.executeUpdate();
                statement.close();

                return rowsChanged > 0;
            } catch (SQLException e) {
                System.out.println("Error adding book: " + e.getMessage());
                return false;
            }
        }

        // Update book method with safety handling
        public boolean updateBook(int bookId, String title, int authorId, int yearPublished) {
            String sql = "UPDATE Books SET Title = ?, AuthorID = ?, YearPublished = ? WHERE BookID = ?";

            try {
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setString(1, title);
                statement.setInt(2, authorId);
                statement.setInt(3, yearPublished);
                statement.setInt(4, bookId);

                int rowsChanged = statement.executeUpdate();
                statement.close();

                return rowsChanged > 0;
            } catch (SQLException e) {
                System.out.println("Error updating book: " + e.getMessage());
                return false;
            }
        }

        // Delete book method with safety handling
        public boolean deleteBook(int bookId) {
            String sql = "DELETE FROM Books WHERE BookID = ?";

            try {
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setInt(1, bookId);

                int rowsChanged = statement.executeUpdate();
                statement.close();

                return rowsChanged > 0;
            } catch (SQLException e) {
                System.out.println("Error deleting book: " + e.getMessage());
                return false;
            }
        }

        // Safely close connection and catch exceptions for DBMS
        public void closeConnection() {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                    System.out.println("Database connection closed.");
                }
            } catch (SQLException e) {
                System.out.println("Error closing database: " + e.getMessage());
            }
        }
    }

    // =========================================================================
    // 2. BOOK MODEL CLASS
    // =========================================================================

    //Book class represents a book and its information
    public static class Book {

        //Stores the unique ID of the book using a JavaFx IntegerProperty
        private final IntegerProperty bookID;

        //Stores the title of the book using a javaFX StringProperty
        private final StringProperty title;

        //Stores the name of the book's author using a JavaFx String
        private final StringProperty authorName;

        //Stores the year the book was published using a javaFX IntegerProperty
        private final IntegerProperty yearPublished;

        //Constructor creates a book object with its ID, title, author name, and publication year
        public Book(int bookID, String title, String authorName, int yearPublished) {

            //Initialize the book ID property
            this.bookID = new SimpleIntegerProperty(bookID);

            //Initialize the title property
            this.title = new SimpleStringProperty(title);

            //Initialize the author name property
            this.authorName = new SimpleStringProperty(authorName);

            //Initialize the year published property
            this.yearPublished = new SimpleIntegerProperty(yearPublished);
        }

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
    // 3. AUTHOR MODEL CLASS
    // =========================================================================


    // Author class represents an author with an ID and a name
    public static class Author {

        //Stores the author's name using a JavaFX IntegerProperty
        private final IntegerProperty authorID;

        //Stores the author's name using a JavaFX StringProperty
        private final StringProperty name;

        //Constructor creates an Author object with an ID and name
        public Author(int authorID, String name) {

            //Initialize the author ID property
            this.authorID = new SimpleIntegerProperty(authorID);

            //Initialize the name property
            this.name = new SimpleStringProperty(name);
        }

        //Returns the author's ID as an integer
        public int getAuthorID() { return authorID.get(); }

        //Returns the JavafX IntegerProperty for the author's ID
        public IntegerProperty authorIDProperty() { return authorID; }


        //Returns the author's name as a String
        public String getName() { return name.get(); }

        //Returns the JavaFX StringProperty for the author's name
        public StringProperty nameProperty() { return name; }

        @Override
        public String toString() {
            return getName();
        }
    }
}