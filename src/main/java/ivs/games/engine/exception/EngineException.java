package ivs.games.engine.exception;

import java.io.Serial;

/**
 * Indicates a failure during game engine execution.
 *
 * <p>Unexpected runtime exceptions from game-specific logic are wrapped
 * in this exception with their original cause preserved.
 */
public class EngineException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Creates an exception with a failure description.
     *
     * @param message the failure description
     */
    public EngineException(String message) {
        super(message);
    }

    /**
     * Creates an exception with a failure description and its cause.
     *
     * @param message the failure description
     * @param cause   the original cause
     */
    public EngineException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates an exception with its original cause.
     *
     * @param cause the original cause
     */
    public EngineException(Throwable cause) {
        super(cause);
    }
}
