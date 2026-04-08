package fr.ubordeaux.scrabble.controller.builders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import fr.ubordeaux.scrabble.model.core.HumanPlayer;
import fr.ubordeaux.scrabble.model.core.Move;
import fr.ubordeaux.scrabble.model.core.Tile;
import fr.ubordeaux.scrabble.model.enums.Direction;
import fr.ubordeaux.scrabble.model.enums.PlayerColor;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class PlayMoveBuilderControllerTest {

  @Test
  void buildShouldParseMultipleNotationForms() {
    HumanPlayer player = new HumanPlayer("Alice", PlayerColor.BLUE);
    player.getRack().setTiles(new ArrayList<>(List.of(
        new Tile('H'),
        new Tile('E'),
        new Tile('L'),
        new Tile('L'),
        new Tile('O'),
        new Tile('A'),
        new Tile('B'))));

    Move numeric = PlayMoveBuilderController.build(player, "7 8 H HELLO");
    assertNotNull(numeric);
    assertEquals(Direction.HORIZONTAL, numeric.getDirection());
    assertEquals(5, numeric.getTiles().size());

    Move alphanumeric = PlayMoveBuilderController.build(player, "a1G HELLO");
    assertNotNull(alphanumeric);
    assertEquals(Direction.HORIZONTAL, alphanumeric.getDirection());

    Move compact = PlayMoveBuilderController.build(player, "1av HELLO");
    assertNotNull(compact);
    assertEquals(Direction.VERTICAL, compact.getDirection());
  }

  @Test
  void buildShouldHandleJokersAndRejectInvalidInputs() {
    HumanPlayer jokerPlayer = new HumanPlayer("Joker", PlayerColor.RED);
    jokerPlayer.getRack().setTiles(new ArrayList<>(List.of(
        new Tile(' ', true),
        new Tile('E'),
        new Tile('L'),
        new Tile('L'),
        new Tile('O'))));

    Move jokerMove = PlayMoveBuilderController.build(jokerPlayer, "a1h hELLO");
    assertNotNull(jokerMove);
    assertTrue(jokerMove.getTiles().getFirst().isJoker());
    assertEquals('H', jokerMove.getTiles().getFirst().getCharacter());

    assertNull(PlayMoveBuilderController.build(jokerPlayer, null));
    assertNull(PlayMoveBuilderController.build(jokerPlayer, ""));
    assertNull(PlayMoveBuilderController.build(jokerPlayer, "a1"));
    assertNull(PlayMoveBuilderController.build(jokerPlayer, "16 8 H HELLO"));
    assertNull(PlayMoveBuilderController.build(jokerPlayer, "7 8 X HELLO"));
    assertNull(PlayMoveBuilderController.build(jokerPlayer, "a1h XYZ"));
  }
}
