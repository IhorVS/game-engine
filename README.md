# game-engine

A Java library providing common contracts and a base implementation for game engines. Concrete games supply their own rules, states, commands and results.

The library supports board and card games, including games with multiple playing sides. It does not manage users, connections or game sessions.

## Requirements

- Java 21
- Maven for building the project
- Apache Commons Lang 3 for base engine validation

JUnit 5 and Mockito are used by the tests.

## Main components

| Type                                       | Responsibility                                                                                  |
|--------------------------------------------|-------------------------------------------------------------------------------------------------|
| `GameEngine<S, C extends Command>`         | Initializes a game and processes a single command                                               |
| `AbstractGameEngine<S, C extends Command>` | Validates arguments and returned results and handles runtime exceptions                         |
| `Command`                                  | Marks game-specific typed commands                                                              |
| `CommandResult<S>`                         | Exposes the resulting state through `getState()`; implementations may add game-specific data    |
| `EngineException`                          | Reports an engine execution failure                                                             |
| `EngineCommandException`                   | Reports a command rejected by game rules                                                        |

The two engine operations return the same result contract:

```java
CommandResult<S> startGame();
CommandResult<S> processCommand(S state, C command);
```

## Engine and session

An engine holds immutable configuration and thread-safe shared components, but does not retain the mutable state of an individual game. Multiple sessions using the same configuration may share one engine. A different configuration requires another engine instance.

A session owns the current state and the association between users and playing sides. It processes its commands sequentially, while separate sessions may call the shared engine concurrently. User authorization, connection handling and session termination belong to the application.

The engine knows playing sides or seats, not user accounts. For example, it checks whether a white move is legal and whether it is white's turn; the session checks which user may act as white.

## State and result ownership

`startGame()` creates an independent initial state. `processCommand()` processes the supplied state according to the concrete game's copying and mutation contract.

The state retained by a session must remain protected if processing fails. Either the session supplies an independent mutable working copy, or the engine derives a new state from an immutable input. The concrete implementation must document which approach it supports. Immutable components may be shared; mutable data must not accidentally link independent games or saved states.

The session accepts the resulting state only after successful processing. It may also record history at that point. An exception does not undo mutations automatically.

A result represents success only. Its `getState()` contract requires a non-null state. Correctness of the result contents, game status and any additional fields is the responsibility of game-specific logic. The abstract engine checks only that the result object itself is non-null; it does not call `getState()` to validate it.

## Validation and exceptions

Game-specific logic performs rule checks in the order required by the game, including checks before or after applying an action and determining whether the game has ended.

The base implementation provides the following behavior:

| Situation                                                    | Behavior                                                |
|--------------------------------------------------------------|---------------------------------------------------------|
| Null input state or command                                  | `NullPointerException`, before game-specific processing |
| Game-specific logic throws `EngineException` or a subclass   | The same exception propagates unchanged                 |
| Other `RuntimeException` during initialization or processing | Wrapped in `EngineException`, with the cause preserved  |
| Game-specific logic returns a null result                    | Validation failure wrapped in `EngineException`         |

Wrapped initialization failures use `Failed to start game`. Wrapped command failures use `Failed to process command: <simple class name>`.

Expected command rejection should explicitly use `EngineCommandException`. Arbitrary runtime exceptions are not automatically classified as rule violations. JVM `Error` instances are not caught by the base implementation.

## Implementing a game

1. Define a state containing all information needed by the rules, including turn order and relevant history.
2. Define immutable typed commands implementing `Command`, optionally through a game-specific command interface.
3. Implement `CommandResult<S>` with a valid resulting state and any additional game data.
4. Extend `AbstractGameEngine<S, C>` and implement `doStartGame()` and `doProcessCommand(...)`.
5. Supply immutable configuration at construction time and ensure shared dependencies are thread-safe.
6. Document input-state mutation and copying requirements.

A minimal example using immutable states:

```java
import ivs.games.engine.AbstractGameEngine;
import ivs.games.engine.Command;
import ivs.games.engine.CommandResult;
import ivs.games.engine.exception.EngineCommandException;

record CounterState(int value) {}
record Increment(int amount) implements Command {}

record CounterResult(CounterState state) implements CommandResult<CounterState> {
    @Override
    public CounterState getState() {
        return state;
    }
}

final class CounterEngine extends AbstractGameEngine<CounterState, Increment> {
    @Override
    protected CommandResult<CounterState> doStartGame() {
        return new CounterResult(new CounterState(0));
    }

    @Override
    protected CommandResult<CounterState> doProcessCommand(
            CounterState state, Increment command) {
        if (command.amount() <= 0) {
            throw new EngineCommandException("Increment must be positive");
        }
        return new CounterResult(
                new CounterState(Math.addExact(state.value(), command.amount())));
    }
}
```

This example demonstrates delegation and state isolation; actual games also determine game status according to their rules.

## Packages and dependencies

Common contracts reside in `ivs.games.engine`; exceptions reside in `ivs.games.engine.exception`.

Concrete engines may use packages such as `ivs.games.engine.chess` and `ivs.games.engine.checkers` and be distributed as separate projects. They depend on `game-engine` and the required accessories, such as `grid-board` or `cards-core`. The common engine library does not depend on those accessories or on concrete games.

## Build and use locally

```shell
mvn test
mvn install
```

The coordinates currently declared in `pom.xml` are:

```xml
<dependency>
    <groupId>ivs.game.engine</groupId>
    <artifactId>game-engine</artifactId>
    <version>1.0.0</version>
</dependency>
```

Install the project locally before using these coordinates in another local project. Availability in a public Maven repository is not assumed.

## License

This project is distributed under the [MIT License](LICENSE).
