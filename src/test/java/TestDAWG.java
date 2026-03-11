import fr.u_bordeaux.scrabble.model.dictionary.DAWG;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class TestDAWG {

    public static void main(String[] args) {
        DAWG dawg = new DAWG();
        String lexiconPath = "/dictionaries/lexicon_en.txt";
        List<String> words = new ArrayList<>();

        System.out.println("--- Start loading ---");
        long loadStartTime = System.currentTimeMillis();

        try (InputStream is = TestDAWG.class.getResourceAsStream(lexiconPath)) {
            if (is == null) throw new Exception("File not found : " + lexiconPath);

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null) {
                words.add(line.trim().toUpperCase());
            }

            long sortStartTime = System.currentTimeMillis();
            Collections.sort(words);
            long sortEndTime = System.currentTimeMillis();

            for (String word : words) {
                dawg.add(word);
            }
            dawg.finish();

            long loadEndTime = System.currentTimeMillis();
            System.out.println("GADDAG loaded");
            System.out.println("Word count      : " + words.size());
            System.out.println("Time to load : " + (loadEndTime - loadStartTime) + " ms");
            System.out.println("-------------------------------------------");

        } catch (Exception e) {
            System.err.println("ERROR : " + e.getMessage());
            return;
        }

        System.out.println("\n--- BENCHMARK (CONTAINS) ---");
        String[] wordsToTest = {"APPLE", "ZYZZYVA", "SCRABBLE", "JAVA", "INEXISTANT"};
        for (String word : wordsToTest) {
            long startTime = System.nanoTime();
            boolean exists = dawg.contains(word);
            long endTime = System.nanoTime();

            double duration = (endTime - startTime) / 1000.0;
            System.out.printf("Word : %-10s | Real : %-5b | Time : %.2f µs%n",
                    word, exists, duration);
        }

        Scanner scan = new Scanner(System.in);
        char hook = ' ';

        System.out.println("Commands :");
        System.out.println("  h <letter> : Define the hook (ex: h C)");
        System.out.println("  r <letters> : Find word(s) with ur rack (ex: r ATSON)");
        System.out.println("  q : Quit");

        while (true) {
            System.out.print("\n(Hook : '" + hook + "') > ");
            if (!scan.hasNext()) break;
            String cmd = scan.next().toLowerCase();

            if (cmd.equals("q")) break;

            if (cmd.equals("h")) {
                String input = scan.next().toUpperCase();
                hook = input.equals("NONE") ? ' ' : input.charAt(0);
            }
            else if (cmd.equals("r")) {
                String rackStr = scan.next().toUpperCase();

                Character[] rack = new Character[rackStr.length()];
                for (int i = 0; i < rackStr.length(); i++) rack[i] = rackStr.charAt(i);

                long startTime = System.nanoTime();
                Set<String> results = dawg.findWordsWithRackAndHook(rack, hook);
                long endTime = System.nanoTime();

                double durationMs = (endTime - startTime) / 1_000_000.0;

                if (results == null || results.isEmpty()) {
                    System.out.println("No result found " + String.format("%.3f", durationMs) + " ms");
                } else {
                    System.out.println(results.size() + " results found in " + String.format("%.3f", durationMs) + " ms :");

                    List<String> sortedResults = new ArrayList<>(results);
                    sortedResults.sort(Comparator.comparingInt(String::length).reversed());

                    for (String res : sortedResults) {
                        System.out.print(res + " ");
                    }
                    System.out.println();
                }
            }
        }
        scan.close();
    }
}