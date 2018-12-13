package ModelsimKaraoke;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;

/**
 * Each letter is 7x6 pixels
 * (counting the small separator)
 */
public class MonospaceFont {
    private final static String path = "fonts/charBinaries_";

    private final HashMap<Character, String[]> charBusMap;

    MonospaceFont(int height, int width) {
        String filename = path + height + "x" + width;
        charBusMap = new HashMap<>();

        try {
            FileReader fr = new FileReader(filename);
            BufferedReader reader = new BufferedReader(fr);
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.matches("\\s*//.*|\\s*")) continue;

                if (line.matches(String.format(".(\\s+[01]{%d}){%d}", height, width))) {
                    char c = line.charAt(0);
                    String[] binaries = line
                            .substring(1).trim()
                            .replaceAll(("0"), ("x"))
                            .replaceAll(("1"), ("0"))
                            .split("\\s+");
                    charBusMap.put(c, binaries);
                } else {
                    throw new RuntimeException(filename + "not formatted properly");
                }
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    HashMap<Character, String[]> getCharBusMap() {
        return charBusMap;
    }
}
