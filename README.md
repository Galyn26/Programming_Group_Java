# 🚀 Group Project Git Cheat Sheet


### 1️⃣ First Time Only (Get the code onto your computer)

Open your terminal or command prompt and run:

```bash

git clone https://github.com/Galyn26/Programming_Group_Java

```


### 🛢️ 2️⃣ Database Setup (Run Once After Cloning)

To load the required database schema, tables, and sample data into your local MySQL server, open your terminal inside the project folder and run:
```bash 

mysql -u root -p < sql/schema.sql
 ```

(Enter your local MySQL root password when prompted)

Every Time Before You Start Coding💡 Always grab the latest changes so you don't work on outdated code!

```bash

git pull

```

3️⃣ When You Finish Writing Your CodeSave all changes in IntelliJ.Run these commands in your terminal:
```bash
git add .
```

```bash

git commit -m "Updated my assigned section"

git push

```

⚠️ The Golden Rule

Always run `git pull` BEFORE you start typing, and only edit your assigned inner class section inside LibraryBookManager.java!   