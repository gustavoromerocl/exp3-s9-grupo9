package com.function;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.messaging.eventgrid.EventGridPublisherClient;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;

/**
 * Test unitario para PlaceOrderFunction SIN modificar la clase.
 * 1. Crea la función con el constructor por defecto
 * 2. Sustituye el campo 'publisher' por un mock mediante reflexión
 * 3. Verifica que responde 200 OK y que se invoca sendEvent una vez
 */
public class PlaceOrderFunctionTest {

    @Test
    void testPlaceOrder_OK() throws Exception {

        /* -------- request mock -------- */
        @SuppressWarnings("unchecked")
        HttpRequestMessage<String> req = mock(HttpRequestMessage.class);

        String jsonBody = "{\"orderId\":\"o-1\",\"restaurantId\":\"r-123\",\"items\":[\"pizza\"]}";
        when(req.getBody()).thenReturn(jsonBody);
        when(req.getQueryParameters()).thenReturn(Collections.emptyMap());

        when(req.createResponseBuilder(any(HttpStatus.class)))
                .thenAnswer(inv -> new HttpResponseMessageMock.HttpResponseMessageBuilderMock()
                        .status(inv.getArgument(0)));

        /* -------- context mock -------- */
        ExecutionContext ctx = mock(ExecutionContext.class);
        when(ctx.getLogger()).thenReturn(Logger.getLogger("test"));

        /* -------- publisher mock -------- */
        @SuppressWarnings("unchecked")
        EventGridPublisherClient<EventGridEvent> publisherMock = mock(EventGridPublisherClient.class);

        /* -------- crear función y reemplazar el publisher -------- */
        PlaceOrderFunction func = new PlaceOrderFunction(); // ctor sin argumentos

        Field pubField = PlaceOrderFunction.class.getDeclaredField("publisher");
        pubField.setAccessible(true); // omite visibilidad
        pubField.set(func, publisherMock); // inyecta el mock

        /* -------- ejecutar y verificar -------- */
        HttpResponseMessage resp = func.run(req, ctx);

        assertEquals(HttpStatus.OK, resp.getStatus());
        verify(publisherMock, times(1)).sendEvent(any(EventGridEvent.class));
    }
}
