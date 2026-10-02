package ivs.games.engine;

/**
 * Represents the result of successful game initialization
 * or command processing.
 *
 * <p>Processing failures are reported through engine exceptions.
 * Concrete games may provide additional result data. Game-specific logic
 * is responsible for result correctness; the abstract engine checks only
 * that the result object itself is non-null.
 *
 * @param <S> the game state type
 */
public interface CommandResult<S> {

    /**
     * Returns the resulting immutable game state.
     *
     * @return the non-null resulting immutable game state
     */
    @SuppressWarnings("unused")
    S getState();
}
