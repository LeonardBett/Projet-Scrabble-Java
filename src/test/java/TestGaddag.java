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

        System.out.println("Chargement du dictionnaire...");
        try (InputStream is = TestGaddag.class.getResourceAsStream(lexiconPath)) {
            if (is == null) throw new Exception("Fichier introuvable : " + lexiconPath);

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null) {
                gaddag.add(line.trim().toUpperCase());
            }
            System.out.println("Dictionnaire chargé.");
        } catch (Exception e) {
            System.err.println("Erreur : " + e.getMessage());
            return;
        }

        Scanner scan = new Scanner(System.in);
        char hook = ' ';

        // Pour word = "apple"
        System.out.println("Est-ce que APPLE est valide ? " + gaddag.containsWord("APPLE")); //true
        System.out.println("Est-ce que ZYZZYVA est valide ? " + gaddag.containsWord("ZYZZYVA")); //false

        String[] wordsToTest = {"APPLE", "ZYZZYVA", "SCRABBLE", "ZURICH"}; // ZURICH est le dernier mot

        System.out.println("\n--- BENCHMARK RECHERCHE DICTIONNAIRE ---");

        for (String word : wordsToTest) {
            // 1. Démarrage du chrono (en nanosecondes)
            long startTime = System.nanoTime();

            // 2. Exécution de la recherche
            boolean exists = gaddag.containsWord(word);

            // 3. Fin du chrono
            long endTime = System.nanoTime();

            // 4. Calcul de la durée
            long duration = endTime - startTime;

            // Conversion en microsecondes pour une lecture plus facile (1 µs = 1000 ns)
            double durationInMicro = duration / 1000.0;

            System.out.printf("Mot: %-10s | Existe: %-5b | Temps: %.2f µs%n",
                    word, exists, durationInMicro);
        }

        System.out.println("\n--- MODE REPL (GADDAG RESULT) ---");

        System.out.println("\n--- TESTEUR DE MOTS GADDAG ---");
        System.out.println("Commandes :");
        System.out.println("  h <lettre> : Définir la lettre d'appui sur le plateau (ex: h C)");
        System.out.println("  r <lettres> : Chercher les mots avec ton rack (ex: r ATSON)");
        System.out.println("  q : Quitter");

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

                // On garde ta conversion en Character[]
                Character[] rack = new Character[rackStr.length()];
                for (int i = 0; i < rackStr.length(); i++) rack[i] = rackStr.charAt(i);

                // --- LA LIGNE CORRIGÉE ---
                // On utilise le type complexe GADDAG.GaddagResult
                HashSet<GADDAG.GaddagResult> results = gaddag.findWordsWithRackAndHook(rack, hook);

                if (results == null || results.isEmpty()) {
                    System.out.println("Aucun résultat.");
                } else {
                    System.out.println(results.size() + " résultats trouvés :");

                    // Parcours des objets résultats
                    for (GADDAG.GaddagResult res : results) {
                        // Ici, j'utilise res.toString().
                        // Si GaddagResult a une méthode .getWord(), utilise-la !
                        System.out.println(" - " + res.toString());
                    }
                }
            }
        }
        scan.close();
    }
}
