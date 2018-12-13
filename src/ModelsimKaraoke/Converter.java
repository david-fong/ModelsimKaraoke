package ModelsimKaraoke;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class Converter {
    private final HashMap<Character, String[]> charBusMap;
    private ArrayList<String> busLists;
    private final int height, width;
    private int numSubLines, charsPerSubLine;
    private final String spaceBusList;

    /**
     * Creates a .txt file representing
     * memory initialization data for a
     * hardware module in Verilog.
     *
     * @param filename a text file to read
     * @param height in pixels of font of choice
     * @param width in pixels of font of choice
     */
    private Converter(String filename, int height, int width) {
        this.height = height;
        this.width = width;

        StringBuilder spaceBuilder = new StringBuilder();
        for (int i = 0; i < height; i++) {
            spaceBuilder.append("x");
        }
        String space = spaceBuilder.append("\n").toString();
        for (int i = 0; i < width - 1; i++) {
            spaceBuilder.append(space);
        }
        this.spaceBusList = spaceBuilder.toString();

        charBusMap = new MonospaceFont(height, width).getCharBusMap();
        int addressability = textToMemory(filename);
    }

    /**
     *
     * @param filename the file containing lyrics
     * @return number of memory addresses needed
     */
    private void textToMemory(String filename) throws IOException {
        FileReader fr = new FileReader(filename);
        BufferedReader reader = new BufferedReader(fr);
        String line;

        numSubLines     = Integer.parseInt(reader.readLine().split("\\s+")[0]);
        charsPerSubLine = Integer.parseInt(reader.readLine().split("\\s+")[0]);

        ArrayList<StringBuilder> busLists = new ArrayList<>();
        for (int i = 0; i < numSubLines; i++) {
            busLists.add(new StringBuilder());
        } // Create a StringBuilder for each line

        while ((line = reader.readLine()) != null) {
            if (line.matches("//.*|>>>.*|")) continue;

            String[] subLines = line.split("\\s*//\\s*");
            if (subLines.length != numSubLines) {
                throw new RuntimeException("number of sub-lines wrong @ " + line);
            } // Check formatting of line

            for (int i = 0; i < numSubLines; i++) {
                String bl = lineToBusList(subLines[i]);
                busLists.get(i).append(bl);
            }
        } // parse each line of sub-lines in the lyrics file

        // return busLists.get(0).toString().split("\n").length * width;
    }

    private String lineToBusList(String line) {
        StringBuilder busList = new StringBuilder();

        line = line.trim().substring(0, charsPerSubLine);
        for (int i = 0; i < (charsPerSubLine - line.length()) / 2; i++) {
            busList.append(spaceBusList);
        } // Center the line

        for (Character c : line.toCharArray()) {
            if (charBusMap.containsKey(c)) {
                for (String bus : charBusMap.get(c)) {
                    busList.append(bus.concat("\n"));
                } // Convert one character to a bus initializer
            } else {
                busList.append(spaceBusList);
            } // If the character was not in the font use blank.
        } // Convert all characters in the line to bus initializers.

        while (busList.length() / (height + 1) < charsPerSubLine * width) {
            busList.append(spaceBusList);
        } // Fill the right half of the line with spaces

        return busList.toString();
    }
}
