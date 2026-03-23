package fr.ubordeaux.scrabble.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import fr.ubordeaux.scrabble.model.core.HumanPlayer;
import fr.ubordeaux.scrabble.model.core.Move;
import fr.ubordeaux.scrabble.model.core.Tile;
import fr.ubordeaux.scrabble.model.enums.MoveType;
import fr.ubordeaux.scrabble.model.enums.PlayerColor;
import fr.ubordeaux.scrabble.model.interfaces.Player;
import fr.ubordeaux.scrabble.view.gui.ExchangeMoveBuilder;
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
    Move move = ExchangeMoveBuilder.build("AB", player);
    assertNotNull(move);
    assertEquals(MoveType.EXCHANGE, move.getType());
  }

  @Test
  void buildShouldReturnNullWhenLetterNotInRack() {
    Move move = ExchangeMoveBuilder.build("Z", player);
    assertNull(move);
  }

  @Test
  void buildShouldReturnNullWhenEmptyString() {
    Move move = ExchangeMoveBuilder.build("", player);
    assertNull(move);
  }

  @Test
  void buildShouldReturnNullWhenDuplicateLetterNotInRack() {
    // Only one 'A' in rack, asking for two should fail
    Move move = ExchangeMoveBuilder.build("AA", player);
    assertNull(move);
  }

  @Test
  void buildShouldReturnMoveForSingleLetter() {
    Move move = ExchangeMoveBuilder.build("C", player);
    assertNotNull(move);
    assertEquals(1, move.getTiles().size());
  }

  @Test
  void buildShouldReturnMoveForAllRackLetters() {
    Move move = ExchangeMoveBuilder.build("ABCD", player);
    assertNotNull(move);
    assertEquals(4, move.getTiles().size());
  }
}
