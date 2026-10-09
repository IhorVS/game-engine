package ivs.games.engine.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EngineCommandExceptionTest {

    public static final String NOT_PERMITTED = "Command is not permitted";

    @Test
    void validateAcceptsTrueExpression() {
        assertDoesNotThrow(() -> EngineCommandException.validate(true, NOT_PERMITTED));
    }

    @Test
    void validateRejectsFalseExpression() {
        var e = assertThrows(EngineCommandException.class, () -> EngineCommandException.validate(false, NOT_PERMITTED));
        assertEquals(NOT_PERMITTED, e.getMessage());
    }
}
