package ivs.games.engine;

import ivs.games.engine.exception.EngineCommandException;
import ivs.games.engine.exception.EngineException;
import org.junit.jupiter.api.Test;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;

/**
 * Tests argument validation, delegation, result validation
 * and exception handling in {@link AbstractGameEngine}.
 */
class AbstractGameEngineTest {

    private final TestState state = new TestState();
    private final TestCommand command = new TestCommand();

    /**
     * Verifies that a null initialization result is reported as an engine failure,
     * preserving the result validation exception as its cause.
     */
    @Test
    void startGameWrapsNullResult() {
        TestEngine engine = new TestEngine(() -> null, null);

        EngineException exception =
                assertThrows(EngineException.class, engine::startGame);

        assertEquals("Failed to start game", exception.getMessage());
        IllegalStateException cause =
                assertInstanceOf(IllegalStateException.class, exception.getCause());
        assertEquals("doStartGame() must not return null", cause.getMessage());
    }

    /**
     * Verifies that an engine exception thrown during initialization
     * propagates as the same instance without wrapping.
     */
    @Test
    void startGamePropagatesEngineExceptionWithoutWrapping() {
        EngineException failure = new EngineException("Initialization failed");
        TestEngine engine = new TestEngine(() -> {
            throw failure;
        }, null);

        EngineException exception = assertThrows(EngineException.class, engine::startGame);
        assertSame(failure, exception);
    }

    /**
     * Verifies that an unexpected runtime exception during initialization
     * is wrapped with the initialization failure message and original cause.
     */
    @Test
    void startGameWrapsRuntimeExceptionAndPreservesCause() {
        RuntimeException failure = new IllegalArgumentException("Invalid setup");
        TestEngine engine = new TestEngine(() -> {
            throw failure;
        }, null);

        EngineException exception =
                assertThrows(EngineException.class, engine::startGame);

        assertEquals("Failed to start game", exception.getMessage());
        assertSame(failure, exception.getCause());
    }

    /**
     * Verifies that a null state is rejected before game-specific processing
     * and that the argument validation exception is not wrapped.
     */
    @Test
    void processCommandRejectsNullStateBeforeDelegation() {
        TestEngine engine = new TestEngine(null, (currentState, currentCommand) -> {
            fail("doProcessCommand() must not be called");
            return null;
        });

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> engine.processCommand(null, command)
        );

        assertEquals("State must not be null", exception.getMessage());
    }

    /**
     * Verifies that a null command is rejected before game-specific processing
     * and that the argument validation exception is not wrapped.
     */
    @Test
    void processCommandRejectsNullCommandBeforeDelegation() {
        TestEngine engine = new TestEngine(null, (currentState, currentCommand) -> {
            fail("doProcessCommand() must not be called");
            return null;
        });

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> engine.processCommand(state, null)
        );

        assertEquals("Command must not be null", exception.getMessage());
    }

    /**
     * Verifies that game-specific processing receives the exact state
     * and command instances supplied to the public method.
     */
    @Test
    void processCommandPassesOriginalArgumentsToImplementation() {
        TestEngine engine = new TestEngine(null, (currentState, currentCommand) -> {
            assertSame(state, currentState);
            assertSame(command, currentCommand);
            throw new EngineCommandException("Command rejected");
        });

        assertThrows(
                EngineCommandException.class,
                () -> engine.processCommand(state, command)
        );
    }

    /**
     * Verifies that a null processing result is reported as an engine failure,
     * including the command class name and preserving the validation cause.
     */
    @Test
    void processCommandWrapsNullResult() {
        TestEngine engine = new TestEngine(null, (currentState, currentCommand) -> null);

        EngineException exception = assertThrows(
                EngineException.class,
                () -> engine.processCommand(state, command)
        );

        assertEquals("Failed to process command: TestCommand", exception.getMessage());
        IllegalStateException cause =
                assertInstanceOf(IllegalStateException.class, exception.getCause());
        assertEquals("doProcessCommand() must not return null", cause.getMessage());
    }

    /**
     * Verifies that an engine exception thrown during command processing
     * propagates as the same instance without wrapping.
     */
    @Test
    void processCommandPropagatesEngineExceptionWithoutWrapping() {
        EngineException failure = new EngineException("Processing failed");
        TestEngine engine = new TestEngine(null, (currentState, currentCommand) -> {
            throw failure;
        });

        EngineException exception = assertThrows(
                EngineException.class,
                () -> engine.processCommand(state, command)
        );

        assertSame(failure, exception);
    }

    /**
     * Verifies that a command rejection retains its exception type
     * and propagates as the same instance without wrapping.
     */
    @Test
    void processCommandPropagatesCommandExceptionWithoutWrapping() {
        EngineCommandException failure =
                new EngineCommandException("Wrong turn");
        TestEngine engine = new TestEngine(null, (currentState, currentCommand) -> {
            throw failure;
        });

        EngineCommandException exception = assertThrows(
                EngineCommandException.class,
                () -> engine.processCommand(state, command)
        );

        assertSame(failure, exception);
    }

    /**
     * Verifies that an unexpected runtime exception during command processing
     * is wrapped with the command class name and original cause.
     */
    @Test
    void processCommandWrapsRuntimeExceptionAndIncludesCommandName() {
        RuntimeException failure = new IllegalStateException("Unexpected failure");
        TestEngine engine = new TestEngine(null, (currentState, currentCommand) -> {
            throw failure;
        });

        EngineException exception = assertThrows(
                EngineException.class,
                () -> engine.processCommand(state, command)
        );

        assertEquals("Failed to process command: TestCommand", exception.getMessage());
        assertSame(failure, exception.getCause());
    }

    /** Minimal state used to verify argument delegation. */
    private static final class TestState {
    }

    /** Named command used to verify delegation and failure messages. */
    private static final class TestCommand implements Command {
    }

    /**
     * Delegates game-specific operations to test-provided handlers.
     */
    private static final class TestEngine
            extends AbstractGameEngine<TestState, TestCommand> {

        private final Supplier<CommandResult<TestState>> startHandler;
        private final BiFunction<TestState, TestCommand, CommandResult<TestState>>
                processHandler;

        private TestEngine(
                Supplier<CommandResult<TestState>> startHandler,
                BiFunction<TestState, TestCommand, CommandResult<TestState>> processHandler
        ) {
            this.startHandler = startHandler;
            this.processHandler = processHandler;
        }

        @Override
        protected CommandResult<TestState> doStartGame() {
            return startHandler.get();
        }

        @Override
        protected CommandResult<TestState> doProcessCommand(
                TestState state,
                TestCommand command
        ) {
            return processHandler.apply(state, command);
        }
    }

    /**
     * Verifies that successful initialization returns the exact result
     * provided by the implementation.
     */
    @Test
    @SuppressWarnings("unchecked")
    void startGameReturnsImplementationResult() {
        CommandResult<TestState> expectedResult = mock(CommandResult.class);
        TestEngine engine = new TestEngine(() -> expectedResult, null);

        CommandResult<TestState> actualResult = engine.startGame();

        assertSame(expectedResult, actualResult);
    }

    /**
     * Verifies that successful processing delegates the original arguments
     * and returns the exact result provided by the implementation.
     */
    @Test
    @SuppressWarnings("unchecked")
    void processCommandReturnsImplementationResult() {
        CommandResult<TestState> expectedResult = mock(CommandResult.class);
        TestEngine engine = new TestEngine(null, (currentState, currentCommand) -> {
            assertSame(state, currentState);
            assertSame(command, currentCommand);
            return expectedResult;
        });

        CommandResult<TestState> actualResult = engine.processCommand(state, command);

        assertSame(expectedResult, actualResult);
    }
}
