# game-engine

A Java library providing common contracts and a base implementation for game engines. Concrete games supply their own rules, states and commands.

The library supports board and card games, including games with multiple playing sides. It does not manage users, connections or game sessions.

## Requirements

- Java 21
- Maven 3.9.9 (or use the included Maven Wrapper)
- Apache Commons Lang 3 for base engine validation

JUnit 5 is used by the tests.

## Main components

| Type                                          | Responsibility                                                                               |
| --------------------------------------------- | -------------------------------------------------------------------------------------------- |
| `GameEngine<S, C extends Command, I>`         | Initializes a game and processes a single command                                            |
| `AbstractGameEngine<S, C extends Command, I>` | Validates arguments and returned states and handles runtime exceptions                       |
| `Command`                                     | Marks game-specific typed commands                                                           |
| `EngineException`                             | Reports an engine execution failure                                                          |
| `EngineCommandException`                      | Reports a command rejected by game rules                                                     |

The two engine operations return the game state directly:

```java
S startGame(I initialSetup);
S processCommand(S state, C command);
```

## Engine and session

An engine holds immutable configuration and thread-safe shared components, but does not retain the mutable state of an individual game. Multiple sessions using the same rule configuration may share one engine, even when their initial setups differ. A different rule configuration requires another engine instance. Initial setup is supplied per call, not stored in the engine configuration.

A session owns the current state and the association between users and playing sides. It processes its commands sequentially, while separate sessions may call the shared engine concurrently. User authorization, connection handling and session termination belong to the application.

The engine knows playing sides or seats, not user accounts. For example, it checks whether a white move is legal and whether it is white's turn; the session checks which user may act as white.

## State ownership

`startGame(initialSetup)` validates the supplied initial setup and creates an independent immutable state. The session selects standard or custom initial data; the engine does not provide a no-argument start method or choose a default setup. Concrete game libraries may provide standard setup objects.

`processCommand()` derives a resulting immutable state without modifying the supplied state or command. Immutability must include nested data and collections. Mutable working data must be isolated from saved snapshots; immutable components may be shared.

The session accepts the resulting state only after successful processing and may retain previous snapshots in game history without copying them. On failure, the original snapshot remains unchanged. This guarantee comes from state isolation and immutability, not automatic rollback by exceptions.

A returned state represents success only. Game-specific logic is responsible for the correctness of the state, including game status and any additional data. The abstract engine checks only that the returned state is non-null. Failures are reported through exceptions.

## Validation and exceptions

After null argument checks, the abstract engine calls `validateCommand(state, command)` before `doProcessCommand(state, command)`. This protected hook has an empty default implementation and may be overridden for preliminary rule checks. It must not modify the state or command. If it throws, command processing is skipped.

Checks requiring a candidate resulting state remain in `doProcessCommand(...)`. Game-specific initialization validates the initial setup in `doStartGame(initialSetup)`. Concrete games determine game status and validate resulting states.

The base implementation provides the following behavior:

| Situation                                                                        | Behavior                                                |
| -------------------------------------------------------------------------------- | ------------------------------------------------------- |
| Null initial setup, state or command                                             | `NullPointerException`, before game-specific processing |
| Game-specific logic throws `EngineException` or a subclass                       | The same exception propagates unchanged                 |
| Other `RuntimeException` during initialization, command validation or processing | Wrapped in `EngineException`, with the cause preserved  |
| Game-specific logic returns a null state                                         | Validation failure wrapped in `EngineException`         |

Wrapped initialization failures use `Failed to start game`. Wrapped command failures use `Failed to process command: <simple class name>`.

Expected command rejection should explicitly use `EngineCommandException`. Arbitrary runtime exceptions are not automatically classified as rule violations. JVM `Error` instances are not caught by the base implementation.

## Implementing a game

1. Define an immutable state containing all information needed by the rules, including turn order and relevant history.
2. Define an initial setup type describing the inputs needed to initialize a game.
3. Define immutable typed commands implementing `Command`, optionally through a game-specific command interface.
4. Extend `AbstractGameEngine<S, C, I>` and implement `doStartGame(I initialSetup)` and `doProcessCommand(S state, C command)`.
5. Optionally override `validateCommand(S state, C command)` for preliminary checks.
6. Supply immutable rule configuration at construction time, ensure shared dependencies are thread-safe and isolate mutable working data from immutable snapshots.

A minimal example using immutable states:

```java
import ivs.games.engine.AbstractGameEngine;
import ivs.games.engine.Command;
import ivs.games.engine.exception.EngineCommandException;

record CounterInitialSetup(int value) {}
record CounterState(int value) {}
record Increment(int amount) implements Command {}

final class CounterEngine extends AbstractGameEngine<CounterState, Increment, CounterInitialSetup> {
    @Override
    protected CounterState doStartGame(CounterInitialSetup initialSetup) {
        return new CounterState(initialSetup.value());
    }

    @Override
    protected void validateCommand(CounterState state, Increment command) {
        if (command.amount() <= 0) {
            throw new EngineCommandException("Increment must be positive");
        }
    }

    @Override
    protected CounterState doProcessCommand(CounterState state, Increment command) {
        return new CounterState(Math.addExact(state.value(), command.amount()));
    }
}
```

Both starts use the same engine instance:

```java
CounterEngine engine = new CounterEngine();
CounterState standard = engine.startGame(new CounterInitialSetup(0));
CounterState custom = engine.startGame(new CounterInitialSetup(10));
CounterState next = engine.processCommand(standard, new Increment(2));
```

This example demonstrates initialization, preliminary validation and state isolation; actual games also determine game status according to their rules.

## Packages and dependencies

Common contracts reside in `ivs.games.engine`; exceptions reside in `ivs.games.engine.exception`.

Concrete engines may use packages such as `ivs.games.engine.chess` and `ivs.games.engine.checkers` and be distributed as separate projects. They depend on `game-engine` and the required accessories, such as `grid-board` or `cards-core`. The common engine library does not depend on those accessories or on concrete games.

## Build and use locally

The Maven Wrapper downloads the configured Maven version if it is not already
available. On macOS or Linux:

```shell
./mvnw test
./mvnw install
```

On Windows:

```shell
mvnw.cmd test
mvnw.cmd install
```

GitHub Actions runs `verify` on JDK 21 for pushes and pull requests.

The coordinates currently declared in `pom.xml` are:

```xml
<dependency>
    <groupId>ivs.games.engine</groupId>
    <artifactId>game-engine</artifactId>
    <version>1.0.0</version>
</dependency>
```

Install the project locally before using these coordinates in another local project. Availability in a public Maven repository is not assumed.

## License

This project is distributed under the [MIT License](LICENSE).
