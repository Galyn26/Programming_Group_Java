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
   - [Sergio Madrid]: DatabaseManager implementation (JDBC connection & SQL CRUD queries).
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

    // Tracks which Book row is currently selected in the table, so
    // Update/Delete know which BookID to act on. (Needed to support the
    // "Get selected Book from table" TODOs below.)
    private Book selectedBook = null;

    @Override
    public void start(Stage primaryStage) {
        // Initialize persistent DB Connection
        dbManager = new DatabaseManager();

        // DatabaseManager's constructor swallows SQLException internally, so
        // check here whether the connection actually succeeded before going on.
        if (dbManager.connection == null) {
            showAlert("Database Connection Failed",
                    "Could not connect to the database. Please verify the MySQL server is running.");
            return;
        }

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
        TableColumn<Book, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(cellData -> cellData.getValue().titleProperty());

        TableColumn<Book, String> authorCol = new TableColumn<>("Author Name");
        authorCol.setCellValueFactory(cellData -> cellData.getValue().authorNameProperty());

        TableColumn<Book, Number> yearCol = new TableColumn<>("Year Published");
        yearCol.setCellValueFactory(cellData -> cellData.getValue().yearPublishedProperty());

        bookTable.getColumns().addAll(titleCol, authorCol, yearCol);

        // Track the selected row so Update/Delete know which book to act on,
        // and populate the form fields for easy editing.
        bookTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            selectedBook = newSel;
            populateFormFromSelection(newSel);
        });

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

    // Populates the form fields from a selected table row so the user can
    // see/edit the current values before clicking Update.
    private void populateFormFromSelection(Book book) {
        if (book == null) {
            clearForm();
            return;
        }

        titleField.setText(book.getTitle());
        yearField.setText(String.valueOf(book.getYearPublished()));

        // Book only stores the author's name (not AuthorID), so match by
        // name against the ComboBox's currently loaded Author list.
        for (Author author : authorComboBox.getItems()) {
            if (author.getName().equals(book.getAuthorName())) {
                authorComboBox.getSelectionModel().select(author);
                break;
            }
        }
    }

    private void clearForm() {
        titleField.clear();
        yearField.clear();
        authorComboBox.getSelectionModel().clearSelection();
        bookTable.getSelectionModel().clearSelection();
        selectedBook = null;
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
  
      // Database login information required by the professor
      private static final String URL =
              "jdbc:mysql://localhost:3306/librarydb";
  
      private static final String USER = "scott";
      private static final String PASSWORD = "tiger";
  
  
      // Constructor connects to the database
      public DatabaseManager() {
  
          try {
              connection =
                      DriverManager.getConnection(URL, USER, PASSWORD);
  
              System.out.println("Connected to database.");
  
          } catch (SQLException e) {
              System.out.println(
                      "Database connection error: " + e.getMessage());
          }
      }
  
  
      // READ: Gets all books and their author names
      public ObservableList<Book> getAllBooks() {
  
          ObservableList<Book> books =
                  FXCollections.observableArrayList();
  
          String sql =
                  "SELECT Books.BookID, Books.Title, " +
                  "Authors.Name, Books.YearPublished " +
                  "FROM Books " +
                  "JOIN Authors " +
                  "ON Books.AuthorID = Authors.AuthorID";
  
          try {
              PreparedStatement statement =
                      connection.prepareStatement(sql);
  
              ResultSet results =
                      statement.executeQuery();
  
              // Reads every book returned by MySQL
              while (results.next()) {
  
                  int bookID =
                          results.getInt("BookID");
  
                  String title =
                          results.getString("Title");
  
                  String authorName =
                          results.getString("Name");
  
                  int yearPublished =
                          results.getInt("YearPublished");
  
                  Book book = new Book(
                          bookID,
                          title,
                          authorName,
                          yearPublished
                  );
  
                  books.add(book);
              }
  
              results.close();
              statement.close();
  
          } catch (SQLException e) {
              System.out.println(
                      "Error loading books: " + e.getMessage());
          }
  
          return books;
      }
  
  
      // READ: Gets all authors for the ComboBox
      public ObservableList<Author> getAllAuthors() {
  
          ObservableList<Author> authors =
                  FXCollections.observableArrayList();
  
          String sql =
                  "SELECT AuthorID, Name FROM Authors";
  
          try {
              PreparedStatement statement =
                      connection.prepareStatement(sql);
  
              ResultSet results =
                      statement.executeQuery();
  
              // Reads every author returned by MySQL
              while (results.next()) {
  
                  int authorID =
                          results.getInt("AuthorID");
  
                  String name =
                          results.getString("Name");
  
                  Author author =
                          new Author(authorID, name);
  
                  authors.add(author);
              }
  
              results.close();
              statement.close();
  
          } catch (SQLException e) {
              System.out.println(
                      "Error loading authors: " + e.getMessage());
          }
  
          return authors;
      }
  
  
      // CREATE: Adds a new book
      public boolean addBook(
              String title,
              int authorId,
              int yearPublished) {
  
          String sql =
                  "INSERT INTO Books " +
                  "(Title, AuthorID, YearPublished) " +
                  "VALUES (?, ?, ?)";
  
          try {
              PreparedStatement statement =
                      connection.prepareStatement(sql);
  
              statement.setString(1, title);
              statement.setInt(2, authorId);
              statement.setInt(3, yearPublished);
  
              int rowsChanged =
                      statement.executeUpdate();
  
              statement.close();
  
              return rowsChanged > 0;
  
          } catch (SQLException e) {
              System.out.println(
                      "Error adding book: " + e.getMessage());
  
              return false;
          }
      }
  
  
      // UPDATE: Changes an existing book
      public boolean updateBook(
              int bookId,
              String title,
              int authorId,
              int yearPublished) {
  
          String sql =
                  "UPDATE Books " +
                  "SET Title = ?, AuthorID = ?, YearPublished = ? " +
                  "WHERE BookID = ?";
  
          try {
              PreparedStatement statement =
                      connection.prepareStatement(sql);
  
              statement.setString(1, title);
              statement.setInt(2, authorId);
              statement.setInt(3, yearPublished);
              statement.setInt(4, bookId);
  
              int rowsChanged =
                      statement.executeUpdate();
  
              statement.close();
  
              return rowsChanged > 0;
  
          } catch (SQLException e) {
              System.out.println(
                      "Error updating book: " + e.getMessage());
  
              return false;
          }
      }
  
  
      // DELETE: Removes a book
      public boolean deleteBook(int bookId) {
  
          String sql =
                  "DELETE FROM Books WHERE BookID = ?";
  
          try {
              PreparedStatement statement =
                      connection.prepareStatement(sql);
  
              statement.setInt(1, bookId);
  
              int rowsChanged =
                      statement.executeUpdate();
  
              statement.close();
  
              return rowsChanged > 0;
  
          } catch (SQLException e) {
              System.out.println(
                      "Error deleting book: " + e.getMessage());
  
              return false;
          }
      }
  
  
      // Closes the database connection when the program exits
      public void closeConnection() {
  
          try {
              if (connection != null &&
                      !connection.isClosed()) {
  
                  connection.close();
  
                  System.out.println(
                          "Database connection closed.");
              }
  
          } catch (SQLException e) {
              System.out.println(
                      "Error closing database: " + e.getMessage());
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
