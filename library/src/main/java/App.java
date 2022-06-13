import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.io.*;

import Exception.*;

public class App {
    private static Library lib = new Library();

    public static void libAddBook(Book book) {
        try {
            lib.addBook(book);
            System.out.println(" [ Книга добавлена ]");
        } catch (SQLException | SuchBookAlreadyExistsException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void libGetBooksByTitle(String title) {
        try {
            ArrayList<Book> books = lib.getBooksByTitle(title);
            System.out.println(" [ Найденные книги ]");
            for (Book book : books) {
                System.out.println("   Название: " + book.getTitle());
                System.out.print("   Авторы: ");
                for (String author : book.getAuthors()) {
                    System.out.print(author + "   ");
                }
                System.out.println();
            }
        } catch (SQLException | NoSuchBookException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void libGetBooksByAuthor(String author) {
        try {
            ArrayList<Book> books = lib.getBooksByAuthor(author);
            System.out.println(" [ Найденные книги ]");
            for (Book book : books) {
                System.out.println("  Название: " + book.getTitle());
                System.out.print("  Авторы: ");
                for (String authors : book.getAuthors()) {
                    System.out.print(authors + "   ");
                }
                System.out.println();
            }
        } catch (SQLException | NoSuchBookException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void libRemoveBook(String title) {
        try {
            lib.removeBook(title);
            System.out.println(" [ Книга удалена ]");
        } catch (SQLException | NoSuchBookException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void libRemoveAuthor(String author) {
        try {
            lib.removeAuthor(author);
            System.out.println(" [ Автор удален ]");
        } catch (SQLException | NoSuchAuthorException e) {
            System.out.println(e.getMessage());
        }
    }


    public static void main(String[] args) {
        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader buffer = new BufferedReader(isr);
        Command[] cmds = {new AddBookCommand(), new GetBooksByTitleCommand(), new GetBooksByAuthorCommand(), new RemoveBookCommand(), new RemoveAuthorCommand(), new ExitCommand()};

        String input;

        System.out.println("\n [ Команды: ADD, GET_BOOK, GET_BOOK_BY_AUTHOR, REMOVE_BOOK, REMOVE_AUTHOR, EXIT ] ");
        System.out.println(" [ Введите команду ] ");

        do {
            // Ввод команды
            try {
                input = buffer.readLine();
            } catch (IOException e) {
                continue;
            }
            input = input.strip().toUpperCase();

            // Поиск команды в cmds
            for (int i = 0 ; i < cmds.length ; i++) {

                if (cmds[i].name().equals(input)) {
                    cmds[i].exec();
                    break;
                }

                // Конец массива, break не сработал - неизвестная команда
                if (i == cmds.length - 1) {
                    System.out.println(" [ Неизвестная команда ] ");
                }
            }
        } while (true);
    }
}
