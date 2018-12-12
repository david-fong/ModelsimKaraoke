import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;

/**
 * Each letter is 7x6 pixels
 * counting the small separator
 */
public class MonospaceFont {
    private static final HashMap<Character, String[]> charBinaries;
    static {
        charBinaries = new HashMap<>();
        try {
            FileReader fileReader = new FileReader("charBinaries");
            BufferedReader reader = new BufferedReader(fileReader);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
