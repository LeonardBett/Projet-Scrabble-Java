package fr.ubordeaux.scrabble.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import fr.ubordeaux.scrabble.controller.builders.ExchangeMoveBuilderController;
import fr.ubordeaux.scrabble.model.core.HumanPlayer;
import fr.ubordeaux.scrabble.model.core.Move;
import fr.ubordeaux.scrabble.model.core.Tile;
import fr.ubordeaux.scrabble.model.enums.MoveType;
import fr.ubordeaux.scrabble.model.enums.PlayerColor;
import fr.ubordeaux.scrabble.model.interfaces.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExchangeMoveBuilderTest {

  private Player player;

  @BeforeEach
  void setUp() {
    player = new HumanPlayer("Alice", PlayerColor.BLUE);
    player.getRack().addTile(new Tile('A'));
    player.getRack().addTile(new Tile('B'));
    player.getRack().addTile(new Tile('C'));
    player.getRack().addTile(new Tile('D'));
  }

  @Test
  void buildShouldReturnMoveWhenLettersAreInRack() {
    Move move = ExchangeMoveBuilderController.build("AB", player);
    assertNotNull(move);
    assertEquals(MoveType.EXCHANGE, move.getType());
  }

  @Test
  void buildShouldReturnNullWhenLetterNotInRack() {
    Move move = ExchangeMoveBuilderController.build("Z", player);
    assertNull(move);
  }

  @Test
  void buildShouldReturnNullWhenEmptyString() {
    Move move = ExchangeMoveBuilderController.build("", player);
    assertNull(move);
  }

  @Test
  void buildShouldReturnNullWhenDuplicateLetterNotInRack() {
    // Only one 'A' in rack, asking for two should fail
    Move move = ExchangeMoveBuilderController.build("AA", player);
    assertNull(move);
  }

  @Test
  void buildShouldReturnMoveForSingleLetter() {
    Move move = ExchangeMoveBuilderController.build("C", player);
    assertNotNull(move);
    assertEquals(1, move.getTiles().size());
  }

  @Test
  void buildShouldReturnMoveForAllRackLetters() {
    Move move = ExchangeMoveBuilderController.build("ABCD", player);
    assertNotNull(move);
    assertEquals(4, move.getTiles().size());
  }
}
