package ivs.games.engine;

/**
 * Marks a game command.
 *
 * <p>A command describes an intended game action and its parameters.
 * Concrete games define their own command types.
 *
 * <p>Commands should be immutable and must not contain session data
 * or user account information.
 */
public interface Command {
}
