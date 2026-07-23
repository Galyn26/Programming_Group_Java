-- Create Database
CREATE DATABASE IF NOT EXISTS librarydb;
USE librarydb;

-- Create User Credentials (Required by Rubric)
CREATE USER IF NOT EXISTS 'scott'@'localhost' IDENTIFIED BY 'tiger';
GRANT ALL PRIVILEGES ON librarydb.* TO 'scott'@'localhost';
FLUSH PRIVILEGES;

-- Table Structures
CREATE TABLE IF NOT EXISTS Authors (
                                       AuthorID INT AUTO_INCREMENT PRIMARY KEY,
                                       Name VARCHAR(50) NOT NULL
    );

CREATE TABLE IF NOT EXISTS Books (
                                     BookID INT AUTO_INCREMENT PRIMARY KEY,
                                     Title VARCHAR(50) NOT NULL,
    AuthorID INT NOT NULL,
    YearPublished YEAR NOT NULL,
    FOREIGN KEY (AuthorID) REFERENCES Authors(AuthorID)
    );

-- Seed Data
INSERT INTO Authors (Name) VALUES ('George Orwell'), ('J.K. Rowling'), ('J.R.R. Tolkien');
INSERT INTO Books (Title, AuthorID, YearPublished) VALUES
                                                       ('1984', 1, 1949),
                                                       ('Harry Potter', 2, 1997),
                                                       ('The Hobbit', 3, 1937);