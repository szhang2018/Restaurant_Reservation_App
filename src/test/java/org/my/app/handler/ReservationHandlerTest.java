package org.my.app.handler;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.muserver.MuServer;
import io.muserver.MuServerBuilder;
import io.muserver.rest.RestHandlerBuilder;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ReservationControllerTest {

  private MuServer server;
  private String baseUrl;
  private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

  @BeforeAll
  public void startServer() {
    server = MuServerBuilder.muServer()
        .withHttpPort(8081)
        .addHandler(RestHandlerBuilder.restHandler(new ReservationHandler()))
        .start();
    baseUrl = server.uri().toString();
  }

  @AfterAll
  public void stopServer() {
    server.stop();
  }

  @Test
  void testCreateAndFetchBooking() throws Exception {
    String bookingJson = mapper.writeValueAsString(new HashMap<String, Object>() {{
      put("customerName", "John Doe");
      put("tableSize", 3);
      put("bookingDate", LocalDate.now().toString());
      put("bookingTime", LocalTime.of(12, 0).toString());
    }});

    HttpURLConnection postConn = (HttpURLConnection) new URL(baseUrl + "/reservations").openConnection();
    postConn.setRequestMethod("POST");
    postConn.setDoOutput(true);
    postConn.setRequestProperty("Content-Type", "application/json");
    postConn.getOutputStream().write(bookingJson.getBytes(StandardCharsets.UTF_8));
    assertEquals(201, postConn.getResponseCode());

    String date = LocalDate.now().toString();
    HttpURLConnection getConn = (HttpURLConnection) new URL(baseUrl + "/reservations/" + date).openConnection();
    getConn.setRequestMethod("GET");
    assertEquals(200, getConn.getResponseCode());

    String response = new BufferedReader(new InputStreamReader(getConn.getInputStream()))
        .lines()
        .reduce("", (a, b) -> a + b);

    assertTrue(response.contains("John Doe"));
    assertTrue(response.contains("12:00"));
  }
}
