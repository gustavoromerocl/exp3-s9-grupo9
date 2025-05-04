package com.function;

import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.EventGridTrigger;
import com.microsoft.azure.functions.annotation.FunctionName;

public class FuncNotificacionActividadSospechozaEventGrid {

    private static final Gson GSON = new Gson();

    @FunctionName("FuncNotificacionActividadSospechozaEventGrid")
    public void run(
            @EventGridTrigger(name = "event") String payload,
            final ExecutionContext context) {

        Logger log = context.getLogger();
        log.info("FuncNotificacionActividadSospechozaEventGrid ejecutada");

        try {
            log.info(String.format("RAW payload: %s", payload));

            JsonObject ev = GSON.fromJson(payload, JsonObject.class);
            String eventType = ev.has("eventType") ? ev.get("eventType").getAsString()
                    : ev.has("type") ? ev.get("type").getAsString() : "unknown";

            if (!"suspicious_activity".equalsIgnoreCase(eventType)) {
                log.info(String.format("Evento ignorado (eventType=%s)", eventType));
                return;
            }

            if (ev.has("data")) {
                log.info(String.format("Actividad sospechosa detectada. Contenido del evento: %s",
                        ev.get("data").toString()));
            } else {
                log.warning("El evento no contiene campo 'data'");
            }

        } catch (Exception ex) {
            log.severe(String.format("Error al procesar evento en FuncNotificacionActividadSospechozaEventGrid: %s",
                    ex.getMessage()));
            throw new RuntimeException(ex);
        }
    }
}
