/**
 * Provides common contracts and a base implementation for game engines.
 *
 * <p>An engine initializes games and processes individual typed commands.
 * Concrete games define their states, rules, commands and successful results.
 * Result contents and game status are validated by game-specific logic.
 *
 * <p>Engines must be thread-safe and must not retain mutable state belonging
 * to an individual game. Sessions own current game states and process their
 * commands sequentially. Sessions and user account management are outside
 * the scope of this package.
 *
 * <p>State copying and mutation are defined by each concrete game's contract.
 * Processing failures must not affect the state retained by the session.
 *
 * @see ivs.games.engine.GameEngine
 * @see ivs.games.engine.AbstractGameEngine
 * @see ivs.games.engine.Command
 * @see ivs.games.engine.CommandResult
 */
package ivs.games.engine;
