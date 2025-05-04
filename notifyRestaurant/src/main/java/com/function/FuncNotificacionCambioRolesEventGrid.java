package com.function;

import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.EventGridTrigger;
import com.microsoft.azure.functions.annotation.FunctionName;

public class FuncNotificacionCambioRolesEventGrid {

    private static final Gson GSON = new Gson();

    @FunctionName("FuncNotificacionCambioRolesEventGrid")
    public void run(
            @EventGridTrigger(name = "event") String payload,
            final ExecutionContext context) {

        Logger log = context.getLogger();
        log.info("FuncNotificacionCambioRolesEventGrid ejecutada");

        try {
            log.info(String.format("RAW payload: %s", payload));

            JsonObject ev = GSON.fromJson(payload, JsonObject.class);
            String eventType = ev.has("eventType") ? ev.get("eventType").getAsString()
                    : ev.has("type") ? ev.get("type").getAsString() : "unknown";

            if (!"role_change".equalsIgnoreCase(eventType)) {
                log.info(String.format("Evento ignorado (eventType=%s)", eventType));
                return;
            }

            if (ev.has("data")) {
                log.info(String.format("Cambio de rol detectado. Contenido del evento: %s", ev.get("data").toString()));
            } else {
                log.warning("El evento no contiene campo 'data'");
            }

        } catch (Exception ex) {
            log.severe(String.format("Error al procesar evento en FuncNotificacionCambioRolesEventGrid: %s",
                    ex.getMessage()));
            throw new RuntimeException(ex);
        }
    }
}
