package ModelsimKaraoke;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;

/**
 * Each letter is 7x6 pixels
 * (counting the small separator)
 */
public class MonospaceFont {
    private final HashMap<Character, int[]> charBinaries;

    MonospaceFont() {
        charBinaries = new HashMap<>();
        Character c = null;
        int[] binaries = new int[6];
        int i = 0;

        try {
            FileReader fr = new FileReader("ModelsimKaraoke/charBinaries[7x6]");
            BufferedReader reader = new BufferedReader(fr);
            String line;

            while (!(line = reader.readLine()).equals("<EOF>")) {
                if (line.matches("//.*")) continue;

                if (line.matches(".")) {
                    if (c != null) {
                        charBinaries.put(c, binaries);
                        binaries = new int[6];
                    }
                    i = 0;
                    c = line.charAt(0);
                } else {
                    // a binary number
                    binaries[i++] = Integer.parseInt(line, (0), (8), (2));
                }
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    HashMap<Character, int[]> getCharBinaries() {
        // TODO: some unmodifiable wrapper?
        return charBinaries;
    }
}
