import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * See the "[36]_Broken_Debugger.txt" file
 * for an example of file formatting expected
 * by this parser.
 */
public class Converter {
    private final HashMap<Character, String[]> charBusMap = new HashMap<>();
    private final String spaceBusList, modelsimPath = "modelsim/";

    private String filename;
    private ArrayList<String> busLists;

    private int numSubLines = 2;
    private final int height;

    private int numAddresses, addrWidthBin;
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

        loadFont();
        textToMemory(filename);
        createMemoryFiles();
        createVerilogFiles();
    }

    /**
     * Expects that the specified font exists.
     */
    private void loadFont() {
        final String path = "fonts/charBinaries_";
        String filename = path + height + "x" + width;

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

    /**
     * Initializes fields for converted bus strings,
     * and number of lines, width of memory, etc.
     * @param filename The file containing lyrics
     */
    private void textToMemory(String filename) {
        FileReader fr = null;
        try {
            fr = new FileReader(modelsimPath + filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        BufferedReader reader = new BufferedReader(fr);
        String line;

        ArrayList<StringBuilder> busLists = new ArrayList<>();
        int numLines = 0;
        try {
            numSubLines     = Integer.parseInt(reader.readLine().split("\\s+")[0]);
            charsPerSubLine = Integer.parseInt(reader.readLine().split("\\s+")[0]);

            for (int i = 0; i < numSubLines; i++) {
                busLists.add(new StringBuilder());
            } // Create a StringBuilder for each line

            while ((line = reader.readLine()) != null) {
                if (line.matches("//.*|>>>.*|\\s*")) continue;

                numLines++;
                String[] subLines = line.split("\\s*//\\s*");
                if (subLines.length < numSubLines) {
                    throw new RuntimeException("number of sub-lines too few @ " + line);
                } // Check formatting of line that enough sublines exist.

                for (int i = 0; i < numSubLines; i++) {
                    String bl = subLineToBusList(subLines[i]);
                    busLists.get(i).append(bl);
                }
            } // parse each line of sub-lines in the lyrics file
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.busLists = new ArrayList<>();
        for (StringBuilder busList : busLists) {
            this.busLists.add(busList.toString());
        }
        this.numLines = numLines;
        this.numAddresses = numLines * charsPerSubLine * width;
        this.addrWidthBin = Integer.highestOneBit(numLines - 1);
    }

    /**
     * Creates memory files for each sublist
     * of strings in bus notation.
     * File name format: "%s_sl%d.txt"
     */
    private void createMemoryFiles() {
        FileWriter fr;
        BufferedWriter writer = null;

        try {
            for (String bl : busLists) {
                String filename_tag = String.format("_sl%d.", busLists.indexOf(bl));
                String[] filename_halves = (modelsimPath + filename).split("\\.");
                fr = new FileWriter(String.join(filename_tag, filename_halves));
                writer = new BufferedWriter(fr);

                String[] charBusLists = bl.split("\n");
                for (String charBl : charBusLists) {
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

    private void createVerilogFiles() {
        final String define = "`define ";
        String instantiate = "    buslistROM #(\"";
        instantiate = instantiate.concat(filename + "_sl%d\") buslistROMx%<d(clk);");

        FileWriter fr;
        BufferedWriter writer = null;

        try {
            ArrayList<String> lines = new ArrayList<>();
            lines.add(define + "CHAR_H " + height);
            lines.add(define + "CHAR_W " + width);
            lines.add(define + "LINE_N " + numLines);
            lines.add(define + "CPSBLN " + charsPerSubLine);
            lines.add(define + "CHAR_W " + width);
            lines.add(define + "ADDR_W " + addrWidthBin);
            Files.write(Paths.get(modelsimPath + "definitions.vh"), lines);
            // Overwrite the definitions file.

            Path path = Paths.get(modelsimPath + "karaoke_format.txt");
            String karaokeFile = String.join("\n", Files.readAllLines(path));
            String[] fileHalves = karaokeFile.split("<subline_list>");
            ArrayList<String> slList = new ArrayList<>();
            for (int i = 0; i < numSubLines; i++) {
                slList.add("sl" + i);
            }
            karaokeFile = String.join(String.join(", ", slList), fileHalves);
            // Read the test module's format file and insert signal declaration names.

            fileHalves = karaokeFile.split("<subline_buslist_instantiations>");
            slList = new ArrayList<>();
            for (int i = 0; i < numSubLines; i++) {
                slList.add(String.format(instantiate, i));
            }
            karaokeFile = String.join(String.join("\n", slList), fileHalves);
            // insert buslistROM module instantiation statements.

            fr = new FileWriter(modelsimPath + "karaoke.v");
            writer = new BufferedWriter(fr);
            writer.write(karaokeFile);


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
        } // Fills the other end of the line with spaces

        for (Character c : lineBuilder.toString().toCharArray()) {
            if (charBusMap.containsKey(c)) {
                for (String bus : charBusMap.get(c)) {
                    busList.append(new StringBuilder(bus).reverse()).append(" ");
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
