package ivs.games.engine;

import ivs.games.engine.exception.EngineCommandException;
import ivs.games.engine.exception.EngineException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

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

    private final TestInitialSetup initialSetup = new TestInitialSetup();
    private final TestState state = new TestState();
    private final TestCommand command = new TestCommand();

    /**
     * Verifies that a null initial setup is rejected before initialization
     * and that the argument validation exception is not wrapped.
     */
    @Test
    void startGameRejectsNullInitialSetupBeforeDelegation() {
        TestEngine engine = new TestEngine(setup -> {
            fail("doStartGame() must not be called");
            return null;
        }, null);

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> engine.startGame(null)
        );

        assertEquals("Initial setup must not be null", exception.getMessage());
    }

    /**
     * Verifies that a null initialization result is reported as an engine failure,
     * preserving the result validation exception as its cause.
     */
    @Test
    void startGameWrapsNullResult() {
        TestEngine engine = new TestEngine(setup -> null, null);

        EngineException exception = assertThrows(
                EngineException.class,
                () -> engine.startGame(initialSetup)
        );

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
        TestEngine engine = new TestEngine(setup -> {
            throw failure;
        }, null);

        EngineException exception = assertThrows(
                EngineException.class,
                () -> engine.startGame(initialSetup)
        );

        assertSame(failure, exception);
    }

    /**
     * Verifies that an unexpected runtime exception during initialization
     * is wrapped with the initialization failure message and original cause.
     */
    @Test
    void startGameWrapsRuntimeExceptionAndPreservesCause() {
        RuntimeException failure = new IllegalArgumentException("Invalid setup");
        TestEngine engine = new TestEngine(setup -> {
            throw failure;
        }, null);

        EngineException exception = assertThrows(
                EngineException.class,
                () -> engine.startGame(initialSetup)
        );

        assertEquals("Failed to start game", exception.getMessage());
        assertSame(failure, exception.getCause());
    }

    /**
     * Verifies that initialization receives the original setup and returns
     * the exact result provided by the implementation.
     */
    @Test
    @SuppressWarnings("unchecked")
    void startGameReturnsImplementationResult() {
        CommandResult<TestState> expectedResult = mock(CommandResult.class);
        TestEngine engine = new TestEngine(setup -> {
            assertSame(initialSetup, setup);
            return expectedResult;
        }, null);

        CommandResult<TestState> actualResult = engine.startGame(initialSetup);

        assertSame(expectedResult, actualResult);
    }

    /**
     * Verifies that a null state is rejected before command validation
     * or processing, and that the argument exception is not wrapped.
     */
    @Test
    void processCommandRejectsNullStateBeforeDelegation() {
        TestEngine engine = new TestEngine(null, (currentState, currentCommand) -> {
            fail("doProcessCommand() must not be called");
            return null;
        }) {
            @Override
            protected void validateCommand(TestState currentState, TestCommand currentCommand) {
                fail("validateCommand() must not be called");
            }
        };

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> engine.processCommand(null, command)
        );

        assertEquals("State must not be null", exception.getMessage());
    }

    /**
     * Verifies that a null command is rejected before command validation
     * or processing, and that the argument exception is not wrapped.
     */
    @Test
    void processCommandRejectsNullCommandBeforeDelegation() {
        TestEngine engine = new TestEngine(null, (currentState, currentCommand) -> {
            fail("doProcessCommand() must not be called");
            return null;
        }) {
            @Override
            protected void validateCommand(TestState currentState, TestCommand currentCommand) {
                fail("validateCommand() must not be called");
            }
        };

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
        EngineCommandException failure = new EngineCommandException("Wrong turn");
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

    /**
     * Verifies that validation receives the original arguments
     * and runs exactly once before command processing.
     */
    @Test
    @SuppressWarnings("unchecked")
    void processCommandValidatesBeforeProcessing() {
        List<String> calls = new ArrayList<>();
        CommandResult<TestState> expectedResult = mock(CommandResult.class);
        TestEngine engine = new TestEngine(null, (currentState, currentCommand) -> {
            calls.add("process");
            return expectedResult;
        }) {
            @Override
            protected void validateCommand(TestState currentState, TestCommand currentCommand) {
                assertSame(state, currentState);
                assertSame(command, currentCommand);
                calls.add("validate");
            }
        };

        assertSame(expectedResult, engine.processCommand(state, command));
        assertEquals(List.of("validate", "process"), calls);
    }

    /**
     * Verifies that an EngineCommandException thrown during validation
     * propagates unchanged and prevents command processing.
     */
    @Test
    void processCommandPropagatesValidationRejectionWithoutProcessing() {
        EngineCommandException failure = new EngineCommandException("Wrong turn");
        TestEngine engine = new TestEngine(null, (currentState, currentCommand) -> {
            fail("doProcessCommand() must not be called");
            return null;
        }) {
            @Override
            protected void validateCommand(TestState currentState, TestCommand currentCommand) {
                throw failure;
            }
        };

        EngineCommandException exception = assertThrows(
                EngineCommandException.class,
                () -> engine.processCommand(state, command)
        );

        assertSame(failure, exception);
    }

    /**
     * Verifies that an EngineException thrown during validation propagates
     * as the same instance without wrapping, and command processing is skipped.
     */
    @Test
    void processCommandPropagatesValidationEngineExceptionWithoutProcessing() {
        EngineException failure = new EngineException("Validation failed");
        TestEngine engine = new TestEngine(null, (currentState, currentCommand) -> {
            fail("doProcessCommand() must not be called");
            return null;
        }) {
            @Override
            protected void validateCommand(TestState currentState, TestCommand currentCommand) {
                throw failure;
            }
        };

        EngineException exception = assertThrows(
                EngineException.class,
                () -> engine.processCommand(state, command)
        );

        assertSame(failure, exception);
    }

    /**
     * Verifies that a runtime exception other than EngineException thrown
     * during validation is wrapped in EngineException with the original cause
     * preserved, and command processing is skipped.
     */
    @Test
    void processCommandWrapsValidationRuntimeExceptionWithoutProcessing() {
        IllegalStateException failure =
                new IllegalStateException("Unexpected validation failure");
        TestEngine engine = new TestEngine(null, (currentState, currentCommand) -> {
            fail("doProcessCommand() must not be called");
            return null;
        }) {
            @Override
            protected void validateCommand(TestState currentState, TestCommand currentCommand) {
                throw failure;
            }
        };

        EngineException exception = assertThrows(
                EngineException.class,
                () -> engine.processCommand(state, command)
        );

        assertEquals("Failed to process command: TestCommand", exception.getMessage());
        assertSame(failure, exception.getCause());
    }

    /** Initial setup used to verify initialization argument delegation. */
    private static final class TestInitialSetup {
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
    private static class TestEngine
            extends AbstractGameEngine<TestState, TestCommand, TestInitialSetup> {

        private final Function<TestInitialSetup, CommandResult<TestState>> startHandler;
        private final BiFunction<TestState, TestCommand, CommandResult<TestState>>
                processHandler;

        private TestEngine(
                Function<TestInitialSetup, CommandResult<TestState>> startHandler,
                BiFunction<TestState, TestCommand, CommandResult<TestState>> processHandler
        ) {
            this.startHandler = startHandler;
            this.processHandler = processHandler;
        }

        @Override
        protected CommandResult<TestState> doStartGame(TestInitialSetup initialSetup) {
            return startHandler.apply(initialSetup);
        }

        @Override
        protected CommandResult<TestState> doProcessCommand(
                TestState state,
                TestCommand command
        ) {
            return processHandler.apply(state, command);
        }
    }
}
