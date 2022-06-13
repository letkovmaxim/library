import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;

public class RemoveAuthorCommand implements Command {
    public String name() {
        return "REMOVE_AUTHOR";
    }

    public void exec() {
        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader buffer = new BufferedReader(isr);
        String input;

        System.out.println(" [ Введите имя автора книги ]");
        try {
            input = buffer.readLine();
            App.libRemoveAuthor(input);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
