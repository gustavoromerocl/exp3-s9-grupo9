// src/main/java/com/function/PlaceOrderFunction.java
package com.function;

import java.util.UUID;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.messaging.eventgrid.EventGridPublisherClient;
import com.azure.messaging.eventgrid.EventGridPublisherClientBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

/**
 * Recibe un pedido vía HTTP POST y publica un evento "order_placed"
 * con el orderId tanto en el subject como en el cuerpo (data.orderId).
 */
public class PlaceOrderFunction {

        @FunctionName("placeOrder")
        public HttpResponseMessage run(
                        @HttpTrigger(name = "req", methods = {
                                        HttpMethod.POST }, authLevel = AuthorizationLevel.FUNCTION) HttpRequestMessage<String> request,
                        final ExecutionContext context) {

                /* -- Topic Config -- */
                String endpoint = System.getenv("EVENT_GRID_TOPIC_ENDPOINT");
                String key = System.getenv("EVENT_GRID_TOPIC_KEY");

                String body = request.getBody();
                if (body == null || body.isBlank()) {
                        return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                                        .body("El body no puede estar vacío; envía el pedido en JSON.")
                                        .build();
                }

                try {
                        /* 1. Construir el publisher */
                        EventGridPublisherClient<EventGridEvent> client = new EventGridPublisherClientBuilder()
                                        .endpoint(endpoint)
                                        .credential(new AzureKeyCredential(key))
                                        .buildEventGridEventPublisherClient();

                        /* 2. Generar orderId y agregarlo el body */
                        String orderId = UUID.randomUUID().toString();

                        JsonObject dataObj = JsonParser.parseString(body).getAsJsonObject();
                        dataObj.addProperty("orderId", orderId);
                        String enrichedBody = dataObj.toString();

                        /* 3. Crear el evento */
                        EventGridEvent event = new EventGridEvent(
                                        "/orders/" + orderId, // subject
                                        "order_placed", // eventType
                                        BinaryData.fromString(enrichedBody),
                                        "1.0");

                        /* 4. Publicar */
                        client.sendEvent(event);
                        context.getLogger().info("order_placed enviado, orderId=" + orderId);

                        return request.createResponseBuilder(HttpStatus.OK)
                                        .body("Pedido recibido. ID=" + orderId)
                                        .build();

                } catch (Exception ex) {
                        context.getLogger().severe("Error al publicar evento: " + ex.getMessage());
                        return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body("Error al publicar evento: " + ex.getMessage())
                                        .build();
                }
        }
}
