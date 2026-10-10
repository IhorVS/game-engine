package ivs.games.engine.exception;

import java.io.Serial;

/**
 * Indicates that a command cannot be executed according to game rules.
 *
 * <p>Game-specific logic throws this exception explicitly, for example
 * when a command violates turn order or requests an illegal action.
 *
 * <p>The engine propagates this exception without wrapping it,
 * allowing the session to distinguish command rejection from
 * a technical failure.
 */
public class EngineCommandException extends EngineException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Creates an exception with a command rejection description.
     *
     * @param message the command rejection description
     */
    public EngineCommandException(String message) {
        super(message);
    }

    /**
     * Creates an exception with a command rejection description
     * and its cause.
     *
     * @param message the command rejection description
     * @param cause   the cause of the command rejection
     */
    public EngineCommandException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Validates a condition required for a command to be permitted.
     *
     * @param expression the condition to validate
     * @param message    the rejection message format
     * @param values     the arguments used to format the rejection message
     * @throws EngineCommandException if the expression is false
     */
    public static void validate(boolean expression, String message, Object... values) {
        if (!expression) {
            throw new EngineCommandException(String.format(message, values));
        }
    }
}
