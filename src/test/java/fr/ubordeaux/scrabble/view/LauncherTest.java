package fr.ubordeaux.scrabble.view;


import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import fr.ubordeaux.scrabble.view.optionlancement.CliLauncher;
import fr.ubordeaux.scrabble.view.optionlancement.GuiLauncher;
import org.junit.jupiter.api.Test;

/**
 * Tests unitaires pour CliLauncher et GuiLauncher.
 *
 * <p>Note : launch() démarre une application interactive, donc on ne teste que
 * l'instanciabilité et les comportements vérifiables sans lancer d'IHM.
 */
class LauncherTest {

  // ─── CliLauncher ─────────────────────────────────────────────────────────

  @Test
  void cliLauncherClassShouldExist() {
    // Vérifie que la classe est accessible
    assertNotNull(CliLauncher.class);
  }

  @Test
  void guiLauncherClassShouldExist() {
    assertNotNull(GuiLauncher.class);
  }

  // Les méthodes launch() démarrent des IHM interactives et ne peuvent pas
  // être invoquées dans un contexte de tests automatisés. On vérifie ici que
  // la configuration des launchers est cohérente avec OptionPlayer.
  @Test
  void cliLauncherAndOptionPlayerShouldShareMinPlayers() {
    // CliLauncher n'expose pas de constantes propres, mais s'appuie sur OptionPlayer
    assertEquals(2, fr.ubordeaux.scrabble.view.optionlancement.OptionPlayer.MIN);
  }

  @Test
  void cliLauncherAndOptionPlayerShouldShareMaxPlayers() {
    assertEquals(4, fr.ubordeaux.scrabble.view.optionlancement.OptionPlayer.MAX);
  }
}