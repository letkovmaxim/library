import java.util.ArrayList;

public class Book {
    private String title;
    private ArrayList<String> authors;

    public Book(String title, ArrayList<String> authors) {
        this.title = title;
        this.authors = authors;
    }

    public Book(String title, String author) {
        this.title = title;
        authors.add(author);
    }

    public String getTitle() {
        return title;
    }

    public ArrayList<String> getAuthors() {
        return authors;
    }
}
