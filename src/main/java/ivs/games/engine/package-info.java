/**
 * Provides common contracts and a base implementation for game engines.
 *
 * <p>An engine initializes games from supplied initial setups and processes
 * individual typed commands.
 * Concrete games define their states, rules and commands. Engine operations return
 * game states directly. State contents and game status are validated by game-specific logic.
 *
 * <p>Engines must be thread-safe and must not retain mutable state belonging
 * to an individual game. Sessions own current game states and process their
 * commands sequentially. Sessions and user account management are outside
 * the scope of this package.
 *
 * <p>Game states are immutable snapshots. Processing must not modify supplied
 * states, commands or initial setups. Immutable data may be shared between
 * snapshots; mutable working data must remain isolated from saved states.
 *
 * <p>The abstract engine provides an optional preliminary command validation
 * hook. Failed validation prevents processing. Concrete game logic validates
 * initial setups and any rule checks requiring a candidate resulting state.
 *
 * <p>One engine may serve sessions with the same rule configuration and different
 * initial setups. Sessions choose the setup and retain successful states.
 *
 * @see ivs.games.engine.GameEngine
 * @see ivs.games.engine.AbstractGameEngine
 * @see ivs.games.engine.Command
 */
package ivs.games.engine;
