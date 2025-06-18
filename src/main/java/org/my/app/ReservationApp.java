package org.my.app;

import io.muserver.MuServer;
import io.muserver.MuServerBuilder;
import io.muserver.rest.RestHandlerBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.my.app.handler.ReservationHandler;

/**
 * Reservation Application Main Class.
 */
@Slf4j
public class ReservationApp {

  private static Map<String, String> loadProperties() {
    try (InputStream input = ReservationApp.class.getClassLoader().getResourceAsStream("application.properties")) {
      if (input == null) {
        throw new RuntimeException("application.properties not found in the classpath");
      }
      var properties = new Properties();
      properties.load(input);
      return properties.entrySet()
          .stream()
          .collect(Collectors.toMap(
              entry -> (String) entry.getKey(),
              entry -> (String) entry.getValue()
          ));

    } catch (IOException e) {
      log.error("Failed to load application properties file", e);
      throw new RuntimeException("Failed to load properties file ", e);
    }
  }

  public static void main(String[] args) {
    // Start the MuServer with the ReservationController
    Map<String, String> configs = loadProperties();
    MuServer server = MuServerBuilder.muServer()
        .withHttpPort(Optional.ofNullable(configs.get("app.server.port")).map(Integer::parseInt).orElse(8080))
        .addHandler(RestHandlerBuilder.restHandler(new ReservationHandler()))
        .start();

    log.info("Server started at " + server.uri());

  }
}
