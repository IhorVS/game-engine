package ivs.games.engine;

/**
 * Represents the result of successful game initialization
 * or command processing.
 *
 * <p>Processing failures are reported through engine exceptions.
 * Concrete games may provide additional result data.
 *
 * @param <S> the game state type
 */
public interface CommandResult<S> {

    /**
     * Returns the resulting game state.
     *
     * @return the non-null resulting game state
     */
    S getState();
}
