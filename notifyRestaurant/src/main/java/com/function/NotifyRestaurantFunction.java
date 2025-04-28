// src/main/java/com/function/NotifyRestaurantFunction.java
package com.function;

import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.EventGridTrigger;
import com.microsoft.azure.functions.annotation.FunctionName;

/**
 * Se dispara con eventos del topic "order-events".
 * Cuando recibe un evento "order_placed", registra en los logs que el
 * restaurante correspondiente fue notificado.
 */
public class NotifyRestaurantFunction {

    private static final Gson GSON = new Gson();

    @FunctionName("notifyRestaurant")
    public void run(
            @EventGridTrigger(name = "event") String payload,
            final ExecutionContext context) {

        Logger log = context.getLogger();
        log.info("notifyRestaurant ejecutada");

        try {
            // ---------- 1. Loguear el JSON crudo ----------
            log.info("RAW payload: " + payload);

            JsonObject ev = GSON.fromJson(payload, JsonObject.class);
            log.info("Parsed keys: " + ev.keySet());

            // ---------- 2. Obtener eventType / type ----------
            String eventType = null;
            if (ev.has("eventType") && !ev.get("eventType").isJsonNull()) {
                eventType = ev.get("eventType").getAsString(); // EventGridEvent
            } else if (ev.has("type") && !ev.get("type").isJsonNull()) {
                eventType = ev.get("type").getAsString(); // CloudEvent
            }

            if (!"order_placed".equals(eventType)) {
                log.info("Evento ignorado (eventType=" + eventType + ")");
                return; // salir sin error
            }

            // ---------- 3. Validar objeto data ----------
            if (!ev.has("data") || !ev.get("data").isJsonObject()) {
                log.warning("Campo 'data' ausente o no es objeto: " + ev);
                return;
            }
            JsonObject data = ev.getAsJsonObject("data");

            // ---------- 4. Extraer orderId y restaurantId ----------
            if (!data.has("orderId") || !data.has("restaurantId")) {
                log.warning("Faltan orderId o restaurantId en data: " + data);
                return;
            }
            String orderId = data.get("orderId").getAsString();
            String restaurantId = data.get("restaurantId").getAsString();

            // ---------- 5. Simular la notificación ----------
            log.info(String.format(
                    "🛎️  Restaurante %s notificado del pedido %s",
                    restaurantId, orderId));

        } catch (Exception ex) {
            // se relanza para que el runtime marque la invocación como Failed
            log.severe("Error al procesar evento: " + ex);
            throw ex;
        }
    }
}
