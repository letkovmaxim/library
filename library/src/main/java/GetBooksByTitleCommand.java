import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;

public class GetBooksByTitleCommand implements Command {
    public String name() {
        return "GET_BOOK";
    }

    public void exec() {
        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader buffer = new BufferedReader(isr);
        String input;

        System.out.println(" [ Введите название книги ]");
        try {
            input = buffer.readLine();
            App.libGetBooksByTitle(input);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
