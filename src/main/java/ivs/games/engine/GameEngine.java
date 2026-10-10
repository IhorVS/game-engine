package ivs.games.engine;

/**
 * Creates games and processes commands according to game-specific rules.
 *
 * <p>Implementations must be thread-safe and must not retain mutable state
 * belonging to an individual game. An engine may be shared by multiple sessions
 * using the same rule configuration and different initial setups.
 *
 * <p>Game states are immutable snapshots. Processing failures are reported
 * through exceptions rather than returned results.
 *
 * @param <S> the game state type
 * @param <C> the supported command type
 * @param <I> the initial setup type
 */
public interface GameEngine<S, C extends Command, I> {

    /**
     * Creates the initial game state from the supplied setup.
     *
     * @param initialSetup the initial setup for the game
     * @return the non-null initial game state
     */
    S startGame(I initialSetup);

    /**
     * Processes a command according to game rules without modifying the supplied state.
     *
     * @param state   the current immutable game state
     * @param command the command to process
     * @return the non-null resulting game state
     */
    S processCommand(S state, C command);
}
