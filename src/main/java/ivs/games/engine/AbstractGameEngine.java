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
 * <p>State copying and mutation follow the contract of the concrete game.
 * Shared immutable data may be reused between states.
 *
 * @param <S> the game state type
 * @param <C> the supported command type
 */
public abstract class AbstractGameEngine<S, C extends Command>
        implements GameEngine<S, C> {

    /**
     * Creates a game and checks the implementation result.
     *
     * <p>Engine exceptions propagate without wrapping. Other runtime exceptions
     * raised during initialization or result validation are wrapped
     * in an {@link EngineException} with the original cause preserved.
     *
     * @return a non-null game creation result
     * @throws EngineException if initialization fails or the implementation
     *         returns null
     */
    @Override
    public final CommandResult<S> startGame() {
        try {
            CommandResult<S> result = doStartGame();
            Validate.validState(result != null, "doStartGame() must not return null");
            return result;

        } catch (EngineException e) {
            throw e;

        } catch (RuntimeException e) {
            throw new EngineException("Failed to start game", e);
        }
    }

    /**
     * Validates the arguments, processes the command and checks the result.
     *
     * <p>An {@link EngineException}, including its subclasses, propagates
     * without wrapping. Other runtime exceptions raised during processing
     * or result validation are wrapped in an {@code EngineException}
     * with the original cause preserved.
     *
     * @param state the current game state
     * @param command the command to process
     * @return a non-null processing result
     * @throws NullPointerException if state or command is null
     * @throws ivs.games.engine.exception.EngineCommandException
     *         if the implementation rejects the command according to game rules
     * @throws EngineException if processing fails or the implementation
     *         returns null
     */
    @Override
    public final CommandResult<S> processCommand(S state, C command) {
        Validate.notNull(state, "State must not be null");
        Validate.notNull(command, "Command must not be null");

        try {
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
     * Creates an independent initial game state and determines its game status.
     *
     * <p>The implementation must not retain mutable data belonging
     * to the created game.
     *
     * @return a non-null game creation result
     * @throws EngineException if the game cannot be initialized
     */
    protected abstract CommandResult<S> doStartGame();

    /**
     * Processes a command according to game rules and determines
     * the resulting game status.
     *
     * <p>The implementation defines whether the supplied state may be modified
     * or an independent working state must be created. Changes must not affect
     * the state retained by the session if processing fails.
     *
     * @param state the non-null current game state
     * @param command the non-null command to process
     * @return a non-null processing result
     * @throws EngineCommandException
     *         if the command is not permitted by game rules
     * @throws EngineException if processing fails
     */
    protected abstract CommandResult<S> doProcessCommand(S state, C command);
}
