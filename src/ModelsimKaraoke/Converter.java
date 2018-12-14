package ModelsimKaraoke;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * See the "[36]_Broken_Debugger.txt" file
 * for an example of file formatting expected
 * by this parser.
 */
public class Converter {
    private final HashMap<Character, String[]> charBusMap;
    private final String spaceBusList;

    private String filename;
    private ArrayList<String> busLists;

    private int numSubLines = 2;
    private final int height;

    private int numAddresses, addrWidthHex, addrWidthBin;
    private int numLines;
    private int charsPerSubLine = 24;
    private final int width;

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
        this.filename = filename;
        this.height = height;
        this.width = width;

        StringBuilder spaceBuilder = new StringBuilder();
        for (int i = 0; i < height; i++) spaceBuilder.append("x");
        String space = spaceBuilder.append(" ").toString();
        spaceBuilder = new StringBuilder();
        for (int i = 0; i < width; i++)  spaceBuilder.append(space);
        this.spaceBusList = spaceBuilder.append("\n").toString();
        // Initialize the "space/substitute-unrecognizable" character.

        charBusMap = new MonospaceFont(height, width).getCharBusMap();

        try {
            textToMemory(filename);
            createMemoryFiles();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initializes fields for converted bus strings,
     * and number of lines, width of memory, etc.
     * @param filename The file containing lyrics
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

        int numLines = 0;
        while ((line = reader.readLine()) != null) {
            if (line.matches("//.*|>>>.*|\\s*")) continue;

            numLines++;
            String[] subLines = line.split("\\s*//\\s*");
            if (subLines.length < numSubLines) {
                throw new RuntimeException("number of sub-lines too few @ " + line);
            } // Check formatting of line

            for (int i = 0; i < numSubLines; i++) {
                String bl = subLineToBusList(subLines[i]);
                busLists.get(i).append(bl);
            }
        } // parse each line of sub-lines in the lyrics file

        this.busLists = new ArrayList<>();
        for (StringBuilder busList : busLists) {
            this.busLists.add(busList.toString());
        }
        this.numLines = numLines;
        this.numAddresses = numLines * charsPerSubLine * width;
        int addressWidthHex = 0, i = 1;
        while (i < numAddresses) {
            addressWidthHex++;
            i *= 16;
        }
        this.addrWidthHex = addressWidthHex;
        this.addrWidthBin = Integer.highestOneBit(numLines - 1);

        // return busLists.get(0).toString().split("\n").length * width;
    }

    /**
     * Creates memory files for each sublist
     * of strings in bus notation.
     * @throws IOException
     */
    private void createMemoryFiles() {
        //String format = "@%0" + addrWidthHex + "x ";
        FileWriter fr;
        BufferedWriter writer = null;

        try {
            for (String bl : busLists) {
                String filename_tag = String.format("_sl%d.", busLists.indexOf(bl));
                fr = new FileWriter(String.join(filename_tag, filename.split("\\.")));
                writer = new BufferedWriter(fr);

                //int address = 0;
                String[] charBusLists = bl.split("\n");
                for (String charBl : charBusLists) {
                    //for (String bus : charBl.split(" ")) {
                    //    //writer.write(String.format(format, address++) + bus);
                    //    writer.write(bus);
                    //}
                    writer.write(charBl.trim());
                    writer.newLine();
                } // Write busses from the same character on one line
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param line a string of characters
     * @return A string of signal bus values
     * representing columns of letters,
     * delimited by spaces. Groups of columns
     * corresponding to one letter are
     * delimited by new-line characters.
     */
    private String subLineToBusList(String line) {
        StringBuilder busList = new StringBuilder();
        StringBuilder lineBuilder = new StringBuilder();

        line = line.trim();
        if (line.length() > charsPerSubLine) {
            line = line.substring(0, charsPerSubLine);
        }
        for (int i = 0; i < (charsPerSubLine - line.length()) / 2; i++) {
            lineBuilder.append(" ");
        } // Center the line
        lineBuilder.append(line);
        while (lineBuilder.length() < charsPerSubLine) {
            lineBuilder.append(" ");
        }

        for (Character c : lineBuilder.toString().toCharArray()) {
            if (charBusMap.containsKey(c)) {
                for (String bus : charBusMap.get(c)) {
                    busList.append(bus).append(" ");
                } // Convert one character to a bus initializer
                busList.append("\n");
            } else {
                busList.append(spaceBusList);
            } // If the character was not in the font use blank.
        } // Convert all characters in the line to bus initializers.

        return busList.toString();
    }

    public static void main(String[] args) {
        Converter c;
        if (String.join(" ", args).matches("[^.]+\\.txt \\d+ \\d+")) {
            String filename = args[0];
            int height = Integer.parseInt(args[1]), width = Integer.parseInt(args[2]);
            c = new Converter(filename, height, width);
        } else {
            System.out.println("no valid arguments detected. running example program...");
            c = new Converter("[36]_Broken_Debugger.txt", 9, 6);
        }
    }
}
