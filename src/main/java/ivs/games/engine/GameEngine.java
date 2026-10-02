package ivs.games.engine;

import ivs.games.engine.exception.EngineCommandException;
import ivs.games.engine.exception.EngineException;

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
     * Validates the supplied initial setup and creates an independent
     * immutable game state, including its game status.
     *
     * <p>The supplied setup must remain unchanged.
     *
     * @param initialSetup the initial setup for the game
     * @return a non-null result containing the initial game state
     * @throws NullPointerException if initialSetup is null
     * @throws EngineException if the initial setup is invalid
     *         or initialization fails
     */
    CommandResult<S> startGame(I initialSetup);

    /**
     * Processes a command in the supplied immutable game state.
     *
     * <p>The supplied state and command must remain unchanged, including
     * when processing fails. Successful processing returns a result
     * containing the resulting game state and its game-specific data.
     *
     * @param state the current immutable game state
     * @param command the command to process
     * @return a non-null processing result
     * @throws NullPointerException if state or command is null
     * @throws EngineCommandException if the command is not permitted
     *         by game rules
     * @throws EngineException if processing fails
     */
    CommandResult<S> processCommand(S state, C command);
}
