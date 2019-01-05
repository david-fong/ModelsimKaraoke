import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Parses a lightly formatted text file containing
 * lyrics and produces Verilog files to display the
 * lyrics in Modelsim Altera's waveform viewer.
 *
 * See the "[36]_Broken_Debugger.txt" file
 * for an example of lyrics file formatting
 * expected by this parser.
 */
public class Converter {
    private final HashMap<Character, String[]> charBusMap = new HashMap<>();
    private final String spaceBusList, msPath = "modelsim/";

    private String filename;
    private ArrayList<String> busLists;

    private final int height;
    private final int width;
    private int numLines;
    private int charsPerSubLine;
    private int addrWidthBin;
    private int numSubLines;

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
        createVerilogDefinitions();
        createVerilogTestModule();
    }

    /**
     * Font file-names follow "charBinaries_%dx%d"
     * Expects that the specified font exists.
     * Parses the font into a Hash-map.
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
            System.out.println("error reading from specified font file.");
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
            fr = new FileReader(msPath + filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        assert fr != null;
        BufferedReader reader = new BufferedReader(fr);
        String line;

        ArrayList<StringBuilder> busLists = new ArrayList<>();
        int numLines = 0;
        try {
            numSubLines     = Integer.parseInt(reader.readLine().split("\\s+")[0]);
            charsPerSubLine = Integer.parseInt(reader.readLine().split("\\s+")[0]);

            for (int i = 0; i < numSubLines; i++) {
                busLists.add(new StringBuilder());
            } // Create a StringBuilder for each sub-line

            // Read all lines of sub-lines
            while ((line = reader.readLine()) != null) {
                if (line.matches("//.*|>>>.*|\\s*")) continue;

                numLines++;
                // Split the line into sublines.
                String[] subLines = line.split("\\s*//\\s*");
                if (subLines.length < numSubLines) {
                    throw new RuntimeException("number of sub-lines too few @ " + line);
                } // Check formatting of line that enough sublines exist.

                for (int i = 0; i < numSubLines; i++) {
                    // Convert the subline to signal bus format.
                    String bl = subLineToBusList(subLines[i]);
                    // Group it with its subline stringBuilder.
                    busLists.get(i).append(bl);
                }
            }
        } catch (IOException e) {
            System.out.println("error reading a line from the lyrics file.");
            e.printStackTrace();
        }

        // save the bus-list data to an object-field to write to memory files.
        this.busLists = new ArrayList<>();
        for (StringBuilder busList : busLists) {
            this.busLists.add(busList.toString());
        }
        // get some data used later in Verilog `define statements.
        this.numLines = numLines;
        this.addrWidthBin = Integer.highestOneBit(numLines - 1);
    }

    /**
     * Creates memory files for each sublist
     * of strings in bus notation.
     * File name format: "%s_sl%d.txt"
     */
    private void createMemoryFiles() {
        // Go through each sub-line.
        for (String bl : busLists) {
            String filename = String.format("_sl%d.", busLists.indexOf(bl));
            String[] filename_halves = (msPath + this.filename).split("\\.");
            // make memory-file name the lyrics filename tagged with "_sl#".
            filename = String.join(filename, filename_halves);

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
                // Split into groups of characters.
                String[] charBusLists = bl.split("\n");
                for (String charBl : charBusLists) {
                    writer.write(charBl.trim());
                    writer.newLine();
                } // Write busses from the same character on one line
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Create a .vh file of define statements
     * used by the karaoke.v and buslistROM.v
     * files.
     */
    private void createVerilogDefinitions() {
        final String define = "`define ";

        // Overwrite the definitions file.
        ArrayList<String> lines = new ArrayList<>();
        lines.add(define + "CHAR_H " + height);
        lines.add(define + "CHAR_W " + width);
        lines.add(define + "LINE_N " + numLines);
        lines.add(define + "CPSBLN " + charsPerSubLine);
        lines.add(define + "CHAR_W " + width);
        lines.add(define + "ADDR_W " + addrWidthBin);
        try {
            Files.write(Paths.get(msPath + "definitions.vh"), lines);
        } catch (IOException e) {
            System.out.println("unable to write to the definitions file.");
            e.printStackTrace();
        }
    }

    /**
     * Produces a file named karaoke.v with
     * signal declarations for each subline,
     * and module instantiations of the
     * buslistROM module for each signal.
     */
    private void createVerilogTestModule() {
        // Read the format file for the test module.
        Path path = Paths.get(msPath + "karaoke_format.txt");
        String karaokeFile = null;
        try {
            karaokeFile = String.join("\n", Files.readAllLines(path));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Insert signal declaration names.
        String signalFormat = "sl%d";
        assert karaokeFile != null;
        String[] fileHalves = karaokeFile.split("<subline_signals>");
        ArrayList<String> slList = new ArrayList<>();
        for (int i = 0; i < numSubLines; i++) {
            slList.add(String.format(signalFormat, i));
        }
        karaokeFile = String.join(String.join(", ", slList), fileHalves);

        // Insert buslistROM module instantiation statements.
        String instantiateFormat = "    buslistROM #(\"" +
                String.join("_sl%d.", this.filename.split("\\.")) +
                "\") blROMx%<d(clk, sl%<d);";
        fileHalves = karaokeFile.split("<buslistROM_instantiations>");
        slList = new ArrayList<>();
        for (int i = 0; i < numSubLines; i++) {
            slList.add(String.format(instantiateFormat, i));
        }
        karaokeFile = String.join(String.join("\n", slList), fileHalves);

        // Write the result to the output file.
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(msPath + "karaoke.v"))) {
            writer.write(karaokeFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param line a string of characters
     * @return A string of signal bus values
     *         representing columns of letters,
     *         delimited by spaces. Groups of columns
     *         corresponding to one letter are
     *         delimited by new-line characters.
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

        busList.append(spaceBusList);
        // Add a space so full lines look separate.

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

    /**
     * Runs the Converter class on a
     * specified text file and font.
     * @param args The first word is the filename
     *             with the .txt extension. The second
     *             is the character height in pixels,
     *             and the third is the character width
     *             also in pixels.
     */
    public static void main(String[] args) {
        if (String.join(" ", args).matches("[^.]+\\.txt \\d+ \\d+")) {
            String filename = args[0];
            int height = Integer.parseInt(args[1]), width = Integer.parseInt(args[2]);
            new Converter(filename, height, width);
        } else {
            System.out.println("no valid arguments detected. running example program...");
            new Converter("[36]_Broken_Debugger.txt", 9, 6);
        }
    }
}
