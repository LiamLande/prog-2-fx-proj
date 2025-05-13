package edu.ntnu.idi.bidata.ui;

/**
 * A record to hold player setup information from the UI.
 *
 * @param name The name of the player.
 * @param pieceIdentifier The identifier for the player's chosen piece.
 */
public record PlayerSetupData(String name, String pieceIdentifier) {
}