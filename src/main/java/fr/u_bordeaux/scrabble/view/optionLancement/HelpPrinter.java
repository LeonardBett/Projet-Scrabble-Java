package fr.u_bordeaux.scrabble.view.optionLancement;

/**
 * Affiche l'aide et la version du programme.
 *
 * <p>SRP : responsabilité unique — tout le texte d'aide
 * est ici, App.java et ArgsParser n'en savent rien.
 */
public class HelpPrinter {

    /** The application version. */
    public static final String VERSION  = "1.0.0";

    /** The application name. */
    public static final String APP_NAME = "Scrabble U-Bordeaux";

    private HelpPrinter() {}

    /**
     * Prints the help message to standard output.
     */
    public static void printHelp() {
        System.out.println("""
                Usage : scrabble [OPTION]

                Options :
                  -h, --help      Affiche ce message d'aide et quitte
                  -V, --version   Affiche la version du programme et quitte
                  -g, --gui       Lance l'interface graphique (JavaFX)
                   -p N, --players N  Nombre de joueurs : 2, 3 ou 4 (défaut : 2)

                Sans option, le jeu démarre en mode terminal (CLI).

                Exemples :
                  java -jar scrabble.jar            Lance en mode CLI
                  java -jar scrabble.jar --gui      Lance en mode GUI
                  java -jar scrabble.jar --version  Affiche la version
                """);
    }

    /**
     * Prints the version string to standard output.
     */
    public static void printVersion() {
        System.out.println(APP_NAME + " v" + VERSION);
    }
}