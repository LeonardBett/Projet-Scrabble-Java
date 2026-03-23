package fr.ubordeaux.scrabble.model.network;

/**
 * Different status for an online player.
 */
public enum PlayerStatus {
  /**
   * Idle player status.
   */
  IDLE,
  /**
   * Ingame player status.
   */
  INGAME,
  /**
   * Away player status.
   */
  AWAY,
  /**
   * Waitgame player status.
   */
  WAITGAME;
}
