package fr.ubordeaux.scrabble.view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class UserInterfaceTest {

  @Test
  void shouldAllowCustomImplementation() {
    FakeView view = new FakeView();

    view.refresh();
    view.displayMessage("hello");
    view.displayError("oops");
    view.displaySuccess("ok");

    assertEquals(1, view.refreshCount);
    assertEquals("hello", view.lastMessage);
    assertEquals("oops", view.lastError);
    assertEquals("ok", view.lastSuccess);
  }

  private static class FakeView implements UserInterface {
    private int refreshCount;
    private String lastMessage;
    private String lastError;
    private String lastSuccess;

    @Override
    public void refresh() {
      refreshCount++;
    }

    @Override
    public void displayMessage(String message) {
      lastMessage = message;
    }

    @Override
    public void displayError(String error) {
      lastError = error;
    }

    @Override
    public void displaySuccess(String message) {
      lastSuccess = message;
    }
  }
}
