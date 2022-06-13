import java.sql.*;
import java.util.ArrayList;
import Exception.*;

public class Library {
    private Connection connection;
    private Statement statement;

    public Library() {
        try {
            this.connection = DriverManager.getConnection("jdbc:h2:~/library");
            this.statement = connection.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS books_titles (id IDENTITY NOT NULL PRIMARY KEY, title VARCHAR(50) NOT NULL);");
            statement.execute("CREATE TABLE IF NOT EXISTS books_authors (id IDENTITY NOT NULL PRIMARY KEY, author VARCHAR(50) NOT NULL);");
            statement.execute("CREATE TABLE IF NOT EXISTS titles_authors (title_id INT NOT NULL, author_id INT NOT NULL);");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void addBook(Book book) throws SQLException {
        ResultSet resultSet;
        PreparedStatement preparedStatement;
        int idTitle = -1;
        int authorId;
        ArrayList<Integer> idsAuthors = new ArrayList<>();

        //Проверяем есть ли уже такая книга (название)
        if (!containsBook(book)) { //Если нет, то

            //Вставляем название
            preparedStatement = connection.prepareStatement("INSERT INTO books_titles(title) VALUES(?);", Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, book.getTitle());
            preparedStatement.executeUpdate();

            //Получаем айди вставленного названия
            resultSet = preparedStatement.getGeneratedKeys();
            resultSet.next();
            idTitle = resultSet.getInt(1);

            //Вставляем авторов
            for (String author : book.getAuthors()) {
                //Если автор уже есть
                authorId = getAuthorId(author);
                if (authorId != -1) {
                    idsAuthors.add(authorId);
                } else { //Если автор новый
                    preparedStatement = connection.prepareStatement("INSERT INTO books_authors(author) VALUES(?);", Statement.RETURN_GENERATED_KEYS);
                    preparedStatement.setString(1, author);
                    preparedStatement.addBatch();
                    preparedStatement.executeBatch();

                    //Получаем айди нового автора
                    resultSet = preparedStatement.getGeneratedKeys();
                    resultSet.next();
                    idsAuthors.add(resultSet.getInt(1));
                }
            }

            //Создаём зависимости
            preparedStatement = connection.prepareStatement("INSERT INTO titles_authors(title_id, author_id) VALUES(?, ?)");
            for (int idAuthor : idsAuthors) {
                preparedStatement.setInt(1, idTitle);
                preparedStatement.setInt(2, idAuthor);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();

        } else {
            throw new SuchBookAlreadyExistsException("Такая книга уже есть");
        }
    }

    public ArrayList<Book> getBooksByTitle(String title) throws SQLException {
        ResultSet resultSet;
        ArrayList<Integer> idsAuthors = new ArrayList<>();
        ArrayList<String> authors = new ArrayList<>();
        ArrayList<Integer> idsTitles = new ArrayList<>();
        ArrayList<Book> books = new ArrayList<>();

        //Ищем айди книг -> ищем айди авторов -> ищем авторов
        resultSet = statement.executeQuery("SELECT * FROM books_titles;");
        while (resultSet.next()) {
            if (resultSet.getString("title").equals(title)) {
                idsTitles.add(resultSet.getInt("id"));
            }
        }

        //Если нашлась книга, то ищем авторов
        if (!idsTitles.isEmpty()) {
            for (int idTitle : idsTitles) {
                idsAuthors = new ArrayList<>();
                authors = new ArrayList<>();

                //Ищем айди авторов
                resultSet = statement.executeQuery("SELECT * FROM titles_authors;");
                while (resultSet.next()) {
                    if (resultSet.getInt("title_id") == idTitle) {
                        idsAuthors.add(resultSet.getInt("author_id"));
                    }
                }

                //Ищем авторов
                resultSet = statement.executeQuery("SELECT * FROM books_authors;");
                while (resultSet.next()) {
                    if (idsAuthors.contains(resultSet.getInt("id"))) {
                        authors.add(resultSet.getString("author"));
                    }
                }

                books.add(new Book(title, authors));
            }
        } else {
            throw new NoSuchBookException("Книги с таким названием нет");
        }

        return books;
    }

    public ArrayList<Book> getBooksByAuthor(String author) throws SQLException {
        ResultSet resultSet;
        int idAuthor = -1;
        String title = "";
        ArrayList<Integer> idsTitles = new ArrayList<>();
        ArrayList<Integer> idsAuthors;
        ArrayList<String> authors;
        ArrayList<Book> books = new ArrayList<>();

        resultSet = statement.executeQuery("SELECT * FROM books_authors;");
        while (resultSet.next()) {
            if (resultSet.getString("author").equals(author)) {
                idAuthor = resultSet.getInt("id");
                break;
            }
        }

        if (idAuthor != -1) {
            //Находим айди книг
            resultSet = statement.executeQuery("SELECT * FROM titles_authors;");
            while (resultSet.next()) {
                if (resultSet.getInt("author_id") == idAuthor) {
                    idsTitles.add(resultSet.getInt("title_id"));
                }
            }

            //Находим названия и авторов
            for (int idTitle : idsTitles) {
                idsAuthors = new ArrayList<>();
                authors = new ArrayList<>();

                //Находим название
                resultSet = statement.executeQuery("SELECT * FROM books_titles;");
                while (resultSet.next()) {
                   if (resultSet.getInt("id") == idTitle) {
                       title = resultSet.getString("title");
                       break;
                   }
                }

                //Находим айди остальных авторов
                resultSet = statement.executeQuery("SELECT * FROM titles_authors;");
                while (resultSet.next()) {
                    if (resultSet.getInt("title_id") == idTitle) {
                       idsAuthors.add(resultSet.getInt("author_id"));
                    }
                }

                //Находим авторов
                resultSet = statement.executeQuery("SELECT * FROM books_authors;");
                while (resultSet.next()) {
                    for (int id : idsAuthors) {
                        if (resultSet.getInt("id") == id) {
                           authors.add(resultSet.getString("author"));
                        }
                    }
                }

                books.add(new Book(title, authors));
           }
        } else {
            throw new NoSuchBookException("Книги с таким автором нет");
        }

        return books;
    }

    public void removeBook(String title) throws SQLException {
        ResultSet resultSet;
        PreparedStatement preparedStatement;
        ArrayList<Integer> titleIds = new ArrayList<>();

        //Собираем айди книги
        resultSet = statement.executeQuery("SELECT * FROM books_titles");
        while (resultSet.next()) {
            if (resultSet.getString("title").equals(title)) {
                titleIds.add(resultSet.getInt("id"));
            }
        }

        if (!titleIds.isEmpty()) {
            //Удаляем связи c полученными айди
            resultSet = statement.executeQuery("SELECT * FROM titles_authors");
            preparedStatement = connection.prepareStatement("DELETE FROM titles_authors WHERE title_id = ?;");
            while (resultSet.next()) {
                for (int titleId : titleIds) {
                    if (resultSet.getInt("title_id") == titleId) {
                        preparedStatement.setInt(1, titleId);
                        preparedStatement.addBatch();
                    }
                }
            }
            preparedStatement.executeBatch();

            updateLib();
        } else {
            throw new NoSuchBookException("Такой книги нет");
        }
    }

    public void removeAuthor(String author) throws SQLException {
        ResultSet resultSet;
        PreparedStatement preparedStatement;
        int idAuthor = -1;

        //Собираем айди автора
        resultSet = statement.executeQuery("SELECT * FROM books_authors");
        while (resultSet.next()) {
            if (resultSet.getString("author").equals(author)) {
                idAuthor = resultSet.getInt("id");
                break;
            }
        }

        if (idAuthor != -1) {
            //Удаляем связи с полученными айди
            resultSet = statement.executeQuery("SELECT * FROM titles_authors");
            preparedStatement = connection.prepareStatement("DELETE FROM titles_authors WHERE author_id = ?;");
            while (resultSet.next()) {
                if (resultSet.getInt("author_id") == idAuthor) {
                    preparedStatement.setInt(1, resultSet.getInt("author_id"));
                    preparedStatement.addBatch();
                }
            }
            preparedStatement.executeBatch();

            updateLib();
        } else {
            throw new NoSuchAuthorException("Такого автора нет");
        }
    }

    public void updateLib() throws SQLException {
        ResultSet resultSet;
        PreparedStatement preparedStatement;
        ArrayList<Integer> titlesIds = new ArrayList<>();
        ArrayList<Integer> authorsIds = new ArrayList<>();

        //Айди всех книг и авторов со связями
        resultSet = statement.executeQuery("SELECT * FROM titles_authors");
        while (resultSet.next()) {
            titlesIds.add(resultSet.getInt("title_id"));
            authorsIds.add(resultSet.getInt("author_id"));
        }

        //Удаляем книги без связей
        resultSet = statement.executeQuery("SELECT * FROM books_titles");
        preparedStatement = connection.prepareStatement("DELETE FROM books_titles WHERE id = ?;");
        while (resultSet.next()) {
            if (!titlesIds.contains(resultSet.getInt("id"))) {
                preparedStatement.setInt(1, resultSet.getInt("id"));
                preparedStatement.addBatch();
            }
        }
        preparedStatement.executeBatch();

        //Удаляем авторов без связей
        resultSet = statement.executeQuery("SELECT * FROM books_authors");
        preparedStatement = connection.prepareStatement("DELETE FROM books_authors WHERE id = ?;");
        while (resultSet.next()) {
            if (!authorsIds.contains(resultSet.getInt("id"))) {
                preparedStatement.setInt(1, resultSet.getInt("id"));
                preparedStatement.addBatch();
            }
        }
        preparedStatement.executeBatch();
    }

    public boolean containsBook(Book book) throws SQLException {
        ResultSet resultSet;
        ArrayList<Integer> authorsIds = new ArrayList<>();
        ArrayList<String> authors = new ArrayList<>();
        int titleId = -1;
        boolean sameTitle = false;
        boolean sameAuthors = false;

        //Есть такая книга
        resultSet = statement.executeQuery("SELECT * FROM books_titles;");
        while (resultSet.next()) {
            if (resultSet.getString("title").equals(book.getTitle())) {
                sameTitle = true;
                titleId = resultSet.getInt("id");
                break;
            }
        }

        if (sameTitle) {
            //Авторы совпадают
            resultSet = statement.executeQuery("SELECT * FROM titles_authors;");
            while (resultSet.next()) {
                if (resultSet.getInt("title_id") == titleId) {
                    authorsIds.add(resultSet.getInt("author_id"));
                }
            }

            resultSet = statement.executeQuery("SELECT * FROM books_authors;");
            while (resultSet.next()) {
                if (authorsIds.contains(resultSet.getInt("id"))) {
                    authors.add(resultSet.getString("author"));
                }
            }

            sameAuthors = authors.equals(book.getAuthors());
        }

        return sameTitle && sameAuthors;
    }

    public int getAuthorId(String author) throws SQLException {
        ResultSet resultSet;
        int id = -1;

        resultSet = statement.executeQuery("SELECT * FROM books_authors");
        while (resultSet.next()) {
            if (resultSet.getString("author").equals(author)) {
                id = resultSet.getInt("id");
                break;
            }
        }
        return id;
    }
}