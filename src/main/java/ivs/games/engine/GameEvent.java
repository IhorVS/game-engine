package ivs.games.engine;

/**
 * Represents an event that occurred during command processing in a game engine.
 *
 * <p>Events describe facts that occurred as a result of processing a command,
 * including changes to the game state and significant game actions.
 * Concrete game engines define their event types and determine when and in
 * what order events are created.
 *
 * <p>Events produced while processing a command are stored in the resulting
 * game state in the order of their occurrence. The event history of a game
 * session is managed separately by the session.
 */
@SuppressWarnings("unused")
public interface GameEvent {
}
