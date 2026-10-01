package ivs.games.engine;

/**
 * Creates games and processes commands according to game-specific rules.
 *
 * <p>Implementations must be thread-safe and must not retain the state
 * of an individual game. An engine may be shared by multiple sessions
 * using the same configuration.
 *
 * @param <S> the game state type
 * @param <C> the supported command type
 */
public interface GameEngine<S, C extends Command> {

    /**
     * Creates an independent initial state using the engine configuration.
     *
     * @return a non-null result containing the initial state or a rejection
     */
    CommandResult<S> startGame();

    /**
     * Processes a command in the supplied game state.
     *
     * <p>The supplied state must remain unchanged. On success, the result
     * contains the new state; on rejection, it contains the rejection reason.
     *
     * @param state the current game state
     * @param command the command to process
     * @return a non-null processing result
     * @throws NullPointerException if state or command is null
     */
    CommandResult<S> processCommand(S state, C command);
}
