package ModelsimKaraoke;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;


public class Converter {
    private final HashMap<Character, int[]> charBinaries;
    private ArrayList<ArrayList<String>> lyrics;

    private Converter(String filename) {
        charBinaries = new MonospaceFont().getCharBinaries();

        try {
            FileReader fr = new FileReader(filename);
            BufferedReader reader = new BufferedReader(fr);

            int linesPerScreen; // TODO: make this the first line in filename

            // TODO: intialize lyrics


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
}
