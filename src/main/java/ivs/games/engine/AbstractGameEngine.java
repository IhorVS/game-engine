package ivs.games.engine;

import ivs.games.engine.exception.EngineCommandException;
import ivs.games.engine.exception.EngineException;
import org.apache.commons.lang3.Validate;

/**
 * Provides common argument and result checks for game engines.
 *
 * <p>Subclasses implement game-specific initialization and command processing
 * through protected methods. Implementations must be thread-safe and must not
 * retain mutable state belonging to an individual game.
 *
 * <p>Game states are immutable snapshots. Command processing creates a new
 * state without modifying the supplied state. Immutable data may be shared
 * between snapshots.
 *
 * <p>Game-specific logic is responsible for the correctness of result contents.
 * This class checks only that the returned result object is non-null.
 *
 * @param <S> the game state type
 * @param <C> the supported command type
 * @param <I> the initial setup type
 */
public abstract class AbstractGameEngine<S, C extends Command, I>
        implements GameEngine<S, C, I> {

    /**
     * Creates a game from the supplied initial setup and checks the result.
     *
     * <p>Engine exceptions propagate without wrapping. Other runtime exceptions
     * raised during initialization or result validation are wrapped
     * in an {@link EngineException} with the original cause preserved.
     *
     * @param initialSetup the initial setup for the game
     * @return a non-null game creation result
     * @throws NullPointerException if initialSetup is null
     * @throws EngineException if initialization fails or the implementation
     *         returns null
     */
    @Override
    public final CommandResult<S> startGame(I initialSetup) {
        Validate.notNull(initialSetup, "Initial setup must not be null");

        try {
            CommandResult<S> result = doStartGame(initialSetup);
            Validate.validState(result != null, "doStartGame() must not return null");
            return result;

        } catch (EngineException e) {
            throw e;

        } catch (RuntimeException e) {
            throw new EngineException("Failed to start game", e);
        }
    }

    /**
     * Validates the arguments and command, processes the command
     * and checks the result.
     *
     * <p>If preliminary command validation fails, processing is not invoked.
     * An {@link EngineException}, including its subclasses, propagates without
     * wrapping. Other runtime exceptions raised during command validation,
     * processing or result validation are wrapped in an {@code EngineException}
     * with the original cause preserved.
     *
     * @param state the current immutable game state
     * @param command the command to process
     * @return a non-null processing result
     * @throws NullPointerException if state or command is null
     * @throws EngineCommandException if the implementation rejects
     *         the command according to game rules
     * @throws EngineException if processing fails or the implementation
     *         returns null
     */
    @Override
    public final CommandResult<S> processCommand(S state, C command) {
        Validate.notNull(state, "State must not be null");
        Validate.notNull(command, "Command must not be null");

        try {
            validateCommand(state, command);

            CommandResult<S> result = doProcessCommand(state, command);
            Validate.validState(result != null, "doProcessCommand() must not return null");
            return result;

        } catch (EngineException e) {
            throw e;

        } catch (RuntimeException e) {
            String commandName = command.getClass().getSimpleName();
            throw new EngineException("Failed to process command: %s".formatted(commandName), e);
        }
    }

    /**
     * Validates the initial setup, creates an independent immutable game state
     * and determines its game status.
     *
     * <p>The implementation must not modify the supplied setup or retain mutable
     * data belonging to the created game. It is responsible for the correctness
     * of the returned result contents.
     *
     * @param initialSetup the non-null initial setup
     * @return a non-null game creation result
     * @throws EngineException if the initial setup is invalid
     *         or the game cannot be initialized
     */
    protected abstract CommandResult<S> doStartGame(I initialSetup);

    /**
     * Processes a command according to game rules and determines
     * the resulting game status.
     *
     * <p>The supplied state and command must remain unchanged, including
     * when processing fails. Any mutable working data must be isolated
     * from the supplied state.
     *
     * <p>The implementation is responsible for the correctness
     * of the returned result contents.
     *
     * @param state the non-null current immutable game state
     * @param command the non-null command to process
     * @return a non-null processing result
     * @throws EngineCommandException if the command is not permitted
     *         by game rules
     * @throws EngineException if processing fails
     */
    protected abstract CommandResult<S> doProcessCommand(S state, C command);

    /**
     * Validates a command against the supplied game state before processing.
     *
     * <p>This method must not modify the state or command. If validation fails,
     * command processing is not invoked.
     *
     * <p>The default implementation performs no validation. Subclasses may
     * override it to provide game-specific preliminary checks. Checks requiring
     * a candidate resulting state remain part of command processing.
     *
     * @param state the non-null current immutable game state
     * @param command the non-null command to validate
     * @throws EngineCommandException if the command is not permitted
     *         by game rules
     */
    @SuppressWarnings("unused")
    protected void validateCommand(S state, C command) {
    }
}
