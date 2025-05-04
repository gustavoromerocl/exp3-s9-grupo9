package com.function;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

public class SendTestEventFunction {

  private static final String EVENT_GRID_TOPIC_ENDPOINT = System.getenv("EVENT_GRID_TOPIC_ENDPOINT");
  private static final String EVENT_GRID_KEY = System.getenv("EVENT_GRID_TOPIC_KEY");

  @FunctionName("SendTestEvent")
  public HttpResponseMessage run(
      @HttpTrigger(name = "req", methods = {
          HttpMethod.POST }, route = "send-event", authLevel = AuthorizationLevel.FUNCTION) HttpRequestMessage<Optional<String>> request,
      final ExecutionContext context) {

    try {
      String body = request.getBody().orElse("{}");
      String eventType = request.getQueryParameters().getOrDefault("eventType", "suspicious_activity");

      context.getLogger().info(String.format("Enviando evento tipo: %s", eventType));

      String eventId = UUID.randomUUID().toString();
      String timestamp = ZonedDateTime.now(ZoneOffset.UTC)
          .format(DateTimeFormatter.ISO_INSTANT);

      String eventPayload = String.format(
          "[{\"id\":\"%s\"," +
              "\"eventType\":\"%s\"," +
              "\"subject\":\"/example/subject\"," +
              "\"eventTime\":\"%s\"," +
              "\"data\":%s," + // Inyectamos el body como objeto JSON
              "\"dataVersion\":\"1.0\"}]",
          eventId, eventType, timestamp, body);

      HttpRequest httpRequest = HttpRequest.newBuilder()
          .uri(URI.create(EVENT_GRID_TOPIC_ENDPOINT))
          .header("Content-Type", "application/json")
          .header("aeg-sas-key", EVENT_GRID_KEY)
          .POST(HttpRequest.BodyPublishers.ofString(eventPayload))
          .build();

      HttpClient client = HttpClient.newHttpClient();
      HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

      context.getLogger().info(String.format("Respuesta de Event Grid: %d - %s",
          response.statusCode(), response.body()));

      return request.createResponseBuilder(HttpStatus.OK)
          .body(String.format("Evento enviado con éxito. Tipo: %s", eventType))
          .build();

    } catch (Exception e) {
      context.getLogger().severe(String.format("Error al enviar evento: %s", e.getMessage()));
      return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Error al enviar evento: " + e.getMessage())
          .build();
    }
  }
}
