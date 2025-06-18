package org.my.app.handler;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import lombok.extern.slf4j.Slf4j;
import org.my.app.service.ReservationService;

@Path("/reservations")
@Slf4j
public class ReservationHandler {
  private final ReservationService reservationService = new ReservationService();

  @POST
  @Consumes("application/json")
  @Produces("application/json")
  public Response createBooking(String request)  {
      log.info("Received booking request: {}", request);
      var bookingResponse = reservationService.createBooking(request);
      return Response.status(bookingResponse.status()).entity(bookingResponse.message()).build();
  }

  @GET
  @Path("/{date}")
  @Produces("application/json")
  public Response getBookingsByDate(@PathParam("date") String dateStr) {
      log.info("Fetching bookings for date: {}", dateStr);
      var getBookingByDateResponse = reservationService.getBookingsByDate(dateStr);
      return Response.status(getBookingByDateResponse.status()).entity(getBookingByDateResponse.message()).build();
  }
}
