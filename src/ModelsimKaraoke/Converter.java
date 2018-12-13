package ModelsimKaraoke;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;


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

        int numLines = 0;
        while ((line = reader.readLine()) != null) {
            if (line.matches("//.*|>>>.*|\\s*")) continue;

            numLines++;
            String[] subLines = line.split("\\s*//\\s*");
            if (subLines.length != numSubLines) {
                throw new RuntimeException("number of sub-lines wrong @ " + line);
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
    private void createMemoryFiles() throws IOException {
        String format = "@%0" + addrWidthHex + "x ";

        for (String busList : busLists) {
            int busListNumber = busLists.indexOf(busList);
            FileWriter fr = new FileWriter(filename + "_sl" + busListNumber);
            BufferedWriter writer = new BufferedWriter(fr);

            int address = 0;
            String[] charBusLists = busList.split("\n");
            for (String charBusList : charBusLists) {
                for (String bus : charBusList.split(" ")) {
                    writer.write(String.format(format, address++) + bus);
                }
                writer.newLine();
            } // Write busses from the same character on one line
        }

        FileWriter fr = new FileWriter(filename + "_stop");
        BufferedWriter writer = new BufferedWriter(fr);

        for (int address = 0; address < numAddresses; address++) {
            if ((address + 1) % (charsPerSubLine * width) == 0) {
                writer.write(String.format(format, address) + 1);
            } else {
                writer.write(String.format(format, address) + 0);
            }
            if ((address + 1) % width == 0) {
                writer.newLine();
            }
        }
    }

    private String subLineToBusList(String line) {
        StringBuilder busList = new StringBuilder();

        line = line.trim();
        if (line.length() > charsPerSubLine) {
            line = line.substring(0, charsPerSubLine);
        }
        for (int i = 0; i < (charsPerSubLine - line.length()) / 2; i++) {
            busList.append(spaceBusList);
        } // Center the line

        for (Character c : line.toCharArray()) {
            if (charBusMap.containsKey(c)) {
                for (String bus : charBusMap.get(c)) {
                    busList.append(bus).append(" ");
                } // Convert one character to a bus initializer
                busList.append("\n");
            } else {
                busList.append(spaceBusList);
            } // If the character was not in the font use blank.
        } // Convert all characters in the line to bus initializers.

        while (busList.length() / (height + 1) < charsPerSubLine * width) {
            busList.append(spaceBusList);
        } // Fill the right half of the line with spaces

        return busList.toString();
    }

    public static void main(String[] args) {
        Converter converter = new Converter("[36] Broken Debugger.txt", 9, 6);
    }
}
