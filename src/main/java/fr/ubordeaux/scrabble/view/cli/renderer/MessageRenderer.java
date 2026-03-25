package fr.ubordeaux.scrabble.view.cli.renderer;

import fr.ubordeaux.scrabble.i18n.I18n;

/**
 * Responsable de l'affichage des messages système dans la console CLI
 * (erreurs, succès, informations, avertissements, séparateurs).
 */ 
public class MessageRenderer {

  public void error(String message) {
    System.out.println(I18n.tr("cli.msg.errorPrefix") + message);
  }

  public void success(String message) {
    System.out.println(I18n.tr("cli.msg.okPrefix") + message);
  }

  public void welcome() {
    System.out.println(I18n.tr("cli.msg.welcome"));
  }

  public void info(String message) {
    System.out.println(I18n.tr("cli.msg.infoPrefix") + message);
  }

  public void separator() {
    System.out.println("----------------------------------------------"); 
  }

  public void sectionTitle(String title) {
    System.out.println("\n=== " + title + " ===");
  }

  public void warning(String message) {
    System.out.println(I18n.tr("cli.msg.warningPrefix") + message);
  }
}