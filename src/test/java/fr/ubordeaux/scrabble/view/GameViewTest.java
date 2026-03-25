package fr.ubordeaux.scrabble.view;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class GameViewTest {

  @Test
  void gameViewShouldBeInstantiable() {
    assertNotNull(new GameView());
  }
}
