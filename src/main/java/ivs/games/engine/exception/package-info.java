/**
 * Provides runtime exceptions for game engine failures and command rejection.
 *
 * <p>{@link ivs.games.engine.exception.EngineCommandException} identifies
 * commands rejected by game rules. Its superclass,
 * {@link ivs.games.engine.exception.EngineException}, reports engine failures
 * and preserves the cause of wrapped runtime exceptions.
 *
 * <p>The abstract engine propagates existing engine exceptions unchanged.
 * Other runtime exceptions from initialization, command validation,
 * processing or result validation
 * are wrapped in an engine exception. Null initial setups, states and commands are checked before
 * delegation and reported directly as {@link java.lang.NullPointerException}.
 *
 * <p>Sessions decide how to handle these failures. Supplied immutable states must remain
 * unchanged; exceptions do not implement rollback of mutable working data.
 */
package ivs.games.engine.exception;
