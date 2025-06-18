package org.my.app.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.ws.rs.core.Response.Status;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.my.app.model.Booking;
import org.my.app.model.Response;
import org.my.app.model.Table;

@Slf4j
public class ReservationService {
  private static final ObjectMapper mapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  private final Map<LocalDate, List<Booking>> bookingsByDate = new ConcurrentHashMap<>();
  private final List<LocalTime> validTimeSlots = List.of(
      LocalTime.of(10, 0),
      LocalTime.of(12, 0),
      LocalTime.of(14, 0),
      LocalTime.of(16, 0),
      LocalTime.of(18, 0),
      LocalTime.of(20, 0)
  );
  private List<Table> tables;
  public ReservationService()  {
    // write code to load tableConfiguration.json into a list of Table objects
    try {
      var jsonInputStream = ReservationService.class.getClassLoader().getResourceAsStream("tableConfiguration.json");
      tables = Arrays.asList(mapper.readValue(jsonInputStream, Table[].class));
      validTimeSlots.forEach( timeSlot ->{

      });

    } catch (IOException e) {
      log.error("Error loading table configuration", e);
      throw new RuntimeException("Failed to load table configuration", e);
    }
  }

  public Response createBooking(String bookingRequest) {
    try{

      var booking = mapper.readValue(bookingRequest, Booking.class);
      if (!isValidBooking(booking)) {
        return new Response(Status.BAD_REQUEST.getStatusCode(),"Invalid booking or non available booking. Must be a 2-hour interval starting at 10:00 to 20:00");
      }

      bookingsByDate.computeIfAbsent(booking.bookingDate(), k -> new ArrayList<>()).add(booking);
      return new Response(Status.CREATED.getStatusCode(), "Booking created");

    } catch (Exception e) {
      log.error("Error logging booking request", e);
      return new Response(Status.BAD_REQUEST.getStatusCode(), "Invalid booking request: " + e.getMessage());
    }
  }

  public Response getBookingsByDate(String dateStr) {
    try{
      log.info("Fetching bookings for date: {}", dateStr);
      var date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
      return new Response(Status.OK.getStatusCode(),
          mapper.writeValueAsString(bookingsByDate.getOrDefault(date, Collections.emptyList())));
    } catch (Exception e) {
      log.error("Error fetching bookings for date: {}", dateStr, e);
      return new Response(Status.BAD_REQUEST.getStatusCode(),
          "Error fetching bookings for date: " + dateStr + ". " + e.getMessage() + "\n"
              + "Please ensure the date is in the format YYYY-MM-DD and is a valid date.");
    }
  }

  private boolean isValidBooking(Booking bookingRequest) {
    //if no matched table size
    if (tables.stream().noneMatch(table -> table.capacity() == bookingRequest.tableSize())) {
      return false;
    }

    //if no matched timeslots
    if (!validTimeSlots.contains(bookingRequest.bookingTime())) {
      return false;
    }

    //if matched table size and time slot, check if the booking can be made
    var bookings = bookingsByDate.getOrDefault(bookingRequest.bookingDate(), Collections.emptyList());
    var matchedTableNumber = bookings.stream().filter(booking-> booking.bookingTime().equals(bookingRequest.bookingTime()) && booking.tableSize() == bookingRequest.tableSize()).count();
    if (matchedTableNumber >= tables.stream().filter(table -> table.capacity() == bookingRequest.tableSize()).count()) {
      return false;
    }
    return true;
  }
}
