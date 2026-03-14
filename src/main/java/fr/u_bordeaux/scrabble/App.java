package fr.u_bordeaux.scrabble;

import fr.u_bordeaux.scrabble.view.optionLancement.CLILauncher;
import fr.u_bordeaux.scrabble.view.optionLancement.GUILauncher;
import fr.u_bordeaux.scrabble.view.optionLancement.HelpPrinter;
import fr.u_bordeaux.scrabble.view.optionLancement.OptionPlayer;

/**
 * Point d'entrée de l'application.
 * Switch direct sur l'argument → appelle le fichier correspondant.
 */
public class App {

    public static void main(String[] args) {
        int players = OptionPlayer.DEFAULT; 
        boolean guiMode = false;
        for (int i=0;i<args.length;i++){
            switch (args[i]) {
                case "-h", "--help"    -> HelpPrinter.printHelp();
                case "-V", "--version" -> HelpPrinter.printVersion();
                case "-g", "--gui"     -> guiMode = true;
                case "-p", "--players" ->  {
                if (i + 1 >= args.length) {
                    System.err.println("'-p' attend un nombre (ex: -p 3).");
                    System.exit(1);
                    }
                    players = OptionPlayer.parsePlayers(args[++i]);
                }
                default -> {
                    System.err.println("Option inconnue : " + args[i]);
                    System.err.println("Utilisez -h ou --help pour afficher l'aide.");
                    System.exit(1);
                }
            }
        }
        if (guiMode) {
            GUILauncher.launch(args,players);
        } else {
            CLILauncher.launch(players);
        }
    }
}