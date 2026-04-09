package fr.ubordeaux.scrabble.view.gui.main;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import org.junit.jupiter.api.Test;

class ScrabbleGuiConfigDialogTest {

  @Test
  void shouldApplyShouldRecognizeAcceptedButtons() {
    ButtonType applyRestart = new ButtonType("applyRestart", ButtonBar.ButtonData.LEFT);

    assertTrue(ScrabbleGuiConfigDialog.shouldApply(Optional.of(ButtonType.OK), applyRestart));
    assertTrue(ScrabbleGuiConfigDialog.shouldApply(Optional.of(applyRestart), applyRestart));
    assertFalse(ScrabbleGuiConfigDialog.shouldApply(Optional.of(ButtonType.CANCEL), applyRestart));
    assertFalse(ScrabbleGuiConfigDialog.shouldApply(Optional.empty(), applyRestart));
  }

  @Test
  void shouldRecreateShouldOnlyBeTrueForApplyRestart() {
    ButtonType applyRestart = new ButtonType("applyRestart", ButtonBar.ButtonData.LEFT);

    assertTrue(ScrabbleGuiConfigDialog.shouldRecreate(Optional.of(applyRestart), applyRestart));
    assertFalse(ScrabbleGuiConfigDialog.shouldRecreate(Optional.of(ButtonType.OK), applyRestart));
    assertFalse(
            ScrabbleGuiConfigDialog.shouldRecreate(Optional.of(ButtonType.CANCEL), applyRestart));
    assertFalse(ScrabbleGuiConfigDialog.shouldRecreate(Optional.empty(), applyRestart));
  }

  @Test
  void buildAssignmentsShouldIncludeAllValues() {
    String assignments = ScrabbleGuiConfigDialog.buildAssignments(
        "fr",
        4,
        true,
        true,
        11,
        7,
        true,
        true,
        " custom.dict ",
        true,
        false);

    assertTrue(assignments.contains("language=fr"));
    assertTrue(assignments.contains("players=4"));
    assertTrue(assignments.contains("super-scrabble=true"));
    assertTrue(assignments.contains("blitz=true"));
    assertTrue(assignments.contains("timeout=11"));
    assertTrue(assignments.contains("ai-time=7"));
    assertTrue(assignments.contains("ai-exptiminimax=true"));
    assertTrue(assignments.contains("ai-ml=true"));
    assertTrue(assignments.contains("dictionary=custom.dict"));
    assertTrue(assignments.contains("debug=true"));
    assertTrue(assignments.contains("verbose=false"));
  }

}
