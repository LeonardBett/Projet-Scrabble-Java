import fr.ubordeaux.scrabble.model.dictionary.Gaddag;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Scanner;

public class TestGaddag {

  public static void main(String[] args) {
    Gaddag gaddag = new Gaddag();
    String lexiconPath = "/dictionaries/lexicon_en.txt";
    int wordCount = 0;

    System.out.println("--- Start loading ---");
    long loadStartTime = System.currentTimeMillis();

    try (InputStream is = TestGaddag.class.getResourceAsStream(lexiconPath)) {
      if (is == null) {
        throw new Exception("File not found : " + lexiconPath);
      }

      BufferedReader br = new BufferedReader(new InputStreamReader(is));
      String line;
      while ((line = br.readLine()) != null) {
        gaddag.add(line.trim().toUpperCase());
        wordCount++;
      }

      long loadEndTime = System.currentTimeMillis();
      System.out.println("Gaddag loaded");
      System.out.println("Word count      : " + wordCount);
      System.out.println("Time to load : " + (loadEndTime - loadStartTime) + " ms");
      System.out.println("-------------------------------------------");

    } catch (Exception e) {
      System.err.println("ERROR " + e.getMessage());
      return;
    }

    System.out.println("\n--- BENCHMARK (CONTAINS) ---");
    String[] wordsToTest = {"JAVA", "ZYZZYVA", "SCRABBLE", "APPLE", "INEXISTANT"};
    for (String word : wordsToTest) {
      long startTime = System.nanoTime();
      boolean exists = gaddag.containsWord(word);
      long endTime = System.nanoTime();

      double duration = (endTime - startTime) / 1000.0;
      System.out.printf("Word : %-10s | Real : %-5b | Time : %.2f µs%n", word, exists, duration);
    }

    // --- SECTION 3 : MODE REPL AVEC TEMPS DE GÉNÉRATION ---
    System.out.println("\n--- TEST Word generation (Gaddag) ---");
    System.out.println("Commands :");
    System.out.println("  h <letter> : Define the hook (ex: h C)");
    System.out.println("  r <letters> : Find word(s) with ur rack (ex: r ATSON)");
    System.out.println("  q : Quit");

    Scanner scan = new Scanner(System.in);
    char hook = ' ';

    while (true) {
      System.out.print("\n(Hook: '" + hook + "') > ");
      if (!scan.hasNext()) {
        break;
      }
      String cmd = scan.next().toLowerCase();

      if (cmd.equals("q")) {
        break;
      }

      if (cmd.equals("h")) {
        hook = scan.next().toUpperCase().charAt(0);
      } else if (cmd.equals("r")) {
        String rackStr = scan.next().toUpperCase();

        Character[] rack = new Character[rackStr.length()];
        for (int i = 0; i < rackStr.length(); i++) {
          rack[i] = rackStr.charAt(i);
        }

        long startTime = System.nanoTime();
        HashSet<Gaddag.GaddagResult> results = gaddag.findWordsWithRackAndHook(rack, hook);
        long endTime = System.nanoTime();

        double durationMs = (endTime - startTime) / 1_000_000.0;

        if (results == null || results.isEmpty()) {
          System.out.println("No result found " + String.format("%.3f", durationMs) + " ms");
        } else {
          System.out.println(
              results.size() + " results found in " + String.format("%.3f", durationMs) + " ms :");

          for (Gaddag.GaddagResult res : results) {
            System.out.print(res.toString() + " ");
          }
          System.out.println();
        }
      }
    }
    scan.close();
  }
}
