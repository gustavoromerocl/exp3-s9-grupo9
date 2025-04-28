// src/test/java/com/foodapp/functions/NotifyRestaurantFunctionTest.java
package com.function;

import com.microsoft.azure.functions.ExecutionContext;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

class NotifyRestaurantFunctionTest {

    @Test
    void testNotifyRestaurant_ok() {
        /* ---------- Evento de ejemplo ---------- */
        String eventJson = "{"
                + "\"id\":\"1\","
                + "\"type\":\"order_placed\","
                + "\"data\":{"
                + "\"orderId\":\"o-1\","
                + "\"restaurantId\":\"r-123\""
                + "}"
                + "}";

        /* ---------- Contexto mock ---------- */
        ExecutionContext ctx = mock(ExecutionContext.class);
        when(ctx.getLogger()).thenReturn(Logger.getLogger("test"));

        /* ---------- Ejecución y verificación ---------- */
        NotifyRestaurantFunction func = new NotifyRestaurantFunction();
        assertDoesNotThrow(() -> func.run(eventJson, ctx));
    }
}
