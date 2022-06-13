import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class GetBooksByAuthorCommand implements Command {
    public String name() {
        return "GET_BOOK_BY_AUTHOR";
    }

    public void exec() {
        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader buffer = new BufferedReader(isr);
        String input;

        System.out.println(" [ Введите имя автора ]");
        try {
            input = buffer.readLine();
            App.libGetBooksByAuthor(input);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
