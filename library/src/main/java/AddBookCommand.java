import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class AddBookCommand implements Command {

    public String name() {
        return "ADD";
    }

    public void exec() {
        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader buffer = new BufferedReader(isr);
        String title;
        String input = "";
        ArrayList<String> authors = new ArrayList<>();

        System.out.println(" [ Введите название книги ] ");
        try {
            title = buffer.readLine();
            System.out.println(" [ Введите имена авторов книги (пустая строка - признак конца) ] ");
            do {
                try {
                    input = buffer.readLine();
                    if (!input.equals("")) {
                        authors.add(input);
                    }
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            } while (!input.equals(""));

            App.libAddBook(new Book(title, authors));

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
