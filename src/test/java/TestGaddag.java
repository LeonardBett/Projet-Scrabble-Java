import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Scanner;

import fr.u_bordeaux.scrabble.model.dictionary.GADDAG;

public class TestGaddag {

    public static void main(String[] args) {
        GADDAG gaddag = new GADDAG();
        String lexiconPath = "/dictionaries/lexicon_en.txt";

        System.out.println("Loading the dictionary");
        try (InputStream is = TestGaddag.class.getResourceAsStream(lexiconPath)) {
            if (is == null) throw new Exception("File not find : " + lexiconPath);

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null) {
                gaddag.add(line.trim().toUpperCase());
            }
            System.out.println("Dictionary loaded");
        } catch (Exception e) {
            System.err.println("Error : " + e.getMessage());
            return;
        }

        Scanner scan = new Scanner(System.in);
        char hook = ' ';

        // Pour word = "apple"
        System.out.println("Is APPLE valid ? " + gaddag.containsWord("APPLE")); //true
        System.out.println("Is ZYZZYVA valid ? " + gaddag.containsWord("ZYZZYVA")); //false

        String[] wordsToTest = {"APPLE", "ZYZZYVA", "SCRABBLE", "ZURICH"}; // ZURICH is the last word

        System.out.println("\n--- BENCHMARK RESEARCH DICTIONARY ---");

        for (String word : wordsToTest) {
            // 1. Start Chrono (en nanosecondes)
            long startTime = System.nanoTime();

            // 2. Search
            boolean exists = gaddag.containsWord(word);

            // 3. End Chrono
            long endTime = System.nanoTime();

            // 4. Calcul time
            long duration = endTime - startTime;

            // Conversion (1 µs = 1000 ns)
            double durationInMicro = duration / 1000.0;

            System.out.printf("Word : %-10s | Exist: %-5b | Time to find : %.2f µs%n",
                    word, exists, durationInMicro);
        }

        System.out.println("\n--- MODE REPL (GADDAG RESULT) ---");

        System.out.println("\n--- TEST GADDAG ---");
        System.out.println("Commands :");
        System.out.println("  h <lettre> :  Define the hook (ex: h C)");
        System.out.println("  r <lettres> : Search word with this rack (ex: r ATSON)");
        System.out.println("  q : Quit");

        while (true) {
            System.out.print("\n(Hook: '" + hook + "') > ");
            if (!scan.hasNext()) break;
            String cmd = scan.next().toLowerCase();

            if (cmd.equals("q")) break;

            if (cmd.equals("h")) {
                hook = scan.next().toUpperCase().charAt(0);
            }
            else if (cmd.equals("r")) {
                String rackStr = scan.next().toUpperCase();

                Character[] rack = new Character[rackStr.length()];
                for (int i = 0; i < rackStr.length(); i++) rack[i] = rackStr.charAt(i);

                HashSet<GADDAG.GaddagResult> results = gaddag.findWordsWithRackAndHook(rack, hook);

                if (results == null || results.isEmpty()) {
                    System.out.println("No result");
                } else {
                    System.out.println(results.size() + " results found :");

                    for (GADDAG.GaddagResult res : results) {
                        System.out.println(" - " + res.toString());
                    }
                }
            }
        }
        scan.close();
    }
}
