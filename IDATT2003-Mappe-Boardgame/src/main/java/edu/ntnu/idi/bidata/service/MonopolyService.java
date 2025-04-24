package edu.ntnu.idi.bidata.service;

import edu.ntnu.idi.bidata.exception.InvalidParameterException;
import edu.ntnu.idi.bidata.model.BoardGame;
import edu.ntnu.idi.bidata.model.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Monopoly-specific game logic.
 * <p>
 * Currently a stub: all methods throw UnsupportedOperationException
 * until you fill in actual Monopoly rules (rolling doubles, buying
 * houses, jail, auction, bankruptcies, etc.).
 */
public class MonopolyService implements GameService {

    @Override
    public void setup(BoardGame game) {
        if (game == null) {
            throw new InvalidParameterException("Game cannot be null");
        }
        // TODO: Place players on GO, give $1500, clear properties, etc.
        throw new UnsupportedOperationException("MonopolyService.setup() not implemented");
    }

    @Override
    public List<Integer> playOneRound(BoardGame game) {
        if (game == null) {
            throw new InvalidParameterException("Game cannot be null");
        }
        List<Integer> rolls = new ArrayList<>();
        // TODO: Loop through players, handle doubles, moves, property logic...
        throw new UnsupportedOperationException("MonopolyService.playOneRound() not implemented");
        // return rolls;
    }

    @Override
    public int playTurn(BoardGame game, Player player) {
        if (game == null || player == null) {
            throw new InvalidParameterException("Game and player must not be null");
        }
        // TODO: Roll two dice, move, handle landing logic, doubles/jail...
        throw new UnsupportedOperationException("MonopolyService.playTurn() not implemented");
        // return rollValue;
    }

    @Override
    public boolean isFinished(BoardGame game) {
        if (game == null) {
            throw new InvalidParameterException("Game cannot be null");
        }
        // TODO: Return true when only one player remains solvent, or after N rounds
        throw new UnsupportedOperationException("MonopolyService.isFinished() not implemented");
    }

    @Override
    public Player getWinner(BoardGame game) {
        if (game == null) {
            throw new InvalidParameterException("Game cannot be null");
        }
        // TODO: Identify the richest player or last player standing
        throw new UnsupportedOperationException("MonopolyService.getWinner() not implemented");
    }
}
